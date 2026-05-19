import os
from pathlib import Path
from pydantic_settings import BaseSettings


BASE_DIR = Path(__file__).resolve().parent.parent.parent

# Load environment variables from .env file manually
env_file = BASE_DIR / ".env"
if env_file.exists():
    with open(env_file, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#"):
                if "=" in line:
                    key, value = line.split("=", 1)
                    os.environ[key] = value


class Settings(BaseSettings):
    database_url: str
    jwt_secret_key: str
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 120
    redis_url: str = "redis://localhost:6379/0"
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "llama3.1:8b"
    admin_email: str | None = None
    admin_password: str | None = None
    google_maps_api_key: str | None = None
    firebase_project_id: str | None = None


# Create settings instance with explicit environment variables
settings = Settings(
    database_url=os.getenv("DATABASE_URL"),
    jwt_secret_key=os.getenv("JWT_SECRET_KEY"),
    jwt_algorithm=os.getenv("JWT_ALGORITHM", "HS256"),
    access_token_expire_minutes=int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "120")),
    redis_url=os.getenv("REDIS_URL", "redis://localhost:6379/0"),
    ollama_base_url=os.getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
    ollama_model=os.getenv("OLLAMA_MODEL", "llama3.1:8b"),
    admin_email=os.getenv("ADMIN_EMAIL"),
    admin_password=os.getenv("ADMIN_PASSWORD"),
    google_maps_api_key=os.getenv("GOOGLE_MAPS_API_KEY"),
    firebase_project_id=os.getenv("FIREBASE_PROJECT_ID"),
)
