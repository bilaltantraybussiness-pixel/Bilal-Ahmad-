package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun SeasonalWorkScreen(viewModel: RozgarViewModel) {
    val seasonalJobs by viewModel.seasonalJobs.collectAsState()
    val seasonalWorkers by viewModel.seasonalWorkers.collectAsState()
    val isJobsLoading by viewModel.isJobsLoading.collectAsState()
    val isWorkersLoading by viewModel.isWorkersLoading.collectAsState()

    var activeCategory by remember { mutableStateOf("All Seasonal Work") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Seasonal Jobs, 1: Seasonal Workers

    val filteredJobs = remember(seasonalJobs, activeCategory) {
        if (activeCategory == "All Seasonal Work") seasonalJobs
        else seasonalJobs.filter { it.seasonalCategory.contains(activeCategory, ignoreCase = true) || it.title.contains(activeCategory, ignoreCase = true) }
    }

    val filteredWorkers = remember(seasonalWorkers, activeCategory) {
        if (activeCategory == "All Seasonal Work") seasonalWorkers
        else seasonalWorkers.filter { it.seasonalSpecialties.contains(activeCategory, ignoreCase = true) || it.profession.contains(activeCategory, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KashmirBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Seasonal Banner Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Surface(
                        color = KashmirGold,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = " KASHMIR HARVEST & WINTER ECONOMY ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Seasonal Work Hub",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Apple picking in Shopian & Sopore, saffron harvesting in Pampore, winter ski tourism in Gulmarg, and snowfall clearing operations.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = KashmirMint)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector: Jobs vs Workers
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = KashmirForestGreen,
                        indicator = {},
                        divider = {},
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    "Seasonal Jobs (${filteredJobs.size})",
                                    color = if (selectedTab == 0) Color.White else KashmirMint,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 0) KashmirEmerald else Color.Transparent)
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    "Seasonal Workers (${filteredWorkers.size})",
                                    color = if (selectedTab == 1) Color.White else KashmirMint,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) KashmirEmerald else Color.Transparent)
                        )
                    }
                }
            }
        }

        // Seasonal Category Filters
        item {
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "Specialty Work Type",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SEASONAL_WORK_TYPES) { type ->
                        FilterChip(
                            selected = activeCategory == type,
                            onClick = { activeCategory = type },
                            label = { Text(type, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirMint,
                                selectedLabelColor = KashmirDeepGreen
                            )
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // Seasonal Jobs List
            if (isJobsLoading) {
                items(3) {
                    JobCardSkeleton()
                }
            } else if (filteredJobs.isEmpty()) {
                item {
                    Text(
                        text = "No seasonal job postings currently for $activeCategory",
                        modifier = Modifier.padding(24.dp),
                        color = KashmirTextSecondary
                    )
                }
            } else {
                items(filteredJobs) { job ->
                    JobCardItem(
                        job = job,
                        onViewDetails = { viewModel.viewJobDetail(job) },
                        onApply = { viewModel.viewJobDetail(job) }
                    )
                }
            }
        } else {
            // Seasonal Workers List
            if (isWorkersLoading) {
                items(3) {
                    WorkerCardSkeleton()
                }
            } else if (filteredWorkers.isEmpty()) {
                item {
                    Text(
                        text = "No seasonal workers currently listed for $activeCategory",
                        modifier = Modifier.padding(24.dp),
                        color = KashmirTextSecondary
                    )
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
                                subject = "Seasonal Work Inquiry: ${worker.profession}"
                            )
                        }
                    )
                }
            }
        }
    }
}
