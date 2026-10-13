package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.DatabasePrepopulator
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class CouponResult {
    data class Success(val coupon: CouponEntity, val discountAmount: Double) : CouponResult()
    data class Error(val messageEn: String, val messageBn: String) : CouponResult()
}

class IlamRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val categoryDao = db.categoryDao()
    private val orderDao = db.orderDao()
    private val cartDao = db.cartDao()
    private val wishlistDao = db.wishlistDao()
    private val courierDao = db.courierDao()
    private val couponDao = db.couponDao()
    private val reviewDao = db.reviewDao()
    private val bannerDao = db.bannerDao()
    private val storeSettingsDao = db.storeSettingsDao()
    private val userDao = db.userDao()
    private val inventoryDao = db.inventoryDao()

    // --- Inventory Stocks & Tracking ---
    val allInventoryStocks: Flow<List<InventoryStockEntity>> = inventoryDao.getAllInventoryStocks()
    val lowStockAlerts: Flow<List<InventoryStockEntity>> = inventoryDao.getLowStockAlerts()

    fun getStocksByProduct(productId: Long): Flow<List<InventoryStockEntity>> = inventoryDao.getStocksByProductId(productId)

    suspend fun insertStock(stock: InventoryStockEntity): Long = withContext(Dispatchers.IO) {
        inventoryDao.insertStock(stock)
    }

    suspend fun updateStock(stock: InventoryStockEntity) = withContext(Dispatchers.IO) {
        inventoryDao.updateStock(stock)
    }

    suspend fun deleteStock(id: Long) = withContext(Dispatchers.IO) {
        inventoryDao.deleteStockById(id)
    }

    suspend fun restockVariant(stockId: Long, additionalQuantity: Int) = withContext(Dispatchers.IO) {
        inventoryDao.restockVariant(stockId, additionalQuantity)
    }

    suspend fun getTotalAvailableStock(productId: Long): Int = withContext(Dispatchers.IO) {
        inventoryDao.getTotalAvailableStockForProduct(productId) ?: 0
    }

    // Ensure database is populated on first access
    suspend fun ensureDatabasePopulated() = withContext(Dispatchers.IO) {
        val count = categoryDao.getAllCategories().firstOrNull()?.size ?: 0
        if (count == 0) {
            DatabasePrepopulator.populateInitialData(db)
        }
    }

    // --- Products ---
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()
    val publishedProducts: Flow<List<ProductEntity>> = productDao.getPublishedProducts()
    val newArrivals: Flow<List<ProductEntity>> = productDao.getNewArrivals()
    val featuredProducts: Flow<List<ProductEntity>> = productDao.getFeaturedProducts()
    val bestSellers: Flow<List<ProductEntity>> = productDao.getBestSellers()
    val saleProducts: Flow<List<ProductEntity>> = productDao.getSaleProducts()
    val lowStockProducts: Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun getProductById(id: Long): Flow<ProductEntity?> = productDao.getProductById(id)
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>> = productDao.getProductsByCategory(categoryId)

    suspend fun findProductBySkuOrBarcode(code: String): ProductEntity? = withContext(Dispatchers.IO) {
        productDao.findProductBySkuOrBarcode(code.trim())
    }

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val updated = product.copy(
            stockStatus = when {
                product.stock <= 0 -> "OUT_OF_STOCK"
                product.stock <= product.minStockAlert -> "LOW_STOCK"
                else -> "IN_STOCK"
            }
        )
        productDao.insert(updated)
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        val updated = product.copy(
            stockStatus = when {
                product.stock <= 0 -> "OUT_OF_STOCK"
                product.stock <= product.minStockAlert -> "LOW_STOCK"
                else -> "IN_STOCK"
            }
        )
        productDao.update(updated)
    }

    suspend fun duplicateProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val uniqueSku = "${product.sku}-COPY-${System.currentTimeMillis() % 1000}"
        val copy = product.copy(
            id = 0,
            sku = uniqueSku,
            barcode = if (product.barcode.isNotBlank()) "${product.barcode}-1" else "",
            nameEn = "${product.nameEn} (Copy)",
            nameBn = "${product.nameBn} (অনুলিপি)",
            createdAt = System.currentTimeMillis()
        )
        productDao.insert(copy)
    }

    suspend fun archiveProduct(id: Long) = withContext(Dispatchers.IO) {
        productDao.archiveProduct(id)
    }

    suspend fun deleteProductPermanently(id: Long) = withContext(Dispatchers.IO) {
        productDao.deleteById(id)
    }

    suspend fun deleteProduct(id: Long) = withContext(Dispatchers.IO) {
        // Soft delete / archive to protect historical sales records!
        productDao.archiveProduct(id)
    }

    suspend fun decreaseProductStock(id: Long, quantity: Int) = withContext(Dispatchers.IO) {
        productDao.decreaseStockQuantity(id, quantity)
    }

    suspend fun updateProductStock(id: Long, newStock: Int) = withContext(Dispatchers.IO) {
        productDao.updateStockLevel(id, newStock)
    }

    suspend fun toggleProductPublish(id: Long, currentPublished: Boolean) = withContext(Dispatchers.IO) {
        productDao.setPublished(id, !currentPublished)
    }

    // --- Categories ---
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val visibleCategories: Flow<List<CategoryEntity>> = categoryDao.getVisibleCategories()

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insert(category)
    }

    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        categoryDao.deleteById(id)
    }

    // --- Orders ---
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val customerOrders: Flow<List<OrderEntity>> = orderDao.getOrdersByCustomer(1)

    fun getOrderById(orderId: Long): Flow<OrderEntity?> = orderDao.getOrderById(orderId)
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>> = orderDao.getOrderItems(orderId)

    suspend fun createOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        customerDistrict: String,
        cartItemsWithProducts: List<Pair<CartItemEntity, ProductEntity>>,
        subtotal: Double,
        discount: Double,
        deliveryCharge: Double,
        totalAmount: Double,
        paymentMethod: String,
        paymentStatus: String,
        courierId: Long,
        courierName: String,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyMMdd-HHmm", Locale.US)
        val orderNumber = "ILAM-${dateFormat.format(Date())}"

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerId = 1,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            customerDistrict = customerDistrict,
            subtotal = subtotal,
            discount = discount,
            deliveryCharge = deliveryCharge,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            orderStatus = "Pending",
            courierId = courierId,
            courierName = courierName,
            trackingNumber = "",
            notes = notes
        )
        val orderId = orderDao.insertOrder(order)

        val orderItems = cartItemsWithProducts.map { (cartItem, product) ->
            // Decrement remaining stock automatically upon sale
            productDao.decreaseStockQuantity(product.id, cartItem.quantity)

            OrderItemEntity(
                orderId = orderId,
                productId = product.id,
                productName = product.nameEn,
                productPrice = if (product.discountPrice > 0) product.discountPrice else product.price,
                quantity = cartItem.quantity,
                size = cartItem.selectedSize,
                color = cartItem.selectedColor
            )
        }
        orderDao.insertOrderItems(orderItems)

        // Clear user's cart
        cartDao.clearCart(1)

        orderId
    }

    suspend fun createPosSale(
        product: ProductEntity,
        quantity: Int,
        size: String,
        color: String,
        unitPrice: Double,
        staffName: String,
        staffRole: String,
        paymentMethod: String = "Cash at Counter",
        customerName: String = "Walk-in Guest",
        customerPhone: String = "+880 1700-000000"
    ): Long = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("yyMMdd-HHmmss", Locale.US)
        val orderNumber = "ILAM-POS-${dateFormat.format(Date())}-${(100..999).random()}"
        val totalAmount = unitPrice * quantity

        val order = OrderEntity(
            orderNumber = orderNumber,
            customerId = 1,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = "Banani Flagship Store Outlet",
            customerDistrict = "Dhaka",
            subtotal = totalAmount,
            discount = 0.0,
            deliveryCharge = 0.0,
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            paymentStatus = "Paid",
            orderStatus = "Delivered", // Instant over-the-counter fulfillment
            courierId = 0,
            courierName = "In-Store Counter Pickup",
            trackingNumber = "POS-INSTANT",
            notes = "Counter Sale processed by $staffName ($staffRole)",
            salesChannel = "OUTLET_POS",
            soldByStaffName = staffName,
            soldByStaffRole = staffRole,
            createdAt = System.currentTimeMillis()
        )
        val orderId = orderDao.insertOrder(order)

        val item = OrderItemEntity(
            orderId = orderId,
            productId = product.id,
            productName = product.nameEn,
            productPrice = unitPrice,
            quantity = quantity,
            size = size,
            color = color
        )
        orderDao.insertOrderItems(listOf(item))

        // Decrement stock automatically
        productDao.decreaseStockQuantity(product.id, quantity)

        orderId
    }

    suspend fun updateOrderStatus(orderId: Long, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)
    }

    suspend fun assignCourier(orderId: Long, courierId: Long, courierName: String, trackingNumber: String) = withContext(Dispatchers.IO) {
        orderDao.assignCourier(orderId, courierId, courierName, trackingNumber)
    }

    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String) = withContext(Dispatchers.IO) {
        orderDao.updatePaymentStatus(orderId, paymentStatus)
    }

    // --- Cart ---
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getCartItems(1)

    suspend fun addToCart(productId: Long, size: String, color: String, quantity: Int = 1) = withContext(Dispatchers.IO) {
        cartDao.insertCartItem(
            CartItemEntity(
                customerId = 1,
                productId = productId,
                quantity = quantity,
                selectedSize = size,
                selectedColor = color
            )
        )
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(cartItemId)
        } else {
            // Find and update
            val currentItems = cartDao.getCartItems(1).firstOrNull() ?: emptyList()
            currentItems.find { it.id == cartItemId }?.let { item ->
                cartDao.updateCartItem(item.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(cartItemId: Long) = withContext(Dispatchers.IO) {
        cartDao.deleteCartItem(cartItemId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart(1)
    }

    // --- Wishlist ---
    val wishlist: Flow<List<WishlistItemEntity>> = wishlistDao.getWishlist(1)

    suspend fun toggleWishlist(productId: Long) = withContext(Dispatchers.IO) {
        val isWishlisted = wishlistDao.isInWishlist(1, productId).firstOrNull() ?: false
        if (isWishlisted) {
            wishlistDao.deleteWishlist(1, productId)
        } else {
            wishlistDao.insertWishlist(WishlistItemEntity(customerId = 1, productId = productId))
        }
    }

    fun isProductInWishlist(productId: Long): Flow<Boolean> = wishlistDao.isInWishlist(1, productId)

    // --- Couriers ---
    val allCouriers: Flow<List<CourierEntity>> = courierDao.getAllCouriers()
    val enabledCouriers: Flow<List<CourierEntity>> = courierDao.getEnabledCouriers()

    suspend fun insertCourier(courier: CourierEntity): Long = withContext(Dispatchers.IO) {
        courierDao.insert(courier)
    }

    suspend fun updateCourier(courier: CourierEntity) = withContext(Dispatchers.IO) {
        courierDao.update(courier)
    }

    suspend fun deleteCourier(id: Long) = withContext(Dispatchers.IO) {
        courierDao.deleteById(id)
    }

    suspend fun setDefaultCourier(id: Long) = withContext(Dispatchers.IO) {
        courierDao.setDefaultCourier(id)
    }

    // --- Coupons ---
    val allCoupons: Flow<List<CouponEntity>> = couponDao.getAllCoupons()

    suspend fun validateCoupon(code: String, subtotal: Double): CouponResult = withContext(Dispatchers.IO) {
        val coupon = couponDao.getCouponByCode(code.trim().uppercase())
            ?: return@withContext CouponResult.Error(
                "Invalid coupon code",
                "কুপন কোডটি সঠিক নয়"
            )

        if (!coupon.isActive) {
            return@withContext CouponResult.Error(
                "This coupon is currently inactive",
                "এই কুপনটি বর্তমানে নিষ্ক্রিয় রয়েছে"
            )
        }

        if (subtotal < coupon.minOrder) {
            return@withContext CouponResult.Error(
                "Minimum order amount of ৳${coupon.minOrder.toInt()} required",
                "ন্যূনতম ৳${coupon.minOrder.toInt()} টাকার অর্ডারে এই কুপন প্রযোজ্য"
            )
        }

        if (coupon.usedCount >= coupon.maxUsage) {
            return@withContext CouponResult.Error(
                "Coupon usage limit exceeded",
                "কুপন ব্যবহারের সর্বোচ্চ সীমা শেষ হয়েছে"
            )
        }

        val discountAmount = if (coupon.discountPercent > 0) {
            (subtotal * coupon.discountPercent / 100.0)
        } else {
            coupon.fixedDiscount
        }

        CouponResult.Success(coupon, discountAmount)
    }

    suspend fun insertCoupon(coupon: CouponEntity): Long = withContext(Dispatchers.IO) {
        couponDao.insert(coupon)
    }

    suspend fun updateCoupon(coupon: CouponEntity) = withContext(Dispatchers.IO) {
        couponDao.update(coupon)
    }

    suspend fun deleteCoupon(id: Long) = withContext(Dispatchers.IO) {
        couponDao.deleteById(id)
    }

    // --- Reviews ---
    val allReviews: Flow<List<ReviewEntity>> = reviewDao.getAllReviews()
    val approvedReviews: Flow<List<ReviewEntity>> = reviewDao.getApprovedReviews()

    fun getApprovedReviewsForProduct(productId: Long): Flow<List<ReviewEntity>> =
        reviewDao.getApprovedReviewsForProduct(productId)

    suspend fun submitReview(productId: Long, customerName: String, rating: Int, reviewText: String): Long = withContext(Dispatchers.IO) {
        reviewDao.insert(
            ReviewEntity(
                productId = productId,
                customerId = 1,
                customerName = customerName,
                rating = rating,
                reviewText = reviewText,
                isApproved = true // auto-approved or pending based on owner policy
            )
        )
    }

    suspend fun updateReviewApproval(id: Long, isApproved: Boolean) = withContext(Dispatchers.IO) {
        reviewDao.updateApproval(id, isApproved)
    }

    suspend fun deleteReview(id: Long) = withContext(Dispatchers.IO) {
        reviewDao.deleteById(id)
    }

    // --- Banners ---
    val allBanners: Flow<List<BannerEntity>> = bannerDao.getAllBanners()
    val activeBanners: Flow<List<BannerEntity>> = bannerDao.getActiveBanners()

    suspend fun insertBanner(banner: BannerEntity): Long = withContext(Dispatchers.IO) {
        bannerDao.insert(banner)
    }

    suspend fun updateBanner(banner: BannerEntity) = withContext(Dispatchers.IO) {
        bannerDao.update(banner)
    }

    suspend fun deleteBanner(id: Long) = withContext(Dispatchers.IO) {
        bannerDao.deleteById(id)
    }

    // --- Store Settings ---
    val storeSettings: Flow<StoreSettingsEntity> = storeSettingsDao.getSettings().map { it ?: StoreSettingsEntity() }

    suspend fun updateStoreSettings(settings: StoreSettingsEntity) = withContext(Dispatchers.IO) {
        storeSettingsDao.insertOrUpdate(settings)
    }

    // --- Customer Profile ---
    val customerProfile: Flow<CustomerEntity?> = userDao.getCustomerByUserId(2)

    suspend fun updateCustomerProfile(customer: CustomerEntity) = withContext(Dispatchers.IO) {
        userDao.updateCustomer(customer)
    }
}
