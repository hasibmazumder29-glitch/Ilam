package com.example.data.database

import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabasePrepopulator {
    suspend fun populateInitialData(db: AppDatabase) = withContext(Dispatchers.IO) {
        val storeSettingsDao = db.storeSettingsDao()
        val categoryDao = db.categoryDao()
        val productDao = db.productDao()
        val courierDao = db.courierDao()
        val couponDao = db.couponDao()
        val bannerDao = db.bannerDao()
        val reviewDao = db.reviewDao()
        val orderDao = db.orderDao()
        val userDao = db.userDao()

        // 1. Store Settings
        storeSettingsDao.insertOrUpdate(
            StoreSettingsEntity(
                id = 1,
                storeNameEn = "ILAM",
                storeNameBn = "ইলাম",
                logoUrl = "",
                phone = "+880 1711-456789",
                email = "contact@ilamfashion.com",
                address = "Level 3, House 42, Road 11, Block D, Banani, Dhaka-1213",
                facebookUrl = "https://facebook.com/ilam.bangladesh",
                messengerUrl = "https://m.me/ilam.bangladesh",
                whatsappNumber = "+8801711456789",
                instagramUrl = "https://instagram.com/ilam_bangladesh",
                defaultDeliveryCharge = 60.0,
                freeDeliveryThreshold = 3000.0,
                currencySymbol = "৳",
                descriptionEn = "Haute Couture & Minimalist Luxury Fashion crafted in Bangladesh for the modern connoisseur.",
                descriptionBn = "আধুনিক আভিজাত্য, আভিজাত্যপূর্ণ পোশাক ও নিখুঁত কারুকাজের সমাহার নিয়ে ইলাম লাক্সারি ফ্যাশন।",
                bkashMerchantNumber = "01711456789",
                bkashMockMode = true
            )
        )

        // 2. Initial Users (Role-Based Permissions: OWNER, MANAGER, STAFF, CUSTOMER)
        val adminUserId = userDao.insertUser(
            UserEntity(
                id = 1,
                username = "owner",
                email = "owner@ilamfashion.com",
                phone = "+8801711456789",
                passwordHash = "ilam2026", // Owner Passcode
                role = "OWNER"
            )
        )

        userDao.insertUser(
            UserEntity(
                id = 3,
                username = "manager",
                email = "manager@ilamfashion.com",
                phone = "+8801722334455",
                passwordHash = "mgr2026", // Manager PIN
                role = "MANAGER"
            )
        )

        userDao.insertUser(
            UserEntity(
                id = 4,
                username = "staff",
                email = "staff@ilamfashion.com",
                phone = "+8801733445566",
                passwordHash = "staff123", // Staff Outlet PIN
                role = "STAFF"
            )
        )

        val customerUserId = userDao.insertUser(
            UserEntity(
                id = 2,
                username = "tanvir_ahmed",
                email = "tanvir@example.com",
                phone = "+8801812345678",
                passwordHash = "123456",
                role = "CUSTOMER"
            )
        )

        userDao.insertCustomer(
            CustomerEntity(
                id = 1,
                userId = customerUserId,
                fullName = "Tanvir Ahmed",
                phone = "+8801812345678",
                email = "tanvir@example.com",
                address = "Apartment 5B, Road 7, Sector 3, Uttara, Dhaka",
                district = "Dhaka"
            )
        )

        // 3. Categories
        val categories = listOf(
            CategoryEntity(
                id = 1,
                slug = "panjabi",
                nameEn = "Luxury Panjabi",
                nameBn = "অভিজাত পাঞ্জাবি",
                imageUrl = "panjabi",
                displayOrder = 1,
                isVisible = true
            ),
            CategoryEntity(
                id = 2,
                slug = "shirts",
                nameEn = "Tailored Shirts",
                nameBn = "টেইলর্ড শার্ট",
                imageUrl = "shirt",
                displayOrder = 2,
                isVisible = true
            ),
            CategoryEntity(
                id = 3,
                slug = "t-shirts",
                nameEn = "Polos & Tees",
                nameBn = "পোলো ও টি-শার্ট",
                imageUrl = "tshirt",
                displayOrder = 3,
                isVisible = true
            ),
            CategoryEntity(
                id = 4,
                slug = "pants",
                nameEn = "Pants & Trousers",
                nameBn = "প্যান্ট ও ট্রাউজার",
                imageUrl = "pants",
                displayOrder = 4,
                isVisible = true
            ),
            CategoryEntity(
                id = 5,
                slug = "women",
                nameEn = "Women's Couture",
                nameBn = "মহিলাদের বিশেষ পোশাক",
                imageUrl = "women",
                displayOrder = 5,
                isVisible = true
            ),
            CategoryEntity(
                id = 6,
                slug = "shoes",
                nameEn = "Artisanal Footwear",
                nameBn = "হ্যান্ডক্রাফটেড জুতো",
                imageUrl = "shoes",
                displayOrder = 6,
                isVisible = true
            ),
            CategoryEntity(
                id = 7,
                slug = "accessories",
                nameEn = "Watches & Accs",
                nameBn = "ঘড়ি ও এক্সেসরিজ",
                imageUrl = "watch",
                displayOrder = 7,
                isVisible = true
            )
        )
        categoryDao.insertAll(categories)

        // 4. Products
        val products = listOf(
            ProductEntity(
                id = 1,
                categoryId = 1,
                sku = "ILAM-PAN-01",
                nameEn = "ILAM Signature Noir Silk Panjabi",
                nameBn = "ইলাম সিগনেচার নোয়ার সিল্ক পাঞ্জাবি",
                descEn = "Exquisitely tailored from premium raw mulberry silk with hand-embroidered collar detailing. Features custom engraved metallic buttons and immaculate French seams for festive elegance.",
                descBn = "খাঁটি মালবেরি র সিল্ক কাপড়ে তৈরি ও কলার জুড়ে সূক্ষ্ম হাতের কাজ। উৎসবের আভিজাত্যের জন্য মেটালিক বাটনের মনোমুগ্ধকর নকশা।",
                price = 4500.0,
                discountPrice = 3950.0,
                stock = 18,
                sizes = "M, L, XL, XXL",
                colors = "Onyx Black, Slate Gray, Midnight Navy",
                isNewArrival = true,
                isFeatured = true,
                isBestSeller = true,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "panjabi",
                barcode = "890123456001",
                brand = "ILAM Royal Panjabi",
                purchasePrice = 2400.0,
                minStockAlert = 5,
                supplier = "Dhaka Silk Loom Artisans",
                stockStatus = "IN_STOCK"
            ),
            ProductEntity(
                id = 2,
                categoryId = 2,
                sku = "ILAM-SHT-02",
                nameEn = "Raw Linen Minimalist Overshirt",
                nameBn = "র লিনেন মিনিমালিস্ট ওভারশার্ট",
                descEn = "Crafted from breathable European 100% linen. Structured drop-shoulder cut with concealed placket and horn buttons. Effortlessly luxurious for casual and semi-formal wear.",
                descBn = "১০০% ইউরোপিয়ান প্রিমিয়াম লিনেনে তৈরি অত্যন্ত আরামদায়ক ও ড্রপ শোল্ডার কাটিং। ক্যাজুয়াল ও সেমি-ফরমাল স্টাইলের সেরা নির্বাচন।",
                price = 2950.0,
                discountPrice = 2550.0,
                stock = 14,
                sizes = "S, M, L, XL",
                colors = "Ivory Cream, Sand Stone, Jet Black",
                isNewArrival = true,
                isFeatured = true,
                isBestSeller = false,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "shirt",
                barcode = "890123456002",
                brand = "ILAM Contemporary",
                purchasePrice = 1450.0,
                minStockAlert = 4,
                supplier = "EuroLinen Imports",
                stockStatus = "IN_STOCK"
            ),
            ProductEntity(
                id = 3,
                categoryId = 1,
                sku = "ILAM-PAN-03",
                nameEn = "Artisanal Dobby Cotton Panjabi",
                nameBn = "আর্টিসানাল ডবি কটন পাঞ্জাবি",
                descEn = "Made from high-thread-count combed cotton with woven geometric dobby textures. Lightweight, breathable, and finished with satin neck piping.",
                descBn = "উচ্চমানের কম্বড সুতির ডবি উইভিং টেক্সচার। সারাদিন পরে থাকার জন্য আরামদায়ক ও আভিজাত্যপূর্ণ লুক।",
                price = 3200.0,
                discountPrice = 0.0,
                stock = 9,
                sizes = "M, L, XL",
                colors = "Pearl White, Champagne Beige",
                isNewArrival = true,
                isFeatured = true,
                isBestSeller = true,
                isSale = false,
                isPublished = true,
                primaryImageUrl = "panjabi"
            ),
            ProductEntity(
                id = 4,
                categoryId = 4,
                sku = "ILAM-TR-04",
                nameEn = "Tailored Pleated Noir Trouser",
                nameBn = "টেইলর্ড প্লিটেড নোয়ার ট্রাউজার",
                descEn = "Double front pleats with relaxed taper cut. Features side adjusters and custom inner waistband gripping for unyielding silhouette perfection.",
                descBn = "ডাবল প্লিটেড রিলাক্সড টেপার্ড ট্রাউজার। সাইড অ্যাডজাস্টার যুক্ত প্রিমিয়াম ফরমাল কাট।",
                price = 2400.0,
                discountPrice = 2100.0,
                stock = 16,
                sizes = "30, 32, 34, 36",
                colors = "Noir Black, Deep Charcoal, Olive Gray",
                isNewArrival = false,
                isFeatured = true,
                isBestSeller = true,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "pants"
            ),
            ProductEntity(
                id = 5,
                categoryId = 3,
                sku = "ILAM-POL-05",
                nameEn = "Supima Mercerized Luxe Polo",
                nameBn = "সুপিমা মার্সারাইজড লাক্সারি পোলো",
                descEn = "Silky smooth hand-feel with rich luster made of 60/2 long-staple Supima cotton. Unfading deep reactive dyes and mother-of-pearl buttons.",
                descBn = "দীর্ঘস্থায়ী কোমল ও মার্সারাইজড সুপিমা কটন দিয়ে তৈরি বিলাসবহুল পোলো শার্ট। মাাদার-অব-পার্ল বোতাম সংযুক্ত।",
                price = 1850.0,
                discountPrice = 0.0,
                stock = 25,
                sizes = "S, M, L, XL, XXL",
                colors = "Black Obsidian, Deep Olive, Pure Chalk",
                isNewArrival = true,
                isFeatured = false,
                isBestSeller = true,
                isSale = false,
                isPublished = true,
                primaryImageUrl = "tshirt"
            ),
            ProductEntity(
                id = 6,
                categoryId = 5,
                sku = "ILAM-WMN-06",
                nameEn = "Hand-Embroidered Velvet Festive Edit",
                nameBn = "হাতে জারদৌজি কাজ করা ভেলভেট উৎসব কুর্তি",
                descEn = "Micro velvet festive gown embellished with traditional zardozi craftsmanship in antique gold threads. Designed for premier evening galas.",
                descBn = "অ্যান্টিক গোল্ড সুতায় ঐতিহ্যবাহী জারদৌজি কাজের মাইক্রো ভেলভেট গাউন। বিয়ে বা রাতের রাজকীয় উৎসবের জন্য সেরা।",
                price = 8900.0,
                discountPrice = 7600.0,
                stock = 4, // Low stock product (< 5) for owner dashboard alert!
                sizes = "Free Size, M, L",
                colors = "Midnight Black, Royal Emerald",
                isNewArrival = true,
                isFeatured = true,
                isBestSeller = false,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "women"
            ),
            ProductEntity(
                id = 7,
                categoryId = 6,
                sku = "ILAM-SH-07",
                nameEn = "Handcrafted Italian Calfskin Loafer",
                nameBn = "হ্যান্ডক্রাফটেড ইতালিয়ান কাফস্কিন লোফার",
                descEn = "Goodyear welted construction using full-grain supple calfskin leather. Cushioned arch support insole and hand-painted burnished patina.",
                descBn = "ইতালিয়ান ফুল গ্রেইন কাফস্কিন চামড়ায় তৈরি আরামদায়ক হ্যান্ডক্রাফটেড লোফার। নিখুঁত ফিনিশিং ও ওয়াটার-রেজিস্ট্যান্ট সোল।",
                price = 5600.0,
                discountPrice = 4900.0,
                stock = 3, // Low stock product (< 5)
                sizes = "40, 41, 42, 43, 44",
                colors = "Burnished Black, Cognac Brown",
                isNewArrival = false,
                isFeatured = true,
                isBestSeller = true,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "shoes"
            ),
            ProductEntity(
                id = 8,
                categoryId = 7,
                sku = "ILAM-WAT-08",
                nameEn = "ILAM Minimalist Matte Chrono Watch",
                nameBn = "ইলাম মিনিমালিস্ট ম্যাট ক্রোনো ওয়াচ",
                descEn = "Surgical grade 316L stainless steel case in satin matte black. Sapphire crystal glass with 5ATM water resistance and genuine leather strap.",
                descBn = "স্যাফায়ার ক্রিস্টাল গ্লাস ও সার্জিকাল গ্রেড স্টেইনলেস স্টিল কেসে তৈরি আধুনিক ওয়াটারপ্রুফ লাক্সারি ঘড়ি।",
                price = 4200.0,
                discountPrice = 3600.0,
                stock = 7,
                sizes = "Standard Case 40mm",
                colors = "Matte Black, Gunmetal Silver",
                isNewArrival = false,
                isFeatured = true,
                isBestSeller = false,
                isSale = true,
                isPublished = true,
                primaryImageUrl = "watch"
            )
        )
        productDao.insertAll(products)

        // 5. Couriers (Unlimited Bangladesh Couriers)
        val couriers = listOf(
            CourierEntity(
                id = 1,
                name = "Steadfast Courier",
                deliveryCharge = 60.0,
                trackingUrlPattern = "https://steadfast.com.bd/t/{tracking_id}",
                apiUrl = "https://portal.steadfast.com.bd/api/v1",
                apiKey = "std_live_key_993821",
                apiSecret = "std_secret_vault_88329",
                isDefault = true,
                isEnabled = true
            ),
            CourierEntity(
                id = 2,
                name = "Pathao Courier",
                deliveryCharge = 70.0,
                trackingUrlPattern = "https://merchant.pathao.com/tracking/{tracking_id}",
                apiUrl = "https://api-hermes.pathao.com/v1",
                apiKey = "pathao_client_id_4429",
                apiSecret = "pathao_secret_token_1942",
                isDefault = false,
                isEnabled = true
            ),
            CourierEntity(
                id = 3,
                name = "RedX Logistics",
                deliveryCharge = 65.0,
                trackingUrlPattern = "https://redx.com.bd/track-order?trackingId={tracking_id}",
                apiUrl = "https://openapi.redx.com.bd/v1.0.0",
                apiKey = "redx_api_key_8841",
                apiSecret = "redx_sec_7721",
                isDefault = false,
                isEnabled = true
            ),
            CourierEntity(
                id = 4,
                name = "Paperfly Private Delivery",
                deliveryCharge = 60.0,
                trackingUrlPattern = "https://paperfly.com.bd/tracking.php?tracking_id={tracking_id}",
                apiUrl = "https://api.paperfly.com.bd/v1",
                apiKey = "",
                apiSecret = "",
                isDefault = false,
                isEnabled = true
            ),
            CourierEntity(
                id = 5,
                name = "Sundarban Courier Service",
                deliveryCharge = 100.0,
                trackingUrlPattern = "https://sundarbancourierltd.com/track/{tracking_id}",
                apiUrl = "",
                apiKey = "",
                apiSecret = "",
                isDefault = false,
                isEnabled = true
            )
        )
        courierDao.insertAll(couriers)

        // 6. Coupons
        val coupons = listOf(
            CouponEntity(
                id = 1,
                code = "ILAM10",
                discountPercent = 10.0,
                fixedDiscount = 0.0,
                minOrder = 1500.0,
                maxUsage = 500,
                usedCount = 24,
                expiryDate = "2026-12-31",
                isActive = true
            ),
            CouponEntity(
                id = 2,
                code = "WELCOME200",
                discountPercent = 0.0,
                fixedDiscount = 200.0,
                minOrder = 1000.0,
                maxUsage = 1000,
                usedCount = 68,
                expiryDate = "2026-12-31",
                isActive = true
            ),
            CouponEntity(
                id = 3,
                code = "EIDVIP",
                discountPercent = 15.0,
                fixedDiscount = 0.0,
                minOrder = 3500.0,
                maxUsage = 100,
                usedCount = 12,
                expiryDate = "2026-11-30",
                isActive = true
            )
        )
        couponDao.insertAll(coupons)

        // 7. Banners
        val banners = listOf(
            BannerEntity(
                id = 1,
                titleEn = "HAUTE COUTURE FESTIVE EDIT 2026",
                titleBn = "আভিজাত্যের নতুন উৎসব সংগ্রহ ২০২৬",
                subtitleEn = "Handcrafted pure mulberry silk & linen silhouettes tailored for supreme elegance.",
                subtitleBn = "বিশুদ্ধ মালবেরি সিল্ক ও প্রিমিয়াম লিনেনের শৈল্পিক রূপায়ণ।",
                buttonTextEn = "Explore Capsule",
                buttonTextBn = "সংগ্রহটি দেখুন",
                imageUrl = "banner_hero",
                targetDestination = "panjabi",
                isEnabled = true,
                displayOrder = 1
            ),
            BannerEntity(
                id = 2,
                titleEn = "THE MONOCHROME PANJABI",
                titleBn = "অনবদ্য মনোক্রোম পাঞ্জাবি",
                subtitleEn = "Signature minimalist cuts with understated hand embroidery.",
                subtitleBn = "মার্জিত হাতের কাজে সমৃদ্ধ মিনিমালিস্ট পাঞ্জাবি সম্ভার।",
                buttonTextEn = "Shop Panjabi",
                buttonTextBn = "পাঞ্জাবি কিনুন",
                imageUrl = "banner_panjabi",
                targetDestination = "panjabi",
                isEnabled = true,
                displayOrder = 2
            ),
            BannerEntity(
                id = 3,
                titleEn = "MODERN LUXURY ESSENTIALS",
                titleBn = "মডার্ন লাক্সারি এসেনশিয়ালস",
                subtitleEn = "Elevate daily sophistication with tailored overshirts and trousers.",
                subtitleBn = "নিত্যদিনের স্টাইলে নিয়ে আসুন আভিজাত্যের ছোঁয়া।",
                buttonTextEn = "View All",
                buttonTextBn = "সব দেখুন",
                imageUrl = "banner_menswear",
                targetDestination = "all",
                isEnabled = true,
                displayOrder = 3
            )
        )
        bannerDao.insertAll(banners)

        // 8. Reviews
        val reviews = listOf(
            ReviewEntity(
                id = 1,
                productId = 1,
                customerId = 1,
                customerName = "Tanvir Ahmed, Gulshan",
                rating = 5,
                reviewText = "Outstanding craftsmanship! The raw silk feels remarkably premium and the fit is top-notch. Truly Bangladesh's finest luxury brand packaging.",
                isApproved = true
            ),
            ReviewEntity(
                id = 2,
                productId = 1,
                customerId = 2,
                customerName = "Fahim Rahman, Dhanmondi",
                rating = 5,
                reviewText = "কাজের ফিনিশিং এবং ফেব্রিক কোয়ালিটি অতুলনীয়। ঈদের জন্য এমন অভিজাত পাঞ্জাবি সত্যিই বিরল।",
                isApproved = true
            ),
            ReviewEntity(
                id = 3,
                productId = 2,
                customerId = 3,
                customerName = "Shahriar Kabir, Banani",
                rating = 5,
                reviewText = "The linen overshirt has the perfect drape. Breathable in Dhaka's weather yet looks ultra sharp.",
                isApproved = true
            ),
            ReviewEntity(
                id = 4,
                productId = 4,
                customerId = 4,
                customerName = "Nafis Imtiaz, Uttara",
                rating = 4,
                reviewText = "Excellent tailored trouser fit. The side adjusters are genuine quality.",
                isApproved = true
            )
        )
        reviewDao.insertAll(reviews)

        // 9. Initial Orders (Demonstrating sales, tracking numbers, couriers)
        val order1Id = orderDao.insertOrder(
            OrderEntity(
                id = 1,
                orderNumber = "ILAM-2026-1001",
                customerId = 1,
                customerName = "Tanvir Ahmed",
                customerPhone = "+8801812345678",
                customerAddress = "House 24, Road 4, Sector 7, Uttara",
                customerDistrict = "Dhaka",
                subtotal = 3950.0,
                discount = 200.0,
                deliveryCharge = 0.0, // Free delivery (> 3000)
                totalAmount = 3750.0,
                paymentMethod = "bKash",
                paymentStatus = "Paid",
                orderStatus = "Delivered",
                courierId = 1,
                courierName = "Steadfast Courier",
                trackingNumber = "STD-8849201",
                notes = "Handover to security if unavailable"
            )
        )
        orderDao.insertOrderItems(
            listOf(
                OrderItemEntity(
                    id = 1,
                    orderId = order1Id,
                    productId = 1,
                    productName = "ILAM Signature Noir Silk Panjabi",
                    productPrice = 3950.0,
                    quantity = 1,
                    size = "L",
                    color = "Onyx Black"
                )
            )
        )

        val order2Id = orderDao.insertOrder(
            OrderEntity(
                id = 2,
                orderNumber = "ILAM-2026-1002",
                customerId = 1,
                customerName = "Zubair Hossain",
                customerPhone = "+8801712998877",
                customerAddress = "5th Floor, South Avenue, Gulshan 2",
                customerDistrict = "Dhaka",
                subtotal = 4650.0,
                discount = 465.0,
                deliveryCharge = 0.0,
                totalAmount = 4185.0,
                paymentMethod = "Cash on Delivery",
                paymentStatus = "Pending",
                orderStatus = "Processing",
                courierId = 2,
                courierName = "Pathao Courier",
                trackingNumber = "PTH-992810",
                notes = "Call before delivery"
            )
        )
        orderDao.insertOrderItems(
            listOf(
                OrderItemEntity(
                    id = 2,
                    orderId = order2Id,
                    productId = 2,
                    productName = "Raw Linen Minimalist Overshirt",
                    productPrice = 2550.0,
                    quantity = 1,
                    size = "M",
                    color = "Sand Stone"
                ),
                OrderItemEntity(
                    id = 3,
                    orderId = order2Id,
                    productId = 4,
                    productName = "Tailored Pleated Noir Trouser",
                    productPrice = 2100.0,
                    quantity = 1,
                    size = "32",
                    color = "Noir Black"
                )
            )
        )

        val order3Id = orderDao.insertOrder(
            OrderEntity(
                id = 3,
                orderNumber = "ILAM-2026-1003",
                customerId = 1,
                customerName = "Mahmudur Rahman",
                customerPhone = "+8801911223344",
                customerAddress = "Nasirabad Housing Society, Road 3",
                customerDistrict = "Chittagong",
                subtotal = 1850.0,
                discount = 0.0,
                deliveryCharge = 120.0,
                totalAmount = 1970.0,
                paymentMethod = "bKash",
                paymentStatus = "Paid",
                orderStatus = "Pending",
                courierId = 1,
                courierName = "Steadfast Courier",
                trackingNumber = "",
                notes = "Urgent dispatch requested"
            )
        )
        orderDao.insertOrderItems(
            listOf(
                OrderItemEntity(
                    id = 4,
                    orderId = order3Id,
                    productId = 5,
                    productName = "Supima Mercerized Luxe Polo",
                    productPrice = 1850.0,
                    quantity = 1,
                    size = "L",
                    color = "Black Obsidian"
                )
            )
        )

        // 10. Seed Granular Inventory Stocks for Fashion Products
        val inventoryDao = db.inventoryDao()
        val initialStocks = listOf(
            // Product 1: Emerald Heritage Silk Panjabi
            InventoryStockEntity(id = 1, productId = 1, size = "M", color = "Emerald Green", quantityOnHand = 12, reservedQuantity = 1, reorderPoint = 3, warehouseLocation = "Banani Flagship", rackBinNumber = "RACK-E1"),
            InventoryStockEntity(id = 2, productId = 1, size = "L", color = "Emerald Green", quantityOnHand = 8, reservedQuantity = 0, reorderPoint = 3, warehouseLocation = "Banani Flagship", rackBinNumber = "RACK-E2"),
            InventoryStockEntity(id = 3, productId = 1, size = "XL", color = "Emerald Green", quantityOnHand = 4, reservedQuantity = 0, reorderPoint = 3, warehouseLocation = "Banani Flagship", rackBinNumber = "RACK-E3"),

            // Product 2: Imperial Gold Embroidered Sherwani
            InventoryStockEntity(id = 4, productId = 2, size = "38R", color = "Imperial Gold", quantityOnHand = 3, reservedQuantity = 1, reorderPoint = 2, warehouseLocation = "Banani Vault", rackBinNumber = "VAULT-S1"),
            InventoryStockEntity(id = 5, productId = 2, size = "40R", color = "Imperial Gold", quantityOnHand = 5, reservedQuantity = 0, reorderPoint = 2, warehouseLocation = "Banani Vault", rackBinNumber = "VAULT-S2"),
            InventoryStockEntity(id = 6, productId = 2, size = "42R", color = "Imperial Gold", quantityOnHand = 2, reservedQuantity = 0, reorderPoint = 2, warehouseLocation = "Banani Vault", rackBinNumber = "VAULT-S3"),

            // Product 3: Noir Minimalist Linen Overshirt
            InventoryStockEntity(id = 7, productId = 3, size = "S", color = "Noir Black", quantityOnHand = 15, reservedQuantity = 0, reorderPoint = 4, warehouseLocation = "Gulshan Hub", rackBinNumber = "RACK-N1"),
            InventoryStockEntity(id = 8, productId = 3, size = "M", color = "Noir Black", quantityOnHand = 20, reservedQuantity = 2, reorderPoint = 5, warehouseLocation = "Gulshan Hub", rackBinNumber = "RACK-N2"),
            InventoryStockEntity(id = 9, productId = 3, size = "L", color = "Noir Black", quantityOnHand = 14, reservedQuantity = 1, reorderPoint = 4, warehouseLocation = "Gulshan Hub", rackBinNumber = "RACK-N3"),

            // Product 4: Crimson Jamdani Royal Sari
            InventoryStockEntity(id = 10, productId = 4, size = "Free Size", color = "Crimson Gold", quantityOnHand = 6, reservedQuantity = 1, reorderPoint = 2, warehouseLocation = "Banani Vault", rackBinNumber = "VAULT-J1"),

            // Product 5: Supima Mercerized Luxe Polo
            InventoryStockEntity(id = 11, productId = 5, size = "M", color = "Black Obsidian", quantityOnHand = 25, reservedQuantity = 2, reorderPoint = 6, warehouseLocation = "Uttara Depot", rackBinNumber = "RACK-P1"),
            InventoryStockEntity(id = 12, productId = 5, size = "L", color = "Black Obsidian", quantityOnHand = 18, reservedQuantity = 1, reorderPoint = 5, warehouseLocation = "Uttara Depot", rackBinNumber = "RACK-P2"),
            InventoryStockEntity(id = 13, productId = 5, size = "XL", color = "Black Obsidian", quantityOnHand = 3, reservedQuantity = 0, reorderPoint = 5, warehouseLocation = "Uttara Depot", rackBinNumber = "RACK-P3"), // Low Stock!

            // Product 6: Artisanal Hand-Burnished Leather Loafers
            InventoryStockEntity(id = 14, productId = 6, size = "40", color = "Hand-Burnished Cognac", quantityOnHand = 4, reservedQuantity = 0, reorderPoint = 2, warehouseLocation = "Banani Flagship", rackBinNumber = "FOOT-L1"),
            InventoryStockEntity(id = 15, productId = 6, size = "41", color = "Hand-Burnished Cognac", quantityOnHand = 6, reservedQuantity = 1, reorderPoint = 2, warehouseLocation = "Banani Flagship", rackBinNumber = "FOOT-L2"),
            InventoryStockEntity(id = 16, productId = 6, size = "42", color = "Hand-Burnished Cognac", quantityOnHand = 2, reservedQuantity = 0, reorderPoint = 2, warehouseLocation = "Banani Flagship", rackBinNumber = "FOOT-L3"), // Low Stock!

            // Product 7: Midnight Blue Slim Stretch Chino
            InventoryStockEntity(id = 17, productId = 7, size = "30", color = "Midnight Blue", quantityOnHand = 10, reservedQuantity = 0, reorderPoint = 3, warehouseLocation = "Gulshan Hub", rackBinNumber = "PANT-C1"),
            InventoryStockEntity(id = 18, productId = 7, size = "32", color = "Midnight Blue", quantityOnHand = 16, reservedQuantity = 1, reorderPoint = 4, warehouseLocation = "Gulshan Hub", rackBinNumber = "PANT-C2"),
            InventoryStockEntity(id = 19, productId = 7, size = "34", color = "Midnight Blue", quantityOnHand = 12, reservedQuantity = 0, reorderPoint = 3, warehouseLocation = "Gulshan Hub", rackBinNumber = "PANT-C3"),

            // Product 8: Gold Calligraphy Pure Silk Scarf
            InventoryStockEntity(id = 20, productId = 8, size = "One Size", color = "Onyx Gold", quantityOnHand = 15, reservedQuantity = 0, reorderPoint = 4, warehouseLocation = "Banani Flagship", rackBinNumber = "ACC-S1")
        )
        inventoryDao.insertAllStocks(initialStocks)
    }
}
