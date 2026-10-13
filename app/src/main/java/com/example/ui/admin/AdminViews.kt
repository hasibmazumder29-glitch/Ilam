package com.example.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.viewmodel.UserRole

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

        // 1. Revenue & Sales KPIs (Daily, Monthly, Lifetime)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = Strings.todaySales(language),
                value = formatBdt(metrics.todaySales),
                subtext = "Daily Sales",
                accentColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (language == AppLanguage.BN) "মাসিক বিক্রয়" else "Monthly Sales",
                value = formatBdt(metrics.monthlySales),
                subtext = "This Month",
                accentColor = Color(0xFF1565C0),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = Strings.totalSales(language),
                value = formatBdt(metrics.totalSales),
                subtext = "Lifetime Revenue",
                accentColor = IlamGoldDark,
                modifier = Modifier.weight(1f)
            )
        }

        // 2. Inventory Stock Health KPIs (Requirement 9: Total Products, Out of Stock, Low Stock, Total Stock Quantity)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = if (language == AppLanguage.BN) "মোট পণ্য" else "Total Products",
                value = metrics.totalProducts.toString(),
                subtext = "Active Items",
                accentColor = IlamBlack,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (language == AppLanguage.BN) "মোট স্টক" else "Total Stock Units",
                value = metrics.totalStockQuantity.toString(),
                subtext = "Pieces in Stock",
                accentColor = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (language == AppLanguage.BN) "কম স্টক" else "Low Stock",
                value = metrics.lowStockCount.toString(),
                subtext = "Stock <= Alert",
                accentColor = Color(0xFFF57F17),
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = if (language == AppLanguage.BN) "স্টক শেষ" else "Out of Stock",
                value = metrics.outOfStockCount.toString(),
                subtext = "0 Units Left",
                accentColor = Color(0xFFC62828),
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

        // Quick Products Overview Card with Quick Add & Manage Button
        val allProductsList by viewModel.allProducts.collectAsState()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IlamWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.BN) "পণ্য ডাটাবেজ স্থিতি" else "Room Products Catalog",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = IlamBlack
                        )
                        Text(
                            text = "${allProductsList.size} items stored in Room DB",
                            fontSize = 11.sp,
                            color = IlamTextSecondary
                        )
                    }

                    Button(
                        onClick = { viewModel.setAdminTab(com.example.ui.viewmodel.AdminTab.PRODUCTS) },
                        colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "পণ্য তালিকা দেখুন" else "Manage Catalog",
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Latest 3 products preview
                allProductsList.take(3).forEach { prod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = prod.nameEn, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "SKU: ${prod.sku} • ${formatBdt(prod.price)} • Stock: ${prod.stock}", fontSize = 10.sp, color = IlamTextSecondary)
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (prod.isPublished) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (prod.isPublished) "Published" else "Draft",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (prod.isPublished) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
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
// 2. PRODUCT MANAGEMENT (POWERFUL CRUD, SEARCH, FILTER, QR, SAFE DELETE & RBAC)
// ==========================================
@Composable
fun AdminProductsView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val products by viewModel.allProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }
    var qrProductToView by remember { mutableStateOf<ProductEntity?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, IN_STOCK, OUT_OF_STOCK, LOW_STOCK, HIDDEN, DELETED

    val filteredProducts = remember(products, categories, searchQuery, selectedCategoryId, statusFilter) {
        products.filter { p ->
            val catName = categories.find { it.id == p.categoryId }?.nameEn ?: ""
            val matchesSearch = searchQuery.isBlank() ||
                p.nameEn.contains(searchQuery, ignoreCase = true) ||
                p.nameBn.contains(searchQuery, ignoreCase = true) ||
                p.sku.contains(searchQuery, ignoreCase = true) ||
                p.barcode.contains(searchQuery, ignoreCase = true) ||
                catName.contains(searchQuery, ignoreCase = true) ||
                p.colors.contains(searchQuery, ignoreCase = true) ||
                p.sizes.contains(searchQuery, ignoreCase = true)

            val matchesCategory = selectedCategoryId == null || p.categoryId == selectedCategoryId

            val matchesStatus = when (statusFilter) {
                "IN_STOCK" -> p.stock > 0 && !p.isDeleted && p.isPublished
                "OUT_OF_STOCK" -> (p.stock <= 0 || p.stockStatus == "OUT_OF_STOCK") && !p.isDeleted
                "LOW_STOCK" -> p.stock > 0 && p.stock <= p.minStockAlert && !p.isDeleted
                "HIDDEN" -> !p.isPublished && !p.isDeleted
                "DELETED" -> p.isDeleted
                else -> !p.isDeleted // Default "ALL" displays active non-deleted items
            }
            matchesSearch && matchesCategory && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_products_view")
    ) {
        // Header with Role Indicator & Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${Strings.manageProducts(language)} (${filteredProducts.size}/${products.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (userRole) {
                            UserRole.OWNER -> IlamGoldDark
                            UserRole.MANAGER -> Color(0xFF1565C0)
                            UserRole.STAFF -> Color(0xFF2E7D32)
                        }
                    ) {
                        Text(
                            text = userRole.name,
                            color = IlamWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = if (language == AppLanguage.BN) "রুম ডাটাবেজে সংরক্ষিত পণ্যের পূর্ণ তালিকা ও নিয়ন্ত্রণ" else "Live Room DB product catalog • Synced across outlets & POS",
                    fontSize = 11.sp,
                    color = IlamTextSecondary
                )
            }

            if (userRole.canAddEditProduct) {
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
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar (Name, SKU, Barcode, Category, Color, Size)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (language == AppLanguage.BN) "নাম, SKU, বারকোড, সাইজ বা রঙ দিয়ে খুঁজুন..." else "Search by Name, SKU, Barcode, Color, or Size...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = IlamTextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = IlamTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips: In Stock, Out of Stock, Low Stock, Hidden, Deleted
        Text(
            text = "Status Filters:",
            fontSize = 10.sp,
            color = IlamTextMuted,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            val statusOptions = listOf(
                Pair("ALL", "All Active (${products.count { !it.isDeleted }})"),
                Pair("IN_STOCK", "In Stock (${products.count { it.stock > 0 && !it.isDeleted && it.isPublished }})"),
                Pair("OUT_OF_STOCK", "Out of Stock (${products.count { (it.stock <= 0 || it.stockStatus == "OUT_OF_STOCK") && !it.isDeleted }})"),
                Pair("LOW_STOCK", "Low Stock (${products.count { it.stock > 0 && it.stock <= it.minStockAlert && !it.isDeleted }})"),
                Pair("HIDDEN", "Hidden (${products.count { !it.isPublished && !it.isDeleted }})"),
                Pair("DELETED", "Deleted / Archived (${products.count { it.isDeleted }})")
            )

            items(statusOptions) { (key, label) ->
                FilterChip(
                    selected = statusFilter == key,
                    onClick = { statusFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (statusFilter == key) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("All Categories", fontSize = 11.sp) }
                )
            }
            items(categories, key = { it.id }) { cat ->
                val count = products.count { it.categoryId == cat.id && !it.isDeleted }
                FilterChip(
                    selected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                    label = { Text("${cat.nameEn} ($count)", fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = IlamTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "কোনো পণ্য পাওয়া যায়নি" else "No matching products found",
                        color = IlamTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val cat = categories.find { it.id == product.categoryId }
                    val isOut = product.stock <= 0 || product.stockStatus == "OUT_OF_STOCK"
                    val isLow = !isOut && product.stock <= product.minStockAlert

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_product_card_${product.id}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = IlamWhite),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (product.isDeleted) Color(0xFFEF9A9A) else if (isOut) Color(0xFFFFCDD2) else if (isLow) Color(0xFFFFE082) else IlamBorderLight
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Top Row: Image, Titles, Pricing, Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IlamSurfaceLight)
                                ) {
                                    ProductImagePlaceholder(categorySlug = product.primaryImageUrl, modifier = Modifier.fillMaxSize())
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = product.nameEn, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                                        if (cat != null) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = IlamSurfaceLight,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = cat.nameEn,
                                                    fontSize = 9.sp,
                                                    color = IlamTextSecondary,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(text = "BN: ${product.nameBn}", fontSize = 11.sp, color = IlamTextSecondary, maxLines = 1)

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "SKU: ${product.sku}", fontSize = 10.sp, color = IlamGoldDark, fontWeight = FontWeight.SemiBold)
                                        if (product.barcode.isNotBlank()) {
                                            Text(text = "Barcode: ${product.barcode}", fontSize = 9.sp, color = IlamTextMuted)
                                        }
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatBdt(product.effectiveSellingPrice),
                                            fontSize = 12.sp,
                                            color = IlamBlack,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (product.discountPrice > 0) {
                                            Text(
                                                text = "MRP: ${formatBdt(product.price)}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        if (userRole.canViewCostPrice && product.purchasePrice > 0.0) {
                                            Text(
                                                text = "Cost: ${formatBdt(product.purchasePrice)}",
                                                fontSize = 9.sp,
                                                color = Color(0xFF1565C0),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // Stock Status Badge
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when {
                                            product.isDeleted -> Color(0xFFFFEBEE)
                                            !product.isPublished -> Color(0xFFECEFF1)
                                            isOut -> Color(0xFFFFEBEE)
                                            isLow -> Color(0xFFFFF8E1)
                                            else -> Color(0xFFE8F5E9)
                                        }
                                    ) {
                                        Text(
                                            text = when {
                                                product.isDeleted -> "DELETED"
                                                !product.isPublished -> "HIDDEN"
                                                isOut -> "OUT OF STOCK"
                                                isLow -> "LOW STOCK"
                                                else -> "IN STOCK"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                product.isDeleted -> IlamCrimson
                                                !product.isPublished -> Color.Gray
                                                isOut -> Color(0xFFC62828)
                                                isLow -> Color(0xFFF57F17)
                                                else -> Color(0xFF2E7D32)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${product.stock} pcs",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOut) Color(0xFFC62828) else if (isLow) Color(0xFFF57F17) else IlamBlack
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Variants summary (Size & Color)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sizes: ${product.sizes} • Colors: ${product.colors}",
                                    fontSize = 10.sp,
                                    color = IlamTextMuted
                                )
                                if (product.supplier.isNotBlank()) {
                                    Text(
                                        text = "Supplier: ${product.supplier}",
                                        fontSize = 10.sp,
                                        color = IlamTextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Action Buttons Row: QR, Duplicate, Edit, Out-of-stock, Restock, Delete
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(IlamSurfaceLight, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // 1. QR Code Button
                                    OutlinedButton(
                                        onClick = { qrProductToView = product },
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.QrCode, contentDescription = "QR Code", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "QR", fontSize = 10.sp)
                                    }

                                    // 2. Duplicate Button (Owner & Manager)
                                    if (userRole.canAddEditProduct) {
                                        OutlinedButton(
                                            onClick = { viewModel.adminDuplicateProduct(product) },
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(text = "Copy", fontSize = 10.sp)
                                        }
                                    }

                                    // 3. Mark Out of Stock OR Restock
                                    if (userRole.canAddEditProduct) {
                                        if (product.stock > 0 && !isOut) {
                                            OutlinedButton(
                                                onClick = { viewModel.adminMarkOutOfStock(product) },
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(text = "Out of Stock", fontSize = 10.sp)
                                            }
                                        } else {
                                            Button(
                                                onClick = { viewModel.adminActivateProduct(product, 10) },
                                                shape = RoundedCornerShape(4.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(text = "+10 Restock", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // 4. Edit Button
                                    OutlinedButton(
                                        onClick = {
                                            editingProduct = product
                                            showProductDialog = true
                                        },
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = if (userRole == UserRole.STAFF) "View" else Strings.editProduct(language), fontSize = 10.sp)
                                    }

                                    // 5. Delete Button (Owner Only - Safe Soft Delete)
                                    if (userRole.canDeleteProduct && !product.isDeleted) {
                                        Button(
                                            onClick = { productToDelete = product },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = IlamCrimson),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(text = Strings.delete(language), fontSize = 10.sp)
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

    // Product Add/Edit Dialog with full fields & image pickers
    if (showProductDialog) {
        ProductEditDialog(
            initialProduct = editingProduct,
            categories = categories,
            language = language,
            userRole = userRole,
            onDismiss = { showProductDialog = false },
            onSave = { savedProduct ->
                viewModel.adminSaveProduct(savedProduct)
                showProductDialog = false
            }
        )
    }

    // QR Code Generator & Print Dialog
    qrProductToView?.let { prod ->
        QrCodeGeneratorDialog(
            product = prod,
            language = language,
            onDismiss = { qrProductToView = null }
        )
    }

    // Safe Delete Confirmation Dialog (Requirement 4)
    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = IlamCrimson)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "আপনি কি এই পণ্যটি মুছে ফেলতে চান?" else "Are you sure you want to delete this product?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Product: ${prod.nameEn} (SKU: ${prod.sku})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "এই পণ্যটি রুম ক্যাটালগ থেকে আর্কাইভ করা হবে। ঐতিহাসিক বিক্রয় হিসাব ও প্রতিবেদনের তথ্য সম্পূর্ণ অক্ষত থাকবে।"
                        else
                            "This product will be archived/soft-deleted. All historical sales records, customer invoices, and analytics will remain permanently intact."
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.adminArchiveProduct(prod.id)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamCrimson)
                ) {
                    Text(Strings.delete(language))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { productToDelete = null }) {
                    Text(Strings.cancel(language), color = IlamTextSecondary)
                }
            },
            containerColor = IlamWhite
        )
    }
}

// ==========================================
// POWERFUL PRODUCT ADD / EDIT DIALOG
// ==========================================
@Composable
fun ProductEditDialog(
    initialProduct: ProductEntity?,
    categories: List<CategoryEntity>,
    language: AppLanguage,
    userRole: UserRole,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var nameEn by remember { mutableStateOf(initialProduct?.nameEn ?: "") }
    var nameBn by remember { mutableStateOf(initialProduct?.nameBn ?: "") }
    var sku by remember { mutableStateOf(initialProduct?.sku ?: "ILAM-${(1000..9999).random()}") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var categoryId by remember { mutableStateOf(initialProduct?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)) }
    var brand by remember { mutableStateOf(initialProduct?.brand ?: "ILAM Atelier") }
    var supplier by remember { mutableStateOf(initialProduct?.supplier ?: "ILAM Banani Master Tailoring") }
    var primaryImageUrl by remember { mutableStateOf(initialProduct?.primaryImageUrl ?: "men_panjabi_luxe") }
    var additionalImages by remember { mutableStateOf(initialProduct?.additionalImages ?: "") }

    var purchasePriceStr by remember { mutableStateOf(initialProduct?.purchasePrice?.toInt()?.toString() ?: "1800") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toInt()?.toString() ?: "3500") }
    var discountPercentStr by remember {
        mutableStateOf(
            if (initialProduct != null && initialProduct.discountPrice > 0.0 && initialProduct.price > 0.0)
                (((initialProduct.price - initialProduct.discountPrice) / initialProduct.price) * 100).toInt().toString()
            else "0"
        )
    }
    var discountPriceStr by remember { mutableStateOf(initialProduct?.discountPrice?.toInt()?.toString() ?: "0") }

    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "15") }
    var minStockAlertStr by remember { mutableStateOf(initialProduct?.minStockAlert?.toString() ?: "3") }
    var sizes by remember { mutableStateOf(initialProduct?.sizes ?: "M, L, XL") }
    var colors by remember { mutableStateOf(initialProduct?.colors ?: "Noir Black, Slate Gray") }
    var descEn by remember { mutableStateOf(initialProduct?.descEn ?: "") }
    var descBn by remember { mutableStateOf(initialProduct?.descBn ?: "") }

    var isNewArrival by remember { mutableStateOf(initialProduct?.isNewArrival ?: true) }
    var isFeatured by remember { mutableStateOf(initialProduct?.isFeatured ?: true) }
    var isBestSeller by remember { mutableStateOf(initialProduct?.isBestSeller ?: false) }
    var isPublished by remember { mutableStateOf(initialProduct?.isPublished ?: true) }

    // Gallery Picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            primaryImageUrl = it.toString()
        }
    }

    // Camera Capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            primaryImageUrl = "camera_captured_${System.currentTimeMillis()}"
        }
    }

    val regularPrice = priceStr.toDoubleOrNull() ?: 0.0
    val discountPercent = discountPercentStr.toDoubleOrNull() ?: 0.0
    val calculatedDiscountPrice = if (discountPercent in 1.0..99.0 && regularPrice > 0.0) {
        regularPrice * (1.0 - discountPercent / 100.0)
    } else {
        discountPriceStr.toDoubleOrNull() ?: 0.0
    }
    val finalSellingPrice = if (calculatedDiscountPrice > 0.0 && calculatedDiscountPrice < regularPrice) calculatedDiscountPrice else regularPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (initialProduct == null) Strings.addProduct(language) else Strings.editProduct(language),
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    fontSize = 18.sp
                )
                Text(
                    text = "Owner & Manager Custom Product Configuration",
                    fontSize = 10.sp,
                    color = IlamTextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Image Uploader & Presets
                Text(text = "Product Photos & Gallery:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IlamSurfaceLight)
                            .border(1.dp, IlamGold, RoundedCornerShape(8.dp))
                    ) {
                        ProductImagePlaceholder(categorySlug = primaryImageUrl, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery", fontSize = 10.sp)
                            }

                            OutlinedButton(
                                onClick = { cameraLauncher.launch(null) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera", fontSize = 10.sp)
                            }
                        }

                        Text(
                            text = "Or choose sample couture asset:",
                            fontSize = 9.sp,
                            color = IlamTextMuted
                        )
                    }
                }

                // Sample image preset chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val sampleImages = listOf("men_panjabi_luxe", "women_saree_regal", "silk_sherwani", "formal_shirt", "kurti_cotton", "tshirt_supima")
                    items(sampleImages) { slug ->
                        val isSel = primaryImageUrl == slug
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) IlamBlack else IlamSurfaceLight,
                            modifier = Modifier.clickable { primaryImageUrl = slug }
                        ) {
                            Text(
                                text = slug.replace("_", " "),
                                fontSize = 9.sp,
                                color = if (isSel) IlamGold else IlamBlack,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = additionalImages,
                    onValueChange = { additionalImages = it },
                    label = { Text("Additional Images (comma-separated URIs)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = IlamBorderLight)

                // Title EN & BN
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    label = { Text("Product Name (English)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    label = { Text("Product Name (বাংলা)*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // SKU & Barcode with auto-generators
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU Identifier*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / EAN") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Brand & Supplier
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = supplier,
                        onValueChange = { supplier = it },
                        label = { Text("Supplier") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Category Selection
                Text(text = "Category:", fontSize = 11.sp, color = IlamTextMuted)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        val isSel = categoryId == cat.id
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSel) IlamBlack else IlamSurfaceLight,
                            modifier = Modifier.clickable { categoryId = cat.id }
                        ) {
                            Text(
                                text = cat.nameEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) IlamGold else IlamBlack,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = IlamBorderLight)

                // Pricing Section (Purchase Price & Selling Price & Discount %)
                Text(text = "Pricing & Discount Rules:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                // Purchase Cost (Owner / Manager only)
                if (userRole.canViewCostPrice) {
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text("🔒 Purchase / Cost Price (৳)") },
                        enabled = userRole.canChangePurchasePrice,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Regular Price (৳)*") },
                        enabled = userRole.canChangeSellingPrice,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountPercentStr,
                        onValueChange = {
                            discountPercentStr = it
                            val pct = it.toDoubleOrNull() ?: 0.0
                            if (regularPrice > 0.0 && pct in 1.0..99.0) {
                                discountPriceStr = (regularPrice * (1.0 - pct / 100.0)).toInt().toString()
                            }
                        },
                        label = { Text("Discount %") },
                        enabled = userRole.canChangeSellingPrice,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Final Selling Price Card
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IlamSurfaceLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final Customer Outlet Price:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(formatBdt(finalSellingPrice), fontSize = 15.sp, fontWeight = FontWeight.Black, color = IlamBlack)
                    }
                }

                HorizontalDivider(color = IlamBorderLight)

                // Inventory & Low Stock Alert Section
                Text(text = "Stock & Reorder Alerts:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Quantity (Stock)*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minStockAlertStr,
                        onValueChange = { minStockAlertStr = it },
                        label = { Text("Min Stock Alert*") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Sizes & Colors
                OutlinedTextField(
                    value = sizes,
                    onValueChange = { sizes = it },
                    label = { Text("Available Sizes (e.g. S, M, L, XL, XXL)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = colors,
                    onValueChange = { colors = it },
                    label = { Text("Colors (e.g. Noir Black, Regal Gold, Emerald)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Descriptions
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
                    Text("New Arrival", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    Text("Featured", fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Checkbox(checked = isBestSeller, onCheckedChange = { isBestSeller = it })
                    Text("Best Seller", fontSize = 11.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPublished, onCheckedChange = { isPublished = it })
                    Text("Publish to Active App Catalog", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stockInt = stockStr.toIntOrNull() ?: 10
                    val minStock = minStockAlertStr.toIntOrNull() ?: 3
                    val p = (initialProduct ?: ProductEntity()).copy(
                        nameEn = nameEn.ifBlank { "ILAM Luxury Couture" },
                        nameBn = nameBn.ifBlank { nameEn },
                        sku = sku.ifBlank { "ILAM-${System.currentTimeMillis() % 10000}" },
                        barcode = barcode,
                        categoryId = categoryId,
                        brand = brand,
                        supplier = supplier,
                        primaryImageUrl = primaryImageUrl,
                        additionalImages = additionalImages,
                        purchasePrice = purchasePriceStr.toDoubleOrNull() ?: 1800.0,
                        price = regularPrice.coerceAtLeast(1.0),
                        discountPrice = if (calculatedDiscountPrice > 0.0 && calculatedDiscountPrice < regularPrice) calculatedDiscountPrice else 0.0,
                        stock = stockInt,
                        minStockAlert = minStock,
                        stockStatus = if (stockInt <= 0) "OUT_OF_STOCK" else if (stockInt <= minStock) "LOW_STOCK" else "IN_STOCK",
                        sizes = sizes,
                        colors = colors,
                        descEn = descEn,
                        descBn = descBn,
                        isNewArrival = isNewArrival,
                        isFeatured = isFeatured,
                        isBestSeller = isBestSeller,
                        isPublished = isPublished,
                        isDeleted = false
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
// 2B. INVENTORY STOCK LEVELS MANAGEMENT
// ==========================================
@Composable
fun AdminInventoryView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val stocks by viewModel.allInventoryStocks.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val lowStockAlerts by viewModel.lowStockAlerts.collectAsState()

    var showStockDialog by remember { mutableStateOf(false) }
    var editingStock by remember { mutableStateOf<InventoryStockEntity?>(null) }
    var filterLowStockOnly by remember { mutableStateOf(false) }
    var selectedProductFilter by remember { mutableStateOf<Long?>(null) }

    val filteredStocks = remember(stocks, filterLowStockOnly, selectedProductFilter) {
        stocks.filter { stock ->
            (!filterLowStockOnly || stock.isLowStock) &&
            (selectedProductFilter == null || stock.productId == selectedProductFilter)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_inventory_view")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.manageInventory(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = if (language == AppLanguage.BN) "পণ্যভিত্তিক সাইজ, কালার ও ওয়্যারহাউজ স্টক লেভেল" else "Variant-level stock counts, warehouse locations & reorder points",
                    fontSize = 11.sp,
                    color = IlamTextSecondary
                )
            }

            Button(
                onClick = {
                    editingStock = null
                    showStockDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("admin_add_stock_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = if (language == AppLanguage.BN) "স্টক যুক্ত করুন" else "Add Stock", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Stock Summary Highlights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val totalUnits = stocks.sumOf { it.quantityOnHand }
            val totalReserved = stocks.sumOf { it.reservedQuantity }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Total Units", fontSize = 10.sp, color = IlamTextMuted)
                    Text(text = "$totalUnits pcs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IlamBlack)
                    Text(text = "Across ${stocks.size} variants", fontSize = 9.sp, color = IlamTextSecondary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Reserved Orders", fontSize = 10.sp, color = IlamTextMuted)
                    Text(text = "$totalReserved pcs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IlamGoldDark)
                    Text(text = "Pending checkout", fontSize = 9.sp, color = IlamTextSecondary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (lowStockAlerts.isNotEmpty()) Color(0xFFFFEBEE) else IlamWhite
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (lowStockAlerts.isNotEmpty()) Color(0xFFEF9A9A) else IlamBorderLight
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Low Stock Alerts", fontSize = 10.sp, color = Color(0xFFC62828))
                    Text(
                        text = "${lowStockAlerts.size} alerts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                    Text(text = "Below reorder point", fontSize = 9.sp, color = Color(0xFFC62828))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filters row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = filterLowStockOnly,
                onClick = { filterLowStockOnly = !filterLowStockOnly },
                label = { Text("Low Stock Only (${lowStockAlerts.size})", fontSize = 11.sp) },
                leadingIcon = {
                    if (filterLowStockOnly) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFC62828))
                    }
                }
            )

            if (selectedProductFilter != null) {
                TextButton(onClick = { selectedProductFilter = null }) {
                    Text("Clear Filter", fontSize = 11.sp, color = IlamGoldDark)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Inventory Stock Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredStocks, key = { it.id }) { stock ->
                val prod = products.find { it.id == stock.productId }
                val isLow = stock.isLowStock

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLow) Color(0xFFFFF8F8) else IlamWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLow) Color(0xFFFFCDD2) else IlamBorderLight
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = prod?.nameEn ?: "Product #${stock.productId}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = IlamBlack
                                )
                                if (isLow) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(4.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFEF5350))
                                    ) {
                                        Text(
                                            text = "LOW STOCK",
                                            color = Color(0xFFC62828),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Size: ${stock.size} • Color: ${stock.color} • ${stock.warehouseLocation} (${stock.rackBinNumber})",
                                fontSize = 11.sp,
                                color = IlamTextSecondary
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "On Hand: ${stock.quantityOnHand}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLow) Color(0xFFC62828) else IlamBlack
                                )
                                Text(
                                    text = "Reserved: ${stock.reservedQuantity}",
                                    fontSize = 11.sp,
                                    color = IlamTextMuted
                                )
                                Text(
                                    text = "Available: ${stock.availableStock}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (stock.availableStock > 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                                Text(
                                    text = "Reorder: ${stock.reorderPoint}",
                                    fontSize = 10.sp,
                                    color = IlamTextMuted
                                )
                            }
                        }

                        // Quick Restock & Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.adminRestockVariant(stock.id, 10) },
                                colors = ButtonDefaults.buttonColors(containerColor = IlamSurfaceDark),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(text = "+10", fontSize = 10.sp, color = IlamGoldLight)
                            }

                            IconButton(
                                onClick = {
                                    editingStock = stock
                                    showStockDialog = true
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Stock", tint = IlamBlack, modifier = Modifier.size(16.dp))
                            }

                            IconButton(
                                onClick = { viewModel.adminDeleteInventoryStock(stock.id) }
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Stock", tint = IlamCrimson, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStockDialog) {
        InventoryStockEditDialog(
            initialStock = editingStock,
            products = products,
            language = language,
            onDismiss = { showStockDialog = false },
            onSave = { stock ->
                viewModel.adminSaveInventoryStock(stock)
                showStockDialog = false
            }
        )
    }
}

@Composable
fun InventoryStockEditDialog(
    initialStock: InventoryStockEntity?,
    products: List<ProductEntity>,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (InventoryStockEntity) -> Unit
) {
    var selectedProductId by remember { mutableStateOf(initialStock?.productId ?: products.firstOrNull()?.id ?: 1L) }
    var size by remember { mutableStateOf(initialStock?.size ?: "M") }
    var color by remember { mutableStateOf(initialStock?.color ?: "Black") }
    var onHandStr by remember { mutableStateOf(initialStock?.quantityOnHand?.toString() ?: "10") }
    var reservedStr by remember { mutableStateOf(initialStock?.reservedQuantity?.toString() ?: "0") }
    var reorderPointStr by remember { mutableStateOf(initialStock?.reorderPoint?.toString() ?: "5") }
    var warehouseLocation by remember { mutableStateOf(initialStock?.warehouseLocation ?: "Banani Flagship") }
    var rackBinNumber by remember { mutableStateOf(initialStock?.rackBinNumber ?: "RACK-A1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialStock == null) "Add Variant Inventory Stock" else "Edit Stock Level",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Product", fontSize = 11.sp, color = IlamTextMuted)
                products.take(6).forEach { prod ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProductId = prod.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedProductId == prod.id,
                            onClick = { selectedProductId = prod.id }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${prod.nameEn} (${prod.sku})", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size (e.g. S, M, L, XL, 32, 40)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color / Fabric Tone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = onHandStr,
                        onValueChange = { onHandStr = it },
                        label = { Text("Quantity On Hand") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = reservedStr,
                        onValueChange = { reservedStr = it },
                        label = { Text("Reserved") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = reorderPointStr,
                    onValueChange = { reorderPointStr = it },
                    label = { Text("Reorder Point (Low Stock Trigger)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = warehouseLocation,
                    onValueChange = { warehouseLocation = it },
                    label = { Text("Warehouse Location / Hub") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rackBinNumber,
                    onValueChange = { rackBinNumber = it },
                    label = { Text("Rack / Bin Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val stock = (initialStock ?: InventoryStockEntity(productId = selectedProductId)).copy(
                        productId = selectedProductId,
                        size = size.ifBlank { "M" },
                        color = color.ifBlank { "Default" },
                        quantityOnHand = onHandStr.toIntOrNull() ?: 0,
                        reservedQuantity = reservedStr.toIntOrNull() ?: 0,
                        reorderPoint = reorderPointStr.toIntOrNull() ?: 5,
                        warehouseLocation = warehouseLocation.ifBlank { "Banani Flagship" },
                        rackBinNumber = rackBinNumber.ifBlank { "RACK-A1" },
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(stock)
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
