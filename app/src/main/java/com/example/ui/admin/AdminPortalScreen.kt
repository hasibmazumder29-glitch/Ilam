package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminTab
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun AdminPortalScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val currentTab by viewModel.adminTab.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamBlack)
            .statusBarsPadding()
            .testTag("admin_portal_screen")
    ) {
        // Admin Top Bar
        Surface(
            color = IlamBlack,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ILAM OWNER CONSOLE",
                        color = IlamWhite,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "পূর্ণ নিয়ন্ত্রণ ও পরিচালনা প্যানেল" else "Full Store Administration & Database Management",
                        color = IlamGold,
                        fontSize = 9.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Language Switcher
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = IlamSurfaceDark,
                        modifier = Modifier.clickable {
                            viewModel.setLanguage(if (language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN)
                        }
                    ) {
                        Text(
                            text = if (language == AppLanguage.EN) "বাংলা" else "EN",
                            color = IlamGoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Exit to Store Button
                    Button(
                        onClick = { viewModel.lockAdmin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IlamSurfaceDark,
                            contentColor = IlamWhite
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("exit_admin_button")
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = Strings.exitAdmin(language), fontSize = 11.sp)
                    }
                }
            }
        }

        // Module Tabs Scrollable Row
        val tabs = listOf(
            Triple(AdminTab.DASHBOARD, Icons.Default.Dashboard, Strings.ownerDashboard(language)),
            Triple(AdminTab.PRODUCTS, Icons.Default.Checkroom, Strings.manageProducts(language)),
            Triple(AdminTab.CATEGORIES, Icons.Default.Category, Strings.manageCategories(language)),
            Triple(AdminTab.ORDERS, Icons.Default.LocalShipping, Strings.manageOrders(language)),
            Triple(AdminTab.COURIERS, Icons.Default.RvHookup, Strings.manageCouriers(language)),
            Triple(AdminTab.COUPONS, Icons.Default.Discount, Strings.manageCoupons(language)),
            Triple(AdminTab.BANNERS, Icons.Default.ViewCarousel, Strings.manageBanners(language)),
            Triple(AdminTab.REVIEWS, Icons.Default.RateReview, Strings.manageReviews(language)),
            Triple(AdminTab.SETTINGS, Icons.Default.Settings, Strings.storeSettings(language))
        )

        ScrollableTabRow(
            selectedTabIndex = tabs.indexOfFirst { it.first == currentTab },
            containerColor = IlamDark,
            contentColor = IlamGold,
            edgePadding = 12.dp,
            divider = {}
        ) {
            tabs.forEach { (tab, icon, title) ->
                val isSelected = currentTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.setAdminTab(tab) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    selectedContentColor = IlamGold,
                    unselectedContentColor = IlamTextMuted
                )
            }
        }

        // Active Admin Tab Content View
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IlamOffWhite)
        ) {
            when (currentTab) {
                AdminTab.DASHBOARD -> AdminDashboardView(viewModel = viewModel, language = language)
                AdminTab.PRODUCTS -> AdminProductsView(viewModel = viewModel, language = language)
                AdminTab.CATEGORIES -> AdminCategoriesView(viewModel = viewModel, language = language)
                AdminTab.ORDERS -> AdminOrdersView(viewModel = viewModel, language = language)
                AdminTab.COURIERS -> AdminCouriersView(viewModel = viewModel, language = language)
                AdminTab.COUPONS -> AdminCouponsView(viewModel = viewModel, language = language)
                AdminTab.BANNERS -> AdminBannersView(viewModel = viewModel, language = language)
                AdminTab.REVIEWS -> AdminReviewsView(viewModel = viewModel, language = language)
                AdminTab.SETTINGS -> AdminSettingsView(viewModel = viewModel, language = language)
            }
        }
    }
}
