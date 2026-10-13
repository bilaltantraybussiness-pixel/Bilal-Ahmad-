package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MessagesScreen(viewModel: RozgarViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val conversations by viewModel.userConversations.collectAsState()
    val isMessagesLoading by viewModel.isMessagesLoading.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KashmirBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Private Messages",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KashmirDeepGreen
                                )
                            )
                            Text(
                                text = "Voice-First private communications between workers, employers & buyers",
                                style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.refreshMessagesData() },
                            enabled = !isMessagesLoading,
                            modifier = Modifier.testTag("refresh_messages_button")
                        ) {
                            if (isMessagesLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = KashmirEmerald
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "Refresh Messages", tint = KashmirEmerald)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = KashmirMint.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mic, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voice-First Protection: New chats always begin with a voice note for genuine contact.",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = KashmirDeepGreen,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        if (isMessagesLoading) {
            items(4) {
                MessageItemSkeleton()
            }
        } else if (conversations.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Forum,
                        contentDescription = null,
                        tint = KashmirTextMuted,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No conversations yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = KashmirTextPrimary
                    )
                    Text(
                        text = "Connect with a worker, employer, or seller to start a conversation.",
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            items(conversations) { conv ->
                val otherUserName = if (conv.user1Id == currentUser?.id) conv.user2Name else conv.user1Name
                val timeString = remember(conv.lastMessageTime) {
                    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                    sdf.format(Date(conv.lastMessageTime))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 5.dp)
                        .clickable { viewModel.openChat(conv) }
                        .testTag("conversation_item_${conv.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(KashmirEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = otherUserName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = otherUserName,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = timeString,
                                    style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextMuted)
                                )
                            }

                            Text(
                                text = conv.subject,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = KashmirEmerald,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (!conv.hasFirstVoiceNote) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice Required",
                                        tint = KashmirWarmGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = conv.lastMessageText.ifBlank { "Tap to open chat" },
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
