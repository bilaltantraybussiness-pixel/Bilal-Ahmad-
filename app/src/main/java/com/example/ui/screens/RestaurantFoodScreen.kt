package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.FoodMenuItemEntity
import com.example.data.model.OrderEntity
import com.example.data.model.RestaurantEntity
import com.example.ui.components.KashmirBadge
import com.example.ui.components.RozgarAsyncImage
import com.example.ui.components.RozgarPaymentCheckoutSheet
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.viewmodel.RozgarViewModel

val KASHMIR_FOOD_CATEGORIES = listOf(
    "All Foods",
    "Wazwan Specialties",
    "Rogan Josh",
    "Yakhni & Gushtaba",
    "Rista & Tabak Maaz",
    "Haak & Traditional Veg",
    "Dum Aloo & Curries",
    "Kashmiri Harissa",
    "Kashmiri Pulao & Modur Pulao",
    "Kahwa & Beverages",
    "Girda, Lavasa & Bakery",
    "Vegetarian",
    "Non-Vegetarian",
    "Snacks & Kebabs",
    "Desserts & Phirni",
    "Wazwan Catering"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantFoodScreen(
    viewModel: RozgarViewModel,
    onNavigateBack: () -> Unit = { viewModel.navigateBack() }
) {
    val restaurants by viewModel.activeRestaurants.collectAsState()
    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    val platformSettings by viewModel.platformSettings.collectAsState()
    val userOrders by viewModel.userOrders.collectAsState()
    val supplierOrders by viewModel.supplierOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Browse Foods, 1: Restaurant Kitchen Dashboard
    var selectedCategory by remember { mutableStateOf("All Foods") }
    var selectedRestaurant by remember { mutableStateOf<RestaurantEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Checkout sheet state
    var orderingItem by remember { mutableStateOf<Pair<RestaurantEntity, FoodMenuItemEntity>?>(null) }
    
    // Restaurant Kitchen Dashboard dialogs
    var showAddFoodItemDialog by remember { mutableStateOf(false) }
    var showEditRestaurantDialog by remember { mutableStateOf(false) }
    var editingMenuItem by remember { mutableStateOf<FoodMenuItemEntity?>(null) }
    var showPayoutDialog by remember { mutableStateOf(false) }
    var payoutUpiInput by remember { mutableStateOf("") }

    val commissionRate = if (platformSettings?.foodCommissionEnabled == true) {
        platformSettings?.foodCommissionPercent ?: 1.0
    } else 1.0

    // High quality complete DEMO culinary items
    val sampleMenuItems = remember {
        listOf(
            FoodMenuItemEntity(
                id = 101,
                restaurantId = 1,
                name = "Kashmiri Wazwan 4-Person Trami Feast",
                description = "Full ceremonial banquet on embossed copper Trami: 4 seekh kebabs, 2 methi maaz, 1 tabak maaz, 4 ristas, 2 rogan josh, 1 gushtaba, rice & walnut chutney.",
                category = "Wazwan Catering",
                price = 2400.0,
                isVegetarian = false,
                preparationTimeMinutes = 45,
                portionSizes = "Trami for 4",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 102,
                restaurantId = 1,
                name = "Mutton Rogan Josh (Mawal Gravy)",
                description = "Tender Kashmiri lamb slowly simmered with cockscomb flower extract (Mawal), fennel, dried ginger and pure mustard oil.",
                category = "Rogan Josh",
                price = 340.0,
                isVegetarian = false,
                preparationTimeMinutes = 25,
                portionSizes = "Plate (2 pcs)",
                imageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500"
            ),
            FoodMenuItemEntity(
                id = 103,
                restaurantId = 1,
                name = "Mutton Yakhni in Aromatic Yogurt Broth",
                description = "Delicate succulent lamb cuts slow-cooked in velvety yogurt broth infused with dried mint, green cardamom and cloves.",
                category = "Yakhni & Gushtaba",
                price = 330.0,
                isVegetarian = false,
                preparationTimeMinutes = 25,
                portionSizes = "Plate (2 pcs)",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 104,
                restaurantId = 1,
                name = "Gushtaba (King of Wazwan)",
                description = "Velvety hand-pounded mutton meatballs gently simmered in creamy yogurt and pure mutton broth. Grand finale of Wazwan.",
                category = "Yakhni & Gushtaba",
                price = 360.0,
                isVegetarian = false,
                preparationTimeMinutes = 30,
                portionSizes = "1 Large Gushtaba",
                imageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500"
            ),
            FoodMenuItemEntity(
                id = 105,
                restaurantId = 1,
                name = "Kashmiri Rista (Mutton)",
                description = "Masterfully pounded mutton meatballs cooked in a saffron-tinted crimson gravy of Kashmiri red chillies.",
                category = "Rista & Tabak Maaz",
                price = 320.0,
                isVegetarian = false,
                preparationTimeMinutes = 25,
                portionSizes = "Plate (2 pcs)",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 106,
                restaurantId = 1,
                name = "Crisp Tabak Maaz (Fried Lamb Ribs)",
                description = "Twice-cooked tender lamb ribs braised in spiced milk and aniseed, then pan-fried in pure desi ghee to golden crunch.",
                category = "Rista & Tabak Maaz",
                price = 310.0,
                isVegetarian = false,
                preparationTimeMinutes = 20,
                portionSizes = "Plate (2 Ribs)",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 107,
                restaurantId = 1,
                name = "Traditional Kashmiri Haak (Collard Greens)",
                description = "Freshly harvested Kashmiri organic greens gently cooked in pure mustard oil with whole green chillies and a hint of hing.",
                category = "Haak & Traditional Veg",
                price = 160.0,
                isVegetarian = true,
                preparationTimeMinutes = 15,
                portionSizes = "Bowl (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500"
            ),
            FoodMenuItemEntity(
                id = 108,
                restaurantId = 1,
                name = "Kashmiri Dum Aloo (Slow-Cooked Baby Potatoes)",
                description = "Pricked baby potatoes deep fried and simmered in a robust, deeply flavorful gravy of Kashmiri red chili and fennel.",
                category = "Dum Aloo & Curries",
                price = 220.0,
                isVegetarian = true,
                preparationTimeMinutes = 20,
                portionSizes = "Plate (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500"
            ),
            FoodMenuItemEntity(
                id = 109,
                restaurantId = 2,
                name = "Authentic Winter Kashmiri Harissa",
                description = "Overnight slow-cooked velvety shredded mutton delicacy seasoned with fennel and rice broth, tempered with boiling desi ghee & Kebab.",
                category = "Kashmiri Harissa",
                price = 380.0,
                isVegetarian = false,
                preparationTimeMinutes = 10,
                portionSizes = "Full Plate (500g)",
                imageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500"
            ),
            FoodMenuItemEntity(
                id = 110,
                restaurantId = 1,
                name = "Fragrant Kashmiri Pulao with Nuts & Fruits",
                description = "Aromatic basmati rice cooked in saffron infused water, decorated with fried cashews, almonds, raisins and fresh pomegranate.",
                category = "Kashmiri Pulao & Modur Pulao",
                price = 280.0,
                isVegetarian = true,
                preparationTimeMinutes = 20,
                portionSizes = "Full Bowl",
                imageUri = "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500"
            ),
            FoodMenuItemEntity(
                id = 111,
                restaurantId = 1,
                name = "Modur Pulao (Celebratory Sweet Saffron Rice)",
                description = "Kashmiri royal celebratory rice infused with Pampore saffron, pure ghee, dates, cinnamon, cardamom and roasted dry fruits.",
                category = "Kashmiri Pulao & Modur Pulao",
                price = 260.0,
                isVegetarian = true,
                preparationTimeMinutes = 20,
                portionSizes = "Bowl (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1512058564366-18510be2db19?w=500"
            ),
            FoodMenuItemEntity(
                id = 112,
                restaurantId = 1,
                name = "Pampore Saffron Kahwa (Pot for 4)",
                description = "Hand-brewed green tea leaves with pure saffron filaments, crushed Giri almonds, cinnamon bark and green cardamom.",
                category = "Kahwa & Beverages",
                price = 140.0,
                isVegetarian = true,
                preparationTimeMinutes = 10,
                portionSizes = "Samovar Pot (4 cups)",
                imageUri = "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500"
            ),
            FoodMenuItemEntity(
                id = 113,
                restaurantId = 1,
                name = "Kandur Girda Bread (Pack of 4)",
                description = "Morning tandoori bread with thumb indentations, crisp on outside and soft inside. Freshly baked in clay tandoor.",
                category = "Girda, Lavasa & Bakery",
                price = 40.0,
                isVegetarian = true,
                preparationTimeMinutes = 5,
                portionSizes = "Pack of 4",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
            ),
            FoodMenuItemEntity(
                id = 114,
                restaurantId = 1,
                name = "Kandur Lavasa Flatbread (Pack of 4)",
                description = "Ultra-thin, soft unleavened tandoor flatbread, traditionally eaten with Kashmiri Tujji barbecue kebabs or morning butter.",
                category = "Girda, Lavasa & Bakery",
                price = 40.0,
                isVegetarian = true,
                preparationTimeMinutes = 5,
                portionSizes = "Pack of 4",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
            ),
            FoodMenuItemEntity(
                id = 115,
                restaurantId = 1,
                name = "Kashmir Kandur Bakery Box (Sheermal, Telvor & Roath)",
                description = "Artisanal bakery selection of sweet Kashmiri Sheermal, crisp sesame Telvor, and traditional dry-fruit infused Roath cake.",
                category = "Girda, Lavasa & Bakery",
                price = 180.0,
                isVegetarian = true,
                preparationTimeMinutes = 5,
                portionSizes = "Box of 6 pieces",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
            ),
            FoodMenuItemEntity(
                id = 116,
                restaurantId = 1,
                name = "Nadur Yakhni (Dal Lake Lotus Stem Curry)",
                description = "Crunchy lotus stem rounds cooked in a delicate spiced yogurt gravy with cloves, cardamom, and mint. Kashmiri classic.",
                category = "Vegetarian",
                price = 240.0,
                isVegetarian = true,
                preparationTimeMinutes = 20,
                portionSizes = "Plate (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500"
            ),
            FoodMenuItemEntity(
                id = 117,
                restaurantId = 1,
                name = "Chaman Kaliya (Turmeric Paneer Gravy)",
                description = "Deep-fried fresh cottage cheese cubes cooked in golden turmeric gravy with milk, fennel, and whole Kashmiri spices.",
                category = "Vegetarian",
                price = 260.0,
                isVegetarian = true,
                preparationTimeMinutes = 20,
                portionSizes = "Plate (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500"
            ),
            FoodMenuItemEntity(
                id = 118,
                restaurantId = 1,
                name = "Methi Maaz (Minced Lamb with Fenugreek)",
                description = "Savory opening dish of Wazwan: finely minced lamb braised with aromatic dried Kashmiri mountain fenugreek herbs.",
                category = "Non-Vegetarian",
                price = 290.0,
                isVegetarian = false,
                preparationTimeMinutes = 20,
                portionSizes = "Plate (Serves 2)",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 119,
                restaurantId = 1,
                name = "Kashmiri Tujji (Barbecue Seekh Kebabs - 6 Skewers)",
                description = "Freshly charcoal-grilled minced mutton kebabs served with onion, radish-walnut chutney (Doon Chetin), and soft Lavasa.",
                category = "Snacks & Kebabs",
                price = 250.0,
                isVegetarian = false,
                preparationTimeMinutes = 15,
                portionSizes = "6 Skewers with 2 Lavasas",
                imageUri = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=500"
            ),
            FoodMenuItemEntity(
                id = 120,
                restaurantId = 1,
                name = "Kashmiri Shufta (Dry Fruits & Paneer Dessert)",
                description = "Festive Kashmiri dessert of toasted almonds, walnuts, cashews, raisins, dry coconut and fried paneer coated in spiced honey syrup.",
                category = "Desserts & Phirni",
                price = 190.0,
                isVegetarian = true,
                preparationTimeMinutes = 10,
                portionSizes = "Bowl",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
            ),
            FoodMenuItemEntity(
                id = 121,
                restaurantId = 1,
                name = "Saffron Phirni with Silver Leaf & Pistachio",
                description = "Traditional slow-cooked ground rice pudding flavored with green cardamom, saffron and kewra water in earthen clay pot.",
                category = "Desserts & Phirni",
                price = 110.0,
                isVegetarian = true,
                preparationTimeMinutes = 5,
                portionSizes = "1 Earthen Matka",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500"
            ),
            FoodMenuItemEntity(
                id = 122,
                restaurantId = 1,
                name = "Traditional Noon Chai / Pink Salt Chai (Pot)",
                description = "Brewed pink savory green tea leaves with milk, baking soda and sea salt, topped with rich clotted cream and crushed walnuts.",
                category = "Kahwa & Beverages",
                price = 90.0,
                isVegetarian = true,
                preparationTimeMinutes = 10,
                portionSizes = "Pot for 3 cups",
                imageUri = "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500"
            ),
            FoodMenuItemEntity(
                id = 123,
                restaurantId = 1,
                name = "Wazwan Catering Booking (Per Trami for 4 Guests)",
                description = "Professional on-site Wazwan catering by master Wazas with authentic wood fire and copperware. Advance booking.",
                category = "Wazwan Catering",
                price = 2200.0,
                isVegetarian = false,
                preparationTimeMinutes = 120,
                portionSizes = "Full Trami catering",
                imageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=500"
            ),
            FoodMenuItemEntity(
                id = 124,
                restaurantId = 1,
                name = "Doon Chetin (Walnut Chutney) & Kashmiri Achar Set",
                description = "Hand-ground walnut-curd chutney with green chillies, paired with authentic mustard oil pickled knol-khol and turnips.",
                category = "Snacks & Kebabs",
                price = 80.0,
                isVegetarian = true,
                preparationTimeMinutes = 5,
                portionSizes = "200g Jar Set",
                imageUri = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500"
            )
        )
    }

    val currentRestaurant = selectedRestaurant ?: restaurants.firstOrNull() ?: RestaurantEntity(
        id = 1L,
        ownerUserId = 1L,
        ownerName = "Ahdoos Kitchen",
        name = "Ahdoos Heritage Wazwan & Bakery",
        description = "Legendary 7-course Kashmiri Wazwan, fresh Girda, Lavasa & Sheermal baked hourly.",
        cuisineType = "Authentic Kashmiri Wazwan & Bakery",
        district = "Srinagar",
        area = "Residency Road / Lal Chowk",
        address = "Residency Road, Lal Chowk, Srinagar",
        openingHours = "10:00 AM - 10:30 PM",
        deliveryFee = 30.0,
        minimumOrder = 150.0,
        rating = 4.9,
        totalReviews = 148,
        bannerImageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
        logoImageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=200"
    )

    val dbMenuItems by viewModel.getMenuItemsForRestaurantFlow(currentRestaurant.id).collectAsState(initial = emptyList())
    val allAvailableItems = remember(dbMenuItems, sampleMenuItems) {
        if (dbMenuItems.isNotEmpty()) dbMenuItems + sampleMenuItems.filter { sm -> dbMenuItems.none { dm -> dm.id == sm.id } }
        else sampleMenuItems
    }

    val displayedItems = remember(allAvailableItems, selectedCategory, searchQuery) {
        allAvailableItems.filter { item ->
            val matchesCategory = when (selectedCategory) {
                "All Foods" -> true
                "Vegetarian" -> item.isVegetarian
                "Non-Vegetarian" -> !item.isVegetarian
                else -> item.category.contains(selectedCategory.take(6), ignoreCase = true)
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.URDU) "کشمیری ریستوران و وازوان"
                            else if (currentLang == AppLanguage.KASHMIRI) "کٲشُر وازوان تہٕ ریستوران"
                            else "Kashmir Food & Restaurants",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Authentic Wazwan, Harissa & Kandur • 1% Platform Fee",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirMint)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { activeTab = if (activeTab == 0) 1 else 0 }) {
                        Icon(
                            if (activeTab == 0) Icons.Default.DashboardCustomize else Icons.Default.RestaurantMenu,
                            contentDescription = "Switch Portal",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KashmirDeepGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding)
        ) {
            // Mode Tabs: 0: Browse Food, 1: Restaurant Kitchen Dashboard
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = KashmirDeepGreen
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Order Food", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Kitchen Dashboard", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Storefront, null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (activeTab == 0) {
                // ================= TAB 0: BROWSE FOOD (CUSTOMER VIEW) =================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    // Search Bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Wazwan, Rogan Josh, Harissa, Girda...") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, null)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("food_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = KashmirEmerald,
                                unfocusedBorderColor = KashmirCardBorder
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Demo content notice banner
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, null, tint = KashmirForestGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        "DEMO FOOD MARKETPLACE",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = KashmirForestGreen
                                    )
                                    Text(
                                        "Authentic Kashmiri menu with real transparent fee breakdown: 1% platform fee, 99% to local chefs. Fully manageable from Kitchen & Admin Dashboards.",
                                        fontSize = 10.sp,
                                        color = KashmirTextSecondary,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Horizontal Category Chips
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(KASHMIR_FOOD_CATEGORIES) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirEmerald,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Featured Restaurant Hero Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    RozgarAsyncImage(
                                        model = currentRestaurant.bannerImageUri.ifBlank { "https://images.unsplash.com/photo-1544025162-d76694265947?w=800" },
                                        contentDescription = currentRestaurant.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = KashmirGold, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                "${currentRestaurant.rating} (${currentRestaurant.totalReviews} reviews)",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = currentRestaurant.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = KashmirDeepGreen
                                        )
                                        KashmirBadge(text = "VERIFIED KITCHEN", containerColor = KashmirMint)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentRestaurant.description,
                                        fontSize = 12.sp,
                                        color = KashmirTextSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(currentRestaurant.openingHours, fontSize = 11.sp, color = KashmirTextMuted)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DeliveryDining, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("₹${currentRestaurant.deliveryFee.toInt()} Delivery Fee", fontSize = 11.sp, color = KashmirTextMuted)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.ShoppingBag, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Min ₹${currentRestaurant.minimumOrder.toInt()}", fontSize = 11.sp, color = KashmirTextMuted)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Menu Items Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kashmiri Dishes (${displayedItems.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "1% Transparent Fee",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KashmirForestGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Food Items List
                    items(displayedItems, key = { it.id }) { item ->
                        FullFoodItemCard(
                            item = item,
                            restaurant = currentRestaurant,
                            commissionPercent = commissionRate,
                            onOrderClick = { orderingItem = Pair(currentRestaurant, item) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {
                // ================= TAB 1: RESTAURANT KITCHEN DASHBOARD =================
                RestaurantKitchenDashboard(
                    restaurant = currentRestaurant,
                    viewModel = viewModel,
                    menuItems = allAvailableItems,
                    orders = supplierOrders,
                    onEditRestaurant = { showEditRestaurantDialog = true },
                    onAddNewDish = { showAddFoodItemDialog = true },
                    onEditMenuItem = { editingMenuItem = it },
                    onRequestPayout = { showPayoutDialog = true }
                )
            }
        }
    }

    // Transparent Checkout Dialog
    orderingItem?.let { (restaurant, item) ->
        val savedAddresses by viewModel.currentUserAddresses.collectAsState()
        RozgarPaymentCheckoutSheet(
            title = item.name,
            subtitle = "From ${restaurant.name} (${restaurant.area}, ${restaurant.district})",
            itemPrice = item.price,
            quantity = 1,
            deliveryFee = restaurant.deliveryFee,
            discountAmount = 0.0,
            platformFeePercent = commissionRate,
            categoryName = "FOOD",
            savedAddresses = savedAddresses,
            onSaveNewAddress = { newAddr ->
                viewModel.saveUserAddress(newAddr)
            },
            onPaymentConfirmed = { method, status, txnRef, _, _, _, _, _, _, chosenAddress ->
                val deliveryAddressStr = if (chosenAddress != null) {
                    "${chosenAddress.recipientName}, ${chosenAddress.houseFlatFloor}, ${chosenAddress.buildingName}, ${chosenAddress.street}, ${chosenAddress.locality}, ${chosenAddress.district} - ${chosenAddress.pincode} (Ph: ${chosenAddress.recipientPhone})"
                } else {
                    "Rajbagh, Srinagar, Kashmir"
                }
                viewModel.placeFoodOrder(
                    restaurant = restaurant,
                    item = item,
                    quantity = 1,
                    deliveryAddress = deliveryAddressStr,
                    paymentMethod = method,
                    paymentStatus = status,
                    paymentTransactionRef = txnRef,
                    deliveryFee = restaurant.deliveryFee,
                    discountAmount = 0.0,
                    onComplete = {
                        orderingItem = null
                    }
                )
            },
            onDismiss = { orderingItem = null }
        )
    }

    // Add Food Item Dialog
    if (showAddFoodItemDialog) {
        AddFoodItemDialog(
            restaurantId = currentRestaurant.id,
            viewModel = viewModel,
            onDismiss = { showAddFoodItemDialog = false }
        )
    }

    // Edit Menu Item Dialog
    editingMenuItem?.let { item ->
        EditMenuItemDialog(
            item = item,
            viewModel = viewModel,
            onDismiss = { editingMenuItem = null }
        )
    }

    // Edit Restaurant Profile Dialog
    if (showEditRestaurantDialog) {
        EditRestaurantProfileDialog(
            restaurant = currentRestaurant,
            viewModel = viewModel,
            onDismiss = { showEditRestaurantDialog = false }
        )
    }

    // Request Kitchen Payout Dialog
    if (showPayoutDialog) {
        AlertDialog(
            onDismissRequest = { showPayoutDialog = false },
            title = { Text("Kitchen Payout Settlement", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter your J&K Bank or UPI address to settle earned kitchen revenue after 1% platform fee deduction.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = payoutUpiInput,
                        onValueChange = { payoutUpiInput = it },
                        label = { Text("UPI ID / J&K Bank mPay") },
                        placeholder = { Text("e.g. ahdoos@okjkbank") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (payoutUpiInput.isNotBlank()) {
                            viewModel.requestPayout(
                                amount = 4500.0,
                                upiId = payoutUpiInput
                            )
                            showPayoutDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Submit Payout Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FullFoodItemCard(
    item: FoodMenuItemEntity,
    restaurant: RestaurantEntity,
    commissionPercent: Double,
    onOrderClick: () -> Unit
) {
    val platformFee = (item.price * commissionPercent) / 100.0
    val chefPayout = item.price - platformFee

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, if (item.isVegetarian) KashmirForestGreen else KashmirError),
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (item.isVegetarian) KashmirForestGreen else KashmirError,
                                    modifier = Modifier.size(8.dp)
                                ) {}
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        KashmirBadge(
                            text = if (item.isVegetarian) "VEG" else "NON-VEG",
                            containerColor = if (item.isVegetarian) KashmirMint else KashmirError.copy(alpha = 0.15f),
                            contentColor = if (item.isVegetarian) KashmirForestGreen else KashmirError
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        KashmirBadge(text = "DEMO", containerColor = KashmirSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = KashmirTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = KashmirTextSecondary,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = KashmirSurfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⏱ ${item.preparationTimeMinutes} mins prep",
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = KashmirTextSecondary
                            )
                        }
                        if (item.portionSizes.isNotBlank()) {
                            Surface(
                                color = KashmirSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🍽 ${item.portionSizes}",
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = KashmirTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Image & Action Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        RozgarAsyncImage(
                            model = item.imageUri.ifBlank { "https://images.unsplash.com/photo-1544025162-d76694265947?w=500" },
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "₹${item.price.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KashmirDeepGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onOrderClick,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_food_${item.id}")
                    ) {
                        Text("Order", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Transparent breakdown bar
            Surface(
                color = KashmirSurfaceGreen.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Fair Share: ₹${chefPayout.toInt()} to Kitchen (1% Platform: ₹${"%.1f".format(platformFee)})",
                        fontSize = 10.sp,
                        color = KashmirForestGreen,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cash on Delivery / UPI",
                        fontSize = 10.sp,
                        color = KashmirTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantKitchenDashboard(
    restaurant: RestaurantEntity,
    viewModel: RozgarViewModel,
    menuItems: List<FoodMenuItemEntity>,
    orders: List<OrderEntity>,
    onEditRestaurant: () -> Unit,
    onAddNewDish: () -> Unit,
    onEditMenuItem: (FoodMenuItemEntity) -> Unit,
    onRequestPayout: () -> Unit
) {
    val totalOrders = orders.size
    val grossSales = orders.sumOf { it.totalAmount }
    val commissionPaid = orders.sumOf { it.platformFeeAmount }
    val netKitchenEarnings = grossSales - commissionPaid

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Kitchen Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                text = restaurant.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = KashmirDeepGreen
                            )
                            Text(
                                text = "${restaurant.area}, ${restaurant.district}",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                        }
                        IconButton(onClick = onEditRestaurant) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = KashmirEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🕒 Hours: ${restaurant.openingHours}", fontSize = 11.sp, color = KashmirTextMuted)
                        Text("🛵 Delivery: ₹${restaurant.deliveryFee.toInt()}", fontSize = 11.sp, color = KashmirTextMuted)
                        Text("📦 Min Order: ₹${restaurant.minimumOrder.toInt()}", fontSize = 11.sp, color = KashmirTextMuted)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Financial & Payout Summary
        item {
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
                        Text("Gross Sales", fontSize = 10.sp, color = KashmirForestGreen)
                        Text("₹${grossSales.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    }
                }
                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Net Payout (99%)", fontSize = 10.sp, color = KashmirDeepGreen)
                        Text("₹${netKitchenEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    }
                }
                Surface(
                    color = KashmirSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Orders", fontSize = 10.sp, color = KashmirTextSecondary)
                        Text("$totalOrders", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestPayout,
                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Request Kitchen Payout Settlement", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Menu Management Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu Management (${menuItems.size} Dishes)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
                Button(
                    onClick = onAddNewDish,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Dish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Dish Cards in Dashboard
        items(menuItems, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            KashmirBadge(
                                text = if (item.isAvailable) "IN STOCK" else "OUT OF STOCK",
                                containerColor = if (item.isAvailable) KashmirMint else KashmirError.copy(alpha = 0.2f),
                                contentColor = if (item.isAvailable) KashmirForestGreen else KashmirError
                            )
                        }
                        Text(
                            text = "₹${item.price.toInt()} • ${item.preparationTimeMinutes} min prep",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onEditMenuItem(item) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Dish", tint = KashmirEmerald)
                        }
                        Switch(
                            checked = item.isAvailable,
                            onCheckedChange = { isAvailable ->
                                viewModel.updateMenuItem(item.copy(isAvailable = isAvailable))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = KashmirEmerald)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun AddFoodItemDialog(
    restaurantId: Long,
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Wazwan Specialties") }
    var priceText by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Add New Dish to Menu", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name *") },
                    placeholder = { Text("e.g. Kashmiri Gushtaba") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    placeholder = { Text("Authentic slow-cooked preparation...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                    label = { Text("Price (₹) *") },
                    placeholder = { Text("e.g. 350") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Vegetarian Dish?", fontSize = 13.sp)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val priceVal = priceText.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && priceVal > 0.0) {
                                viewModel.addFoodMenuItem(
                                    restaurantId = restaurantId,
                                    name = name,
                                    description = description,
                                    category = category,
                                    price = priceVal,
                                    isVeg = isVeg,
                                    onComplete = { onDismiss() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                    ) {
                        Text("Add Dish")
                    }
                }
            }
        }
    }
}

@Composable
fun EditMenuItemDialog(
    item: FoodMenuItemEntity,
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember { mutableStateOf(item.price.toInt().toString()) }
    var prepTimeText by remember { mutableStateOf(item.preparationTimeMinutes.toString()) }
    var isAvailable by remember { mutableStateOf(item.isAvailable) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Edit Dish", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Dish Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                    label = { Text("Price (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = prepTimeText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) prepTimeText = it },
                    label = { Text("Preparation Time (Minutes)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("In Stock / Available", fontSize = 13.sp)
                    Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = {
                            viewModel.deleteMenuItem(item.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = KashmirError)
                    ) {
                        Text("Remove")
                    }
                    Row {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val priceVal = priceText.toDoubleOrNull() ?: item.price
                                val prepVal = prepTimeText.toIntOrNull() ?: item.preparationTimeMinutes
                                viewModel.updateMenuItem(
                                    item.copy(
                                        name = name,
                                        price = priceVal,
                                        preparationTimeMinutes = prepVal,
                                        isAvailable = isAvailable
                                    )
                                )
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditRestaurantProfileDialog(
    restaurant: RestaurantEntity,
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(restaurant.name) }
    var hours by remember { mutableStateOf(restaurant.openingHours) }
    var deliveryFeeText by remember { mutableStateOf(restaurant.deliveryFee.toInt().toString()) }
    var minOrderText by remember { mutableStateOf(restaurant.minimumOrder.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Edit Kitchen Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Restaurant Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Opening Hours") },
                    placeholder = { Text("e.g. 10:00 AM - 10:30 PM") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deliveryFeeText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) deliveryFeeText = it },
                    label = { Text("Delivery Fee (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = minOrderText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) minOrderText = it },
                    label = { Text("Minimum Order (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val feeVal = deliveryFeeText.toDoubleOrNull() ?: restaurant.deliveryFee
                            val minVal = minOrderText.toDoubleOrNull() ?: restaurant.minimumOrder
                            viewModel.updateRestaurant(
                                restaurant.copy(
                                    name = name,
                                    openingHours = hours,
                                    deliveryFee = feeVal,
                                    minimumOrder = minVal
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}
