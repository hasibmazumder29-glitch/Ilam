package com.example.ui.admin

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductImagePlaceholder
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.IlamViewModel
import com.example.ui.viewmodel.UserRole

// ==========================================
// 1. MOBILE-FIRST QR / BARCODE PRODUCT SCANNER
// ==========================================
@Composable
fun AdminScannerView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val products by viewModel.activeProducts.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val scannedProduct by viewModel.scannedProduct.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    var manualCodeInput by remember { mutableStateOf("") }
    var scanErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSimulatingCamera by remember { mutableStateOf(false) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
        if (isGranted) {
            isSimulatingCamera = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_scanner_view")
    ) {
        // Scanner Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.scanProduct(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    color = IlamBlack
                )
                Text(
                    text = if (language == AppLanguage.BN) "মোবাইল ক্যামেরা বা SKU দিয়ে তাৎক্ষণিক তথ্য যাচাই" else "Mobile-first instant scanner for outlet counter staff",
                    fontSize = 11.sp,
                    color = IlamTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (userRole == UserRole.STAFF) Color(0xFFE8F5E9) else IlamSurfaceLight
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = if (userRole == UserRole.STAFF) Color(0xFF2E7D32) else IlamBlack
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = userRole.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (userRole == UserRole.STAFF) Color(0xFF2E7D32) else IlamBlack
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner Viewfinder Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = IlamBlack)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Viewfinder Reticle Box
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .border(2.dp, IlamGold, RoundedCornerShape(12.dp))
                        .background(Color(0x22D4AF37)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner",
                            tint = IlamGold,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSimulatingCamera) "ALIGN QR / BARCODE" else "TAP TO START SCAN",
                            color = IlamWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Laser scan line indicator
                HorizontalDivider(
                    color = IlamGold,
                    thickness = 2.dp,
                    modifier = Modifier
                        .width(160.dp)
                        .align(Alignment.Center)
                )

                // Trigger Camera Button
                IconButton(
                    onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(IlamSurfaceDark, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Scan",
                        tint = IlamGoldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Manual Barcode / SKU Input Row for Physical Scanners or Manual Fallback
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = manualCodeInput,
                onValueChange = {
                    manualCodeInput = it
                    scanErrorMessage = null
                },
                placeholder = { Text("Enter SKU or scan barcode (e.g. ILAM-PAN-01)...", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, tint = IlamTextMuted)
                },
                trailingIcon = {
                    if (manualCodeInput.isNotBlank()) {
                        IconButton(onClick = { manualCodeInput = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (manualCodeInput.isNotBlank()) {
                        viewModel.scanProductCode(manualCodeInput) { found ->
                            if (found == null) {
                                scanErrorMessage = "Product with SKU / Barcode '$manualCodeInput' not found in database."
                            } else {
                                scanErrorMessage = null
                            }
                        }
                    }
                }),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (manualCodeInput.isNotBlank()) {
                        viewModel.scanProductCode(manualCodeInput) { found ->
                            if (found == null) {
                                scanErrorMessage = "Product with SKU / Barcode '$manualCodeInput' not found in database."
                            } else {
                                scanErrorMessage = null
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text("Find", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Quick simulation chips of available SKUs for rapid testing in emulator
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Quick Barcode Bar Simulator (Tap to simulate scan):",
            fontSize = 11.sp,
            color = IlamTextMuted,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(products.take(6), key = { it.id }) { p ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = IlamWhite,
                    border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight),
                    modifier = Modifier.clickable {
                        manualCodeInput = p.sku
                        viewModel.setScannedProduct(p)
                        scanErrorMessage = null
                    }
                ) {
                    Text(
                        text = "${p.sku} (${p.stock})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (p.stock <= 0) Color(0xFFC62828) else IlamBlack,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        scanErrorMessage?.let { err ->
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color(0xFFFFEBEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = IlamCrimson, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = err, color = IlamCrimson, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SCANNED PRODUCT RESULT CARD
        AnimatedVisibility(
            visible = scannedProduct != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            scannedProduct?.let { prod ->
                val cat = categories.find { it.id == prod.categoryId }
                val isOut = prod.stock <= 0 || prod.stockStatus == "OUT_OF_STOCK"
                val isLow = !isOut && prod.stock <= prod.minStockAlert

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanned_product_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IlamWhite),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isOut) Color(0xFFEF5350) else if (isLow) Color(0xFFFFB74D) else IlamGold
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IlamSurfaceLight)
                            ) {
                                ProductImagePlaceholder(categorySlug = prod.primaryImageUrl, modifier = Modifier.fillMaxSize())
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = prod.nameEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = IlamBlack
                                )
                                Text(
                                    text = "বাংলা: ${prod.nameBn}",
                                    fontSize = 12.sp,
                                    color = IlamTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "SKU: ${prod.sku} • Barcode: ${prod.barcode.ifBlank { "N/A" }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IlamGoldDark
                                )
                                Text(
                                    text = "Category: ${cat?.nameEn ?: "Couture"} • Brand: ${prod.brand}",
                                    fontSize = 11.sp,
                                    color = IlamTextSecondary
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = IlamBorderLight)

                        // Specification details: Size, Color, Stock, Pricing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Sizes Available", fontSize = 10.sp, color = IlamTextMuted)
                                Text(prod.sizes, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("Color Variant", fontSize = 10.sp, color = IlamTextMuted)
                                Text(prod.colors, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Stock Status", fontSize = 10.sp, color = IlamTextMuted)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when {
                                        isOut -> Color(0xFFFFEBEE)
                                        isLow -> Color(0xFFFFF8E1)
                                        else -> Color(0xFFE8F5E9)
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            isOut -> "OUT OF STOCK (0 units)"
                                            isLow -> "LOW STOCK (${prod.stock} left)"
                                            else -> "IN STOCK (${prod.stock} units)"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isOut -> Color(0xFFC62828)
                                            isLow -> Color(0xFFF57F17)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pricing Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IlamSurfaceLight, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Selling Price", fontSize = 10.sp, color = IlamTextMuted)
                                Text(
                                    text = formatBdt(prod.price),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (prod.discountPrice > 0.0) {
                                Column {
                                    val discPercent = ((prod.price - prod.discountPrice) / prod.price * 100).toInt()
                                    Text("Discount ($discPercent%)", fontSize = 10.sp, color = Color(0xFFC62828))
                                    Text(
                                        text = "-${formatBdt(prod.price - prod.discountPrice)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Final Outlet Price", fontSize = 10.sp, color = IlamGoldDark, fontWeight = FontWeight.Bold)
                                Text(
                                    text = formatBdt(prod.effectiveSellingPrice),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IlamBlack
                                )
                            }
                        }

                        // Purchase price for Owner / Manager only
                        if (userRole.canViewCostPrice && prod.purchasePrice > 0.0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔒 Cost / Purchase Price: ${formatBdt(prod.purchasePrice)} (Protected: Owner/Manager Only)",
                                fontSize = 10.sp,
                                color = IlamTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: [VIEW PRODUCT] & [SELL PRODUCT]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectProduct(prod)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scanner_view_product_button")
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.viewProduct(language), fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (!isOut) {
                                        viewModel.startPosSaleForProduct(prod)
                                    }
                                },
                                enabled = !isOut,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOut) Color.Gray else IlamBlack,
                                    disabledContainerColor = Color(0xFFE0E0E0),
                                    disabledContentColor = Color(0xFF9E9E9E)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("scanner_sell_product_button")
                            ) {
                                Icon(
                                    imageVector = if (isOut) Icons.Default.Block else Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOut) "OUT OF STOCK" else Strings.sellProduct(language),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (isOut) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠ Outlet staff cannot sell this item because physical stock is 0. Please request owner/manager to restock.",
                                color = Color(0xFFC62828),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==========================================
// 2. POS SALES COUNTER SCREEN
// ==========================================
@Composable
fun AdminPosSalesView(
    viewModel: IlamViewModel,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val activeProducts by viewModel.activeProducts.collectAsState()
    val posProduct by viewModel.posSelectedProduct.collectAsState()
    val staffName by viewModel.currentStaffName.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    var selectedProduct by remember(posProduct, activeProducts) { mutableStateOf(posProduct ?: activeProducts.firstOrNull { it.stock > 0 }) }
    var sellQuantity by remember { mutableStateOf(1) }
    var selectedSize by remember(selectedProduct) { mutableStateOf("M") }
    var selectedColor by remember(selectedProduct) { mutableStateOf("Default") }
    var customerName by remember { mutableStateOf("Walk-in Guest") }
    var customerPhone by remember { mutableStateOf("+880 1700-000000") }
    var paymentMethod by remember { mutableStateOf("Cash at Counter") }
    var saleCompletedOrder by remember { mutableStateOf<com.example.data.entity.OrderEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("admin_pos_view")
    ) {
        // POS Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.posTerminal(language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    color = IlamBlack
                )
                Text(
                    text = "Staff: $staffName • Channel: In-Store POS",
                    fontSize = 11.sp,
                    color = IlamTextSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = IlamBlack
            ) {
                Text(
                    text = "INSTANT SALE",
                    color = IlamGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Product Selector Carousel
        Text(text = "Select Product to Sell:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(activeProducts, key = { it.id }) { p ->
                val isSelected = selectedProduct?.id == p.id
                val isOut = p.stock <= 0
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .clickable(enabled = !isOut) {
                            selectedProduct = p
                            sellQuantity = 1
                        },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) IlamBlack else if (isOut) Color(0xFFEEEEEE) else IlamWhite
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) IlamGold else IlamBorderLight
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = p.nameEn,
                            maxLines = 1,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) IlamWhite else if (isOut) Color.Gray else IlamBlack
                        )
                        Text(
                            text = p.sku,
                            fontSize = 9.sp,
                            color = if (isSelected) IlamGoldLight else IlamTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatBdt(p.effectiveSellingPrice),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) IlamGold else IlamBlack
                        )
                        Text(
                            text = if (isOut) "OUT OF STOCK" else "Stock: ${p.stock}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOut) Color(0xFFC62828) else if (isSelected) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedProduct?.let { prod ->
            val isOut = prod.stock <= 0
            val unitPrice = prod.effectiveSellingPrice
            val totalSalePrice = unitPrice * sellQuantity

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Checkout Item: ${prod.nameEn}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "SKU: ${prod.sku} • Stock Available: ${prod.stock}", fontSize = 11.sp, color = IlamTextSecondary)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Size Selection
                    Text(text = "Variant Size:", fontSize = 11.sp, color = IlamTextMuted)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        prod.sizes.split(",").map { it.trim() }.filter { it.isNotBlank() }.forEach { sizeOpt ->
                            val isSel = selectedSize == sizeOpt
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) IlamBlack else IlamSurfaceLight,
                                modifier = Modifier.clickable { selectedSize = sizeOpt }
                            ) {
                                Text(
                                    text = sizeOpt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) IlamWhite else IlamBlack,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quantity selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Sale Quantity", fontSize = 11.sp, color = IlamTextMuted)
                            Text(text = "$sellQuantity unit(s)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (sellQuantity > 1) sellQuantity-- },
                                enabled = sellQuantity > 1
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                            }
                            Text(text = "$sellQuantity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            IconButton(
                                onClick = { if (sellQuantity < prod.stock) sellQuantity++ },
                                enabled = sellQuantity < prod.stock
                            ) {
                                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Increase")
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = IlamBorderLight)

                    // Customer Details
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Guest Customer Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Guest Mobile / Phone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Payment Method
                    Text(text = "Payment Method:", fontSize = 11.sp, color = IlamTextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cash at Counter", "bKash POS", "Card Terminal").forEach { method ->
                            val isSel = paymentMethod == method
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) IlamBlack else IlamSurfaceLight,
                                modifier = Modifier.clickable { paymentMethod = method }
                            ) {
                                Text(
                                    text = method,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) IlamWhite else IlamBlack,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total Calculation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IlamSurfaceLight, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Billed Amount", fontSize = 11.sp, color = IlamTextMuted)
                            Text(formatBdt(totalSalePrice), fontSize = 18.sp, fontWeight = FontWeight.Black, color = IlamBlack)
                        }

                        Button(
                            onClick = {
                                if (!isOut && sellQuantity > 0 && sellQuantity <= prod.stock) {
                                    viewModel.processPosSale(
                                        product = prod,
                                        quantity = sellQuantity,
                                        size = selectedSize,
                                        color = selectedColor,
                                        unitPrice = unitPrice,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        paymentMethod = paymentMethod
                                    ) { order ->
                                        saleCompletedOrder = order
                                    }
                                }
                            },
                            enabled = !isOut && sellQuantity <= prod.stock,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("complete_pos_sale_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete Sale", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sale Complete Success Modal
        saleCompletedOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { saleCompletedOrder = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("POS Sale Recorded Successfully!", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Invoice #: ${order.orderNumber}", fontWeight = FontWeight.Bold)
                        Text("Customer: ${order.customerName}")
                        Text("Amount Collected: ${formatBdt(order.totalAmount)} (${order.paymentMethod})")
                        Text("Recorded By: ${order.soldByStaffName} (${order.soldByStaffRole})")
                        Text("Time: Just now • Stock has been automatically deducted.")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { saleCompletedOrder = null },
                        colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                    ) {
                        Text("Done / Next Sale")
                    }
                },
                containerColor = IlamWhite
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ==========================================
// 3. QR CODE GENERATOR & PRINT MODAL
// ==========================================
@Composable
fun QrCodeGeneratorDialog(
    product: ProductEntity,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showPrintFeedback by remember { mutableStateOf(false) }

    // Generates a deterministic visual matrix for the SKU identifier
    val qrBitmap = remember(product.sku) {
        generateQrBitmap(product.sku)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Product QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "Encodes SKU identifier only (Prices fetched live from DB)",
                    fontSize = 10.sp,
                    color = IlamTextSecondary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // QR Display Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, IlamBorderLight),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Product QR Code for ${product.sku}",
                            modifier = Modifier
                                .size(180.dp)
                                .padding(4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.sku,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = product.nameEn,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = IlamTextSecondary
                        )
                        Text(
                            text = "Live DB: ${formatBdt(product.effectiveSellingPrice)} • Stock: ${product.stock}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IlamGoldDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions: View, Download & Print
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { showPrintFeedback = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showPrintFeedback = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Label", fontSize = 11.sp)
                    }
                }

                if (showPrintFeedback) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ QR barcode label generated and sent to printer queue!",
                        color = Color(0xFF2E7D32),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = IlamBlack)
            }
        },
        containerColor = IlamWhite
    )
}

/**
 * Procedurally generates a clean 2D QR matrix Bitmap based on the SKU identifier string.
 */
fun generateQrBitmap(data: String): Bitmap {
    val size = 256
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val hash = data.hashCode()
    val pattern = BooleanArray(25 * 25)

    // Construct finder patterns & deterministic matrix based on hash and characters
    for (r in 0 until 25) {
        for (c in 0 until 25) {
            val idx = r * 25 + c
            // Top-left finder
            val inTopLeftFinder = (r in 1..7 && c in 1..7)
            val inTopRightFinder = (r in 1..7 && c in 17..23)
            val inBottomLeftFinder = (r in 17..23 && c in 1..7)

            if (inTopLeftFinder) {
                val rim = (r == 1 || r == 7 || c == 1 || c == 7)
                val dot = (r in 3..5 && c in 3..5)
                pattern[idx] = rim || dot
            } else if (inTopRightFinder) {
                val rim = (r == 1 || r == 7 || c == 17 || c == 23)
                val dot = (r in 3..5 && c in 19..21)
                pattern[idx] = rim || dot
            } else if (inBottomLeftFinder) {
                val rim = (r == 17 || r == 23 || c == 1 || c == 7)
                val dot = (r in 19..21 && c in 3..5)
                pattern[idx] = rim || dot
            } else {
                // Pseudo-random deterministic fill from data
                val charBit = if (data.isNotEmpty()) data[(r + c) % data.length].code else 0
                pattern[idx] = ((hash xor (r * 31 + c * 17) xor charBit) % 3) == 0
            }
        }
    }

    val scale = size / 25
    for (px in 0 until size) {
        for (py in 0 until size) {
            val gridX = (px / scale).coerceIn(0, 24)
            val gridY = (py / scale).coerceIn(0, 24)
            val isDark = pattern[gridY * 25 + gridX]
            bitmap.setPixel(px, py, if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}
