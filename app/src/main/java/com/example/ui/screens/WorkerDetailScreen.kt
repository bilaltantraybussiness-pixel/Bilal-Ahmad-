package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDetailScreen(viewModel: RozgarViewModel) {
    val worker by viewModel.selectedWorker.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var showBookingDialog by remember { mutableStateOf(false) }

    val w = worker
    if (w == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Worker profile not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.toggleSave("WORKER", w.id, w.name, w.profession)
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
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
                        Text("Expected Payment", fontSize = 11.sp, color = KashmirTextMuted)
                        Text(
                            w.expectedPayment,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = KashmirDeepGreen
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.startConversationWith(
                                    targetUserId = w.userId,
                                    targetUserName = w.name,
                                    subject = "Inquiry regarding ${w.profession}"
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                            border = BorderStroke(1.dp, KashmirEmerald),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("worker_contact_button")
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showBookingDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("worker_book_button")
                        ) {
                            Icon(Icons.Default.Handshake, null, modifier = Modifier.size(16.dp), tint = KashmirGold)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hire / Book", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            // Profile Card Header
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
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(KashmirEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = w.name.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = w.name,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = KashmirEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = w.profession,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = KashmirEmerald
                            )
                        )

                        Text(
                            text = "${w.area}, ${w.district}, Kashmir",
                            style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Privacy Notice Card (Phone is private)
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
                                Icon(Icons.Default.Lock, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Phone number is protected by Rozgar Kashmir. Connect directly via voice note.",
                                    fontSize = 10.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoStatItem("Experience", "${w.experienceYears} Years")
                            InfoStatItem("Rating", "★ ${w.rating} (${w.reviewCount})")
                            InfoStatItem("Status", w.availability)
                        }
                    }
                }
            }

            // About & Bio
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("About & Background", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = w.bio.ifBlank { "Dedicated artisan providing skilled professional services across Kashmir valley." },
                            style = MaterialTheme.typography.bodyMedium.copy(color = KashmirTextSecondary)
                        )

                        if (w.previousWork.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Previous Experience & Projects", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirTextPrimary)
                            Text(w.previousWork, fontSize = 13.sp, color = KashmirTextSecondary)
                        }

                        if (w.services.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Services Offered", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirTextPrimary)
                            Text(w.services, fontSize = 13.sp, color = KashmirTextSecondary)
                        }
                    }
                }
            }

            // Skills & Languages
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Skills & Specializations", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        val skillList = w.skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(skillList) { skill ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = KashmirMint
                                ) {
                                    Text(
                                        skill,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = KashmirDeepGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Languages Spoken: ${w.languages}", fontSize = 13.sp, color = KashmirTextPrimary)

                        if (w.education.isNotBlank()) {
                            Text("Education: ${w.education}", fontSize = 13.sp, color = KashmirTextSecondary)
                        }

                        if (w.isSeasonalWorker && w.seasonalSpecialties.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Seasonal Specialties: ${w.seasonalSpecialties}", fontSize = 13.sp, color = KashmirWarmGold, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("Incorrect Information") }
        var details by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Worker Profile", color = KashmirError, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select reason for reporting this worker profile:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Fraud / Fake Skills", "Incorrect Information", "Unprofessional Behavior", "Spam").forEach { r ->
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
                        viewModel.submitReport("WORKER", w.id, w.name, reason, details)
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirError)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showBookingDialog) {
        WorkerBookingDialog(
            viewModel = viewModel,
            worker = w,
            onDismiss = { showBookingDialog = false }
        )
    }
}

@Composable
fun WorkerBookingDialog(
    viewModel: RozgarViewModel,
    worker: com.example.data.model.WorkerProfileEntity,
    onDismiss: () -> Unit
) {
    var serviceRequired by remember { mutableStateOf(worker.profession) }
    var scheduledDate by remember { mutableStateOf("Tomorrow / Immediate") }
    var scheduledTime by remember { mutableStateOf("Morning (9:00 AM - 1:00 PM)") }
    var bookingType by remember { mutableStateOf("DAILY") }
    var agreedRateText by remember { mutableStateOf(worker.expectedPayment.filter { it.isDigit() }.ifBlank { "800" }) }
    var customerAddress by remember { mutableStateOf("") }
    var customerNotes by remember { mutableStateOf("") }
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Hire & Book Worker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                    )
                    Text(
                        text = "Booking ${worker.name} • ${worker.profession}",
                        fontSize = 12.sp,
                        color = KashmirEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item {
                    OutlinedTextField(
                        value = serviceRequired,
                        onValueChange = { serviceRequired = it },
                        label = { Text("Task / Work Required *") },
                        placeholder = { Text("e.g. Electrical wiring, Ceiling woodwork, Apple harvest") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = scheduledDate,
                            onValueChange = { scheduledDate = it },
                            label = { Text("Date *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = agreedRateText,
                            onValueChange = { agreedRateText = it },
                            label = { Text("Agreed Rate (₹) *") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Text("Time Slot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    val timeSlots = listOf("Morning (9 AM - 1 PM)", "Afternoon (2 PM - 6 PM)", "Full Day (9 AM - 6 PM)")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(timeSlots) { slot ->
                            FilterChip(
                                selected = scheduledTime == slot,
                                onClick = { scheduledTime = slot },
                                label = { Text(slot, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = customerAddress,
                        onValueChange = { customerAddress = it },
                        label = { Text("Work Location / Address in Kashmir *") },
                        placeholder = { Text("e.g. House No. 4, Rajbagh, Srinagar") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = { customerNotes = it },
                        label = { Text("Notes / Special Instructions") },
                        placeholder = { Text("e.g. Bring own tools, ladder available at site") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 2
                    )
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        Text(errorMessage, color = KashmirError, fontSize = 12.sp)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = KashmirTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val rate = agreedRateText.toDoubleOrNull() ?: 0.0
                                if (serviceRequired.isBlank() || customerAddress.isBlank() || rate <= 0.0) {
                                    errorMessage = "Please enter task description, address, and valid rate."
                                    return@Button
                                }
                                viewModel.createWorkerBooking(
                                    worker = worker,
                                    serviceRequired = serviceRequired,
                                    scheduledDate = scheduledDate,
                                    scheduledTime = scheduledTime,
                                    bookingType = bookingType,
                                    agreedRate = rate,
                                    estimatedTotal = rate,
                                    customerAddress = customerAddress,
                                    customerNotes = customerNotes,
                                    onComplete = { onDismiss() }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("confirm_booking_button")
                        ) {
                            Text("Confirm Booking Request", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 11.sp, color = KashmirTextMuted)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
    }
}
