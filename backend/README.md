# Smart E-Commerce Backend (FastAPI)

## 1) Setup

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

## 2) Infrastructure locale

- PostgreSQL: creer la base `smart_shop`
- Redis: lancer sur `localhost:6379`
- Ollama: installer puis lancer `ollama serve`
- Pull d'un modele (exemple): `ollama pull llama3.1:8b`

## 3) Run

```bash
uvicorn app.main:app --reload
```

Swagger: <http://127.0.0.1:8000/docs>

## 4) Seed data

```bash
python seed_data.py
```

## 5) Endpoints principaux

- `POST /auth/register`
- `POST /auth/login`
- `GET|POST|PUT|DELETE /products`
- `GET|POST|DELETE /cart`
- `POST /orders`
- `GET /orders`
- `GET /recommendations/similar/{product_id}`
- `GET /recommendations/personalized`
- `GET /recommendations/personalized-advanced`
- `GET /recommendations/home`
- `GET /chatbot`
  - Chatbot page with camera object scanning and semantic search.
- `POST /chatbot/reindex`
- `POST /chatbot`

## 6) Configuration LLM local (Ollama)

Variables a verifier dans `.env`:

- `OLLAMA_BASE_URL=http://localhost:11434`
- `OLLAMA_MODEL=llama3.1:8b`
- `ADMIN_EMAIL=admin@example.com`
- `ADMIN_PASSWORD=change_this_secret`

## 7) Admin setup

Pour creer un utilisateur admin, ajoutez `ADMIN_EMAIL` et `ADMIN_PASSWORD` dans `.env`, puis lancez:

```bash
python seed_data.py
```

Admin-only endpoints:

- `POST /products/`
- `PUT /products/{product_id}`
- `DELETE /products/{product_id}`
- `POST /chatbot/reindex`
- `GET /admin/users`
- `PUT /admin/users/{user_id}/promote`
- `PUT /admin/users/{user_id}/demote`

## 8) Pipeline RAG

1. Ajouter des produits
2. Appeler `POST /chatbot/reindex`
3. Poser une question via `POST /chatbot`

Si Ollama est disponible, une reponse LLM locale est generee.
Sinon, l'API retourne une reponse basee sur le contexte retrouve.
