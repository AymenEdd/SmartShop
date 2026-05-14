from pathlib import Path
import uuid

from fastapi import APIRouter, Depends, File, HTTPException, UploadFile, status
from sqlalchemy.orm import Session

from app.db import get_db
from app.dependencies import get_current_admin_user
from app.models.product import Product
from app.schemas.product import ProductCreate, ProductOut, ProductUpdate
from app.services.rag_service import rag_service

router = APIRouter(prefix="/products", tags=["products"])
UPLOAD_DIR = Path(__file__).resolve().parent.parent.parent / "uploads"
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


def save_image_file(upload_file: UploadFile) -> str:
    suffix = Path(upload_file.filename).suffix or ".jpg"
    filename = f"{uuid.uuid4().hex}{suffix}"
    target_path = UPLOAD_DIR / filename
    with target_path.open("wb") as buffer:
        while True:
            chunk = upload_file.file.read(1024 * 1024)
            if not chunk:
                break
            buffer.write(chunk)
    return f"/uploads/{filename}"


@router.post("/upload-image")
def upload_image(file: UploadFile = File(...), current_user=Depends(get_current_admin_user)):
    image_url = save_image_file(file)
    return {"url": image_url}


@router.get("/", response_model=list[ProductOut])
def list_products(db: Session = Depends(get_db)):
    return db.query(Product).order_by(Product.created_at.desc()).all()


@router.get("/{product_id}", response_model=ProductOut)
def get_product(product_id: int, db: Session = Depends(get_db)):
    product = db.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    return product


@router.post("/", response_model=ProductOut)
def create_product(payload: ProductCreate, db: Session = Depends(get_db), current_user = Depends(get_current_admin_user)):
    product = Product(**payload.model_dump())
    db.add(product)
    db.commit()
    db.refresh(product)
    rag_service.index_products(db)
    return product


@router.put("/{product_id}", response_model=ProductOut)
def update_product(product_id: int, payload: ProductUpdate, db: Session = Depends(get_db), current_user = Depends(get_current_admin_user)):
    product = db.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    for key, value in payload.model_dump(exclude_none=True).items():
        setattr(product, key, value)
    db.commit()
    db.refresh(product)
    rag_service.index_products(db)
    return product


@router.delete("/{product_id}")
def delete_product(product_id: int, db: Session = Depends(get_db), current_user = Depends(get_current_admin_user)):
    product = db.get(Product, product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")
    db.delete(product)
    db.commit()
    rag_service.index_products(db)
    return {"message": "Product deleted"}
