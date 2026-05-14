package com.example.smartshop.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.smartshop.network.AdminOrder
import com.example.smartshop.network.Product
import com.example.smartshop.network.RetrofitClient
import com.example.smartshop.network.UserProfile
import com.example.smartshop.ui.components.*
import com.example.smartshop.viewmodel.AdminDashboardViewModel
import com.example.smartshop.viewmodel.AppContainer
import kotlinx.coroutines.launch

// ── Tab definitions ───────────────────────────────────────────────────────────
private enum class AdminTab { ADD_PRODUCT, PRODUCTS, ORDERS, USERS }

@Composable
fun AdminDashboardScreen(
    vm: AdminDashboardViewModel,
    onNavigateToUserDetail: (UserProfile) -> Unit = {},
    onNavigateToOrderDetail: (AdminOrder) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(AdminTab.ADD_PRODUCT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
    ) {
        // ── Page header ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor)
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text(text = "Admin", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Manage your store",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
            )
        }
        Divider(color = BorderColor, thickness = 0.5.dp)

        // ── Tab row ───────────────────────────────────────────────────────
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            tonalElevation = 0.dp,
            containerColor = SurfaceColor,
        ) {
            NavigationBarItem(
                selected = selectedTab == AdminTab.ADD_PRODUCT,
                onClick  = { selectedTab = AdminTab.ADD_PRODUCT },
                icon     = { Icon(Icons.Outlined.Add, contentDescription = "Add product") },
                label    = { Text("Add") },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentColor,
                    unselectedIconColor = TextMuted2,
                    selectedTextColor = AccentColor,
                    unselectedTextColor = TextMuted2,
                    indicatorColor = AccentBgColor,
                ),
            )
            NavigationBarItem(
                selected = selectedTab == AdminTab.PRODUCTS,
                onClick  = { selectedTab = AdminTab.PRODUCTS },
                icon     = { Icon(Icons.Outlined.ShoppingCart, contentDescription = "Products") },
                label    = { Text("Products") },
            )
            NavigationBarItem(
                selected = selectedTab == AdminTab.USERS,
                onClick  = { selectedTab = AdminTab.USERS },
                icon     = { Icon(Icons.Outlined.Person, contentDescription = "Users") },
                label    = { Text("Users") },
            )
            NavigationBarItem(
                selected = selectedTab == AdminTab.ORDERS,
                onClick  = { selectedTab = AdminTab.ORDERS },
                icon     = { Icon(Icons.AutoMirrored.Outlined.ReceiptLong, contentDescription = "Orders") },
                label    = { Text("Orders") },
            )
        }

        Divider(color = BorderColor, thickness = 0.5.dp)

        // ── Tab content ───────────────────────────────────────────────────
        LaunchedEffect(Unit) { vm.loadData() }

        when (selectedTab) {
            AdminTab.ADD_PRODUCT -> AddProductTab(vm)
            AdminTab.PRODUCTS    -> ProductsTab(vm)
            AdminTab.ORDERS      -> OrdersTab(vm, onNavigateToOrderDetail)
            AdminTab.USERS       -> UsersTab(vm, onNavigateToUserDetail)
        }
    }
}

