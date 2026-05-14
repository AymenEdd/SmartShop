import json

import redis
from sqlalchemy import and_, func, select
from sqlalchemy.orm import Session

from app.core.config import settings
from app.models.order import Order, OrderItem
from app.models.product import Product


redis_client = redis.Redis.from_url(settings.redis_url, decode_responses=True)


def _hydrate_products(db: Session, ids: list[int]) -> list[Product]:
    products: list[Product] = []
    for pid in ids:
        product = db.get(Product, pid)
        if product:
            products.append(product)
    return products


def _filter_in_stock(query):
    return query.filter(Product.stock > 0)


def top_trending_products(db: Session, limit: int = 6) -> list[Product]:
    cache_key = f"trending:{limit}"
    cached = redis_client.get(cache_key)
    if cached:
        return _hydrate_products(db, json.loads(cached))

    rows = (
        db.query(OrderItem.product_id, func.sum(OrderItem.quantity).label("score"))
        .join(Order, Order.id == OrderItem.order_id)
        .join(Product, Product.id == OrderItem.product_id)
        .filter(Product.stock > 0)
        .group_by(OrderItem.product_id)
        .order_by(func.sum(OrderItem.quantity).desc())
        .limit(limit)
        .all()
    )
    ids = [row[0] for row in rows]
    products = _hydrate_products(db, ids)
    redis_client.setex(cache_key, 300, json.dumps(ids))
    return products


def recent_arrivals(db: Session, limit: int = 6) -> list[Product]:
    return (
        db.query(Product)
        .filter(Product.stock > 0)
        .order_by(Product.created_at.desc())
        .limit(limit)
        .all()
    )


def similar_products(db: Session, product_id: int, limit: int = 6) -> list[Product]:
    cache_key = f"similar:{product_id}:{limit}"
    cached = redis_client.get(cache_key)
    if cached:
        return _hydrate_products(db, json.loads(cached))

    target = db.get(Product, product_id)
    if not target:
        return []

    products = (
        db.query(Product)
        .filter(and_(Product.id != product_id, Product.category == target.category, Product.stock > 0))
        .order_by(func.abs(Product.price - target.price))
        .limit(limit)
        .all()
    )
    redis_client.setex(cache_key, 300, json.dumps([p.id for p in products]))
    return products


def personalized_recommendations(db: Session, user_id: int, limit: int = 6) -> list[Product]:
    cache_key = f"personalized:{user_id}:{limit}"
    cached = redis_client.get(cache_key)
    if cached:
        return _hydrate_products(db, json.loads(cached))

    category_scores = (
        db.query(
            Product.category,
            func.count(OrderItem.id).label("count"),
            func.max(Order.created_at).label("last_order")
        )
        .join(OrderItem, Product.id == OrderItem.product_id)
        .join(Order, Order.id == OrderItem.order_id)
        .filter(Order.user_id == user_id)
        .group_by(Product.category)
        .order_by(func.count(OrderItem.id).desc(), func.max(Order.created_at).desc())
        .all()
    )

    if not category_scores:
        products = recent_arrivals(db, limit)
    else:
        favorite_categories = [row[0] for row in category_scores[:3]]
        bought_ids_query = (
            db.query(OrderItem.product_id)
            .join(Order, Order.id == OrderItem.order_id)
            .filter(Order.user_id == user_id)
            .subquery()
        )
        products = (
            db.query(Product)
            .filter(Product.category.in_(favorite_categories), Product.stock > 0)
            .filter(~Product.id.in_(select(bought_ids_query.c.product_id)))
            .order_by(Product.created_at.desc())
            .all()
        )
        products.sort(key=lambda p: favorite_categories.index(p.category) if p.category in favorite_categories else len(favorite_categories))
        products = products[:limit]

    if len(products) < limit:
        seen_ids = {p.id for p in products}
        extras = [p for p in top_trending_products(db, limit * 2) if p.id not in seen_ids]
        products.extend(extras[: limit - len(products)])

    redis_client.setex(cache_key, 300, json.dumps([p.id for p in products]))
    return products


def advanced_collaborative_recommendations(db: Session, user_id: int, limit: int = 6) -> list[Product]:
    cache_key = f"advanced_cf:{user_id}:{limit}"
    cached = redis_client.get(cache_key)
    if cached:
        return _hydrate_products(db, json.loads(cached))

    user_product_ids = [
        pid
        for (pid,) in (
            db.query(OrderItem.product_id)
            .join(Order, Order.id == OrderItem.order_id)
            .filter(Order.user_id == user_id)
            .distinct()
            .all()
        )
    ]
    if not user_product_ids:
        return personalized_recommendations(db, user_id, limit)

    sibling_order_ids = (
        db.query(OrderItem.order_id)
        .filter(OrderItem.product_id.in_(user_product_ids))
        .distinct()
        .subquery()
    )
    candidate_rows = (
        db.query(OrderItem.product_id, func.count(OrderItem.id).label("score"))
        .filter(OrderItem.order_id.in_(select(sibling_order_ids.c.order_id)))
        .filter(~OrderItem.product_id.in_(user_product_ids))
        .group_by(OrderItem.product_id)
        .order_by(func.count(OrderItem.id).desc())
        .limit(limit)
        .all()
    )
    ids = [row[0] for row in candidate_rows]
    products = _hydrate_products(db, ids)
    redis_client.setex(cache_key, 300, json.dumps(ids))
    return products


def home_recommendations(db: Session, user_id: int | None = None, limit: int = 6) -> list[Product]:
    if user_id is None:
        trending = top_trending_products(db, limit)
        if len(trending) < limit:
            trending += [p for p in recent_arrivals(db, limit) if p.id not in {prod.id for prod in trending}]
        return trending[:limit]

    recommendations = advanced_collaborative_recommendations(db, user_id, limit)
    seen_ids = {p.id for p in recommendations}

    if len(recommendations) < limit:
        fallback = personalized_recommendations(db, user_id, limit * 2)
        for product in fallback:
            if len(recommendations) >= limit:
                break
            if product.id not in seen_ids:
                recommendations.append(product)
                seen_ids.add(product.id)

    if len(recommendations) < limit:
        for product in top_trending_products(db, limit * 2):
            if len(recommendations) >= limit:
                break
            if product.id not in seen_ids:
                recommendations.append(product)
                seen_ids.add(product.id)

    return recommendations[:limit]
