from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from app.db import Base, engine
from app.models import CartItem, Order, OrderItem, Product, User
from app.routes.auth import router as auth_router
from app.routes.admin import router as admin_router
from app.routes.cart import router as cart_router
from app.routes.chatbot import router as chatbot_router
from app.routes.orders import router as orders_router
from app.routes.products import router as products_router
from app.routes.recommendations import router as recommendations_router
from app.routes.search import router as search_router
from app.services.rag_service import rag_service
from seed_data import (
    ensure_admin_column,
    ensure_image_urls_column,
    ensure_order_shipping_address_column,
    ensure_user_location_columns,
)

BASE_DIR = Path(__file__).resolve().parent.parent
STATIC_DIR = BASE_DIR / "static"

app = FastAPI(title="Smart E-Commerce API", version="1.0.0")

UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
STATIC_DIR.mkdir(parents=True, exist_ok=True)
app.mount("/uploads", StaticFiles(directory=str(UPLOAD_DIR)), name="uploads")
app.mount("/static", StaticFiles(directory=str(STATIC_DIR)), name="static")

Base.metadata.create_all(bind=engine)
ensure_admin_column()
ensure_image_urls_column()
ensure_user_location_columns()
ensure_order_shipping_address_column()


@app.on_event("startup")
def startup_reindex_products():
    from app.db import SessionLocal

    with SessionLocal() as db:
        indexed = rag_service.index_products(db)
        if indexed:
            print(f"RAG: indexed {indexed} products on startup")
        else:
            print("RAG: no products indexed on startup")

app.include_router(auth_router)
app.include_router(admin_router)
app.include_router(products_router)
app.include_router(cart_router)
app.include_router(orders_router)
app.include_router(recommendations_router)
app.include_router(chatbot_router)
app.include_router(search_router)


@app.get("/chatbot", include_in_schema=False)
def chatbot_page():
    return FileResponse(STATIC_DIR / "chatbot.html")


@app.get("/health")
def health():
    return {"status": "ok"}
