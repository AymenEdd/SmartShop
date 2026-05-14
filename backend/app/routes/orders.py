from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, joinedload

from app.db import get_db
from app.dependencies import get_current_user
from app.models.cart import CartItem
from app.models.order import Order, OrderItem
from app.models.user import User
from app.schemas.order import OrderCreate, OrderOut

router = APIRouter(prefix="/orders", tags=["orders"])


@router.post("/", response_model=OrderOut)
def create_order(
    payload: OrderCreate | None = None,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    cart_items = (
        db.query(CartItem)
        .options(joinedload(CartItem.product))
        .filter(CartItem.user_id == current_user.id)
        .all()
    )
    if not cart_items:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cart is empty")

    order = Order(
        user_id=current_user.id,
        total_amount=0,
        shipping_address=payload.shipping_address.strip() if payload and payload.shipping_address else None,
    )
    db.add(order)
    db.flush()

    total = 0.0
    for cart_item in cart_items:
        unit_price = cart_item.product.price
        total += unit_price * cart_item.quantity
        order_item = OrderItem(
            order_id=order.id,
            product_id=cart_item.product_id,
            quantity=cart_item.quantity,
            unit_price=unit_price,
        )
        db.add(order_item)
        db.delete(cart_item)

    order.total_amount = total
    db.commit()
    db.refresh(order)
    return (
        db.query(Order)
        .options(joinedload(Order.items).joinedload(OrderItem.product))
        .filter(Order.id == order.id)
        .first()
    )


@router.get("/", response_model=list[OrderOut])
def list_orders(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return (
        db.query(Order)
        .options(joinedload(Order.items).joinedload(OrderItem.product))
        .filter(Order.user_id == current_user.id)
        .order_by(Order.created_at.desc())
        .all()
    )
