package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.database.DatabasePrepopulator
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.repository.CouponResult
import com.example.data.repository.IlamRepository
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: IlamRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = IlamRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read string from context matches ILAM brand name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ILAM", appName)
    }

    @Test
    fun `test bilingual localization strings`() {
        val brandEn = Strings.brandName(AppLanguage.EN)
        val brandBn = Strings.brandName(AppLanguage.BN)
        assertEquals("ILAM", brandEn)
        assertEquals("ইলাম", brandBn)

        val cartEn = Strings.cart(AppLanguage.EN)
        val cartBn = Strings.cart(AppLanguage.BN)
        assertEquals("Bag", cartEn)
        assertEquals("ব্যাগ", cartBn)
    }

    @Test
    fun `test database operations and seed prepopulation`() = runBlocking {
        DatabasePrepopulator.populateInitialData(db)

        val categories = repository.allCategories.first()
        assertTrue("Categories should be seeded", categories.isNotEmpty())

        val products = repository.allProducts.first()
        assertTrue("Products should be seeded", products.isNotEmpty())

        val couriers = repository.allCouriers.first()
        assertTrue("Couriers should be seeded", couriers.isNotEmpty())

        val coupons = repository.allCoupons.first()
        assertTrue("Coupons should be seeded", coupons.isNotEmpty())

        // Test Coupon Validation
        val validCoupon = repository.validateCoupon("ILAM10", 2500.0)
        assertTrue("ILAM10 should succeed on 2500 order", validCoupon is CouponResult.Success)

        val invalidCoupon = repository.validateCoupon("NONEXISTENT", 2500.0)
        assertTrue("Unknown coupon should fail", invalidCoupon is CouponResult.Error)
    }

    @Test
    fun `test product CRUD operations`() = runBlocking {
        val testCategory = CategoryEntity(nameEn = "Test Category", nameBn = "টেস্ট", slug = "test-cat", imageUrl = "")
        val catId = repository.insertCategory(testCategory)

        val newProduct = ProductEntity(
            categoryId = catId,
            sku = "ILAM-TEST-001",
            nameEn = "Luxury Silk Kurti",
            nameBn = "লাক্সারি সিল্ক কুর্তি",
            descEn = "Pure mulberry silk handcrafted kurti.",
            descBn = "খাঁটি তুঁত রেশমের তৈরি কুর্তি।",
            price = 4500.0,
            stock = 8
        )
        val prodId = repository.insertProduct(newProduct)
        assertTrue(prodId > 0)

        val fetched = repository.getProductById(prodId).first()
        assertNotNull(fetched)
        assertEquals("Luxury Silk Kurti", fetched?.nameEn)
        assertEquals(4500.0, fetched?.price ?: 0.0, 0.01)

        // Update product
        val updatedProduct = fetched!!.copy(stock = 15, price = 4800.0)
        repository.updateProduct(updatedProduct)

        val updatedFetched = repository.getProductById(prodId).first()
        assertEquals(15, updatedFetched?.stock)
        assertEquals(4800.0, updatedFetched?.price ?: 0.0, 0.01)

        // Delete product (Soft delete archive to preserve sales records)
        repository.deleteProduct(prodId)
        val activeAfterDelete = repository.activeProducts.first()
        assertFalse("Archived product should disappear from active catalog", activeAfterDelete.any { it.id == prodId })
        val archived = repository.getProductById(prodId).first()
        assertNotNull(archived)
        assertTrue("isDeleted flag should be set to true", archived!!.isDeleted)

        // Permanent deletion test
        repository.deleteProductPermanently(prodId)
        val afterHardDelete = repository.getProductById(prodId).first()
        assertNull(afterHardDelete)
    }

    @Test
    fun `test localized loading and authentication strings`() {
        val loadingEn = Strings.loading(AppLanguage.EN)
        val loadingBn = Strings.loading(AppLanguage.BN)
        assertEquals("Loading...", loadingEn)
        assertEquals("লোড হচ্ছে...", loadingBn)

        val authEn = Strings.authenticating(AppLanguage.EN)
        val authBn = Strings.authenticating(AppLanguage.BN)
        assertEquals("Verifying Security Credentials...", authEn)
        assertEquals("নিরাপত্তা পাসকোড যাচাই করা হচ্ছে...", authBn)

        val pleaseWaitEn = Strings.pleaseWait(AppLanguage.EN)
        val pleaseWaitBn = Strings.pleaseWait(AppLanguage.BN)
        assertEquals("Please wait a moment", pleaseWaitEn)
        assertEquals("অনুগ্রহ করে অপেক্ষা করুন", pleaseWaitBn)
    }

    @Test
    fun `test ViewModel global loading state lifecycle`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.IlamViewModel(app)

        assertFalse("Initially should not be loading", vm.isLoading.value)
        assertNull(vm.loadingMessage.value)

        // Trigger loading
        vm.showLoading("Verifying Credentials...", "Security Check")
        assertTrue("Should be loading", vm.isLoading.value)
        assertEquals("Verifying Credentials...", vm.loadingMessage.value)
        assertEquals("Security Check", vm.loadingSubtitle.value)

        // Dismiss loading
        vm.hideLoading()
        assertFalse("Should no longer be loading", vm.isLoading.value)
        assertNull(vm.loadingMessage.value)

        // withLoading helper test
        var executed = false
        val result = vm.withLoading("Processing...", delayMs = 10L) {
            executed = true
            42
        }
        assertTrue("Block should have executed", executed)
        assertEquals(42, result)
        assertFalse("Should be reset to false after withLoading", vm.isLoading.value)
    }

    @Test
    fun `test authentication verification flow`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.IlamViewModel(app)

        // Correct PIN
        var authSuccess: Boolean? = null
        vm.unlockAdminWithVerification("ilam2026", delayMs = 0L) { success ->
            authSuccess = success
        }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(true, authSuccess)
        assertTrue(vm.isAdminUnlocked.value)
        assertEquals(com.example.ui.viewmodel.AppDestination.ADMIN_PORTAL, vm.destination.value)

        // Lock again
        vm.lockAdmin()
        assertFalse(vm.isAdminUnlocked.value)

        // Wrong PIN
        var wrongPinSuccess: Boolean? = null
        vm.unlockAdminWithVerification("wrong_pin", delayMs = 0L) { success ->
            wrongPinSuccess = success
        }
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(false, wrongPinSuccess)
        assertFalse(vm.isAdminUnlocked.value)
    }

    @Test
    fun `test fashion inventory stock schema and operations`() = runBlocking {
        // Seed initial data
        DatabasePrepopulator.populateInitialData(db)

        val inventoryStocks = repository.allInventoryStocks.first()
        assertTrue("Inventory stocks should be seeded", inventoryStocks.isNotEmpty())

        // Check product 1 variant stock levels
        val prod1Stocks = repository.getStocksByProduct(1L).first()
        assertTrue("Product 1 should have multiple size/color variants", prod1Stocks.size >= 3)
        assertTrue(prod1Stocks.any { it.size == "M" })
        assertTrue(prod1Stocks.any { it.size == "L" })

        // Check low stock query
        val lowStockVariants = repository.lowStockAlerts.first()
        assertTrue("Should detect variants with low stock", lowStockVariants.isNotEmpty())

        // Test insertion of new fashion stock level variant
        val newStock = com.example.data.entity.InventoryStockEntity(
            productId = 1L,
            size = "XXL",
            color = "Emerald Gold Trim",
            quantityOnHand = 15,
            reservedQuantity = 2,
            reorderPoint = 4,
            warehouseLocation = "Gulshan Atelier",
            rackBinNumber = "RACK-E4"
        )
        val stockId = repository.insertStock(newStock)
        assertTrue(stockId > 0)

        assertEquals(13, newStock.availableStock)
        assertFalse(newStock.isLowStock)

        // Restock variant operation
        repository.restockVariant(stockId, 10)
        val updatedStocks = repository.getStocksByProduct(1L).first()
        val restocked = updatedStocks.find { it.id == stockId }
        assertNotNull(restocked)
        assertEquals(25, restocked?.quantityOnHand)
        assertEquals(23, restocked?.availableStock)

        // Delete stock variant
        repository.deleteStock(stockId)
        val afterDelete = repository.getStocksByProduct(1L).first()
        assertFalse(afterDelete.any { it.id == stockId })
    }

    @Test
    fun `test complete owner and staff QR scanning and POS sale lifecycle`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.ui.viewmodel.IlamViewModel(app)

        // Step 1. OWNER: Login as OWNER
        assertTrue("Owner login with PIN should succeed", vm.unlockAdmin("ilam2026"))
        assertEquals(com.example.ui.viewmodel.UserRole.OWNER, vm.currentUserRole.value)
        assertTrue(vm.currentUserRole.value.canDeleteProduct)
        assertTrue(vm.currentUserRole.value.canChangeSellingPrice)
        assertTrue(vm.currentUserRole.value.canViewCostPrice)

        // Step 2. OWNER: Create product with photo, price, size/color, stock
        val testSku = "ILAM-QR-TEST-001"
        val product = ProductEntity(
            sku = testSku,
            barcode = "8901234567890",
            nameEn = "Royal Zari Panjabi",
            nameBn = "রয়্যাল জরি পাঞ্জাবি",
            price = 5000.0,
            discountPrice = 4500.0,
            purchasePrice = 2500.0,
            stock = 2,
            minStockAlert = 1,
            sizes = "M, L, XL",
            colors = "Regal Gold, Midnight Navy",
            primaryImageUrl = "men_panjabi_luxe",
            isPublished = true
        )
        val productId = repository.insertProduct(product)
        assertTrue("Product should be saved with ID > 0", productId > 0)

        val saved = repository.findProductBySkuOrBarcode(testSku)
        assertNotNull("Product should be saved in DB", saved)
        assertEquals(2, saved!!.stock)
        assertEquals(4500.0, saved.effectiveSellingPrice, 0.01)

        // Step 3. OWNER: Generate QR Code (deterministic QR matrix contains SKU identifier only)
        val qrBitmap = com.example.ui.admin.generateQrBitmap(testSku)
        assertNotNull("QR bitmap should be generated", qrBitmap)
        assertEquals(256, qrBitmap.width)
        assertEquals(256, qrBitmap.height)

        // Step 4. STAFF: Login as STAFF
        vm.unlockAdmin("staff123")
        assertEquals(com.example.ui.viewmodel.UserRole.STAFF, vm.currentUserRole.value)
        assertFalse("Staff cannot delete products", vm.currentUserRole.value.canDeleteProduct)
        assertFalse("Staff cannot change selling price", vm.currentUserRole.value.canChangeSellingPrice)
        assertFalse("Staff cannot change purchase price", vm.currentUserRole.value.canChangePurchasePrice)

        // Step 5. STAFF: Scan QR / Barcode -> Product appears, view price
        val foundProduct = repository.findProductBySkuOrBarcode(testSku)
        assertNotNull("Scanning QR SKU should find the exact product", foundProduct)
        assertEquals("Royal Zari Panjabi", foundProduct?.nameEn)
        assertEquals(4500.0, foundProduct?.effectiveSellingPrice ?: 0.0, 0.01)

        // Step 6. STAFF: Sell Product (1 unit) -> stock decreases
        val posSaleOrderId = repository.createPosSale(
            product = foundProduct!!,
            quantity = 1,
            size = "L",
            color = "Regal Gold",
            unitPrice = 4500.0,
            staffName = "Outlet Sales Associate",
            staffRole = "STAFF",
            customerName = "Mr. Shakib",
            customerPhone = "+880 1711-223344",
            paymentMethod = "Cash at Counter"
        )
        assertTrue("POS sale order ID should be valid", posSaleOrderId > 0)

        // Verify sale record & stock decrease
        val updatedAfterSale1 = repository.findProductBySkuOrBarcode(testSku)
        assertEquals("Stock should decrease from 2 to 1", 1, updatedAfterSale1?.stock)
        assertEquals("LOW_STOCK", updatedAfterSale1?.stockStatus)

        // Step 7. Complete another sale so stock reaches 0 -> shows OUT OF STOCK
        repository.createPosSale(
            product = updatedAfterSale1!!,
            quantity = 1,
            size = "M",
            color = "Regal Gold",
            unitPrice = 4500.0,
            staffName = "Outlet Sales Associate",
            staffRole = "STAFF",
            customerName = "Walk-in Guest",
            customerPhone = "+880 1700-000000",
            paymentMethod = "bKash POS"
        )

        val outOfStockProduct = repository.findProductBySkuOrBarcode(testSku)
        assertEquals("Stock should reach 0", 0, outOfStockProduct?.stock)
        assertEquals("OUT_OF_STOCK", outOfStockProduct?.stockStatus)
        assertTrue("Product isOutOfStock should be true", outOfStockProduct!!.isOutOfStock)

        // Step 8. OWNER: Edit price -> New price appears after scanning the same QR
        vm.unlockAdmin("ilam2026")
        val editedProduct = outOfStockProduct.copy(
            price = 6000.0,
            discountPrice = 5500.0,
            stock = 5,
            stockStatus = "IN_STOCK"
        )
        repository.updateProduct(editedProduct)

        // Staff scans the same QR code again -> new price appears
        val scannedAgain = repository.findProductBySkuOrBarcode(testSku)
        assertEquals("Scanned product should reflect new price", 5500.0, scannedAgain?.effectiveSellingPrice ?: 0.0, 0.01)
        assertEquals("Scanned product should reflect restocked count", 5, scannedAgain?.stock)

        // Step 9. OWNER: Delete/archive product -> Disappears from active products, historical sales remain intact
        val totalOrdersBeforeArchive = repository.allOrders.first().size
        assertTrue("Should have at least 2 historical sales recorded", totalOrdersBeforeArchive >= 2)

        repository.archiveProduct(productId)
        val activeProducts = repository.activeProducts.first()
        assertFalse("Archived product must disappear from active catalog", activeProducts.any { it.id == productId })

        // Historical sales remain intact
        val ordersAfterArchive = repository.allOrders.first()
        assertEquals("Historical sales count must not decrease when product is deleted", totalOrdersBeforeArchive, ordersAfterArchive.size)
        val posSales = ordersAfterArchive.filter { it.salesChannel == "OUTLET_POS" }
        assertEquals(2, posSales.size)
        assertTrue("Historical order details must be intact", posSales.any { it.soldByStaffRole == "STAFF" })
    }
}
