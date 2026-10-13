package com.example.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DeliveryAddressEntity
import com.example.ui.theme.*
import com.example.util.LocationHelper
import com.example.util.ResolvedAddress
import kotlinx.coroutines.launch

@Composable
fun DeliveryAddressSelectorSection(
    addresses: List<DeliveryAddressEntity>,
    selectedAddressId: Long,
    onSelectAddress: (DeliveryAddressEntity) -> Unit,
    onAddNewAddress: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = KashmirDeepGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delivery Address",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
            }

            TextButton(
                onClick = onAddNewAddress,
                modifier = Modifier.testTag("add_new_address_btn")
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = KashmirEmerald)
                Spacer(modifier = Modifier.width(2.dp))
                Text("Add New", color = KashmirEmerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (addresses.isEmpty()) {
            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddNewAddress() }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddLocationAlt, null, tint = KashmirEmerald, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No saved delivery address",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = KashmirTextPrimary
                        )
                        Text(
                            text = "Tap to use current location or enter your Kashmir street address",
                            fontSize = 11.sp,
                            color = KashmirTextSecondary
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = KashmirTextMuted)
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(addresses) { addr ->
                    val isSelected = addr.id == selectedAddressId
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { onSelectAddress(addr) }
                            .testTag("address_card_${addr.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) KashmirSurfaceGreen else Color.White),
                        border = BorderStroke(1.5.dp, if (isSelected) KashmirEmerald else KashmirCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isSelected) KashmirEmerald else KashmirMint,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = addr.label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else KashmirForestGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = addr.recipientName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KashmirTextPrimary
                            )
                            Text(
                                text = "Ph: ${addr.recipientPhone}",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary
                            )
                            Text(
                                text = addr.formattedAddress,
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                maxLines = 2,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAddressDialog(
    initialAddress: DeliveryAddressEntity? = null,
    defaultRecipientName: String = "",
    defaultRecipientPhone: String = "",
    defaultDistrict: String = "Srinagar",
    onSaveAddress: (DeliveryAddressEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var label by remember { mutableStateOf(initialAddress?.label ?: "HOME") }
    var recipientName by remember { mutableStateOf(initialAddress?.recipientName ?: defaultRecipientName) }
    var recipientPhone by remember { mutableStateOf(initialAddress?.recipientPhone ?: defaultRecipientPhone) }
    var houseFlatFloor by remember { mutableStateOf(initialAddress?.houseFlatFloor ?: "") }
    var buildingName by remember { mutableStateOf(initialAddress?.buildingName ?: "") }
    var street by remember { mutableStateOf(initialAddress?.street ?: "") }
    var locality by remember { mutableStateOf(initialAddress?.locality ?: "") }
    var landmark by remember { mutableStateOf(initialAddress?.landmark ?: "") }
    var district by remember { mutableStateOf(initialAddress?.district ?: defaultDistrict) }
    var pincode by remember { mutableStateOf(initialAddress?.pincode ?: "190001") }
    var deliveryInstructions by remember { mutableStateOf(initialAddress?.deliveryInstructions ?: "") }
    var latitude by remember { mutableDoubleStateOf(initialAddress?.latitude ?: 34.0837) }
    var longitude by remember { mutableDoubleStateOf(initialAddress?.longitude ?: 74.7973) }
    var isDefault by remember { mutableStateOf(initialAddress?.isDefault ?: true) }

    var isLocating by remember { mutableStateOf(false) }
    var locationSuccessMsg by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    // Search dialog state
    var showSearchSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ResolvedAddress>>(emptyList()) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            scope.launch {
                isLocating = true
                val loc = LocationHelper.fetchCurrentLocation(context)
                isLocating = false
                if (loc != null) {
                    latitude = loc.latitude
                    longitude = loc.longitude
                    locality = loc.locality
                    district = loc.district
                    pincode = loc.pincode
                    street = loc.street
                    houseFlatFloor = loc.houseOrBuilding
                    locationSuccessMsg = "Location pinned: ${loc.locality}, ${loc.district}"
                }
            }
        } else {
            errorMessage = "Location permission denied. Please enter address manually."
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (initialAddress == null) "Add Delivery Address" else "Edit Address",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "Kashmir Valley verified courier route",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons: Use Current Location & Search Address
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (LocationHelper.hasLocationPermission(context)) {
                                    scope.launch {
                                        isLocating = true
                                        val loc = LocationHelper.fetchCurrentLocation(context)
                                        isLocating = false
                                        if (loc != null) {
                                            latitude = loc.latitude
                                            longitude = loc.longitude
                                            locality = loc.locality
                                            district = loc.district
                                            pincode = loc.pincode
                                            street = loc.street
                                            houseFlatFloor = loc.houseOrBuilding
                                            locationSuccessMsg = "Pinned: ${loc.locality}, ${loc.district}"
                                        }
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("use_current_location_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.MyLocation, null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Current Location", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showSearchSheet = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("search_places_btn"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp), tint = KashmirEmerald)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Search Places", fontSize = 11.sp, color = KashmirDeepGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (locationSuccessMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(locationSuccessMsg, fontSize = 11.sp, color = KashmirForestGreen, fontWeight = FontWeight.SemiBold)
                    }
                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(errorMessage, fontSize = 11.sp, color = KashmirError, fontWeight = FontWeight.SemiBold)
                    }

                    // Google Maps API Credentials Status & Pin Confirm Visual Card
                    Spacer(modifier = Modifier.height(8.dp))
                    val isGoogleMapsConfigured = remember {
                        val key = com.example.BuildConfig.GOOGLE_MAPS_API_KEY
                        key.isNotBlank() && !key.contains("DEFAULT_")
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KashmirSurfaceGreen,
                        border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PinDrop, contentDescription = null, tint = KashmirDeepGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Map Pin & Coordinates", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirDeepGreen)
                                }
                                Surface(
                                    color = if (isGoogleMapsConfigured) KashmirMint else Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isGoogleMapsConfigured) "Maps SDK Ready" else "Google Maps Key Unset",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGoogleMapsConfigured) KashmirForestGreen else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (!isGoogleMapsConfigured) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Google Places/Maps API key is not configured yet. Fallback geocoder and offline Kashmir town directory are active. Pinned coordinates will be saved securely.",
                                    fontSize = 10.sp,
                                    color = KashmirTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lat: ${"%.4f".format(latitude)} • Long: ${"%.4f".format(longitude)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KashmirTextPrimary
                                )
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            isLocating = true
                                            val resolved = LocationHelper.reverseGeocode(context, latitude, longitude)
                                            isLocating = false
                                            locality = resolved.locality
                                            district = resolved.district
                                            pincode = resolved.pincode
                                            street = resolved.street.ifBlank { street }
                                            locationSuccessMsg = "Pin synced: ${resolved.locality}, ${resolved.district}"
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = KashmirEmerald)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Re-Geocode Pin", fontSize = 10.sp, color = KashmirEmerald, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Address Type: HOME, WORK, OTHER
                    Text("Address Label", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirTextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HOME", "WORK", "OTHER").forEach { l ->
                            FilterChip(
                                selected = label == l,
                                onClick = { label = l },
                                label = { Text(l, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KashmirMint,
                                    selectedLabelColor = KashmirForestGreen
                                )
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Recipient Mobile Phone *") },
                        placeholder = { Text("10-digit mobile number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = houseFlatFloor,
                            onValueChange = { houseFlatFloor = it },
                            label = { Text("House / Flat / Shop #") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = buildingName,
                            onValueChange = { buildingName = it },
                            label = { Text("Building / Estate") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("Street / Mohalla / Lane *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = locality,
                            onValueChange = { locality = it },
                            label = { Text("Locality / Town *") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it },
                            label = { Text("PIN Code *") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = landmark,
                        onValueChange = { landmark = it },
                        label = { Text("Nearby Landmark (e.g. Near Jamia Masjid / Bridge) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    // District selection dropdown
                    var districtExpanded by remember { mutableStateOf(false) }
                    val kashmirDistricts = listOf(
                        "Srinagar", "Baramulla", "Anantnag", "Budgam", "Pulwama",
                        "Kupwara", "Shopian", "Ganderbal", "Kulgam", "Bandipora"
                    )

                    ExposedDropdownMenuBox(
                        expanded = districtExpanded,
                        onExpandedChange = { districtExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = district,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kashmir District *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false }
                        ) {
                            kashmirDistricts.forEach { dist ->
                                DropdownMenuItem(
                                    text = { Text(dist) },
                                    onClick = {
                                        district = dist
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = deliveryInstructions,
                        onValueChange = { deliveryInstructions = it },
                        label = { Text("Delivery Instructions (Optional)") },
                        placeholder = { Text("e.g. Leave with security / Call upon arrival") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                        Text("Set as default delivery address", fontSize = 12.sp)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (recipientName.isBlank()) {
                                errorMessage = "Please enter recipient name"
                                return@Button
                            }
                            if (recipientPhone.isBlank() || recipientPhone.length < 10) {
                                errorMessage = "Please enter valid 10-digit recipient phone number"
                                return@Button
                            }
                            if (street.isBlank() && locality.isBlank()) {
                                errorMessage = "Please enter street or locality"
                                return@Button
                            }
                            if (pincode.isBlank()) {
                                errorMessage = "Please enter valid PIN Code"
                                return@Button
                            }

                            val parts = listOfNotNull(
                                houseFlatFloor.ifBlank { null },
                                buildingName.ifBlank { null },
                                street.ifBlank { null },
                                locality.ifBlank { null },
                                landmark.ifBlank { null }?.let { "Near $it" },
                                "$district, Jammu & Kashmir $pincode"
                            )
                            val formatted = parts.joinToString(", ")

                            val entity = DeliveryAddressEntity(
                                id = initialAddress?.id ?: 0L,
                                userId = initialAddress?.userId ?: 1L,
                                label = label,
                                recipientName = recipientName.trim(),
                                recipientPhone = recipientPhone.trim(),
                                houseFlatFloor = houseFlatFloor.trim(),
                                buildingName = buildingName.trim(),
                                street = street.trim(),
                                locality = locality.trim(),
                                landmark = landmark.trim(),
                                district = district,
                                state = "Jammu & Kashmir",
                                pincode = pincode.trim(),
                                latitude = latitude,
                                longitude = longitude,
                                formattedAddress = formatted,
                                deliveryInstructions = deliveryInstructions.trim(),
                                isDefault = isDefault
                            )
                            onSaveAddress(entity)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_address_btn")
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm & Save Delivery Address", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Search Kashmir Places Sheet
    if (showSearchSheet) {
        Dialog(onDismissRequest = { showSearchSheet = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Search Kashmir Locality",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            scope.launch {
                                searchResults = LocationHelper.searchKashmirPlaces(it)
                            }
                        },
                        placeholder = { Text("Search Lal Chowk, Sopore, Hazratbal, KP Road...") },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                    ) {
                        if (searchResults.isEmpty()) {
                            item {
                                Text(
                                    text = if (searchQuery.isBlank()) "Type a locality, chowk, or town name" else "No matching Kashmir locations found",
                                    color = KashmirTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        } else {
                            items(searchResults) { res ->
                                Surface(
                                    color = Color.White,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            latitude = res.latitude
                                            longitude = res.longitude
                                            locality = res.locality
                                            street = res.street
                                            district = res.district
                                            pincode = res.pincode
                                            buildingName = res.houseOrBuilding
                                            landmark = res.landmark
                                            locationSuccessMsg = "Selected: ${res.locality}, ${res.district}"
                                            showSearchSheet = false
                                        }
                                        .padding(vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Place, null, tint = KashmirEmerald, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(res.formattedAddress, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            Text("District: ${res.district} • PIN: ${res.pincode}", fontSize = 10.sp, color = KashmirTextSecondary)
                                        }
                                    }
                                }
                                HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showSearchSheet = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
