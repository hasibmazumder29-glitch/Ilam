package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val passwordHash: String = "",
    val role: String = "CUSTOMER", // "CUSTOMER" or "ADMIN"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val district: String = "Dhaka",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val name: String = "",
    val roleTitle: String = "Store Owner",
    val permissions: String = "ALL"
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slug: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val imageUrl: String = "",
    val displayOrder: Int = 0,
    val isVisible: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subcategories")
data class SubcategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long = 0,
    val nameEn: String = "",
    val nameBn: String = "",
    val isVisible: Boolean = true
)

@Entity(tableName = "products", indices = [Index(value = ["categoryId"]), Index(value = ["sku"], unique = true)])
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long = 1,
    val subcategoryId: Long = 0,
    val sku: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val descEn: String = "",
    val descBn: String = "",
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val stock: Int = 10,
    val sizes: String = "M, L, XL", // Comma-separated sizes
    val colors: String = "Black, White", // Comma-separated colors
    val isNewArrival: Boolean = false,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isSale: Boolean = false,
    val isPublished: Boolean = true,
    val primaryImageUrl: String = "",
    val additionalImages: String = "", // Comma-separated image URLs or URIs
    val barcode: String = "", // UPC/EAN/Barcode string
    val brand: String = "ILAM Atelier",
    val purchasePrice: Double = 0.0, // Cost price (hidden from standard STAFF)
    val minStockAlert: Int = 3, // Minimum stock threshold before warning
    val supplier: String = "ILAM In-house Master Tailoring",
    val isDeleted: Boolean = false, // Soft-delete / archive flag
    val stockStatus: String = "IN_STOCK", // "IN_STOCK", "OUT_OF_STOCK", "LOW_STOCK"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isOutOfStock: Boolean
        get() = stock <= 0 || stockStatus == "OUT_OF_STOCK"

    val isLowStock: Boolean
        get() = !isOutOfStock && stock <= minStockAlert

    val effectiveSellingPrice: Double
        get() = if (discountPrice > 0.0) discountPrice else price

    val discountPercentage: Int
        get() = if (price > 0.0 && discountPrice > 0.0 && discountPrice < price) (((price - discountPrice) / price) * 100).toInt() else 0

    val effectiveStockStatus: String
        get() = when {
            isDeleted -> "DELETED"
            !isPublished -> "HIDDEN"
            stock <= 0 -> "OUT_OF_STOCK"
            stock <= minStockAlert -> "LOW_STOCK"
            else -> "IN_STOCK"
        }
}

/**
 * Dedicated inventory stock level entity for granular fashion inventory tracking
 * by product variant (size, color, location/rack/warehouse, reorder threshold).
 */
@Entity(
    tableName = "inventory_stocks",
    indices = [
        Index(value = ["productId"]),
        Index(value = ["productId", "size", "color"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class InventoryStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val size: String = "M",
    val color: String = "Default",
    val quantityOnHand: Int = 0,
    val reservedQuantity: Int = 0, // In active checkouts / reserved
    val reorderPoint: Int = 5,      // Low stock warning threshold
    val warehouseLocation: String = "Banani Main Flagship",
    val rackBinNumber: String = "RACK-A1",
    val lastRestockedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val availableStock: Int
        get() = (quantityOnHand - reservedQuantity).coerceAtLeast(0)

    val isLowStock: Boolean
        get() = availableStock <= reorderPoint
}

@Entity(tableName = "product_images")
data class ProductImageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long = 0,
    val imageUrl: String = "",
    val isPrimary: Boolean = false,
    val displayOrder: Int = 0
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long = 1,
    val productId: Long = 0,
    val quantity: Int = 1,
    val selectedSize: String = "M",
    val selectedColor: String = "Black",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist", indices = [Index(value = ["customerId", "productId"], unique = true)])
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long = 1,
    val productId: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders", indices = [Index(value = ["orderNumber"], unique = true)])
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String = "", // e.g. "ILAM-2026-1001"
    val customerId: Long = 1,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val customerDistrict: String = "Dhaka",
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val deliveryCharge: Double = 60.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "bKash", // "bKash", "Cash on Delivery"
    val paymentStatus: String = "Pending", // "Pending", "Paid", "Failed"
    val orderStatus: String = "Pending", // "Pending", "Confirmed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled", "Returned"
    val courierId: Long = 0,
    val courierName: String = "Steadfast Courier",
    val trackingNumber: String = "",
    val notes: String = "",
    val salesChannel: String = "ONLINE", // "ONLINE" or "OUTLET_POS"
    val soldByStaffName: String = "Online Checkout",
    val soldByStaffRole: String = "CUSTOMER", // "OWNER", "MANAGER", "STAFF"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long = 0,
    val productName: String = "",
    val productPrice: Double = 0.0,
    val quantity: Int = 1,
    val size: String = "M",
    val color: String = "Black"
)

@Entity(tableName = "couriers")
data class CourierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val deliveryCharge: Double = 60.0,
    val trackingUrlPattern: String = "",
    val apiUrl: String = "",
    val apiKey: String = "",
    val apiSecret: String = "",
    val isDefault: Boolean = false,
    val isEnabled: Boolean = true
)

@Entity(tableName = "coupons", indices = [Index(value = ["code"], unique = true)])
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String = "",
    val discountPercent: Double = 0.0, // e.g. 10.0 for 10%
    val fixedDiscount: Double = 0.0, // e.g. 200.0 for ৳200
    val minOrder: Double = 1000.0,
    val maxUsage: Int = 100,
    val usedCount: Int = 0,
    val expiryDate: String = "2026-12-31",
    val isActive: Boolean = true
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long = 0,
    val customerId: Long = 1,
    val customerName: String = "",
    val rating: Int = 5,
    val reviewText: String = "",
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String = "",
    val titleBn: String = "",
    val subtitleEn: String = "",
    val subtitleBn: String = "",
    val buttonTextEn: String = "Shop Collection",
    val buttonTextBn: String = "সংগ্রহ দেখুন",
    val imageUrl: String = "",
    val targetDestination: String = "all", // category slug or destination
    val isEnabled: Boolean = true,
    val displayOrder: Int = 0
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
    @PrimaryKey val id: Long = 1, // Singleton row
    val storeNameEn: String = "ILAM",
    val storeNameBn: String = "ইলাম",
    val logoUrl: String = "",
    val phone: String = "+880 1711-456789",
    val email: String = "contact@ilamfashion.com",
    val address: String = "Level 3, House 42, Road 11, Block D, Banani, Dhaka-1213",
    val facebookUrl: String = "https://facebook.com/ilam.bangladesh",
    val messengerUrl: String = "https://m.me/ilam.bangladesh",
    val whatsappNumber: String = "+8801711456789",
    val instagramUrl: String = "https://instagram.com/ilam_bangladesh",
    val defaultDeliveryCharge: Double = 60.0,
    val freeDeliveryThreshold: Double = 3000.0,
    val currencySymbol: String = "৳",
    val descriptionEn: String = "Haute Couture & Minimalist Luxury Fashion crafted in Bangladesh for the modern connoisseur.",
    val descriptionBn: String = "আধুনিক আভিজাত্য, আভিজাত্যপূর্ণ পোশাক ও নিখুঁত কারুকাজের সমাহার নিয়ে ইলাম লাক্সারি ফ্যাশন।",
    val bkashMerchantNumber: String = "01711456789",
    val bkashMockMode: Boolean = true
)
