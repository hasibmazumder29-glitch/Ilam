package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.ProductImagePlaceholder
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.IlamViewModel

// ==========================================
// 1. DASHBOARD VIEW
// ==========================================
@Composable
fun AdminDashboardView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.dashboardMetrics.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val orders by viewModel.allOrders.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Strings.ownerDashboard(language),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = IlamBlack,
            fontFamily = FontFamily.Serif
        )

        // KPI Highlights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = Strings.totalSales(language),
                value = formatBdt(metrics.totalSales),
                subtext = "All Time Revenue",
                accentColor = IlamGoldDark,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = Strings.todaySales(language),
                value = formatBdt(metrics.todaySales),
                subtext = "Today's Orders",
                accentColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiCard(
                title = Strings.totalOrders(language),
                value = metrics.totalOrders.toString(),
                subtext = "Lifetime Orders",
                accentColor = IlamBlack,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = Strings.totalCustomers(language),
                value = metrics.totalCustomers.toString(),
                subtext = "Registered VIPs",
                accentColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
        }

        // Order Pipeline Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IlamWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (language == AppLanguage.BN) "অর্ডার পাইপলাইন স্থিতি" else "Order Pipeline Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = IlamBlack
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusCounter(title = Strings.pendingOrders(language), count = metrics.pendingOrders, color = Color(0xFFE65100))
                    StatusCounter(title = Strings.processingOrders(language), count = metrics.processingOrders, color = Color(0xFF1565C0))
                    StatusCounter(title = Strings.deliveredOrders(language), count = metrics.deliveredOrders, color = Color(0xFF2E7D32))
                    StatusCounter(title = Strings.cancelledOrders(language), count = metrics.cancelledOrders, color = Color(0xFFC62828))
                }
            }
        }

        // Low Stock Alert
        if (lowStockProducts.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF57F17))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${Strings.lowStockAlert(language)} (${lowStockProducts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFF57F17)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    lowStockProducts.forEach { prod ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = prod.nameEn, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text(text = "SKU: ${prod.sku} • Stock: ${prod.stock} left", fontSize = 11.sp, color = Color(0xFFC62828))
                            }
                            Button(
                                onClick = {
                                    viewModel.adminSaveProduct(prod.copy(stock = prod.stock + 10))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(text = "+10 Restock", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Recent Orders Snapshot
        Text(
            text = if (language == AppLanguage.BN) "সাম্প্রতিক অর্ডারসমূহ" else "Recent Customer Orders",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = IlamBlack,
            fontFamily = FontFamily.Serif
        )

        orders.take(5).forEach { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "${order.customerName} • ${order.customerPhone}", fontSize = 11.sp, color = IlamTextSecondary)
                        Text(text = "Total: ${formatBdt(order.totalAmount)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OrderStatusBadge(status = order.orderStatus)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = IlamWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = IlamTextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, color = IlamTextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
fun StatusCounter(
    title: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        Text(text = title, fontSize = 10.sp, color = IlamTextSecondary)
    }
}

// ==========================================
// 2. PRODUCT MANAGEMENT (CRUD)
// ==========================================
@Composable
fun AdminProductsView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_products_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.manageProducts(language)} (${products.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
            Button(
                onClick = {
                    editingProduct = null
                    showProductDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("admin_add_product_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = Strings.addProduct(language), fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(products, key = { it.id }) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(IlamSurfaceLight)
                        ) {
                            ProductImagePlaceholder(categorySlug = product.primaryImageUrl, modifier = Modifier.fillMaxSize())
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = product.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                            Text(text = "BN: ${product.nameBn}", fontSize = 11.sp, color = IlamTextSecondary, maxLines = 1)
                            Text(text = "SKU: ${product.sku} • Stock: ${product.stock} • ${formatBdt(product.price)}", fontSize = 11.sp, color = IlamGoldDark, fontWeight = FontWeight.SemiBold)
                        }

                        // Published toggle switch
                        Switch(
                            checked = product.isPublished,
                            onCheckedChange = { viewModel.adminToggleProductPublish(product) },
                            colors = SwitchDefaults.colors(checkedThumbColor = IlamWhite, checkedTrackColor = IlamBlack)
                        )

                        // Edit Button
                        IconButton(onClick = {
                            editingProduct = product
                            showProductDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = IlamBlack, modifier = Modifier.size(18.dp))
                        }

                        // Delete Button
                        IconButton(onClick = { viewModel.adminDeleteProduct(product.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = IlamCrimson, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Product Add/Edit Dialog
    if (showProductDialog) {
        ProductEditDialog(
            initialProduct = editingProduct,
            categories = categories,
            language = language,
            onDismiss = { showProductDialog = false },
            onSave = { savedProduct ->
                viewModel.adminSaveProduct(savedProduct)
                showProductDialog = false
            }
        )
    }
}

@Composable
fun ProductEditDialog(
    initialProduct: ProductEntity?,
    categories: List<CategoryEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var nameEn by remember { mutableStateOf(initialProduct?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(initialProduct?.nameBn ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "ILAM-SKU-${System.currentTimeMillis() % 10000}") }
    var categoryId by remember { mutableStateOf(initialProduct?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)) }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "3500") }
    var discountPriceStr by remember { mutableStateOf(initialProduct?.discountPrice?.toInt()?.toString() ?: "0") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "15") }
    var sizes by remember { mutableStateOf(initialProduct?.sizes ?: "M, L, XL") }
    var colors by remember { mutableStateOf(initialProduct?.colors ?: "Noir Black, Slate Gray") }
    var descEn by remember { mutableStateOf(initialProduct?.descEn ?: "") }
    var descBn by remember { mutableStateOf(initialProduct?.descBn ?: "") }
    var isNewArrival by remember { mutableStateOf(initialProduct?.isNewArrival ?: true) }
    var isFeatured by remember { mutableStateOf(initialProduct?.isFeatured ?: true) }
    var isBestSeller by remember { mutableStateOf(initialProduct?.isBestSeller ?: false) }
    var isPublished by remember { mutableStateOf(initialProduct?.isPublished ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) Strings.addProduct(language) else Strings.editProduct(language),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Product Name (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Regular Price (৳)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountPriceStr,
                        onValueChange = { discountPriceStr = it },
                        label = { Text("Discount Price (৳)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Quantity") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = sizes,
                        onValueChange = { sizes = it },
                        label = { Text("Sizes (M, L, XL)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = colors,
                    onValueChange = { colors = it },
                    label = { Text("Colors (Black, Gray)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descEn,
                    onValueChange = { descEn = it },
                    label = { Text("Description (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                OutlinedTextField(
                    value = descBn,
                    onValueChange = { descBn = it },
                    label = { Text("Description (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Feature toggles
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isNewArrival, onCheckedChange = { isNewArrival = it })
                    Text("New Arrival", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Featured", fontSize = 12.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isBestSeller, onCheckedChange = { isBestSeller = it })
                    Text("Best Seller", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Publish to App", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = (initialProduct ?: ProductEntity()).copy(
                        nameEn = nameEn.ifBlank { "ILAM Luxury Garment" },
                        nameBn = nameBn.ifBlank { nameEn },
                        sku = sku,
                        categoryId = categoryId,
                        price = priceStr.toDoubleOrNull() ?: 3500.0,
                        discountPrice = discountPriceStr.toDoubleOrNull() ?: 0.0,
                        stock = stockStr.toIntOrNull() ?: 10,
                        sizes = sizes,
                        colors = colors,
                        descEn = descEn,
                        descBn = descBn,
                        isNewArrival = isNewArrival,
                        isFeatured = isFeatured,
                        isBestSeller = isBestSeller,
                        isPublished = isPublished
                    )
                    onSave(p)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
            ) {
                Text(Strings.saveChanges(language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.cancel(language), color = IlamTextSecondary)
            }
        },
        containerColor = IlamWhite
    )
}

// ==========================================
// 3. CATEGORY MANAGEMENT
// ==========================================
@Composable
fun AdminCategoriesView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.allCategories.collectAsState()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_categories_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.manageCategories(language)} (${categories.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
            Button(
                onClick = {
                    editingCategory = null
                    showCategoryDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Category", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = cat.nameEn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "বাংলা: ${cat.nameBn} • Slug: ${cat.slug}", fontSize = 11.sp, color = IlamTextSecondary)
                        }

                        IconButton(onClick = {
                            editingCategory = cat
                            showCategoryDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = IlamBlack, modifier = Modifier.size(18.dp))
                        }

                        IconButton(onClick = { viewModel.adminDeleteCategory(cat.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = IlamCrimson, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        var nameEn by remember { mutableStateOf(editingCategory?.nameEn ?: "") }
        var nameBn by remember { mutableStateOf(editingCategory?.nameBn ?: "") }
        var slug by remember { mutableStateOf(editingCategory?.slug ?: "") }

        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text(if (editingCategory == null) "Add Category" else "Edit Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("Category Name (English)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nameBn,
                        onValueChange = { nameBn = it },
                        label = { Text("Category Name (বাংলা)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = slug,
                        onValueChange = { slug = it },
                        label = { Text("Category Slug (e.g. panjabi)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = (editingCategory ?: CategoryEntity()).copy(
                            nameEn = nameEn,
                            nameBn = nameBn,
                            slug = slug.ifBlank { nameEn.lowercase().replace(" ", "-") }
                        )
                        viewModel.adminSaveCategory(c)
                        showCategoryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text(Strings.saveChanges(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// 4. ORDER MANAGEMENT
// ==========================================
@Composable
fun AdminOrdersView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.allOrders.collectAsState()
    val couriers by viewModel.allCouriers.collectAsState()
    var filterStatus by remember { mutableStateOf("ALL") }

    val filteredOrders = remember(orders, filterStatus) {
        if (filterStatus == "ALL") orders else orders.filter { it.orderStatus == filterStatus }
    }

    var selectedOrderForUpdate by remember { mutableStateOf<OrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_orders_view")
    ) {
        Text(
            text = "${Strings.manageOrders(language)} (${orders.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("ALL", "Pending", "Processing", "Delivered", "Cancelled").forEach { status ->
                val isSelected = filterStatus == status
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) IlamBlack else IlamWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight),
                    modifier = Modifier.clickable { filterStatus = status }
                ) {
                    Text(
                        text = status,
                        color = if (isSelected) IlamWhite else IlamTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredOrders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            OrderStatusBadge(status = order.orderStatus)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp)
                        Text(text = "Address: ${order.customerAddress}, ${order.customerDistrict}", fontSize = 11.sp, color = IlamTextSecondary)
                        Text(text = "Courier: ${order.courierName} • Tracking: ${order.trackingNumber.ifBlank { "None" }}", fontSize = 11.sp, color = IlamGoldDark)
                        Text(text = "Payment: ${order.paymentMethod} (${order.paymentStatus}) • Total: ${formatBdt(order.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { selectedOrderForUpdate = order },
                            colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Status & Courier Tracking", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Order Update Dialog
    selectedOrderForUpdate?.let { order ->
        var newStatus by remember { mutableStateOf(order.orderStatus) }
        var trackingInput by remember { mutableStateOf(order.trackingNumber) }
        var selectedCourierId by remember { mutableStateOf(order.courierId) }

        AlertDialog(
            onDismissRequest = { selectedOrderForUpdate = null },
            title = { Text("Update Order #${order.orderNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Order Status:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Pending", "Processing", "Delivered", "Cancelled").forEach { st ->
                            val isSel = newStatus == st
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSel) IlamBlack else IlamSurfaceLight,
                                modifier = Modifier.clickable { newStatus = st }
                            ) {
                                Text(
                                    text = st,
                                    color = if (isSel) IlamWhite else IlamBlack,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Assign Courier Tracking:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = trackingInput,
                        onValueChange = { trackingInput = it },
                        label = { Text("Courier Tracking ID (e.g. STD-88492)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminUpdateOrderStatus(order.id, newStatus)
                        val courierObj = couriers.find { it.id == selectedCourierId }
                        viewModel.adminAssignCourier(
                            order.id,
                            selectedCourierId,
                            courierObj?.name ?: order.courierName,
                            trackingInput
                        )
                        selectedOrderForUpdate = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text("Save Updates")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForUpdate = null }) {
                    Text("Cancel")
                }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// 5. COURIER MANAGEMENT (UNLIMITED BANGLADESH COURIERS)
// ==========================================
@Composable
fun AdminCouriersView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val couriers by viewModel.allCouriers.collectAsState()
    var showCourierDialog by remember { mutableStateOf(false) }
    var editingCourier by remember { mutableStateOf<CourierEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_couriers_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.manageCouriers(language)} (${couriers.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
            Button(
                onClick = {
                    editingCourier = null
                    showCourierDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Courier", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(couriers, key = { it.id }) { courier ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = courier.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (courier.isDefault) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = IlamGoldDark
                                    ) {
                                        Text(text = "DEFAULT", fontSize = 9.sp, color = IlamWhite, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(text = formatBdt(courier.deliveryCharge), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Text(text = "Tracking URL: ${courier.trackingUrlPattern}", fontSize = 11.sp, color = IlamTextSecondary)
                        if (courier.apiUrl.isNotBlank()) {
                            Text(text = "API: ${courier.apiUrl}", fontSize = 10.sp, color = IlamTextMuted)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!courier.isDefault) {
                                TextButton(onClick = { viewModel.adminSetDefaultCourier(courier.id) }) {
                                    Text("Make Default", fontSize = 11.sp, color = IlamGoldDark)
                                }
                            }
                            IconButton(onClick = {
                                editingCourier = courier
                                showCourierDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.adminDeleteCourier(courier.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IlamCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCourierDialog) {
        var name by remember { mutableStateOf(editingCourier?.name ?: "") }
        var chargeStr by remember { mutableStateOf(editingCourier?.deliveryCharge?.toInt()?.toString() ?: "60") }
        var trackingUrl by remember { mutableStateOf(editingCourier?.trackingUrlPattern ?: "https://steadfast.com.bd/t/{tracking_id}") }
        var isDefault by remember { mutableStateOf(editingCourier?.isDefault ?: false) }

        AlertDialog(
            onDismissRequest = { showCourierDialog = false },
            title = { Text(if (editingCourier == null) "Add Courier Service" else "Edit Courier Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Courier Name (e.g. Steadfast, Pathao)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = chargeStr,
                        onValueChange = { chargeStr = it },
                        label = { Text("Standard Delivery Charge (৳)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = trackingUrl,
                        onValueChange = { trackingUrl = it },
                        label = { Text("Tracking URL Pattern ({tracking_id})") },
                        singleLine = true
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                        Text("Set as Default Courier for Store", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = (editingCourier ?: CourierEntity()).copy(
                            name = name,
                            deliveryCharge = chargeStr.toDoubleOrNull() ?: 60.0,
                            trackingUrlPattern = trackingUrl,
                            isDefault = isDefault,
                            isEnabled = true
                        )
                        viewModel.adminSaveCourier(c)
                        showCourierDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text(Strings.saveChanges(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCourierDialog = false }) {
                    Text(Strings.cancel(language))
                }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// 6. COUPON & PROMOTION MANAGEMENT
// ==========================================
@Composable
fun AdminCouponsView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val coupons by viewModel.allCoupons.collectAsState()
    var showCouponDialog by remember { mutableStateOf(false) }
    var editingCoupon by remember { mutableStateOf<CouponEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_coupons_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.manageCoupons(language)} (${coupons.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
            Button(
                onClick = {
                    editingCoupon = null
                    showCouponDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Create Coupon", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(coupons, key = { it.id }) { coupon ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = coupon.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IlamGoldDark)
                            Text(
                                text = if (coupon.discountPercent > 0) "${coupon.discountPercent.toInt()}% Off" else "৳${coupon.fixedDiscount.toInt()} Flat Off",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Min Order: ${formatBdt(coupon.minOrder)} • Uses: ${coupon.usedCount}/${coupon.maxUsage}", fontSize = 11.sp, color = IlamTextSecondary)
                        }

                        Switch(
                            checked = coupon.isActive,
                            onCheckedChange = { viewModel.adminSaveCoupon(coupon.copy(isActive = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = IlamWhite, checkedTrackColor = IlamBlack)
                        )

                        IconButton(onClick = { viewModel.adminDeleteCoupon(coupon.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IlamCrimson, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showCouponDialog) {
        var code by remember { mutableStateOf(editingCoupon?.code ?: "") }
        var discountPctStr by remember { mutableStateOf(editingCoupon?.discountPercent?.toInt()?.toString() ?: "10") }
        var minOrderStr by remember { mutableStateOf(editingCoupon?.minOrder?.toInt()?.toString() ?: "1500") }
        var maxUsageStr by remember { mutableStateOf(editingCoupon?.maxUsage?.toString() ?: "500") }

        AlertDialog(
            onDismissRequest = { showCouponDialog = false },
            title = { Text("Create Discount Coupon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
                        label = { Text("Coupon Code (e.g. EID15)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountPctStr,
                        onValueChange = { discountPctStr = it },
                        label = { Text("Discount Percentage (%)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minOrderStr,
                        onValueChange = { minOrderStr = it },
                        label = { Text("Minimum Order Amount (৳)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxUsageStr,
                        onValueChange = { maxUsageStr = it },
                        label = { Text("Maximum Total Usages") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val c = (editingCoupon ?: CouponEntity()).copy(
                            code = code.trim().uppercase(),
                            discountPercent = discountPctStr.toDoubleOrNull() ?: 10.0,
                            minOrder = minOrderStr.toDoubleOrNull() ?: 1000.0,
                            maxUsage = maxUsageStr.toIntOrNull() ?: 500,
                            isActive = true
                        )
                        viewModel.adminSaveCoupon(c)
                        showCouponDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text("Create Coupon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCouponDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// 7. BANNERS MANAGEMENT
// ==========================================
@Composable
fun AdminBannersView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val banners by viewModel.allBanners.collectAsState()
    var showBannerDialog by remember { mutableStateOf(false) }
    var editingBanner by remember { mutableStateOf<BannerEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_banners_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${Strings.manageBanners(language)} (${banners.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif
            )
            Button(
                onClick = {
                    editingBanner = null
                    showBannerDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Add Banner", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(banners, key = { it.id }) { banner ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = banner.titleEn, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "বাংলা: ${banner.titleBn}", fontSize = 11.sp, color = IlamTextSecondary)
                            Text(text = "Button: ${banner.buttonTextEn} • Target: ${banner.targetDestination}", fontSize = 11.sp, color = IlamGoldDark)
                        }

                        Switch(
                            checked = banner.isEnabled,
                            onCheckedChange = { viewModel.adminSaveBanner(banner.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = IlamWhite, checkedTrackColor = IlamBlack)
                        )

                        IconButton(onClick = { viewModel.adminDeleteBanner(banner.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = IlamCrimson, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showBannerDialog) {
        var titleEn by remember { mutableStateOf(editingBanner?.titleEn ?: "") }
        var titleBn by remember { mutableStateOf(editingBanner?.titleBn ?: "") }
        var subtitleEn by remember { mutableStateOf(editingBanner?.subtitleEn ?: "") }
        var subtitleBn by remember { mutableStateOf(editingBanner?.subtitleBn ?: "") }
        var buttonTextEn by remember { mutableStateOf(editingBanner?.buttonTextEn ?: "Shop Now") }

        AlertDialog(
            onDismissRequest = { showBannerDialog = false },
            title = { Text("Add / Edit Home Banner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Banner Heading (EN)") }, singleLine = true)
                    OutlinedTextField(value = titleBn, onValueChange = { titleBn = it }, label = { Text("Banner Heading (বাংলা)") }, singleLine = true)
                    OutlinedTextField(value = subtitleEn, onValueChange = { subtitleEn = it }, label = { Text("Subtitle (EN)") }, singleLine = true)
                    OutlinedTextField(value = subtitleBn, onValueChange = { subtitleBn = it }, label = { Text("Subtitle (বাংলা)") }, singleLine = true)
                    OutlinedTextField(value = buttonTextEn, onValueChange = { buttonTextEn = it }, label = { Text("Button CTA Text") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val b = (editingBanner ?: BannerEntity()).copy(
                            titleEn = titleEn,
                            titleBn = titleBn,
                            subtitleEn = subtitleEn,
                            subtitleBn = subtitleBn,
                            buttonTextEn = buttonTextEn,
                            isEnabled = true
                        )
                        viewModel.adminSaveBanner(b)
                        showBannerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text(Strings.saveChanges(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBannerDialog = false }) { Text("Cancel") }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// 8. REVIEWS MANAGEMENT
// ==========================================
@Composable
fun AdminReviewsView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val reviews by viewModel.allReviews.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_reviews_view")
    ) {
        Text(
            text = "${Strings.manageReviews(language)} (${reviews.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(reviews, key = { it.id }) { review ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = review.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row {
                                repeat(review.rating) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = IlamGold, modifier = Modifier.size(13.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "\"${review.reviewText}\"", fontSize = 12.sp, color = IlamTextSecondary)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(
                                    checked = review.isApproved,
                                    onCheckedChange = { viewModel.adminToggleReviewApproval(review) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = IlamWhite, checkedTrackColor = IlamBlack)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (review.isApproved) "Published" else "Hidden",
                                    fontSize = 11.sp,
                                    color = if (review.isApproved) Color(0xFF2E7D32) else IlamTextMuted
                                )
                            }

                            TextButton(onClick = { viewModel.adminDeleteReview(review.id) }) {
                                Text("Delete", color = IlamCrimson, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 9. STORE SETTINGS
// ==========================================
@Composable
fun AdminSettingsView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val storeSettings by viewModel.storeSettings.collectAsState()

    var nameEn by remember(storeSettings) { mutableStateOf(storeSettings.storeNameEn) }
    var nameBn by remember(storeSettings) { mutableStateOf(storeSettings.storeNameBn) }
    var phone by remember(storeSettings) { mutableStateOf(storeSettings.phone) }
    var email by remember(storeSettings) { mutableStateOf(storeSettings.email) }
    var address by remember(storeSettings) { mutableStateOf(storeSettings.address) }
    var whatsapp by remember(storeSettings) { mutableStateOf(storeSettings.whatsappNumber) }
    var freeDeliveryThresholdStr by remember(storeSettings) { mutableStateOf(storeSettings.freeDeliveryThreshold.toInt().toString()) }
    var defaultChargeStr by remember(storeSettings) { mutableStateOf(storeSettings.defaultDeliveryCharge.toInt().toString()) }
    var bkashMerchantNumber by remember(storeSettings) { mutableStateOf(storeSettings.bkashMerchantNumber) }

    var savedFeedback by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_settings_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = Strings.storeSettings(language),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            fontFamily = FontFamily.Serif
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IlamWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Brand Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Brand Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Brand Name (বাংলা)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Store Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Contact Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Store Address in Bangladesh") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IlamWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Delivery & Payment Rules", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                OutlinedTextField(
                    value = freeDeliveryThresholdStr,
                    onValueChange = { freeDeliveryThresholdStr = it },
                    label = { Text("Free Delivery Threshold (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = defaultChargeStr,
                    onValueChange = { defaultChargeStr = it },
                    label = { Text("Default Delivery Charge (৳)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bkashMerchantNumber,
                    onValueChange = { bkashMerchantNumber = it },
                    label = { Text("bKash Merchant Account Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("WhatsApp Support Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Button(
            onClick = {
                val updated = storeSettings.copy(
                    storeNameEn = nameEn,
                    storeNameBn = nameBn,
                    phone = phone,
                    email = email,
                    address = address,
                    whatsappNumber = whatsapp,
                    freeDeliveryThreshold = freeDeliveryThresholdStr.toDoubleOrNull() ?: 3000.0,
                    defaultDeliveryCharge = defaultChargeStr.toDoubleOrNull() ?: 60.0,
                    bkashMerchantNumber = bkashMerchantNumber
                )
                viewModel.adminSaveStoreSettings(updated)
                savedFeedback = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = Strings.saveChanges(language), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        if (savedFeedback) {
            Text(
                text = "✓ Store settings updated successfully!",
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