// ── ADD PRODUCT TAB ───────────────────────────────────────────────────────────
@Composable
private fun AddProductTab(vm: AdminDashboardViewModel) {
    var name         by remember { mutableStateOf("") }
    var category     by remember { mutableStateOf("Smartphones") }
    var price        by remember { mutableStateOf("299.99") }
    var addImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadLoading by remember { mutableStateOf(false) }
    var uploadError   by remember { mutableStateOf<String?>(null) }
    var addSelectedUri by remember { mutableStateOf<Uri?>(null) }

    val context      = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val addImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { addSelectedUri = it } }

    fun resolveUrl(rawUrl: String) =
        if (rawUrl.startsWith("http")) rawUrl else "${RetrofitClient.BASE_URL}$rawUrl"

    LaunchedEffect(addSelectedUri) {
        addSelectedUri?.let { uri ->
            uploadLoading = true
            uploadError   = null
            try {
                val url      = AppContainer.repo.uploadProductImage(context, uri)
                addImageUrls = addImageUrls + url
            } catch (e: Exception) {
                uploadError = e.message ?: "Unable to upload image"
            }
            uploadLoading  = false
            addSelectedUri = null
        }
    }

    // ── AI insight card ───────────────────────────────────────────────────
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = AccentBgColor),
                border    = androidx.compose.foundation.BorderStroke(0.5.dp, HeroBorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(13.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Lock,
                            contentDescription = null,
                            tint     = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                    Column {
                        Text(
                            text       = "AI insight",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = HeroTextDark,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text       = vm.insight.ifBlank {
                                "Laptops trending +18% this week. Consider restocking before the weekend."
                            },
                            style      = MaterialTheme.typography.bodySmall,
                            color      = HeroTextMid,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }
        }

        // ── Stats row ─────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatCard(
                    label      = "Orders",
                    value      = "${vm.orders.size}",
                    valueColor = AccentColor,
                    modifier   = Modifier.weight(1f),
                )
                StatCard(
                    label      = "Revenue",
                    value      = "€${"%,.0f".format(vm.products.sumOf { it.price * 3 })}",
                    valueColor = GreenColor,
                    modifier   = Modifier.weight(1f),
                )
            }
        }

        // ── Add product form ──────────────────────────────────────────────
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
                border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(text = "Add product", style = MaterialTheme.typography.titleMedium)

                    FieldInput(
                        label = "Name", value = name, onValueChange = { name = it },
                    )
                    FieldInput(
                        label = "Category", value = category, onValueChange = { category = it },
                    )
                    FieldInput(
                        label           = "Price (EUR)",
                        value           = price,
                        onValueChange   = { price = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(onClick = { addImageLauncher.launch("image/*") }) {
                            Text("Upload image")
                        }
                        if (uploadLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }

                    addImageUrls.takeIf { it.isNotEmpty() }?.let { uploaded ->
                        Text(
                            text     = "Uploaded images",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = TextMuted,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uploaded) { url ->
                                SubcomposeAsyncImage(
                                    model              = resolveUrl(url),
                                    contentDescription = "Preview image",
                                    modifier           = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface2Color),
                                    contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                                    loading = { AdminPreviewImageFallback() },
                                    error = { AdminPreviewImageFallback() },
                                )
                            }
                        }
                    }

                    uploadError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(Modifier.height(2.dp))

                    PrimaryButton(
                        text    = "Add product",
                        onClick = {
                            vm.addProduct(name, category, price.toDoubleOrNull() ?: 0.0, addImageUrls)
                            name         = ""
                            addImageUrls = emptyList()
                        },
                    )
                }
            }
        }
    }
}

