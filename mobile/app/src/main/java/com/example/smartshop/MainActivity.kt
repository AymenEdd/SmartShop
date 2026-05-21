package com.example.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartshop.ui.screens.AccentBgColor
import com.example.smartshop.ui.screens.AccentColor
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.smartshop.ui.screens.AdminDashboardScreen
import com.example.smartshop.ui.screens.AdminOrderDetailScreen
import com.example.smartshop.ui.screens.CartScreen
import com.example.smartshop.ui.screens.ChatbotScreen
import com.example.smartshop.ui.screens.CheckoutScreen
import com.example.smartshop.ui.screens.HomeScreen
import com.example.smartshop.ui.screens.LoginScreen
import com.example.smartshop.ui.screens.ProductDetailScreen
import com.example.smartshop.ui.screens.ProductsScreen
import com.example.smartshop.ui.screens.ProfileScreen
import com.example.smartshop.ui.screens.SmartSearchScreen
import com.example.smartshop.ui.screens.UserDetailScreen
import com.example.smartshop.ui.screens.RegisterScreen
import com.example.smartshop.viewmodel.AdminDashboardViewModel
import com.example.smartshop.viewmodel.AuthViewModel
import com.example.smartshop.viewmodel.CartViewModel
import com.example.smartshop.viewmodel.ChatbotViewModel
import com.example.smartshop.viewmodel.CheckoutViewModel
import com.example.smartshop.viewmodel.HomeViewModel
import com.example.smartshop.viewmodel.ProductDetailsViewModel
import com.example.smartshop.viewmodel.ProductsViewModel
import com.example.smartshop.viewmodel.ProfileViewModel
import com.example.smartshop.viewmodel.SearchViewModel

data class BottomNavItem(val route: String, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Filled.Home, "Home"),
    BottomNavItem("search", Icons.Filled.Search, "Search"),
    BottomNavItem("chatbot", Icons.Filled.Email, "Chat"),
    BottomNavItem("cart", Icons.Filled.ShoppingCart, "Cart"),
    BottomNavItem("profile", Icons.Filled.Person, "Profile")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartShopApp() }
    }
}

@Composable
fun SmartShopApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()
    val productDetailsViewModel: ProductDetailsViewModel = viewModel()
    val productsViewModel: ProductsViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val checkoutViewModel: CheckoutViewModel = viewModel()
    val chatbotViewModel: ChatbotViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val adminViewModel: AdminDashboardViewModel = viewModel()

    LaunchedEffect(authViewModel.isLoggedIn) {
        cartViewModel.isLoggedIn = authViewModel.isLoggedIn
        profileViewModel.isLoggedIn = authViewModel.isLoggedIn
    }

    val currentRoute by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            val currentRouteValue = currentRoute?.destination?.route
            val bottomItems = if (authViewModel.isAdmin) {
                bottomNavItems + BottomNavItem("admin", Icons.Filled.Lock, "Admin")
            } else {
                bottomNavItems
            }
            if (currentRouteValue in bottomItems.map { it.route }) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp),
                        tonalElevation = 8.dp,
                        containerColor = Color.White,
                    ) {
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRouteValue == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = AccentColor,
                                    unselectedIconColor = Color(0xFF8A94A6),
                                    selectedTextColor = AccentColor,
                                    unselectedTextColor = Color(0xFF8A94A6),
                                    indicatorColor = AccentBgColor,
                                ),
                            )
                        }
                    }
                }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    vm = authViewModel,
                    onSuccess = {
                        val redirect = authViewModel.redirectRouteAfterLogin
                        authViewModel.redirectRouteAfterLogin = null
                        navController.navigate(redirect ?: "home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(authViewModel) {
                    val redirect = authViewModel.redirectRouteAfterLogin
                    authViewModel.redirectRouteAfterLogin = null
                    navController.navigate(redirect ?: "home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            }
            composable("home") {
                HomeScreen(
                    vm = homeViewModel,
                    onOpenSearch = { navController.navigate("search") },
                    onOpenAllProducts = { navController.navigate("all_products") },
                    onOpenCart = { navController.navigate("cart") },
                    onOpenProduct = { productId -> navController.navigate("product/$productId") }
                )
            }
            composable("search") {
                SmartSearchScreen(searchViewModel) { navController.navigate("product/$it") }
            }
            composable("all_products") {
                ProductsScreen(
                    vm = productsViewModel,
                    onOpenProduct = { navController.navigate("product/$it") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("product/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                ProductDetailScreen(
                    vm = productDetailsViewModel,
                    productId = id,
                    onBack = { navController.popBackStack() },
                    onAdded = { navController.navigate("cart") },
                    isLoggedIn = authViewModel.isLoggedIn,
                    onRequireLogin = {
                        authViewModel.redirectRouteAfterLogin = "profile"
                        navController.navigate("login")
                    }
                )
            }
            composable("cart") {
                CartScreen(
                    vm = cartViewModel,
                    onCheckout = { navController.navigate("checkout") },
                    onOpenChatbot = { navController.navigate("chatbot") },
                    onLoginClick = {
                        authViewModel.redirectRouteAfterLogin = "cart"
                        navController.navigate("login")
                    }
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    vm = checkoutViewModel,
                    cartVm = cartViewModel,
                    profileVm = profileViewModel,
                    onSuccess = { navController.navigate("home") }
                )
            }
            composable("chatbot") {
                ChatbotScreen(chatbotViewModel)
            }
            composable("profile") {
                ProfileScreen(
                    vm = profileViewModel,
                    onLoginClick = {
                        authViewModel.redirectRouteAfterLogin = "profile"
                        navController.navigate("login")
                    },
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
            composable("admin") {
                AdminDashboardScreen(
                    vm = adminViewModel,
                    onNavigateToUserDetail = { user ->
                        adminViewModel.selectedUser = user
                        navController.navigate("user_detail")
                    },
                    onNavigateToOrderDetail = { order ->
                        adminViewModel.selectedOrder = order
                        navController.navigate("admin_order_detail")
                    },
                )
            }
            composable("admin_order_detail") {
                AdminOrderDetailScreen(adminViewModel.selectedOrder) { navController.popBackStack() }
            }
            composable("user_detail") {
                val user = adminViewModel.selectedUser
                if (user != null) {
                    UserDetailScreen(user) { navController.popBackStack() }
                }
            }
        }
    }
}
