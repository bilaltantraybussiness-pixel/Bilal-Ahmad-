package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(viewModel: RozgarViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    var title by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf(KASHMIR_PROFESSIONS.first()) }
    var customProfession by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var requiredSkills by remember { mutableStateOf("") }
    var requiredSkillsList by remember { mutableStateOf(listOf<String>()) }
    var workersNeeded by remember { mutableIntStateOf(1) }
    var district by remember { mutableStateOf(currentUser?.district ?: KASHMIR_DISTRICTS.first()) }
    var area by remember { mutableStateOf(currentUser?.area ?: "") }
    var workLocation by remember { mutableStateOf("On-site") }
    var workPreference by remember { mutableStateOf("On-site") }
    var workType by remember { mutableStateOf(JOB_TYPES.first()) }
    var startDate by remember { mutableStateOf("Immediate") }
    var endDate by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("8:00 AM - 5:00 PM") }
    var paymentAmount by remember { mutableStateOf("₹900") }
    var paymentPeriod by remember { mutableStateOf("Per Day") }
    var experienceRequired by remember { mutableStateOf("1+ years") }
    var requirements by remember { mutableStateOf("Honest, punctual, reliable") }
    var isSeasonal by remember { mutableStateOf(false) }
    var seasonalCategory by remember { mutableStateOf(SEASONAL_WORK_TYPES.first()) }

    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a Job in Kashmir", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Job Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = "Reach thousands of skilled workers across Kashmir valley.",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Job Title *") },
                            placeholder = { Text("e.g. 10 Apple Pickers Needed in Shopian") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("post_job_title_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Profession selection
                        Text("Category / Profession", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(KASHMIR_PROFESSIONS.take(8)) { prof ->
                                FilterChip(
                                    selected = profession == prof,
                                    onClick = { profession = prof },
                                    label = { Text(prof, fontSize = 11.sp) }
                                )
                            }
                        }

                        if (profession.contains("Other")) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customProfession,
                                onValueChange = { customProfession = it },
                                label = { Text("Enter Custom Profession *") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Job Description *") },
                            placeholder = { Text("Describe the work, responsibilities, meals/transport provided...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("post_job_desc_input"),
                            shape = RoundedCornerShape(10.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Required Skills with Search, Top 5/Top 10 suggestions, and Custom Skill
                        SkillPickerComponent(
                            selectedSkills = requiredSkillsList,
                            onSkillsChanged = {
                                requiredSkillsList = it
                                requiredSkills = it.joinToString(", ")
                            },
                            profession = if (profession.contains("Other")) customProfession else profession,
                            label = "Required Skills for Job",
                            hint = "Search or type any required skill (e.g. Apple Picking, Wood Finishing)..."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Workers needed & District
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = workersNeeded.toString(),
                                onValueChange = { workersNeeded = it.toIntOrNull() ?: 1 },
                                label = { Text("Workers Needed") },
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

                        // District Pills for fast selection
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(KASHMIR_DISTRICTS) { dist ->
                                FilterChip(
                                    selected = district == dist,
                                    onClick = { district = dist },
                                    label = { Text(dist, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Area & Work Location
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = area,
                                onValueChange = { area = it },
                                label = { Text("Area / Village / Town *") },
                                placeholder = { Text("e.g. Zainapora, Sopore, Lal Chowk") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = workLocation,
                                onValueChange = { workLocation = it },
                                label = { Text("Location Type") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Work Type Selection
                        Text("Work Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(JOB_TYPES) { type ->
                                FilterChip(
                                    selected = workType == type,
                                    onClick = { workType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Work Location Preference (On-site, Remote, Hybrid)
                        Text("Work Preference", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WORK_PREFERENCES) { pref ->
                                FilterChip(
                                    selected = workPreference == pref,
                                    onClick = {
                                        workPreference = pref
                                        workLocation = pref
                                    },
                                    label = { Text(pref, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment & Period
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = paymentAmount,
                                onValueChange = { paymentAmount = it },
                                label = { Text("Payment (₹) *") },
                                placeholder = { Text("e.g. ₹950") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = paymentPeriod,
                                onValueChange = { paymentPeriod = it },
                                label = { Text("Period") },
                                placeholder = { Text("Per Day, Per Month") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        // Payment Frequency Quick Selectors
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(PAYMENT_FREQUENCIES) { freq ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (paymentPeriod.equals("Per $freq", ignoreCase = true) || paymentPeriod.equals(freq, ignoreCase = true)) KashmirMint else Color.Transparent,
                                    border = BorderStroke(1.dp, KashmirCardBorder),
                                    modifier = Modifier.clickable { paymentPeriod = "Per $freq" }
                                ) {
                                    Text(
                                        text = freq,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = KashmirDeepGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Experience Level Required
                        Text("Experience Required", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(EXPERIENCE_LEVELS) { exp ->
                                FilterChip(
                                    selected = experienceRequired == exp,
                                    onClick = { experienceRequired = exp },
                                    label = { Text(exp, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Spacer(modifier = Modifier.height(14.dp))

                        // Working hours & Start Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = hours,
                                onValueChange = { hours = it },
                                label = { Text("Hours / Shift") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Start Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Seasonal Work Toggle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KashmirMint.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Is this a Seasonal Kashmir Job?",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = KashmirDeepGreen
                                        )
                                        Text(
                                            text = "Apple picking, orchard pruning, tourism, snow clearing, etc.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                        )
                                    }
                                    Switch(
                                        checked = isSeasonal,
                                        onCheckedChange = { isSeasonal = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = KashmirDeepGreen,
                                            checkedTrackColor = KashmirMint
                                        )
                                    )
                                }

                                if (isSeasonal) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Seasonal Category", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(SEASONAL_WORK_TYPES) { cat ->
                                            FilterChip(
                                                selected = seasonalCategory == cat,
                                                onClick = { seasonalCategory = cat },
                                                label = { Text(cat, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (errorMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = errorMessage,
                                color = KashmirError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (title.isBlank() || description.isBlank() || area.isBlank() || paymentAmount.isBlank()) {
                                    errorMessage = "Please fill in all required fields (Title, Description, Area, Payment)."
                                    return@Button
                                }
                                val effectiveProf = if (profession.contains("Other") && customProfession.isNotBlank()) {
                                    customProfession
                                } else {
                                    profession
                                }
                                viewModel.postJob(
                                    title = title,
                                    profession = effectiveProf,
                                    description = description,
                                    requiredSkills = requiredSkills,
                                    workersNeeded = workersNeeded,
                                    district = district,
                                    area = area,
                                    workLocation = workLocation,
                                    workType = workType,
                                    startDate = startDate,
                                    endDate = endDate,
                                    hours = hours,
                                    paymentAmount = paymentAmount,
                                    paymentPeriod = paymentPeriod,
                                    experienceRequired = experienceRequired,
                                    requirements = requirements,
                                    isSeasonal = isSeasonal,
                                    seasonalCategory = if (isSeasonal) seasonalCategory else "None",
                                    onComplete = {
                                        viewModel.navigateTo(Screen.FIND_JOBS)
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_post_job_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                        ) {
                            Icon(Icons.Default.Check, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Publish Job Post", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
