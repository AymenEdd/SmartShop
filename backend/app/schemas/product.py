from pydantic import BaseModel, Field


class ProductBase(BaseModel):
    name: str = Field(min_length=2, max_length=120)
    description: str = Field(default="", max_length=2000)
    category: str = Field(min_length=2, max_length=80)
    price: float = Field(gt=0)
    stock: int = Field(ge=0)
    image_urls: list[str] | None = None


class ProductCreate(ProductBase):
    pass


class ProductUpdate(BaseModel):
    name: str | None = Field(default=None, min_length=2, max_length=120)
    description: str | None = Field(default=None, max_length=2000)
    category: str | None = Field(default=None, min_length=2, max_length=80)
    price: float | None = Field(default=None, gt=0)
    stock: int | None = Field(default=None, ge=0)
    image_urls: list[str] | None = None


class ProductOut(ProductBase):
    id: int

    class Config:
        from_attributes = True
