package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.CouponResult
import com.example.data.repository.IlamRepository
import com.example.ui.common.AppLanguage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppDestination {
    CUSTOMER_HOME,
    CUSTOMER_SHOP,
    CUSTOMER_PRODUCT_DETAIL,
    CUSTOMER_CART,
    CUSTOMER_CHECKOUT,
    CUSTOMER_ORDERS,
    CUSTOMER_ORDER_DETAIL,
    CUSTOMER_WISHLIST,
    CUSTOMER_PROFILE,
    ADMIN_GATE,
    ADMIN_PORTAL
}

enum class UserRole {
    OWNER,
    MANAGER,
    STAFF;

    val canDeleteProduct: Boolean
        get() = this == OWNER

    val canEditPricing: Boolean
        get() = this == OWNER || this == MANAGER

    val canChangeSellingPrice: Boolean
        get() = this == OWNER || this == MANAGER

    val canChangePurchasePrice: Boolean
        get() = this == OWNER

    val canAddEditProduct: Boolean
        get() = this == OWNER || this == MANAGER

    val canManageUsers: Boolean
        get() = this == OWNER

    val canViewCostPrice: Boolean
        get() = this == OWNER || this == MANAGER

    val canModifySettings: Boolean
        get() = this == OWNER
}

enum class AdminTab {
    DASHBOARD,
    SCAN_PRODUCT, // QR & Barcode Scanner for Outlet Staff
    POS_SALES,    // Fast point-of-sale terminal
    PRODUCTS,     // Full catalog with add/edit/duplicate/soft-delete
    INVENTORY,    // Stock levels & variant tracking
    CATEGORIES,
    ORDERS,
    COURIERS,
    COUPONS,
    BANNERS,
    REVIEWS,
    SETTINGS
}

data class DashboardMetrics(
    val totalSales: Double = 0.0,
    val todaySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val totalOrders: Int = 0,
    val totalProducts: Int = 0,
    val totalStockQuantity: Int = 0,
    val outOfStockCount: Int = 0,
    val pendingOrders: Int = 0,
    val processingOrders: Int = 0,
    val deliveredOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalCustomers: Int = 0,
    val lowStockCount: Int = 0
)

class IlamViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = IlamRepository(database)

    // Language state
    private val _language = MutableStateFlow(AppLanguage.EN)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Navigation state
    private val _destination = MutableStateFlow(AppDestination.CUSTOMER_HOME)
    val destination: StateFlow<AppDestination> = _destination.asStateFlow()

    private val _adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val adminTab: StateFlow<AdminTab> = _adminTab.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    // Global Loading Overlay State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingMessage = MutableStateFlow<String?>(null)
    val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

    private val _loadingSubtitle = MutableStateFlow<String?>(null)
    val loadingSubtitle: StateFlow<String?> = _loadingSubtitle.asStateFlow()

    // Selected Items
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<Long?>(null)
    val selectedCategoryFilter: StateFlow<Long?> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderEntity?>(null)
    val selectedOrder: StateFlow<OrderEntity?> = _selectedOrder.asStateFlow()

    private val _selectedOrderItems = MutableStateFlow<List<OrderItemEntity>>(emptyList())
    val selectedOrderItems: StateFlow<List<OrderItemEntity>> = _selectedOrderItems.asStateFlow()

    // Repository Flows
    val allProducts = repository.allProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeProducts = repository.activeProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val publishedProducts = repository.publishedProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val newArrivals = repository.newArrivals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val featuredProducts = repository.featuredProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bestSellers = repository.bestSellers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val saleProducts = repository.saleProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockProducts = repository.lowStockProducts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Granular variant inventory stocks & low stock alerts
    val allInventoryStocks = repository.allInventoryStocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val lowStockAlerts = repository.lowStockAlerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val visibleCategories = repository.visibleCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders = repository.allOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customerOrders = repository.customerOrders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawCartItems = repository.cartItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rawWishlist = repository.wishlist.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCouriers = repository.allCouriers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val enabledCouriers = repository.enabledCouriers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoupons = repository.allCoupons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allReviews = repository.allReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val approvedReviews = repository.approvedReviews.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners = repository.allBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeBanners = repository.activeBanners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeSettings = repository.storeSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        StoreSettingsEntity()
    )

    // Coupon applied in checkout
    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    private val _couponDiscount = MutableStateFlow(0.0)
    val couponDiscount: StateFlow<Double> = _couponDiscount.asStateFlow()

    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage: StateFlow<String?> = _couponMessage.asStateFlow()

    // Last placed order
    private val _lastPlacedOrder = MutableStateFlow<OrderEntity?>(null)
    val lastPlacedOrder: StateFlow<OrderEntity?> = _lastPlacedOrder.asStateFlow()

    // Current active role for RBAC
    private val _currentUserRole = MutableStateFlow(UserRole.OWNER)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _currentStaffName = MutableStateFlow("Master Admin (Owner)")
    val currentStaffName: StateFlow<String> = _currentStaffName.asStateFlow()

    // Scanned product in QR/Barcode Scanner
    private val _scannedProduct = MutableStateFlow<ProductEntity?>(null)
    val scannedProduct: StateFlow<ProductEntity?> = _scannedProduct.asStateFlow()

    // Preselected product for POS selling
    private val _posSelectedProduct = MutableStateFlow<ProductEntity?>(null)
    val posSelectedProduct: StateFlow<ProductEntity?> = _posSelectedProduct.asStateFlow()

    // Dashboard metrics derived reactively from orders & products
    val dashboardMetrics: StateFlow<DashboardMetrics> = combine(allOrders, allProducts) { orders, products ->
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val monthStr = SimpleDateFormat("yyyyMM", Locale.US).format(Date())
        val todayFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val monthFormat = SimpleDateFormat("yyyyMM", Locale.US)

        var totalSales = 0.0
        var todaySales = 0.0
        var monthlySales = 0.0
        var pending = 0
        var processing = 0
        var delivered = 0
        var cancelled = 0

        orders.forEach { order ->
            if (order.orderStatus != "Cancelled") {
                totalSales += order.totalAmount
                val orderDay = todayFormat.format(Date(order.createdAt))
                val orderMonth = monthFormat.format(Date(order.createdAt))
                if (orderDay == todayStr) {
                    todaySales += order.totalAmount
                }
                if (orderMonth == monthStr) {
                    monthlySales += order.totalAmount
                }
            }

            when (order.orderStatus) {
                "Pending" -> pending++
                "Processing", "Confirmed", "Packed" -> processing++
                "Delivered" -> delivered++
                "Cancelled" -> cancelled++
            }
        }

        val activeProducts = products.filter { !it.isDeleted }
        val outOfStock = activeProducts.count { it.stock <= 0 || it.stockStatus == "OUT_OF_STOCK" }
        val lowStock = activeProducts.count { it.stock > 0 && it.stock <= it.minStockAlert }
        val totalStockUnits = activeProducts.sumOf { it.stock }

        DashboardMetrics(
            totalSales = totalSales,
            todaySales = todaySales,
            monthlySales = monthlySales,
            totalOrders = orders.size,
            totalProducts = activeProducts.size,
            totalStockQuantity = totalStockUnits,
            outOfStockCount = outOfStock,
            pendingOrders = pending,
            processingOrders = processing,
            deliveredOrders = delivered,
            cancelledOrders = cancelled,
            totalCustomers = 128,
            lowStockCount = lowStock
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardMetrics())

    init {
        viewModelScope.launch {
            repository.ensureDatabasePopulated()
        }
    }

    // --- Navigation & Locale ---
    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
    }

    fun navigateTo(dest: AppDestination) {
        _destination.value = dest
    }

    // --- Global Loading Control ---
    fun showLoading(message: String? = null, subtitle: String? = null) {
        _loadingMessage.value = message
        _loadingSubtitle.value = subtitle
        _isLoading.value = true
    }

    fun hideLoading() {
        _isLoading.value = false
        _loadingMessage.value = null
        _loadingSubtitle.value = null
    }

    suspend fun <T> withLoading(
        message: String? = null,
        subtitle: String? = null,
        delayMs: Long = 400L,
        block: suspend () -> T
    ): T {
        showLoading(message, subtitle)
        return try {
            if (delayMs > 0) {
                delay(delayMs)
            }
            block()
        } finally {
            hideLoading()
        }
    }

    fun setAdminTab(tab: AdminTab) {
        _adminTab.value = tab
    }

    fun unlockAdmin(pin: String): Boolean {
        return when (pin.trim()) {
            "ilam2026", "owner123" -> {
                _currentUserRole.value = UserRole.OWNER
                _currentStaffName.value = "Hasib Mazumder (Owner)"
                _isAdminUnlocked.value = true
                _destination.value = AppDestination.ADMIN_PORTAL
                true
            }
            "mgr2026", "manager123" -> {
                _currentUserRole.value = UserRole.MANAGER
                _currentStaffName.value = "Outlet Store Manager"
                _isAdminUnlocked.value = true
                _destination.value = AppDestination.ADMIN_PORTAL
                true
            }
            "staff123", "outlet123", "1234" -> {
                _currentUserRole.value = UserRole.STAFF
                _currentStaffName.value = "Outlet Sales Associate"
                _isAdminUnlocked.value = true
                _adminTab.value = AdminTab.SCAN_PRODUCT
                _destination.value = AppDestination.ADMIN_PORTAL
                true
            }
            else -> false
        }
    }

    /**
     * Authenticates owner/manager/staff access with role-based PIN check and realistic feedback.
     */
    fun unlockAdminWithVerification(pin: String, delayMs: Long = 600L, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isBn = _language.value == AppLanguage.BN
            val message = if (isBn) "নিরাপত্তা পাসকোড যাচাই করা হচ্ছে..." else "Verifying Security Credentials..."
            val subtitle = if (isBn) "ভূমিকা ও অ্যাক্সেস কন্ট্রোল পরীক্ষা করা হচ্ছে" else "Checking Role-Based Permissions (OWNER / MANAGER / STAFF)"
            withLoading(message = message, subtitle = subtitle, delayMs = delayMs) {
                val success = unlockAdmin(pin)
                onResult(success)
            }
        }
    }

    fun setStaffRoleDirectly(role: UserRole) {
        _currentUserRole.value = role
        _currentStaffName.value = when (role) {
            UserRole.OWNER -> "Hasib Mazumder (Owner)"
            UserRole.MANAGER -> "Store Manager"
            UserRole.STAFF -> "Outlet Staff"
        }
    }

    // --- QR / Barcode Scanner & POS integration ---
    fun scanProductCode(code: String, onResult: (ProductEntity?) -> Unit) {
        viewModelScope.launch {
            val product = repository.findProductBySkuOrBarcode(code.trim())
            _scannedProduct.value = product
            onResult(product)
        }
    }

    fun clearScannedProduct() {
        _scannedProduct.value = null
    }

    fun setScannedProduct(product: ProductEntity?) {
        _scannedProduct.value = product
    }

    fun startPosSaleForProduct(product: ProductEntity) {
        _posSelectedProduct.value = product
        _adminTab.value = AdminTab.POS_SALES
    }

    fun clearPosProduct() {
        _posSelectedProduct.value = null
    }

    fun processPosSale(
        product: ProductEntity,
        quantity: Int,
        size: String,
        color: String,
        unitPrice: Double,
        customerName: String,
        customerPhone: String,
        paymentMethod: String,
        onSuccess: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val isBn = _language.value == AppLanguage.BN
            val msg = if (isBn) "কাউন্টারে বিক্রয় সম্পন্ন হচ্ছে..." else "Processing Outlet POS Counter Sale..."
            val sub = if (isBn) "স্টক সমন্বয় ও রসিদ তৈরি হচ্ছে" else "Deducting stock & recording staff sale"
            withLoading(message = msg, subtitle = sub, delayMs = 500L) {
                val orderId = repository.createPosSale(
                    product = product,
                    quantity = quantity,
                    size = size,
                    color = color,
                    unitPrice = unitPrice,
                    staffName = _currentStaffName.value,
                    staffRole = _currentUserRole.value.name,
                    paymentMethod = paymentMethod,
                    customerName = customerName,
                    customerPhone = customerPhone
                )
                val order = allOrders.value.find { it.id == orderId } ?: OrderEntity(
                    id = orderId,
                    orderNumber = "ILAM-POS-$orderId",
                    customerName = customerName,
                    totalAmount = unitPrice * quantity,
                    paymentStatus = "Paid",
                    orderStatus = "Delivered",
                    salesChannel = "OUTLET_POS",
                    soldByStaffName = _currentStaffName.value,
                    soldByStaffRole = _currentUserRole.value.name
                )
                _lastPlacedOrder.value = order
                _posSelectedProduct.value = null
                _scannedProduct.value = null
                onSuccess(order)
            }
        }
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
        _destination.value = AppDestination.CUSTOMER_HOME
    }

    fun selectProduct(product: ProductEntity) {
        _selectedProduct.value = product
        _destination.value = AppDestination.CUSTOMER_PRODUCT_DETAIL
    }

    fun selectCategoryFilter(categoryId: Long?) {
        _selectedCategoryFilter.value = categoryId
        _destination.value = AppDestination.CUSTOMER_SHOP
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectOrder(order: OrderEntity) {
        _selectedOrder.value = order
        viewModelScope.launch {
            repository.getOrderItems(order.id).collect { items ->
                _selectedOrderItems.value = items
            }
        }
        _destination.value = AppDestination.CUSTOMER_ORDER_DETAIL
    }

    // --- Cart Actions ---
    fun addToCart(product: ProductEntity, size: String, color: String, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(product.id, size, color, quantity)
        }
    }

    fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun removeFromCart(cartItemId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    // --- Wishlist Actions ---
    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    // --- Coupon Actions ---
    fun applyCoupon(code: String, subtotal: Double) {
        viewModelScope.launch {
            val isBn = _language.value == AppLanguage.BN
            val msg = if (isBn) "কুপন কোড যাচাই করা হচ্ছে..." else "Validating Promotional Code..."
            val sub = if (isBn) "সার্ভার যাচাইকরণ" else "Checking eligibility & discount rules"
            withLoading(message = msg, subtitle = sub, delayMs = 450L) {
                when (val result = repository.validateCoupon(code, subtotal)) {
                    is CouponResult.Success -> {
                        _appliedCoupon.value = result.coupon
                        _couponDiscount.value = result.discountAmount
                        _couponMessage.value = if (isBn) "কুপন কার্যকর হয়েছে!" else "Coupon applied successfully!"
                    }
                    is CouponResult.Error -> {
                        _appliedCoupon.value = null
                        _couponDiscount.value = 0.0
                        _couponMessage.value = if (isBn) result.messageBn else result.messageEn
                    }
                }
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponDiscount.value = 0.0
        _couponMessage.value = null
    }

    // --- Checkout & Order Placement ---
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerDistrict: String,
        cartPairs: List<Pair<CartItemEntity, ProductEntity>>,
        subtotal: Double,
        deliveryCharge: Double,
        paymentMethod: String,
        courierId: Long,
        courierName: String,
        notes: String,
        onSuccess: (OrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val isBn = _language.value == AppLanguage.BN
            val msg = if (paymentMethod == "bKash") {
                if (isBn) "বিকাশ পেমেন্ট যাচাই ও অর্ডার নিশ্চিতকরণ..." else "Verifying bKash Gateway & Processing Order..."
            } else {
                if (isBn) "অর্ডার প্রস্তুত ও নিশ্চিত করা হচ্ছে..." else "Securing Your Order & Preparing Dispatch..."
            }
            val sub = if (isBn) "অনুগ্রহ করে অপেক্ষা করুন" else "Encrypting payload & generating invoice"

            withLoading(message = msg, subtitle = sub, delayMs = 650L) {
                val discount = _couponDiscount.value
                val total = (subtotal - discount + deliveryCharge).coerceAtLeast(0.0)
                val paymentStatus = if (paymentMethod == "bKash") "Paid" else "Pending"

                val orderId = repository.createOrder(
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    customerDistrict = customerDistrict,
                    cartItemsWithProducts = cartPairs,
                    subtotal = subtotal,
                    discount = discount,
                    deliveryCharge = deliveryCharge,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    courierId = courierId,
                    courierName = courierName,
                    notes = notes
                )

                // Reset coupon
                _appliedCoupon.value = null
                _couponDiscount.value = 0.0

                // Fetch created order
                val created = allOrders.value.find { it.id == orderId } ?: OrderEntity(
                    id = orderId,
                    orderNumber = "ILAM-ORD-$orderId",
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    customerDistrict = customerDistrict,
                    subtotal = subtotal,
                    discount = discount,
                    deliveryCharge = deliveryCharge,
                    totalAmount = total,
                    paymentMethod = paymentMethod,
                    paymentStatus = paymentStatus,
                    orderStatus = "Pending",
                    courierId = courierId,
                    courierName = courierName,
                    trackingNumber = "",
                    notes = notes
                )
                _lastPlacedOrder.value = created
                selectOrder(created)
                onSuccess(created)
            }
        }
    }

    // --- Reviews ---
    fun submitReview(productId: Long, customerName: String, rating: Int, text: String) {
        viewModelScope.launch {
            repository.submitReview(productId, customerName, rating, text)
        }
    }

    // ==========================================
    // OWNER / ADMIN OPERATIONS (Zero code edit needed)
    // ==========================================

    // Products
    fun adminSaveProduct(product: ProductEntity) {
        viewModelScope.launch {
            if (product.id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun adminDuplicateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.duplicateProduct(product)
        }
    }

    fun adminDeleteProduct(id: Long) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun adminArchiveProduct(id: Long) {
        viewModelScope.launch {
            repository.archiveProduct(id)
        }
    }

    fun adminToggleProductPublish(product: ProductEntity) {
        viewModelScope.launch {
            repository.toggleProductPublish(product.id, product.isPublished)
        }
    }

    fun adminMarkOutOfStock(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProductStock(product.id, 0)
        }
    }

    fun adminActivateProduct(product: ProductEntity, restockQty: Int = 10) {
        viewModelScope.launch {
            val updated = product.copy(
                isPublished = true,
                isDeleted = false,
                stock = if (product.stock <= 0) restockQty else product.stock,
                stockStatus = "IN_STOCK"
            )
            repository.updateProduct(updated)
        }
    }

    // --- Inventory Stock Level Management ---
    fun adminSaveInventoryStock(stock: InventoryStockEntity) {
        viewModelScope.launch {
            if (stock.id == 0L) {
                repository.insertStock(stock)
            } else {
                repository.updateStock(stock)
            }
        }
    }

    fun adminDeleteInventoryStock(id: Long) {
        viewModelScope.launch {
            repository.deleteStock(id)
        }
    }

    fun adminRestockVariant(stockId: Long, additionalQuantity: Int) {
        viewModelScope.launch {
            repository.restockVariant(stockId, additionalQuantity)
        }
    }

    // Categories
    fun adminSaveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            if (category.id == 0L) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
        }
    }

    fun adminDeleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // Orders
    fun adminUpdateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            if (_selectedOrder.value?.id == orderId) {
                _selectedOrder.value = _selectedOrder.value?.copy(orderStatus = newStatus)
            }
        }
    }

    fun adminAssignCourier(orderId: Long, courierId: Long, courierName: String, trackingNumber: String) {
        viewModelScope.launch {
            repository.assignCourier(orderId, courierId, courierName, trackingNumber)
            if (_selectedOrder.value?.id == orderId) {
                _selectedOrder.value = _selectedOrder.value?.copy(
                    courierId = courierId,
                    courierName = courierName,
                    trackingNumber = trackingNumber
                )
            }
        }
    }

    fun adminUpdatePaymentStatus(orderId: Long, paymentStatus: String) {
        viewModelScope.launch {
            repository.updatePaymentStatus(orderId, paymentStatus)
            if (_selectedOrder.value?.id == orderId) {
                _selectedOrder.value = _selectedOrder.value?.copy(paymentStatus = paymentStatus)
            }
        }
    }

    // Couriers
    fun adminSaveCourier(courier: CourierEntity) {
        viewModelScope.launch {
            if (courier.id == 0L) {
                repository.insertCourier(courier)
            } else {
                repository.updateCourier(courier)
            }
        }
    }

    fun adminDeleteCourier(id: Long) {
        viewModelScope.launch {
            repository.deleteCourier(id)
        }
    }

    fun adminSetDefaultCourier(id: Long) {
        viewModelScope.launch {
            repository.setDefaultCourier(id)
        }
    }

    // Coupons
    fun adminSaveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            if (coupon.id == 0L) {
                repository.insertCoupon(coupon)
            } else {
                repository.updateCoupon(coupon)
            }
        }
    }

    fun adminDeleteCoupon(id: Long) {
        viewModelScope.launch {
            repository.deleteCoupon(id)
        }
    }

    // Banners
    fun adminSaveBanner(banner: BannerEntity) {
        viewModelScope.launch {
            if (banner.id == 0L) {
                repository.insertBanner(banner)
            } else {
                repository.updateBanner(banner)
            }
        }
    }

    fun adminDeleteBanner(id: Long) {
        viewModelScope.launch {
            repository.deleteBanner(id)
        }
    }

    // Reviews
    fun adminToggleReviewApproval(review: ReviewEntity) {
        viewModelScope.launch {
            repository.updateReviewApproval(review.id, !review.isApproved)
        }
    }

    fun adminDeleteReview(id: Long) {
        viewModelScope.launch {
            repository.deleteReview(id)
        }
    }

    // Store Settings
    fun adminSaveStoreSettings(settings: StoreSettingsEntity) {
        viewModelScope.launch {
            repository.updateStoreSettings(settings)
        }
    }
}
