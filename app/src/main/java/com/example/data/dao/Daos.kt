package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Long): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductByIdSync(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE isPublished = 1 AND categoryId = :categoryId ORDER BY id DESC")
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 AND isNewArrival = 1 ORDER BY id DESC")
    fun getNewArrivals(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 AND isFeatured = 1 ORDER BY id DESC")
    fun getFeaturedProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 AND isBestSeller = 1 ORDER BY id DESC")
    fun getBestSellers(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isPublished = 1 AND (isSale = 1 OR discountPrice > 0) ORDER BY id DESC")
    fun getSaleProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE stock <= :threshold ORDER BY stock ASC")
    fun getLowStockProducts(threshold: Int = 5): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE products SET isPublished = :isPublished WHERE id = :id")
    suspend fun setPublished(id: Long, isPublished: Boolean)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC, id ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isVisible = 1 ORDER BY displayOrder ASC, id ASC")
    fun getVisibleCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY id DESC")
    fun getOrdersByCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Flow<OrderEntity?>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItemsSync(orderId: Long): List<OrderItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String)

    @Query("UPDATE orders SET courierId = :courierId, courierName = :courierName, trackingNumber = :trackingNumber WHERE id = :orderId")
    suspend fun assignCourier(orderId: Long, courierId: Long, courierName: String, trackingNumber: String)

    @Query("UPDATE orders SET paymentStatus = :paymentStatus WHERE id = :orderId")
    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: String)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getCartItems(customerId: Long): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity)

    @Update
    suspend fun updateCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun clearCart(customerId: Long)
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist WHERE customerId = :customerId ORDER BY addedAt DESC")
    fun getWishlist(customerId: Long): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist WHERE customerId = :customerId AND productId = :productId")
    suspend fun deleteWishlist(customerId: Long, productId: Long)

    @Query("SELECT COUNT(*) > 0 FROM wishlist WHERE customerId = :customerId AND productId = :productId")
    fun isInWishlist(customerId: Long, productId: Long): Flow<Boolean>
}

@Dao
interface CourierDao {
    @Query("SELECT * FROM couriers ORDER BY isDefault DESC, id ASC")
    fun getAllCouriers(): Flow<List<CourierEntity>>

    @Query("SELECT * FROM couriers WHERE isEnabled = 1 ORDER BY isDefault DESC, id ASC")
    fun getEnabledCouriers(): Flow<List<CourierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(courier: CourierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(couriers: List<CourierEntity>)

    @Update
    suspend fun update(courier: CourierEntity)

    @Delete
    suspend fun delete(courier: CourierEntity)

    @Query("DELETE FROM couriers WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE couriers SET isDefault = CASE WHEN id = :defaultId THEN 1 ELSE 0 END")
    suspend fun setDefaultCourier(defaultId: Long)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons ORDER BY id DESC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coupon: CouponEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coupons: List<CouponEntity>)

    @Update
    suspend fun update(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE coupons SET usedCount = usedCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews ORDER BY id DESC")
    fun getAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE isApproved = 1 ORDER BY id DESC")
    fun getApprovedReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE productId = :productId AND isApproved = 1 ORDER BY id DESC")
    fun getApprovedReviewsForProduct(productId: Long): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(review: ReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<ReviewEntity>)

    @Query("UPDATE reviews SET isApproved = :isApproved WHERE id = :id")
    suspend fun updateApproval(id: Long, isApproved: Boolean)

    @Query("DELETE FROM reviews WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners ORDER BY displayOrder ASC, id ASC")
    fun getAllBanners(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners WHERE isEnabled = 1 ORDER BY displayOrder ASC, id ASC")
    fun getActiveBanners(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(banner: BannerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banners: List<BannerEntity>)

    @Update
    suspend fun update(banner: BannerEntity)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface StoreSettingsDao {
    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<StoreSettingsEntity?>

    @Query("SELECT * FROM store_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): StoreSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: StoreSettingsEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM customers WHERE userId = :userId LIMIT 1")
    fun getCustomerByUserId(userId: Long): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
}
