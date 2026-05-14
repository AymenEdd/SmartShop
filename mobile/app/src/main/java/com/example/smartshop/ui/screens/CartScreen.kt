package com.example.smartshop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.viewmodel.CartViewModel
import com.example.smartshop.viewmodel.UiState

@Composable
fun CartScreen(
    vm: CartViewModel,
    onCheckout: () -> Unit,
    onOpenChatbot: () -> Unit,
    onLoginClick: () -> Unit
) {
    // ── NOT LOGGED IN GATE ──────────────────────────────────────────────────
    if (!vm.isLoggedIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBgColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(HeroColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = AccentColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "You're not logged in",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Log in to view your cart and start shopping.",
                    color = MutedTextColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Log in", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
        return
    }

    // ── LOGGED IN CONTENT ───────────────────────────────────────────────────
    LaunchedEffect(Unit) { vm.loadCart() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor)
    ) {

        // ── HERO HEADER ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFCDE4FF), Color(0xFFF0F6FF))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = AccentColor,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        "Shopping Cart",
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary
                    )
                }
                Text(
                    "${vm.cartItems.size} item${if (vm.cartItems.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextColor
                )
            }
        }

        // ── CART ITEMS ─────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 8.dp)
        ) {
            items(vm.cartItems) { item ->
                CartItemRow(
                    item = item,
                    onIncrement = { vm.increment(item) },
                    onDecrement = { vm.decrement(item) },
                    onRemove = { vm.remove(item) }
                )
            }
        }

        // ── BOTTOM ZONE ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val state = vm.cartState) {
                UiState.Loading -> CircularProgressIndicator(color = AccentColor)
                is UiState.Error -> Text(state.message, color = ErrorRedColor)
                else -> {}
            }

            // Total card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = HeroColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        "${"%.2f".format(vm.total)} EUR",
                        color = AccentColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
            ) {
                Text("Checkout", fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            OutlinedButton(
                onClick = onOpenChatbot,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text("Ask shopping AI", color = AccentColor)
            }
        }
    }
}