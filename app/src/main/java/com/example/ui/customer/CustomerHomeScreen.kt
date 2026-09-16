package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.*
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductImagePlaceholder
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun CustomerHomeScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val banners by viewModel.activeBanners.collectAsState()
    val categories by viewModel.visibleCategories.collectAsState()
    val newArrivals by viewModel.newArrivals.collectAsState()
    val featuredProducts by viewModel.featuredProducts.collectAsState()
    val bestSellers by viewModel.bestSellers.collectAsState()
    val saleProducts by viewModel.saleProducts.collectAsState()
    val reviews by viewModel.approvedReviews.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val wishlist by viewModel.rawWishlist.collectAsState()

    val wishlistedIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .verticalScroll(scrollState)
            .testTag("customer_home_screen")
    ) {
        // Complimentary delivery banner
        Surface(
            color = IlamDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = IlamGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = Strings.freeDeliveryAbove(storeSettings.freeDeliveryThreshold.toInt(), language),
                    color = IlamGoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Hero Banners Carousel / Featured Banner
        if (banners.isNotEmpty()) {
            val primaryBanner = banners.first()
            val bannerTitle = if (language == AppLanguage.BN && primaryBanner.titleBn.isNotBlank()) primaryBanner.titleBn else primaryBanner.titleEn
            val bannerSubtitle = if (language == AppLanguage.BN && primaryBanner.subtitleBn.isNotBlank()) primaryBanner.subtitleBn else primaryBanner.subtitleEn
            val buttonText = if (language == AppLanguage.BN && primaryBanner.buttonTextBn.isNotBlank()) primaryBanner.buttonTextBn else primaryBanner.buttonTextEn

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IlamBlack)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    // Visual background styling
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Surface(
                                shape = RoundedCornerShape(2.dp),
                                color = IlamGoldDark,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "উৎসব সংস্করণ" else "EXCLUSIVE EDIT",
                                    color = IlamBlack,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = bannerTitle,
                                color = IlamWhite,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = bannerSubtitle,
                                color = IlamTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                maxLines = 2
                            )
                        }

                        Button(
                            onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IlamWhite,
                                contentColor = IlamBlack
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("banner_cta_button")
                        ) {
                            Text(
                                text = buttonText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Category Chips Row
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.categories(language),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = Strings.viewAll(language),
                    color = IlamGoldDark,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        viewModel.selectCategoryFilter(null)
                    }
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val catName = if (language == AppLanguage.BN && category.nameBn.isNotBlank()) category.nameBn else category.nameEn
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = IlamWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight),
                        modifier = Modifier
                            .clickable {
                                viewModel.selectCategoryFilter(category.id)
                            }
                            .testTag("category_chip_${category.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(IlamGold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = catName,
                                color = IlamBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Section: New Arrivals
        ProductSection(
            title = Strings.newArrivals(language),
            products = newArrivals,
            language = language,
            wishlistedIds = wishlistedIds,
            onProductClick = { viewModel.selectProduct(it) },
            onWishlistToggle = { viewModel.toggleWishlist(it.id) },
            onViewAll = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) }
        )

        // Section: Curated Featured Pieces
        ProductSection(
            title = Strings.featuredPieces(language),
            products = featuredProducts,
            language = language,
            wishlistedIds = wishlistedIds,
            onProductClick = { viewModel.selectProduct(it) },
            onWishlistToggle = { viewModel.toggleWishlist(it.id) },
            onViewAll = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) }
        )

        // Section: Best Sellers
        ProductSection(
            title = Strings.bestSellers(language),
            products = bestSellers,
            language = language,
            wishlistedIds = wishlistedIds,
            onProductClick = { viewModel.selectProduct(it) },
            onWishlistToggle = { viewModel.toggleWishlist(it.id) },
            onViewAll = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) }
        )

        // Section: Exclusive Sale
        if (saleProducts.isNotEmpty()) {
            ProductSection(
                title = Strings.onSale(language),
                products = saleProducts,
                language = language,
                wishlistedIds = wishlistedIds,
                onProductClick = { viewModel.selectProduct(it) },
                onWishlistToggle = { viewModel.toggleWishlist(it.id) },
                onViewAll = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) }
            )
        }

        // Customer Reviews & Testimonials Section
        if (reviews.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = Strings.customerReviews(language),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reviews) { review ->
                        Card(
                            modifier = Modifier
                                .width(270.dp)
                                .height(150.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = IlamWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Row {
                                        repeat(review.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = IlamGold,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"${review.reviewText}\"",
                                        color = IlamTextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        maxLines = 3
                                    )
                                }
                                Text(
                                    text = review.customerName,
                                    color = IlamBlack,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Luxury Brand Footer
        Surface(
            color = IlamDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ILAM",
                    color = IlamWhite,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = if (language == AppLanguage.BN) storeSettings.descriptionBn else storeSettings.descriptionEn,
                    color = IlamTextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(vertical = 10.dp)
                )

                Divider(color = Color(0xFF26262B), modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = IlamGold, modifier = Modifier.size(16.dp))
                    Text(
                        text = storeSettings.address,
                        color = IlamWhite,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = IlamGold, modifier = Modifier.size(16.dp))
                    Text(
                        text = storeSettings.phone,
                        color = IlamGoldLight,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "© 2026 ILAM Luxury Fashion. All Rights Reserved. Dhaka, Bangladesh.",
                    color = IlamTextMuted,
                    fontSize = 10.sp
                )
            }
        }

        // Bottom space so content doesn't collide with navigation bar
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProductSection(
    title: String,
    products: List<ProductEntity>,
    language: AppLanguage,
    wishlistedIds: Set<Long>,
    onProductClick: (ProductEntity) -> Unit,
    onWishlistToggle: (ProductEntity) -> Unit,
    onViewAll: () -> Unit
) {
    if (products.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = IlamBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                fontFamily = FontFamily.Serif
            )
            Text(
                text = Strings.viewAll(language),
                color = IlamGoldDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onViewAll() }
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    language = language,
                    isWishlisted = wishlistedIds.contains(product.id),
                    onProductClick = { onProductClick(product) },
                    onWishlistToggle = { onWishlistToggle(product) },
                    modifier = Modifier.width(170.dp)
                )
            }
        }
    }
}
