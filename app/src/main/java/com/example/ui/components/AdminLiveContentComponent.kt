package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.PlatformSettingsEntity
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@Composable
fun AdminLiveContentContent(
    viewModel: RozgarViewModel,
    currentSettings: PlatformSettingsEntity?
) {
    val initial = currentSettings ?: PlatformSettingsEntity()

    var bannerText by remember(currentSettings) { mutableStateOf(initial.announcementBanner) }
    var bannerActive by remember(currentSettings) { mutableStateOf(initial.announcementBannerActive) }
    var demoBadgeEnabled by remember(currentSettings) { mutableStateOf(initial.demoModeLabelEnabled) }
    var categoriesCsv by remember(currentSettings) { mutableStateOf(initial.customCategoriesCsv) }

    var newCategoryInput by remember { mutableStateOf("") }
    var showSeedSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Card 1: Live Broadcast Announcement Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, KashmirCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Live App Announcement Banner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Broadcast important messages to all Kashmir users on the Home screen",
                            fontSize = 11.sp,
                            color = KashmirTextSecondary
                        )
                    }

                    Switch(
                        checked = bannerActive,
                        onCheckedChange = { bannerActive = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bannerText,
                    onValueChange = { bannerText = it },
                    label = { Text("Banner Message (English / Urdu / Kashmiri)") },
                    placeholder = { Text("e.g. Welcome to Rozgar Kashmir — Valley's Verified Employment & Artisan Marketplace") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banner_text_input"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val updated = initial.copy(
                            announcementBanner = bannerText.trim(),
                            announcementBannerActive = bannerActive,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updatePlatformSettings(updated)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("save_banner_settings_btn")
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Live Banner")
                }
            }
        }

        // Card 2: Demo & Realistic Sample Content Manager (Requirement 3)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KashmirSurfaceGreen),
            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Demo & Realistic Sample Content Controls",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = KashmirDeepGreen
                )
                Text(
                    text = "Ensure first-time visitors always see rich Kashmiri content (jobs, artisans, shops, wazwan, properties).",
                    fontSize = 11.sp,
                    color = KashmirTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Display [DEMO / SAMPLE] Badges",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Clearly label sample listings for regulatory transparency during app store review.",
                            fontSize = 11.sp,
                            color = KashmirTextMuted
                        )
                    }

                    Switch(
                        checked = demoBadgeEnabled,
                        onCheckedChange = {
                            demoBadgeEnabled = it
                            val updated = initial.copy(
                                demoModeLabelEnabled = it,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.updatePlatformSettings(updated)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = KashmirEmerald.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sample Data Lifecycle Actions",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = KashmirDeepGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.reseedRealisticDemoContent()
                            showSeedSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reseed_sample_content_btn")
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-Seed Sample Data", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.purgeDemoContent()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("purge_sample_content_btn")
                    ) {
                        Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Purge Demo Data", fontSize = 11.sp)
                    }
                }

                if (showSeedSuccess) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Realistic Kashmir sample jobs, shops, crafts, and food populated!",
                        color = KashmirEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Card 3: Dynamic Category Manager (No Code Changes Required)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, KashmirCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Unlimited Dynamic Marketplace Categories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = KashmirDeepGreen
                )
                Text(
                    text = "Add or remove categories dynamically without redeploying APK code.",
                    fontSize = 11.sp,
                    color = KashmirTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Current Categories as Chips
                val categoriesList = remember(categoriesCsv) {
                    categoriesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoriesList.forEach { category ->
                        InputChip(
                            selected = true,
                            onClick = {},
                            label = { Text(category, fontSize = 11.sp) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            val updatedList = categoriesList.filter { it != category }
                                            categoriesCsv = updatedList.joinToString(", ")
                                            val updatedSettings = initial.copy(customCategoriesCsv = categoriesCsv)
                                            viewModel.updatePlatformSettings(updatedSettings)
                                        }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text("New Category Name") },
                        placeholder = { Text("e.g. Kashmiri Honey & Bee Keeping") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newCategoryInput.isNotBlank()) {
                                val updatedList = categoriesList + newCategoryInput.trim()
                                categoriesCsv = updatedList.joinToString(", ")
                                val updatedSettings = initial.copy(customCategoriesCsv = categoriesCsv)
                                viewModel.updatePlatformSettings(updatedSettings)
                                newCategoryInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
