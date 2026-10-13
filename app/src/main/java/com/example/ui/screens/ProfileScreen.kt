package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: RozgarViewModel,
    onOpenAuth: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allWorkers by viewModel.allWorkers.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val userApplications by viewModel.userApplications.collectAsState()
    val employerApplications by viewModel.employerApplications.collectAsState()
    val savedItems by viewModel.userSavedItems.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFirestoreProfileManager by remember { mutableStateOf(false) }
    var employerSubTab by remember { mutableIntStateOf(0) } // 0: Posted Jobs, 1: Applications, 2: Shortlisted, 3: Accepted, 4: Closed

    val firestoreProfile by viewModel.firestoreUserProfile.collectAsState()
    val isSyncingProfile by viewModel.isSyncingProfileWithFirestore.collectAsState()

    val user = currentUser
    if (user == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.AccountCircle, null, tint = KashmirDeepGreen, modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to Rozgar Kashmir",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Sign in or create a free account to apply for jobs, list services, or sell items.",
                style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary),
                modifier = Modifier.padding(16.dp)
            )
            Button(
                onClick = onOpenAuth,
                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login or Register")
            }
        }
        return
    }

    val myWorkerProfile = remember(allWorkers, user.id) {
        allWorkers.firstOrNull { it.userId == user.id }
    }
    val myJobs = remember(allJobs, user.id) {
        allJobs.filter { it.employerUserId == user.id }
    }
    val myProducts = remember(allProducts, user.id) {
        allProducts.filter { it.sellerUserId == user.id }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KashmirBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // User Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(KashmirEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.fullName,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, "Verified", tint = KashmirEmerald, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "${user.area.ifBlank { "Kashmir Valley" }}, ${user.district}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary)
                            )
                            Text(
                                text = "Roles: ${user.roles}",
                                style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Privacy Shield Notice
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
                            Icon(Icons.Default.Lock, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Privacy Active: Your phone number (${user.phoneNumber}) is private and hidden from public view.",
                                style = MaterialTheme.typography.labelSmall.copy(color = KashmirDeepGreen, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile Completion Meter
                    val completionPercent = remember(myWorkerProfile) {
                        if (myWorkerProfile == null) 25 else {
                            var score = 30
                            if (myWorkerProfile.photoUrl.isNotBlank()) score += 15
                            if (myWorkerProfile.skills.isNotBlank()) score += 15
                            if (myWorkerProfile.previousWork.isNotBlank()) score += 15
                            if (myWorkerProfile.bio.isNotBlank()) score += 15
                            if (myWorkerProfile.expectedPayment.isNotBlank()) score += 10
                            score.coerceAtMost(100)
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = KashmirMint.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.CREATE_PROFILE) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stars, null, tint = KashmirWarmGold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Profile Completion: $completionPercent%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = KashmirDeepGreen
                                    )
                                }
                                Text(
                                    text = if (completionPercent >= 100) "100% Complete" else "Improve Score ›",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { completionPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (completionPercent >= 70) KashmirEmerald else KashmirWarmGold,
                                trackColor = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showFirestoreProfileManager = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_manage_cloud_profile")
                        ) {
                            Icon(Icons.Default.CloudSync, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage Cloud Profile", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.SELLER_DASHBOARD) },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Storefront, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Seller Hub", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.CREATE_PROFILE) },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirSurfaceGreen, contentColor = KashmirDeepGreen),
                            border = BorderStroke(1.dp, KashmirDeepGreen.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Local Card", fontSize = 12.sp)
                        }

                        if (user.roles.contains("ADMIN")) {
                            Button(
                                onClick = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Admin Panel", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Firebase Firestore Cloud Profile & Service History Component Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, KashmirMint)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with Cloud status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(KashmirEmerald.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = KashmirDeepGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Cloud Profile & Service History",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                                )
                                Text(
                                    text = "Firebase Firestore Storage",
                                    style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSyncingProfile) KashmirWarmGold.copy(alpha = 0.2f) else KashmirSurfaceGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSyncingProfile) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = KashmirWarmGold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Syncing...", fontSize = 10.sp, color = KashmirWarmGold, fontWeight = FontWeight.Bold)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(KashmirEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Firestore Synced", fontSize = 10.sp, color = KashmirDeepGreen, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Profile details overview
                    val profile = firestoreProfile
                    val isEmployerRole = profile?.userRole == "EMPLOYER" || (user.roles.contains("EMPLOYER") && !user.roles.contains("WORKER"))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = KashmirBackground,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isEmployerRole) KashmirWarmGold.copy(alpha = 0.2f) else KashmirEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isEmployerRole) "Role: Employer / Hirer" else "Role: Artisan / Worker",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isEmployerRole) KashmirForestGreen else KashmirDeepGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Text(
                                    text = if (isEmployerRole) (profile?.industry ?: "Agriculture & Trade") else (profile?.profession ?: "Craftsman / Artisan"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = KashmirTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Verified Skills", fontSize = 10.sp, color = KashmirTextSecondary)
                                    Text(
                                        text = "${profile?.skills?.size ?: 0} skills configured",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                }
                                Column {
                                    Text("Experience", fontSize = 10.sp, color = KashmirTextSecondary)
                                    Text(
                                        text = "${profile?.experienceYears ?: 3} years",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirTextPrimary
                                    )
                                }
                                Column {
                                    Text("Service History", fontSize = 10.sp, color = KashmirTextSecondary)
                                    Text(
                                        text = "${profile?.serviceHistory?.size ?: 0} completed contracts",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirEmerald
                                    )
                                }
                            }
                        }
                    }

                    // Skills preview
                    val skills = profile?.skills ?: emptyList()
                    if (skills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Skills & Proficiencies:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KashmirTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(skills.take(5)) { skill ->
                                val proficiency = profile?.skillProficiencies?.get(skill) ?: "Expert"
                                Surface(
                                    color = KashmirMint.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, KashmirDeepGreen.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(skill, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = KashmirDeepGreen)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("($proficiency)", fontSize = 9.sp, color = KashmirEmerald, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Service History Preview
                    val serviceHistory = profile?.serviceHistory ?: emptyList()
                    if (serviceHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Service History (Firestore):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KashmirTextSecondary
                            )
                            Text(
                                text = "${serviceHistory.size} total",
                                fontSize = 10.sp,
                                color = KashmirEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        serviceHistory.take(2).forEach { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                color = KashmirSurfaceGreen.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.5.dp, KashmirMint)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = KashmirDeepGreen
                                        )
                                        Surface(
                                            color = if (item.status == "Completed") KashmirMint else KashmirWarmGold.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (item.status == "Completed") KashmirDeepGreen else KashmirWarmGold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.clientOrEmployerName} • ${item.dateRange}",
                                            fontSize = 10.sp,
                                            color = KashmirTextSecondary
                                        )
                                        Text(
                                            text = item.amountEarnedOrBudget,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirForestGreen
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showFirestoreProfileManager = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_firestore_profile_manager"),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ManageAccounts, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEmployerRole) "Manage Employer Details & History (Firestore)" else "Manage Personal Details, Skills & History (Firestore)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Worker Profile Preview / Creation Prompt
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Worker Profile",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (myWorkerProfile != null) {
                        Text(
                            text = "Edit Card",
                            color = KashmirEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { viewModel.navigateTo(Screen.CREATE_PROFILE) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (myWorkerProfile != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
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
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(KashmirEmerald),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = myWorkerProfile.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(myWorkerProfile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(myWorkerProfile.profession, fontSize = 12.sp, color = KashmirEmerald, fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Surface(
                                    color = if (myWorkerProfile.availability.contains("Available")) KashmirMint else KashmirError.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = myWorkerProfile.availability,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (myWorkerProfile.availability.contains("Available")) KashmirDeepGreen else KashmirError,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Availability Toggle Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Work Status", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text("Toggle to set yourself Available or Unavailable", fontSize = 10.sp, color = KashmirTextSecondary)
                                }
                                Switch(
                                    checked = myWorkerProfile.availability.contains("Available"),
                                    onCheckedChange = { isAvail ->
                                        viewModel.setWorkerAvailability(isAvail)
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.viewWorkerDetail(myWorkerProfile) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Preview Public Profile", fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.CREATE_PROFILE) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Full Profile", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.navigateTo(Screen.CREATE_PROFILE) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AddBusiness, null, tint = KashmirEmerald)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Create Your Worker / Artisan Card", fontWeight = FontWeight.Bold)
                                Text("List skills, craft, and availability for employers", fontSize = 12.sp, color = KashmirTextSecondary)
                            }
                        }
                    }
                }
            }
        }

        // My Job Applications Tracker (Worker Perspective)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "My Applications (${userApplications.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Track your application status from submission to completion",
                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (userApplications.isEmpty()) {
                    Text(
                        text = "You haven't applied to any jobs yet.",
                        fontSize = 13.sp,
                        color = KashmirTextSecondary
                    )
                } else {
                    userApplications.forEach { app ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(app.jobTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Availability: ${app.availabilityNote}", fontSize = 12.sp, color = KashmirTextSecondary)
                                        if (app.expectedPay.isNotBlank()) {
                                            Text("Expected Rate: ${app.expectedPay}", fontSize = 12.sp, color = KashmirEmerald)
                                        }
                                    }
                                    Surface(
                                        color = when (app.status) {
                                            "ACCEPTED" -> KashmirMint
                                            "SHORTLISTED" -> KashmirEmerald.copy(alpha = 0.2f)
                                            "UNDER_REVIEW" -> KashmirAmber.copy(alpha = 0.2f)
                                            "COMPLETED" -> KashmirDeepGreen.copy(alpha = 0.2f)
                                            "REJECTED" -> KashmirError.copy(alpha = 0.2f)
                                            else -> KashmirWarmGold.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = when (app.status) {
                                                "SUBMITTED" -> "Applied"
                                                "UNDER_REVIEW" -> "Under Review"
                                                "SHORTLISTED" -> "Shortlisted"
                                                "ACCEPTED" -> "Accepted"
                                                "REJECTED" -> "Rejected"
                                                "COMPLETED" -> "Completed"
                                                else -> app.status
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = when (app.status) {
                                                "ACCEPTED" -> KashmirDeepGreen
                                                "SHORTLISTED" -> KashmirEmerald
                                                "REJECTED" -> KashmirError
                                                "COMPLETED" -> KashmirDeepGreen
                                                else -> KashmirWarmGold
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (app.status == "ACCEPTED" || app.status == "SHORTLISTED") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            viewModel.startConversationWith(
                                                targetUserId = app.employerUserId,
                                                targetUserName = "Employer",
                                                subject = "Regarding Application: ${app.jobTitle}"
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Voice Chat with Employer", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // EMPLOYER DASHBOARD SECTION
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, KashmirEmerald.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Dashboard, null, tint = KashmirDeepGreen, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Employer Dashboard",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                        }
                        TextButton(onClick = { viewModel.navigateTo(Screen.POST_JOB) }) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = KashmirEmerald)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Post New Job", fontSize = 11.sp, color = KashmirEmerald, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dashboard Tabs: Posted Jobs, Applications, Shortlisted, Accepted, Closed
                    val empTabs = listOf(
                        "Posted (${myJobs.filter { it.status == "ACTIVE" }.size})",
                        "Applications (${employerApplications.size})",
                        "Shortlisted (${employerApplications.filter { it.status == "SHORTLISTED" }.size})",
                        "Accepted (${employerApplications.filter { it.status == "ACCEPTED" }.size})",
                        "Closed (${myJobs.filter { it.status == "CLOSED" }.size})"
                    )

                    ScrollableTabRow(
                        selectedTabIndex = employerSubTab,
                        edgePadding = 0.dp,
                        containerColor = KashmirMint.copy(alpha = 0.5f),
                        indicator = {},
                        divider = {},
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        empTabs.forEachIndexed { idx, label ->
                            Tab(
                                selected = employerSubTab == idx,
                                onClick = { employerSubTab = idx },
                                text = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (employerSubTab == idx) FontWeight.Bold else FontWeight.Normal,
                                        color = if (employerSubTab == idx) KashmirDeepGreen else KashmirTextMuted
                                    )
                                },
                                modifier = Modifier
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (employerSubTab == idx) Color.White else Color.Transparent)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (employerSubTab) {
                        0 -> {
                            // ACTIVE POSTED JOBS
                            val activeJobs = myJobs.filter { it.status == "ACTIVE" }
                            if (activeJobs.isEmpty()) {
                                Text("No active posted jobs. Tap '+ Post New Job' to hire artisans.", fontSize = 12.sp, color = KashmirTextSecondary)
                            } else {
                                activeJobs.forEach { job ->
                                    val appsForThisJob = employerApplications.filter { it.jobId == job.id }
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = KashmirSurfaceGreen)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("${job.paymentAmount}/${job.paymentPeriod} • ${job.area}, ${job.district}", fontSize = 11.sp, color = KashmirTextSecondary)
                                                }
                                                Surface(color = KashmirMint, shape = RoundedCornerShape(4.dp)) {
                                                    Text("${appsForThisJob.size} Applicants", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                OutlinedButton(
                                                    onClick = { viewModel.viewJobDetail(job) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text("View Job", fontSize = 11.sp)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Button(
                                                    onClick = { viewModel.closeJob(job.id) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirError.copy(alpha = 0.8f)),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Close Job", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1, 2, 3 -> {
                            // APPLICATIONS, SHORTLISTED, ACCEPTED
                            val list = when (employerSubTab) {
                                2 -> employerApplications.filter { it.status == "SHORTLISTED" }
                                3 -> employerApplications.filter { it.status == "ACCEPTED" }
                                else -> employerApplications
                            }

                            if (list.isEmpty()) {
                                Text("No applicants in this category.", fontSize = 12.sp, color = KashmirTextSecondary)
                            } else {
                                list.forEach { app ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = KashmirSurfaceGreen)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(app.applicantName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("For: ${app.jobTitle} • ${app.applicantDistrict}", fontSize = 11.sp, color = KashmirTextSecondary)
                                                    if (app.expectedPay.isNotBlank()) {
                                                        Text("Expected: ${app.expectedPay}", fontSize = 11.sp, color = KashmirEmerald)
                                                    }
                                                }
                                                Surface(
                                                    color = when (app.status) {
                                                        "ACCEPTED" -> KashmirMint
                                                        "SHORTLISTED" -> KashmirEmerald.copy(alpha = 0.2f)
                                                        "COMPLETED" -> KashmirDeepGreen.copy(alpha = 0.2f)
                                                        "REJECTED" -> KashmirError.copy(alpha = 0.2f)
                                                        else -> KashmirWarmGold.copy(alpha = 0.2f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = app.status,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = when (app.status) {
                                                            "ACCEPTED" -> KashmirDeepGreen
                                                            "SHORTLISTED" -> KashmirEmerald
                                                            "REJECTED" -> KashmirError
                                                            else -> KashmirWarmGold
                                                        },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (app.coverNote.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text("\"${app.coverNote}\"", fontSize = 11.sp, color = KashmirTextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Action Buttons for Employer
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Shortlist
                                                if (app.status != "SHORTLISTED" && app.status != "ACCEPTED") {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.updateApplicationStatus(app.id, "SHORTLISTED", app.applicantUserId, app.jobTitle)
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Shortlist", fontSize = 10.sp)
                                                    }
                                                }

                                                // Accept
                                                if (app.status != "ACCEPTED") {
                                                    Button(
                                                        onClick = {
                                                            viewModel.updateApplicationStatus(app.id, "ACCEPTED", app.applicantUserId, app.jobTitle)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("Accept", fontSize = 10.sp)
                                                    }
                                                }

                                                // Voice Chat
                                                Button(
                                                    onClick = {
                                                        viewModel.startConversationWith(
                                                            targetUserId = app.applicantUserId,
                                                            targetUserName = app.applicantName,
                                                            subject = "Regarding ${app.jobTitle}"
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                                    shape = RoundedCornerShape(6.dp),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Mic, null, modifier = Modifier.size(13.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Voice Chat", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        4 -> {
                            // CLOSED JOBS
                            val closedJobs = myJobs.filter { it.status == "CLOSED" }
                            if (closedJobs.isEmpty()) {
                                Text("No closed jobs.", fontSize = 12.sp, color = KashmirTextSecondary)
                            } else {
                                closedJobs.forEach { job ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(job.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Closed • ${job.area}, ${job.district}", fontSize = 11.sp, color = KashmirTextMuted)
                                            }
                                            Button(
                                                onClick = { viewModel.reopenJob(job.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Re-open", fontSize = 11.sp)
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

        // My Marketplace Items
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "My Marketplace Listings (${myProducts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (myProducts.isEmpty()) {
                    Text("No products listed for sale.", fontSize = 13.sp, color = KashmirTextSecondary)
                } else {
                    myProducts.forEach { product ->
                        MarketProductCard(
                            product = product,
                            onViewDetail = { viewModel.viewProductDetail(product) },
                            onContactSeller = {}
                        )
                    }
                }
            }
        }

        // Account & Security Governance
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account & Governance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    )
                    Text(
                        text = "Access administrative governance tools or sign out of your Kashmir account.",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dedicated Admin Login entry point (Clearly accessible to everyone, but access protected)
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_login_entry_point"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                        border = BorderStroke(1.dp, KashmirDeepGreen.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null, modifier = Modifier.size(18.dp), tint = KashmirGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (user.roles.contains("ADMIN") || user.email in listOf("bilaltantraypersonal@gmail.com", "admin@rozgarkashmir.in"))
                                "Open Owner / Super Admin Dashboard"
                            else
                                "Owner / Super Admin Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Legal & Policies Entry Point
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.POLICIES) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_legal_policies_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                        border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Policy, null, modifier = Modifier.size(18.dp), tint = KashmirEmerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Legal, Privacy Policy & Terms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_logout_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                        border = BorderStroke(1.dp, KashmirError.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp), tint = KashmirError)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out (${user.fullName})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showEditProfileDialog) {
        EditWorkerProfileDialog(
            viewModel = viewModel,
            existing = myWorkerProfile,
            currentUserDistrict = user.district,
            currentUserName = user.fullName,
            onDismiss = { showEditProfileDialog = false }
        )
    }

    if (showFirestoreProfileManager) {
        Dialog(
            onDismissRequest = { showFirestoreProfileManager = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = KashmirBackground
            ) {
                UserProfileManagerComponent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize(),
                    onClose = { showFirestoreProfileManager = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkerProfileDialog(
    viewModel: RozgarViewModel,
    existing: com.example.data.model.WorkerProfileEntity?,
    currentUserDistrict: String,
    currentUserName: String,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: currentUserName) }
    var district by remember { mutableStateOf(existing?.district ?: currentUserDistrict) }
    var area by remember { mutableStateOf(existing?.area ?: "") }
    var profession by remember { mutableStateOf(existing?.profession ?: KASHMIR_PROFESSIONS.first()) }
    var customProfession by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf(existing?.occupation ?: "Craftsman / Specialist") }
    var skills by remember { mutableStateOf(existing?.skills ?: "") }
    var education by remember { mutableStateOf(existing?.education ?: "") } // Explicitly NOT mandatory!
    var experienceYears by remember { mutableIntStateOf(existing?.experienceYears ?: 3) }
    var languages by remember { mutableStateOf(existing?.languages ?: "Kashmiri, Urdu") }
    var bio by remember { mutableStateOf(existing?.bio ?: "") }
    var previousWork by remember { mutableStateOf(existing?.previousWork ?: "") }
    var services by remember { mutableStateOf(existing?.services ?: "") }
    var availability by remember { mutableStateOf(existing?.availability ?: WORKER_AVAILABILITY.first()) }
    var workType by remember { mutableStateOf(existing?.workType ?: JOB_TYPES.first()) }
    var expectedPayment by remember { mutableStateOf(existing?.expectedPayment ?: "₹800/day") }
    var isSeasonalWorker by remember { mutableStateOf(existing?.isSeasonalWorker ?: false) }
    var seasonalSpecialties by remember { mutableStateOf(existing?.seasonalSpecialties ?: "Apple Picking, Orchard Work") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                Text(
                    text = "Worker Profile & Card",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                )
                Text(
                    text = "List your skills, crafts, availability & rates. Note: Education is NOT mandatory.",
                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Profession / Occupation", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(KASHMIR_PROFESSIONS.take(8)) { prof ->
                        FilterChip(
                            selected = profession == prof,
                            onClick = { profession = prof },
                            label = { Text(prof, fontSize = 11.sp) }
                        )
                    }
                }

                if (profession.contains("Other")) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customProfession,
                        onValueChange = { customProfession = it },
                        label = { Text("Enter Your Exact Profession") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Key Skills (comma separated) *") },
                    placeholder = { Text("e.g. Walnut Carving, Chinar Motifs, Sanding") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Education note
                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("Education (Optional - Not Mandatory)") },
                    placeholder = { Text("Leave blank if informal learning / self-taught") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = experienceYears.toString(),
                        onValueChange = { experienceYears = it.toIntOrNull() ?: 1 },
                        label = { Text("Years Exp") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = expectedPayment,
                        onValueChange = { expectedPayment = it },
                        label = { Text("Expected Pay") },
                        placeholder = { Text("₹900/day") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Area / Village *") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("District") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Availability Status", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WORKER_AVAILABILITY) { avail ->
                        FilterChip(
                            selected = availability == avail,
                            onClick = { availability = avail },
                            label = { Text(avail, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Seasonal Worker Toggle
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = KashmirMint.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seasonal Worker (Apples / Harvest / Snow)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Switch(checked = isSeasonalWorker, onCheckedChange = { isSeasonalWorker = it })
                        }
                        if (isSeasonalWorker) {
                            OutlinedTextField(
                                value = seasonalSpecialties,
                                onValueChange = { seasonalSpecialties = it },
                                label = { Text("Seasonal Specialties") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio & Experience Summary") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val effectiveProf = if (profession.contains("Other") && customProfession.isNotBlank()) {
                            customProfession
                        } else {
                            profession
                        }
                        viewModel.saveWorkerProfile(
                            name = name,
                            district = district,
                            area = area,
                            profession = effectiveProf,
                            occupation = occupation,
                            skills = skills,
                            education = education,
                            experienceYears = experienceYears,
                            languages = languages,
                            bio = bio,
                            previousWork = previousWork,
                            services = services,
                            availability = availability,
                            workType = workType,
                            expectedPayment = expectedPayment,
                            isSeasonalWorker = isSeasonalWorker,
                            seasonalSpecialties = seasonalSpecialties,
                            onComplete = onDismiss
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Worker Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
