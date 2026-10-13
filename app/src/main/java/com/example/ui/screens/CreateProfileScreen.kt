package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allWorkers by viewModel.allWorkers.collectAsState()

    val existingProfile = remember(allWorkers, currentUser?.id) {
        allWorkers.firstOrNull { it.userId == currentUser?.id }
    }

    // Form State
    var name by remember { mutableStateOf(existingProfile?.name ?: currentUser?.fullName ?: "") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedAvatarPreset by remember { mutableStateOf(existingProfile?.photoUrl ?: "avatar_craftsman") }
    var profession by remember { mutableStateOf(existingProfile?.profession ?: KASHMIR_PROFESSIONS.first()) }
    var customProfession by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf(existingProfile?.occupation ?: "Craftsman / Specialist") }
    var skills by remember { mutableStateOf(existingProfile?.skills ?: "Walnut Carving, Sanding, Chinar Motifs") }
    var experienceYears by remember { mutableIntStateOf(existingProfile?.experienceYears ?: 4) }
    var previousWork by remember { mutableStateOf(existingProfile?.previousWork ?: "") }
    var services by remember { mutableStateOf(existingProfile?.services ?: "") }
    var education by remember { mutableStateOf(existingProfile?.education ?: "") } // Strictly optional!
    var availability by remember { mutableStateOf(existingProfile?.availability ?: WORKER_AVAILABILITY.first()) }
    var workType by remember { mutableStateOf(existingProfile?.workType ?: JOB_TYPES.first()) }
    var expectedPayment by remember { mutableStateOf(existingProfile?.expectedPayment ?: "₹800/day") }
    var isSeasonalWorker by remember { mutableStateOf(existingProfile?.isSeasonalWorker ?: false) }
    var seasonalSpecialties by remember { mutableStateOf(existingProfile?.seasonalSpecialties ?: "Apple Picking, Orchard Work") }
    var district by remember { mutableStateOf(existingProfile?.district ?: currentUser?.district ?: KASHMIR_DISTRICTS.first()) }
    var area by remember { mutableStateOf(existingProfile?.area ?: currentUser?.area ?: "") }
    var languages by remember { mutableStateOf(existingProfile?.languages ?: "Kashmiri, Urdu") }
    var bio by remember { mutableStateOf(existingProfile?.bio ?: "") }

    var errorMessage by remember { mutableStateOf("") }
    var showVoiceBioModal by remember { mutableStateOf(false) }

    // Speech Recognizer Intent Launcher for About Me / Bio
    val speechBioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                bio = if (bio.isBlank()) spoken else "$bio $spoken"
                Toast.makeText(context, "Voice transcribed into 'About Me'!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val startVoiceRecording = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your craft, skills, and experience in Kashmir...")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            speechBioLauncher.launch(intent)
        } catch (e: Exception) {
            // Fallback to voice bio recording modal with sample Kashmiri artisan transcriptions
            showVoiceBioModal = true
        }
    }

    // Microphone Permission Launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecording()
        } else {
            Toast.makeText(context, "Microphone permission required for direct voice recording", Toast.LENGTH_SHORT).show()
            showVoiceBioModal = true
        }
    }

    fun onMicButtonClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startVoiceRecording()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // --- Profile Completion Meter Calculation ---
    val hasPhoto = (selectedPhotoUri != null || selectedAvatarPreset.isNotBlank())
    val hasBasicInfo = name.isNotBlank() && area.isNotBlank()
    val hasProfession = profession.isNotBlank() && (!profession.contains("Other") || customProfession.isNotBlank())
    val hasSkills = skills.isNotBlank() && skills.split(",").filter { it.isNotBlank() }.size >= 2
    val hasExperience = experienceYears > 0 && previousWork.isNotBlank()
    val hasAvailabilityAndPay = availability.isNotBlank() && expectedPayment.isNotBlank()
    val hasBio = bio.isNotBlank()

    val completionPercentage = remember(hasPhoto, hasBasicInfo, hasProfession, hasSkills, hasExperience, hasAvailabilityAndPay, hasBio) {
        var score = 0
        if (hasPhoto) score += 15
        if (hasBasicInfo) score += 15
        if (hasProfession) score += 15
        if (hasSkills) score += 15
        if (hasExperience) score += 15
        if (hasAvailabilityAndPay) score += 10
        if (hasBio) score += 15
        score.coerceAtMost(100)
    }

    // Zero-permission Android Photo Picker contract
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedAvatarPreset = uri.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (existingProfile != null) "Edit Worker Card" else "Create Worker Profile",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rozgar Kashmir Skills Directory",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirMint)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            // 0. Profile Completion Progress & Checklist Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_completion_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, KashmirGold.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(KashmirGold.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = "Completion",
                                        tint = KashmirWarmGold,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Profile Strength: $completionPercentage%",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KashmirDeepGreen
                                        )
                                    )
                                    Text(
                                        text = if (completionPercentage >= 100) "🌟 Full Worker Card Active"
                                        else if (completionPercentage >= 70) "Almost Ready • High Visibility"
                                        else "In Progress • Add Voice Bio & Skills",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (completionPercentage >= 70) KashmirEmerald else KashmirWarmGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            Surface(
                                color = if (completionPercentage >= 100) KashmirMint else KashmirGold.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (completionPercentage >= 100) "100% READY" else "$completionPercentage% DONE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KashmirDeepGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { completionPercentage / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (completionPercentage >= 70) KashmirEmerald else KashmirWarmGold,
                            trackColor = KashmirMint.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Checklist
                        Text(
                            text = "Profile Completion Checklist:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextSecondary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompletionChip(label = "Photo", done = hasPhoto, modifier = Modifier.weight(1f))
                            CompletionChip(label = "Craft", done = hasProfession, modifier = Modifier.weight(1f))
                            CompletionChip(label = "Skills", done = hasSkills, modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompletionChip(label = "Experience", done = hasExperience, modifier = Modifier.weight(1f))
                            CompletionChip(label = "Rates", done = hasAvailabilityAndPay, modifier = Modifier.weight(1f))
                            CompletionChip(label = "Voice Bio", done = hasBio, modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 1. Privacy Protection Notice
            item {
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
                        Icon(Icons.Default.Lock, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Strict Contact Privacy Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KashmirDeepGreen
                            )
                            Text(
                                text = "Your phone number will NOT be shown on this public profile. Potential employers will connect with you via voice-first chat.",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 2. Photo / Avatar Upload Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Profile Photo / Artisan Avatar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Add a photo of yourself or your workshop to build trust with employers",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Avatar Preview Circle
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(KashmirMint)
                                .border(2.dp, KashmirEmerald, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("profile_photo_picker_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedPhotoUri != null) {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = "Selected Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Pick Photo",
                                        tint = KashmirDeepGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Add Photo",
                                        fontSize = 10.sp,
                                        color = KashmirDeepGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose From Gallery", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Kashmir Artisan Avatar Presets
                        Text(
                            text = "Or choose a Kashmir Craft Avatar Badge:",
                            fontSize = 11.sp,
                            color = KashmirTextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val avatarPresets = listOf(
                            Pair("Walnut Wood Carver", Icons.Default.Architecture),
                            Pair("Pashmina Weaver", Icons.Default.DryCleaning),
                            Pair("Apple Orchardist", Icons.Default.Park),
                            Pair("Plumber & Piping", Icons.Default.Plumbing),
                            Pair("Electrician", Icons.Default.ElectricBolt),
                            Pair("Builder / Mason", Icons.Default.Foundation)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(avatarPresets) { (presetName, icon) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedAvatarPreset == presetName) KashmirEmerald else KashmirMint.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                                    modifier = Modifier.clickable {
                                        selectedAvatarPreset = presetName
                                        selectedPhotoUri = null
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (selectedAvatarPreset == presetName) Color.White else KashmirDeepGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = presetName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selectedAvatarPreset == presetName) Color.White else KashmirDeepGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 3. Name & Profession Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "General Information & Craft",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Ghulam Mohammad Rather") },
                            leadingIcon = { Icon(Icons.Default.Person, null, tint = KashmirEmerald) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_profile_name_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Select Profession / Trade *",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = KashmirTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(KASHMIR_PROFESSIONS) { prof ->
                                FilterChip(
                                    selected = profession == prof,
                                    onClick = { profession = prof },
                                    label = { Text(prof, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirMint,
                                        selectedLabelColor = KashmirDeepGreen
                                    )
                                )
                            }
                        }

                        if (profession.contains("Other")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customProfession,
                                onValueChange = { customProfession = it },
                                label = { Text("Enter Your Exact Profession / Trade *") },
                                placeholder = { Text("e.g. Kashmiri Copper Craftsman (Kandkari)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("create_profile_custom_profession"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text("Title / Specialty Title") },
                            placeholder = { Text("e.g. Master Woodcarver / Orchard Lead") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 4. Skills & Experience
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Skills & Experience",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "List key techniques, tools, or specializations you excel at",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Unlimited Skills with Search, Top 5/Top 10 suggestions, and Add Custom Skill
                        val currentSkillsList = remember(skills) {
                            skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        }

                        SkillPickerComponent(
                            selectedSkills = currentSkillsList,
                            onSkillsChanged = { updatedList ->
                                skills = updatedList.joinToString(", ")
                            },
                            profession = if (profession.contains("Other")) customProfession else profession,
                            label = "Unlimited Professional Skills",
                            hint = "Search or type any skill related to your profession..."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Experience Years
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Experience Duration", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$experienceYears Years of Professional Work", fontSize = 12.sp, color = KashmirEmerald, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledIconButton(
                                    onClick = { if (experienceYears > 1) experienceYears-- },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = KashmirMint),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null, tint = KashmirDeepGreen)
                                }
                                Text(
                                    text = "$experienceYears",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp)
                                )
                                FilledIconButton(
                                    onClick = { if (experienceYears < 40) experienceYears++ },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = KashmirMint),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = KashmirDeepGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = previousWork,
                            onValueChange = { previousWork = it },
                            label = { Text("Previous Work & Projects") },
                            placeholder = { Text("e.g. Carved ceiling motifs in Srinagar heritage home, 5 seasons orchard harvesting in Sopore") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = services,
                            onValueChange = { services = it },
                            label = { Text("Services Offered") },
                            placeholder = { Text("e.g. Custom furniture carving, restoration, onsite repairs") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 5. Education Section (STRICTLY OPTIONAL)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, null, tint = KashmirWarmGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Education (OPTIONAL — Not Mandatory)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = KashmirDeepGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "In Kashmir, the greatest master artisans and agricultural experts learn through heritage, family tradition, and hands-on apprenticeship. Formal degrees or diplomas are NOT required. Leave this blank if self-taught or informally trained.",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = education,
                            onValueChange = { education = it },
                            label = { Text("Education / Apprenticeship (Optional)") },
                            placeholder = { Text("e.g. Traditional Apprenticeship under Master Carver / Matric / Diploma / Leave Blank") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_profile_education_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 6. Availability, Work Type & Rates
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Availability & Rates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Current Availability Status *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(WORKER_AVAILABILITY) { avail ->
                                FilterChip(
                                    selected = availability == avail,
                                    onClick = { availability = avail },
                                    label = { Text(avail, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirMint,
                                        selectedLabelColor = KashmirDeepGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = workType,
                                onValueChange = { workType = it },
                                label = { Text("Work Type") },
                                placeholder = { Text("Daily wage, Contract") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = expectedPayment,
                                onValueChange = { expectedPayment = it },
                                label = { Text("Expected Payment *") },
                                placeholder = { Text("₹800/day") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("create_profile_pay_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Seasonal Worker Section
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KashmirMint.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Seasonal Worker (Apple / Harvest / Snow)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = KashmirDeepGreen
                                        )
                                        Text(
                                            text = "Check this if you work during seasonal harvesting or winter operations",
                                            fontSize = 10.sp,
                                            color = KashmirTextSecondary
                                        )
                                    }
                                    Switch(checked = isSeasonalWorker, onCheckedChange = { isSeasonalWorker = it })
                                }

                                if (isSeasonalWorker) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = seasonalSpecialties,
                                        onValueChange = { seasonalSpecialties = it },
                                        label = { Text("Seasonal Specialties") },
                                        placeholder = { Text("e.g. Apple Picking, Tree Climbing, Grading") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 7. Location & Bio
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Location & About You",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Kashmir District *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(KASHMIR_DISTRICTS) { dist ->
                                FilterChip(
                                    selected = district == dist,
                                    onClick = { district = dist },
                                    label = { Text(dist, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirMint,
                                        selectedLabelColor = KashmirDeepGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = area,
                            onValueChange = { area = it },
                            label = { Text("Area / Town / Village *") },
                            placeholder = { Text("e.g. Safa Kadal, Sopore, Bijbehara") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_profile_area_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = languages,
                            onValueChange = { languages = it },
                            label = { Text("Languages Spoken") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bio Header with Voice-to-Text Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "About Me / Voice Bio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KashmirTextPrimary
                            )

                            // Voice Input Trigger Button
                            FilledTonalButton(
                                onClick = { onMicButtonClick() },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = KashmirMint,
                                    contentColor = KashmirDeepGreen
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("record_bio_voice_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Bio Input", modifier = Modifier.size(16.dp), tint = KashmirDeepGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speak / Record Bio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Voice Philosophy Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KashmirMint.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Voice-First: Tap 'Speak / Record Bio' to speak in Kashmiri, Urdu, or English. Your words will be transcribed directly!",
                                    fontSize = 10.sp,
                                    color = KashmirDeepGreen,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("About Me / Bio & Personal Statement") },
                            placeholder = { Text("Introduce your craft, techniques, experience, or tap 'Speak / Record Bio' to voice record...") },
                            trailingIcon = {
                                IconButton(onClick = { onMicButtonClick() }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Record Bio", tint = KashmirEmerald)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(115.dp)
                                .testTag("create_profile_bio_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 5
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error display
            if (errorMessage.isNotBlank()) {
                item {
                    Text(
                        text = errorMessage,
                        color = KashmirError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }

            // 8. Submit Profile Button
            item {
                Button(
                    onClick = {
                        val effectiveProf = if (profession.contains("Other") && customProfession.isNotBlank()) {
                            customProfession.trim()
                        } else {
                            profession
                        }

                        if (name.isBlank() || effectiveProf.isBlank() || skills.isBlank() || area.isBlank()) {
                            errorMessage = "Please complete all required fields (Name, Profession, Skills, Area)."
                            return@Button
                        }

                        val photoData = selectedPhotoUri?.toString() ?: selectedAvatarPreset

                        viewModel.saveWorkerProfile(
                            name = name.trim(),
                            photoUrl = photoData,
                            district = district,
                            area = area.trim(),
                            profession = effectiveProf,
                            occupation = occupation.trim(),
                            skills = skills.trim(),
                            education = education.trim(), // Optional!
                            experienceYears = experienceYears,
                            languages = languages.trim(),
                            bio = bio.trim(),
                            previousWork = previousWork.trim(),
                            services = services.trim(),
                            availability = availability,
                            workType = workType.trim(),
                            expectedPayment = expectedPayment.trim(),
                            isSeasonalWorker = isSeasonalWorker,
                            seasonalSpecialties = seasonalSpecialties.trim(),
                            onComplete = {
                                viewModel.navigateTo(Screen.MY_PROFILE)
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_create_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingProfile != null) "Update Worker Profile" else "Publish Worker Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Voice Bio Recording Modal
    if (showVoiceBioModal) {
        VoiceBioModal(
            currentBio = bio,
            onDismiss = { showVoiceBioModal = false },
            onApply = { newText, append ->
                bio = if (append && bio.isNotBlank()) "$bio $newText" else newText
                showVoiceBioModal = false
                Toast.makeText(context, "Voice bio updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun CompletionChip(label: String, done: Boolean, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (done) KashmirMint else KashmirSurfaceGreen,
        border = BorderStroke(1.dp, if (done) KashmirEmerald else KashmirCardBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (done) KashmirDeepGreen else KashmirTextMuted,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
                color = if (done) KashmirDeepGreen else KashmirTextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceBioModal(
    currentBio: String,
    onDismiss: () -> Unit,
    onApply: (String, Boolean) -> Unit
) {
    var spokenBioText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice 'About Me' Recorder",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KashmirDeepGreen
                )
            )
            Text(
                text = "Speak in Kashmiri, Urdu, or English to transcribe your artisan story",
                style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Mic Circle
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(KashmirMint),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KashmirDeepGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = KashmirGold,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KashmirEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Microphone Ready • Speak your bio or select a template",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = KashmirEmerald
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Kashmir Artisan Quick Voice Templates
            Text(
                text = "Artisan & Worker Voice Templates (Tap to Preview):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = KashmirTextMuted
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val voiceTemplates = listOf(
                Pair(
                    "Walnut Carver (Downtown Srinagar)",
                    "Master walnut woodcarver with over 12 years experience in Srinagar. Specializes in intricate Chinar leaf and lotus motifs for luxury furniture, doors, and heritage panels."
                ),
                Pair(
                    "Apple Orchardist (Sopore)",
                    "Experienced apple harvest specialist from Sopore. Expert in high-density tree pruning, harvest crew supervision, grading, and mandi packing across North Kashmir."
                ),
                Pair(
                    "Pashmina & Sozni Weaver (Budgam)",
                    "Traditional needlework artisan from Kanihama Budgam crafting authentic GI-tagged Pashmina shawls with Sozni and Tilla embroidery."
                ),
                Pair(
                    "Electrician & Inverter Tech (Pulwama)",
                    "Certified electrician and solar inverter specialist available for residential wiring, backup power maintenance, and emergency repairs across South Kashmir."
                )
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(voiceTemplates) { (title, fullText) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KashmirSurfaceGreen,
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { spokenBioText = fullText }
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VolumeUp, null, tint = KashmirDeepGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fullText,
                                fontSize = 10.sp,
                                color = KashmirTextSecondary,
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Transcription Edit Box
            OutlinedTextField(
                value = spokenBioText,
                onValueChange = { spokenBioText = it },
                label = { Text("Transcribed Voice 'About Me'") },
                placeholder = { Text("Your spoken words will appear here. You can also edit before saving...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = KashmirTextSecondary)
                }

                if (spokenBioText.isNotBlank()) {
                    if (currentBio.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        OutlinedButton(
                            onClick = { onApply(spokenBioText, true) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Append to Bio", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onApply(spokenBioText, false) },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (currentBio.isBlank()) "Apply to Bio" else "Replace Bio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

