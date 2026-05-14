# Smart E-Commerce Intelligent App

Projet full-stack avec:
- Backend FastAPI + PostgreSQL + Redis
- Mobile Android natif Kotlin + Jetpack Compose (MVVM)
- Recommandation simple + avancee
- Chatbot RAG (ChromaDB + embeddings + LLM)

## Structure

- `backend/`
  - `app/models`
  - `app/schemas`
  - `app/routes`
  - `app/services` (recommandation + RAG)
- `mobile/`
  - `app/src/main/java/com/example/smartshop/...`

## Lancer le backend

Suivre les etapes de `backend/README.md`.

## Lancer Android

1. Ouvrir le dossier `mobile` dans Android Studio
2. Synchroniser Gradle
3. Lancer un emulateur Android
4. Run application (`MainActivity`)

## Test rapide API (ordre recommande)

1. `POST /auth/register`
2. `POST /auth/login` (recuperer `access_token`)
3. `GET /products/`
4. `POST /cart/` avec `Authorization: Bearer <token>`
5. `POST /orders/`
6. `GET /recommendations/personalized`
7. `GET /recommendations/personalized-advanced`
8. `POST /chatbot/reindex`
9. `POST /chatbot/`
# SmartShop
# SmartShop
