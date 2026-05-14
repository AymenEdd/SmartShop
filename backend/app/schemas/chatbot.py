from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    question: str = Field(min_length=2, max_length=500)
    top_k: int = Field(default=4, ge=1, le=10)


class ChatResponse(BaseModel):
    answer: str
    sources: list[str]
