from datetime import datetime, timedelta, timezone

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.core.config import settings

try:
    from google.auth.transport import requests as google_requests
    from google.oauth2 import id_token as google_id_token
except ImportError:  # pragma: no cover - depends on optional runtime package
    google_requests = None
    google_id_token = None


pwd_context = CryptContext(schemes=["pbkdf2_sha256"], deprecated="auto")


def hash_password(password: str) -> str:
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def create_access_token(subject: str) -> str:
    expire = datetime.now(timezone.utc) + timedelta(minutes=settings.access_token_expire_minutes)
    to_encode = {"sub": subject, "exp": expire}
    return jwt.encode(to_encode, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)


def decode_access_token(token: str | None) -> str | None:
    if not token:
        return None
    try:
        payload = jwt.decode(token, settings.jwt_secret_key, algorithms=[settings.jwt_algorithm])
        return payload.get("sub")
    except (JWTError, TypeError):
        return None


def decode_firebase_token(token: str | None) -> dict | None:
    if not token:
        return None
    if not settings.firebase_project_id:
        return None
    if google_id_token is None or google_requests is None:
        return None
    try:
        return google_id_token.verify_firebase_token(
            token,
            google_requests.Request(),
            audience=settings.firebase_project_id,
        )
    except Exception:
        return None
