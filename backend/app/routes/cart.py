from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, joinedload

from app.db import get_db
from app.dependencies import get_current_user
from app.models.cart import CartItem
from app.models.product import Product
from app.models.user import User
from app.schemas.cart import CartAddRequest, CartItemOut

router = APIRouter(prefix="/cart", tags=["cart"])


@router.get("/", response_model=list[CartItemOut])
def get_cart(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return (
        db.query(CartItem)
        .options(joinedload(CartItem.product))
        .filter(CartItem.user_id == current_user.id)
        .all()
    )


@router.post("/", response_model=CartItemOut)
def add_to_cart(
    payload: CartAddRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    product = db.get(Product, payload.product_id)
    if not product:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Product not found")

    item = (
        db.query(CartItem)
        .filter(CartItem.user_id == current_user.id, CartItem.product_id == payload.product_id)
        .first()
    )
    if item:
        item.quantity += payload.quantity
    else:
        item = CartItem(user_id=current_user.id, product_id=payload.product_id, quantity=payload.quantity)
        db.add(item)

    db.commit()
    db.refresh(item)
    return item


@router.delete("/{item_id}")
def remove_item(item_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    item = db.get(CartItem, item_id)
    if not item or item.user_id != current_user.id:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Cart item not found")
    db.delete(item)
    db.commit()
    return {"message": "Cart item removed"}
