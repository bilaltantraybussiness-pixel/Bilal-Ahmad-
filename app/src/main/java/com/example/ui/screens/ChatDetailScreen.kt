package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.MessageEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(viewModel: RozgarViewModel) {
    val conversation by viewModel.selectedConversation.collectAsState()
    val messages by viewModel.currentChatMessages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showShareContactDialog by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Timer for active voice recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    val conv = conversation
    if (conv == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Conversation not found")
        }
        return
    }

    val otherUserName = if (conv.user1Id == currentUser?.id) conv.user2Name else conv.user1Name
    val otherUserId = if (conv.user1Id == currentUser?.id) conv.user2Id else conv.user1Id

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = otherUserName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = conv.subject,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = KashmirMint,
                                fontSize = 11.sp
                            ),
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report User") },
                            leadingIcon = { Icon(Icons.Default.Report, null, tint = KashmirError) },
                            onClick = {
                                showOptionsMenu = false
                                showReportDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Block User") },
                            leadingIcon = { Icon(Icons.Default.Block, null, tint = KashmirError) },
                            onClick = {
                                showOptionsMenu = false
                                viewModel.submitReport(
                                    reportedType = "USER",
                                    targetId = otherUserId,
                                    targetTitle = otherUserName,
                                    reason = "User Block Request",
                                    details = "Blocked by user."
                                )
                            }
                        )
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
                Column(modifier = Modifier.padding(12.dp)) {
                    // Voice First Rule Enforcement Banner
                    if (!conv.hasFirstVoiceNote) {
                        Surface(
                            color = KashmirMint,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = KashmirDeepGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Voice-First Security Rule Active",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = KashmirDeepGreen
                                    )
                                    Text(
                                        text = "The first message MUST be a voice note. Record a voice note below to start the conversation.",
                                        fontSize = 11.sp,
                                        color = KashmirTextSecondary,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Active Recording HUD
                    if (isRecording) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(KashmirError)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recording Voice Note: ${recordingDuration}s",
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirError,
                                    fontSize = 14.sp
                                )
                            }

                            Row {
                                OutlinedButton(
                                    onClick = { isRecording = false },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Cancel", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val duration = if (recordingDuration > 0) recordingDuration else 5
                                        isRecording = false
                                        viewModel.sendVoiceMessage(
                                            durationSec = duration,
                                            noteSummary = "Voice Introduction ($duration s)"
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("send_voice_note_button")
                                ) {
                                    Text("Send Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Input Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (conv.hasFirstVoiceNote) {
                                // Once voice-first note is done, voluntary contact share is allowed!
                                IconButton(
                                    onClick = { showShareContactDialog = true },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "Share Contact",
                                        tint = KashmirEmerald
                                    )
                                }
                            }

                            // Record Voice Note Button
                            FilledIconButton(
                                onClick = { isRecording = true },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (!conv.hasFirstVoiceNote) KashmirGold else KashmirMint
                                ),
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("record_voice_button")
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Record Voice Note",
                                    tint = KashmirDeepGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Text Field (Disabled if first voice note not sent!)
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                placeholder = {
                                    Text(
                                        if (conv.hasFirstVoiceNote) "Type a message..."
                                        else "Record a voice note first to unlock text chat",
                                        fontSize = 12.sp,
                                        color = KashmirTextMuted
                                    )
                                },
                                enabled = conv.hasFirstVoiceNote,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_text_input"),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Send Button
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendTextMessage(textInput)
                                        textInput = ""
                                    }
                                },
                                enabled = conv.hasFirstVoiceNote && textInput.isNotBlank(),
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (conv.hasFirstVoiceNote && textInput.isNotBlank()) KashmirDeepGreen else KashmirTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { msg ->
                val isSelf = msg.senderUserId == currentUser?.id
                val timeStr = remember(msg.timestamp) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
                ) {
                    if (msg.messageType == "VOICE") {
                        VoiceNotePlayer(
                            durationSeconds = msg.voiceDurationSeconds.coerceAtLeast(3),
                            senderName = msg.senderName,
                            timestamp = timeStr,
                            isSelf = isSelf
                        )
                    } else if (msg.messageType == "CONTACT_SHARE") {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KashmirMint,
                            border = BorderStroke(1.dp, KashmirEmerald),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContactPhone, null, tint = KashmirDeepGreen)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Voluntary Direct Contact Shared",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = KashmirDeepGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = msg.sharedContactInfo,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = KashmirTextPrimary
                                )
                                Text(
                                    text = "Shared privately by ${msg.senderName} • $timeStr",
                                    fontSize = 10.sp,
                                    color = KashmirTextSecondary
                                )
                            }
                        }
                    } else {
                        // Standard text bubble
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isSelf) 14.dp else 2.dp,
                                bottomEnd = if (isSelf) 2.dp else 14.dp
                            ),
                            color = if (isSelf) KashmirDeepGreen else Color.White,
                            shadowElevation = 1.dp,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = msg.textContent,
                                    color = if (isSelf) Color.White else KashmirTextPrimary,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = timeStr,
                                    color = if (isSelf) KashmirMint.copy(alpha = 0.8f) else KashmirTextMuted,
                                    fontSize = 9.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Voluntary Share Contact Dialog
    if (showShareContactDialog) {
        var contactText by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
        AlertDialog(
            onDismissRequest = { showShareContactDialog = false },
            title = { Text("Share Direct Contact Privately", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "You can voluntarily share your phone number directly inside this private chat once you trust this worker/employer. It will NOT be shown publicly.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = contactText,
                        onValueChange = { contactText = it },
                        label = { Text("Phone Number / WhatsApp") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (contactText.isNotBlank()) {
                            viewModel.shareContactPrivately(contactText)
                            showShareContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                ) {
                    Text("Share Privately")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        var reason by remember { mutableStateOf("Fraud / Scam") }
        var details by remember { mutableStateOf("") }
        val reasons = listOf("Fraud / Scam", "Harassment / Abuse", "Inappropriate Content", "Spam", "Fake Identity")

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report User", fontWeight = FontWeight.Bold, color = KashmirError) },
            text = {
                Column {
                    Text("Select reason for reporting this user to Rozgar Kashmir administration:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    reasons.forEach { r ->
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
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text("Additional details (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitReport(
                            reportedType = "USER",
                            targetId = otherUserId,
                            targetTitle = otherUserName,
                            reason = reason,
                            details = details
                        )
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirError)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
