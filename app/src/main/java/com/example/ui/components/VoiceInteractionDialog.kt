package com.example.ui.components

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.SpeechToTextManager
import com.example.util.VoiceAction
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceInteractionDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val speechManager = remember { SpeechToTextManager(context) }

    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val isListening by speechManager.isListening.collectAsState()
    val recognizedText by speechManager.recognizedText.collectAsState()
    val partialText by speechManager.partialText.collectAsState()
    val rmsDb by speechManager.rmsDb.collectAsState()
    val errorMessage by speechManager.errorMessage.collectAsState()

    var selectedLang by remember { mutableStateOf("en-IN") } // "en-IN", "ur-IN", "hi-IN"

    // Cleanup on dispose
    DisposableEffect(speechManager) {
        onDispose {
            speechManager.destroy()
        }
    }

    // Auto-start listening if permission granted
    LaunchedEffect(audioPermissionState.status.isGranted) {
        if (audioPermissionState.status.isGranted) {
            speechManager.startListening(selectedLang)
        }
    }

    // Animated pulse scale for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val audioMeterScale = (1f + (rmsDb / 15f).coerceIn(0f, 0.4f)) * if (isListening) pulseScale else 1f

    val detectedAction = remember(recognizedText) {
        if (recognizedText.isNotBlank()) SpeechToTextManager.parseVoiceCommand(recognizedText) else null
    }

    Dialog(onDismissRequest = {
        speechManager.stopListening()
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("voice_interaction_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top row with status & close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isListening) KashmirEmerald else KashmirTextSecondary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isListening) "Listening in Kashmir..." else "Tap Mic to Speak",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isListening) KashmirDeepGreen else KashmirTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    IconButton(
                        onClick = {
                            speechManager.stopListening()
                            onDismiss()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = KashmirTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Audio Microphone Orb
                Box(
                    modifier = Modifier
                        .size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(audioMeterScale)
                                .clip(CircleShape)
                                .background(KashmirEmerald.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .scale(audioMeterScale * 0.9f)
                                .clip(CircleShape)
                                .background(KashmirEmerald.copy(alpha = 0.35f))
                        )
                    }

                    IconButton(
                        onClick = {
                            if (!audioPermissionState.status.isGranted) {
                                audioPermissionState.launchPermissionRequest()
                            } else {
                                if (isListening) {
                                    speechManager.stopListening()
                                } else {
                                    speechManager.startListening(selectedLang)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(if (isListening) KashmirEmerald else KashmirDeepGreen)
                            .testTag("voice_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selector Pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguagePill(
                        label = "English",
                        isSelected = selectedLang == "en-IN",
                        onClick = {
                            selectedLang = "en-IN"
                            if (audioPermissionState.status.isGranted) speechManager.startListening("en-IN")
                        }
                    )
                    LanguagePill(
                        label = "اردو (Urdu)",
                        isSelected = selectedLang == "ur-IN",
                        onClick = {
                            selectedLang = "ur-IN"
                            if (audioPermissionState.status.isGranted) speechManager.startListening("ur-IN")
                        }
                    )
                    LanguagePill(
                        label = "Hindi / Kashmiri",
                        isSelected = selectedLang == "hi-IN",
                        onClick = {
                            selectedLang = "hi-IN"
                            if (audioPermissionState.status.isGranted) speechManager.startListening("hi-IN")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Transcription Display
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = KashmirBackground,
                    border = androidx.compose.foundation.BorderStroke(1.dp, KashmirCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val textToDisplay = when {
                            partialText.isNotBlank() -> partialText
                            recognizedText.isNotBlank() -> recognizedText
                            isListening -> "Say e.g. \"Find carpenters in Srinagar\", \"Apple harvest jobs\", \"Marketplace\"..."
                            else -> "Tap the microphone to speak."
                        }

                        Text(
                            text = textToDisplay,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (recognizedText.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (recognizedText.isNotBlank()) KashmirTextPrimary else KashmirTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }

                // Error alert if any
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = KashmirError,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Permission prompt if not granted
                if (!audioPermissionState.status.isGranted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { audioPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Mic, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Microphone Access", fontSize = 13.sp)
                    }
                }

                // Actionable Detected Voice Command
                detectedAction?.let { action ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = KashmirMint,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Detected Intent:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            when (action) {
                                is VoiceAction.NavigateToJobs -> {
                                    VoiceActionButton(
                                        title = "Open Find Jobs",
                                        icon = Icons.Default.Work,
                                        onClick = {
                                            viewModel.navigateTo(Screen.FIND_JOBS)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToWorkers -> {
                                    VoiceActionButton(
                                        title = "Find Verified Workers",
                                        icon = Icons.Default.Engineering,
                                        onClick = {
                                            viewModel.navigateTo(Screen.FIND_WORKERS)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToSeasonal -> {
                                    VoiceActionButton(
                                        title = "Open Seasonal Work (Apple/Saffron)",
                                        icon = Icons.Default.Park,
                                        onClick = {
                                            viewModel.navigateTo(Screen.SEASONAL_WORK)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToMarketplace -> {
                                    VoiceActionButton(
                                        title = "Open Kashmiri Marketplace",
                                        icon = Icons.Default.Storefront,
                                        onClick = {
                                            viewModel.navigateTo(Screen.MARKETPLACE)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToPostJob -> {
                                    VoiceActionButton(
                                        title = "Post a New Job",
                                        icon = Icons.Default.AddCircle,
                                        onClick = {
                                            viewModel.navigateTo(Screen.POST_JOB)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToMessages -> {
                                    VoiceActionButton(
                                        title = "Open Messages & Inquiries",
                                        icon = Icons.Default.Chat,
                                        onClick = {
                                            viewModel.navigateTo(Screen.MESSAGES)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToProfile -> {
                                    VoiceActionButton(
                                        title = "Open My Profile",
                                        icon = Icons.Default.Person,
                                        onClick = {
                                            viewModel.navigateTo(Screen.MY_PROFILE)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToAdmin -> {
                                    VoiceActionButton(
                                        title = "Open Admin Dashboard",
                                        icon = Icons.Default.AdminPanelSettings,
                                        onClick = {
                                            viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.NavigateToDonate -> {
                                    VoiceActionButton(
                                        title = "Support Platform Donation",
                                        icon = Icons.Default.VolunteerActivism,
                                        onClick = {
                                            viewModel.navigateTo(Screen.DONATE)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.Search -> {
                                    VoiceActionButton(
                                        title = "Search for \"${action.query}\"",
                                        icon = Icons.Default.Search,
                                        onClick = {
                                            viewModel.searchQuery.value = action.query
                                            viewModel.navigateTo(Screen.FIND_WORKERS)
                                            onDismiss()
                                        }
                                    )
                                }
                                is VoiceAction.GeneralQuery -> {
                                    VoiceActionButton(
                                        title = "Search \"${action.rawText}\"",
                                        icon = Icons.Default.Search,
                                        onClick = {
                                            viewModel.searchQuery.value = action.rawText
                                            viewModel.navigateTo(Screen.FIND_WORKERS)
                                            onDismiss()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) KashmirDeepGreen else KashmirBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) KashmirDeepGreen else KashmirCardBorder
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else KashmirTextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun VoiceActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