// ── PRODUCTS TAB ──────────────────────────────────────────────────────────────
@Composable
private fun ProductsTab(vm: AdminDashboardViewModel) {
    var editingProduct    by remember { mutableStateOf<Product?>(null) }
    var editName          by remember { mutableStateOf("") }
    var editCategory      by remember { mutableStateOf("") }
    var editPrice         by remember { mutableStateOf("") }
    var editStock         by remember { mutableStateOf("") }
    var editImageUrlsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var uploadLoading     by remember { mutableStateOf(false) }
    var uploadError       by remember { mutableStateOf<String?>(null) }
    var editSelectedUri   by remember { mutableStateOf<Uri?>(null) }

    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val editImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { editSelectedUri = it } }

    fun resolveUrl(rawUrl: String) =
        if (rawUrl.startsWith("http")) rawUrl else "${RetrofitClient.BASE_URL}$rawUrl"

    fun openEditor(product: Product) {
        editingProduct    = product
        editName          = product.name
        editCategory      = product.category
        editPrice         = product.price.toString()
        editStock         = product.stock.toString()
        editImageUrlsList = product.image_urls ?: emptyList()
    }

    LaunchedEffect(editSelectedUri) {
        editSelectedUri?.let { uri ->
            uploadLoading = true
            uploadError   = null
            try {
                val url           = AppContainer.repo.uploadProductImage(context, uri)
                editImageUrlsList = editImageUrlsList + url
            } catch (e: Exception) {
                uploadError = e.message ?: "Unable to upload image"
            }
            uploadLoading   = false
            editSelectedUri = null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
                border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Text(
                        text     = "Products",
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    vm.products.forEachIndexed { index, product ->
                        if (index > 0) {
                            Divider(
                                color     = BorderColor,
                                thickness = 0.5.dp,
                                modifier  = Modifier.padding(vertical = 1.dp),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text       = product.name,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    maxLines   = 1,
                                    overflow   = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text  = product.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                )
                            }
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text       = "€${"%.2f".format(product.price)}",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    color      = AccentColor,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                IconButton(
                                    onClick  = { openEditor(product) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentBgColor),
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = "Edit ${product.name}",
                                        tint     = AccentColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                                IconButton(
                                    onClick  = { vm.deleteProduct(product.id) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RedBgColor),
                                ) {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = "Delete ${product.name}",
                                        tint     = RedColor,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Edit dialog ───────────────────────────────────────────────────────
    editingProduct?.let { product ->
        AlertDialog(
            onDismissRequest = { editingProduct = null },
            title = { Text("Edit product") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FieldInput(label = "Name",     value = editName,     onValueChange = { editName = it })
                    FieldInput(label = "Category", value = editCategory, onValueChange = { editCategory = it })
                    FieldInput(
                        label           = "Price",
                        value           = editPrice,
                        onValueChange   = { editPrice = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    FieldInput(
                        label           = "Stock",
                        value           = editStock,
                        onValueChange   = { editStock = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Button(onClick = { editImageLauncher.launch("image/*") }) {
                            Text("Upload image")
                        }
                        if (uploadLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                    editImageUrlsList.takeIf { it.isNotEmpty() }?.let { uploaded ->
                        Text(
                            text     = "Uploaded images",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = TextMuted,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uploaded) { url ->
                                SubcomposeAsyncImage(
                                    model              = resolveUrl(url),
                                    contentDescription = "Preview image",
                                    modifier           = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface2Color),
                                    contentScale       = androidx.compose.ui.layout.ContentScale.Crop,
                                    loading = { AdminPreviewImageFallback() },
                                    error = { AdminPreviewImageFallback() },
                                )
                            }
                        }
                    }
                    uploadError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateProduct(
                        product.copy(
                            name       = editName,
                            category   = editCategory,
                            price      = editPrice.toDoubleOrNull() ?: product.price,
                            stock      = editStock.toIntOrNull() ?: product.stock,
                            image_urls = editImageUrlsList,
                        )
                    )
                    editingProduct = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingProduct = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AdminPreviewImageFallback() {
    Box(
        modifier = Modifier
            .size(64.dp)
            .background(AccentBgColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Outlined.Person,
            contentDescription = null,
            tint = AccentColor,
            modifier = Modifier.size(22.dp),
        )
    }
}

// ── USERS TAB ─────────────────────────────────────────────────────────────────
@Composable
private fun OrdersTab(
    vm: AdminDashboardViewModel,
    onNavigateToOrderDetail: (AdminOrder) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "Orders",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
            )
        }

        if (vm.orders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Text(
                        text = "No orders yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(15.dp),
                    )
                }
            }
        } else {
            items(vm.orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                    elevation = CardDefaults.cardElevation(0.dp),
                    onClick = { onNavigateToOrderDetail(order) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentBgColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier.size(19.dp),
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Order #${order.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            )
                            Text(
                                text = order.user.full_name,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = formatOrderDate(order.created_at),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted2,
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "€${"%.2f".format(order.total_amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AccentColor,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${order.items.sumOf { it.quantity }} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UsersTab(
    vm: AdminDashboardViewModel,
    onNavigateToUserDetail: (UserProfile) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        item {
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(16.dp),
                colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
                border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
                elevation = CardDefaults.cardElevation(0.dp),
            ) {
                Column(modifier = Modifier.padding(15.dp)) {
                    Text(
                        text     = "Users",
                        style    = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    vm.users.forEachIndexed { index, user ->
                        if (index > 0) {
                            HorizontalDivider(
                                color     = BorderColor,
                                thickness = 0.5.dp,
                                modifier  = Modifier.padding(vertical = 8.dp),
                            )
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape   = RoundedCornerShape(12.dp),
                            colors  = CardDefaults.cardColors(containerColor = AccentBgColor),
                            onClick = { onNavigateToUserDetail(user) },
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text       = user.full_name,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                                Text(
                                    text  = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    if (user.is_admin) {
                                        Card(
                                            shape  = RoundedCornerShape(4.dp),
                                            colors = CardDefaults.cardColors(containerColor = AccentColor),
                                            modifier = Modifier.padding(vertical = 2.dp),
                                        ) {
                                            Text(
                                                "Admin",
                                                style    = MaterialTheme.typography.labelSmall,
                                                color    = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.padding(4.dp, 2.dp),
                                            )
                                        }
                                    }
                                    if (user.latitude != null && user.longitude != null) {
                                        Card(
                                            shape    = RoundedCornerShape(4.dp),
                                            colors   = CardDefaults.cardColors(containerColor = GreenColor),
                                            modifier = Modifier.padding(vertical = 2.dp),
                                        ) {
                                            Text(
                                                "📍 Location",
                                                style    = MaterialTheme.typography.labelSmall,
                                                color    = androidx.compose.ui.graphics.Color.White,
                                                modifier = Modifier.padding(4.dp, 2.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── StatCard ──────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SurfaceColor),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, BorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Text(
                text          = label.uppercase(),
                style         = MaterialTheme.typography.labelSmall,
                color         = TextMuted,
                letterSpacing = 0.4.sp,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp),
                color      = valueColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

internal fun formatOrderDate(value: String): String {
    return value
        .replace("T", " ")
        .replace("Z", "")
        .substringBefore(".")
}
