package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ShopEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen

private val QUICK_CATEGORY_ICONS = listOf(
    "All Categories" to (Icons.Default.Storefront to "All"),
    "Pherans & Traditional Wear" to (Icons.Default.Checkroom to "Pheran"),
    "Kashmiri Cultural Clothing" to (Icons.Default.DryCleaning to "Clothing"),
    "Kangri & Winter Crafts" to (Icons.Default.LocalFireDepartment to "Kangri"),
    "Copper Utensils & Samovars" to (Icons.Default.Coffee to "Copper"),
    "Walnut Wood & Carvings" to (Icons.Default.Weekend to "Walnut"),
    "Kashmir Saffron & Spices" to (Icons.Default.Spa to "Saffron"),
    "Walnuts, Kernels & Seeds" to (Icons.Default.Grain to "Dry Fruit"),
    "Kashmiri Handicrafts & Shawls" to (Icons.Default.Gesture to "Shawls"),
    "Topi, Caps & Headwear" to (Icons.Default.Face to "Topi"),
    "Electronics & Mobile Phones" to (Icons.Default.PhoneAndroid to "Gadgets"),
    "Second-Hand / Pre-Loved Marketplace" to (Icons.Default.Autorenew to "Pre-Loved")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val allProducts by viewModel.allProducts.collectAsState()
    val secondHandProducts by viewModel.secondHandProducts.collectAsState()
    val allShops by viewModel.allShops.collectAsState()
    val supplierOrders by viewModel.supplierOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isMarketplaceLoading by viewModel.isMarketplaceLoading.collectAsState()

    // 4 Tabs: 0: Shop (Amazon/Marketplace), 1: Resell & Earn (Meesho), 2: Pre-Loved (Second-Hand), 3: Seller Hub
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedSortOption by remember { mutableStateOf("Featured") } // "Featured", "Price: Low to High", "Price: High to Low", "Top Rated"
    var selectedPriceRange by remember { mutableStateOf("All Prices") } // "All Prices", "Under ₹1,000", "₹1,000 - ₹5,000", "₹5,000+"

    // Dialog States
    var showListProductDialog by remember { mutableStateOf(false) }
    var showCreateShopDialog by remember { mutableStateOf(false) }
    var productForQuickOrder by remember { mutableStateOf<ProductEntity?>(null) }
    var productForResellerOrder by remember { mutableStateOf<ProductEntity?>(null) }

    val productsToDisplay = remember(activeTab, allProducts, secondHandProducts) {
        when (activeTab) {
            1 -> allProducts.filter { it.isResellable || it.maxResellerMargin > 0.0 }
            2 -> secondHandProducts
            else -> allProducts
        }
    }

    val filteredProducts = remember(
        productsToDisplay,
        searchQuery,
        selectedDistrict,
        selectedCategory,
        selectedSortOption,
        selectedPriceRange
    ) {
        var list = productsToDisplay.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.category.contains(searchQuery, ignoreCase = true) ||
                p.area.contains(searchQuery, ignoreCase = true) ||
                p.district.contains(searchQuery, ignoreCase = true) ||
                p.sellerName.contains(searchQuery, ignoreCase = true) ||
                p.culturalProductType.contains(searchQuery, ignoreCase = true)

            val matchesDistrict = selectedDistrict == "All Districts" ||
                p.district.equals(selectedDistrict, ignoreCase = true)

            val matchesCat = selectedCategory == "All Categories" ||
                p.category.equals(selectedCategory, ignoreCase = true) ||
                (selectedCategory == "Pherans & Traditional Wear" && p.culturalProductType.contains("Pheran", ignoreCase = true)) ||
                (selectedCategory == "Kangri & Winter Crafts" && p.culturalProductType.contains("Kangri", ignoreCase = true))

            val matchesPrice = when (selectedPriceRange) {
                "Under ₹1,000" -> p.price < 1000.0
                "₹1,000 - ₹5,000" -> p.price in 1000.0..5000.0
                "₹5,000+" -> p.price > 5000.0
                else -> true
            }

            matchesQuery && matchesDistrict && matchesCat && matchesPrice
        }

        list = when (selectedSortOption) {
            "Price: Low to High" -> list.sortedBy { it.price }
            "Price: High to Low" -> list.sortedByDescending { it.price }
            "Top Rated" -> list.sortedByDescending { it.stockQuantity }
            else -> list.sortedByDescending { it.id }
        }

        list
    }

    val trendingProducts = remember(allProducts) {
        allProducts.take(6)
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showListProductDialog = true },
                icon = { Icon(Icons.Default.Sell, null) },
                text = { Text("List Product", fontWeight = FontWeight.Bold) },
                containerColor = KashmirDeepGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("marketplace_sell_fab")
                    .padding(bottom = 60.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. TOP HEADER & SEARCH BAR
            item {
                Surface(
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rozgar Kashmir Marketplace",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Authentic Valley Handicrafts, Pherans, Dry Fruits & Community Reselling",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.refreshMarketplaceData() },
                                enabled = !isMarketplaceLoading,
                                modifier = Modifier.testTag("refresh_marketplace_button")
                            ) {
                                if (isMarketplaceLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = KashmirEmerald
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, "Refresh Marketplace", tint = KashmirEmerald)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modern Universal Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Search Pheran, Kangri, Saffron, Walnuts, Shop...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("market_search_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4 Primary Multi-Vendor Tabs
                        TabRow(
                            selectedTabIndex = activeTab,
                            containerColor = KashmirMint.copy(alpha = 0.5f),
                            indicator = {},
                            divider = {},
                            modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        ) {
                            val tabs = listOf(
                                Triple(0, "Shop (${allProducts.size})", Icons.Default.Storefront),
                                Triple(1, "Resell & Earn", Icons.Default.Paid),
                                Triple(2, "Pre-Loved", Icons.Default.Autorenew),
                                Triple(3, "Seller Hub", Icons.Default.Dashboard)
                            )
                            tabs.forEach { (index, title, icon) ->
                                val selected = activeTab == index
                                Tab(
                                    selected = selected,
                                    onClick = { activeTab = index },
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                icon,
                                                null,
                                                modifier = Modifier.size(13.dp),
                                                tint = if (selected) KashmirDeepGreen else KashmirTextMuted
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                title,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) KashmirDeepGreen else KashmirTextMuted,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) Color.White else Color.Transparent)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // TAB 0: AMAZON / MEESHO SHOPPING EXPERIENCE
            // =========================================================================
            if (activeTab == 0) {
                // Category Visual Circular Icons Strip
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = "Explore Kashmir Categories",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KashmirDeepGreen,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(QUICK_CATEGORY_ICONS) { (fullCat, pair) ->
                                val (icon, shortLabel) = pair
                                val isSelected = selectedCategory == fullCat
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.selectedCategory.value = if (isSelected) "All Categories" else fullCat
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) KashmirEmerald else KashmirMint.copy(alpha = 0.6f))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) KashmirDeepGreen else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = shortLabel,
                                            tint = if (isSelected) Color.White else KashmirDeepGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = shortLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) KashmirDeepGreen else KashmirTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // District Filter Chips
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedDistrict == "All Districts",
                                onClick = { viewModel.selectedDistrict.value = "All Districts" },
                                label = { Text("All 10 Districts", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KashmirDeepGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(KASHMIR_DISTRICTS) { district ->
                            FilterChip(
                                selected = selectedDistrict == district,
                                onClick = { viewModel.selectedDistrict.value = district },
                                label = { Text(district, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KashmirMint,
                                    selectedLabelColor = KashmirDeepGreen
                                )
                            )
                        }
                    }
                }

                // Promotional Kashmiri Deals Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = KashmirSaffron,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "100% AUTHENTIC VALLEY CRAFTS",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Handmade Pherans & Dry Fruits",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Direct from Srinagar, Pampore & Shopian artisans. Cash on Delivery across Kashmir.",
                                    fontSize = 11.sp,
                                    color = KashmirMint.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Verified, null, tint = KashmirGold, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }

                // Featured Kashmiri Shops & Top Artisans
                if (allShops.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Featured Kashmiri Shops & Guilds",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = KashmirDeepGreen
                                )
                                Text(
                                    text = "${allShops.size} registered",
                                    fontSize = 11.sp,
                                    color = KashmirTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(allShops) { shop ->
                                    FeaturedShopCard(
                                        shop = shop,
                                        onClick = {
                                            viewModel.searchQuery.value = shop.shopName
                                            Toast.makeText(context, "Browsing ${shop.shopName}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Trending Kashmiri Products Row
                if (trendingProducts.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All Categories") {
                    item {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = "Trending This Season",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = KashmirDeepGreen,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(trendingProducts) { product ->
                                    TrendingProductMiniCard(
                                        product = product,
                                        onView = { viewModel.viewProductDetail(product) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Sort & Price Range Filter Row
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KashmirCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            // Sort Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Sort, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sort:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                val sortOptions = listOf("Featured", "Price: Low to High", "Price: High to Low", "Top Rated")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(sortOptions) { opt ->
                                        FilterChip(
                                            selected = selectedSortOption == opt,
                                            onClick = { selectedSortOption = opt },
                                            label = { Text(opt, fontSize = 10.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Price Filter Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CurrencyRupee, null, tint = KashmirForestGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Price:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                val priceFilters = listOf("All Prices", "Under ₹1,000", "₹1,000 - ₹5,000", "₹5,000+")
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    items(priceFilters) { pFilter ->
                                        FilterChip(
                                            selected = selectedPriceRange == pFilter,
                                            onClick = { selectedPriceRange = pFilter },
                                            label = { Text(pFilter, fontSize = 10.sp) },
                                            modifier = Modifier.height(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Feed Section Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Products (${filteredProducts.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KashmirDeepGreen
                        )
                        if (selectedCategory != "All Categories" || selectedDistrict != "All Districts") {
                            TextButton(onClick = {
                                viewModel.selectedCategory.value = "All Categories"
                                viewModel.selectedDistrict.value = "All Districts"
                            }) {
                                Text("Clear Filters", fontSize = 11.sp, color = KashmirEmerald)
                            }
                        }
                    }
                }

                // Product Cards Grid/Feed
                if (filteredProducts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ProductionQuantityLimits, null, tint = KashmirTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No products match your search or filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Try clearing filters or changing districts", fontSize = 12.sp, color = KashmirTextSecondary)
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        MarketProductCard(
                            product = product,
                            onViewDetail = { viewModel.viewProductDetail(product) },
                            onQuickOrder = { productForQuickOrder = product },
                            onToggleWishlist = {
                                viewModel.toggleSave("PRODUCT", product.id, product.title, "₹${product.price.toInt()} • ${product.district}")
                            }
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 1: RESELL & EARN MARKETPLACE (MEESHO STYLE)
            // =========================================================================
            else if (activeTab == 1) {
                item {
                    // Reselling Explainer Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KashmirGold)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(KashmirGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Zero-Investment Reselling", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                                    Text("Start your home business with genuine Kashmiri crafts", fontSize = 11.sp, color = KashmirTextSecondary)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = KashmirSurfaceGreen,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ResellStep(step = "1", title = "Select", subtitle = "Pick wholesale item")
                                    ResellStep(step = "2", title = "Share", subtitle = "WhatsApp with margin")
                                    ResellStep(step = "3", title = "Earn", subtitle = "Customer pays COD")
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Wholesale Products Eligible for Reselling (${filteredProducts.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = KashmirDeepGreen,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (filteredProducts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Storefront, null, tint = KashmirTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No reselling products found", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        ResellerProductCard(
                            product = product,
                            onShareWhatsApp = {
                                val supplierBase = if (product.supplierPrice > 0.0) product.supplierPrice else (product.price * 0.8)
                                val recommendedMargin = if (product.maxResellerMargin > 0.0) product.maxResellerMargin else (product.price * 0.25)
                                val retailPrice = (supplierBase + recommendedMargin).toInt()
                                val shareText = """
                                    *${product.title}*
                                    Authentic Kashmiri handcrafted item.
                                    • Price: ₹$retailPrice (Free Home Delivery)
                                    • Cash on Delivery Available across Kashmir
                                    • High quality verified craft.
                                    Reply to order today!
                                """.trimIndent()

                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share on WhatsApp / Socials"))
                            },
                            onPlaceCustomerOrder = {
                                productForResellerOrder = product
                            },
                            onViewDetail = {
                                viewModel.viewProductDetail(product)
                            }
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 2: PRE-LOVED (SECOND-HAND) MARKETPLACE
            // =========================================================================
            else if (activeTab == 2) {
                item {
                    Surface(
                        color = KashmirWarmGold.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, KashmirGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Autorenew, null, tint = KashmirSaffron, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Kashmir Pre-Loved Community", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                                Text("Buy & sell second-hand appliances, laptops, farm tools, and vehicles across all districts.", fontSize = 11.sp, color = KashmirTextSecondary)
                            }
                        }
                    }
                }

                if (filteredProducts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Autorenew, null, tint = KashmirTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No second-hand items listed currently", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap 'List Product' to sell your pre-owned items!", fontSize = 12.sp, color = KashmirTextSecondary)
                        }
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { product ->
                        MarketProductCard(
                            product = product,
                            onViewDetail = { viewModel.viewProductDetail(product) },
                            onQuickOrder = { productForQuickOrder = product },
                            onToggleWishlist = {
                                viewModel.toggleSave("PRODUCT", product.id, product.title, "₹${product.price.toInt()} • ${product.district}")
                            }
                        )
                    }
                }
            }

            // =========================================================================
            // TAB 3: SELLER HUB & ORDERS DASHBOARD
            // =========================================================================
            else if (activeTab == 3) {
                val userShop = allShops.firstOrNull { it.ownerUserId == currentUser?.id }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = userShop?.shopName ?: "Artisan & Seller Portal",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirDeepGreen
                                        )
                                    )
                                    Text(
                                        text = if (userShop != null) "${userShop.shopType} • ${userShop.district}" else "Create a shop to sell to customers valley-wide",
                                        fontSize = 12.sp,
                                        color = KashmirTextSecondary
                                    )
                                }
                                Icon(Icons.Default.Storefront, null, tint = KashmirEmerald, modifier = Modifier.size(28.dp))
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.SELLER_DASHBOARD) },
                                    modifier = Modifier.weight(1.2f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Dashboard, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Full Seller Dashboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                if (userShop == null) {
                                    OutlinedButton(
                                        onClick = { showCreateShopDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Register Shop", fontSize = 12.sp)
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { showListProductDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Add Product", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Customer Orders Received (${supplierOrders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                if (supplierOrders.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Inventory2, null, tint = KashmirTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No customer orders yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "When customers buy your products via Cash on Delivery, orders appear here for packing & dispatch.",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    items(supplierOrders, key = { it.id }) { order ->
                        SellerSimpleOrderCard(
                            order = order,
                            onUpdateStatus = { targetStatus ->
                                viewModel.updateOrderStatus(order.id, targetStatus, "Status updated by seller")
                            }
                        )
                    }
                }
            }
        }
    }

    // =========================================================================
    // DIALOGS
    // =========================================================================

    // 1. Quick Customer Buy (Cash on Delivery) Dialog
    productForQuickOrder?.let { prod ->
        QuickOrderDialog(
            product = prod,
            currentUser = currentUser,
            onDismiss = { productForQuickOrder = null },
            onConfirmOrder = { qty, size, color, address, phone ->
                viewModel.placeProductOrder(
                    product = prod,
                    quantity = qty,
                    selectedSize = size,
                    selectedColor = color,
                    deliveryAddress = address,
                    customerPhone = phone,
                    onComplete = {
                        productForQuickOrder = null
                    }
                )
            }
        )
    }

    // 2. Reseller Customer Order Dialog (Meesho Mode)
    productForResellerOrder?.let { prod ->
        ResellerOrderDialog(
            product = prod,
            currentUser = currentUser,
            onDismiss = { productForResellerOrder = null },
            onConfirmOrder = { margin, qty, name, phone, address, district, area, size, color, instructions ->
                viewModel.placeResellerCustomerOrder(
                    product = prod,
                    resellerMargin = margin,
                    quantity = qty,
                    customerName = name,
                    customerPhone = phone,
                    deliveryAddress = address,
                    customerDistrict = district,
                    customerArea = area,
                    selectedSize = size,
                    selectedColor = color,
                    deliveryInstructions = instructions,
                    onComplete = {
                        productForResellerOrder = null
                    }
                )
            }
        )
    }

    // 3. Post Product Dialog
    if (showListProductDialog) {
        PostProductDialog(
            viewModel = viewModel,
            onDismiss = { showListProductDialog = false }
        )
    }

    // 4. Create Shop Dialog
    if (showCreateShopDialog) {
        CreateShopDialog(
            viewModel = viewModel,
            onDismiss = { showCreateShopDialog = false }
        )
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPONENTS
// -----------------------------------------------------------------------------

@Composable
private fun ResellStep(step: String, title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(KashmirDeepGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(step, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = KashmirDeepGreen)
        Text(subtitle, fontSize = 9.sp, color = KashmirTextSecondary)
    }
}

@Composable
fun FeaturedShopCard(shop: ShopEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KashmirMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Storefront, null, tint = KashmirDeepGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = shop.shopName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.Verified, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
            }
            Text(
                text = "${shop.area}, ${shop.district}",
                fontSize = 10.sp,
                color = KashmirTextSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, null, tint = KashmirWarmGold, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("4.9", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
            }
        }
    }
}

@Composable
fun TrendingProductMiniCard(product: ProductEntity, onView: () -> Unit) {
    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable { onView() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column {
            if (product.imageUri.isNotBlank()) {
                RozgarAsyncImage(
                    model = product.imageUri,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(85.dp),
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
                    defaultPlaceholderIcon = Icons.Default.LocalMall
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "₹${product.price.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = KashmirDeepGreen
                )
            }
        }
    }
}

@Composable
fun MarketProductCard(
    product: ProductEntity,
    onViewDetail: () -> Unit,
    onQuickOrder: () -> Unit = onViewDetail,
    onToggleWishlist: () -> Unit = {},
    onContactSeller: () -> Unit = onViewDetail
) {
    val estimatedMrp = (product.price * 1.25).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onViewDetail() }
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (product.imageUri.isNotBlank()) {
                    RozgarAsyncImage(
                        model = product.imageUri,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
                        defaultPlaceholderIcon = if (product.isSecondHand) Icons.Default.Autorenew else Icons.Default.LocalMall
                    )
                }

                // Badges top row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (product.isSecondHand) {
                            Surface(
                                color = KashmirSaffron,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "PRE-LOVED",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = KashmirEmerald,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "20% OFF",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (product.culturalProductType.isNotBlank()) {
                            Surface(
                                color = KashmirGold,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    product.culturalProductType.uppercase(),
                                    color = KashmirDeepGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Wishlist heart button
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onToggleWishlist() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FavoriteBorder, null, tint = KashmirDeepGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // Title & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = KashmirTextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, null, tint = KashmirForestGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = product.sellerName,
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, null, tint = KashmirEmerald, modifier = Modifier.size(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${product.price.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = KashmirDeepGreen
                        )
                        if (!product.isSecondHand) {
                            Text(
                                text = "₹$estimatedMrp",
                                fontSize = 10.sp,
                                color = KashmirTextMuted,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Location & Rating Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = KashmirTextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("${product.area}, ${product.district}", fontSize = 11.sp, color = KashmirTextSecondary)
                    }

                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = KashmirWarmGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("4.8", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewDetail,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("View Details", fontSize = 12.sp, color = KashmirDeepGreen)
                    }

                    Button(
                        onClick = onQuickOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(14.dp), tint = KashmirGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buy (COD)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ResellerProductCard(
    product: ProductEntity,
    onShareWhatsApp: () -> Unit,
    onPlaceCustomerOrder: () -> Unit,
    onViewDetail: () -> Unit
) {
    val supplierBase = if (product.supplierPrice > 0.0) product.supplierPrice else (product.price * 0.8)
    val maxMargin = if (product.maxResellerMargin > 0.0) product.maxResellerMargin else (product.price * 0.3)
    val retailPrice = (supplierBase + maxMargin).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (product.imageUri.isNotBlank()) {
                    RozgarAsyncImage(
                        model = product.imageUri,
                        contentDescription = product.title,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        defaultPlaceholderIcon = Icons.Default.LocalMall
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Supplier: ${product.sellerName} (${product.district})",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ELIGIBLE FOR RESELLING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing and Profit Strip
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
                        Text("Wholesale Base", fontSize = 10.sp, color = KashmirTextSecondary)
                        Text("₹${supplierBase.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                    }
                    Column {
                        Text("Max Retail Cap", fontSize = 10.sp, color = KashmirTextSecondary)
                        Text("₹$retailPrice", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirTextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Your Potential Profit", fontSize = 10.sp, color = KashmirForestGreen, fontWeight = FontWeight.Bold)
                        Text("Up to ₹${maxMargin.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = KashmirSaffron)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reseller Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShareWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(15.dp), tint = KashmirForestGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPlaceCustomerOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(15.dp), tint = KashmirGold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Order for Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SellerSimpleOrderCard(
    order: OrderEntity,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                Surface(
                    color = when (order.orderStatus) {
                        "DELIVERED" -> KashmirMint
                        "DISPATCHED" -> KashmirWarmGold.copy(alpha = 0.3f)
                        "PREPARING" -> KashmirSurfaceGreen
                        else -> KashmirCardBorder
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        order.orderStatus,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = KashmirDeepGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(order.productTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Customer: ${order.customerName} • Phone: ${order.customerPhone}", fontSize = 11.sp, color = KashmirTextSecondary)
            Text("Address: ${order.customerAddress}, ${order.customerDistrict}", fontSize = 11.sp, color = KashmirTextMuted)
            Text("Total: ₹${order.totalAmount.toInt()} (COD)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirDeepGreen)

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (order.orderStatus in listOf("ORDERED", "PENDING")) {
                    Button(
                        onClick = { onUpdateStatus("CONFIRMED") },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm Order", fontSize = 11.sp)
                    }
                } else if (order.orderStatus == "CONFIRMED") {
                    Button(
                        onClick = { onUpdateStatus("PREPARING") },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirForestGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark Packed", fontSize = 11.sp)
                    }
                } else if (order.orderStatus == "PREPARING") {
                    Button(
                        onClick = { onUpdateStatus("DISPATCHED") },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Dispatch", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// CHECKOUT & RESELLER ORDER DIALOGS
// -----------------------------------------------------------------------------

@Composable
fun QuickOrderDialog(
    product: ProductEntity,
    currentUser: com.example.data.model.UserEntity?,
    onDismiss: () -> Unit,
    onConfirmOrder: (qty: Int, size: String, color: String, address: String, phone: String) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }
    var selectedSize by remember { mutableStateOf(product.availableSizes.split(",").firstOrNull()?.trim() ?: "") }
    var selectedColor by remember { mutableStateOf(product.availableColors.split(",").firstOrNull()?.trim() ?: "") }
    var address by remember { mutableStateOf(currentUser?.area ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var errorMsg by remember { mutableStateOf("") }

    val totalAmount = (product.price * quantity).toInt()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cash on Delivery Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(product.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Price: ₹${product.price.toInt()} per piece", fontSize = 12.sp, color = KashmirTextSecondary)

                Spacer(modifier = Modifier.height(10.dp))

                // Quantity selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Quantity:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(Icons.Default.RemoveCircleOutline, null, tint = KashmirDeepGreen)
                    }
                    Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = { if (quantity < product.stockQuantity) quantity++ }) {
                        Icon(Icons.Default.AddCircleOutline, null, tint = KashmirDeepGreen)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone *") },
                    placeholder = { Text("e.g. 9419012345") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Complete Delivery Address *") },
                    placeholder = { Text("House/Shop, Mohalla, Tehsil, District") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = KashmirMint.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total (Cash on Delivery):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹$totalAmount", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = KashmirDeepGreen)
                        }
                        Text("Zero advance payment. Inspect item upon delivery.", fontSize = 10.sp, color = KashmirTextSecondary)
                    }
                }

                if (errorMsg.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMsg, color = KashmirError, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (phone.isBlank() || address.isBlank()) {
                            errorMsg = "Please enter contact phone and delivery address."
                            return@Button
                        }
                        onConfirmOrder(quantity, selectedSize, selectedColor, address, phone)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Confirm Cash on Delivery Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ResellerOrderDialog(
    product: ProductEntity,
    currentUser: com.example.data.model.UserEntity?,
    onDismiss: () -> Unit,
    onConfirmOrder: (
        margin: Double,
        qty: Int,
        name: String,
        phone: String,
        address: String,
        district: String,
        area: String,
        size: String,
        color: String,
        instructions: String
    ) -> Unit
) {
    val supplierBase = if (product.supplierPrice > 0.0) product.supplierPrice else (product.price * 0.8)
    val maxMargin = if (product.maxResellerMargin > 0.0) product.maxResellerMargin else (product.price * 0.3)

    var marginInput by remember { mutableStateOf(maxMargin.toInt().toString()) }
    var quantity by remember { mutableIntStateOf(1) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var customerDistrict by remember { mutableStateOf(product.district) }
    var customerArea by remember { mutableStateOf(product.area) }
    var instructions by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    val currentMargin = marginInput.toDoubleOrNull() ?: 0.0
    val customerUnitPrice = supplierBase + currentMargin
    val totalCustomerAmount = (customerUnitPrice * quantity).toInt()
    val totalResellerProfit = (currentMargin * quantity).toInt()

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(18.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Place Order for Customer", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    }
                    Text("You earn margin directly when customer pays COD", fontSize = 11.sp, color = KashmirTextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Profit breakdown card
                    Surface(
                        color = KashmirSurfaceGreen,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Wholesale Base: ₹${supplierBase.toInt()}", fontSize = 11.sp)
                                Text("Max Margin: ₹${maxMargin.toInt()}", fontSize = 11.sp, color = KashmirForestGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Customer Pays: ₹$totalCustomerAmount", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Your Profit: ₹$totalResellerProfit", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = KashmirSaffron)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = marginInput,
                        onValueChange = {
                            if (it.all { ch -> ch.isDigit() }) {
                                val v = it.toDoubleOrNull() ?: 0.0
                                if (v <= maxMargin) marginInput = it
                            }
                        },
                        label = { Text("Your Reseller Margin (₹ per item)") },
                        placeholder = { Text("Max ₹${maxMargin.toInt()}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Phone Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Customer Delivery Address *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Delivery Instructions / Landmark") },
                        placeholder = { Text("e.g. Near Jamia Masjid") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (errorMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(errorMsg, color = KashmirError, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (customerName.isBlank() || customerPhone.isBlank() || customerAddress.isBlank()) {
                                errorMsg = "Please fill customer name, phone and address."
                                return@Button
                            }
                            onConfirmOrder(
                                currentMargin,
                                quantity,
                                customerName,
                                customerPhone,
                                customerAddress,
                                customerDistrict,
                                customerArea,
                                "",
                                "",
                                instructions
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Book Customer COD Order (Earn ₹$totalResellerProfit)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// POST PRODUCT & CREATE SHOP DIALOGS (PRESERVED & ENHANCED)
// -----------------------------------------------------------------------------

@Composable
fun CreateShopDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var shopName by remember { mutableStateOf("") }
    var shopType by remember { mutableStateOf("Kashmiri Handicrafts & Apparel") }
    var description by remember { mutableStateOf("") }
    var district by remember { mutableStateOf(currentUser?.district ?: "Srinagar") }
    var area by remember { mutableStateOf(currentUser?.area ?: "Lal Chowk") }
    var address by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var errorMessage by remember { mutableStateOf("") }

    val platformSettings by viewModel.platformSettings.collectAsState()
    val dynamicCategories = remember(platformSettings?.customCategoriesCsv) {
        val custom = platformSettings?.customCategoriesCsv?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }
        if (!custom.isNullOrEmpty()) custom else MARKETPLACE_CATEGORIES.filter { it != "All Categories" }
    }

    val subcategoryMap = remember {
        mapOf(
            "Kashmiri Handicrafts" to listOf("Pashmina & Kani Shawls", "Sozni & Aari Embroidery", "Paper Mache Art", "Crewel Drapery"),
            "Pherans & Cultural Wear" to listOf("Tilla Embroidered Pherans", "Zari Work Pherans", "Pure Wool Tweed Pherans", "Kashmiri Bridal"),
            "Kangri & Winter Crafts" to listOf("Charar-i-Sharief Kangri", "Willow Wicker Baskets", "Anantnag Winter Warmers"),
            "Copperware & Utensils" to listOf("Hand-Hammered Samovars", "Engraved Tramic Plates", "Kashmiri Cups & Jugs"),
            "Walnut Wood & Carvings" to listOf("Carved Wooden Boxes", "Walnut Dining & Tables", "Chinar Leaf Trays"),
            "Kashmir Saffron & Spices" to listOf("Pampore Mongra Saffron", "Shahi Zeera (Caraway)", "Kashmiri Mirch", "Ver (Spice Cake)"),
            "Dry Fruits & Kernels" to listOf("Kagzi Walnuts (Giri)", "Mamra Almonds", "Dried Apricots", "Pine Nuts (Chilgoza)"),
            "Food & Wazwan" to listOf("Gushtaba & Rista", "Tabak Maaz", "Rogan Josh", "Wazwan Spices")
        )
    }

    var selectedSubcategory by remember { mutableStateOf("") }

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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Register Your Kashmir Shop",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Text(
                        text = "Open a digital storefront and sell to buyers across Kashmir with Cash on Delivery",
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Shop / Business Name *") },
                        placeholder = { Text("e.g. Royal Kashmir Shawls & Pherans") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Text("Primary Shop Category *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Configured and managed by Rozgar Kashmir Platform Administrators", fontSize = 11.sp, color = KashmirTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(dynamicCategories) { cat ->
                            FilterChip(
                                selected = shopType == cat,
                                onClick = {
                                    shopType = cat
                                    selectedSubcategory = ""
                                },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    val availableSubcats = subcategoryMap[shopType] ?: emptyList()
                    if (availableSubcats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Subcategory / Craft Specialty", fontWeight = FontWeight.Medium, fontSize = 12.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(availableSubcats) { subcat ->
                                FilterChip(
                                    selected = selectedSubcategory == subcat,
                                    onClick = { selectedSubcategory = if (selectedSubcategory == subcat) "" else subcat },
                                    label = { Text(subcat, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirMint,
                                        selectedLabelColor = KashmirDeepGreen
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Shop Bio & Specialties") },
                        placeholder = { Text("Describe your craft, materials, years of legacy...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area / Market *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("Shop Contact Phone *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        Text(errorMessage, color = KashmirError, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (shopName.isBlank() || contactPhone.isBlank()) {
                                    errorMessage = "Please enter shop name and contact phone."
                                    return@Button
                                }
                                val effectiveShopType = if (selectedSubcategory.isNotBlank()) "$shopType ($selectedSubcategory)" else shopType
                                viewModel.createShop(
                                    shopName = shopName,
                                    shopType = effectiveShopType,
                                    description = description,
                                    district = district,
                                    area = area,
                                    address = address.ifBlank { "$area, $district" },
                                    contactPhone = contactPhone,
                                    onComplete = { onDismiss() }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Shop", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostProductDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MARKETPLACE_CATEGORIES.getOrElse(1) { "Pherans & Traditional Wear" }) }
    var condition by remember { mutableStateOf("Brand New") }
    var isSecondHand by remember { mutableStateOf(false) }
    var priceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var isNegotiable by remember { mutableStateOf(true) }
    var district by remember { mutableStateOf(currentUser?.district ?: "Srinagar") }
    var area by remember { mutableStateOf(currentUser?.area ?: "Lal Chowk") }
    var errorMessage by remember { mutableStateOf("") }

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
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "List Item on Marketplace",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Text(
                        text = "Reach thousands of Kashmiri buyers across all 10 districts",
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Item Title *") },
                        placeholder = { Text("e.g. Pure Woolen Kashmiri Pheran with Tilla Work") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Surface(
                        color = KashmirSurfaceGreen,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Is this a Second-Hand / Pre-Owned item?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("It will appear in the dedicated Second-Hand section", fontSize = 11.sp, color = KashmirTextSecondary)
                            }
                            Switch(checked = isSecondHand, onCheckedChange = { isSecondHand = it })
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(MARKETPLACE_CATEGORIES) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Text("Condition", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PRODUCT_CONDITIONS) { cond ->
                            FilterChip(
                                selected = condition == cond,
                                onClick = { condition = cond },
                                label = { Text(cond, fontSize = 11.sp) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price (₹) *") },
                            placeholder = { Text("e.g. 3500") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isNegotiable, onCheckedChange = { isNegotiable = it })
                        Text("Price is Negotiable", fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description *") },
                        placeholder = { Text("Provide details about material, size, condition...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area / Town *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        Text(errorMessage, color = KashmirError, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item {
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull()
                            if (title.isBlank() || description.isBlank() || area.isBlank() || price == null) {
                                errorMessage = "Please enter valid title, description, area, and numeric price."
                                return@Button
                            }
                            viewModel.postProduct(
                                title = title,
                                description = description,
                                category = category,
                                condition = condition,
                                isSecondHand = isSecondHand,
                                price = price,
                                isNegotiable = isNegotiable,
                                quantity = quantityText.toIntOrNull() ?: 1,
                                district = district,
                                area = area,
                                onComplete = onDismiss
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_marketplace_listing_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publish Marketplace Listing", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
