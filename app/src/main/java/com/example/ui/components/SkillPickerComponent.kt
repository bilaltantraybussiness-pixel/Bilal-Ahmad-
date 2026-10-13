package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SkillPickerComponent(
    selectedSkills: List<String>,
    onSkillsChanged: (List<String>) -> Unit,
    profession: String = "",
    label: String = "Skills & Expertise",
    hint: String = "Search or add any skill related to your work...",
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var customSkillInput by remember { mutableStateOf("") }
    var showAllSuggestions by remember { mutableStateOf(false) }

    // Retrieve suggested skills for the selected profession
    val suggestedSkills = remember(profession) {
        val matched = SUGGESTED_SKILLS_BY_PROFESSION.entries.firstOrNull { (prof, _) ->
            prof.contains(profession, ignoreCase = true) || profession.contains(prof, ignoreCase = true)
        }?.value ?: GENERAL_POPULAR_SKILLS
        matched
    }

    val top5Suggestions = remember(suggestedSkills) {
        suggestedSkills.take(5)
    }

    val top10Suggestions = remember(suggestedSkills) {
        suggestedSkills.take(10)
    }

    // Filtered skills based on user search
    val filteredSkills = remember(searchQuery, suggestedSkills) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val allSkillsPool = (suggestedSkills + GENERAL_POPULAR_SKILLS + SUGGESTED_SKILLS_BY_PROFESSION.values.flatten()).distinct()
            allSkillsPool.filter { it.contains(searchQuery.trim(), ignoreCase = true) && it !in selectedSkills }
        }
    }

    fun addSkill(skill: String) {
        val trimmed = skill.trim()
        if (trimmed.isNotBlank() && !selectedSkills.any { it.equals(trimmed, ignoreCase = true) }) {
            onSkillsChanged(selectedSkills + trimmed)
        }
    }

    fun removeSkill(skill: String) {
        onSkillsChanged(selectedSkills.filterNot { it.equals(skill, ignoreCase = true) })
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("skill_picker_component"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = KashmirTextPrimary)
                    )
                }

                if (selectedSkills.isNotEmpty()) {
                    Text(
                        text = "${selectedSkills.size} selected",
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Skills Chips
            if (selectedSkills.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedSkills.forEach { skill ->
                        InputChip(
                            selected = true,
                            onClick = { removeSkill(skill) },
                            label = { Text(skill, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove $skill", modifier = Modifier.size(14.dp))
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = KashmirMint,
                                selectedLabelColor = KashmirDeepGreen
                            ),
                            border = InputChipDefaults.inputChipBorder(
                                enabled = true,
                                selected = true,
                                borderColor = KashmirEmerald.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("skill_chip_${skill.replace(" ", "_")}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    text = "No skills selected yet. Choose from suggested list below or type a custom skill.",
                    fontSize = 12.sp,
                    color = KashmirTextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Search Skills Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(hint, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = KashmirEmerald, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skill_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Search Autocomplete Results
            if (filteredSkills.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Search Matches (Tap to add):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredSkills.take(8).forEach { match ->
                        SuggestionChip(
                            onClick = {
                                addSkill(match)
                                searchQuery = ""
                            },
                            label = { Text("+ $match", fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = KashmirMint
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = KashmirEmerald
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Suggested Top 5 / Top 10 Skills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllSuggestions) "Top 10 Suggested Skills:" else "Top 5 Suggested Skills:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = KashmirDeepGreen
                )

                Text(
                    text = if (showAllSuggestions) "Show Top 5" else "Show Top 10",
                    fontSize = 11.sp,
                    color = KashmirEmerald,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAllSuggestions = !showAllSuggestions }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val currentSuggestions = if (showAllSuggestions) top10Suggestions else top5Suggestions
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentSuggestions.forEach { skill ->
                    val isAlreadySelected = selectedSkills.any { it.equals(skill, ignoreCase = true) }
                    AssistChip(
                        onClick = {
                            if (isAlreadySelected) removeSkill(skill) else addSkill(skill)
                        },
                        label = {
                            Text(
                                text = if (isAlreadySelected) "✓ $skill" else "+ $skill",
                                fontSize = 11.sp,
                                fontWeight = if (isAlreadySelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isAlreadySelected) KashmirEmerald else KashmirBackground,
                            labelColor = if (isAlreadySelected) Color.White else KashmirTextPrimary
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = if (isAlreadySelected) KashmirEmerald else KashmirCardBorder
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add Custom Skill Section
            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Text("Add Any Custom Skill:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = KashmirTextSecondary)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customSkillInput,
                    onValueChange = { customSkillInput = it },
                    placeholder = { Text("e.g. Kashmiri Wood Lacquer or Antique Restoration", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_custom_skill_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (customSkillInput.isNotBlank()) {
                            addSkill(customSkillInput)
                            customSkillInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("add_custom_skill_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
