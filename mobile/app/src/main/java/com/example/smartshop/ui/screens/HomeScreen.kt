package com.example.smartshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartshop.ui.components.*
import com.example.smartshop.viewmodel.HomeViewModel
import com.example.smartshop.viewmodel.UiState

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onOpenSearch: () -> Unit,
    onOpenAllProducts: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenProduct: (Int) -> Unit,
) {
    var searchInput by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { vm.loadHome() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceColor)
                    .padding(horizontal = 16.dp)
                    .padding(top = 18.dp, bottom = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column {

                        Text(
                            text  = "SmartShop",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                        )
                    }

                }

                Spacer(Modifier.height(14.dp))

                // Search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface2Color)
                        .padding(horizontal = 19.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = TextMuted,
                        modifier = Modifier.size(17.dp),
                    )
                    TextField(
                        value         = searchInput,
                        onValueChange = {
                            searchInput = it
                            vm.fetchAutocomplete(it)
                        },
                        placeholder = {
                            Text(
                                "Search products…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted2,
                            )
                        },
                        singleLine = true,
                        modifier   = Modifier.weight(1f),
                        colors     = TextFieldDefaults.colors(
                            focusedContainerColor   = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor   = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                    )
                    if (searchInput.isNotEmpty()) {
                        IconButton(
                            onClick  = { onOpenSearch() },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Go",
                                tint = AccentColor,
                            )
                        }
                    }
                }
            }
        }

        // ── Category chips ───────────────────────────────────────────────────
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(vm.categories) { cat ->
                    CategoryChip(
                        label    = cat,
                        selected = selectedCat == cat,
                        onClick  = { selectedCat = cat },
                    )
                }
            }
        }

        // ── Hero banner ──────────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape  = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = AccentBgColor),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, HeroBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Background emoji
                    Text(
                        text = "⌚",
                        fontSize = 52.sp,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                            .alpha(.5f),
                    )
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Pill badge
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentColor)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text  = "Limited offer",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                            )
                        }
                        Spacer(Modifier.height(9.dp))
                        Text(
                            text  = "Luxury picks\nfor every occasion",
                            style = MaterialTheme.typography.titleLarge,
                            color = HeroTextDark,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text  = "Curated fashion, watches and signature accessories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HeroTextMid,
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onOpenSearch,
                            shape  = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text  = "Shop now",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }

        // ── AI autocomplete chips ─────────────────────────────────────────────
        if (vm.aiSuggestions.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(vm.aiSuggestions) { suggestion ->
                        AssistChip(
                            onClick = onOpenSearch,
                            label   = { Text(suggestion, style = MaterialTheme.typography.bodySmall) },
                        )
                    }
                }
            }
        }

        // ── Recommended ───────────────────────────────────────────────────────
        item { SectionHeader(title = "Recommended", actionLabel = "See all", onAction = onOpenAllProducts) }
        item {
            val filtered = if (selectedCat == "All") vm.recommended
            else vm.recommended.filter { it.category == selectedCat }
            ProductRow(products = filtered, onOpenProduct = onOpenProduct)
            Spacer(Modifier.height(4.dp))
        }

        // ── Trending ──────────────────────────────────────────────────────────
        item { SectionHeader(title = "Trending now", onAction = {}) }
        item {
            ProductRow(products = vm.trending, onOpenProduct = onOpenProduct)
            Spacer(Modifier.height(16.dp))
        }

        // ── Loading / error ───────────────────────────────────────────────────
        when (val state = vm.homeState) {
            UiState.Loading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = AccentColor) }
            }
            is UiState.Error -> item {
                Text(
                    text     = state.message,
                    color    = RedColor,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            else -> {}
        }
    }
}

// Extension helpers
private fun Modifier.alpha(a: Float) = this.then(
    Modifier.graphicsLayer { alpha = a }
)
