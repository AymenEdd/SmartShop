from datetime import datetime

from pydantic import BaseModel

from app.schemas.auth import UserOut
from app.schemas.product import ProductOut


class OrderItemOut(BaseModel):
    id: int
    quantity: int
    unit_price: float
    product: ProductOut

    class Config:
        from_attributes = True


class OrderCreate(BaseModel):
    shipping_address: str | None = None


class OrderOut(BaseModel):
    id: int
    total_amount: float
    shipping_address: str | None = None
    created_at: datetime
    items: list[OrderItemOut]

    class Config:
        from_attributes = True


class AdminOrderOut(OrderOut):
    user: UserOut
