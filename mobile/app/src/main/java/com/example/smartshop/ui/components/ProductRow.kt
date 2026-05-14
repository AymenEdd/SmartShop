package com.example.smartshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.smartshop.network.Product
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.ui.screens.AccentBgColor
import com.example.smartshop.ui.screens.AccentColor
import com.example.smartshop.ui.screens.AmberColor
import com.example.smartshop.ui.screens.Border2Color
import com.example.smartshop.ui.screens.BorderColor
import com.example.smartshop.ui.screens.HeroBorderColor
import com.example.smartshop.ui.screens.HeroTextDark
import com.example.smartshop.ui.screens.HeroTextMid
import com.example.smartshop.ui.screens.Surface2Color
import com.example.smartshop.ui.screens.SurfaceColor
import com.example.smartshop.ui.screens.TextMuted

// ── ProductCard ───────────────────────────────────────────────────────────────
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .width(152.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        val imageUrl = RetrofitClient.resolveUrl(product.image_urls?.firstOrNull())
        if (!imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                loading = { ProductImageFallback(product.category) },
                error = { ProductImageFallback(product.category) },
            )
        } else {
            ProductImageFallback(product.category)
       }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text  = product.category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = AccentColor,
                modifier = Modifier.padding(bottom = 2.dp),
            )
            Text(
                text     = product.name,
                style    = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "€${"%,.0f".format(product.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = AccentColor,
                    fontWeight = FontWeight.SemiBold,
                )
                ProductRatingBadge(product.rating)
            }
        }
    }
}

@Composable
private fun ProductRatingBadge(rating: Double?) {
    if (rating == null) {
        Text(
            text = "New",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
        )
        return
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            tint = AmberColor,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.bodySmall,
            color = AmberColor,
        )
    }
}

// ── ProductRow ────────────────────────────────────────────────────────────────
@Composable
fun ProductRow(
    products: List<Product>,
    onOpenProduct: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(
                product = product,
                onClick  = { onOpenProduct(product.id) },
            )
        }
    }
}

// ── SectionHeader ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    title: String,
    actionLabel: String = "See all",
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (onAction != null) {
            Text(
                text     = actionLabel,
                style    = MaterialTheme.typography.bodySmall,
                color    = AccentColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

// ── CategoryChip ──────────────────────────────────────────────────────────────
@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg      = if (selected) AccentColor else SurfaceColor
    val fg      = if (selected) Color.White  else TextMuted
    val border  = if (selected) AccentColor  else Border2Color

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(0.5.dp, border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── TagPill ───────────────────────────────────────────────────────────────────
@Composable
fun TagPill(label: Int) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Surface2Color)
            .border(0.5.dp, Border2Color, CircleShape)
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(
            text  = label.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── PrimaryButton ─────────────────────────────────────────────────────────────
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
    ) {
        Text(
            text  = text,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

// ── OutlineButton ─────────────────────────────────────────────────────────────
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        shape  = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentColor),
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// ── AiAnswerCard ──────────────────────────────────────────────────────────────
@Composable
fun AiAnswerCard(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AccentBgColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, HeroBorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 7.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Star, // swap for Sparkles when available
                    contentDescription = null,
                    tint = AccentColor,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text  = "AI answer",
                    style = MaterialTheme.typography.labelSmall,
                    color = HeroTextDark,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                text  = text,
                style = MaterialTheme.typography.bodyMedium,
                color = HeroTextMid,
            )
        }
    }
}

// ── FieldInput ────────────────────────────────────────────────────────────────
@Composable
fun FieldInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text  = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            letterSpacing = 0.3.sp,
        )
        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            modifier      = Modifier.fillMaxWidth(),
            singleLine    = true,
            keyboardOptions = keyboardOptions,
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AccentColor,
                unfocusedBorderColor = Border2Color,
                focusedContainerColor   = Surface2Color,
                unfocusedContainerColor = Surface2Color,
            ),
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ProductImageFallback(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .background(AccentBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
            )
        }
    }
}
