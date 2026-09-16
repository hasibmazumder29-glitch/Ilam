package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.admin.AdminGateScreen
import com.example.ui.admin.AdminPortalScreen
import com.example.ui.common.AppLanguage
import com.example.ui.components.CustomerBottomNavBar
import com.example.ui.components.GlobalLoadingOverlay
import com.example.ui.components.IlamTopBar
import com.example.ui.customer.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: IlamViewModel = viewModel()
                IlamAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun IlamAppRoot(viewModel: IlamViewModel) {
    val destination by viewModel.destination.collectAsState()
    val language by viewModel.language.collectAsState()
    val rawCartItems by viewModel.rawCartItems.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val selectedOrder by viewModel.selectedOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingMessage by viewModel.loadingMessage.collectAsState()
    val loadingSubtitle by viewModel.loadingSubtitle.collectAsState()
    val cartCount = remember(rawCartItems) { rawCartItems.sumOf { it.quantity } }

    // Intercept back button for deep screens
    BackHandler(enabled = destination != AppDestination.CUSTOMER_HOME) {
        when (destination) {
            AppDestination.CUSTOMER_PRODUCT_DETAIL -> viewModel.navigateTo(AppDestination.CUSTOMER_SHOP)
            AppDestination.CUSTOMER_ORDER_DETAIL -> viewModel.navigateTo(AppDestination.CUSTOMER_ORDERS)
            AppDestination.CUSTOMER_CHECKOUT -> viewModel.navigateTo(AppDestination.CUSTOMER_CART)
            AppDestination.ADMIN_PORTAL -> viewModel.lockAdmin()
            AppDestination.ADMIN_GATE -> viewModel.navigateTo(AppDestination.CUSTOMER_HOME)
            else -> viewModel.navigateTo(AppDestination.CUSTOMER_HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (destination) {
            AppDestination.ADMIN_GATE -> {
                AdminGateScreen(viewModel = viewModel)
            }
            AppDestination.ADMIN_PORTAL -> {
                AdminPortalScreen(viewModel = viewModel)
            }
            else -> {
                // Customer Experience
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        IlamTopBar(
                            language = language,
                            onLanguageToggle = {
                                viewModel.setLanguage(if (language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN)
                            },
                            cartItemCount = cartCount,
                            onCartClick = { viewModel.navigateTo(AppDestination.CUSTOMER_CART) },
                            onWishlistClick = { viewModel.navigateTo(AppDestination.CUSTOMER_WISHLIST) },
                            onAdminClick = { viewModel.navigateTo(AppDestination.ADMIN_GATE) }
                        )
                    },
                    bottomBar = {
                        CustomerBottomNavBar(
                            currentDestination = destination,
                            onNavigate = { dest -> viewModel.navigateTo(dest) },
                            language = language,
                            cartBadgeCount = cartCount
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (destination) {
                            AppDestination.CUSTOMER_HOME -> CustomerHomeScreen(viewModel = viewModel)
                            AppDestination.CUSTOMER_SHOP -> CustomerShopScreen(viewModel = viewModel)
                            AppDestination.CUSTOMER_PRODUCT_DETAIL -> {
                                selectedProduct?.let { product ->
                                    CustomerProductDetailScreen(product = product, viewModel = viewModel)
                                } ?: CustomerShopScreen(viewModel = viewModel)
                            }
                            AppDestination.CUSTOMER_CART, AppDestination.CUSTOMER_CHECKOUT -> {
                                CustomerCartCheckoutScreen(viewModel = viewModel)
                            }
                            AppDestination.CUSTOMER_ORDERS -> CustomerOrdersScreen(viewModel = viewModel)
                            AppDestination.CUSTOMER_ORDER_DETAIL -> {
                                selectedOrder?.let { order ->
                                    CustomerOrderDetailScreen(order = order, viewModel = viewModel)
                                } ?: CustomerOrdersScreen(viewModel = viewModel)
                            }
                            AppDestination.CUSTOMER_WISHLIST, AppDestination.CUSTOMER_PROFILE -> {
                                CustomerProfileWishlistScreen(viewModel = viewModel)
                            }
                            else -> CustomerHomeScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Global Loading Overlay (Active across any screen during network or authentication requests)
        GlobalLoadingOverlay(
            isVisible = isLoading,
            message = loadingMessage,
            subtitle = loadingSubtitle,
            language = language
        )
    }
}
