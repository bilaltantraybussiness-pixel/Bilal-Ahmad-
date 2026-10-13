package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: RozgarViewModel) {
    val notifications by viewModel.userNotifications.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isNotificationsLoading by viewModel.isNotificationsLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    currentUser?.let { user ->
                        TextButton(onClick = {
                            // Mark read
                        }) {
                            Text("Mark all read", color = Color.White, fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KashmirDeepGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            if (isNotificationsLoading) {
                items(5) {
                    NotificationCardSkeleton()
                }
            } else if (notifications.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = KashmirTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No notifications yet",
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                        Text(
                            text = "You'll be notified of new job alerts, voice messages and application status updates here.",
                            color = KashmirTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }
                }
            } else {
                items(notifications) { notif ->
                    val timeStr = remember(notif.createdAt) {
                        SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(notif.createdAt))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (notif.type) {
                                    "VOICE_MESSAGE" -> KashmirGold.copy(alpha = 0.25f)
                                    "JOB_APPLIED" -> KashmirMint
                                    else -> KashmirSurfaceVariant
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (notif.type) {
                                            "VOICE_MESSAGE" -> Icons.Default.Mic
                                            "MESSAGE" -> Icons.Default.Chat
                                            "JOB_APPLIED" -> Icons.Default.AssignmentInd
                                            else -> Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = KashmirDeepGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(notif.message, fontSize = 12.sp, color = KashmirTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(timeStr, fontSize = 10.sp, color = KashmirTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
