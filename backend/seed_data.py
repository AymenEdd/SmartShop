from sqlalchemy import inspect, text
from urllib.parse import quote_plus

from app.db import SessionLocal, engine
from app.core.config import settings
from app.core.security import hash_password
from app.models.product import Product
from app.models.user import User


SAMPLE_PRODUCTS = [
    {
        "name": "Blazer Milano Wool",
        "description": "Blazer en laine structuree, coupe premium pour tenue business ou soiree.",
        "category": "Vetements",
        "price": 349.99,
        "stock": 18,
        "image_urls": [
            "https://placehold.co/900x600/E6F1FB/185FA5/png?text=Blazer+Milano+Wool",
            "https://placehold.co/900x600/F0F6FF/042C53/png?text=Blazer+Detail",
        ],
    },
    {
        "name": "Robe Satin Aurelia",
        "description": "Robe longue en satin fluide avec finition elegante et silhouette raffinee.",
        "category": "Vetements",
        "price": 229.99,
        "stock": 14,
        "image_urls": [
            "https://placehold.co/900x600/FCEBEB/A32D2D/png?text=Robe+Satin+Aurelia",
        ],
    },
    {
        "name": "Chemise Oxford Prestige",
        "description": "Chemise oxford coton premium, col net et boutons nacres.",
        "category": "Vetements",
        "price": 119.99,
        "stock": 32,
        "image_urls": [
            "https://placehold.co/900x600/EAF3DE/3B6D11/png?text=Chemise+Oxford+Prestige",
        ],
    },
    {
        "name": "Manteau Cachemire Noir",
        "description": "Manteau long melange cachemire, coupe droite et doublure satin.",
        "category": "Vetements",
        "price": 699.99,
        "stock": 9,
        "image_urls": [
            "https://placehold.co/900x600/042C53/FFFFFF/png?text=Manteau+Cachemire",
        ],
    },
    {
        "name": "Montre Heritage Automatic",
        "description": "Montre automatique acier, cadran bleu soleille et bracelet cuir italien.",
        "category": "Watches",
        "price": 1299.99,
        "stock": 11,
        "image_urls": [
            "https://placehold.co/900x600/FAEEDA/854F0B/png?text=Montre+Heritage",
            "https://placehold.co/900x600/FFFFFF/185FA5/png?text=Montre+Wristshot",
        ],
    },
    {
        "name": "Montre Chronographe Royale",
        "description": "Chronographe luxe en acier poli avec verre saphir et etancheite 100m.",
        "category": "Watches",
        "price": 2499.99,
        "stock": 7,
        "image_urls": [
            "https://placehold.co/900x600/111827/FFFFFF/png?text=Chronographe+Royale",
        ],
    },
    {
        "name": "Montre Slim Gold",
        "description": "Montre extra-plate finition or, design minimaliste et bracelet noir.",
        "category": "Watches",
        "price": 1899.99,
        "stock": 6,
        "image_urls": [
            "https://placehold.co/900x600/FFF7D6/854F0B/png?text=Montre+Slim+Gold",
        ],
    },
    {
        "name": "Sac Cuir Riviera",
        "description": "Sac a main en cuir pleine fleur, coutures fines et fermoir metal premium.",
        "category": "Luxury",
        "price": 899.99,
        "stock": 10,
        "image_urls": [
            "https://placehold.co/900x600/E6F1FB/042C53/png?text=Sac+Cuir+Riviera",
        ],
    },
    {
        "name": "Lunettes Signature Noir",
        "description": "Lunettes de soleil luxe, monture acetate noir et verres polarises.",
        "category": "Luxury",
        "price": 399.99,
        "stock": 22,
        "image_urls": [
            "https://placehold.co/900x600/F0F6FF/111827/png?text=Lunettes+Signature",
        ],
    },
    {
        "name": "Parfum Maison Ambre",
        "description": "Parfum de niche aux notes ambrees, bois precieux et vanille douce.",
        "category": "Luxury",
        "price": 179.99,
        "stock": 26,
        "image_urls": [
            "https://placehold.co/900x600/FAEEDA/854F0B/png?text=Parfum+Maison+Ambre",
        ],
    },
]


