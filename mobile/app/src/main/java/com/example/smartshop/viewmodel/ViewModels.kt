package com.example.smartshop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartshop.network.CartItem
import com.example.smartshop.network.AdminOrder
import com.example.smartshop.network.Order
import com.example.smartshop.network.Product
import com.example.smartshop.network.UserProfile
import com.example.smartshop.repository.ShopRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AppContainer {
    val repo = ShopRepository()
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class AuthViewModel : ViewModel() {
    var loginError by mutableStateOf<String?>(null)
    var registerError by mutableStateOf<String?>(null)
    var authLoading by mutableStateOf(false)
    var userProfile by mutableStateOf<UserProfile?>(null)
    var redirectRouteAfterLogin: String? = null
    val isAdmin get() = userProfile?.is_admin == true
    val isLoggedIn get() = userProfile != null

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authLoading = true
            loginError = null
            try {
                AppContainer.repo.login(email, password)
                userProfile = AppContainer.repo.userProfile
                onSuccess()
            } catch (e: Exception) {
                loginError = e.message ?: "Login failed. Please check your connection and try again."
                e.printStackTrace()
            } finally {
                authLoading = false
            }
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            runCatching { AppContainer.repo.refreshProfile() }
                .onSuccess { userProfile = it }
        }
    }

    fun register(email: String, fullName: String, password: String, latitude: Double? = null, longitude: Double? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authLoading = true
            runCatching { AppContainer.repo.register(email, fullName, password, latitude, longitude) }
                .onSuccess { onSuccess() }
                .onFailure { registerError = it.message }
            authLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            AppContainer.repo.logout()
            userProfile = null
        }
    }
}

class HomeViewModel : ViewModel() {
    var homeState by mutableStateOf<UiState<List<Product>>>(UiState.Idle)
    var categories by mutableStateOf(listOf("All"))
    var aiSuggestions by mutableStateOf<List<String>>(emptyList())
    var recommended by mutableStateOf<List<Product>>(emptyList())
    var trending by mutableStateOf<List<Product>>(emptyList())
    var promotionText by mutableStateOf("AI Deal: up to 25% on products you are likely to buy")
    private var suggestionJob: Job? = null

    fun loadHome() {
        viewModelScope.launch {
            homeState = UiState.Loading
            runCatching { AppContainer.repo.getProducts() }
                .onSuccess {
                    categories = listOf("All") + it.map { p -> p.category }.filter { c -> c.isNotBlank() }.distinct().sorted()
                    val top = it.take(8)
                    trending = top.sortedByDescending { p -> p.stock }.take(6)
                    recommended = runCatching {
                        AppContainer.repo.getPersonalizedRecommendations().products
                    }.getOrDefault(top.take(6))
                    homeState = UiState.Success(it)
                }
                .onFailure {
                    homeState = UiState.Error(it.message ?: "Home loading failed")
                }
        }
    }

    fun fetchAutocomplete(input: String) {
        suggestionJob?.cancel()
        if (input.length < 2) {
            aiSuggestions = emptyList()
            return
        }
        suggestionJob = viewModelScope.launch {
            delay(250)
            aiSuggestions = AppContainer.repo.getSuggestions(input)
        }
    }
}

class SearchViewModel : ViewModel() {
    var query by mutableStateOf("")
    var selectedCategory by mutableStateOf<String?>(null)
    var minPrice by mutableStateOf("")
    var maxPrice by mutableStateOf("")
    var minRating by mutableStateOf("0")
    var searchHistory = mutableStateListOf<String>()
    var suggestions by mutableStateOf<List<String>>(emptyList())
    var state by mutableStateOf<UiState<List<Product>>>(UiState.Idle)
    private var searchJob: Job? = null

    fun onQueryChange(value: String) {
        query = value
        searchJob?.cancel()
        if (value.length < 2) {
            suggestions = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(200)
            suggestions = AppContainer.repo.getSuggestions(value)
            search()
        }
    }

    fun setVoiceQuery(mockVoiceText: String) {
        query = mockVoiceText
        search()
    }

