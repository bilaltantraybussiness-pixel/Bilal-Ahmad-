package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LegalPoliciesDefaults
import com.example.data.model.LegalPolicyEntity
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoliciesAndLegalScreen(
    viewModel: RozgarViewModel,
    initialPolicyKey: String = "privacy_policy",
    onNavigateBack: () -> Unit = { viewModel.navigateBack() }
) {
    val policiesFromDb by viewModel.allLegalPolicies.collectAsState()
    val allPolicies = remember(policiesFromDb) {
        if (policiesFromDb.isNotEmpty()) policiesFromDb else LegalPoliciesDefaults.getDefaultPolicies()
    }

    var selectedKey by remember(initialPolicyKey) { mutableStateOf(initialPolicyKey) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val activePolicy = remember(allPolicies, selectedKey) {
        allPolicies.find { it.policyKey == selectedKey } ?: allPolicies.firstOrNull()
    }

    val categories = listOf("ALL", "LEGAL", "COMMERCE", "SAFETY", "USER")
    val filteredPolicies = remember(allPolicies, selectedCategory) {
        if (selectedCategory == "ALL") allPolicies else allPolicies.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Legal, Policies & Terms",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary
                            )
                        )
                        Text(
                            text = "Rozgar Kashmir Platform Governance",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = KashmirDeepGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding)
        ) {
            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KashmirMint,
                            selectedLabelColor = KashmirForestGreen
                        )
                    )
                }
            }

            // Policy Document Selector Carousel
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredPolicies) { pol ->
                    val isSelected = pol.policyKey == selectedKey
                    SuggestionChip(
                        onClick = { selectedKey = pol.policyKey },
                        label = {
                            Text(
                                text = pol.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) KashmirSurfaceGreen else Color.White,
                            labelColor = if (isSelected) KashmirDeepGreen else KashmirTextSecondary
                        ),
                        border = BorderStroke(1.dp, if (isSelected) KashmirEmerald else KashmirCardBorder)
                    )
                }
            }

            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))

            // Main Policy Reader Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (activePolicy != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, KashmirCardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = KashmirMint,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = activePolicy.category,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirForestGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Text(
                                        text = "Version ${activePolicy.version} • ${activePolicy.lastUpdated}",
                                        fontSize = 11.sp,
                                        color = KashmirTextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = activePolicy.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    color = KashmirSurfaceGreen,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Official Platform Policy: Legally binding across all Rozgar Kashmir commerce and employment services.",
                                            fontSize = 11.sp,
                                            color = KashmirDeepGreen
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Render policy text cleanly
                                val paragraphs = activePolicy.contentMarkdown.split("\n\n")
                                paragraphs.forEach { para ->
                                    val trimmed = para.trim()
                                    if (trimmed.startsWith("# ")) {
                                        Text(
                                            text = trimmed.removePrefix("# "),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = KashmirDeepGreen
                                            ),
                                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                        )
                                    } else if (trimmed.startsWith("### ")) {
                                        Text(
                                            text = trimmed.removePrefix("### "),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = KashmirTextPrimary
                                            ),
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    } else if (trimmed.startsWith("* ")) {
                                        Row(modifier = Modifier.padding(vertical = 3.dp)) {
                                            Text("• ", color = KashmirForestGreen, fontWeight = FontWeight.Bold)
                                            Text(
                                                text = trimmed.removePrefix("* "),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = KashmirTextPrimary,
                                                    lineHeight = 20.sp
                                                )
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = trimmed,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = KashmirTextPrimary,
                                                lineHeight = 22.sp
                                            ),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                } else {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Select a policy document above to view details.")
                            }
                        }
                    }
                }
            }
        }
    }
}