def product_placeholder_url(product: Product) -> str:
    label = quote_plus(product.name[:42])
    return f"https://placehold.co/900x600/E6F1FB/185FA5/png?text={label}"


def ensure_admin_column():
    inspector = inspect(engine)
    if "users" in inspector.get_table_names():
        if not any(col["name"] == "is_admin" for col in inspector.get_columns("users")):
            with engine.begin() as conn:
                conn.execute(text("ALTER TABLE users ADD COLUMN is_admin BOOLEAN NOT NULL DEFAULT false"))
            print("Added missing users.is_admin column.")


def ensure_image_urls_column():
    inspector = inspect(engine)
    if "products" in inspector.get_table_names():
        if not any(col["name"] == "image_urls" for col in inspector.get_columns("products")):
            with engine.begin() as conn:
                conn.execute(text("ALTER TABLE products ADD COLUMN image_urls JSON"))
            print("Added missing products.image_urls column.")


def ensure_user_location_columns():
    inspector = inspect(engine)
    if "users" in inspector.get_table_names():
        if not any(col["name"] == "latitude" for col in inspector.get_columns("users")):
            with engine.begin() as conn:
                conn.execute(text("ALTER TABLE users ADD COLUMN latitude FLOAT"))
            print("Added missing users.latitude column.")
        if not any(col["name"] == "longitude" for col in inspector.get_columns("users")):
            with engine.begin() as conn:
                conn.execute(text("ALTER TABLE users ADD COLUMN longitude FLOAT"))
            print("Added missing users.longitude column.")


def ensure_order_shipping_address_column():
    inspector = inspect(engine)
    if "orders" in inspector.get_table_names():
        if not any(col["name"] == "shipping_address" for col in inspector.get_columns("orders")):
            with engine.begin() as conn:
                conn.execute(text("ALTER TABLE orders ADD COLUMN shipping_address VARCHAR"))
            print("Added missing orders.shipping_address column.")


def run():
    ensure_admin_column()
    ensure_image_urls_column()
    ensure_user_location_columns()
    ensure_order_shipping_address_column()
    db = SessionLocal()
    try:
        existing_products = {
            product.name: product
            for product in db.query(Product).filter(Product.name.in_([item["name"] for item in SAMPLE_PRODUCTS])).all()
        }
        inserted = 0
        updated = 0
        for item in SAMPLE_PRODUCTS:
            existing = existing_products.get(item["name"])
            if existing:
                for key, value in item.items():
                    if getattr(existing, key) != value:
                        setattr(existing, key, value)
                        updated += 1
            else:
                db.add(Product(**item))
                inserted += 1

        for product in db.query(Product).all():
            image_urls = product.image_urls or []
            if not image_urls or any("via.placeholder.com" in url for url in image_urls):
                product.image_urls = [product_placeholder_url(product)]
                updated += 1

        if inserted:
            db.commit()
            print(f"Sample products inserted: {inserted}, updated fields: {updated}.")
        elif updated:
            db.commit()
            print(f"Sample products updated fields: {updated}.")
        else:
            print("Sample products already exist, skipping seed.")

        if settings.admin_email and settings.admin_password:
            admin_user = db.query(User).filter(User.email == settings.admin_email).first()
            if not admin_user:
                db.add(
                    User(
                        email=settings.admin_email,
                        full_name="Admin User",
                        hashed_password=hash_password(settings.admin_password),
                        is_admin=True,
                    )
                )
                db.commit()
                print(f"Admin user created: {settings.admin_email}")
            elif not admin_user.is_admin:
                admin_user.is_admin = True
                db.commit()
                print(f"Existing user upgraded to admin: {settings.admin_email}")
    finally:
        db.close()


if __name__ == "__main__":
    run()
