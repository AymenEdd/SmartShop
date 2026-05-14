import os
import requests
import certifi

os.environ["SSL_CERT_FILE"]      = certifi.where()
os.environ["REQUESTS_CA_BUNDLE"] = certifi.where()
os.environ["CURL_CA_BUNDLE"]     = certifi.where()
os.environ["HUGGINGFACE_HUB_DISABLE_SSL_VERIFY"] = "1"

from sentence_transformers import SentenceTransformer
from sqlalchemy.orm import Session
import chromadb

from app.core.config import settings
from app.models.product import Product

MODEL_PATH = os.path.normpath(
    os.path.join(os.path.dirname(__file__), "..", "..", "models", "all-MiniLM-L6-v2")
)


class RagService:
    def __init__(self):
        self.embedder = None
        self.chroma_client = chromadb.PersistentClient(path="./chroma_data")
        self.collection = self.chroma_client.get_or_create_collection(name="products")

    def _get_embedder(self):
        if self.embedder is not None:
            return self.embedder
        try:
            if os.path.isdir(MODEL_PATH):
                print(f"Loading model from local path: {MODEL_PATH}")
                self.embedder = SentenceTransformer(MODEL_PATH)
            else:
                print(f"Local model not found at {MODEL_PATH}, attempting download...")
                self.embedder = SentenceTransformer(
                    "all-MiniLM-L6-v2",
                    cache_folder="./models"
                )
        except Exception as e:
            print(f"Warning: Failed to load SentenceTransformer model: {e}")
            print("RAG functionality will be disabled")
            self.embedder = None
        return self.embedder

    def index_products(self, db: Session) -> int:
        embedder = self._get_embedder()
        if embedder is None:
            print("Warning: Cannot index products - embedder not available")
            return 0

        products = db.query(Product).all()
        if not products:
            print("RAG: no products found in database")
            return 0

        try:
            self.chroma_client.delete_collection(name="products")
        except Exception:
            pass
        self.collection = self.chroma_client.get_or_create_collection(name="products")

        docs, ids, metadatas = [], [], []
        for p in products:
            text = f"{p.name}. Category: {p.category}. Price: {p.price}. Description: {p.description}"
            docs.append(text)
            ids.append(str(p.id))
            metadatas.append({
                "name": p.name,
                "category": p.category,
                "price": float(p.price)
            })

        embeddings = embedder.encode(docs).tolist()
        self.collection.add(
            ids=ids,
            documents=docs,
            embeddings=embeddings,
            metadatas=metadatas
        )
        print(f"RAG: indexed {len(products)} products successfully")
        return len(products)

    def query_products(self, question: str, top_k: int = 4) -> list[str]:
        embedder = self._get_embedder()
        if embedder is None:
            print("Warning: Cannot query products - embedder not available")
            return []

        # Guard: don't query an empty collection
        try:
            count = self.collection.count()
        except Exception:
            count = 0

        if count == 0:
            print("RAG: collection is empty, skipping query")
            return []

        try:
            query_embedding = embedder.encode([question]).tolist()[0]
            result = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=min(top_k, count)   # never request more than available
            )
            return result.get("documents", [[]])[0]
        except Exception as e:
            print(f"RAG query error: {e}")
            return []

    def query_product_ids(self, question: str, top_k: int = 4) -> list[int]:
        """Query products and return product IDs instead of documents."""
        embedder = self._get_embedder()
        if embedder is None:
            print("Warning: Cannot query products - embedder not available")
            return []

        # Guard: don't query an empty collection
        try:
            count = self.collection.count()
        except Exception:
            count = 0

        if count == 0:
            print("RAG: collection is empty, skipping query")
            return []

        try:
            query_embedding = embedder.encode([question]).tolist()[0]
            result = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=min(top_k, count),
                include=["documents", "metadatas", "distances"]
            )
            ids = result.get("ids", [[]])[0]
            return [int(id) for id in ids]
        except Exception as e:
            print(f"RAG query error: {e}")
            return []

    def _parse_ollama_response(self, data) -> str:
        if isinstance(data, dict):
            if "response" in data:
                return str(data["response"]).strip()
            if "choices" in data and isinstance(data["choices"], list) and data["choices"]:
                first_choice = data["choices"][0]
                if isinstance(first_choice, dict):
                    return str(first_choice.get("content", "")).strip()
        return ""

    def generate_answer(self, question: str, contexts: list[str]) -> str:
        context_block = "\n".join(f"- {c}" for c in contexts) if contexts else "No product context available."
        prompt = (
            "You are an e-commerce shopping assistant.\n"
            "You have access to product catalog context and should answer based on that information.\n"
            "If the question cannot be answered from the provided product context, say so clearly and do not invent details.\n\n"
            f"Product context:\n{context_block}\n\n"
            f"User question: {question}\n\n"
            "Answer in French when the question is in French, otherwise answer in the same language as the user."
        )

        try:
            response = requests.post(
                f"{settings.ollama_base_url}/api/generate",
                json={
                    "model": settings.ollama_model,
                    "prompt": prompt,
                    "temperature": 0.2,
                    "max_tokens": 512,
                    "stream": False,
                },
                timeout=60,
            )
            response.raise_for_status()
            answer = self._parse_ollama_response(response.json())
            return answer or "Aucune réponse du modèle."

        except requests.exceptions.ConnectionError:
            print("Ollama connection error: is Ollama running?")
            return (
                "Le service Ollama est inaccessible. "
                "Assurez-vous qu'Ollama est démarré (`ollama serve`).\n\n"
                f"Infos catalogue disponibles :\n{context_block}"
            )
        except requests.exceptions.Timeout:
            print("Ollama request timed out")
            return "Le modèle IA met trop de temps à répondre. Réessayez dans un moment."
        except requests.exceptions.HTTPError as exc:
            print(f"Ollama HTTP error: {exc} — response: {exc.response.text if exc.response else 'N/A'}")
            return (
                f"Erreur du modèle IA ({exc.response.status_code if exc.response else 'inconnue'}).\n\n"
                f"Infos catalogue disponibles :\n{context_block}"
            )
        except requests.RequestException as exc:
            print(f"Ollama request failed: {exc}")
            return (
                "Ollama local indisponible. Voici les infos retrouvées dans le catalogue :\n"
                f"{context_block}"
            )

    def ask(self, question: str) -> str:
        if not question or not question.strip():
            return "Veuillez poser une question."
        contexts = self.query_products(question)
        return self.generate_answer(question, contexts)


rag_service = RagService()