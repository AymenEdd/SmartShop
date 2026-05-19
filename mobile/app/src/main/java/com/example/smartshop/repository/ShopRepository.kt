package com.example.smartshop.repository

import android.content.Context
import android.net.Uri
import com.example.smartshop.network.CartAddRequest
import com.example.smartshop.network.ChatRequest
import com.example.smartshop.network.CreateOrderRequest
import com.example.smartshop.network.ImageUploadResponse
import com.example.smartshop.network.Order
import com.example.smartshop.network.Product
import com.example.smartshop.network.RecommendationResponse
import com.example.smartshop.network.RegisterRequest
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.network.SearchRequest
import com.example.smartshop.network.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.math.absoluteValue

class ShopRepository {
    private val api = RetrofitClient.api
    private val firebaseAuth = FirebaseAuth.getInstance()
    var token: String = ""
    var userProfile: UserProfile? = null
    private val localCartOverrides = mutableMapOf<Int, Int>()
    private var cachedProducts: List<Product> = emptyList()

    init {
        firebaseAuth.currentUser?.let { user ->
            userProfile = user.toUserProfile()
        }
    }

    suspend fun login(email: String, password: String) {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase login failed")
        token = user.getBearerToken()
        userProfile = user.toUserProfile()
        userProfile = api.getProfile(authHeader())
    }

    suspend fun register(email: String, fullName: String, password: String, latitude: Double? = null, longitude: Double? = null) {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("Firebase registration failed")
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(fullName)
            .build()
        user.updateProfile(profileUpdates).await()
        token = user.getBearerToken(forceRefresh = true)
        userProfile = user.toUserProfile(latitude = latitude, longitude = longitude)
        syncBackendRegister(email, fullName, password, latitude, longitude)
        userProfile = api.getProfile(authHeader())
    }

    suspend fun getUsers(): List<UserProfile> = api.getUsers(authHeader())
    suspend fun getAdminOrders() = api.getAdminOrders(authHeader())
    suspend fun getAdminOrderById(orderId: Int) = api.getAdminOrderById(authHeader(), orderId)

    suspend fun getProducts(): List<Product> {
        cachedProducts = api.getProducts()
        return cachedProducts
    }

    suspend fun getProductById(productId: Int): Product = api.getProductById(productId)

    suspend fun getCart() = api.getCart(authHeader()).map {
        val overrideQuantity = localCartOverrides[it.id]
        if (overrideQuantity != null) it.copy(quantity = overrideQuantity) else it
    }

    suspend fun addToCart(productId: Int, quantity: Int) = api.addToCart(authHeader(), CartAddRequest(productId, quantity))

    suspend fun removeCartItem(cartItemId: Int) {
        api.removeCartItem(authHeader(), cartItemId)
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
        api.createOrder(authHeader(), CreateOrderRequest(shippingAddress.takeIf { it.isNotBlank() }))
    suspend fun getOrders(): List<Order> = api.getOrders(authHeader())
    suspend fun getPersonalizedRecommendations(): RecommendationResponse =
        api.getPersonalizedRecommendations(authHeader())

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

    suspend fun refreshProfile(): UserProfile {
        val currentProfile = currentFirebaseProfile() ?: error("No Firebase user is signed in")
        userProfile = runCatching { api.getProfile(authHeader()) }
            .getOrElse {
                currentProfile.copy(
                    latitude = userProfile?.latitude,
                    longitude = userProfile?.longitude,
                )
            }
        return userProfile ?: currentProfile
    }

    suspend fun updateProfile(latitude: Double? = null, longitude: Double? = null): UserProfile {
        val currentProfile = currentFirebaseProfile() ?: error("No Firebase user is signed in")
        userProfile = runCatching {
            api.updateProfile(
                authHeader(),
                com.example.smartshop.network.UpdateProfileRequest(
                    latitude = latitude ?: userProfile?.latitude,
                    longitude = longitude ?: userProfile?.longitude,
                ),
            )
        }.getOrElse {
            currentProfile.copy(
                latitude = latitude ?: userProfile?.latitude,
                longitude = longitude ?: userProfile?.longitude,
            )
        }
        return userProfile ?: currentProfile
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        token = ""
        userProfile = null
    }

    suspend fun uploadProductImage(context: Context, imageUri: Uri): String {
        val tempFile = createTempFileFromUri(context, imageUri)
        val mimeType = context.contentResolver.getType(imageUri) ?: "application/octet-stream"
        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
        return api.uploadProductImage(authHeader(), body).url
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

    suspend fun adminCreateProduct(product: Product): Product = api.createProduct(authHeader(), product)
    suspend fun updateProduct(product: Product): Product = api.updateProduct(authHeader(), product.id, product)
    suspend fun adminDeleteProduct(productId: Int) = api.deleteProduct(authHeader(), productId)

    fun currentFirebaseProfile(): UserProfile? {
        val user = firebaseAuth.currentUser ?: return null
        return user.toUserProfile().also { userProfile = it }
    }

    private suspend fun com.google.firebase.auth.FirebaseUser.getBearerToken(forceRefresh: Boolean = false): String {
        val idToken = getIdToken(forceRefresh).await().token.orEmpty()
        return if (idToken.isBlank()) "" else "Bearer $idToken"
    }

    private suspend fun authHeader(forceRefresh: Boolean = false): String {
        val user = firebaseAuth.currentUser ?: error("No Firebase user is signed in")
        token = user.getBearerToken(forceRefresh)
        if (token.isBlank()) error("Firebase token is empty")
        return token
    }

    private fun com.google.firebase.auth.FirebaseUser.toUserProfile(
        latitude: Double? = null,
        longitude: Double? = null,
    ): UserProfile {
        return UserProfile(
            id = uid.hashCode().absoluteValue,
            email = email.orEmpty(),
            full_name = displayName?.takeIf { it.isNotBlank() } ?: email.orEmpty().substringBefore("@"),
            is_admin = false,
            latitude = latitude,
            longitude = longitude,
        )
    }

    private suspend fun syncBackendRegister(
        email: String,
        fullName: String,
        password: String,
        latitude: Double?,
        longitude: Double?,
    ) {
        runCatching {
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
    }

}
