from pydantic import BaseModel, Field

from app.schemas.product import ProductOut


class CartAddRequest(BaseModel):
    product_id: int
    quantity: int = Field(default=1, ge=1, le=100)


class CartItemOut(BaseModel):
    id: int
    quantity: int
    product: ProductOut

    class Config:
        from_attributes = True
