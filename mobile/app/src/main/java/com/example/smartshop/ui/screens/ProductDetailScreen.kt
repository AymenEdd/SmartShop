package com.example.smartshop.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.ui.components.*
import com.example.smartshop.viewmodel.ProductDetailsViewModel
import com.example.smartshop.viewmodel.UiState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductDetailScreen(
    vm: ProductDetailsViewModel,
    productId: Int,
    onBack: () -> Unit,
    onAdded: () -> Unit,
    isLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
) {
    LaunchedEffect(productId) { vm.load(productId) }

    when (val state = vm.productState) {
        UiState.Loading -> Box(
            Modifier.fillMaxSize().background(BgColor),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator(color = AccentColor) }

        is UiState.Error -> Box(
            Modifier.fillMaxSize().background(BgColor),
            contentAlignment = Alignment.Center,
        ) { Text(state.message, color = RedColor) }

        is UiState.Success -> {
            val product = state.data
            val images  = product.image_urls?.map { RetrofitClient.resolveUrl(it) ?: it }
                ?: listOf(
                    "https://picsum.photos/seed/${product.id}a/700/400",
                    "https://picsum.photos/seed/${product.id}b/700/400",
                )
            val pagerState = rememberPagerState(pageCount = { images.size })

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgColor),
            ) {
                // ── Back button ──────────────────────────────────────────────
                item {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.padding(top = 16.dp, start = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted),
                    ) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Back", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // ── Image pager ──────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface2Color),
                    ) {
                        HorizontalPager(
                            state    = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            SubcomposeAsyncImage(
                                model          = images[page],
                                contentDescription = product.name,
                                contentScale   = ContentScale.Crop,
                                modifier       = Modifier.fillMaxSize(),
                                loading = { DetailImageFallback(product.category) },
                                error = { DetailImageFallback(product.category) },
                            )
                        }
                        // Pager dots
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            repeat(images.size) { i ->
                                val isActive = pagerState.currentPage == i
                                Box(
                                    modifier = Modifier
                                        .height(5.dp)
                                        .width(if (isActive) 16.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) AccentColor else TextMuted2),
                                )
                            }
                        }
                    }
                }

                // ── Product info ─────────────────────────────────────────────
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 14.dp),
                    ) {
                        // Category badge
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentBgColor)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text  = product.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = HeroTextMid,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text  = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                        )

                        Spacer(Modifier.height(10.dp))

                        // Price + rating row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text  = "€${"%.2f".format(product.price)}",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp),
                                color = AccentColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (product.rating != null) {
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(AmberBgColor)
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Outlined.Star,
                                        contentDescription = "Rating",
                                        tint = AmberColor,
                                        modifier = Modifier.size(13.dp),
                                    )
                                    Text(
                                        text  = "%.1f".format(product.rating),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AmberColor,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Description
                        Text(
                            text  = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                        )

                        Spacer(Modifier.height(14.dp))

                        // Tags
                        if (!product.category.isNullOrEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                modifier = Modifier.padding(bottom = 18.dp),
                            ) {
                                items(product.category) { TagPill(it) }
                            }
                        } else {
                            Spacer(Modifier.height(18.dp))
                        }

                        // CTA buttons
                        PrimaryButton(
                            text    = "Add to cart",
                            onClick = {
                                if (isLoggedIn) {
                                    vm.addToCart(productId)
                                    onAdded()
                                } else {
                                    onRequireLogin()
                                }
                            },
                        )
                        Spacer(Modifier.height(9.dp))
                        OutlineButton(
                            text    = "✦  Ask AI about this product",
                            onClick = { vm.askAi(product.name) },
                        )

                        // AI answer
                        if (vm.aiAnswer.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            AiAnswerCard(text = vm.aiAnswer)
                        }
                    }
                }

                // ── Similar products ─────────────────────────────────────────
                item {
                    SectionHeader(title = "Similar products")
                    ProductRow(
                        products      = vm.similarProducts,
                        onOpenProduct = {},
                    )
                    Spacer(Modifier.height(4.dp))
                }

                // ── Reviews ──────────────────────────────────────────────────
                item {
                    SectionHeader(title = "Reviews")
                    Card(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
                        border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        if (product.rating != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = AmberColor,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Average rating %.1f. Written reviews are not available yet.".format(product.rating),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                )
                            }
                        } else {
                            Text(
                                text = "No verified reviews yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                modifier = Modifier.padding(14.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        else -> {}
    }
}

@Composable
private fun DetailImageFallback(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccentBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                tint = AccentColor,
                modifier = Modifier.size(34.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun LazyListScope.items(
    count: String,
    itemContent: @Composable LazyItemScope.(Int) -> Unit
) {
}
