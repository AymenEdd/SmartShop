# Recommendation System Design

## 1. Content-Based Filtering

- Input signals: `category`, `price`, product metadata.
- Strategy:
  - Find products with same category.
  - Rank by absolute price distance.
- Endpoint:
  - `GET /recommendations/similar/{product_id}`
- Output:
  - `strategy = "content_based_simple"`
  - List of similar products.

## 2. Collaborative Filtering

- Input signals: order history across users.
- Strategy:
  - Identify products bought by users/orders that overlap with the current user's purchases.
  - Rank candidates by co-occurrence count.
- Endpoint:
  - `GET /recommendations/personalized-advanced`
- Output:
  - `strategy = "collaborative_filtering"`
  - Ranked list of products not purchased yet by current user.

## 3. User Behavior Tracking

- Tracked behavior:
  - Purchased products
  - Purchase frequency by category
  - Recency via order timestamps
- Current storage:
  - PostgreSQL (`orders`, `order_items`, `products`)
  - Redis cache for recommendation responses.

## 4. Recommendation API

- `GET /recommendations/personalized`
  - History/category based recommendations for current user.
- `GET /recommendations/personalized-advanced`
  - Collaborative filtering recommendations.
- `GET /recommendations/home`
  - Home feed recommendations: personalized blend for logged-in users, trending feed for guests.
- `GET /recommendations/similar/{product_id}`
  - Product-to-product recommendations.

## 5. Future AI Improvements

- Add embedding-based user/item vectors.
- Blend collaborative and content scores with weighted ranking.
- Add clickstream events (views, dwell time, add-to-cart without checkout).
