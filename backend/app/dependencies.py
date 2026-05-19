from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from typing import Optional

from app.core.security import decode_access_token, decode_firebase_token, hash_password
from app.db import get_db
from app.core.config import settings
from app.models.user import User


oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login", auto_error=False)


def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> User:
    user_id = decode_access_token(token)
    if user_id:
        user = db.get(User, int(user_id))
        if not user:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
        return user

    firebase_payload = decode_firebase_token(token)
    if not firebase_payload:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")

    email = firebase_payload.get("email")
    if not email:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Firebase token has no email")
    # Determine admin status from Firebase custom claims or configured admin email
    is_admin_claim = False
    try:
        # custom claims are top-level keys in the Firebase token
        is_admin_claim = bool(firebase_payload.get("admin") or firebase_payload.get("is_admin"))
    except Exception:
        is_admin_claim = False

    user = db.query(User).filter(User.email == email).first()
    if not user:
        display_name = firebase_payload.get("name") or email.split("@", 1)[0]
        user = User(
            email=email,
            full_name=display_name,
            hashed_password=hash_password(f"firebase:{firebase_payload.get('user_id') or email}"),
            is_admin=(is_admin_claim or (settings.admin_email and settings.admin_email == email)),
        )
        db.add(user)
        db.commit()
        db.refresh(user)
    else:
        # Ensure admin flag is set if token or config indicates admin
        should_be_admin = is_admin_claim or (settings.admin_email and settings.admin_email == email)
        if should_be_admin and not user.is_admin:
            user.is_admin = True
            db.add(user)
            db.commit()
            db.refresh(user)
    return user


def get_current_admin_user(current_user: User = Depends(get_current_user)) -> User:
    if not current_user.is_admin:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin privileges required")
    return current_user


def get_current_user_optional(token: Optional[str] = Depends(oauth2_scheme), db: Session = Depends(get_db)) -> Optional[User]:
    if not token:
        return None
    try:
        user_id = decode_access_token(token)
        if user_id:
            return db.get(User, int(user_id))

        firebase_payload = decode_firebase_token(token)
        if not firebase_payload:
            return None
        email = firebase_payload.get("email")
        if not email:
            return None
        user = db.query(User).filter(User.email == email).first()
        if not user:
            return None
        # if firebase token contains admin claim or email matches configured admin, ensure flag
        is_admin_claim = bool(firebase_payload.get("admin") or firebase_payload.get("is_admin"))
        if (is_admin_claim or (settings.admin_email and settings.admin_email == email)) and not user.is_admin:
            user.is_admin = True
            db.add(user)
            db.commit()
            db.refresh(user)
        return user
    except:
        return None
