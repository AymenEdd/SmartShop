from pydantic import BaseModel

from app.schemas.product import ProductOut


class RecommendationResponse(BaseModel):
    strategy: str
    products: list[ProductOut]
