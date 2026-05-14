package com.example.smartshop.network

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val email: String,
    val full_name: String,
    val password: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
data class UpdateProfileRequest(
    val full_name: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
data class TokenResponse(val access_token: String, val token_type: String)

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val stock: Int,
    val rating: Double? = null,
    val image_urls: List<String>? = null
)

data class CartAddRequest(val product_id: Int, val quantity: Int)
data class CartItem(val id: Int, val quantity: Int, val product: Product)
data class OrderItem(val id: Int, val quantity: Int, val unit_price: Double, val product: Product)
data class CreateOrderRequest(val shipping_address: String? = null)
data class Order(
    val id: Int,
    val total_amount: Double,
    val shipping_address: String? = null,
    val created_at: String,
    val items: List<OrderItem>,
)
data class AdminOrder(
    val id: Int,
    val total_amount: Double,
    val shipping_address: String? = null,
    val created_at: String,
    val items: List<OrderItem>,
    val user: UserProfile,
)

data class RecommendationResponse(val strategy: String, val products: List<Product>)
data class ImageUploadResponse(val url: String)
data class ChatRequest(val question: String, val top_k: Int = 4)
data class ChatResponse(val answer: String, val sources: List<String>)
data class SearchRequest(
    val query: String,
    val min_price: Double? = null,
    val max_price: Double? = null,
    val category: String? = null,
    val min_rating: Double? = null
)
data class SearchResponse(val products: List<Product>, val suggestions: List<String> = emptyList())
data class CartUpdateRequest(val quantity: Int)
data class UserProfile(
    val id: Int,
    val email: String,
    val full_name: String,
    val is_admin: Boolean,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
data class AdminInsight(val top_products: List<String>, val trend_summary: String)
