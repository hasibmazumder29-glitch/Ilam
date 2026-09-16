package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CartItemEntity
import com.example.data.entity.CourierEntity
import com.example.data.entity.ProductEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.ProductImagePlaceholder
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel

@Composable
fun CustomerCartCheckoutScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val rawCartItems by viewModel.rawCartItems.collectAsState()
    val publishedProducts by viewModel.publishedProducts.collectAsState()
    val enabledCouriers by viewModel.enabledCouriers.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponDiscount by viewModel.couponDiscount.collectAsState()
    val couponMessage by viewModel.couponMessage.collectAsState()

    // Match cart items with product models
    val cartPairs = remember(rawCartItems, publishedProducts) {
        rawCartItems.mapNotNull { cartItem ->
            val prod = publishedProducts.find { it.id == cartItem.productId }
            if (prod != null) cartItem to prod else null
        }
    }

    // Checkout Form state
    var customerName by remember { mutableStateOf("Tanvir Ahmed") }
    var customerPhone by remember { mutableStateOf("+880 1812-345678") }
    var customerAddress by remember { mutableStateOf("Apartment 5B, Road 7, Sector 3, Uttara") }
    var customerDistrict by remember { mutableStateOf("Dhaka") }
    var orderNotes by remember { mutableStateOf("") }
    var selectedCourierId by remember(enabledCouriers) {
        mutableStateOf(enabledCouriers.firstOrNull { it.isDefault }?.id ?: enabledCouriers.firstOrNull()?.id ?: 1L)
    }
    var paymentMethod by remember { mutableStateOf("bKash") } // "bKash" or "Cash on Delivery"
    var couponInput by remember { mutableStateOf("") }
    var isPlacingOrder by remember { mutableStateOf(false) }

    // Mock bKash dialog
    var showBkashModal by remember { mutableStateOf(false) }
    var bkashPhoneInput by remember { mutableStateOf("01812345678") }
    var bkashPinInput by remember { mutableStateOf("") }

    // Calculations
    val subtotal = remember(cartPairs) {
        cartPairs.sumOf { (item, prod) ->
            val price = if (prod.discountPrice > 0) prod.discountPrice else prod.price
            price * item.quantity
        }
    }

    val selectedCourier = remember(enabledCouriers, selectedCourierId) {
        enabledCouriers.find { it.id == selectedCourierId }
    }

    val isFreeDelivery = subtotal >= storeSettings.freeDeliveryThreshold
    val deliveryFee = if (isFreeDelivery) 0.0 else (selectedCourier?.deliveryCharge ?: storeSettings.defaultDeliveryCharge)
    val grandTotal = (subtotal - couponDiscount + deliveryFee).coerceAtLeast(0.0)

    if (cartPairs.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(IlamOffWhite)
                .testTag("empty_cart_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingBag,
                    contentDescription = null,
                    tint = IlamTextMuted,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = Strings.emptyBag(language),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (language == AppLanguage.BN) "আপনার শপিং ব্যাগে কোন পণ্য নেই।" else "Add pieces from our latest couture collection.",
                    color = IlamTextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("empty_cart_shop_button")
                ) {
                    Text(text = Strings.exploreCollections(language), fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .padding(horizontal = 16.dp)
            .testTag("customer_cart_checkout_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "${Strings.cart(language)} (${cartPairs.size})",
                color = IlamBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif
            )
        }

        // Cart Items List
        items(cartPairs, key = { it.first.id }) { (cartItem, product) ->
            CartItemCard(
                cartItem = cartItem,
                product = product,
                language = language,
                onQuantityChange = { newQty -> viewModel.updateCartQuantity(cartItem.id, newQty) },
                onRemove = { viewModel.removeFromCart(cartItem.id) }
            )
        }

        // Coupon Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "কুপন কোড (ILAM10 / WELCOME200)" else "Promotional Code (ILAM10 / WELCOME200)",
                        color = IlamBlack,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (appliedCoupon == null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = couponInput,
                                onValueChange = { couponInput = it.uppercase() },
                                placeholder = { Text("e.g. ILAM10", fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("coupon_input_field"),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    if (couponInput.isNotBlank()) {
                                        viewModel.applyCoupon(couponInput, subtotal)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IlamBlack),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("apply_coupon_button")
                            ) {
                                Text(text = Strings.apply(language), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Applied coupon chip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${appliedCoupon?.code}: -${formatBdt(couponDiscount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.BN) "মুছুন" else "Remove",
                                color = IlamCrimson,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .clickable { viewModel.removeCoupon() }
                                    .testTag("remove_coupon_button")
                            )
                        }
                    }

                    couponMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = msg,
                            color = if (appliedCoupon != null) Color(0xFF2E7D32) else IlamCrimson,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Delivery & Shipping Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = Strings.shippingInfo(language),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif
                    )

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(Strings.fullName(language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_name_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(Strings.phoneNumber(language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_phone_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text(Strings.deliveryAddress(language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkout_address_input"),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = customerDistrict,
                        onValueChange = { customerDistrict = it },
                        label = { Text(Strings.cityDistrict(language)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Courier Partner Selection (Dynamic from Database Couriers!)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = Strings.selectCourier(language),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    enabledCouriers.forEach { courier ->
                        val isSelected = selectedCourierId == courier.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) IlamSurfaceLight else IlamWhite,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) IlamBlack else IlamBorderLight),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedCourierId = courier.id }
                                .testTag("courier_option_${courier.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedCourierId = courier.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = IlamBlack)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = courier.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = IlamBlack
                                        )
                                        Text(
                                            text = if (isFreeDelivery) {
                                                if (language == AppLanguage.BN) "ফ্রি ডেলিভারি প্রযোজ্য" else "Complimentary Delivery"
                                            } else {
                                                "Standard Delivery: ${formatBdt(courier.deliveryCharge)}"
                                            },
                                            fontSize = 11.sp,
                                            color = if (isFreeDelivery) Color(0xFF2E7D32) else IlamTextSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = if (isFreeDelivery) Strings.free(language) else formatBdt(courier.deliveryCharge),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isFreeDelivery) Color(0xFF2E7D32) else IlamBlack
                                )
                            }
                        }
                    }
                }
            }
        }

        // Payment Method Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = Strings.paymentMethod(language),
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // bKash Option
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (paymentMethod == "bKash") Color(0xFFFCE4EC) else IlamWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (paymentMethod == "bKash") IlamBkashPink else IlamBorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { paymentMethod = "bKash" }
                            .testTag("payment_method_bkash")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "bKash",
                                onClick = { paymentMethod = "bKash" },
                                colors = RadioButtonDefaults.colors(selectedColor = IlamBkashPink)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = Strings.bkashPayment(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = IlamBkashPink
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "বিকাশ অ্যাপ/সিমুলেটেড গেটওয়ে দিয়ে সরাসরি পেমেন্ট" else "Official bKash Digital Gateway & Instant Verification",
                                    fontSize = 11.sp,
                                    color = IlamTextSecondary
                                )
                            }
                        }
                    }

                    // Cash on Delivery Option
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (paymentMethod == "Cash on Delivery") IlamSurfaceLight else IlamWhite,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (paymentMethod == "Cash on Delivery") IlamBlack else IlamBorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { paymentMethod = "Cash on Delivery" }
                            .testTag("payment_method_cod")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = paymentMethod == "Cash on Delivery",
                                onClick = { paymentMethod = "Cash on Delivery" },
                                colors = RadioButtonDefaults.colors(selectedColor = IlamBlack)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = Strings.cashOnDelivery(language),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = IlamBlack
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "পণ্য হাতে পেয়ে মূল্য পরিশোধ করুন" else "Pay in cash upon receiving package at doorstep",
                                    fontSize = 11.sp,
                                    color = IlamTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Order Summary Calculation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamBlack)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.orderSummary(language),
                        color = IlamWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Strings.subtotal(language), color = IlamTextMuted, fontSize = 13.sp)
                        Text(text = formatBdt(subtotal), color = IlamWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    if (couponDiscount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = Strings.discount(language), color = IlamGold, fontSize = 13.sp)
                            Text(text = "-${formatBdt(couponDiscount)}", color = IlamGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = Strings.deliveryFee(language), color = IlamTextMuted, fontSize = 13.sp)
                        Text(
                            text = if (isFreeDelivery) Strings.free(language) else formatBdt(deliveryFee),
                            color = if (isFreeDelivery) Color(0xFF81C784) else IlamWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Divider(color = Color(0xFF333333), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = Strings.total(language), color = IlamWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = formatBdt(grandTotal), color = IlamGoldLight, fontWeight = FontWeight.Black, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final Place Order Button
                    Button(
                        onClick = {
                            if (paymentMethod == "bKash") {
                                showBkashModal = true
                            } else {
                                isPlacingOrder = true
                                viewModel.placeOrder(
                                    customerName = customerName,
                                    customerPhone = customerPhone,
                                    customerAddress = customerAddress,
                                    customerDistrict = customerDistrict,
                                    cartPairs = cartPairs,
                                    subtotal = subtotal,
                                    deliveryCharge = deliveryFee,
                                    paymentMethod = paymentMethod,
                                    courierId = selectedCourier?.id ?: 1L,
                                    courierName = selectedCourier?.name ?: "Steadfast Courier",
                                    notes = orderNotes,
                                    onSuccess = { order ->
                                        isPlacingOrder = false
                                        viewModel.selectOrder(order)
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (paymentMethod == "bKash") IlamBkashPink else IlamWhite,
                            contentColor = if (paymentMethod == "bKash") IlamWhite else IlamBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("confirm_place_order_button")
                    ) {
                        Text(
                            text = if (paymentMethod == "bKash") {
                                if (language == AppLanguage.BN) "বিকাশ পেমেন্ট ও অর্ডার নিশ্চিত করুন" else "Pay with bKash & Confirm Order"
                            } else {
                                Strings.placeOrder(language)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    // bKash Checkout Gateway Simulation Dialog (Safe test/mock payment mode prepared for official bKash API)
    if (showBkashModal) {
        AlertDialog(
            onDismissRequest = { showBkashModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IlamBkashPink),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "৳", color = IlamWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "bKash Payment Gateway", fontWeight = FontWeight.Bold, color = IlamBlack)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Merchant: ${storeSettings.storeNameEn} (${storeSettings.bkashMerchantNumber})",
                        fontSize = 12.sp,
                        color = IlamTextSecondary
                    )
                    Text(
                        text = "Payable Amount: ${formatBdt(grandTotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IlamBkashPink
                    )

                    OutlinedTextField(
                        value = bkashPhoneInput,
                        onValueChange = { bkashPhoneInput = it },
                        label = { Text("bKash Account Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bkashPinInput,
                        onValueChange = { bkashPinInput = it },
                        label = { Text("PIN / Verification Code") },
                        placeholder = { Text("Enter 1234 for test") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Surface(
                        color = Color(0xFFFCE4EC),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Test Mode: You can enter any 4-digit code (e.g. 1234) to simulate instant bKash payment verification.",
                            color = IlamBkashPink,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBkashModal = false
                        viewModel.placeOrder(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            customerAddress = customerAddress,
                            customerDistrict = customerDistrict,
                            cartPairs = cartPairs,
                            subtotal = subtotal,
                            deliveryCharge = deliveryFee,
                            paymentMethod = "bKash",
                            courierId = selectedCourier?.id ?: 1L,
                            courierName = selectedCourier?.name ?: "Steadfast Courier",
                            notes = orderNotes,
                            onSuccess = { order ->
                                viewModel.selectOrder(order)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IlamBkashPink)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBkashModal = false }) {
                    Text("Cancel", color = IlamTextSecondary)
                }
            },
            containerColor = IlamWhite
        )
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItemEntity,
    product: ProductEntity,
    language: AppLanguage,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val productName = if (language == AppLanguage.BN && product.nameBn.isNotBlank()) product.nameBn else product.nameEn
    val price = if (product.discountPrice > 0) product.discountPrice else product.price

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_card_${cartItem.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = IlamWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IlamSurfaceLight)
            ) {
                ProductImagePlaceholder(
                    categorySlug = product.primaryImageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    color = IlamBlack,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Size: ${cartItem.selectedSize} • Color: ${cartItem.selectedColor}",
                    color = IlamTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = formatBdt(price),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Quantity stepper & delete
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("cart_remove_button_${cartItem.id}")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = IlamTextMuted, modifier = Modifier.size(16.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(IlamSurfaceLight, RoundedCornerShape(6.dp))
                ) {
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(12.dp))
                    }
                    Text(
                        text = cartItem.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(cartItem.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}
