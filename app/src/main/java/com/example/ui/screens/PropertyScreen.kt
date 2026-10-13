package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PropertyListingEntity
import com.example.ui.components.KashmirBadge
import com.example.ui.components.RozgarAsyncImage
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.viewmodel.RozgarViewModel

val KASHMIR_DISTRICTS_LIST = listOf(
    "All Districts",
    "Srinagar",
    "Baramulla",
    "Anantnag",
    "Budgam",
    "Pulwama",
    "Kupwara",
    "Shopian",
    "Ganderbal",
    "Kulgam",
    "Bandipora"
)

val PROPERTY_TYPES_LIST = listOf(
    "All Types",
    "Rooms & Shared",
    "Student Accommodation & PG",
    "Flats & Apartments",
    "Houses & Kothis",
    "Luxury Kashmiri Villas",
    "Commercial Shops",
    "Offices & Workspaces",
    "Commercial Spaces & Halls",
    "Land & Plots",
    "Agricultural Orchards"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyScreen(
    viewModel: RozgarViewModel,
    onNavigateBack: () -> Unit = { viewModel.navigateBack() }
) {
    val properties by viewModel.activeProperties.collectAsState()
    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    val platformSettings by viewModel.platformSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDistrict by remember { mutableStateOf("All Districts") }
    var selectedType by remember { mutableStateOf("All Types") }
    var selectedMode by remember { mutableStateOf("All") } // "All", "Rent", "Sale"
    var showAddPropertyDialog by remember { mutableStateOf(false) }

    // Complete 12 DEMO Properties representing every requested category
    val demoPropertiesList = remember {
        listOf(
            PropertyListingEntity(
                id = 201,
                ownerUserId = 1,
                ownerName = "Ghulam Mohammad Rather",
                title = "Clean Single Room for Student / Artisan",
                description = "Well-lit room with separate entrance, geyser, wooden flooring, near Downtown bus stop and Jamia Masjid.",
                propertyType = "Rooms & Shared",
                listingType = "Rent",
                rentOrPrice = 4500.0,
                deposit = 5000.0,
                district = "Srinagar",
                town = "Downtown / Nowhatta",
                address = "Malkha Road, Nowhatta, Srinagar",
                furnishing = "Semi-Furnished",
                bedrooms = 1,
                bathrooms = 1,
                amenities = "Geyser, Separate Entrance, 24h Water",
                imageUris = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?w=600"
            ),
            PropertyListingEntity(
                id = 202,
                ownerUserId = 2,
                ownerName = "Mohammad Shafi Dar",
                title = "Shared 2-Bed Student Accommodation near KU",
                description = "Quiet shared room with high-speed Wi-Fi, study desks, warm Bukhari heating, 5 mins walk from Kashmir University Sir Syed Gate.",
                propertyType = "Shared Accommodation",
                listingType = "Rent",
                rentOrPrice = 3500.0,
                deposit = 3500.0,
                district = "Srinagar",
                town = "Hazratbal",
                address = "Near University Market, Hazratbal, Srinagar",
                furnishing = "Furnished",
                bedrooms = 1,
                bathrooms = 1,
                amenities = "Wi-Fi, Study Desks, Inverter Backup, Mess Available",
                imageUris = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=600"
            ),
            PropertyListingEntity(
                id = 203,
                ownerUserId = 2,
                ownerName = "Mohammad Shafi Dar",
                title = "Furnished 2BHK Flat near Dal Lake & Boulevard",
                description = "Sunny second-floor flat with authentic Khatamband wooden ceilings, scenic mountain view, modern kitchen, and dedicated parking.",
                propertyType = "Flats & Apartments",
                listingType = "Rent",
                rentOrPrice = 14000.0,
                deposit = 28000.0,
                district = "Srinagar",
                town = "Dalgate / Boulevard",
                address = "Boulevard Road, Dalgate, Srinagar",
                furnishing = "Furnished",
                bedrooms = 2,
                bathrooms = 2,
                amenities = "Khatamband, Geyser, Lake View, Car Parking",
                imageUris = "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600"
            ),
            PropertyListingEntity(
                id = 204,
                ownerUserId = 1,
                ownerName = "Ghulam Mohammad Rather",
                title = "Independent 3BHK Kashmiri House with Garden",
                description = "Traditional brick house with pine wood paneling, front lawn with apple & walnut trees, hamam room, and quiet residential neighborhood.",
                propertyType = "Houses & Kothis",
                listingType = "Rent",
                rentOrPrice = 22000.0,
                deposit = 44000.0,
                district = "Srinagar",
                town = "Rawalpora",
                address = "Housing Colony, Rawalpora, Srinagar",
                furnishing = "Semi-Furnished",
                bedrooms = 3,
                bathrooms = 2,
                amenities = "Hamam Heating, Garden, Inverter, 2 Car Parking",
                imageUris = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600"
            ),
            PropertyListingEntity(
                id = 205,
                ownerUserId = 3,
                ownerName = "Farooq Ahmad Shah",
                title = "Luxury 4BHK Pine Wood Villa overlooking Zabarwan",
                description = "Premium hillside villa crafted with Deodar wood, central heating, terrace overlooking Dal Lake and Zabarwan range, servant quarters.",
                propertyType = "Luxury Kashmiri Villas",
                listingType = "Rent",
                rentOrPrice = 45000.0,
                deposit = 90000.0,
                district = "Srinagar",
                town = "Nishat / Harwan",
                address = "Near Nishat Garden, Srinagar",
                furnishing = "Furnished",
                bedrooms = 4,
                bathrooms = 4,
                amenities = "Central Heating, Mountain View, Landscaped Lawn, Security Guard",
                imageUris = "https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600"
            ),
            PropertyListingEntity(
                id = 206,
                ownerUserId = 2,
                ownerName = "Mohammad Shafi Dar",
                title = "Commercial Shop on Sopore Fruit Mandi Main Road",
                description = "Corner shutter shop with high vehicle and footfall traffic, ideal for dry fruit trading, commission agent, or retail store.",
                propertyType = "Commercial Shops",
                listingType = "Rent",
                rentOrPrice = 8500.0,
                deposit = 25000.0,
                district = "Baramulla",
                town = "Sopore",
                address = "Fruit Mandi Link Road, Sopore",
                furnishing = "Unfurnished",
                bedrooms = 0,
                bathrooms = 1,
                amenities = "Rolling Shutter, 3-Phase Power, Main Road Frontage",
                imageUris = "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600"
            ),
            PropertyListingEntity(
                id = 207,
                ownerUserId = 1,
                ownerName = "Ghulam Mohammad Rather",
                title = "Corporate Office Space with Fiber & Power Backup",
                description = "Furnished office floor with 12 workstations, manager cabin, conference room, and separate pantry on Lal Chowk arterial road.",
                propertyType = "Offices & Workspaces",
                listingType = "Rent",
                rentOrPrice = 25000.0,
                deposit = 50000.0,
                district = "Srinagar",
                town = "Residency Road / Lal Chowk",
                address = "Residency Road, Lal Chowk, Srinagar",
                furnishing = "Furnished",
                bedrooms = 0,
                bathrooms = 2,
                amenities = "High-speed Fiber, Generator Backup, Conference Room, Lift",
                imageUris = "https://images.unsplash.com/photo-1497366216548-37526070297c?w=600"
            ),
            PropertyListingEntity(
                id = 208,
                ownerUserId = 3,
                ownerName = "Farooq Ahmad Shah",
                title = "Spacious 1,500 Sq Ft Commercial Hall for Showroom/Gym",
                description = "Open pillar-free ground floor hall with wide glass facade on Srinagar Bypass, ideal for gym, automobile showroom, or warehouse.",
                propertyType = "Commercial Spaces & Halls",
                listingType = "Rent",
                rentOrPrice = 35000.0,
                deposit = 100000.0,
                district = "Srinagar",
                town = "Hyderpora Bypass",
                address = "National Highway Bypass, Hyderpora, Srinagar",
                furnishing = "Unfurnished",
                bedrooms = 0,
                bathrooms = 2,
                amenities = "Glass Facade, Heavy Footfall, Easy Truck Access, 24h Security",
                imageUris = "https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=600"
            ),
            PropertyListingEntity(
                id = 209,
                ownerUserId = 2,
                ownerName = "Mohammad Shafi Dar",
                title = "5 Marla Prime Residential Plot for Immediate Sale",
                description = "Clean freehold title plot in approved residential layout, 20-foot wide macadamized road, ready for immediate construction.",
                propertyType = "Land & Plots",
                listingType = "Sale",
                rentOrPrice = 3500000.0,
                deposit = 0.0,
                district = "Srinagar",
                town = "Bemina",
                address = "Near Highway Bypass, Bemina, Srinagar",
                furnishing = "Unfurnished",
                bedrooms = 0,
                bathrooms = 0,
                amenities = "Clear Revenue Title, Water & Electricity Line, 20ft Wide Road",
                imageUris = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=600"
            ),
            PropertyListingEntity(
                id = 210,
                ownerUserId = 3,
                ownerName = "Wani Orchards",
                title = "10 Kanal High-Density Organic Apple Orchard",
                description = "Productive orchard producing Grade-A Kulu Delicious and Gala apples, natural spring water irrigation, fenced perimeter.",
                propertyType = "Agricultural Orchards",
                listingType = "Sale",
                rentOrPrice = 6500000.0,
                deposit = 0.0,
                district = "Shopian",
                town = "Zainapora",
                address = "Main Orchard Belt, Zainapora, Shopian",
                furnishing = "Unfurnished",
                bedrooms = 0,
                bathrooms = 0,
                amenities = "Natural Spring, Drip Irrigation Provision, Concrete Fencing, Motorable Path",
                imageUris = "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=600"
            ),
            PropertyListingEntity(
                id = 211,
                ownerUserId = 1,
                ownerName = "Ghulam Mohammad Rather",
                title = "Modern 4BHK Independent Kothi for Sale",
                description = "Recently built designer house with Hamam, Khatamband ceilings, Italian marble and walnut fixtures, prime neighborhood.",
                propertyType = "Houses & Kothis",
                listingType = "Sale",
                rentOrPrice = 9500000.0,
                deposit = 0.0,
                district = "Srinagar",
                town = "Sanat Nagar",
                address = "Sanat Nagar Colony, Srinagar",
                furnishing = "Furnished",
                bedrooms = 4,
                bathrooms = 4,
                amenities = "Deodar Woodwork, Hamam, Modular Kitchen, 2-Car Garage",
                imageUris = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?w=600"
            ),
            PropertyListingEntity(
                id = 212,
                ownerUserId = 2,
                ownerName = "Mohammad Shafi Dar",
                title = "Student PG & Hostel with Heating & Mess Facility",
                description = "Safe secure accommodation for college and competitive exam aspirants, includes hot water, winter heating, 3 meals daily.",
                propertyType = "Student Accommodation & PG",
                listingType = "Rent",
                rentOrPrice = 5500.0,
                deposit = 3000.0,
                district = "Srinagar",
                town = "Hazratbal / Naseem Bagh",
                address = "Near NIT Srinagar Campus Gate",
                furnishing = "Furnished",
                bedrooms = 1,
                bathrooms = 1,
                amenities = "Mess (3 Meals), Heating, Wi-Fi, 24h Hot Water, CCTV Security",
                imageUris = "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=600"
            )
        )
    }

    val combinedProperties = remember(properties, demoPropertiesList) {
        if (properties.isNotEmpty()) {
            properties + demoPropertiesList.filter { demo -> properties.none { p -> p.id == demo.id } }
        } else demoPropertiesList
    }

    val filteredProperties = remember(combinedProperties, searchQuery, selectedDistrict, selectedType, selectedMode) {
        combinedProperties.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.title.contains(searchQuery, ignoreCase = true) ||
                p.description.contains(searchQuery, ignoreCase = true) ||
                p.town.contains(searchQuery, ignoreCase = true) ||
                p.address.contains(searchQuery, ignoreCase = true)

            val matchesDistrict = selectedDistrict == "All Districts" ||
                p.district.equals(selectedDistrict, ignoreCase = true)

            val matchesType = selectedType == "All Types" ||
                p.propertyType.contains(selectedType.take(4), ignoreCase = true)

            val matchesMode = selectedMode == "All" ||
                p.listingType.equals(selectedMode, ignoreCase = true)

            matchesQuery && matchesDistrict && matchesType && matchesMode
        }
    }

    val rentalCommissionRate = platformSettings?.propertyRentalCommissionPercent ?: 5.0
    val saleCommissionRate = platformSettings?.propertySaleCommissionPercent ?: 5.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentLang == AppLanguage.URDU) "کشمیر پراپرٹی و کرایہ"
                            else if (currentLang == AppLanguage.KASHMIRI) "کٲشِر جایداد تہٕ کرایہٕ"
                            else "Kashmir Property & Rentals",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rooms, Flats, Houses, Shops & Orchards",
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
                    Button(
                        onClick = { showAddPropertyDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirGold),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("list_property_button")
                    ) {
                        Icon(Icons.Default.AddHome, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("List Property", color = KashmirDeepGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KashmirDeepGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by area, title, e.g. Dalgate, Rajbagh, Sopore...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("property_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = KashmirEmerald,
                        unfocusedBorderColor = KashmirCardBorder
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Mode Selector: All / Rent / Sale
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Rent", "Sale").forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(if (mode == "Rent") "For Rent (کرایہ)" else if (mode == "Sale") "For Sale (فروخت)" else "All Listings") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirDeepGreen,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // District Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KASHMIR_DISTRICTS_LIST) { district ->
                        FilterChip(
                            selected = selectedDistrict == district,
                            onClick = { selectedDistrict = district },
                            label = { Text(district, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirEmerald,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Property Type Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PROPERTY_TYPES_LIST) { pType ->
                        FilterChip(
                            selected = selectedType == pType,
                            onClick = { selectedType = pType },
                            label = { Text(pType, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirForestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Platform Commission & Fraud Policy Notice
            item {
                Surface(
                    color = KashmirMint.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, null, tint = KashmirForestGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rozgar Verified Property Policy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KashmirDeepGreen
                            )
                            Text(
                                text = "Disclosed Commission: ${rentalCommissionRate.toInt()}% of 1st month rent on confirmed tenancy; ${saleCommissionRate.toInt()}% on qualifying property sales. Direct contact numbers are masked for anti-fraud protection.",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Listings List
            if (filteredProperties.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.HomeWork, null, tint = KashmirTextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No properties found matching your filters",
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                            Text(
                                "Try selecting 'All Districts' or 'All Types'",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(filteredProperties) { property ->
                    PropertyCard(
                        property = property,
                        onInquire = {
                            viewModel.startConversationWith(
                                targetUserId = property.ownerUserId,
                                targetUserName = property.ownerName,
                                subject = "Property Inquiry: ${property.title} (${property.town}, ${property.district})"
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddPropertyDialog) {
        AddPropertyListingDialog(
            viewModel = viewModel,
            onDismiss = { showAddPropertyDialog = false }
        )
    }
}

@Composable
fun PropertyCard(
    property: PropertyListingEntity,
    onInquire: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column {
            // Photo Banner with Overlay Badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                RozgarAsyncImage(
                    model = property.imageUris.ifBlank {
                        when {
                            property.propertyType.contains("Villa", ignoreCase = true) -> "https://images.unsplash.com/photo-1613977257363-707ba9348227?w=600"
                            property.propertyType.contains("Flat", ignoreCase = true) -> "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=600"
                            property.propertyType.contains("Shop", ignoreCase = true) -> "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600"
                            property.propertyType.contains("Orchard", ignoreCase = true) -> "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=600"
                            property.propertyType.contains("Office", ignoreCase = true) -> "https://images.unsplash.com/photo-1497366216548-37526070297c?w=600"
                            property.propertyType.contains("Hall", ignoreCase = true) || property.propertyType.contains("Commercial", ignoreCase = true) -> "https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=600"
                            property.propertyType.contains("Land", ignoreCase = true) || property.propertyType.contains("Plot", ignoreCase = true) -> "https://images.unsplash.com/photo-1500382017468-9049fed747ef?w=600"
                            property.propertyType.contains("Student", ignoreCase = true) || property.propertyType.contains("Shared", ignoreCase = true) -> "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?w=600"
                            else -> "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?w=600"
                        }
                    },
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        KashmirBadge(
                            text = if (property.listingType == "Rent") "FOR RENT" else "FOR SALE",
                            containerColor = if (property.listingType == "Rent") KashmirDeepGreen else KashmirWarmGold,
                            contentColor = Color.White
                        )
                        KashmirBadge(
                            text = property.propertyType,
                            containerColor = Color.Black.copy(alpha = 0.65f),
                            contentColor = Color.White
                        )
                    }

                    KashmirBadge(
                        text = "AVAILABLE",
                        containerColor = KashmirMint,
                        contentColor = KashmirDeepGreen
                    )
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (property.listingType == "Rent") "₹${property.rentOrPrice.toInt()}/month"
                        else "₹${(property.rentOrPrice / 100000).format(1)} Lakhs",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KashmirDeepGreen
                    )
                    if (property.deposit > 0.0) {
                        Text(
                            text = "Deposit: ₹${property.deposit.toInt()}",
                            fontSize = 11.sp,
                            color = KashmirTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.town}, ${property.district} • ${property.address}",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (property.bedrooms > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bed, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${property.bedrooms} Bed", fontSize = 11.sp, color = KashmirTextMuted)
                        }
                    }
                    if (property.bathrooms > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bathtub, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${property.bathrooms} Bath", fontSize = 11.sp, color = KashmirTextMuted)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Chair, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(property.furnishing, fontSize = 11.sp, color = KashmirTextMuted)
                    }
                }

                if (property.amenities.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Amenities: ${property.amenities}",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = property.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Owner / Agent:", fontSize = 10.sp, color = KashmirTextMuted)
                        Text(property.ownerName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirDeepGreen)
                    }

                    Button(
                        onClick = onInquire,
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("inquire_property_${property.id}")
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Inquire / View", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPropertyListingDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("Flat / Apartment") }
    var listingType by remember { mutableStateOf("Rent") }
    var priceText by remember { mutableStateOf("12000") }
    var district by remember { mutableStateOf("Srinagar") }
    var town by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var bedroomsText by remember { mutableStateOf("2") }
    var amenities by remember { mutableStateOf("Hamam Heating, 24/7 Water, Parking") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .imePadding()
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Text(
                        "List a Property in Kashmir",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Text(
                        "Rooms, flats, commercial space, or orchards for rent/sale.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Property Title *") },
                        placeholder = { Text("e.g. Sunny 2BHK Flat with Hamam in Rajbagh") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Rent", "Sale").forEach { mode ->
                            FilterChip(
                                selected = listingType == mode,
                                onClick = { listingType = mode },
                                label = { Text("For $mode") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text(if (listingType == "Rent") "Monthly Rent (₹) *" else "Total Sale Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = town,
                            onValueChange = { town = it },
                            label = { Text("Town / Area *") },
                            placeholder = { Text("e.g. Dalgate") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Specific Locality / Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = amenities,
                        onValueChange = { amenities = it },
                        label = { Text("Amenities / Features") },
                        placeholder = { Text("e.g. Khatamband, Backup Inverter, Hamam") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = KashmirTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val price = priceText.toDoubleOrNull() ?: 10000.0
                                val beds = bedroomsText.toIntOrNull() ?: 1
                                if (title.isNotBlank()) {
                                    viewModel.submitPropertyListing(
                                        title = title,
                                        description = description,
                                        propertyType = propertyType,
                                        listingType = listingType,
                                        price = price,
                                        district = district,
                                        town = town.ifBlank { district },
                                        address = address,
                                        bedrooms = beds,
                                        amenities = amenities,
                                        onComplete = { onDismiss() }
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && priceText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("submit_property_button")
                        ) {
                            Text("Publish Property", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun Double.format(digits: Int): String = "%.${digits}f".format(this)
