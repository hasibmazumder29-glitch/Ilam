package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun CustomerProfileWishlistScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val wishlist by viewModel.rawWishlist.collectAsState()
    val allProducts by viewModel.publishedProducts.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    val wishlistedProducts = remember(wishlist, allProducts) {
        val ids = wishlist.map { it.productId }.toSet()
        allProducts.filter { ids.contains(it.id) }
    }

    val wishlistedIds = remember(wishlist) { wishlist.map { it.productId }.toSet() }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .testTag("customer_profile_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = IlamBlack)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(IlamCardDark)
                        .border(1.dp, IlamGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T",
                        color = IlamGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Tanvir Ahmed",
                        color = IlamWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "+880 1812-345678",
                        color = IlamGoldLight,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Uttara, Dhaka, Bangladesh",
                        color = IlamTextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Action Shortcuts
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = IlamWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
        ) {
            Column {
                // My Orders
                ListItem(
                    headlineContent = { Text(Strings.orders(language), fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    leadingContent = { Icon(Icons.Outlined.LocalShipping, contentDescription = null, tint = IlamBlack) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IlamTextMuted) },
                    modifier = Modifier.clickable { viewModel.navigateTo(AppDestination.CUSTOMER_ORDERS) }
                )
                Divider(color = IlamBorderLight)

                // Language Selector
                ListItem(
                    headlineContent = { Text(if (language == AppLanguage.BN) "ভাষা নির্বাচন" else "App Language", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    supportingContent = { Text(if (language == AppLanguage.BN) "বর্তমান: বাংলা" else "Current: English", fontSize = 11.sp, color = IlamTextSecondary) },
                    leadingContent = { Icon(Icons.Outlined.Language, contentDescription = null, tint = IlamBlack) },
                    trailingContent = {
                        Text(
                            text = if (language == AppLanguage.EN) "বাংলা করুন" else "Switch to EN",
                            color = IlamGoldDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable {
                                viewModel.setLanguage(if (language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN)
                            }
                        )
                    }
                )
                Divider(color = IlamBorderLight)

                // Owner & Admin Portal Access (Role Protected)
                ListItem(
                    headlineContent = {
                        Text(
                            text = Strings.adminPortal(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = IlamGoldDark
                        )
                    },
                    supportingContent = {
                        Text(
                            text = if (language == AppLanguage.BN) "স্টোর মালিকদের জন্য পিন সুরক্ষিত প্যানেল" else "PIN-secured portal for store owners & management",
                            fontSize = 11.sp,
                            color = IlamTextSecondary
                        )
                    },
                    leadingContent = { Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = IlamGoldDark) },
                    trailingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = IlamGoldDark, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppDestination.ADMIN_GATE) }
                        .testTag("profile_admin_portal_button")
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wishlist Section
        Text(
            text = "${Strings.wishlist(language)} (${wishlistedProducts.size})",
            color = IlamBlack,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (wishlistedProducts.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = IlamWhite,
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = IlamTextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "আপনার উইশলিস্টে কোন পণ্য নেই" else "Your wishlist is empty",
                        fontSize = 13.sp,
                        color = IlamTextSecondary
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                wishlistedProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        language = language,
                        isWishlisted = true,
                        onProductClick = { viewModel.selectProduct(product) },
                        onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