    fun search() {
        if (query.isBlank()) return
        viewModelScope.launch {
            state = UiState.Loading
            val results = runCatching {
                AppContainer.repo.semanticSearch(
                    query = query,
                    minPrice = minPrice.toDoubleOrNull(),
                    maxPrice = maxPrice.toDoubleOrNull(),
                    category = selectedCategory,
                    minRating = minRating.toDoubleOrNull()
                )
            }
            results.onSuccess {
                if (searchHistory.firstOrNull() != query) searchHistory.add(0, query)
                state = UiState.Success(it)
            }.onFailure {
                state = UiState.Error(it.message ?: "Semantic search failed")
            }
        }
    }
}

class ProductDetailsViewModel : ViewModel() {
    var productState by mutableStateOf<UiState<Product>>(UiState.Idle)
    var similarProducts by mutableStateOf<List<Product>>(emptyList())
    var aiAnswer by mutableStateOf("")

    fun load(productId: Int) {
        viewModelScope.launch {
            productState = UiState.Loading
            runCatching { AppContainer.repo.getProductById(productId) }
                .onSuccess { p ->
                    productState = UiState.Success(p)
                    similarProducts = runCatching {
                        AppContainer.repo.getSimilarProducts(productId).products
                    }.getOrDefault(emptyList())
                }
                .onFailure { productState = UiState.Error(it.message ?: "Product loading failed") }
        }
    }

    fun addToCart(productId: Int) {
        viewModelScope.launch { runCatching { AppContainer.repo.addToCart(productId, 1) } }
    }

    fun askAi(productName: String) {
        viewModelScope.launch {
            aiAnswer = "Loading AI analysis..."
            val result = runCatching {
                AppContainer.repo.askBot("Explain pros/cons of $productName and compare alternatives.")
            }
            aiAnswer = result.fold(
                onSuccess = { it.answer.takeIf { ans -> ans.isNotBlank() } ?: "No analysis available" },
                onFailure = { "Unable to get AI analysis. Please try again." }
            )
        }
    }
}

class ProductsViewModel : ViewModel() {
    var state by mutableStateOf<UiState<List<Product>>>(UiState.Idle)
    var categories by mutableStateOf(listOf("All"))
    var selectedCategory by mutableStateOf("All")

    val visibleProducts: List<Product>
        get() {
            val products = (state as? UiState.Success<List<Product>>)?.data ?: emptyList()
            return if (selectedCategory == "All") products else products.filter { it.category == selectedCategory }
        }

    fun loadProducts() {
        viewModelScope.launch {
            state = UiState.Loading
            runCatching { AppContainer.repo.getProducts() }
                .onSuccess {
                    categories = listOf("All") + it.map { p -> p.category }.filter { c -> c.isNotBlank() }.distinct().sorted()
                    if (selectedCategory !in categories) selectedCategory = "All"
                    state = UiState.Success(it)
                }
                .onFailure { state = UiState.Error(it.message ?: "Could not load products") }
        }
    }
}

class CartViewModel : ViewModel() {
    var cartItems by mutableStateOf<List<CartItem>>(emptyList())
    var cartState by mutableStateOf<UiState<List<CartItem>>>(UiState.Idle)
    var total by mutableStateOf(0.0)

    // Set by MainActivity when auth state changes
    var isLoggedIn by mutableStateOf(false)

    fun loadCart() {
        viewModelScope.launch {
            cartState = UiState.Loading
            runCatching { AppContainer.repo.getCart() }
                .onSuccess {
                    cartItems = it
                    total = cartItems.sumOf { item -> item.product.price * item.quantity }
                    cartState = UiState.Success(it)
                }
                .onFailure {
                    cartItems = emptyList()
                    total = 0.0
                    cartState = UiState.Error(it.message ?: "Cart sync failed")
                }
        }
    }

    fun increment(item: CartItem) {
        viewModelScope.launch {
            AppContainer.repo.setCartQuantity(item.id, item.quantity, item.quantity + 1)
            loadCart()
        }
    }

    fun decrement(item: CartItem) {
        viewModelScope.launch {
            AppContainer.repo.setCartQuantity(item.id, item.quantity, item.quantity - 1)
            loadCart()
        }
    }

    fun remove(item: CartItem) {
        viewModelScope.launch {
            runCatching { AppContainer.repo.removeCartItem(item.id) }
            loadCart()
        }
    }
}

