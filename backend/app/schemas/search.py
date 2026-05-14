from pydantic import BaseModel
from typing import List, Optional

from app.schemas.product import ProductOut


class SearchRequest(BaseModel):
    query: str
    min_price: Optional[float] = None
    max_price: Optional[float] = None
    category: Optional[str] = None
    min_rating: Optional[float] = None


class SearchResponse(BaseModel):
    products: List[ProductOut]
    suggestions: List[str] = []