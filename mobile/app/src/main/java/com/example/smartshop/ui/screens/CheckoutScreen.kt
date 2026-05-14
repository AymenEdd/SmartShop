package com.example.smartshop.ui.screens

import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.viewmodel.CartViewModel
import com.example.smartshop.viewmodel.CheckoutViewModel
import com.example.smartshop.viewmodel.ProfileViewModel
import com.example.smartshop.viewmodel.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun CheckoutScreen(
    vm: CheckoutViewModel,
    cartVm: CartViewModel,
    profileVm: ProfileViewModel,
    onSuccess: () -> Unit,
) {
    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var geocodingLoading by remember { mutableStateOf(false) }
    var geocodingError   by remember { mutableStateOf<String?>(null) }

    val profile     = profileVm.profile
    val hasLocation = profile?.latitude != null && profile?.longitude != null

    fun fillAddressFromLocation() {
        val lat = profile?.latitude ?: return
        val lng = profile?.longitude ?: return
        coroutineScope.launch {
            geocodingLoading = true
            geocodingError   = null
            try {
                val address = withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    val results = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)
                    results?.firstOrNull()?.getAddressLine(0) ?: "$lat, $lng"
                }
                vm.shippingAddress = address
            } catch (e: Exception) {
                vm.shippingAddress = "$lat, $lng"
            }
            geocodingLoading = false
        }
    }

    LaunchedEffect(Unit) { profileVm.loadProfile() }

    Column(
        Modifier
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Checkout",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    "Almost there — confirm your order",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextColor
                )
            }
        }

        // ── CONTENT ────────────────────────────────────────────────────────
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // Shipping & payment card
            Card(
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = CardSoftColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier  = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Section label
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .background(AccentColor, RoundedCornerShape(2.dp))
                        )
                        Text(
                            "Delivery details",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    // Shipping address field
                    OutlinedTextField(
                        value         = vm.shippingAddress,
                        onValueChange = { vm.shippingAddress = it },
                        modifier      = Modifier.fillMaxWidth(),
                        label         = { Text("Shipping address") },
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = SurfaceColor,
                            focusedContainerColor   = SurfaceColor,
                            unfocusedBorderColor    = BorderColor,
                            focusedBorderColor      = AccentColor,
                        ),
                        trailingIcon = {
                            if (geocodingLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        },
                    )

                    // Use saved location button
                    if (hasLocation) {
                        OutlinedButton(
                            onClick  = { fillAddressFromLocation() },
                            enabled  = !geocodingLoading,
                            shape    = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
                            border   = BorderStroke(1.dp, BorderColor),
                        ) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (geocodingLoading) "Locating…" else "Use my saved location",
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        geocodingError?.let { Text(it, color = ErrorRedColor, fontSize = 12.sp) }
                    }

                    // Payment method
                    var paymentExpanded by remember { mutableStateOf(false) }
                    val paymentOptions = listOf("Card", "Payment on delivery")

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value         = vm.paymentMethod,
                            onValueChange = { },
                            modifier      = Modifier.fillMaxWidth(),
                            label         = { Text("Payment method") },
                            shape         = RoundedCornerShape(12.dp),
                            readOnly      = true,
                            colors        = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = SurfaceColor,
                                focusedContainerColor   = SurfaceColor,
                                unfocusedBorderColor    = BorderColor,
                                focusedBorderColor      = AccentColor,
                            ),
                            trailingIcon = {
                                Icon(
                                    imageVector = if (paymentExpanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { paymentExpanded = !paymentExpanded }
                                )
                            },
                        )
                        DropdownMenu(
                            expanded         = paymentExpanded,
                            onDismissRequest = { paymentExpanded = false },
                            modifier         = Modifier.fillMaxWidth(0.95f)
                        ) {
                            paymentOptions.forEach { option ->
                                DropdownMenuItem(
                                    text    = { Text(option) },
                                    onClick = { vm.paymentMethod = option; paymentExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // Order summary card
            Card(
                shape    = RoundedCornerShape(16.dp),
                colors   = CardDefaults.cardColors(containerColor = HeroColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "Order summary",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            "${cartVm.cartItems.size} item${if (cartVm.cartItems.size != 1) "s" else ""}",
                            color    = MutedTextColor,
                            fontSize = 12.sp,
                        )
                    }
                    Text(
                        "${"%.2f".format(cartVm.total)} EUR",
                        color      = AccentColor,
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            // Action button
            when (val state = vm.state) {
                UiState.Loading -> {
                    Button(
                        onClick  = {},
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        enabled  = false,
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    }
                }
                is UiState.Error -> {
                    Text(state.message, color = ErrorRedColor)
                    Button(
                        onClick  = vm::confirmOrder,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    ) { Text("Retry", fontWeight = FontWeight.SemiBold, color = Color.White) }
                }
                is UiState.Success -> {
                    Card(
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "✓  Order #${state.data.id} confirmed",
                            modifier   = Modifier.padding(16.dp),
                            color      = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Button(
                        onClick  = onSuccess,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    ) { Text("Continue shopping", fontWeight = FontWeight.SemiBold, color = Color.White) }
                }
                else -> {
                    Button(
                        onClick  = vm::confirmOrder,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    ) { Text("Confirm order", fontWeight = FontWeight.SemiBold, color = Color.White) }
                }
            }
        }
    }
}