from pydantic import BaseModel, EmailStr, Field


class UserRegister(BaseModel):
    email: EmailStr
    full_name: str = Field(min_length=2, max_length=120)
    password: str = Field(min_length=6, max_length=128)
    latitude: float | None = None
    longitude: float | None = None


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


class UserOut(BaseModel):
    id: int
    email: EmailStr
    full_name: str
    is_admin: bool = False
    latitude: float | None = None
    longitude: float | None = None

    class Config:
        from_attributes = True


class UserUpdate(BaseModel):
    full_name: str | None = None
    latitude: float | None = None
    longitude: float | None = None
