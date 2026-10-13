package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RozgarTopBar(
    viewModel: RozgarViewModel,
    onOpenAuth: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenDonate: () -> Unit,
    onOpenVoice: () -> Unit = {}
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        color = KashmirDeepGreen,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Title & Chinar Emblem
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(Screen.HOME) }
                        .testTag("brand_logo_button")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KashmirGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = "Emblem",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ROZGAR KASHMIR",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Connecting Skills With Opportunities",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = KashmirMint,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Right Actions
                val context = LocalContext.current
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Global Language Switcher (English + Urdu + Kashmiri)
                    val currentLang by LocalizationManager.currentLanguage.collectAsState()
                    var showLangMenu by remember { mutableStateOf(false) }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = KashmirSurfaceGreen.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable { showLangMenu = true }
                                .testTag("top_language_switcher_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Change Language",
                                    tint = KashmirGold,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = when (currentLang) {
                                        AppLanguage.ENGLISH -> "EN"
                                        AppLanguage.URDU -> "اردو"
                                        AppLanguage.KASHMIRI -> "کٲشُر"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirGold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            AppLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${lang.displayName} (${lang.nativeName})",
                                                fontWeight = if (currentLang == lang) FontWeight.Bold else FontWeight.Normal,
                                                color = if (currentLang == lang) KashmirDeepGreen else KashmirTextPrimary
                                            )
                                            if (currentLang == lang) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(Icons.Default.Check, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    },
                                    onClick = {
                                        LocalizationManager.setLanguage(lang, context)
                                        showLangMenu = false
                                    },
                                    modifier = Modifier.testTag("lang_select_${lang.code}")
                                )
                            }
                        }
                    }

                    // Voice assistant microphone button
                    IconButton(
                        onClick = onOpenVoice,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("top_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Assistant & Search",
                            tint = KashmirMint
                        )
                    }

                    // Donate heart shortcut
                    IconButton(
                        onClick = onOpenDonate,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("top_donate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = "Support & Donate",
                            tint = KashmirGold
                        )
                    }

                    // Notifications icon with badge
                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("top_notifications_button")
                    ) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge(containerColor = KashmirSaffron) {
                                    Text("$unreadCount", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White
                            )
                        }
                    }

                    // User Profile / Login Avatar
                    Box {
                        IconButton(
                            onClick = {
                                if (currentUser == null) {
                                    viewModel.navigateTo(Screen.LOGIN)
                                } else {
                                    showUserMenu = true
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("top_user_button")
                        ) {
                            if (currentUser != null) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(KashmirEmerald)
                                        .border(1.5.dp, KashmirGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentUser?.fullName?.take(1)?.uppercase() ?: "U",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Login / Register",
                                    tint = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            currentUser?.let { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(user.fullName, fontWeight = FontWeight.Bold)
                                            Text(
                                                "Role: ${user.roles.split(",").firstOrNull() ?: "Member"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KashmirTextSecondary
                                            )
                                            Text(
                                                "District: ${user.district}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = KashmirEmerald
                                            )
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.navigateTo(Screen.MY_PROFILE)
                                    }
                                )
                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("My Profile & Worker Card") },
                                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.navigateTo(Screen.MY_PROFILE)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("My Applications") },
                                    leadingIcon = { Icon(Icons.Default.Assignment, null) },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.navigateTo(Screen.MY_APPLICATIONS)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Saved Items") },
                                    leadingIcon = { Icon(Icons.Default.Bookmark, null) },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.navigateTo(Screen.SAVED_ITEMS)
                                    }
                                )

                                if (user.roles.contains("ADMIN")) {
                                    DropdownMenuItem(
                                        text = { Text("Admin Dashboard", color = KashmirEmerald, fontWeight = FontWeight.Bold) },
                                        leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null, tint = KashmirEmerald) },
                                        onClick = {
                                            showUserMenu = false
                                            onOpenAdmin()
                                        }
                                    )
                                }

                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Sign In / Admin Login") },
                                    leadingIcon = { Icon(Icons.Default.Login, null) },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.navigateTo(Screen.LOGIN)
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Switch User (Demo Accounts)") },
                                    leadingIcon = { Icon(Icons.Default.SwitchAccount, null) },
                                    onClick = {
                                        showUserMenu = false
                                        onOpenAuth()
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Log Out", color = KashmirError) },
                                    leadingIcon = { Icon(Icons.Default.Logout, null, tint = KashmirError) },
                                    onClick = {
                                        showUserMenu = false
                                        viewModel.logout()
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

@Composable
fun RozgarBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val navItems = listOf(
            Triple(Screen.HOME, "Home", Icons.Default.Home),
            Triple(Screen.FIND_JOBS, "Jobs", Icons.Default.Work),
            Triple(Screen.MARKETPLACE, "Shop", Icons.Default.Storefront),
            Triple(Screen.FOOD, "Food", Icons.Default.Restaurant),
            Triple(Screen.MY_ORDERS, "Orders", Icons.Default.ShoppingBag),
            Triple(Screen.MY_PROFILE, "Profile", Icons.Default.Person)
        )

        navItems.forEach { (screen, label, icon) ->
            val selected = when (screen) {
                Screen.HOME -> currentScreen == Screen.HOME
                Screen.FIND_JOBS -> currentScreen in listOf(Screen.FIND_JOBS, Screen.JOB_DETAIL, Screen.POST_JOB)
                Screen.MARKETPLACE -> currentScreen in listOf(Screen.MARKETPLACE, Screen.SECOND_HAND, Screen.PRODUCT_DETAIL, Screen.SELLER_DASHBOARD)
                Screen.FOOD -> currentScreen == Screen.FOOD
                Screen.MY_ORDERS -> currentScreen in listOf(Screen.MY_ORDERS, Screen.MY_LISTINGS, Screen.MY_APPLICATIONS)
                Screen.MY_PROFILE -> currentScreen in listOf(Screen.MY_PROFILE, Screen.CREATE_PROFILE, Screen.PAYOUTS_SETTINGS)
                else -> currentScreen == screen
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = KashmirDeepGreen,
                    selectedTextColor = KashmirDeepGreen,
                    indicatorColor = KashmirMint,
                    unselectedIconColor = KashmirTextMuted,
                    unselectedTextColor = KashmirTextMuted
                )
            )
        }
    }
}

