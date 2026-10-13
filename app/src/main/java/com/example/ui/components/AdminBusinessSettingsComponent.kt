package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayoutRequestEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.TransactionLedgerEntity
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

fun LazyListScope.adminCommissionSettingsTab(
    viewModel: RozgarViewModel,
    settings: PlatformSettingsEntity?
) {
    item {
        AdminCommissionSettingsContent(viewModel = viewModel, currentSettings = settings)
    }
}

@Composable
fun AdminCommissionSettingsContent(
    viewModel: RozgarViewModel,
    currentSettings: PlatformSettingsEntity?
) {
    val initial = currentSettings ?: PlatformSettingsEntity()

    var productRate by remember(currentSettings) { mutableStateOf(initial.productCommissionPercent.toString()) }
    var propSaleRate by remember(currentSettings) { mutableStateOf(initial.propertySaleCommissionPercent.toString()) }
    var propRentalRate by remember(currentSettings) { mutableStateOf(initial.propertyRentalCommissionPercent.toString()) }
    var bookRate by remember(currentSettings) { mutableStateOf(initial.bookCommissionPercent.toString()) }
    var foodRate by remember(currentSettings) { mutableStateOf(initial.foodCommissionPercent.toString()) }
    var resellerRate by remember(currentSettings) { mutableStateOf(initial.resellerCommissionPercent.toString()) }
    var membershipPrice by remember(currentSettings) { mutableStateOf(initial.membershipPriceMonthly.toInt().toString()) }

    var productEnabled by remember(currentSettings) { mutableStateOf(initial.productCommissionEnabled) }
    var propSaleEnabled by remember(currentSettings) { mutableStateOf(initial.propertySaleCommissionEnabled) }
    var propRentalEnabled by remember(currentSettings) { mutableStateOf(initial.propertyRentalCommissionEnabled) }
    var bookEnabled by remember(currentSettings) { mutableStateOf(initial.bookCommissionEnabled) }
    var foodEnabled by remember(currentSettings) { mutableStateOf(initial.foodCommissionEnabled) }
    var resellerEnabled by remember(currentSettings) { mutableStateOf(initial.resellerCommissionEnabled) }

    var includeDelivery by remember(currentSettings) { mutableStateOf(initial.includeDeliveryInCommission) }
    var includeTaxes by remember(currentSettings) { mutableStateOf(initial.includeTaxesInCommission) }
    var effectiveDate by remember(currentSettings) { mutableStateOf(initial.effectiveDate) }

    // Live Preview Calculator State
    var calcAmountText by remember { mutableStateOf("1000") }
    var calcCategory by remember { mutableStateOf("New & Second-Hand Products (5%)") }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, null, tint = KashmirDeepGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Commission & Business Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "All marketplace commission rates, subscription plans, and fee rules are dynamically configured here without requiring an Android app rebuild.",
                    fontSize = 12.sp,
                    color = KashmirTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Commission Rates Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Category Commission Percentages",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                CommissionRateRow(
                    title = "New & Second-Hand Products",
                    basis = "Applied to gross sale price of items",
                    rate = productRate,
                    onRateChange = { productRate = it },
                    enabled = productEnabled,
                    onToggleEnabled = { productEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                CommissionRateRow(
                    title = "Property Sales (Real Estate)",
                    basis = "5% of qualifying agreed property sale price",
                    rate = propSaleRate,
                    onRateChange = { propSaleRate = it },
                    enabled = propSaleEnabled,
                    onToggleEnabled = { propSaleEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                CommissionRateRow(
                    title = "Property Rentals (Flats / Rooms / Shops)",
                    basis = "5% of explicitly disclosed 1st month's rent",
                    rate = propRentalRate,
                    onRateChange = { propRentalRate = it },
                    enabled = propRentalEnabled,
                    onToggleEnabled = { propRentalEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                CommissionRateRow(
                    title = "Kashmiri Books & Authorized Literature",
                    basis = "2% of eligible paid book sales (free books are 0%)",
                    rate = bookRate,
                    onRateChange = { bookRate = it },
                    enabled = bookEnabled,
                    onToggleEnabled = { bookEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                CommissionRateRow(
                    title = "Restaurant & Food Orders (Wazwan / Kitchens)",
                    basis = "1% of eligible food order amount (e.g. ₹1 per ₹100 order)",
                    rate = foodRate,
                    onRateChange = { foodRate = it },
                    enabled = foodEnabled,
                    onToggleEnabled = { foodEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                CommissionRateRow(
                    title = "Reseller Transactions",
                    basis = "5% of eligible transaction margin amount",
                    rate = resellerRate,
                    onRateChange = { resellerRate = it },
                    enabled = resellerEnabled,
                    onToggleEnabled = { resellerEnabled = it }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = KashmirCardBorder.copy(alpha = 0.5f))

                // Membership Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monthly Premium Membership", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Configurable monthly recurring subscription fee", fontSize = 11.sp, color = KashmirTextSecondary)
                    }
                    OutlinedTextField(
                        value = membershipPrice,
                        onValueChange = { membershipPrice = it },
                        prefix = { Text("₹") },
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Advanced Policies Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "Calculation Rules & Effective Date",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeDelivery,
                        onCheckedChange = { includeDelivery = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Include local delivery/shipping charges in commission calculation basis", fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeTaxes,
                        onCheckedChange = { includeTaxes = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Include GST/taxes in commission calculation basis", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = effectiveDate,
                    onValueChange = { effectiveDate = it },
                    label = { Text("Effective Date for Rate Changes") },
                    placeholder = { Text("Immediate or e.g. 2026-11-01") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interactive Live Preview Calculator
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = KashmirSurfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, null, tint = KashmirDeepGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Live Commission Preview Calculator",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Simulate an order or transaction before saving settings to preview platform revenue and seller payout.",
                    fontSize = 11.sp,
                    color = KashmirTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = calcAmountText,
                    onValueChange = { calcAmountText = it },
                    label = { Text("Enter Sample Transaction Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                val categories = listOf(
                    "Products (5%)",
                    "Property Sale (5%)",
                    "Property Rental (5%)",
                    "Books (2%)",
                    "Restaurant Food (1%)",
                    "Reseller Margin (5%)"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = calcCategory == cat,
                            onClick = { calcCategory = cat },
                            label = { Text(cat.take(13), fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = calcCategory == cat,
                            onClick = { calcCategory = cat },
                            label = { Text(cat.take(13), fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val sampleAmt = calcAmountText.toDoubleOrNull() ?: 1000.0
                val activeRate = when (calcCategory) {
                    "Books (2%)" -> bookRate.toDoubleOrNull() ?: 2.0
                    "Restaurant Food (1%)" -> foodRate.toDoubleOrNull() ?: 1.0
                    "Property Sale (5%)" -> propSaleRate.toDoubleOrNull() ?: 5.0
                    "Property Rental (5%)" -> propRentalRate.toDoubleOrNull() ?: 5.0
                    "Reseller Margin (5%)" -> resellerRate.toDoubleOrNull() ?: 5.0
                    else -> productRate.toDoubleOrNull() ?: 5.0
                }

                val calculatedFee = (sampleAmt * activeRate) / 100.0
                val sellerNetPayout = sampleAmt - calculatedFee

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, KashmirCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sample Gross Transaction:", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("₹${sampleAmt.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Platform Commission (${activeRate}%):", fontSize = 12.sp, color = KashmirForestGreen)
                            Text("₹${"%.2f".format(calculatedFee)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Kashmiri Seller / Provider Payout:", fontSize = 12.sp, color = KashmirDeepGreen)
                            Text("₹${"%.2f".format(sellerNetPayout)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save Settings Action
        Button(
            onClick = {
                val updated = PlatformSettingsEntity(
                    id = 1,
                    productCommissionPercent = productRate.toDoubleOrNull() ?: 5.0,
                    propertySaleCommissionPercent = propSaleRate.toDoubleOrNull() ?: 5.0,
                    propertyRentalCommissionPercent = propRentalRate.toDoubleOrNull() ?: 5.0,
                    bookCommissionPercent = bookRate.toDoubleOrNull() ?: 2.0,
                    foodCommissionPercent = foodRate.toDoubleOrNull() ?: 1.0,
                    resellerCommissionPercent = resellerRate.toDoubleOrNull() ?: 5.0,
                    membershipPriceMonthly = membershipPrice.toDoubleOrNull() ?: 99.0,
                    productCommissionEnabled = productEnabled,
                    propertySaleCommissionEnabled = propSaleEnabled,
                    propertyRentalCommissionEnabled = propRentalEnabled,
                    bookCommissionEnabled = bookEnabled,
                    foodCommissionEnabled = foodEnabled,
                    resellerCommissionEnabled = resellerEnabled,
                    includeDeliveryInCommission = includeDelivery,
                    includeTaxesInCommission = includeTaxes,
                    effectiveDate = effectiveDate.ifBlank { "Immediate" },
                    updatedAt = System.currentTimeMillis(),
                    updatedByAdminEmail = "bilaltantraypersonal@gmail.com"
                )
                viewModel.updatePlatformSettings(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_commission_settings_button")
        ) {
            Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & Apply Business Settings", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Last updated: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(initial.updatedAt))} by ${initial.updatedByAdminEmail}",
            fontSize = 11.sp,
            color = KashmirTextMuted
        )
    }
}

@Composable
fun CommissionRateRow(
    title: String,
    basis: String,
    rate: String,
    onRateChange: (String) -> Unit,
    enabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(basis, fontSize = 11.sp, color = KashmirTextSecondary)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = rate,
                onValueChange = onRateChange,
                suffix = { Text("%") },
                modifier = Modifier.width(84.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Switch(
                checked = enabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(checkedThumbColor = KashmirEmerald)
            )
        }
    }
}

fun LazyListScope.adminCentralLedgerTab(
    viewModel: RozgarViewModel,
    ledgerEntries: List<TransactionLedgerEntity>,
    payoutRequests: List<PayoutRequestEntity>,
    totalCommission: Double
) {
    val totalGrossSales = ledgerEntries.sumOf { it.grossAmount }
    val totalPlatformCommission = totalCommission
    val totalGatewayFees = ledgerEntries.sumOf { it.paymentProviderFee }
    val totalSellerPayable = ledgerEntries.sumOf { it.sellerPayableAmount }
    val totalRefunds = ledgerEntries.filter { it.paymentStatus == "REFUNDED" }.sumOf { it.grossAmount }
    val netPlatformEarnings = (totalPlatformCommission - totalGatewayFees).coerceAtLeast(0.0)
    val totalPendingPayouts = payoutRequests.filter { it.status == "PENDING" }.sumOf { it.amount }
    val totalCompletedPayouts = payoutRequests.filter { it.status == "PAID" }.sumOf { it.amount }

    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, null, tint = KashmirDeepGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Central Financial & Payout Control",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Transparent platform earnings, seller settlements, refunds, and gateway fees across all Kashmir commerce verticals.",
                    fontSize = 12.sp,
                    color = KashmirTextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Row 1: Gross Sales, Net Earnings, Platform Commission
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = KashmirSurfaceGreen,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Gross Sales", fontSize = 10.sp, color = KashmirDeepGreen)
                            Text("₹${totalGrossSales.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                        }
                    }

                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Platform Comm.", fontSize = 10.sp, color = KashmirForestGreen)
                            Text("₹${"%.2f".format(totalPlatformCommission)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KashmirForestGreen)
                        }
                    }

                    Surface(
                        color = KashmirSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Net Earnings", fontSize = 10.sp, color = KashmirTextSecondary)
                            Text("₹${"%.2f".format(netPlatformEarnings)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Seller Payable, Gateway Fees, Refunds
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = KashmirSurfaceGreen.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Seller Payable", fontSize = 10.sp, color = KashmirDeepGreen)
                            Text("₹${totalSellerPayable.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        }
                    }

                    Surface(
                        color = KashmirSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Gateway Fees", fontSize = 10.sp, color = KashmirTextSecondary)
                            Text("₹${"%.2f".format(totalGatewayFees)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                        }
                    }

                    Surface(
                        color = KashmirSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Refunds", fontSize = 10.sp, color = KashmirTextSecondary)
                            Text("₹${totalRefunds.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KashmirTextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: Pending Payouts vs Completed Payouts
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = KashmirGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Pending Payouts", fontSize = 10.sp, color = KashmirDeepGreen)
                            Text("₹${totalPendingPayouts.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        }
                    }

                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Completed Payouts", fontSize = 10.sp, color = KashmirForestGreen)
                            Text("₹${totalCompletedPayouts.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    // Platform Own Settlement Account Configuration
    item {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, KashmirCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Platform Settlement Account & Gateway Setup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Secure credentials for Rozgar Kashmir's nodal bank account and UPI gateway settlement. Financial credentials are never hard-coded in client code.",
                    fontSize = 11.sp,
                    color = KashmirTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Platform Nodal Bank: J&K Bank (The Jammu & Kashmir Bank Ltd)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        Text("Account Name: Rozgar Kashmir Public Employment & Artisan Foundation", fontSize = 11.sp, color = KashmirTextSecondary)
                        Text("Account Number: •••••••••••• 8910 (Masked Nodal Account)", fontSize = 11.sp, color = KashmirTextSecondary)
                        Text("IFSC Code: JAKA0CHINAR (Chinar Complex Branch)", fontSize = 11.sp, color = KashmirTextSecondary)
                        Text("Merchant UPI VPA: rozgarkashmir@okjkbank", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KashmirForestGreen)
                        Text("Payment Gateway: Razorpay / Cashfree Verified Multi-Vendor Route", fontSize = 11.sp, color = KashmirTextMuted)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    }

    item {
        Text(
            text = "Settlement Transactions Ledger (${ledgerEntries.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (ledgerEntries.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text("No ledger transactions recorded yet.", modifier = Modifier.padding(24.dp), color = KashmirTextSecondary)
            }
        }
    } else {
        items(ledgerEntries) { entry ->
            LedgerEntryCard(
                entry = entry,
                onSettle = { viewModel.settleLedgerTransaction(entry.id, "Verified by Admin") }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    item {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Seller & Contributor Payout Requests (${payoutRequests.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (payoutRequests.isEmpty()) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text("No pending payout requests.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
            }
        }
    } else {
        items(payoutRequests) { req ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (req.status == "PENDING") KashmirGold else KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(req.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Role: ${req.userRole} • Requested: ₹${req.amount.toInt()}", fontSize = 12.sp, color = KashmirDeepGreen, fontWeight = FontWeight.SemiBold)
                        }
                        KashmirBadge(
                            text = req.status,
                            containerColor = if (req.status == "PENDING") KashmirGold.copy(alpha = 0.25f) else KashmirMint
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("Masked UPI: ${req.upiIdMasked}", fontSize = 11.sp, color = KashmirTextSecondary)

                    if (req.status == "PENDING") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = { viewModel.processPayoutRequest(req.id, false, "Declined by admin") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError)
                            ) {
                                Text("Reject")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.processPayoutRequest(req.id, true, "Paid via verified bank settlement") },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                            ) {
                                Text("Approve & Mark Paid")
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun LedgerEntryCard(
    entry: TransactionLedgerEntity,
    onSettle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KashmirBadge(
                        text = entry.category,
                        containerColor = KashmirMint
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(entry.transactionRef, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirTextMuted)
                }

                KashmirBadge(
                    text = entry.settlementStatus,
                    containerColor = if (entry.settlementStatus == "SETTLED") KashmirMint else KashmirGold.copy(alpha = 0.25f),
                    contentColor = if (entry.settlementStatus == "SETTLED") KashmirForestGreen else KashmirDeepGreen
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(entry.listingTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirTextPrimary)
            Text("Order: ${entry.orderRef} • Buyer: ${entry.buyerName} ➔ Seller: ${entry.sellerName}", fontSize = 11.sp, color = KashmirTextSecondary)

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gross Amount: ₹${entry.grossAmount.toInt()}", fontSize = 11.sp, color = KashmirTextSecondary)
                        Text("Calculation: ${entry.commissionCalculationBasis}", fontSize = 10.sp, color = KashmirTextMuted)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Fee: ₹${"%.2f".format(entry.platformCommission)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                        Text("Payable: ₹${"%.2f".format(entry.sellerPayableAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    }
                }
            }

            if (entry.settlementStatus != "SETTLED") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onSettle,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirForestGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Confirm Settlement", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
