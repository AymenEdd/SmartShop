package com.example.smartshop.ui.screens

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.smartshop.network.AdminOrder
import com.example.smartshop.network.RetrofitClient

@Composable
fun AdminOrderDetailScreen(order: AdminOrder, onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFCDE4FF), Color(0xFFF0F6FF)),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.65f)),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = AccentColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AccentColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Order #${order.id}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                        )
                        Text(
                            text = formatOrderDate(order.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextColor,
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DetailSectionTitle("Customer")
                    DetailRow(icon = Icons.Outlined.Person, label = "Name", value = order.user.full_name)
                    DetailRow(icon = Icons.Outlined.Email, label = "Email", value = order.user.email)
                    DetailRow(
                        icon = Icons.Outlined.LocationOn,
                        label = "Shipping address",
                        value = order.shipping_address?.takeIf { it.isNotBlank() } ?: "No address provided",
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSoftColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailSectionTitle("Items")
                    Spacer(Modifier.height(6.dp))
                    order.items.forEachIndexed { index, item ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = BorderColor,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 10.dp),
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SubcomposeAsyncImage(
                                model = RetrofitClient.resolveUrl(item.product.image_urls?.firstOrNull()),
                                contentDescription = item.product.name,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Surface2Color),
                                contentScale = ContentScale.Crop,
                                loading = { OrderItemImageFallback() },
                                error = { OrderItemImageFallback() },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Qty ${item.quantity} x €${"%.2f".format(item.unit_price)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTextColor,
                                )
                            }
                            Text(
                                text = "€${"%.2f".format(item.quantity * item.unit_price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AccentBgColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedTextColor,
                        )
                        Text(
                            text = "${order.items.sumOf { it.quantity }} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                        )
                    }
                    Text(
                        text = "€${"%.2f".format(order.total_amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AccentColor,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(10.dp)) }
    }
}

@Composable
private fun OrderItemImageFallback() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(AccentBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.Image,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentColor),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(18.dp),
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedTextColor)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        }
    }
}
