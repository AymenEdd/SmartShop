package com.example.smartshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.smartshop.ui.components.ProductCard
import com.example.smartshop.ui.components.CategoryChip
import com.example.smartshop.network.Product
import com.example.smartshop.viewmodel.ProductsViewModel
import com.example.smartshop.viewmodel.UiState

@Composable
fun ProductsScreen(
    vm: ProductsViewModel,
    onOpenProduct: (Int) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        vm.loadProducts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBgColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "All Products",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Browse the full catalog",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted2
        )
        Spacer(Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(vm.categories) { category ->
                CategoryChip(
                    label = category,
                    selected = vm.selectedCategory == category,
                    onClick = { vm.selectedCategory = category },
                )
            }
        }

        when (val state = vm.state) {
            UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentColor)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = RedColor,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is UiState.Success -> {
                val products = vm.visibleProducts
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product: Product ->
                        ProductCard(
                            product = product,
                            onClick = { onOpenProduct(product.id) }
                        )
                    }
                }
            }
            else -> {}
        }
    }
}
