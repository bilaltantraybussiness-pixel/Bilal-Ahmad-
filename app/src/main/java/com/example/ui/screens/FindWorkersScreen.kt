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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindWorkersScreen(viewModel: RozgarViewModel) {
    val allWorkers by viewModel.allWorkers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedProfession by viewModel.selectedProfession.collectAsState()
    val selectedAvailability by viewModel.selectedAvailability.collectAsState()
    val isWorkersLoading by viewModel.isWorkersLoading.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredWorkers = remember(allWorkers, searchQuery, selectedDistrict, selectedProfession, selectedAvailability) {
        allWorkers.filter { w ->
            val matchesQuery = searchQuery.isBlank() ||
                w.name.contains(searchQuery, ignoreCase = true) ||
                w.profession.contains(searchQuery, ignoreCase = true) ||
                w.occupation.contains(searchQuery, ignoreCase = true) ||
                w.skills.contains(searchQuery, ignoreCase = true) ||
                w.area.contains(searchQuery, ignoreCase = true) ||
                w.district.contains(searchQuery, ignoreCase = true)

            val matchesDistrict = selectedDistrict == "All Districts" ||
                w.district.equals(selectedDistrict, ignoreCase = true)

            val matchesProf = selectedProfession == "All Professions" ||
                w.profession.contains(selectedProfession, ignoreCase = true)

            val matchesAvail = selectedAvailability == "All Availability" ||
                w.availability.equals(selectedAvailability, ignoreCase = true)

            matchesQuery && matchesDistrict && matchesProf && matchesAvail
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KashmirBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Header
        item {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Skilled Kashmiri Workers",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "${filteredWorkers.size} verified workers & artisans available",
                                style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.refreshWorkersData() },
                                enabled = !isWorkersLoading,
                                modifier = Modifier.testTag("refresh_workers_button")
                            ) {
                                if (isWorkersLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = KashmirEmerald
                                    )
                                } else {
                                    Icon(Icons.Default.Refresh, "Refresh Workers", tint = KashmirEmerald)
                                }
                            }
                            IconButton(
                                onClick = { showFilterSheet = true },
                                modifier = Modifier.testTag("filter_workers_button")
                            ) {
                                Icon(Icons.Default.Tune, "Filters", tint = KashmirEmerald)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Universal Worker Search
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        placeholder = { Text("Search by skill, craft, profession or area...") },
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
                            .testTag("workers_search_field"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KashmirEmerald,
                            unfocusedBorderColor = KashmirCardBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // District Selector Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedDistrict == "All Districts",
                                onClick = { viewModel.selectedDistrict.value = "All Districts" },
                                label = { Text("All Districts", fontSize = 11.sp) }
                            )
                        }
                        items(KASHMIR_DISTRICTS) { dist ->
                            FilterChip(
                                selected = selectedDistrict == dist,
                                onClick = { viewModel.selectedDistrict.value = dist },
                                label = { Text(dist, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Privacy Banner Reminder (Ensures User Trust)
        item {
            Surface(
                color = KashmirMint.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, null, tint = KashmirEmerald, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Protected: Worker phone numbers are never publicly visible. Connect securely via private voice-first messaging.",
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirDeepGreen, fontSize = 11.sp)
                    )
                }
            }
        }

        // Workers List
        if (isWorkersLoading) {
            items(4) {
                WorkerCardSkeleton()
            }
        } else if (filteredWorkers.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = KashmirTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No workers found matching your query",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KashmirTextPrimary
                    )
                    Text(
                        text = "Try adjusting your search criteria or district filter.",
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                    )
                }
            }
        } else {
            items(filteredWorkers) { worker ->
                WorkerCardItem(
                    worker = worker,
                    onViewProfile = { viewModel.viewWorkerDetail(worker) },
                    onMessage = {
                        viewModel.startConversationWith(
                            targetUserId = worker.userId,
                            targetUserName = worker.name,
                            subject = "Inquiry regarding ${worker.profession}"
                        )
                    }
                )
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Filter Workers",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Availability", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedAvailability == "All Availability",
                            onClick = { viewModel.selectedAvailability.value = "All Availability" },
                            label = { Text("All Availability") }
                        )
                    }
                    items(WORKER_AVAILABILITY) { avail ->
                        FilterChip(
                            selected = selectedAvailability == avail,
                            onClick = { viewModel.selectedAvailability.value = avail },
                            label = { Text(avail) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Popular Professions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedProfession == "All Professions",
                            onClick = { viewModel.selectedProfession.value = "All Professions" },
                            label = { Text("All Professions") }
                        )
                    }
                    items(KASHMIR_PROFESSIONS.take(10)) { prof ->
                        FilterChip(
                            selected = selectedProfession == prof,
                            onClick = { viewModel.selectedProfession.value = prof },
                            label = { Text(prof) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Apply Filters")
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun WorkerCardItem(
    worker: WorkerProfileEntity,
    onViewProfile: () -> Unit,
    onMessage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onViewProfile() }
            .testTag("worker_item_${worker.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(KashmirEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = worker.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Worker",
                            tint = KashmirEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = worker.profession,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = KashmirEmerald
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = KashmirTextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${worker.area}, ${worker.district}",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• ${worker.experienceYears}y exp",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextMuted)
                        )
                    }
                }

                Surface(
                    color = KashmirMint,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = KashmirWarmGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${worker.rating}",
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = "(${worker.reviewCount})",
                            fontSize = 10.sp,
                            color = KashmirTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (worker.bio.isNotBlank()) {
                Text(
                    text = worker.bio,
                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Skills chips
            val skillList = worker.skills.split(",").map { it.trim() }.filter { it.isNotBlank() }
            if (skillList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(skillList.take(4)) { skill ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = KashmirSurfaceVariant
                        ) {
                            Text(
                                text = skill,
                                fontSize = 11.sp,
                                color = KashmirTextPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Availability & Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KashmirBadge(
                        text = worker.availability,
                        containerColor = if (worker.availability == "Available Now") KashmirMint else KashmirCardBorder,
                        contentColor = if (worker.availability == "Available Now") KashmirDeepGreen else KashmirTextPrimary
                    )
                    if (worker.isSeasonalWorker) {
                        KashmirBadge(
                            text = "Seasonal Worker",
                            containerColor = KashmirGold.copy(alpha = 0.25f),
                            contentColor = KashmirDeepGreen
                        )
                    }
                }

                Text(
                    text = worker.expectedPayment,
                    fontWeight = FontWeight.Bold,
                    color = KashmirDeepGreen,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewProfile,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("View Profile", fontSize = 12.sp)
                }

                Button(
                    onClick = onMessage,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect (Voice First)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
