package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ProductEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductImagePlaceholder
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun CustomerProductDetailScreen(
    product: ProductEntity,
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val wishlist by viewModel.rawWishlist.collectAsState()
    val allReviews by viewModel.approvedReviews.collectAsState()

    val isWishlisted = remember(wishlist, product.id) {
        wishlist.any { it.productId == product.id }
    }
    val productReviews = remember(allReviews, product.id) {
        allReviews.filter { it.productId == product.id }
    }

    val availableSizes = remember(product.sizes) {
        product.sizes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    val availableColors = remember(product.colors) {
        product.colors.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    var selectedSize by remember { mutableStateOf(availableSizes.firstOrNull() ?: "M") }
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "Black") }
    var quantity by remember { mutableIntStateOf(1) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    val productName = if (language == AppLanguage.BN && product.nameBn.isNotBlank()) product.nameBn else product.nameEn
    val productDesc = if (language == AppLanguage.BN && product.descBn.isNotBlank()) product.descBn else product.descEn
    val hasDiscount = product.discountPrice > 0 && product.discountPrice < product.price
    val effectivePrice = if (hasDiscount) product.discountPrice else product.price

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            // Persistent Bottom Action Bar
            Surface(
                color = IlamWhite,
                shadowElevation = 12.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wishlist button
                    OutlinedButton(
                        onClick = { viewModel.toggleWishlist(product.id) },
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) IlamCrimson else IlamBlack
                        )
                    }

                    // Add to Bag Button
                    Button(
                        onClick = {
                            viewModel.addToCart(product, selectedSize, selectedColor, quantity)
                            snackbarMessage = if (language == AppLanguage.BN) "ব্যাগে যুক্ত করা হয়েছে!" else "Added to your Bag!"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_to_bag_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IlamBlack,
                            contentColor = IlamWhite
                        ),
                        enabled = product.stock > 0
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (product.stock > 0) Strings.addToBag(language) else Strings.outOfStock(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Buy Now Button
                    Button(
                        onClick = {
                            viewModel.addToCart(product, selectedSize, selectedColor, quantity)
                            viewModel.navigateTo(AppDestination.CUSTOMER_CART)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("buy_now_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IlamGoldDark,
                            contentColor = IlamWhite
                        ),
                        enabled = product.stock > 0
                    ) {
                        Text(
                            text = Strings.buyNow(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IlamOffWhite)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .testTag("product_detail_screen")
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) },
                    modifier = Modifier.testTag("product_detail_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = IlamBlack)
                }

                Text(
                    text = "SKU: ${product.sku}",
                    color = IlamTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )

                IconButton(onClick = { viewModel.toggleWishlist(product.id) }) {
                    Icon(
                        imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Wishlist",
                        tint = if (isWishlisted) IlamCrimson else IlamBlack
                    )
                }
            }

            // Product Hero Showcase
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(IlamSurfaceLight)
            ) {
                ProductImagePlaceholder(
                    categorySlug = product.primaryImageUrl,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay tag
                if (hasDiscount) {
                    val discountPercent = (((product.price - product.discountPrice) / product.price) * 100).toInt()
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = IlamCrimson,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "SAVE $discountPercent%",
                            color = IlamWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Product Details Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name & Brand
                Text(
                    text = "ILAM HAUTE COUTURE",
                    color = IlamGoldDark,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = productName,
                    color = IlamBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Price Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formatBdt(effectivePrice),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    if (hasDiscount) {
                        Text(
                            text = formatBdt(product.price),
                            color = IlamTextMuted,
                            fontSize = 15.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    // Stock availability pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (product.stock > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (product.stock > 0) Strings.inStock(product.stock, language) else Strings.outOfStock(language),
                            color = if (product.stock > 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Size Selector
                if (availableSizes.isNotEmpty()) {
                    Text(
                        text = Strings.selectSize(language),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableSizes.forEach { size ->
                            val isSelected = selectedSize == size
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) IlamBlack else IlamWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IlamBlack else IlamBorderLight),
                                modifier = Modifier
                                    .clickable { selectedSize = size }
                                    .testTag("size_chip_$size")
                            ) {
                                Text(
                                    text = size,
                                    color = if (isSelected) IlamWhite else IlamBlack,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Color Selector
                if (availableColors.isNotEmpty()) {
                    Text(
                        text = Strings.selectColor(language),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableColors.forEach { color ->
                            val isSelected = selectedColor == color
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) IlamGoldDark else IlamWhite,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IlamGoldDark else IlamBorderLight),
                                modifier = Modifier.clickable { selectedColor = color }
                            ) {
                                Text(
                                    text = color,
                                    color = if (isSelected) IlamWhite else IlamBlack,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "পরিমাণ" else "Quantity",
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(IlamWhite, RoundedCornerShape(6.dp))
                            .border(1.dp, IlamBorderLight, RoundedCornerShape(6.dp))
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp))
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { if (quantity < product.stock) quantity++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Description
                Text(
                    text = Strings.productDetails(language),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = productDesc,
                    color = IlamTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 16.dp))

                // Reviews Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Strings.reviewsAndRatings(language)} (${productReviews.size})",
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif
                    )
                    TextButton(onClick = { showReviewDialog = true }) {
                        Text(
                            text = Strings.writeReview(language),
                            color = IlamGoldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (productReviews.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.BN) "এই পণ্যের এখনও কোন রিভিউ নেই। প্রথম রিভিউ দিন!" else "No customer reviews yet. Be the first to review!",
                        color = IlamTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        productReviews.forEach { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = review.customerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = IlamBlack
                                        )
                                        Row {
                                            repeat(review.rating) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = IlamGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = review.reviewText,
                                        color = IlamTextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Feedback Snackbar
    snackbarMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2000)
            snackbarMessage = null
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                color = IlamBlack,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 6.dp
            ) {
                Text(
                    text = msg,
                    color = IlamWhite,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }

    // Leave a Review Dialog
    if (showReviewDialog) {
        var reviewerName by remember { mutableStateOf("") }
        var reviewText by remember { mutableStateOf("") }
        var rating by remember { mutableIntStateOf(5) }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = Strings.writeReview(language),
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = IlamBlack
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Star Rating Picker
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (1..5).forEach { starIndex ->
                            IconButton(onClick = { rating = starIndex }) {
                                Icon(
                                    imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$starIndex Stars",
                                    tint = IlamGold,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewerName,
                        onValueChange = { reviewerName = it },
                        label = { Text(Strings.yourName(language)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text(Strings.yourReview(language)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reviewerName.isNotBlank() && reviewText.isNotBlank()) {
                            viewModel.submitReview(product.id, reviewerName, rating, reviewText)
                            showReviewDialog = false
                            snackbarMessage = if (language == AppLanguage.BN) "রিভিউ জমা দেওয়া হয়েছে!" else "Thank you for your review!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                ) {
                    Text(text = Strings.submit(language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text(text = Strings.cancel(language), color = IlamTextSecondary)
                }
            },
            containerColor = IlamWhite
        )
    }
}
