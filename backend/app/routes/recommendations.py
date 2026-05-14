from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.db import get_db
from app.dependencies import get_current_user, get_current_user_optional
from app.models.user import User
from app.schemas.recommendation import RecommendationResponse
from app.services.recommender import (
    advanced_collaborative_recommendations,
    home_recommendations,
    personalized_recommendations,
    similar_products,
)

router = APIRouter(prefix="/recommendations", tags=["recommendations"])


@router.get("/home", response_model=RecommendationResponse)
def recommend_home(current_user: User | None = Depends(get_current_user_optional), db: Session = Depends(get_db)):
    products = home_recommendations(db, current_user.id if current_user else None)
    strategy = "home_personalized_blend" if current_user else "home_trending"
    return RecommendationResponse(strategy=strategy, products=products)


@router.get("/similar/{product_id}", response_model=RecommendationResponse)
def recommend_similar(product_id: int, db: Session = Depends(get_db)):
    products = similar_products(db, product_id)
    return RecommendationResponse(strategy="content_based_simple", products=products)


@router.get("/personalized", response_model=RecommendationResponse)
def recommend_personalized(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    products = personalized_recommendations(db, current_user.id)
    return RecommendationResponse(strategy="history_based_simple", products=products)


@router.get("/personalized-advanced", response_model=RecommendationResponse)
def recommend_personalized_advanced(
    current_user: User = Depends(get_current_user), db: Session = Depends(get_db)
):
    products = advanced_collaborative_recommendations(db, current_user.id)
    return RecommendationResponse(strategy="collaborative_filtering", products=products)