class CheckoutViewModel : ViewModel() {
    var shippingAddress by mutableStateOf("")
    var paymentMethod by mutableStateOf("Card")
    var state by mutableStateOf<UiState<Order>>(UiState.Idle)

    fun confirmOrder() {
        if (shippingAddress.isBlank()) {
            state = UiState.Error("Shipping address is required")
            return
        }
        viewModelScope.launch {
            state = UiState.Loading
            runCatching { AppContainer.repo.createOrder(shippingAddress.trim()) }
                .onSuccess { state = UiState.Success(it) }
                .onFailure { state = UiState.Error(it.message ?: "Checkout failed") }
        }
    }
}

class ChatbotViewModel : ViewModel() {
    data class ChatMessage(val text: String, val isUser: Boolean)
    var messages = mutableStateListOf<ChatMessage>()
    var loading by mutableStateOf(false)
    var input by mutableStateOf("")

    fun ask(question: String = input) {
        if (question.isBlank()) return
        messages.add(ChatMessage(question, true))
        input = ""
        viewModelScope.launch {
            loading = true
            runCatching { AppContainer.repo.askBot(question).answer }
                .onSuccess {
                    messages.add(ChatMessage(it, false))
                }
                .onFailure {
                    messages.add(ChatMessage(it.message ?: "Chatbot error", false))
                }
            loading = false
        }
    }
}

class ProfileViewModel : ViewModel() {
    var profile by mutableStateOf<UserProfile?>(null)
    var ordersState by mutableStateOf<UiState<List<Order>>>(UiState.Idle)
    var personalized by mutableStateOf<List<Product>>(emptyList())

    // Set by MainActivity when auth state changes
    var isLoggedIn by mutableStateOf(false)

    fun loadProfile() {
        viewModelScope.launch {
            ordersState = UiState.Loading
            val profileResult = runCatching { AppContainer.repo.refreshProfile() }
            val ordersResult = runCatching { AppContainer.repo.getOrders() }
            val recoResult = runCatching { AppContainer.repo.getPersonalizedRecommendations().products }

            profileResult.onSuccess { profile = it }
                .onFailure { /* profile stays null */ }

            ordersResult.onSuccess { ordersState = UiState.Success(it) }
                .onFailure { ordersState = UiState.Error(it.message ?: "Orders loading failed") }
            personalized = recoResult.getOrDefault(emptyList())
        }
    }

    fun saveProfileLocation(latitude: Double?, longitude: Double?, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            runCatching { AppContainer.repo.updateProfile(latitude = latitude, longitude = longitude) }
                .onSuccess {
                    profile = it
                    onResult(true, null)
                }
                .onFailure {
                    onResult(false, it.message ?: "Failed to update location")
                }
        }
    }
}
}

class AdminDashboardViewModel : ViewModel() {
    lateinit var selectedUser: UserProfile
    lateinit var selectedOrder: AdminOrder
    var products by mutableStateOf<List<Product>>(emptyList())
    var orders by mutableStateOf<List<AdminOrder>>(emptyList())
    var users by mutableStateOf<List<UserProfile>>(emptyList())
    var insight by mutableStateOf("AI insight: waiting for data")

    fun loadData() {
        viewModelScope.launch {
            products = runCatching { AppContainer.repo.getProducts() }.getOrDefault(emptyList())
            orders = runCatching { AppContainer.repo.getAdminOrders() }.getOrDefault(emptyList())
            users = runCatching { AppContainer.repo.getUsers() }.getOrDefault(emptyList())
            val top = products.sortedByDescending { it.stock }.take(3).joinToString { it.name }
            insight = "AI insight: top stocked products now are $top"
        }
    }

    fun addProduct(name: String, category: String, price: Double, imageUrls: List<String> = emptyList()) {
        viewModelScope.launch {
            val product = Product(
                id = 0,
                name = name,
                description = "Created from admin dashboard",
                category = category,
                price = price,
                stock = 10,
                image_urls = imageUrls.takeIf { it.isNotEmpty() }
            )
            runCatching { AppContainer.repo.adminCreateProduct(product) }
            loadData()
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            runCatching { AppContainer.repo.updateProduct(product) }
            loadData()
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            runCatching { AppContainer.repo.adminDeleteProduct(id) }
            loadData()
        }
    }
}
