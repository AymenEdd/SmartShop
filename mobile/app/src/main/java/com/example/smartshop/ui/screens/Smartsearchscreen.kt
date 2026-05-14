package com.example.smartshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.smartshop.network.Product
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.viewmodel.SearchViewModel
import com.example.smartshop.viewmodel.UiState

@Composable
fun SmartSearchScreen(vm: SearchViewModel, onOpenProduct: (Int) -> Unit) {
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
                    "Smart Search",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
                Text(
                    "Use AI filters to find the perfect match",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextColor
                )
            }
        }

        // ── FILTERS + RESULTS ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = vm.query,
                onValueChange = vm::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                placeholder = { Text("Semantic AI search…", color = TextMuted2) },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MutedTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceColor,
                    focusedContainerColor   = SurfaceColor,
                    unfocusedBorderColor    = BorderColor,
                    focusedBorderColor      = AccentColor,
                )
            )
            Spacer(Modifier.height(10.dp))

            // Price + Rating row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterField(vm.minPrice,  { vm.minPrice  = it }, "Min €",  Modifier.weight(1f), KeyboardType.Number)
                FilterField(vm.maxPrice,  { vm.maxPrice  = it }, "Max €",  Modifier.weight(1f), KeyboardType.Number)
                FilterField(vm.minRating, { vm.minRating = it }, "Min ★",  Modifier.weight(1f), KeyboardType.Decimal)
            }
            Spacer(Modifier.height(10.dp))

            // Action row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.fillMaxWidth()
            ) {

                Spacer(Modifier.weight(1f))
                Button(
                    onClick  = vm::search,
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = AccentColor),
                    modifier = Modifier.height(40.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text("Search", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                }
            }

            // Suggestions chips
            if (vm.suggestions.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(vm.suggestions) { s ->
                        SuggestionChip(
                            onClick = { vm.onQueryChange(s) },
                            label = { Text(s, fontSize = 12.sp, color = AccentColor, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(20.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = AccentBgColor),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = Border2Color,
                                borderWidth = 0.5.dp
                            )
                        )
                    }
                }
            }

            // Recent searches
            if (vm.searchHistory.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent:", style = MaterialTheme.typography.labelSmall, color = TextMuted2)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        vm.searchHistory.take(3).joinToString(" · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedTextColor
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Results
            when (val state = vm.state) {
                UiState.Loading -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentColor, strokeWidth = 2.5.dp)
                    }
                }
                is UiState.Error -> {
                    Surface(
                        color = RedBgColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(state.message, color = ErrorRedColor, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                    }
                }
                is UiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding        = PaddingValues(bottom = 32.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data) { p: Product ->
                            ProductCard(p, onOpenProduct)
                        }
                    }
                }
                else -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Surface2Color)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = TextMuted2, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Type to get semantic suggestions", color = TextMuted2, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ────────────────────────────────────────────────────────────

@Composable
private fun FilterField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboard: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onChange,
        modifier        = modifier.height(50.dp),
        label           = { Text(label, fontSize = 11.sp) },
        singleLine      = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape           = RoundedCornerShape(12.dp),
        textStyle       = LocalTextStyle.current.copy(fontSize = 13.sp),
        colors          = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = SurfaceColor,
            focusedContainerColor   = SurfaceColor,
            unfocusedBorderColor    = BorderColor,
            focusedBorderColor      = AccentColor,
        )
    )
}

@Composable
private fun ProductCard(product: Product, onOpenProduct: (Int) -> Unit) {
    Card(
        modifier  = Modifier.clickable { onOpenProduct(product.id) },
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardSoftColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor)
    ) {
        Column {
            SubcomposeAsyncImage(
                model = RetrofitClient.resolveUrl(product.image_urls?.firstOrNull())
                    ?: "https://picsum.photos/seed/${product.id}s/400/260",
                contentDescription  = product.name,
                contentScale        = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                loading = { SearchImageFallback(product.category) },
                error = { SearchImageFallback(product.category) },
            )
            Column(
                Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Surface(color = AccentBgColor, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        product.category,
                        fontSize = 10.sp,
                        color    = AccentColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
                Text("${"%.2f".format(product.price)} €", color = AccentColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SearchImageFallback(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(AccentBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = AccentColor, modifier = Modifier.size(22.dp))
            Text(label, color = MutedTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
