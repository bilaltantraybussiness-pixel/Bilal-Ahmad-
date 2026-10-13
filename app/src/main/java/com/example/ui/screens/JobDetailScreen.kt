package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val job by viewModel.selectedJob.collectAsState()
    var showApplyDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    val j = job
    if (j == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job details not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Rozgar Kashmir Job Opportunity: ${j.title} in ${j.area}, ${j.district}.\nPayment: ${j.paymentAmount}/${j.paymentPeriod}.\nProfession: ${j.profession}.\nStart Date: ${j.startDate}.\nApply on Rozgar Kashmir app."
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Job via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Job")
                    }
                    IconButton(onClick = {
                        viewModel.toggleSave("JOB", j.id, j.title, "${j.area}, ${j.district}")
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Job")
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Compensation", fontSize = 11.sp, color = KashmirTextMuted)
                        Text(
                            "${j.paymentAmount} / ${j.paymentPeriod}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KashmirDeepGreen
                        )
                    }

                    Row {
                        OutlinedButton(
                            onClick = {
                                viewModel.startConversationWith(
                                    targetUserId = j.employerUserId,
                                    targetUserName = j.employerName,
                                    subject = "Inquiry: ${j.title}"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Chat", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showApplyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("apply_job_button")
                        ) {
                            Text("Apply Now", fontWeight = FontWeight.Bold)
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KashmirBadge(text = j.workType, containerColor = KashmirMint)
                            if (j.isSeasonal) {
                                KashmirBadge(
                                    text = "Seasonal (${j.seasonalCategory})",
                                    containerColor = KashmirGold.copy(alpha = 0.3f),
                                    contentColor = KashmirDeepGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = j.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary
                            )
                        )

                        Text(
                            text = "Employer: ${j.employerName}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = KashmirEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${j.area}, ${j.district} (${j.workLocation})",
                                style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            JobDetailKeyVal("Workers Needed", "${j.workersNeeded} workers")
                            JobDetailKeyVal("Working Hours", j.hours)
                            JobDetailKeyVal("Start Date", j.startDate)
                        }
                    }
                }
            }

            // Description & Requirements
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Job Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(j.description, style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary))

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Required Skills", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(j.requiredSkills, style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary))

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Experience & Requirements", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirTextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Experience: ${j.experienceRequired}", style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary))
                        Text("Requirements: ${j.requirements}", style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary))
                    }
                }
            }
        }
    }

    if (showApplyDialog) {
        val currentUser by viewModel.currentUser.collectAsState()
        val allWorkers by viewModel.allWorkers.collectAsState()
        val myWorkerProfile = remember(allWorkers, currentUser?.id) {
            allWorkers.firstOrNull { it.userId == currentUser?.id }
        }

        var expectedPay by remember { mutableStateOf(myWorkerProfile?.expectedPayment ?: j.paymentAmount) }
        var availabilityNote by remember { mutableStateOf(myWorkerProfile?.availability ?: "Available immediately") }
        var skillsSummary by remember { mutableStateOf(myWorkerProfile?.skills ?: "") }
        var experienceYears by remember { mutableIntStateOf(myWorkerProfile?.experienceYears ?: 2) }
        var coverNote by remember { mutableStateOf("") }
        var portfolioLink by remember { mutableStateOf(myWorkerProfile?.previousWork ?: "") }
        var attachVoiceIntro by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Send, null, tint = KashmirDeepGreen, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply for Job (Text-First)", fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Applying for: ${j.title}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirTextSecondary)
                    Text("Employer: ${j.employerName} • ${j.district}", fontSize = 11.sp, color = KashmirEmerald)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = expectedPay,
                        onValueChange = { expectedPay = it },
                        label = { Text("Expected Payment *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = availabilityNote,
                        onValueChange = { availabilityNote = it },
                        label = { Text("Your Availability *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = skillsSummary,
                        onValueChange = { skillsSummary = it },
                        label = { Text("Your Key Skills & Techniques") },
                        placeholder = { Text("e.g. Walnut Carving, Pruning, Masonry...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Experience: $experienceYears Years", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Row {
                            TextButton(onClick = { if (experienceYears > 0) experienceYears-- }) { Text("-") }
                            TextButton(onClick = { experienceYears++ }) { Text("+") }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = coverNote,
                        onValueChange = { coverNote = it },
                        label = { Text("Cover Message to Employer") },
                        placeholder = { Text("Explain your past work in Kashmir and why you're a good fit...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = portfolioLink,
                        onValueChange = { portfolioLink = it },
                        label = { Text("Portfolio / Past Projects / References (optional)") },
                        placeholder = { Text("Describe past workshops, orchards, or projects...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Optional Voice Introduction Toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = KashmirMint.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Optional Voice Intro", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirDeepGreen)
                                    Text("Attach a brief voice introduction note", fontSize = 10.sp, color = KashmirTextSecondary)
                                }
                            }
                            Switch(
                                checked = attachVoiceIntro,
                                onCheckedChange = { attachVoiceIntro = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = KashmirDeepGreen, checkedTrackColor = KashmirMint)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val voicePath = if (attachVoiceIntro) "voice_intro_${currentUser?.id ?: 0}.m4a" else ""
                        viewModel.applyForJob(
                            job = j,
                            expectedPay = expectedPay,
                            availabilityNote = availabilityNote,
                            coverNote = coverNote,
                            skillsSummary = skillsSummary,
                            experienceYears = experienceYears,
                            portfolioLink = portfolioLink,
                            optionalVoicePath = voicePath
                        )
                        showApplyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Submit Application", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApplyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("Misleading Information") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Job Listing", color = KashmirError, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Fraud / Scam", "Misleading Information", "Illegal Work", "Spam").forEach { r ->
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
                        label = { Text("Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitReport("JOB", j.id, j.title, reason, details)
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
}

@Composable
fun JobDetailKeyVal(title: String, value: String) {
    Column {
        Text(title, fontSize = 11.sp, color = KashmirTextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
    }
}
