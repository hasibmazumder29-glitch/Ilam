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

        // Delete product
        repository.deleteProduct(prodId)
        val afterDelete = repository.getProductById(prodId).first()
        assertNull(afterDelete)
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
}
