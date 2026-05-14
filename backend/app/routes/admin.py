from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session, joinedload

from app.db import get_db
from app.dependencies import get_current_admin_user
from app.models.order import Order, OrderItem
from app.models.user import User
from app.schemas.auth import UserOut
from app.schemas.order import AdminOrderOut

router = APIRouter(prefix="/admin", tags=["admin"])


@router.get("/users", response_model=list[UserOut])
def list_users(db: Session = Depends(get_db), current_user: User = Depends(get_current_admin_user)):
    return db.query(User).order_by(User.created_at.desc()).all()


@router.get("/orders", response_model=list[AdminOrderOut])
def list_orders(db: Session = Depends(get_db), current_user: User = Depends(get_current_admin_user)):
    return (
        db.query(Order)
        .options(
            joinedload(Order.user),
            joinedload(Order.items).joinedload(OrderItem.product),
        )
        .order_by(Order.created_at.desc())
        .all()
    )


@router.get("/orders/{order_id}", response_model=AdminOrderOut)
def get_order(order_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_admin_user)):
    order = (
        db.query(Order)
        .options(
            joinedload(Order.user),
            joinedload(Order.items).joinedload(OrderItem.product),
        )
        .filter(Order.id == order_id)
        .first()
    )
    if not order:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Order not found")
    return order


@router.put("/users/{user_id}/promote", response_model=UserOut)
def promote_user(user_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_admin_user)):
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    if user.is_admin:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User is already an admin")
    user.is_admin = True
    db.commit()
    db.refresh(user)
    return user


@router.put("/users/{user_id}/demote", response_model=UserOut)
def demote_user(user_id: int, db: Session = Depends(get_db), current_user: User = Depends(get_current_admin_user)):
    if user_id == current_user.id:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot demote yourself")
    user = db.get(User, user_id)
    if not user:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    if not user.is_admin:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="User is not an admin")

    admin_count = db.query(User).filter(User.is_admin.is_(True)).count()
    if admin_count <= 1:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Cannot demote the last admin")

    user.is_admin = False
    db.commit()
    db.refresh(user)
    return user
