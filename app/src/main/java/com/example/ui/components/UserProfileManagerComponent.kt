package com.example.ui.components

import android.Manifest
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ServiceHistoryItem
import com.example.data.model.UserProfileFirestore
import com.example.ui.theme.*
import com.example.util.SpeechToTextManager
import com.example.viewmodel.RozgarViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.text.SimpleDateFormat
import java.util.*

val TOP_KASHMIR_SKILLS = listOf(
    "Walnut Wood Carving",
    "Pashmina Embroidery",
    "Apple Picking & Grading",
    "Orchard Pruning",
    "Saffron Harvest & Curing",
    "Carpet Weaving (Kaleen)",
    "Kandkari Copperware",
    "Masonry & Bricklaying",
    "Plumbing & Sanitation",
    "House Carpentry",
    "Electrical Wiring",
    "House Painting & POP",
    "Tilla & Sozni Embroidery",
    "Wazwan Catering",
    "Driver / Transport",
    "Tractor Operation",
    "Roofing & Tin Work",
    "Tourism Guide & Hospitality"
)

val KASHMIR_LANGUAGES = listOf("Kashmiri", "Urdu", "English", "Gojri", "Pahari", "Hindi")
val SERVICE_STATUSES = listOf("Completed", "In Progress", "Verified by Client")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalPermissionsApi::class)
@Composable
fun UserProfileManagerComponent(
    viewModel: RozgarViewModel,
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val firestoreProfile by viewModel.firestoreUserProfile.collectAsState()
    val isSyncing by viewModel.isSyncingProfileWithFirestore.collectAsState()

    // Active sub-section tab: 0: Personal Details, 1: Skills, 2: Service History
    var activeSectionTab by remember { mutableIntStateOf(0) }

    // Draft Profile State initialized from Firestore profile or local user
    var userRole by remember { mutableStateOf(firestoreProfile?.userRole ?: "WORKER") }
    var fullName by remember { mutableStateOf(firestoreProfile?.fullName ?: currentUser?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(firestoreProfile?.phoneNumber ?: currentUser?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(firestoreProfile?.email ?: currentUser?.email ?: "") }
    var district by remember { mutableStateOf(firestoreProfile?.district ?: currentUser?.district ?: KASHMIR_DISTRICTS.first()) }
    var area by remember { mutableStateOf(firestoreProfile?.area ?: currentUser?.area ?: "") }
    var bio by remember { mutableStateOf(firestoreProfile?.bio ?: "") }
    var selectedLanguages by remember { mutableStateOf(firestoreProfile?.languages ?: listOf("Kashmiri", "Urdu")) }

    // Worker fields
    var profession by remember { mutableStateOf(firestoreProfile?.profession ?: "Artisan / Craftsman") }
    var occupation by remember { mutableStateOf(firestoreProfile?.occupation ?: "Specialist") }
    var skillsList by remember { mutableStateOf(firestoreProfile?.skills ?: listOf("Walnut Wood Carving", "Quality Assurance")) }
    var skillProficiencies by remember { mutableStateOf(firestoreProfile?.skillProficiencies ?: mapOf("Walnut Wood Carving" to "Master", "Quality Assurance" to "Expert")) }
    var experienceYears by remember { mutableIntStateOf(firestoreProfile?.experienceYears ?: 4) }
    var expectedPayment by remember { mutableStateOf(firestoreProfile?.expectedPayment ?: "₹800/day") }
    var paymentFrequency by remember { mutableStateOf(firestoreProfile?.paymentFrequency ?: "Daily") }
    var workPreference by remember { mutableStateOf(firestoreProfile?.workPreference ?: "On-site") }
    var availability by remember { mutableStateOf(firestoreProfile?.availability ?: "Available Now") }
    var toolsOwned by remember { mutableStateOf(firestoreProfile?.toolsOwned ?: "") }

    // Employer fields
    var businessOrEstateName by remember { mutableStateOf(firestoreProfile?.businessOrEstateName ?: "") }
    var industry by remember { mutableStateOf(firestoreProfile?.industry ?: "Agriculture & Kashmiri Crafts") }
    var businessAddress by remember { mutableStateOf(firestoreProfile?.businessAddress ?: "") }
    var totalWorkersEngaged by remember { mutableIntStateOf(firestoreProfile?.totalWorkersEngaged ?: 12) }
    var paymentTerms by remember { mutableStateOf(firestoreProfile?.paymentTerms ?: "Weekly Settlement") }

    // Service History local list
    var serviceHistoryList by remember { mutableStateOf(firestoreProfile?.serviceHistory ?: emptyList()) }

    // Update draft whenever cloud state updates
    LaunchedEffect(firestoreProfile) {
        firestoreProfile?.let { prof ->
            userRole = prof.userRole
            fullName = prof.fullName
            phoneNumber = prof.phoneNumber
            email = prof.email
            district = prof.district
            area = prof.area
            bio = prof.bio
            selectedLanguages = prof.languages
            profession = prof.profession
            occupation = prof.occupation
            skillsList = prof.skills
            skillProficiencies = prof.skillProficiencies
            experienceYears = prof.experienceYears
            expectedPayment = prof.expectedPayment
            paymentFrequency = prof.paymentFrequency
            workPreference = prof.workPreference
            availability = prof.availability
            toolsOwned = prof.toolsOwned
            businessOrEstateName = prof.businessOrEstateName
            industry = prof.industry
            businessAddress = prof.businessAddress
            totalWorkersEngaged = prof.totalWorkersEngaged
            paymentTerms = prof.paymentTerms
            serviceHistoryList = prof.serviceHistory
        }
    }

    // New/Edit Service Dialog State
    var showServiceDialog by remember { mutableStateOf(false) }
    var editingServiceItem by remember { mutableStateOf<ServiceHistoryItem?>(null) }

    // Custom Skill Input State
    var customSkillInput by remember { mutableStateOf("") }
    var skillSearchQuery by remember { mutableStateOf("") }

    // Speech to text manager for voice input on Bio and Name
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val speechManager = remember { SpeechToTextManager(context) }
    val isListening by speechManager.isListening.collectAsState()
    val recognizedText by speechManager.recognizedText.collectAsState()
    var voiceTargetField by remember { mutableStateOf("") } // "NAME" or "BIO"

    DisposableEffect(speechManager) {
        onDispose { speechManager.destroy() }
    }

    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            if (voiceTargetField == "NAME") {
                fullName = recognizedText.trim().replaceFirstChar { it.uppercase() }
            } else if (voiceTargetField == "BIO") {
                bio = if (bio.isBlank()) recognizedText else "$bio $recognizedText"
            }
            voiceTargetField = ""
        }
    }

    fun startVoiceInput(field: String) {
        voiceTargetField = field
        if (audioPermissionState.status.isGranted) {
            speechManager.startListening("en-IN")
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    fun buildCurrentProfile(): UserProfileFirestore {
        val uid = firestoreProfile?.userId ?: currentUser?.id?.toString() ?: "1"
        return UserProfileFirestore(
            userId = uid,
            userRole = userRole,
            fullName = fullName.trim(),
            phoneNumber = phoneNumber.trim(),
            email = email.trim(),
            district = district,
            area = area.trim(),
            bio = bio.trim(),
            languages = selectedLanguages,
            profession = profession,
            occupation = occupation,
            skills = skillsList,
            skillProficiencies = skillProficiencies,
            experienceYears = experienceYears,
            expectedPayment = expectedPayment,
            paymentFrequency = paymentFrequency,
            workPreference = workPreference,
            availability = availability,
            toolsOwned = toolsOwned,
            businessOrEstateName = businessOrEstateName.trim(),
            industry = industry.trim(),
            businessAddress = businessAddress.trim(),
            totalWorkersEngaged = totalWorkersEngaged,
            paymentTerms = paymentTerms,
            serviceHistory = serviceHistoryList,
            lastSyncedAt = System.currentTimeMillis()
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KashmirBackground)
    ) {
        // --- 1. Cloud Firestore Status & Header Banner ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KashmirGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Cloud Firestore",
                                tint = KashmirDeepGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Firebase Firestore Profile",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KashmirEmerald,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE CLOUD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            val lastSyncFormatted = remember(firestoreProfile?.lastSyncedAt) {
                                val ts = firestoreProfile?.lastSyncedAt ?: System.currentTimeMillis()
                                SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(ts))
                            }
                            Text(
                                text = "Last Synced: $lastSyncFormatted",
                                fontSize = 11.sp,
                                color = KashmirMint
                            )
                        }
                    }

                    // Sync Now Button
                    IconButton(
                        onClick = { viewModel.syncCurrentProfileWithFirestore() },
                        enabled = !isSyncing,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("firestore_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = KashmirGold, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync Now", tint = KashmirGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector: Worker vs Employer Profile
                Surface(
                    color = KashmirForestGreen,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Worker Profile Tab
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userRole == "WORKER") KashmirGold else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { userRole = "WORKER" }
                                .testTag("role_worker_tab")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = if (userRole == "WORKER") KashmirDeepGreen else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Worker Profile",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userRole == "WORKER") KashmirDeepGreen else Color.White
                                )
                            }
                        }

                        // Employer Profile Tab
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (userRole == "EMPLOYER") KashmirGold else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { userRole = "EMPLOYER" }
                                .testTag("role_employer_tab")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BusinessCenter,
                                    contentDescription = null,
                                    tint = if (userRole == "EMPLOYER") KashmirDeepGreen else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Employer Profile",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userRole == "EMPLOYER") KashmirDeepGreen else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Sub-Section Tabs (Details, Skills, Service History) ---
        TabRow(
            selectedTabIndex = activeSectionTab,
            containerColor = Color.White,
            contentColor = KashmirDeepGreen,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, KashmirCardBorder, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeSectionTab == 0,
                onClick = { activeSectionTab = 0 },
                modifier = Modifier.testTag("tab_personal_details"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = activeSectionTab == 1,
                onClick = { activeSectionTab = 1 },
                modifier = Modifier.testTag("tab_skills"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Handyman, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skills (${skillsList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = activeSectionTab == 2,
                onClick = { activeSectionTab = 2 },
                modifier = Modifier.testTag("tab_service_history"),
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("History (${serviceHistoryList.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. Sub-Section Contents ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // ==================== TAB 0: PERSONAL & BUSINESS DETAILS ====================
            if (activeSectionTab == 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, KashmirCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Personal & Contact Information",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "Keep your profile up to date for local jobs & contracts.",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Full Name with Voice Mic
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name *") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = KashmirEmerald) },
                                trailingIcon = {
                                    IconButton(onClick = { startVoiceInput("NAME") }) {
                                        Icon(
                                            Icons.Default.Mic,
                                            contentDescription = "Speak Name",
                                            tint = if (isListening && voiceTargetField == "NAME") KashmirGold else KashmirEmerald
                                        )
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_full_name_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Phone Number (Private)
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number (Private Shield Guaranteed)") },
                                leadingIcon = { Icon(Icons.Default.Phone, null, tint = KashmirEmerald) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("profile_phone_input"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email (Optional)
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address (Optional)") },
                                placeholder = { Text("e.g. craftsman@rozgarkashmir.in") },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = KashmirEmerald) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // District Selector
                            var districtExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = districtExpanded,
                                onExpandedChange = { districtExpanded = !districtExpanded }
                            ) {
                                OutlinedTextField(
                                    value = district,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("District in Jammu & Kashmir *") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = districtExpanded,
                                    onDismissRequest = { districtExpanded = false }
                                ) {
                                    KASHMIR_DISTRICTS.forEach { d ->
                                        DropdownMenuItem(
                                            text = { Text(d) },
                                            onClick = {
                                                district = d
                                                districtExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = area,
                                onValueChange = { area = it },
                                label = { Text("Area / Tehsil / Village") },
                                placeholder = { Text("e.g. Sopore Fruit Mandi, Lal Chowk, Pahalgam") },
                                leadingIcon = { Icon(Icons.Default.Map, null, tint = KashmirEmerald) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bio / Description with Voice Input
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Bio / Summary of Experience") },
                                placeholder = { Text("Describe your services, background, and specializations...") },
                                leadingIcon = { Icon(Icons.Default.Description, null, tint = KashmirEmerald) },
                                trailingIcon = {
                                    IconButton(onClick = { startVoiceInput("BIO") }) {
                                        Icon(
                                            Icons.Default.Mic,
                                            contentDescription = "Speak Bio",
                                            tint = if (isListening && voiceTargetField == "BIO") KashmirGold else KashmirEmerald
                                        )
                                    }
                                },
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Languages spoken
                            Text(
                                text = "Languages Spoken:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                KASHMIR_LANGUAGES.forEach { lang ->
                                    val isSelected = selectedLanguages.contains(lang)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedLanguages = if (isSelected) {
                                                selectedLanguages.filterNot { it == lang }
                                            } else {
                                                selectedLanguages + lang
                                            }
                                        },
                                        label = { Text(lang, fontSize = 11.sp) },
                                        leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) } } else null
                                    )
                                }
                            }
                        }
                    }
                }

                // Role-Specific Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, KashmirCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            if (userRole == "WORKER") {
                                Text(
                                    text = "Craft & Work Preferences",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Details displayed to employers looking to hire you.",
                                    fontSize = 12.sp,
                                    color = KashmirTextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Profession Dropdown
                                var professionExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = professionExpanded,
                                    onExpandedChange = { professionExpanded = !professionExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = profession,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Primary Profession / Trade *") },
                                        leadingIcon = { Icon(Icons.Default.Work, null, tint = KashmirEmerald) },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = professionExpanded) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = professionExpanded,
                                        onDismissRequest = { professionExpanded = false }
                                    ) {
                                        KASHMIR_PROFESSIONS.forEach { prof ->
                                            DropdownMenuItem(
                                                text = { Text(prof) },
                                                onClick = {
                                                    profession = prof
                                                    professionExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = expectedPayment,
                                        onValueChange = { expectedPayment = it },
                                        label = { Text("Expected Wage") },
                                        placeholder = { Text("₹800/day") },
                                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, null, tint = KashmirEmerald) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )

                                    OutlinedTextField(
                                        value = experienceYears.toString(),
                                        onValueChange = { experienceYears = it.toIntOrNull() ?: 1 },
                                        label = { Text("Years Exp.") },
                                        leadingIcon = { Icon(Icons.Default.Timeline, null, tint = KashmirEmerald) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Work preference chips (On-site, Remote, Hybrid)
                                Text("Work Preference:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("On-site", "Remote", "Hybrid").forEach { pref ->
                                        FilterChip(
                                            selected = workPreference == pref,
                                            onClick = { workPreference = pref },
                                            label = { Text(pref) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Availability status
                                Text("Availability Status:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Available Now", "Available Next Week", "Seasonal Worker", "Booked").forEach { avail ->
                                        FilterChip(
                                            selected = availability == avail,
                                            onClick = { availability = avail },
                                            label = { Text(avail) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = toolsOwned,
                                    onValueChange = { toolsOwned = it },
                                    label = { Text("Tools & Equipment Owned") },
                                    placeholder = { Text("e.g. Chisels, Drill, Safety Harness, Ladders") },
                                    leadingIcon = { Icon(Icons.Default.Construction, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            } else {
                                // EMPLOYER FIELDS
                                Text(
                                    text = "Employer & Business Information",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Details shared with workers applying to your job postings.",
                                    fontSize = 12.sp,
                                    color = KashmirTextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = businessOrEstateName,
                                    onValueChange = { businessOrEstateName = it },
                                    label = { Text("Business / Orchard / Firm Name *") },
                                    placeholder = { Text("e.g. Sopore Valley Orchards & Packing") },
                                    leadingIcon = { Icon(Icons.Default.Store, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = industry,
                                    onValueChange = { industry = it },
                                    label = { Text("Industry / Sector") },
                                    placeholder = { Text("e.g. Apple Orchard, Handicrafts, Construction") },
                                    leadingIcon = { Icon(Icons.Default.Category, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = businessAddress,
                                    onValueChange = { businessAddress = it },
                                    label = { Text("Workplace / Orchard Location") },
                                    placeholder = { Text("e.g. Fruit Mandi Road, Shopian") },
                                    leadingIcon = { Icon(Icons.Default.PinDrop, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = paymentTerms,
                                    onValueChange = { paymentTerms = it },
                                    label = { Text("Payment Terms for Workers") },
                                    placeholder = { Text("e.g. Daily Cash, Weekly Bank Settlement") },
                                    leadingIcon = { Icon(Icons.Default.Payments, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ==================== TAB 1: SKILLS MANAGEMENT ====================
            if (activeSectionTab == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, KashmirCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Skills & Expertise Inventory",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "Add standard or custom Kashmir skills and set your proficiency level.",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Custom Skill Input with Add Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSkillInput,
                                    onValueChange = { customSkillInput = it },
                                    label = { Text("Add Any Custom Skill") },
                                    placeholder = { Text("e.g. Walnut Carving, Tilla Work") },
                                    leadingIcon = { Icon(Icons.Default.Add, null, tint = KashmirEmerald) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("custom_skill_input"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        val trimmed = customSkillInput.trim()
                                        if (trimmed.isNotBlank() && !skillsList.contains(trimmed)) {
                                            skillsList = skillsList + trimmed
                                            skillProficiencies = skillProficiencies + (trimmed to "Expert")
                                            customSkillInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .height(54.dp)
                                        .testTag("add_custom_skill_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                                ) {
                                    Text("Add")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Suggested Top Kashmir Skills
                            Text(
                                text = "Suggested Kashmir Trades (1-Tap Add):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(TOP_KASHMIR_SKILLS) { skill ->
                                    val isAdded = skillsList.contains(skill)
                                    SuggestionChip(
                                        onClick = {
                                            if (!isAdded) {
                                                skillsList = skillsList + skill
                                                skillProficiencies = skillProficiencies + (skill to "Expert")
                                            }
                                        },
                                        label = { Text(skill, fontSize = 11.sp) },
                                        icon = {
                                            Icon(
                                                imageVector = if (isAdded) Icons.Default.Check else Icons.Default.Add,
                                                contentDescription = null,
                                                tint = if (isAdded) KashmirEmerald else KashmirTextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.6f))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Active Profile Skills List with Proficiency
                            Text(
                                text = "Your Configured Skills (${skillsList.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                            Text(
                                text = "Tap a skill chip to cycle proficiency: Beginner ➔ Intermediate ➔ Expert ➔ Master",
                                fontSize = 11.sp,
                                color = KashmirTextMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (skillsList.isEmpty()) {
                                Surface(
                                    color = KashmirMint.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No skills added yet. Tap suggested trades above or add custom skills.",
                                        fontSize = 12.sp,
                                        color = KashmirDeepGreen,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    skillsList.forEach { skill ->
                                        val level = skillProficiencies[skill] ?: "Expert"
                                        val levelColor = when (level) {
                                            "Master" -> KashmirGold
                                            "Expert" -> KashmirDeepGreen
                                            "Intermediate" -> KashmirEmerald
                                            else -> KashmirTextSecondary
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = KashmirBackground,
                                            border = BorderStroke(1.dp, KashmirCardBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Verified, null, tint = levelColor, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(skill, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text("Proficiency: $level", fontSize = 11.sp, color = levelColor, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    // Cycle level button
                                                    TextButton(
                                                        onClick = {
                                                            val next = when (level) {
                                                                "Beginner" -> "Intermediate"
                                                                "Intermediate" -> "Expert"
                                                                "Expert" -> "Master"
                                                                else -> "Beginner"
                                                            }
                                                            skillProficiencies = skillProficiencies + (skill to next)
                                                        }
                                                    ) {
                                                        Text("Level ↻", fontSize = 11.sp, color = KashmirEmerald)
                                                    }

                                                    // Remove skill button
                                                    IconButton(
                                                        onClick = {
                                                            skillsList = skillsList.filterNot { it == skill }
                                                            skillProficiencies = skillProficiencies - skill
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, "Remove Skill", tint = KashmirError, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ==================== TAB 2: SERVICE HISTORY ====================
            if (activeSectionTab == 2) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Service History & Contracts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "Documented work records stored in Firestore.",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                        }

                        Button(
                            onClick = {
                                editingServiceItem = null
                                showServiceDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_service_history_button")
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isSyncing) {
                    items(3) {
                        ServiceHistoryCardSkeleton()
                    }
                } else if (serviceHistoryList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, KashmirCardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.WorkHistory, null, tint = KashmirEmerald, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Service Records Yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add past projects, restorations, or contracts to build trust and win higher-value opportunities.",
                                    fontSize = 12.sp,
                                    color = KashmirTextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        editingServiceItem = null
                                        showServiceDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Add First Project")
                                }
                            }
                        }
                    }
                } else {
                    items(serviceHistoryList, key = { it.id }) { service ->
                        val statusColor = when (service.status) {
                            "Completed" -> KashmirDeepGreen
                            "In Progress" -> KashmirWarmGold
                            else -> KashmirEmerald
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, KashmirCardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = service.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = KashmirTextPrimary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Business, null, tint = KashmirTextSecondary, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(service.clientOrEmployerName, fontSize = 12.sp, color = KashmirTextSecondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("•", color = KashmirTextSecondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(service.locationDistrict, fontSize = 12.sp, color = KashmirEmerald, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Surface(
                                        color = statusColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = service.status,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (service.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(service.description, fontSize = 12.sp, color = KashmirTextPrimary, maxLines = 3)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (service.amountEarnedOrBudget.isNotBlank()) {
                                            Text(
                                                text = service.amountEarnedOrBudget,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = KashmirDeepGreen
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        if (service.dateRange.isNotBlank()) {
                                            Text(service.dateRange, fontSize = 11.sp, color = KashmirTextMuted)
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingServiceItem = service
                                                showServiceDialog = true
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                serviceHistoryList = serviceHistoryList.filterNot { it.id == service.id }
                                                viewModel.removeServiceHistoryRecord(service.id)
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = KashmirError, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Bottom Sticky Action Bar: Save to Firestore ---
        Surface(
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, KashmirCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }

                Button(
                    onClick = {
                        val profileToSave = buildCurrentProfile()
                        viewModel.saveFirestoreUserProfile(profileToSave)
                    },
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("save_profile_firestore_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Syncing Firestore...")
                    } else {
                        Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Sync to Firestore", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // --- 5. Add / Edit Service History Dialog ---
    if (showServiceDialog) {
        val isEditing = editingServiceItem != null
        var itemTitle by remember { mutableStateOf(editingServiceItem?.title ?: "") }
        var itemClient by remember { mutableStateOf(editingServiceItem?.clientOrEmployerName ?: "") }
        var itemCategory by remember { mutableStateOf(editingServiceItem?.category ?: "General Services") }
        var itemDate by remember { mutableStateOf(editingServiceItem?.dateRange ?: "") }
        var itemDesc by remember { mutableStateOf(editingServiceItem?.description ?: "") }
        var itemAmount by remember { mutableStateOf(editingServiceItem?.amountEarnedOrBudget ?: "₹15,000") }
        var itemStatus by remember { mutableStateOf(editingServiceItem?.status ?: "Completed") }
        var itemDistrict by remember { mutableStateOf(editingServiceItem?.locationDistrict ?: district) }
        var itemDeliverables by remember { mutableStateOf(editingServiceItem?.deliverables ?: "") }

        Dialog(onDismissRequest = { showServiceDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) "Edit Service Record" else "Add Service Record",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        )
                        IconButton(onClick = { showServiceDialog = false }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("Service / Project Title *") },
                        placeholder = { Text("e.g. Walnut Wood Carved Panel Restoration") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("service_title_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = itemClient,
                        onValueChange = { itemClient = it },
                        label = { Text("Client / Employer / Firm *") },
                        placeholder = { Text("e.g. Heritage Trust Srinagar / Mr. Farooq") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("service_client_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = itemAmount,
                            onValueChange = { itemAmount = it },
                            label = { Text("Amount (₹)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = itemDate,
                            onValueChange = { itemDate = it },
                            label = { Text("Date / Period") },
                            placeholder = { Text("Aug - Sep 2025") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Dropdown
                    var statusExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = itemStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Project Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            SERVICE_STATUSES.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        itemStatus = s
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = itemDesc,
                        onValueChange = { itemDesc = it },
                        label = { Text("Work Description & Deliverables") },
                        placeholder = { Text("Detailed summary of tasks accomplished, materials used, inspection results...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (itemTitle.isBlank()) return@Button
                            val record = ServiceHistoryItem(
                                id = editingServiceItem?.id ?: UUID.randomUUID().toString(),
                                title = itemTitle.trim(),
                                clientOrEmployerName = itemClient.trim().ifBlank { "Private Client" },
                                category = itemCategory,
                                dateRange = itemDate.trim().ifBlank { "Recent" },
                                description = itemDesc.trim(),
                                amountEarnedOrBudget = itemAmount.trim(),
                                status = itemStatus,
                                locationDistrict = itemDistrict,
                                deliverables = itemDeliverables.trim()
                            )

                            if (isEditing) {
                                serviceHistoryList = serviceHistoryList.map { if (it.id == record.id) record else it }
                                viewModel.updateServiceHistoryRecord(record)
                            } else {
                                serviceHistoryList = listOf(record) + serviceHistoryList
                                viewModel.addServiceHistoryRecord(record)
                            }
                            showServiceDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_service_record_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Check, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditing) "Update Record" else "Add to Service History", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
