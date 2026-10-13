package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- SHOPS & SELLERS MODERATION ---
fun LazyListScope.adminShopsTab(
    viewModel: RozgarViewModel,
    shops: List<ShopEntity>
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Artisan Shops & Registered Merchants (${shops.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                Text("Manage merchant trust verification and regional shop discoverability across Kashmir.", fontSize = 11.sp, color = KashmirTextSecondary)
            }
        }
    }

    if (shops.isEmpty()) {
        item {
            Text("No registered shops found.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
        }
    } else {
        items(shops) { shop ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(shop.shopName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Owner: ${shop.ownerName} • ${shop.area}, ${shop.district}", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("Phone: ${shop.contactPhone} • Total Orders: ${shop.totalOrdersCount}", fontSize = 11.sp, color = KashmirDeepGreen)
                        }
                        Surface(
                            color = if (shop.isVerified) KashmirMint else KashmirWarmGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (shop.isVerified) "VERIFIED SHOP" else "UNVERIFIED",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (shop.isVerified) KashmirDeepGreen else KashmirWarmGold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.adminModerateShop(shop.id, !shop.isVerified) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (shop.isVerified) KashmirTextMuted else KashmirEmerald
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (shop.isVerified) "Revoke Verification" else "Approve & Verify Shop", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- RESTAURANTS & KITCHENS MODERATION ---
fun LazyListScope.adminRestaurantsTab(
    viewModel: RozgarViewModel,
    restaurants: List<RestaurantEntity>
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Kashmiri Restaurants & Kitchens (${restaurants.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                Text("Manage operational status, delivery zones, and Wazwan caterers across J&K.", fontSize = 11.sp, color = KashmirTextSecondary)
            }
        }
    }

    if (restaurants.isEmpty()) {
        item {
            Text("No restaurants currently registered.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
        }
    } else {
        items(restaurants) { rest ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rest.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${rest.area}, ${rest.district} • Hours: ${rest.openingHours}", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("Delivery Fee: ₹${rest.deliveryFee.toInt()} • Min Order: ₹${rest.minimumOrder.toInt()}", fontSize = 11.sp, color = KashmirDeepGreen)
                        }
                        Surface(
                            color = if (!rest.isClosedTemporarily) KashmirMint else KashmirError.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (!rest.isClosedTemporarily) "ACCEPTING ORDERS" else "CLOSED TEMPORARILY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (!rest.isClosedTemporarily) KashmirDeepGreen else KashmirError,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.adminModerateRestaurant(rest.id, isClosed = !rest.isClosedTemporarily) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!rest.isClosedTemporarily) KashmirError else KashmirEmerald
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(if (!rest.isClosedTemporarily) "Mark Closed" else "Open Kitchen", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- ALL ORDERS (MARKETPLACE & FOOD) MODERATION ---
fun LazyListScope.adminOrdersTab(
    viewModel: RozgarViewModel,
    orders: List<OrderEntity>
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Central Orders & Fulfillment (${orders.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                Text("Oversee marketplace products, food delivery orders, COD collection, and supplier settlement.", fontSize = 11.sp, color = KashmirTextSecondary)
            }
        }
    }

    if (orders.isEmpty()) {
        item {
            Text("No customer orders recorded yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
        }
    } else {
        items(orders) { order ->
            val dateStr = remember(order.orderedAt) {
                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.orderedAt))
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Order #${order.id} • ${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp, color = KashmirTextPrimary)
                            Text("Date: $dateStr", fontSize = 11.sp, color = KashmirTextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KashmirDeepGreen)
                            KashmirBadge(text = order.orderStatus)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Item: ${order.productTitle} (Qty: ${order.quantity})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KashmirDeepGreen
                    )
                    Text(
                        text = "Address: ${if (order.deliveryAddressFormatted.isNotBlank()) order.deliveryAddressFormatted else order.customerAddress}",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary,
                        maxLines = 2
                    )
                    Text(
                        text = "Payment: ${order.paymentMethod} (${order.paymentStatus}) • Ref: ${order.paymentTransactionRef}",
                        fontSize = 10.sp,
                        color = KashmirTextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (order.orderStatus != "DELIVERED") {
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(order.id, "DELIVERED", "Confirmed delivered by Admin") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Mark Delivered", fontSize = 10.sp)
                            }
                        }
                        if (order.orderStatus != "PROCESSING") {
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(order.id, "PROCESSING", "In dispatch process") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Processing", fontSize = 10.sp)
                            }
                        }
                        if (order.orderStatus != "CANCELLED" && order.orderStatus != "REFUNDED") {
                            Button(
                                onClick = { viewModel.adminUpdateOrderStatus(order.id, "CANCELLED", "Cancelled by Admin") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Cancel", fontSize = 10.sp)
                            }
                        }
                        if (order.orderStatus != "REFUNDED") {
                            Button(
                                onClick = { viewModel.adminRefundOrder(order.id, "Admin verified customer return/cancellation") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Refund ₹${order.totalAmount.toInt()}", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PROPERTY LISTINGS MODERATION ---
fun LazyListScope.adminPropertiesTab(
    viewModel: RozgarViewModel,
    properties: List<PropertyListingEntity>
) {
    item {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Property Listings Moderation (${properties.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                Text("Audit residential flats, commercial shops, plots, and student accommodation across Kashmir.", fontSize = 11.sp, color = KashmirTextSecondary)
            }
        }
    }

    if (properties.isEmpty()) {
        item {
            Text("No property listings recorded.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
        }
    } else {
        items(properties) { prop ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prop.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${prop.propertyType} • ${prop.listingType} • ${prop.town}, ${prop.district}", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("Owner: ${prop.ownerName}", fontSize = 11.sp, color = KashmirDeepGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("₹${prop.rentOrPrice.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KashmirDeepGreen)
                            KashmirBadge(text = prop.status)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (prop.status == "ACTIVE") {
                            Button(
                                onClick = { viewModel.adminModerateProperty(prop.id, "HIDDEN") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirTextMuted),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Hide Listing", fontSize = 10.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.adminModerateProperty(prop.id, "ACTIVE") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Restore Active", fontSize = 10.sp)
                            }
                        }
                        Button(
                            onClick = { viewModel.adminModerateProperty(prop.id, "REMOVED") },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Remove", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
