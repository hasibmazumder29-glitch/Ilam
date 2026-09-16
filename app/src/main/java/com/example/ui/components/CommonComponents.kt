package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.ProductEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import java.text.NumberFormat
import java.util.Locale

@Composable
fun IlamTopBar(
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    cartItemCount: Int,
    onCartClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onAdminClick: () -> Unit,
    isAdminMode: Boolean = false,
    onExitAdmin: (() -> Unit)? = null
) {
    Surface(
        color = IlamBlack,
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Monogram
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onExitAdmin?.invoke() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IlamCardDark)
                            .border(1.dp, IlamGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "I",
                            color = IlamGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ILAM",
                            color = IlamWhite,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Serif
                        )
                        Text(
                            text = if (language == AppLanguage.BN) "ইলাম • প্রিমিয়াম ফ্যাশন" else "HAUTE COUTURE",
                            color = IlamGold,
                            fontSize = 9.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Action controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Language Switcher
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IlamSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, IlamCardDark),
                        modifier = Modifier
                            .testTag("language_switch_button")
                            .clickable { onLanguageToggle() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.EN) "বাংলা" else "EN",
                                color = IlamGoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!isAdminMode) {
                        // Wishlist Icon
                        IconButton(
                            onClick = onWishlistClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("top_bar_wishlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = IlamWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Bag / Cart with Badge
                        IconButton(
                            onClick = onCartClick,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("top_bar_cart_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemCount > 0) {
                                        Badge(
                                            containerColor = IlamGold,
                                            contentColor = IlamBlack
                                        ) {
                                            Text(
                                                text = cartItemCount.toString(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingBag,
                                    contentDescription = "Shopping Bag",
                                    tint = IlamWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Admin / Owner Portal Entry or Exit
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAdminMode) IlamGoldDark else IlamSurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isAdminMode) IlamGold else IlamCardDark),
                        modifier = Modifier
                            .testTag("admin_portal_button")
                            .clickable {
                                if (isAdminMode) onExitAdmin?.invoke() else onAdminClick()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAdminMode) Icons.Default.Storefront else Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Owner",
                                tint = if (isAdminMode) IlamBlack else IlamGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAdminMode) {
                                    if (language == AppLanguage.BN) "স্টোরে ফিরুন" else "Exit Admin"
                                } else {
                                    if (language == AppLanguage.BN) "মালিক প্যানেল" else "Owner"
                                },
                                color = if (isAdminMode) IlamBlack else IlamWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerBottomNavBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    language: AppLanguage,
    cartBadgeCount: Int
) {
    NavigationBar(
        containerColor = IlamBlack,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("customer_bottom_nav")
    ) {
        val navItems = listOf(
            Triple(AppDestination.CUSTOMER_HOME, Icons.Filled.Home to Icons.Outlined.Home, Strings.home(language)),
            Triple(AppDestination.CUSTOMER_SHOP, Icons.Filled.GridView to Icons.Outlined.GridView, Strings.shop(language)),
            Triple(AppDestination.CUSTOMER_CART, Icons.Filled.ShoppingBag to Icons.Outlined.ShoppingBag, Strings.cart(language)),
            Triple(AppDestination.CUSTOMER_ORDERS, Icons.Filled.LocalShipping to Icons.Outlined.LocalShipping, Strings.orders(language)),
            Triple(AppDestination.CUSTOMER_PROFILE, Icons.Filled.Person to Icons.Outlined.Person, Strings.account(language))
        )

        navItems.forEach { (destination, icons, label) ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    if (destination == AppDestination.CUSTOMER_CART && cartBadgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = IlamGold,
                                    contentColor = IlamBlack
                                ) {
                                    Text(text = cartBadgeCount.toString(), fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) icons.first else icons.second,
                                contentDescription = label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) icons.first else icons.second,
                            contentDescription = label
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = IlamBlack,
                    selectedTextColor = IlamGold,
                    indicatorColor = IlamGold,
                    unselectedIconColor = IlamTextMuted,
                    unselectedTextColor = IlamTextMuted
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    language: AppLanguage,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onWishlistToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val productName = if (language == AppLanguage.BN && product.nameBn.isNotBlank()) product.nameBn else product.nameEn
    val hasDiscount = product.discountPrice > 0 && product.discountPrice < product.price
    val effectivePrice = if (hasDiscount) product.discountPrice else product.price

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = IlamWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
    ) {
        Column {
            // Product Visual Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(IlamSurfaceLight)
            ) {
                // Fallback / Dynamic Image
                ProductImagePlaceholder(
                    categorySlug = product.primaryImageUrl,
                    modifier = Modifier.fillMaxSize()
                )

                // Badges overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (product.isNewArrival) {
                            BadgeChip(
                                text = if (language == AppLanguage.BN) "নতুন" else "NEW",
                                backgroundColor = IlamBlack,
                                textColor = IlamWhite
                            )
                        }
                        if (hasDiscount) {
                            val percent = (((product.price - product.discountPrice) / product.price) * 100).toInt()
                            BadgeChip(
                                text = "-$percent%",
                                backgroundColor = IlamCrimson,
                                textColor = IlamWhite
                            )
                        } else if (product.isBestSeller) {
                            BadgeChip(
                                text = if (language == AppLanguage.BN) "বেস্টসেলার" else "BESTSELLER",
                                backgroundColor = IlamGoldDark,
                                textColor = IlamWhite
                            )
                        }
                    }

                    // Wishlist Heart Button
                    IconButton(
                        onClick = onWishlistToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IlamWhite.copy(alpha = 0.9f))
                            .testTag("wishlist_toggle_${product.id}")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) IlamCrimson else IlamBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Low stock indicator
                if (product.stock in 1..4) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .background(IlamDark.copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "মাত্র ${product.stock} টি বাকি!" else "Only ${product.stock} left!",
                            color = IlamGoldLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Info Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = productName,
                    color = IlamBlack,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = formatBdt(effectivePrice),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    if (hasDiscount) {
                        Text(
                            text = formatBdt(product.price),
                            color = IlamTextMuted,
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Surface(
        shape = RoundedCornerShape(3.dp),
        color = backgroundColor,
        modifier = Modifier.height(18.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun OrderStatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Pending" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Confirmed", "Processing", "Packed" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "Shipped", "Out for Delivery" -> Color(0xFFEDE7F6) to Color(0xFF5E35B1)
        "Delivered" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Cancelled", "Returned" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> IlamSurfaceLight to IlamTextSecondary
    }

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ProductImagePlaceholder(
    categorySlug: String,
    modifier: Modifier = Modifier
) {
    val icon = when (categorySlug.lowercase()) {
        "panjabi", "banner_panjabi" -> Icons.Default.Checkroom
        "shirt", "shirts", "banner_menswear" -> Icons.Default.DryCleaning
        "tshirt", "t-shirts" -> Icons.Default.Style
        "pants" -> Icons.Default.LinearScale
        "women" -> Icons.Default.Diamond
        "shoes" -> Icons.Default.Hiking
        "watch", "accessories" -> Icons.Default.Watch
        else -> Icons.Default.ShoppingBag
    }

    Box(
        modifier = modifier
            .background(IlamSurfaceLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(IlamWhite)
                    .border(1.dp, IlamBorderLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IlamGoldDark,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ILAM HAUTE COUTURE",
                color = IlamTextMuted,
                fontSize = 8.sp,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatBdt(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    return "৳" + formatter.format(amount.toLong())
}
