package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DeliveryAddressEntity
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class PaymentOption {
    COD,
    UPI,
    CARD,
    NET_BANKING
}

enum class GatewayStatus {
    IDLE,
    INITIATING,
    PROCESSING,
    VERIFIED_SUCCESS,
    FAILED
}

@Composable
fun RozgarPaymentCheckoutSheet(
    title: String,
    subtitle: String,
    itemPrice: Double,
    quantity: Int = 1,
    deliveryFee: Double = 0.0,
    discountAmount: Double = 0.0,
    platformFeePercent: Double = 5.0,
    categoryName: String = "PRODUCTS",
    savedAddresses: List<DeliveryAddressEntity> = emptyList(),
    onSaveNewAddress: ((DeliveryAddressEntity) -> Unit)? = null,
    onPaymentConfirmed: (
        paymentMethod: String,
        paymentStatus: String,
        transactionRef: String,
        itemSubtotal: Double,
        deliveryCharge: Double,
        discount: Double,
        finalTotal: Double,
        platformCommission: Double,
        sellerPayable: Double,
        deliveryAddress: DeliveryAddressEntity?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedAddressId by remember(savedAddresses) {
        mutableLongStateOf(savedAddresses.firstOrNull { it.isDefault }?.id ?: savedAddresses.firstOrNull()?.id ?: 0L)
    }
    var selectedAddress by remember(savedAddresses, selectedAddressId) {
        mutableStateOf(savedAddresses.find { it.id == selectedAddressId } ?: savedAddresses.firstOrNull())
    }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    var selectedPaymentOption by remember { mutableStateOf(PaymentOption.COD) }
    var selectedUpiApp by remember { mutableStateOf("Google Pay") }
    var userUpiId by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("J&K Bank") }

    var gatewayStatus by remember { mutableStateOf(GatewayStatus.IDLE) }
    var gatewayStatusMessage by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf("") }

    // Precise transparent calculations
    val itemSubtotal = itemPrice * quantity
    val rawTotal = itemSubtotal + deliveryFee - discountAmount
    val finalTotal = if (rawTotal > 0.0) rawTotal else 0.0

    // Commission snapshot calculation
    val platformCommission = (itemSubtotal * platformFeePercent) / 100.0
    val sellerPayable = (itemSubtotal - platformCommission) + deliveryFee

    Dialog(onDismissRequest = {
        if (gatewayStatus != GatewayStatus.PROCESSING) onDismiss()
    }) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            border = BorderStroke(1.dp, KashmirCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Checkout & Secure Settlement",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KashmirTextPrimary,
                            maxLines = 1
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        enabled = gatewayStatus != GatewayStatus.PROCESSING
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. DELIVERY ADDRESS SECTION
                DeliveryAddressSelectorSection(
                    addresses = savedAddresses,
                    selectedAddressId = selectedAddressId,
                    onSelectAddress = {
                        selectedAddressId = it.id
                        selectedAddress = it
                        validationError = ""
                    },
                    onAddNewAddress = { showAddAddressDialog = true }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. ORDER SUMMARY CARD
                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item ($quantity x ₹${itemPrice.toInt()}):", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("₹${itemSubtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (deliveryFee > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Valley Delivery Fee:", fontSize = 12.sp, color = KashmirTextSecondary)
                                Text("₹${deliveryFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery:", fontSize = 12.sp, color = KashmirTextSecondary)
                                Text("FREE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                            }
                        }

                        if (discountAmount > 0.0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount Applied:", fontSize = 12.sp, color = KashmirForestGreen)
                                Text("- ₹${discountAmount.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Final Total Amount:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirDeepGreen)
                            Text("₹${finalTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = KashmirDeepGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. TRANSPARENT PLATFORM SETTLEMENT BOX (No false payout claims)
                Surface(
                    color = KashmirMint.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Transparent Platform Settlement Ledger",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = KashmirDeepGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform Fee (${platformFeePercent.toInt()}%):", fontSize = 11.sp, color = KashmirTextSecondary)
                            Text("₹${"%.2f".format(platformCommission)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Artisan / Merchant Settlement:", fontSize = 11.sp, color = KashmirTextSecondary)
                            Text("₹${"%.2f".format(sellerPayable)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        }
                        Text(
                            text = "Compliant platform collection with verified seller payout settlement upon order completion.",
                            fontSize = 10.sp,
                            color = KashmirTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. PAYMENT METHOD SELECTION
                Text(
                    text = "Select Payment Method",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = KashmirDeepGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Option 1: Cash on Delivery
                    PaymentMethodTile(
                        title = "Cash on Delivery (COD)",
                        subtitle = "Pay upon inspection at doorstep across Kashmir",
                        icon = Icons.Default.LocalShipping,
                        selected = selectedPaymentOption == PaymentOption.COD,
                        onClick = { selectedPaymentOption = PaymentOption.COD }
                    )

                    // Option 2: UPI Gateway
                    PaymentMethodTile(
                        title = "Instant UPI (GPay / PhonePe / Paytm / BHIM)",
                        subtitle = "Real-time bank settlement with NPCI verification",
                        icon = Icons.Default.QrCodeScanner,
                        selected = selectedPaymentOption == PaymentOption.UPI,
                        onClick = { selectedPaymentOption = PaymentOption.UPI }
                    )

                    // Option 3: Debit / Credit Card
                    PaymentMethodTile(
                        title = "Debit / Credit Card (RuPay, Visa, Mastercard)",
                        subtitle = "Secured gateway with 3D-Secure bank OTP",
                        icon = Icons.Default.CreditCard,
                        selected = selectedPaymentOption == PaymentOption.CARD,
                        onClick = { selectedPaymentOption = PaymentOption.CARD }
                    )

                    // Option 4: Net Banking
                    PaymentMethodTile(
                        title = "Net Banking (J&K Bank, HDFC, SBI)",
                        subtitle = "Direct Internet banking switch",
                        icon = Icons.Default.AccountBalance,
                        selected = selectedPaymentOption == PaymentOption.NET_BANKING,
                        onClick = { selectedPaymentOption = PaymentOption.NET_BANKING }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment specifics
                when (selectedPaymentOption) {
                    PaymentOption.COD -> {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pay ₹${finalTotal.toInt()} in cash when delivery rider arrives at your confirmed address.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    PaymentOption.UPI -> {
                        Column {
                            Text("Select Your UPI Provider", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { app ->
                                    FilterChip(
                                        selected = selectedUpiApp == app,
                                        onClick = { selectedUpiApp = app },
                                        label = { Text(app, fontSize = 10.sp) },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = userUpiId,
                                onValueChange = { userUpiId = it },
                                label = { Text("Your UPI ID (Optional)") },
                                placeholder = { Text("e.g. mobile@okjkbank") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    PaymentOption.CARD -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16 && it.all { c -> c.isDigit() }) cardNumber = it },
                                label = { Text("Card Number (RuPay / Visa / Master)") },
                                placeholder = { Text("16-digit card number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it },
                                    label = { Text("MM/YY") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) cardCvv = it },
                                    label = { Text("CVV") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    PaymentOption.NET_BANKING -> {
                        Column {
                            Text("Select Your Bank", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("J&K Bank", "HDFC Bank", "SBI", "PNB").forEach { bank ->
                                    FilterChip(
                                        selected = selectedBank == bank,
                                        onClick = { selectedBank = bank },
                                        label = { Text(bank, fontSize = 10.sp) },
                                        modifier = Modifier.height(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                val isOnlineGatewayConfigured = remember {
                    val razorpayKey = com.example.BuildConfig.RAZORPAY_KEY_ID
                    razorpayKey.isNotBlank() && !razorpayKey.contains("DEFAULT_")
                }

                // Gateway Notice for Online Payment methods when Gateway is unconfigured
                if (selectedPaymentOption != PaymentOption.COD && !isOnlineGatewayConfigured) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Online payment gateway is not configured yet.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = "To place this order now, please switch to 'Cash on Delivery (COD)'. Online gateway credentials (e.g. Razorpay Key ID / Webhook Secret) can be added securely in the admin settings without hardcoding secrets.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                }

                if (validationError.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError,
                        color = KashmirError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gateway Status Display
                if (gatewayStatus == GatewayStatus.PROCESSING) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = KashmirDeepGreen,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Payment Gateway Initiation", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirDeepGreen)
                                Text(gatewayStatusMessage, fontSize = 11.sp, color = KashmirTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Confirm / Pay Button
                Button(
                    onClick = {
                        val addr = selectedAddress
                        if (addr == null || addr.formattedAddress.isBlank()) {
                            validationError = "Please select or add a valid delivery address before proceeding."
                            return@Button
                        }

                        if (selectedPaymentOption != PaymentOption.COD && !isOnlineGatewayConfigured) {
                            validationError = "Online payment gateway is not configured yet. Please select 'Cash on Delivery (COD)' to complete your order."
                            return@Button
                        }

                        if (selectedPaymentOption == PaymentOption.CARD) {
                            if (cardNumber.length < 16) {
                                validationError = "Please enter valid 16-digit card number"
                                return@Button
                            }
                            if (cardExpiry.length < 4) {
                                validationError = "Please enter valid MM/YY expiry"
                                return@Button
                            }
                            if (cardCvv.length < 3) {
                                validationError = "Please enter valid 3-digit CVV"
                                return@Button
                            }
                        }

                        if (selectedPaymentOption == PaymentOption.COD) {
                            val codTxnRef = "COD-${System.currentTimeMillis() % 1000000}"
                            onPaymentConfirmed(
                                "CASH_ON_DELIVERY",
                                "PENDING",
                                codTxnRef,
                                itemSubtotal,
                                deliveryFee,
                                discountAmount,
                                finalTotal,
                                platformCommission,
                                sellerPayable,
                                addr
                            )
                        } else {
                            // Online Payment Gateway Architecture:
                            // Do NOT fake payment success or fake transaction IDs.
                            // In real production, this initiates a server-side order session with the payment provider.
                            gatewayStatus = GatewayStatus.PROCESSING
                            gatewayStatusMessage = "Connecting to production payment provider..."
                            coroutineScope.launch {
                                delay(600)
                                gatewayStatus = GatewayStatus.FAILED
                                gatewayStatusMessage = "Online payment gateway is not configured yet. Server order session could not be established."
                                validationError = "Online payment gateway is not configured yet. Please select Cash on Delivery (COD) to place your order."
                            }
                        }
                    },
                    enabled = gatewayStatus != GatewayStatus.PROCESSING,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("checkout_confirm_payment_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedPaymentOption != PaymentOption.COD && !isOnlineGatewayConfigured) {
                            KashmirTextMuted
                        } else KashmirDeepGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    val label = when (selectedPaymentOption) {
                        PaymentOption.COD -> "Confirm Order (COD) • ₹${finalTotal.toInt()}"
                        PaymentOption.UPI -> if (isOnlineGatewayConfigured) "Pay via UPI • ₹${finalTotal.toInt()}" else "Online Gateway Not Configured"
                        PaymentOption.CARD -> if (isOnlineGatewayConfigured) "Pay via Card • ₹${finalTotal.toInt()}" else "Online Gateway Not Configured"
                        PaymentOption.NET_BANKING -> if (isOnlineGatewayConfigured) "Pay via NetBanking • ₹${finalTotal.toInt()}" else "Online Gateway Not Configured"
                    }
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    if (showAddAddressDialog) {
        AddEditAddressDialog(
            defaultDistrict = "Srinagar",
            onSaveAddress = { newAddr ->
                onSaveNewAddress?.invoke(newAddr)
                selectedAddress = newAddr
                selectedAddressId = newAddr.id
                showAddAddressDialog = false
                validationError = ""
            },
            onDismiss = { showAddAddressDialog = false }
        )
    }
}

@Composable
private fun PaymentMethodTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (selected) KashmirMint.copy(alpha = 0.45f) else Color.White,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) KashmirDeepGreen else KashmirCardBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (selected) KashmirDeepGreen else KashmirSurfaceGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else KashmirDeepGreen,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = KashmirDeepGreen
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = KashmirTextSecondary
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = KashmirDeepGreen)
            )
        }
    }
}
