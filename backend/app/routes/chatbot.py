from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db import get_db
from app.dependencies import get_current_admin_user
from app.schemas.chatbot import ChatRequest, ChatResponse
from app.services.rag_service import rag_service

router = APIRouter(prefix="/chatbot", tags=["chatbot"])


@router.post("/reindex")
def reindex_products(db: Session = Depends(get_db), current_user = Depends(get_current_admin_user)):
    count = rag_service.index_products(db)
    return {"indexed_products": count}


@router.post("/", response_model=ChatResponse)
def chat(payload: ChatRequest):
    try:
        contexts = rag_service.query_products(payload.question, payload.top_k)
        answer = rag_service.generate_answer(payload.question, contexts)
        return ChatResponse(answer=answer, sources=contexts)
    except Exception as e:
        print(f"Chatbot error: {type(e).__name__}: {e}")
        import traceback
        traceback.print_exc()
        return ChatResponse(
            answer=f"Erreur du chatbot: {str(e)}",
            sources=[]
        )
