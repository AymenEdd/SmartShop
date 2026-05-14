package com.example.smartshop.network

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("/auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @GET("/auth/me")
    suspend fun getProfile(@Header("Authorization") token: String): UserProfile

    @PATCH("/auth/me")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body body: UpdateProfileRequest): UserProfile

    @POST("/auth/register")
    suspend fun register(@Body body: RegisterRequest)

    @GET("/admin/users")
    suspend fun getUsers(@Header("Authorization") token: String): List<UserProfile>

    @GET("/admin/orders")
    suspend fun getAdminOrders(@Header("Authorization") token: String): List<AdminOrder>

    @GET("/admin/orders/{id}")
    suspend fun getAdminOrderById(@Header("Authorization") token: String, @Path("id") orderId: Int): AdminOrder

    @GET("/products/")
    suspend fun getProducts(): List<Product>

    @GET("/products/{id}")
    suspend fun getProductById(@Path("id") productId: Int): Product

    @POST("/products/")
    suspend fun createProduct(@Header("Authorization") token: String, @Body product: Product): Product
    @PUT("/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") productId: Int,
        @Body product: Product,
    ): Product
    @DELETE("/products/{id}")
    suspend fun deleteProduct(@Header("Authorization") token: String, @Path("id") productId: Int)

    @Multipart
    @POST("/products/upload-image")
    suspend fun uploadProductImage(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
    ): ImageUploadResponse

    @POST("/cart/")
    suspend fun addToCart(@Header("Authorization") token: String, @Body body: CartAddRequest): CartItem

    @GET("/cart/")
    suspend fun getCart(@Header("Authorization") token: String): List<CartItem>

    @DELETE("/cart/{id}")
    suspend fun removeCartItem(@Header("Authorization") token: String, @Path("id") cartItemId: Int)

    @POST("/orders/")
    suspend fun createOrder(@Header("Authorization") token: String, @Body body: CreateOrderRequest): Order

    @GET("/orders/")
    suspend fun getOrders(@Header("Authorization") token: String): List<Order>

    @GET("/recommendations/personalized")
    suspend fun getPersonalizedRecommendations(@Header("Authorization") token: String): RecommendationResponse

    @GET("/recommendations/similar/{id}")
    suspend fun getSimilarProducts(@Path("id") productId: Int): RecommendationResponse

    @POST("/search")
    suspend fun semanticSearch(@Body body: SearchRequest): SearchResponse

    @GET("/search/suggestions")
    suspend fun suggestions(@Query("q") query: String): List<String>

    @POST("/chatbot/")
    suspend fun askBot(@Body body: ChatRequest): ChatResponse
}
