package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(viewModel: RozgarViewModel) {
    val product by viewModel.selectedProduct.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }
    var showResellDialog by remember { mutableStateOf(false) }

    val p = product
    if (p == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    val allPhotos = remember(p.imageUri, p.imageUris) {
        val list = mutableListOf<String>()
        if (p.imageUri.isNotBlank()) list.add(p.imageUri)
        if (p.imageUris.isNotBlank()) {
            p.imageUris.split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it !in list }
                .let { list.addAll(it) }
        }
        list
    }
    var selectedPhotoIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleSave("PRODUCT", p.id, p.title, "₹${p.price.toInt()} • ${p.district}")
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Product")
                    }
                    IconButton(onClick = { showReportDialog = true }) {
                        Icon(Icons.Default.Report, contentDescription = "Report", tint = KashmirError)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KashmirDeepGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "₹${p.price.toInt()}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = if (p.isNegotiable) "Negotiable" else "Fixed",
                            fontSize = 10.sp,
                            color = KashmirTextSecondary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.startConversationWith(
                                    targetUserId = p.sellerUserId,
                                    targetUserName = p.sellerName,
                                    subject = "Item Inquiry: ${p.title}"
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                            border = BorderStroke(1.dp, KashmirEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("contact_seller_button")
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Chat", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showResellDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                            border = BorderStroke(1.dp, KashmirGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("resell_product_button")
                        ) {
                            Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp), tint = KashmirWarmGold)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Resell", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showOrderDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("order_product_button")
                        ) {
                            Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(14.dp), tint = KashmirGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Order (COD)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Visual Header / Product Image Gallery
            item {
                if (allPhotos.isNotEmpty()) {
                    val currentPhoto = allPhotos.getOrElse(selectedPhotoIndex) { allPhotos.first() }
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            RozgarAsyncImage(
                                model = currentPhoto,
                                contentDescription = p.title,
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(16.dp),
                                defaultPlaceholderIcon = Icons.Default.LocalMall
                            )

                            // Photo counter badge (e.g. 1/5)
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${selectedPhotoIndex + 1} / ${allPhotos.size} photos",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Thumbnail strip if multiple photos
                        if (allPhotos.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            androidx.compose.foundation.lazy.LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(allPhotos.size) { idx ->
                                    val photo = allPhotos[idx]
                                    val isSelected = idx == selectedPhotoIndex
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) KashmirEmerald else KashmirCardBorder,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedPhotoIndex = idx }
                                    ) {
                                        RozgarAsyncImage(
                                            model = photo,
                                            contentDescription = "Thumbnail $idx",
                                            modifier = Modifier.fillMaxSize(),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(KashmirSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (p.isSecondHand) Icons.Default.Autorenew else Icons.Default.LocalMall,
                                contentDescription = null,
                                tint = KashmirEmerald,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = p.category,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (p.isSecondHand) {
                                KashmirBadge(
                                    text = "SECOND-HAND",
                                    containerColor = KashmirAmber.copy(alpha = 0.25f),
                                    contentColor = KashmirSaffron
                                )
                            }
                            KashmirBadge(text = p.condition, containerColor = KashmirMint)
                            KashmirBadge(text = "${p.quantity} available", containerColor = KashmirSurfaceGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = p.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Listed by: ${p.sellerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = KashmirEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${p.area}, ${p.district}, Kashmir",
                                style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Safe Trading Guarantee
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Private Seller: Contact details are secured. Reach seller through voice-first chat.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Item Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(p.description, style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary))
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("Fraudulent Listing") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Listing", color = KashmirError, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Fraudulent Listing", "Counterfeit Kashmiri Good", "Wrong Pricing", "Spam").forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reason = r }
                        ) {
                            RadioButton(selected = reason == r, onClick = { reason = r })
                            Text(r, fontSize = 13.sp)
                        }
                    }
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Details (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitReport("PRODUCT", p.id, p.title, reason, details)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirError)
                ) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showOrderDialog) {
        ProductOrderDialog(
            viewModel = viewModel,
            product = p,
            onDismiss = { showOrderDialog = false }
        )
    }

    if (showResellDialog) {
        ProductResellDialog(
            viewModel = viewModel,
            product = p,
            onDismiss = { showResellDialog = false }
        )
    }
}

