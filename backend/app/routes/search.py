from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session
from typing import List, Optional

from app.db import get_db
from app.dependencies import get_current_user_optional
from app.models.user import User
from app.schemas.search import SearchRequest, SearchResponse
from app.schemas.product import ProductOut
from app.services.rag_service import rag_service
from app.services.recommender import personalized_recommendations


router = APIRouter(prefix="/search", tags=["search"])


@router.get("/suggestions", response_model=List[str])
def get_suggestions(
    q: str = Query(..., min_length=1, max_length=100),
    limit: int = Query(5, ge=1, le=20)
):
    """
    Get search suggestions based on partial query.
    Returns matching product names from database.
    """
    from app.models.product import Product
    from app.db import SessionLocal
    
    db = SessionLocal()
    try:
        suggestions = (
            db.query(Product.name)
            .filter(Product.name.ilike(f"%{q}%"))
            .distinct()
            .limit(limit)
            .all()
        )
        return [s[0] for s in suggestions]
    finally:
        db.close()


@router.post("/", response_model=SearchResponse)
def semantic_search(
    request: SearchRequest,
    db: Session = Depends(get_db),
    current_user: Optional[User] = Depends(get_current_user_optional)
):
    """
    Unified semantic search with AI-powered recommendations.
    - Uses RAG for semantic matching
    - Applies filters (price, category, rating) server-side
    - Includes personalized recommendations for authenticated users
    - NO client-side fallback needed
    """
    # Get semantic search results from RAG service (product IDs)
    product_ids = rag_service.query_product_ids(
        question=request.query,
        top_k=20  # Get more results for filtering
    )

    # Fetch products from database and apply filters
    products = []
    if product_ids:
        from app.models.product import Product
        db_products = db.query(Product).filter(Product.id.in_(product_ids)).all()

        # Sort by the order returned by RAG (semantic relevance)
        product_dict = {p.id: p for p in db_products}
        for pid in product_ids:
            if pid in product_dict:
                product = product_dict[pid]

                # Apply filters (all server-side - NO CLIENT FILTERING)
                if request.min_price is not None and product.price < request.min_price:
                    continue
                if request.max_price is not None and product.price > request.max_price:
                    continue
                if request.category and product.category.lower() != request.category.lower():
                    continue
                if request.min_rating is not None and getattr(product, 'rating', 0) < request.min_rating:
                    continue

                products.append(product)

    # Get personalized recommendations if user is authenticated
    suggestions = []
    if current_user:
        try:
            recommended_products = personalized_recommendations(db, current_user.id)
            suggestions = [f"Based on your history: {p.name}" for p in recommended_products[:3]]
        except Exception:
            # If recommendations fail, continue without them
            pass

    # If no semantic results, fall back to basic category/price filtering
    if not products and (request.category or request.min_price or request.max_price):
        from app.models.product import Product
        query = db.query(Product)

        if request.category:
            query = query.filter(Product.category.ilike(f"%{request.category}%"))
        if request.min_price is not None:
            query = query.filter(Product.price >= request.min_price)
        if request.max_price is not None:
            query = query.filter(Product.price <= request.max_price)

        products = query.limit(20).all()

    return SearchResponse(
        products=[ProductOut.model_validate(p) for p in products[:10]],  # Limit to 10 results
        suggestions=suggestions
    )