@Composable
fun KashmirBadge(
    text: String,
    containerColor: Color = KashmirMint,
    contentColor: Color = KashmirDeepGreen
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VoiceNotePlayer(
    durationSeconds: Int,
    senderName: String,
    timestamp: String,
    isSelf: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalSteps = durationSeconds * 10
            for (step in 1..totalSteps) {
                delay(100)
                currentProgress = step / totalSteps.toFloat()
            }
            isPlaying = false
            currentProgress = 0f
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelf) KashmirEmerald.copy(alpha = 0.15f) else KashmirMint.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isSelf) KashmirEmerald.copy(alpha = 0.3f) else KashmirCardBorder),
        modifier = Modifier.fillMaxWidth(0.85f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledIconButton(
                onClick = { isPlaying = !isPlaying },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isSelf) KashmirDeepGreen else KashmirForestGreen
                ),
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Voice Note" else "Play Voice Note",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voice Note (${durationSeconds}s)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = KashmirDeepGreen
                    )
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = KashmirTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { if (isPlaying) currentProgress else 0.4f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isSelf) KashmirDeepGreen else KashmirEmerald,
                    trackColor = KashmirCardBorder
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.SearchOff,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KashmirSurfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(KashmirMint.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = KashmirDeepGreen,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = KashmirDeepGreen,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = KashmirTextSecondary,
                textAlign = TextAlign.Center
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(text = actionText, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