@Composable
fun ProductOrderDialog(
    viewModel: RozgarViewModel,
    product: com.example.data.model.ProductEntity,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf("Standard / Free Size") }
    var deliveryAddress by remember { mutableStateOf(currentUser?.area?.let { "$it, ${currentUser?.district ?: "Kashmir"}" } ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    val platformSettings by viewModel.platformSettings.collectAsState()
    val commissionPercent = platformSettings?.productCommissionPercent ?: 5.0
    val itemSubtotal = product.price * quantity
    val platformFee = (itemSubtotal * commissionPercent) / 100.0
    val sellerPayout = itemSubtotal - platformFee

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Cash on Delivery Order",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Text(
                        text = product.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = KashmirTextPrimary,
                        maxLines = 1
                    )
                }

                // Price and quantity
                item {
                    Surface(
                        color = KashmirSurfaceGreen,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Unit Price", fontSize = 11.sp, color = KashmirTextMuted)
                                Text("₹${product.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (quantity > 1) quantity-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, null, tint = KashmirForestGreen)
                                }
                                Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 6.dp))
                                IconButton(
                                    onClick = { if (quantity < product.stockQuantity) quantity++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, null, tint = KashmirForestGreen)
                                }
                            }
                        }
                    }
                }

                // Size selection if apparel / traditional wear
                if (product.category.contains("Clothing", ignoreCase = true) || product.category.contains("Pheran", ignoreCase = true)) {
                    item {
                        Text("Select Size", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        val sizes = listOf("S", "M", "L", "XL", "XXL", "Free Size")
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sizes.forEach { size ->
                                FilterChip(
                                    selected = selectedSize == size,
                                    onClick = { selectedSize = size },
                                    label = { Text(size, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone (Kashmir) *") },
                        placeholder = { Text("e.g. 9419012345") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Delivery Address *") },
                        placeholder = { Text("e.g. House No. 22, Near Mosque, Srinagar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Commission breakdown
                item {
                    Surface(
                        color = KashmirSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal ($quantity items):", fontSize = 12.sp, color = KashmirTextSecondary)
                                Text("₹${itemSubtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Method:", fontSize = 11.sp, color = KashmirForestGreen)
                                Text("Cash On Delivery", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Artisan / Seller Payout:", fontSize = 10.sp, color = KashmirTextMuted)
                                Text("₹${sellerPayout.toInt()} (Platform: ₹${platformFee.toInt()})", fontSize = 10.sp, color = KashmirTextMuted)
                            }
                        }
                    }
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        Text(errorMessage, color = KashmirError, fontSize = 12.sp)
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel", color = KashmirTextSecondary) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (deliveryAddress.isBlank() || phone.isBlank()) {
                                    errorMessage = "Please enter phone and delivery address."
                                    return@Button
                                }
                                viewModel.placeProductOrder(
                                    product = product,
                                    quantity = quantity,
                                    selectedSize = selectedSize,
                                    deliveryAddress = deliveryAddress,
                                    customerPhone = phone,
                                    onComplete = { onDismiss() }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("confirm_place_order_button")
                        ) {
                            Text("Confirm Order (₹${itemSubtotal.toInt()})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductResellDialog(
    viewModel: RozgarViewModel,
    product: com.example.data.model.ProductEntity,
    onDismiss: () -> Unit
) {
    var marginText by remember { mutableStateOf("250") }
    val supplierPrice = product.price
    val margin = marginText.toDoubleOrNull() ?: 0.0
    val customerPrice = supplierPrice + margin

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Resell Kashmiri Product",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
                Text(
                    text = "Earn margin by sharing authentic Kashmiri goods with your network.",
                    fontSize = 12.sp,
                    color = KashmirTextSecondary
                )

                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(product.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Wholesale / Supplier Price: ₹${supplierPrice.toInt()}", fontSize = 11.sp, color = KashmirTextSecondary)
                    }
                }

                OutlinedTextField(
                    value = marginText,
                    onValueChange = { marginText = it },
                    label = { Text("Your Margin (₹ Profit) *") },
                    placeholder = { Text("e.g. 250") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Surface(
                    color = KashmirMint.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Final Customer Price:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        Text("₹${customerPrice.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.resellProduct(
                                product = product,
                                resellerMargin = margin,
                                onComplete = { onDismiss() }
                            )
                        },
                        enabled = margin > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Resell Link", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

