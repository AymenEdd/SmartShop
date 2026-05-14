package com.example.smartshop.repository

import android.content.Context
import android.net.Uri
import com.example.smartshop.network.CartAddRequest
import com.example.smartshop.network.ChatRequest
import com.example.smartshop.network.CreateOrderRequest
import com.example.smartshop.network.ImageUploadResponse
import com.example.smartshop.network.Order
import com.example.smartshop.network.Product
import com.example.smartshop.network.LoginRequest
import com.example.smartshop.network.RecommendationResponse
import com.example.smartshop.network.RegisterRequest
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.network.SearchRequest
import com.example.smartshop.network.UpdateProfileRequest
import com.example.smartshop.network.UserProfile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ShopRepository {
    private val api = RetrofitClient.api
    var token: String = ""
    var userProfile: UserProfile? = null
    private val localCartOverrides = mutableMapOf<Int, Int>()
    private var cachedProducts: List<Product> = emptyList()

    suspend fun login(email: String, password: String) {
        val res = api.login(LoginRequest(email, password))
        token = "Bearer ${res.access_token}"
        userProfile = api.getProfile(token)
    }

    suspend fun register(email: String, fullName: String, password: String, latitude: Double? = null, longitude: Double? = null) {
        api.register(
            RegisterRequest(
                email = email,
                full_name = fullName,
                password = password,
                latitude = latitude,
                longitude = longitude,
            )
        )
    }

    suspend fun getUsers(): List<UserProfile> = api.getUsers(token)
    suspend fun getAdminOrders() = api.getAdminOrders(token)
    suspend fun getAdminOrderById(orderId: Int) = api.getAdminOrderById(token, orderId)

    suspend fun getProducts(): List<Product> {
        cachedProducts = api.getProducts()
        return cachedProducts
    }

    suspend fun getProductById(productId: Int): Product = api.getProductById(productId)

    suspend fun getCart() = api.getCart(token).map {
        val overrideQuantity = localCartOverrides[it.id]
        if (overrideQuantity != null) it.copy(quantity = overrideQuantity) else it
    }

    suspend fun addToCart(productId: Int, quantity: Int) = api.addToCart(token, CartAddRequest(productId, quantity))

    suspend fun removeCartItem(cartItemId: Int) {
        api.removeCartItem(token, cartItemId)
        localCartOverrides.remove(cartItemId)
    }

    suspend fun setCartQuantity(itemId: Int, oldQuantity: Int, newQuantity: Int) {
        localCartOverrides[itemId] = newQuantity
        if (newQuantity <= 0) {
            removeCartItem(itemId)
            return
        }
        if (newQuantity > oldQuantity) {
            val delta = newQuantity - oldQuantity
            val item = getCart().firstOrNull { it.id == itemId } ?: return
            addToCart(item.product.id, delta)
        }
    }

    suspend fun createOrder(shippingAddress: String): Order =
        api.createOrder(token, CreateOrderRequest(shippingAddress.takeIf { it.isNotBlank() }))
    suspend fun getOrders(): List<Order> = api.getOrders(token)
    suspend fun getPersonalizedRecommendations(): RecommendationResponse =
        api.getPersonalizedRecommendations(token)

    suspend fun getSimilarProducts(productId: Int): RecommendationResponse = api.getSimilarProducts(productId)

    suspend fun semanticSearch(
        query: String,
        minPrice: Double?,
        maxPrice: Double?,
        category: String?,
        minRating: Double?
    ): List<Product> {
        return runCatching {
            api.semanticSearch(
                SearchRequest(
                    query = query,
                    min_price = minPrice,
                    max_price = maxPrice,
                    category = category,
                    min_rating = minRating
                )
            ).products
        }.getOrElse {
            // Fallback local filtering if /search is not yet available on backend.
            val source = if (cachedProducts.isNotEmpty()) cachedProducts else getProducts()
            source.filter { p ->
                p.name.contains(query, ignoreCase = true) ||
                    p.description.contains(query, ignoreCase = true) ||
                    p.category.contains(query, ignoreCase = true)
            }.filter { p -> minPrice == null || p.price >= minPrice }
                .filter { p -> maxPrice == null || p.price <= maxPrice }
                .filter { p -> category.isNullOrBlank() || p.category.equals(category, ignoreCase = true) }
                .filter { p -> minRating == null || (p.rating ?: 0.0) >= minRating }
        }
    }

    suspend fun getSuggestions(input: String): List<String> {
        return runCatching { api.suggestions(input) }.getOrElse {
            val source = if (cachedProducts.isNotEmpty()) cachedProducts else getProducts()
            source.map { it.name }
                .filter { it.contains(input, ignoreCase = true) }
                .distinct()
                .take(6)
        }
    }

    suspend fun askBot(question: String) = api.askBot(ChatRequest(question))

    suspend fun refreshProfile(): UserProfile = api.getProfile(token).also { userProfile = it }

    suspend fun updateProfile(latitude: Double? = null, longitude: Double? = null): UserProfile =
        api.updateProfile(token, UpdateProfileRequest(latitude = latitude, longitude = longitude)).also {
            userProfile = it
        }

    suspend fun logout() {
        token = ""
        userProfile = null
    }

    suspend fun uploadProductImage(context: Context, imageUri: Uri): String {
        val tempFile = createTempFileFromUri(context, imageUri)
        val mimeType = context.contentResolver.getType(imageUri) ?: "application/octet-stream"
        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        return api.uploadProductImage(token, body).url
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val fileName = queryFileName(context, uri)
        val suffix = fileName.substringAfterLast('.', "tmp")
        val tempFile = File.createTempFile("upload_", ".$suffix", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun queryFileName(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndexOpenableColumnsDisplayName()
            if (it.moveToFirst() && nameIndex != -1) {
                it.getString(nameIndex)
            } else {
                uri.lastPathSegment ?: "upload"
            }
        } ?: uri.lastPathSegment ?: "upload"
    }

    private fun android.database.Cursor.getColumnIndexOpenableColumnsDisplayName(): Int {
        return getColumnIndex("_display_name").takeIf { it != -1 } ?: getColumnIndex("displayName")
    }

    suspend fun adminCreateProduct(product: Product): Product = api.createProduct(token, product)
    suspend fun updateProduct(product: Product): Product = api.updateProduct(token, product.id, product)
    suspend fun adminDeleteProduct(productId: Int) = api.deleteProduct(token, productId)
}
