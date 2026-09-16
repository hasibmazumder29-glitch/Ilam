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
import com.example.data.entity.OrderEntity
import com.example.data.entity.OrderItemEntity
import com.example.ui.common.AppLanguage
import com.example.ui.common.Strings
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.formatBdt
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.IlamViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomerOrdersScreen(
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val orders by viewModel.customerOrders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .padding(horizontal = 16.dp)
            .testTag("customer_orders_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = Strings.orders(language),
            color = IlamBlack,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            fontFamily = FontFamily.Serif
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.LocalShipping,
                        contentDescription = null,
                        tint = IlamTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "আপনার কোন পূর্বের অর্ডার নেই" else "No orders placed yet",
                        color = IlamBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_SHOP) },
                        colors = ButtonDefaults.buttonColors(containerColor = IlamBlack)
                    ) {
                        Text(text = Strings.exploreCollections(language))
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    CustomerOrderCard(
                        order = order,
                        language = language,
                        onClick = { viewModel.selectOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: OrderEntity,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val dateStr = remember(order.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(order.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("order_card_${order.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = IlamWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = IlamBlack
                )
                OrderStatusBadge(status = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = dateStr,
                color = IlamTextMuted,
                fontSize = 11.sp
            )

            Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${order.courierName} ${if (order.trackingNumber.isNotBlank()) "• ${order.trackingNumber}" else ""}",
                        fontSize = 11.sp,
                        color = IlamTextSecondary
                    )
                    Text(
                        text = "Payment: ${order.paymentMethod} (${order.paymentStatus})",
                        fontSize = 11.sp,
                        color = if (order.paymentStatus == "Paid") Color(0xFF2E7D32) else IlamGoldDark,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = formatBdt(order.totalAmount),
                    color = IlamBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun CustomerOrderDetailScreen(
    order: OrderEntity,
    viewModel: IlamViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val items by viewModel.selectedOrderItems.collectAsState()

    val dateStr = remember(order.createdAt) {
        SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.US).format(Date(order.createdAt))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IlamOffWhite)
            .padding(horizontal = 16.dp)
            .testTag("order_detail_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Navigation
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppDestination.CUSTOMER_ORDERS) },
                    modifier = Modifier.testTag("order_detail_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = IlamBlack)
                }
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    color = IlamBlack
                )
            }
        }

        // Tracking Timeline Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.orderStatus(language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = IlamBlack
                        )
                        OrderStatusBadge(status = order.orderStatus)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Step Tracker Visual
                    val steps = listOf("Pending", "Processing", "Shipped", "Delivered")
                    val currentStepIndex = when (order.orderStatus) {
                        "Pending" -> 0
                        "Confirmed", "Processing", "Packed" -> 1
                        "Shipped", "Out for Delivery" -> 2
                        "Delivered" -> 3
                        else -> 0
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, stepName ->
                            val isCompleted = index <= currentStepIndex
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isCompleted) IlamGoldDark else IlamSurfaceLight)
                                        .border(1.dp, if (isCompleted) IlamGold else IlamBorderLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = IlamWhite, modifier = Modifier.size(14.dp))
                                    } else {
                                        Text(text = "${index + 1}", fontSize = 10.sp, color = IlamTextMuted)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stepName,
                                    fontSize = 10.sp,
                                    color = if (isCompleted) IlamBlack else IlamTextMuted,
                                    fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    if (order.trackingNumber.isNotBlank()) {
                        Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${Strings.courierAssigned(language)}: ${order.courierName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${Strings.trackingNumber(language)}: ${order.trackingNumber}", fontSize = 12.sp, color = IlamGoldDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Ordered Items Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "অর্ডারের পণ্যসমূহ" else "Order Items",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Serif,
                        color = IlamBlack
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.productName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = IlamBlack)
                                Text(text = "Size: ${item.size} • Color: ${item.color} • Qty: ${item.quantity}", fontSize = 11.sp, color = IlamTextSecondary)
                            }
                            Text(text = formatBdt(item.productPrice * item.quantity), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IlamBlack)
                        }
                        Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // Calculation Summary
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = Strings.subtotal(language), fontSize = 12.sp, color = IlamTextSecondary)
                        Text(text = formatBdt(order.subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (order.discount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = Strings.discount(language), fontSize = 12.sp, color = IlamGoldDark)
                            Text(text = "-${formatBdt(order.discount)}", fontSize = 12.sp, color = IlamGoldDark, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = Strings.deliveryFee(language), fontSize = 12.sp, color = IlamTextSecondary)
                        Text(text = if (order.deliveryCharge == 0.0) Strings.free(language) else formatBdt(order.deliveryCharge), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(color = IlamBorderLight, modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = Strings.total(language), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = formatBdt(order.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Black, color = IlamGoldDark)
                    }
                }
            }
        }

        // Delivery Address Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = IlamWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, IlamBorderLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.shippingInfo(language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = IlamBlack
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = order.customerPhone, color = IlamTextSecondary, fontSize = 12.sp)
                    Text(text = "${order.customerAddress}, ${order.customerDistrict}", color = IlamTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
