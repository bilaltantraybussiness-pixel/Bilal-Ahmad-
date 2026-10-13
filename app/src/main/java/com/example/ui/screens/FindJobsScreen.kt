package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindJobsScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val allJobs by viewModel.activeJobs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedDistrict by viewModel.selectedDistrict.collectAsState()
    val selectedWorkType by viewModel.selectedWorkType.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isJobsLoading by viewModel.isJobsLoading.collectAsState()
    val isSyncing by viewModel.isSyncingJobs.collectAsState()

    var selectedQuickTab by remember { mutableIntStateOf(0) } // 0: All, 1: Latest, 2: Nearby, 3: Seasonal
    var selectedProfession by remember { mutableStateOf("All Professions") }
    var selectedPaymentFilter by remember { mutableStateOf("All") }
    var showFilterSheet by remember { mutableStateOf(false) }

    val filteredJobs = remember(
        allJobs,
        searchQuery,
        selectedDistrict,
        selectedWorkType,
        selectedProfession,
        selectedPaymentFilter,
        selectedQuickTab,
        currentUser?.district
    ) {
        var list = allJobs.filter { job ->
            val matchesQuery = searchQuery.isBlank() ||
                job.title.contains(searchQuery, ignoreCase = true) ||
                job.profession.contains(searchQuery, ignoreCase = true) ||
                job.requiredSkills.contains(searchQuery, ignoreCase = true) ||
                job.area.contains(searchQuery, ignoreCase = true) ||
                job.district.contains(searchQuery, ignoreCase = true)

            val matchesDistrict = selectedDistrict == "All Districts" ||
                job.district.equals(selectedDistrict, ignoreCase = true)

            val matchesType = selectedWorkType == "All Types" ||
                job.workType.equals(selectedWorkType, ignoreCase = true)

            val matchesProf = selectedProfession == "All Professions" ||
                job.profession.contains(selectedProfession, ignoreCase = true)

            val matchesPayment = when (selectedPaymentFilter) {
                "Daily ₹800+" -> job.paymentAmount.filter { it.isDigit() }.toIntOrNull()?.let { it >= 800 } ?: true
                "Daily ₹1,000+" -> job.paymentAmount.filter { it.isDigit() }.toIntOrNull()?.let { it >= 1000 } ?: true
                "Monthly ₹15k+" -> job.paymentAmount.filter { it.isDigit() }.toIntOrNull()?.let { it >= 15000 } ?: true
                else -> true
            }

            val matchesTab = when (selectedQuickTab) {
                2 -> currentUser?.district?.let { job.district.equals(it, ignoreCase = true) } ?: true
                3 -> job.isSeasonal
                else -> true
            }

            matchesQuery && matchesDistrict && matchesType && matchesProf && matchesPayment && matchesTab
        }

        if (selectedQuickTab == 1) {
            list = list.sortedByDescending { it.createdAt }
        }
        list
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.POST_JOB) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Post a Job", fontWeight = FontWeight.Bold) },
                containerColor = KashmirDeepGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .testTag("find_jobs_post_fab")
                    .padding(bottom = 60.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Search & Filters
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
                                    text = "Find Jobs in Kashmir",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "${filteredJobs.size} jobs available across 10 districts",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.refreshJobsData() },
                                    enabled = !isSyncing && !isJobsLoading,
                                    modifier = Modifier.testTag("sync_firestore_jobs_button")
                                ) {
                                    if (isSyncing || isJobsLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = KashmirEmerald
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.CloudSync,
                                            contentDescription = "Sync Jobs with Firestore",
                                            tint = KashmirEmerald
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showFilterSheet = true },
                                    modifier = Modifier.testTag("filter_jobs_button")
                                ) {
                                    Icon(Icons.Default.Tune, "Filters", tint = KashmirEmerald)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text("Search by title, skills, area, or payment...") },
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
                                .testTag("jobs_search_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KashmirEmerald,
                                unfocusedBorderColor = KashmirCardBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Filter Tabs (All, Latest, Nearby, Seasonal)
                        val quickTabs = listOf("All Jobs", "⚡ Latest Jobs", "📍 Nearby Jobs", "🍎 Seasonal Jobs")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickTabs.indices.toList()) { idx ->
                                FilterChip(
                                    selected = selectedQuickTab == idx,
                                    onClick = { selectedQuickTab = idx },
                                    label = { Text(quickTabs[idx], fontSize = 11.sp, fontWeight = if (selectedQuickTab == idx) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KashmirMint,
                                        selectedLabelColor = KashmirDeepGreen
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // District Filter Carousel
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

            // Results count and reset if active
            item {
                if (selectedDistrict != "All Districts" || selectedWorkType != "All Types" || selectedProfession != "All Professions" || searchQuery.isNotBlank() || selectedQuickTab != 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filtered Jobs: ${filteredJobs.size}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        )
                        Text(
                            text = "Reset All Filters",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald, fontWeight = FontWeight.Bold),
                            modifier = Modifier.clickable {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedDistrict.value = "All Districts"
                                viewModel.selectedWorkType.value = "All Types"
                                selectedProfession = "All Professions"
                                selectedPaymentFilter = "All"
                                selectedQuickTab = 0
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Job List Feed
            if (isJobsLoading || isSyncing) {
                items(4) {
                    JobCardSkeleton()
                }
            } else if (filteredJobs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.WorkOff,
                            contentDescription = null,
                            tint = KashmirTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No jobs found matching your filters.",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Try clearing filters or search another Kashmiri district.",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.searchQuery.value = ""
                                viewModel.selectedDistrict.value = "All Districts"
                                viewModel.selectedWorkType.value = "All Types"
                                selectedProfession = "All Professions"
                                selectedPaymentFilter = "All"
                                selectedQuickTab = 0
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                        ) {
                            Text("Show All Jobs")
                        }
                    }
                }
            } else {
                items(filteredJobs, key = { it.id }) { job ->
                    JobCardItem(
                        job = job,
                        onViewDetails = { viewModel.viewJobDetail(job) },
                        onApply = { viewModel.viewJobDetail(job) },
                        onSave = {
                            viewModel.toggleSave("JOB", job.id, job.title, "${job.area}, ${job.district}")
                        },
                        onShare = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Rozgar Kashmir Job: ${job.title}\nProfession: ${job.profession}\nLocation: ${job.area}, ${job.district}\nWage: ${job.paymentAmount}/${job.paymentPeriod}\nStart Date: ${job.startDate}\nApply directly on Rozgar Kashmir."
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Job via"))
                        }
                    )
                }
            }
        }
    }

    // Comprehensive Filter Modal Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter Kashmir Jobs", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    TextButton(onClick = {
                        viewModel.selectedDistrict.value = "All Districts"
                        viewModel.selectedWorkType.value = "All Types"
                        selectedProfession = "All Professions"
                        selectedPaymentFilter = "All"
                        selectedQuickTab = 0
                    }) {
                        Text("Reset All", color = KashmirEmerald)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Profession Filter
                Text("Required Profession", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedProfession == "All Professions",
                            onClick = { selectedProfession = "All Professions" },
                            label = { Text("All Professions", fontSize = 11.sp) }
                        )
                    }
                    items(KASHMIR_PROFESSIONS.take(15)) { prof ->
                        FilterChip(
                            selected = selectedProfession == prof,
                            onClick = { selectedProfession = prof },
                            label = { Text(prof, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Work Type
                Text("Work Type", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedWorkType == "All Types",
                            onClick = { viewModel.selectedWorkType.value = "All Types" },
                            label = { Text("All Types", fontSize = 11.sp) }
                        )
                    }
                    items(JOB_TYPES) { type ->
                        FilterChip(
                            selected = selectedWorkType == type,
                            onClick = { viewModel.selectedWorkType.value = type },
                            label = { Text(type, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Filter
                Text("Payment / Wage Range", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                val payFilters = listOf("All", "Daily ₹800+", "Daily ₹1,000+", "Monthly ₹15k+")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(payFilters) { p ->
                        FilterChip(
                            selected = selectedPaymentFilter == p,
                            onClick = { selectedPaymentFilter = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showFilterSheet = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun JobCardItem(
    job: JobEntity,
    onViewDetails: () -> Unit,
    onApply: () -> Unit,
    onSave: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    val formattedPostedDate = remember(job.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(job.createdAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onViewDetails() }
            .testTag("job_item_${job.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title, Employer, Payment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Posted by: ${job.employerName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = KashmirMint,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${job.paymentAmount} / ${job.paymentPeriod}",
                        fontWeight = FontWeight.ExtraBold,
                        color = KashmirDeepGreen,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Required Profession Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = KashmirSurfaceGreen,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Engineering, null, tint = KashmirDeepGreen, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Profession: ${job.profession}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KashmirDeepGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                // Job Status
                Surface(
                    color = if (job.status == "ACTIVE") KashmirMint else KashmirCardBorder,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = job.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (job.status == "ACTIVE") KashmirDeepGreen else KashmirTextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: District & Area, Work Type, Seasonal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KashmirBadge(text = "${job.area}, ${job.district}", containerColor = KashmirSurfaceGreen)
                KashmirBadge(text = job.workType, containerColor = KashmirMint)
                if (job.isSeasonal) {
                    KashmirBadge(
                        text = "Seasonal (${job.seasonalCategory})",
                        containerColor = KashmirGold.copy(alpha = 0.25f),
                        contentColor = KashmirDeepGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Operational Details: Workers Needed, Start Date, Posted Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${job.workersNeeded} Workers Needed",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Event, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Starts: ${job.startDate}",
                        fontSize = 11.sp,
                        color = KashmirTextSecondary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = KashmirTextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formattedPostedDate,
                        fontSize = 11.sp,
                        color = KashmirTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Actions: Save, Share, View & Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onSave, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.BookmarkBorder, "Save", tint = KashmirEmerald, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Share, "Share", tint = KashmirTextSecondary, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("View & Apply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
