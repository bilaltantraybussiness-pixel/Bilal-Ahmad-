package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ShopEntity
import com.example.ui.components.KASHMIR_DISTRICTS
import com.example.ui.components.KashmirBadge
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen

private val SELLER_PRODUCT_CATEGORIES = listOf(
    "Kashmiri Handicrafts (دستکاری)",
    "Pheran & Traditional Wear (پھؠرَن)",
    "Kangri & Winter Crafts (کانگٕرؠ)",
    "Copperware & Utensils (ترٛامِک بانہٕ)",
    "Walnut Wood & Carvings",
    "Walnuts, Kernels & Seeds",
    "Kashmir Saffron & Spices",
    "Clothing & Apparel (Men/Women/Kids)",
    "Shawls & Stoles (Pashmina/Kani)",
    "Art & Calligraphy (فن اور خطاطی)",
    "Kashmir Groceries & Dried Foods",
    "Books & Kashmir Literature",
    "Electronics & Mobile Accessories",
    "Household & Kashmiri Samovars",
    "Second-Hand / Pre-Loved Marketplace"
)

private val SAMPLE_PRODUCT_PHOTOS = listOf(
    "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500" to "Kashmir Handloom Shawl",
    "https://images.unsplash.com/photo-1590736969955-71cc94801759?w=500" to "Traditional Pheran",
    "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500" to "Carved Walnut Box",
    "https://images.unsplash.com/photo-1541689592655-f5f52825a3b8?w=500" to "Hammered Copper Samovar",
    "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500" to "Pure Kashmir Saffron",
    "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=500" to "Organic Kashmir Apples",
    "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500" to "Walnut Kernels (Giri)",
    "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500" to "Woven Kangri Firepot"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(viewModel: RozgarViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allShops by viewModel.allShops.collectAsState()
    val supplierOrders by viewModel.supplierOrders.collectAsState()
    val sellerProducts by viewModel.sellerProducts.collectAsState()
    val userPayoutRequests by viewModel.userPayoutRequests.collectAsState()
    val centralLedgerEntries by viewModel.centralLedgerEntries.collectAsState()

    val userShop = remember(allShops, currentUser) {
        allShops.firstOrNull { it.ownerUserId == currentUser?.id }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Stock, 2: Add Product, 3: Payouts, 4: Settings
    var orderStatusFilter by remember { mutableStateOf("ALL") }
    var stockFilter by remember { mutableStateOf("ALL") } // ALL, LOW, OUT

    // Dialog state for stock edit
    var editingProductForStock by remember { mutableStateOf<ProductEntity?>(null) }
    var newStockInput by remember { mutableStateOf("") }

    // Dialog state for order notes / tracking
    var updatingOrderWithNotes by remember { mutableStateOf<Pair<OrderEntity, String>?>(null) }
    var customTrackingNote by remember { mutableStateOf("") }

    // Payout dialogs
    var showBankSetupDialog by remember { mutableStateOf(false) }
    var showRequestPayoutDialog by remember { mutableStateOf(false) }
    var sellerUpiInput by remember { mutableStateOf("9858****@okjkbank") }
    var sellerBankAccInput by remember { mutableStateOf("J&K Bank •••• 4182") }
    var sellerIfscInput by remember { mutableStateOf("JAKA0NOWHAT") }
    var payoutAmountInput by remember { mutableStateOf("") }

    val filteredOrders = remember(supplierOrders, orderStatusFilter) {
        when (orderStatusFilter) {
            "PENDING" -> supplierOrders.filter { it.orderStatus in listOf("ORDERED", "PENDING") }
            "CONFIRMED" -> supplierOrders.filter { it.orderStatus == "CONFIRMED" }
            "PREPARING" -> supplierOrders.filter { it.orderStatus in listOf("PREPARING", "PACKED") }
            "DISPATCHED" -> supplierOrders.filter { it.orderStatus in listOf("DISPATCHED", "SHIPPED") }
            "OUT_FOR_DELIVERY" -> supplierOrders.filter { it.orderStatus == "OUT_FOR_DELIVERY" }
            "DELIVERED" -> supplierOrders.filter { it.orderStatus == "DELIVERED" }
            "CANCELLED" -> supplierOrders.filter { it.orderStatus == "CANCELLED" }
            else -> supplierOrders
        }
    }

    val filteredProducts = remember(sellerProducts, stockFilter) {
        when (stockFilter) {
            "LOW" -> sellerProducts.filter { it.stockQuantity in 1..4 }
            "OUT" -> sellerProducts.filter { it.stockQuantity <= 0 }
            "IN" -> sellerProducts.filter { it.stockQuantity >= 5 }
            else -> sellerProducts
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = userShop?.shopName ?: "Seller Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KashmirEmerald)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firebase Firestore • Real-time Sync Active",
                                fontSize = 11.sp,
                                color = KashmirEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.MARKETPLACE) }) {
                        Icon(Icons.Default.Storefront, contentDescription = "View Marketplace", tint = KashmirDeepGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = KashmirDeepGreen
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KashmirBackground),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header: Shop Overview & Stats
            item {
                SellerHeaderSection(
                    shop = userShop,
                    totalProducts = sellerProducts.size,
                    totalOrders = supplierOrders.size,
                    pendingOrders = supplierOrders.count { it.orderStatus in listOf("ORDERED", "PENDING", "CONFIRMED", "PREPARING") },
                    inStockCount = sellerProducts.sumOf { it.stockQuantity }
                )
            }

            // Dashboard Tabs Navigation
            item {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = KashmirDeepGreen,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Orders", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                val pendingCount = supplierOrders.count { it.orderStatus in listOf("ORDERED", "PENDING") }
                                if (pendingCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = KashmirSaffron) { Text("$pendingCount") }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Stock", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                val lowStock = sellerProducts.count { it.stockQuantity in 1..4 }
                                if (lowStock > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Badge(containerColor = KashmirAmber) { Text("$lowStock") }
                                }
                            }
                        },
                        icon = { Icon(Icons.Default.Inventory, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Add Product", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.AddCircleOutline, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Payouts", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: ORDERS & FULFILLMENT WORKFLOW (Pending -> Confirmed -> Packed -> Dispatched -> Out for Delivery -> Delivered)
                    item {
                        OrderFiltersRow(
                            selectedFilter = orderStatusFilter,
                            onSelectFilter = { orderStatusFilter = it },
                            orders = supplierOrders
                        )
                    }

                    if (filteredOrders.isEmpty()) {
                        item {
                            EmptyOrdersState(orderStatusFilter)
                        }
                    } else {
                        items(filteredOrders, key = { it.id }) { order ->
                            SellerOrderCard(
                                order = order,
                                onUpdateStatus = { targetStatus, note ->
                                    viewModel.updateOrderStatus(order.id, targetStatus, note)
                                },
                                onOpenTrackingNoteDialog = { targetStatus ->
                                    updatingOrderWithNotes = Pair(order, targetStatus)
                                    customTrackingNote = order.trackingNotes
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // TAB 1: INVENTORY & STOCK MANAGEMENT
                    item {
                        StockFiltersRow(
                            selectedFilter = stockFilter,
                            onSelectFilter = { stockFilter = it },
                            products = sellerProducts
                        )
                    }

                    if (filteredProducts.isEmpty()) {
                        item {
                            EmptyStockState(
                                onNavigateToAddProduct = { selectedTab = 2 }
                            )
                        }
                    } else {
                        items(filteredProducts, key = { it.id }) { product ->
                            SellerStockCard(
                                product = product,
                                onIncrementStock = {
                                    val newStock = product.stockQuantity + 1
                                    viewModel.updateProductStock(product.id, newStock)
                                },
                                onDecrementStock = {
                                    if (product.stockQuantity > 0) {
                                        val newStock = product.stockQuantity - 1
                                        viewModel.updateProductStock(product.id, newStock)
                                    }
                                },
                                onOpenStockDialog = {
                                    editingProductForStock = product
                                    newStockInput = product.stockQuantity.toString()
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // TAB 2: COMPREHENSIVE ADD PRODUCT FORM (Up to 10 photos, sizes, colors, wholesale reselling)
                    item {
                        AddSellerProductForm(
                            viewModel = viewModel,
                            onProductAdded = {
                                selectedTab = 1 // Switch to stock view
                            }
                        )
                    }
                }

                3 -> {
                    // TAB 3: SELLER / SUPPLIER PAYOUTS & FINANCIAL LEDGER
                    item {
                        SellerPayoutsSection(
                            orders = supplierOrders,
                            payoutRequests = userPayoutRequests,
                            ledgerEntries = centralLedgerEntries,
                            maskedUpi = sellerUpiInput,
                            maskedBank = sellerBankAccInput,
                            onOpenBankSetup = { showBankSetupDialog = true },
                            onRequestPayout = { showRequestPayoutDialog = true }
                        )
                    }
                }

                4 -> {
                    // TAB 4: SHOP SETTINGS & VERIFICATION
                    item {
                        SellerShopSettingsSection(
                            shop = userShop,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Dialog for Bank / UPI Setup
    if (showBankSetupDialog) {
        var tempUpi by remember { mutableStateOf(sellerUpiInput) }
        var tempBank by remember { mutableStateOf(sellerBankAccInput) }
        var tempIfsc by remember { mutableStateOf(sellerIfscInput) }

        AlertDialog(
            onDismissRequest = { showBankSetupDialog = false },
            title = { Text("Bank & UPI Payout Setup", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Configure your secure settlement payout destination. Details are stored securely and masked for privacy.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempUpi,
                        onValueChange = { tempUpi = it },
                        label = { Text("UPI ID (Google Pay, PhonePe, J&K Bank mPay)") },
                        placeholder = { Text("e.g. yourname@okaxis") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempBank,
                        onValueChange = { tempBank = it },
                        label = { Text("Bank Account Number") },
                        placeholder = { Text("e.g. 0123040100001234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempIfsc,
                        onValueChange = { tempIfsc = it },
                        label = { Text("IFSC Code") },
                        placeholder = { Text("e.g. JAKA0NOWHAT") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        sellerUpiInput = if (tempUpi.length > 5) "${tempUpi.take(4)}****@${tempUpi.substringAfter("@", "upi")}" else tempUpi
                        sellerBankAccInput = if (tempBank.length > 4) "J&K Bank •••• ${tempBank.takeLast(4)}" else tempBank
                        sellerIfscInput = tempIfsc
                        showBankSetupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Save Payout Method")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBankSetupDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog for Requesting Payout
    if (showRequestPayoutDialog) {
        val availableForPayout = supplierOrders
            .filter { it.orderStatus == "DELIVERED" }
            .sumOf { it.supplierShareAmount }

        AlertDialog(
            onDismissRequest = { showRequestPayoutDialog = false },
            title = { Text("Request Payout Settlement", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Available for Immediate Payout: ₹${availableForPayout.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = KashmirForestGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Payouts are processed directly to your linked UPI/Bank: $sellerUpiInput",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = payoutAmountInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) payoutAmountInput = it },
                        label = { Text("Payout Amount (₹)") },
                        placeholder = { Text("Enter amount up to ₹${availableForPayout.toInt()}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = payoutAmountInput.toDoubleOrNull() ?: availableForPayout
                        if (amt > 0.0) {
                            viewModel.requestPayout(
                                amount = amt,
                                upiId = sellerUpiInput,
                                bankAccount = sellerBankAccInput,
                                ifsc = sellerIfscInput
                            )
                            showRequestPayoutDialog = false
                            payoutAmountInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Confirm Payout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestPayoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog for custom stock edit
    editingProductForStock?.let { prod ->
        AlertDialog(
            onDismissRequest = { editingProductForStock = null },
            title = { Text("Update Stock Quantity", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(prod.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStockInput,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) newStockInput = it },
                        label = { Text("Available Units") },
                        placeholder = { Text("e.g. 20") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Stock changes are synced instantly to Firebase Firestore.",
                        fontSize = 11.sp,
                        color = KashmirEmerald
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stockVal = newStockInput.toIntOrNull() ?: prod.stockQuantity
                        viewModel.updateProductStock(prod.id, stockVal)
                        editingProductForStock = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Save to Cloud")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingProductForStock = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog for custom order tracking note
    updatingOrderWithNotes?.let { (order, targetStatus) ->
        AlertDialog(
            onDismissRequest = { updatingOrderWithNotes = null },
            title = { Text("Add Tracking / Dispatch Note", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Order: ${order.orderNumber}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Moving status to: $targetStatus", fontSize = 12.sp, color = KashmirDeepGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customTrackingNote,
                        onValueChange = { customTrackingNote = it },
                        label = { Text("Tracking Notes / Courier Name") },
                        placeholder = { Text("e.g. Handed to Valley Express Rider Bilal (Phone: 94190...)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateOrderStatus(order.id, targetStatus, customTrackingNote)
                        updatingOrderWithNotes = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Update Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { updatingOrderWithNotes = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SellerHeaderSection(
    shop: ShopEntity?,
    totalProducts: Int,
    totalOrders: Int,
    pendingOrders: Int,
    inStockCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = KashmirMint,
                        border = BorderStroke(1.5.dp, KashmirEmerald)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Storefront, null, tint = KashmirDeepGreen, modifier = Modifier.size(24.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = shop?.shopName ?: "My Kashmiri Shop",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = KashmirDeepGreen
                            )
                            if (shop?.isVerified == true) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, "Verified", tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            text = "${shop?.shopType ?: "Artisan & Merchant"} • ${shop?.district ?: "Srinagar"}",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )
                    }
                }

                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "⭐ ${shop?.rating ?: 5.0f} (${shop?.reviewCount ?: 0})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // 4 Key KPI metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Listings", value = "$totalProducts", icon = Icons.Default.Category)
                MetricItem(label = "In Stock", value = "$inStockCount", icon = Icons.Default.Inventory2)
                MetricItem(
                    label = "Pending",
                    value = "$pendingOrders",
                    icon = Icons.Default.PendingActions,
                    highlight = pendingOrders > 0
                )
                MetricItem(label = "All Orders", value = "$totalOrders", icon = Icons.Default.ReceiptLong)
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    highlight: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (highlight) KashmirSaffron else KashmirDeepGreen
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (highlight) KashmirSaffron else KashmirDeepGreen
            )
        }
        Text(text = label, fontSize = 11.sp, color = KashmirTextSecondary)
    }
}

@Composable
private fun OrderFiltersRow(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    orders: List<OrderEntity>
) {
    val filters = listOf(
        "ALL" to "All (${orders.size})",
        "PENDING" to "Pending (${orders.count { it.orderStatus in listOf("ORDERED", "PENDING") }})",
        "CONFIRMED" to "Confirmed (${orders.count { it.orderStatus == "CONFIRMED" }})",
        "PREPARING" to "Preparing (${orders.count { it.orderStatus in listOf("PREPARING", "PACKED") }})",
        "DISPATCHED" to "Dispatched (${orders.count { it.orderStatus in listOf("DISPATCHED", "SHIPPED") }})",
        "OUT_FOR_DELIVERY" to "Out for Delivery (${orders.count { it.orderStatus == "OUT_FOR_DELIVERY" }})",
        "DELIVERED" to "Delivered (${orders.count { it.orderStatus == "DELIVERED" }})",
        "CANCELLED" to "Cancelled (${orders.count { it.orderStatus == "CANCELLED" }})"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        filters.forEach { (key, label) ->
            FilterChip(
                selected = selectedFilter == key,
                onClick = { onSelectFilter(key) },
                label = { Text(label, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = KashmirMint,
                    selectedLabelColor = KashmirDeepGreen
                )
            )
        }
    }
}

@Composable
private fun SellerOrderCard(
    order: OrderEntity,
    onUpdateStatus: (String, String) -> Unit,
    onOpenTrackingNoteDialog: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KashmirDeepGreen
                    )
                    Text(
                        text = "Placed by ${order.customerName}",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )
                }

                Surface(
                    color = when (order.orderStatus) {
                        "DELIVERED" -> KashmirMint
                        "OUT_FOR_DELIVERY" -> Color(0xFFE0F2FE)
                        "DISPATCHED", "SHIPPED" -> KashmirWarmGold.copy(alpha = 0.2f)
                        "PREPARING", "PACKED" -> Color(0xFFFEF3C7)
                        "CONFIRMED" -> Color(0xFFEDE9FE)
                        "CANCELLED" -> Color(0xFFFEE2E2)
                        else -> KashmirSurfaceGreen
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.orderStatus,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = when (order.orderStatus) {
                            "DELIVERED" -> KashmirDeepGreen
                            "OUT_FOR_DELIVERY" -> Color(0xFF0369A1)
                            "DISPATCHED", "SHIPPED" -> KashmirWarmGold
                            "PREPARING", "PACKED" -> KashmirAmber
                            "CONFIRMED" -> Color(0xFF6B21A8)
                            "CANCELLED" -> KashmirError
                            else -> KashmirForestGreen
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Product Details Row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                if (order.productImageUri.isNotBlank()) {
                    AsyncImage(
                        model = order.productImageUri,
                        contentDescription = order.productTitle,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.productTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (order.selectedSize.isNotBlank() || order.selectedColor.isNotBlank()) {
                        Text(
                            text = "Variant: ${order.selectedSize} ${order.selectedColor}".trim(),
                            fontSize = 11.sp,
                            color = KashmirTextSecondary
                        )
                    }
                    Text(
                        text = "Qty: ${order.quantity} • Total (COD): ₹${order.totalAmount.toInt()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Delivery Address & Contact
            Surface(
                color = KashmirSurfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = KashmirDeepGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${order.customerAddress}, ${order.customerDistrict}",
                            fontSize = 11.sp,
                            color = KashmirTextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (order.customerPhone.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, null, tint = KashmirForestGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Contact: ${order.customerPhone}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = KashmirDeepGreen
                            )
                        }
                    }
                    if (order.trackingNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Notes: ${order.trackingNotes}",
                            fontSize = 10.sp,
                            color = KashmirTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ORDER STATUS PROGRESSION BUTTONS (Pending to Delivered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (order.orderStatus) {
                    "ORDERED", "PENDING" -> {
                        Button(
                            onClick = { onUpdateStatus("CONFIRMED", "Order confirmed by Kashmiri artisan") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("confirm_order_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onUpdateStatus("CANCELLED", "Cancelled by seller") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("Reject", fontSize = 11.sp)
                        }
                    }

                    "CONFIRMED" -> {
                        Button(
                            onClick = { onUpdateStatus("PREPARING", "Goods packed and ready for pickup") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirForestGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Inventory2, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Packed / Preparing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "PREPARING", "PACKED" -> {
                        Button(
                            onClick = { onOpenTrackingNoteDialog("DISPATCHED") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dispatch / Handover", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "DISPATCHED", "SHIPPED" -> {
                        Button(
                            onClick = { onUpdateStatus("OUT_FOR_DELIVERY", "Rider out for doorstep delivery") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DirectionsBike, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Out for Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "OUT_FOR_DELIVERY" -> {
                        Button(
                            onClick = { onUpdateStatus("DELIVERED", "Delivered successfully & COD collected") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_delivered_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Delivered (Collect COD)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    "DELIVERED" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Fulfilled & Payment Collected", fontSize = 11.sp, color = KashmirEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    "CANCELLED" -> {
                        Text("Order was cancelled", fontSize = 11.sp, color = KashmirError, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StockFiltersRow(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    products: List<ProductEntity>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { onSelectFilter("ALL") },
            label = { Text("All (${products.size})", fontSize = 12.sp) }
        )
        FilterChip(
            selected = selectedFilter == "IN",
            onClick = { onSelectFilter("IN") },
            label = { Text("In Stock (${products.count { it.stockQuantity >= 5 }})", fontSize = 12.sp) }
        )
        FilterChip(
            selected = selectedFilter == "LOW",
            onClick = { onSelectFilter("LOW") },
            label = { Text("Low Stock (${products.count { it.stockQuantity in 1..4 }})", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFEF3C7),
                selectedLabelColor = KashmirAmber
            )
        )
        FilterChip(
            selected = selectedFilter == "OUT",
            onClick = { onSelectFilter("OUT") },
            label = { Text("Out of Stock (${products.count { it.stockQuantity <= 0 }})", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFFEE2E2),
                selectedLabelColor = KashmirError
            )
        )
    }
}

@Composable
private fun SellerStockCard(
    product: ProductEntity,
    onIncrementStock: () -> Unit,
    onDecrementStock: () -> Unit,
    onOpenStockDialog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            AsyncImage(
                model = if (product.imageUri.isNotBlank()) product.imageUri else "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500",
                contentDescription = product.title,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category,
                    fontSize = 11.sp,
                    color = KashmirTextSecondary
                )
                Text(
                    text = "₹${product.price.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = KashmirDeepGreen
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stock status badge
                Surface(
                    color = when {
                        product.stockQuantity >= 5 -> KashmirMint
                        product.stockQuantity in 1..4 -> Color(0xFFFEF3C7)
                        else -> Color(0xFFFEE2E2)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when {
                            product.stockQuantity >= 5 -> "In Stock: ${product.stockQuantity} units"
                            product.stockQuantity in 1..4 -> "Low Stock: ${product.stockQuantity} left"
                            else -> "Out of Stock"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            product.stockQuantity >= 5 -> KashmirDeepGreen
                            product.stockQuantity in 1..4 -> KashmirAmber
                            else -> KashmirError
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick stock +/- counter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = onDecrementStock,
                        modifier = Modifier.size(30.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = KashmirSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${product.stockQuantity}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { onOpenStockDialog() }
                            .padding(horizontal = 8.dp)
                    )

                    FilledIconButton(
                        onClick = onIncrementStock,
                        modifier = Modifier.size(30.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = KashmirMint)
                    ) {
                        Icon(Icons.Default.Add, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                    }
                }

                TextButton(
                    onClick = onOpenStockDialog,
                    contentPadding = PaddingValues(2.dp)
                ) {
                    Text("Edit Stock", fontSize = 10.sp, color = KashmirForestGreen)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSellerProductForm(
    viewModel: RozgarViewModel,
    onProductAdded: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val platformSettings by viewModel.platformSettings.collectAsState()
    val availableCategories = remember(platformSettings?.customCategoriesCsv) {
        val custom = platformSettings?.customCategoriesCsv?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        if (!custom.isNullOrEmpty()) custom else SELLER_PRODUCT_CATEGORIES
    }

    var selectedCategory by remember(availableCategories) { mutableStateOf(availableCategories.firstOrNull() ?: SELLER_PRODUCT_CATEGORIES[0]) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var condition by remember { mutableStateOf("Brand New (نئی)") }
    var priceText by remember { mutableStateOf("") }
    var stockQuantityText by remember { mutableStateOf("10") }
    var district by remember { mutableStateOf(currentUser?.district ?: "Srinagar") }
    var area by remember { mutableStateOf("") }

    // Multi-sizes selection
    val availableSizeOptions = listOf("Free Size", "S", "M", "L", "XL", "XXL", "Custom")
    val selectedSizes = remember { mutableStateListOf("Free Size") }

    // Multi-colors selection
    val availableColorOptions = listOf("Natural", "Maroon", "Pine Green", "Navy Blue", "Copper", "Walnut", "Black", "White")
    val selectedColors = remember { mutableStateListOf("Natural") }

    // Photos (up to 10 images)
    val photoUrls = remember { mutableStateListOf<String>() }
    var customPhotoUrlInput by remember { mutableStateOf("") }

    // Reselling options
    var isResellable by remember { mutableStateOf(true) }
    var supplierPriceText by remember { mutableStateOf("") }
    var maxMarginText by remember { mutableStateOf("300") }

    var isSubmitting by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "List New Product for Sale",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = KashmirDeepGreen
            )
            Text(
                text = "Upload handicraft, pheran, copperware, or store items. Automatically synced to Firebase Firestore for valley-wide discovery.",
                fontSize = 12.sp,
                color = KashmirTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Product Title *") },
                placeholder = { Text("e.g. Handmade Tilla Embroidery Pheran") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("product_title_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    availableCategories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat, fontSize = 13.sp) },
                            onClick = {
                                selectedCategory = cat
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing & Stock
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                    label = { Text("Selling Price (₹) *") },
                    placeholder = { Text("e.g. 2499") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_price_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = stockQuantityText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) stockQuantityText = it },
                    label = { Text("Initial Stock *") },
                    placeholder = { Text("e.g. 15") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("product_stock_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Photos Management (Up to 10 photos)
            Text("Product Photos (${photoUrls.size}/10)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Select preset Kashmir craft photos or paste your photo URL:", fontSize = 11.sp, color = KashmirTextSecondary)

            Spacer(modifier = Modifier.height(6.dp))

            // Preset photo pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SAMPLE_PRODUCT_PHOTOS.forEach { (url, label) ->
                    AssistChip(
                        onClick = {
                            if (photoUrls.size < 10 && url !in photoUrls) {
                                photoUrls.add(url)
                            }
                        },
                        label = { Text("+ $label", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(12.dp)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Custom URL input
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customPhotoUrlInput,
                    onValueChange = { customPhotoUrlInput = it },
                    label = { Text("Image Link / URI") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (customPhotoUrlInput.isNotBlank() && photoUrls.size < 10) {
                            photoUrls.add(customPhotoUrlInput.trim())
                            customPhotoUrlInput = ""
                        }
                    },
                    modifier = Modifier.background(KashmirMint, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Add, null, tint = KashmirDeepGreen)
                }
            }

            // Photo preview thumbnails
            if (photoUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    photoUrls.forEachIndexed { index, url ->
                        Box(modifier = Modifier.size(60.dp)) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { photoUrls.removeAt(index) },
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sizes Selection
            Text("Available Sizes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableSizeOptions.forEach { size ->
                    FilterChip(
                        selected = size in selectedSizes,
                        onClick = {
                            if (size in selectedSizes) selectedSizes.remove(size) else selectedSizes.add(size)
                        },
                        label = { Text(size, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Colors Selection
            Text("Available Colors", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableColorOptions.forEach { colorName ->
                    FilterChip(
                        selected = colorName in selectedColors,
                        onClick = {
                            if (colorName in selectedColors) selectedColors.remove(colorName) else selectedColors.add(colorName)
                        },
                        label = { Text(colorName, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Product Description & Craft Details") },
                placeholder = { Text("Describe materials, authenticity, care instructions, origin...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Location
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("District") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Area / Market") },
                    placeholder = { Text("e.g. Lal Chowk") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resell & Earn Feature
            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Others to Resell & Earn", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                            Text("Kashmiri youths can share this item and earn a profit margin.", fontSize = 11.sp, color = KashmirTextSecondary)
                        }
                        Switch(
                            checked = isResellable,
                            onCheckedChange = { isResellable = it }
                        )
                    }

                    if (isResellable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = supplierPriceText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) supplierPriceText = it },
                                label = { Text("Supplier Wholesale (₹)") },
                                placeholder = { Text("e.g. 1999") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = maxMarginText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) maxMarginText = it },
                                label = { Text("Max Reseller Margin (₹)") },
                                placeholder = { Text("e.g. 500") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    val stockVal = stockQuantityText.toIntOrNull() ?: 10
                    val suppPrice = supplierPriceText.toDoubleOrNull() ?: (priceVal * 0.8)
                    val margin = maxMarginText.toDoubleOrNull() ?: 300.0

                    if (title.isBlank() || priceVal <= 0.0) {
                        return@Button
                    }

                    isSubmitting = true
                    val primaryPhoto = photoUrls.firstOrNull() ?: "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500"
                    val allPhotosCsv = photoUrls.joinToString(",")

                    viewModel.addSellerProduct(
                        title = title,
                        description = description,
                        category = selectedCategory,
                        culturalProductType = selectedCategory,
                        condition = condition,
                        price = priceVal,
                        stockQuantity = stockVal,
                        availableSizes = selectedSizes.joinToString(", "),
                        availableColors = selectedColors.joinToString(", "),
                        genderTarget = "Unisex",
                        district = district,
                        area = area,
                        imageUri = primaryPhoto,
                        imageUris = allPhotosCsv,
                        isResellable = isResellable,
                        supplierPrice = suppPrice,
                        maxResellerMargin = margin,
                        onComplete = {
                            isSubmitting = false
                            onProductAdded()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_product_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                shape = RoundedCornerShape(10.dp),
                enabled = !isSubmitting && title.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0.0
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Syncing with Firestore...")
                } else {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Product to Marketplace & Cloud", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SellerShopSettingsSection(
    shop: ShopEntity?,
    viewModel: RozgarViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Shop Profile & Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (shop?.isVerified == true) Icons.Default.Verified else Icons.Default.VerifiedUser,
                        null,
                        tint = KashmirEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (shop?.isVerified == true) "Verified Kashmiri Shop Badge" else "Apply for Verified Shop Badge",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = if (shop?.isVerified == true) "Your shop carries the verified badge for maximum customer trust." else "Get a verified badge and priority placement for ₹49/month.",
                            fontSize = 11.sp,
                            color = KashmirTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Shop Details", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Name: ${shop?.shopName ?: "Not registered"}", fontSize = 12.sp)
            Text("Type: ${shop?.shopType ?: "Retail Shop"}", fontSize = 12.sp)
            Text("Location: ${shop?.area ?: "Lal Chowk"}, ${shop?.district ?: "Srinagar"}", fontSize = 12.sp)
            Text("Hours: ${shop?.openingHours ?: "9:00 AM - 8:00 PM"}", fontSize = 12.sp)
            Text("Delivery: All Valley / District Cash on Delivery supported", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { viewModel.navigateTo(Screen.MARKETPLACE) },
                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Storefront, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Public Shop Front", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyOrdersState(filter: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ReceiptLong, null, tint = KashmirTextMuted, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text("No orders in \"$filter\" state", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(
            "Customer orders placed for your products will show here for confirmation, packaging, and dispatch.",
            fontSize = 12.sp,
            color = KashmirTextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
private fun EmptyStockState(onNavigateToAddProduct: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, null, tint = KashmirTextMuted, modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text("No products in this inventory filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text("Add your crafts, pherans, or copperware products to manage live stock.", fontSize = 12.sp, color = KashmirTextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onNavigateToAddProduct,
            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Product")
        }
    }
}

@Composable
private fun SellerPayoutsSection(
    orders: List<OrderEntity>,
    payoutRequests: List<com.example.data.model.PayoutRequestEntity>,
    ledgerEntries: List<com.example.data.model.TransactionLedgerEntity>,
    maskedUpi: String,
    maskedBank: String,
    onOpenBankSetup: () -> Unit,
    onRequestPayout: () -> Unit
) {
    val deliveredOrders = orders.filter { it.orderStatus == "DELIVERED" }
    val totalGrossSales = orders.sumOf { it.totalAmount }
    val commissionDeducted = orders.sumOf { it.platformFeeAmount }
    val totalSupplierPayable = orders.sumOf { it.supplierShareAmount }
    val totalPaidOut = payoutRequests.filter { it.status == "PAID" }.sumOf { it.amount }
    val pendingPayout = payoutRequests.filter { it.status == "PENDING" }.sumOf { it.amount }
    val availableBalance = (deliveredOrders.sumOf { it.supplierShareAmount } - totalPaidOut - pendingPayout).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Financial Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = KashmirMint,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Available Payout", fontSize = 10.sp, color = KashmirForestGreen)
                    Text("₹${availableBalance.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                }
            }
            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pending Payout", fontSize = 10.sp, color = KashmirDeepGreen)
                    Text("₹${pendingPayout.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                }
            }
            Surface(
                color = KashmirSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Completed", fontSize = 10.sp, color = KashmirTextSecondary)
                    Text("₹${totalPaidOut.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Payout Request Action
        Button(
            onClick = onRequestPayout,
            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("request_payout_button")
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Request Settlement Payout (₹${availableBalance.toInt()})", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bank Account & UPI Setup Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, KashmirCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payout Settlement Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirDeepGreen)
                    }
                    TextButton(onClick = onOpenBankSetup) {
                        Text("Edit", fontSize = 12.sp, color = KashmirEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = KashmirSurfaceGreen.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Linked UPI ID:", fontSize = 11.sp, color = KashmirTextSecondary)
                            Text(maskedUpi, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KashmirDeepGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Linked Bank:", fontSize = 11.sp, color = KashmirTextSecondary)
                            Text(maskedBank, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KashmirDeepGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Security:", fontSize = 10.sp, color = KashmirTextMuted)
                            Text("Masked & encrypted for privacy", fontSize = 10.sp, color = KashmirForestGreen)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Financial Metrics Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, KashmirCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Lifetime Earnings & Deductions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirDeepGreen)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Order Sales:", fontSize = 12.sp, color = KashmirTextSecondary)
                    Text("₹${totalGrossSales.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Platform Commission Deducted (5%):", fontSize = 12.sp, color = KashmirError)
                    Text("-₹${commissionDeducted.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirError)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Refund Adjustments:", fontSize = 12.sp, color = KashmirTextSecondary)
                    Text("₹0.00 (Zero Disputes)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirForestGreen)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = KashmirCardBorder)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Seller Payable (95%):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    Text("₹${totalSupplierPayable.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction History
        Text(
            text = "Settlement Transaction History (${orders.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KashmirDeepGreen
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (orders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    "No completed customer sales yet. When customers order your crafts or produce, transparent earnings and payouts will appear here.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    color = KashmirTextSecondary
                )
            }
        } else {
            orders.forEach { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KashmirCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = order.orderNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KashmirDeepGreen
                            )
                            KashmirBadge(
                                text = order.orderStatus,
                                containerColor = if (order.orderStatus == "DELIVERED") KashmirMint else KashmirGold.copy(alpha = 0.2f),
                                contentColor = KashmirDeepGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(order.productTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gross: ₹${order.totalAmount.toInt()}", fontSize = 11.sp, color = KashmirTextMuted)
                            Text("Fee (5%): ₹${order.platformFeeAmount.toInt()}", fontSize = 11.sp, color = KashmirError)
                            Text("Net: ₹${order.supplierShareAmount.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Payout Requests List
        Text(
            text = "Payout Requests (${payoutRequests.size})",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KashmirDeepGreen
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (payoutRequests.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    "No withdrawal requests yet. You can request payouts anytime using the button above.",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 12.sp,
                    color = KashmirTextSecondary
                )
            }
        } else {
            payoutRequests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (req.status == "PENDING") KashmirGold else KashmirCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("₹${req.amount.toInt()} to ${req.upiIdMasked}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Role: ${req.userRole} • Payout Status: ${req.status}", fontSize = 11.sp, color = KashmirTextMuted)
                        }
                        KashmirBadge(
                            text = req.status,
                            containerColor = if (req.status == "PAID") KashmirMint else KashmirGold.copy(alpha = 0.25f),
                            contentColor = KashmirDeepGreen
                        )
                    }
                }
            }
        }
    }
}

