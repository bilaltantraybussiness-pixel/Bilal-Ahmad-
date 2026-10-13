package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.JobEntity
import com.example.data.model.ProductEntity
import com.example.data.model.WorkerProfileEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomeScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val activeJobs by viewModel.activeJobs.collectAsState()
    val allWorkers by viewModel.allWorkers.collectAsState()
    val activeProducts by viewModel.activeProducts.collectAsState()
    val activeProperties by viewModel.activeProperties.collectAsState()
    val activeRestaurants by viewModel.activeRestaurants.collectAsState()
    val seasonalJobs by viewModel.seasonalJobs.collectAsState()
    val isJobsLoading by viewModel.isJobsLoading.collectAsState()
    val isWorkersLoading by viewModel.isWorkersLoading.collectAsState()
    val isMarketplaceLoading by viewModel.isMarketplaceLoading.collectAsState()
    val platformSettings by viewModel.platformSettings.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var showVoiceDialog by remember { mutableStateOf(false) }

    // Android System Speech Recognizer Intent Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                searchInput = spokenText
                viewModel.searchQuery.value = spokenText
                Toast.makeText(context, "Voice query: \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onStartVoiceSearch = {
        showVoiceDialog = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(KashmirBackground),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Hero Banner with Dal Lake / Valley Graphic
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_kashmir_hero),
                    contentDescription = "Kashmir Valley",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Kashmir Deep Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    KashmirDeepGreen.copy(alpha = 0.75f),
                                    KashmirDeepGreen.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Surface(
                        color = KashmirGold,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = " KASHMIR EMPLOYMENT & MARKETPLACE ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = KashmirDeepGreen,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "Rozgar Kashmir",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Connecting Every Legitimate Worker & Opportunity Across 10 Districts",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = KashmirMint
                        ),
                        maxLines = 2
                    )
                }
            }
        }

        // 2. Global Universal Search Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = KashmirEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = {
                            searchInput = it
                            viewModel.searchQuery.value = it
                        },
                        placeholder = {
                            Text(
                                "Search job, worker, skill or product...",
                                fontSize = 13.sp,
                                color = KashmirTextMuted
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_search_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                    if (searchInput.isNotBlank()) {
                        IconButton(onClick = {
                            searchInput = ""
                            viewModel.searchQuery.value = ""
                        }) {
                            Icon(Icons.Default.Clear, "Clear", tint = KashmirTextMuted)
                        }
                    }

                    // Voice-to-Text Search Button
                    FilledIconButton(
                        onClick = onStartVoiceSearch,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = KashmirMint,
                            contentColor = KashmirDeepGreen
                        ),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("home_voice_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice to Text Search",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Live Admin Announcement Banner
        if (platformSettings?.announcementBannerActive == true && !platformSettings?.announcementBanner.isNullOrBlank()) {
            item {
                Surface(
                    color = KashmirGold.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, KashmirGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Campaign, null, tint = KashmirDeepGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = platformSettings?.announcementBanner ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KashmirDeepGreen,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Voice-First Phone Auth Call-To-Action Banner (if guest)
        if (currentUser == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.navigateTo(Screen.AUTH) }
                        .testTag("home_phone_auth_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KashmirGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneIphone,
                                contentDescription = null,
                                tint = KashmirDeepGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Voice Phone Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = KashmirEmerald,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "FIREBASE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Speak or enter your Kashmir mobile number for fast SMS OTP sign in or register.",
                                fontSize = 11.sp,
                                color = KashmirMint,
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Sign in",
                            tint = KashmirGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        } else {
            item {
                val firestoreProfile by viewModel.firestoreUserProfile.collectAsState()
                val isSyncing by viewModel.isSyncingProfileWithFirestore.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { viewModel.navigateTo(Screen.MY_PROFILE) }
                        .testTag("home_firestore_profile_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KashmirSurfaceGreen),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KashmirEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Cloud Profile (Firestore)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = KashmirDeepGreen
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isSyncing) KashmirWarmGold else KashmirEmerald,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isSyncing) "SYNCING" else "FIRESTORE ACTIVE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            val roleText = if (firestoreProfile?.userRole == "EMPLOYER") "Employer" else "Worker / Artisan"
                            val skillsCount = firestoreProfile?.skills?.size ?: 0
                            val historyCount = firestoreProfile?.serviceHistory?.size ?: 0
                            Text(
                                text = "$roleText • $skillsCount skills • $historyCount service history records",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open profile",
                            tint = KashmirEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // 3. Primary Five Main Action Buttons (Find Work, Find Workers, Post Job, Buy & Sell, Profile)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Quick Services & Navigation",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = KashmirDeepGreen
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Row 1: Jobs & Careers & Hire Workers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionCard(
                        title = "Jobs & Careers",
                        subtitle = "Find Active Work",
                        icon = Icons.Default.Work,
                        backgroundColor = KashmirDeepGreen,
                        accentColor = KashmirGold,
                        modifier = Modifier.weight(1f),
                        tag = "action_find_work"
                    ) {
                        viewModel.navigateTo(Screen.FIND_JOBS)
                    }

                    HomeActionCard(
                        title = "Hire Workers",
                        subtitle = "Skilled Artisans & Pros",
                        icon = Icons.Default.Engineering,
                        backgroundColor = KashmirForestGreen,
                        accentColor = KashmirMint,
                        modifier = Modifier.weight(1f),
                        tag = "action_find_workers"
                    ) {
                        viewModel.navigateTo(Screen.FIND_WORKERS)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Shop & Marketplace & Food & Wazwan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionCard(
                        title = "Shop & Market",
                        subtitle = "Handicrafts & Goods",
                        icon = Icons.Default.Storefront,
                        backgroundColor = KashmirEmerald,
                        accentColor = KashmirGold,
                        modifier = Modifier.weight(1f),
                        tag = "action_buy_sell"
                    ) {
                        viewModel.navigateTo(Screen.MARKETPLACE)
                    }

                    HomeActionCard(
                        title = "Food & Wazwan",
                        subtitle = "Kitchens & Wazwan",
                        icon = Icons.Default.Restaurant,
                        backgroundColor = KashmirSaffron,
                        accentColor = KashmirDeepGreen,
                        modifier = Modifier.weight(1f),
                        tag = "action_food_restaurants"
                    ) {
                        viewModel.navigateTo(Screen.FOOD)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Property & Rent & Kashmir Seasonal Hub
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionCard(
                        title = "Property & Rent",
                        subtitle = "Flats, Houses, Shops",
                        icon = Icons.Default.HomeWork,
                        backgroundColor = KashmirWarmGold,
                        accentColor = KashmirDeepGreen,
                        modifier = Modifier.weight(1f),
                        tag = "action_property_rent"
                    ) {
                        viewModel.navigateTo(Screen.PROPERTY)
                    }

                    HomeActionCard(
                        title = "Seasonal Hub",
                        subtitle = "Apple Picking & Snow",
                        icon = Icons.Default.Agriculture,
                        backgroundColor = KashmirDeepGreen,
                        accentColor = KashmirGold,
                        modifier = Modifier.weight(1f),
                        tag = "action_seasonal_work"
                    ) {
                        viewModel.navigateTo(Screen.SEASONAL_WORK)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 4: Post Job / Listing Quick Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.POST_JOB) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_post_job"),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(18.dp), tint = KashmirGold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post Job / Vacancy", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.navigateTo(Screen.MY_PROFILE) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("action_my_profile_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                        border = BorderStroke(1.5.dp, KashmirEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = KashmirEmerald)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("My Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3 / Card 5: Profile - Premium White Card with Deep Green & Gold Accents
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, KashmirGold.copy(alpha = 0.7f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.MY_PROFILE) }
                        .testTag("action_profile")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(KashmirDeepGreen)
                                    .border(1.dp, KashmirGold, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = KashmirGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Profile",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KashmirDeepGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = KashmirGold.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "WORKER CARD & ACCOUNT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirDeepGreen,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Manage your skills card, applications & listings",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Open Profile",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Card 6: Seller Dashboard - Multi-Vendor Marketplace & Firestore Inventory
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KashmirSurfaceGreen),
                    border = BorderStroke(1.5.dp, KashmirEmerald.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(Screen.SELLER_DASHBOARD) }
                        .testTag("action_seller_dashboard")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(KashmirEmerald),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = "Seller Dashboard",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Seller & Shop Dashboard",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = KashmirDeepGreen
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = KashmirEmerald,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "FIRESTORE CLOUD",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Add products, track live stock, manage orders from Pending to Delivered",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForwardIos,
                            contentDescription = "Open Dashboard",
                            tint = KashmirEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Dedicated Kashmiri Literature & Bookstore Spotlight
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.KASHMIRI_LITERATURE) }
                    .testTag("banner_kashmiri_literature"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KashmirForestGreen),
                border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KashmirGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Kashmiri Literature & Books",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "کٲشِر زَبانہِ ہِنٛز خِدمَت • Kashmiri Literature",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "Publish writings, poetry & stories. Discover & buy Kashmiri books in digital marketplace.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KashmirMint
                            ),
                            maxLines = 2
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = "Open Literature",
                        tint = KashmirGold,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 4. Dedicated Seasonal Work Spotlight (Apple Picking, Fruit Picking, Orchard Work, Farm Work, Tourism, Snow Removal)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.SEASONAL_WORK) }
                    .testTag("banner_seasonal_work"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(KashmirGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = "Seasonal Work",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KASHMIR SEASONAL HUB",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = KashmirSaffron,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE NOW",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Apple Picking, Orchard Work, Packing, Tourism & Snow Removal",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KashmirMint,
                                fontSize = 12.sp
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = Color.White
                    )
                }
            }
        }

        // 4b. Dedicated Property & Food Modules
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Property & Rentals
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.PROPERTY) }
                        .testTag("banner_property"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KashmirCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HomeWork, null, tint = KashmirDeepGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Property & Rent", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                            Text("Flats, Rooms, Shops", fontSize = 10.sp, color = KashmirTextSecondary)
                        }
                    }
                }

                // Food Orders & Wazwan
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.navigateTo(Screen.FOOD) }
                        .testTag("banner_food"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, KashmirCardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = KashmirMint,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Restaurant, null, tint = KashmirForestGreen, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Wazwan & Food", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                            Text("1% fee • Kitchens", fontSize = 10.sp, color = KashmirTextSecondary)
                        }
                    }
                }
            }
        }

        // 5. Districts of Kashmir Quick Filter
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Districts of Kashmir",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                    )
                    Text(
                        text = "All 10 Covered",
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(KASHMIR_DISTRICTS) { district ->
                        val isSelected = viewModel.selectedDistrict.collectAsState().value == district
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectedDistrict.value = if (isSelected) "All Districts" else district
                                viewModel.navigateTo(Screen.FIND_JOBS)
                            },
                            label = { Text(district, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSelected) KashmirDeepGreen else KashmirTextMuted
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirMint,
                                selectedLabelColor = KashmirDeepGreen
                            )
                        )
                    }
                }
            }
        }

        // 6. Popular Kashmir Professions
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular Professions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = KashmirEmerald,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { viewModel.navigateTo(Screen.FIND_WORKERS) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(KASHMIR_PROFESSIONS.take(8)) { prof ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, KashmirCardBorder),
                            modifier = Modifier.clickable {
                                viewModel.selectedProfession.value = prof
                                viewModel.navigateTo(Screen.FIND_WORKERS)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.WorkOutline,
                                    null,
                                    tint = KashmirEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = prof,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = KashmirTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 7. Latest Kashmir Jobs (Preview)
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Latest Jobs",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirTextPrimary
                    )
                )
                Text(
                    text = "See All (${activeJobs.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KashmirEmerald,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.FIND_JOBS) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isJobsLoading || activeJobs.isEmpty()) {
            items(2) {
                JobCardSkeleton()
            }
        } else {
            items(activeJobs.take(3)) { job ->
                HomeJobCard(job = job, onClick = { viewModel.viewJobDetail(job) })
            }
        }

        // 8. Featured Kashmiri Workers (Preview)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skilled Kashmiri Workers",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirTextPrimary
                    )
                )
                Text(
                    text = "Explore (${allWorkers.size})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KashmirEmerald,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.FIND_WORKERS) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isWorkersLoading || allWorkers.isEmpty()) {
            items(2) {
                WorkerCardSkeleton()
            }
        } else {
            items(allWorkers.take(3)) { worker ->
                HomeWorkerCard(worker = worker, onClick = { viewModel.viewWorkerDetail(worker) })
            }
        }

        // 9. Marketplace Highlights (Handicrafts & Second-hand)
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Marketplace Listings",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KashmirTextPrimary
                    )
                )
                Text(
                    text = "Visit Market",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KashmirEmerald,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.MARKETPLACE) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (isMarketplaceLoading || activeProducts.isEmpty()) {
            items(2) {
                ProductCardSkeleton()
            }
        } else {
            items(activeProducts.take(3)) { product ->
                HomeProductCard(product = product, onClick = { viewModel.viewProductDetail(product) })
            }
        }

        // 9b. Live Wazwan & Kashmiri Kitchens Preview
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Wazwan & Kitchen Orders",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                    )
                    Text(
                        text = "Authentic Dining • 1% Fair Platform Fee",
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald)
                    )
                }
                Text(
                    text = "See All Foods",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KashmirEmerald,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.FOOD) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val restaurantItems = if (activeRestaurants.isNotEmpty()) activeRestaurants else emptyList()
                items(restaurantItems.take(5)) { rest ->
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { viewModel.navigateTo(Screen.FOOD) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rest.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = KashmirDeepGreen,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = KashmirGold, modifier = Modifier.size(13.dp))
                                    Text(
                                        "${rest.rating}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = rest.cuisineType,
                                fontSize = 11.sp,
                                color = KashmirEmerald,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${rest.area}, ${rest.district}",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.navigateTo(Screen.FOOD) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.RestaurantMenu, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Menu & Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 9c. Live Property & Rentals Preview
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Properties & Rentals in Kashmir",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                    )
                    Text(
                        text = "Flats, Houses, Shops & Land (${activeProperties.size})",
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald)
                    )
                }
                Text(
                    text = "Explore All",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = KashmirEmerald,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(Screen.PROPERTY) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activeProperties.take(5)) { prop ->
                    Card(
                        modifier = Modifier
                            .width(230.dp)
                            .clickable { viewModel.navigateTo(Screen.PROPERTY) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KashmirBadge(
                                    text = prop.listingType.uppercase(),
                                    containerColor = if (prop.listingType.equals("Rent", ignoreCase = true)) KashmirMint else KashmirGold.copy(alpha = 0.25f),
                                    contentColor = if (prop.listingType.equals("Rent", ignoreCase = true)) KashmirDeepGreen else KashmirDeepGreen
                                )
                                Text(
                                    text = if (prop.listingType.equals("Rent", ignoreCase = true)) "₹${prop.rentOrPrice.toInt()}/mo" else "₹${prop.rentOrPrice.toInt()}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = prop.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = KashmirTextPrimary,
                                maxLines = 1
                            )
                            Text(
                                text = "${prop.propertyType} • ${prop.town}, ${prop.district}",
                                fontSize = 11.sp,
                                color = KashmirTextSecondary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { viewModel.navigateTo(Screen.PROPERTY) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirDeepGreen),
                                border = BorderStroke(1.dp, KashmirEmerald),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.HomeWork, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View Property", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 10. Support & Donation Callout
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.DONATE) }
                    .testTag("home_donate_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KashmirMint)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KashmirEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Support",
                            tint = KashmirGold,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Support Rozgar Kashmir",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = "Help maintain this free platform for workers and businesses across J&K.",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = KashmirDeepGreen)
                }
            }
        }

        // 11. Platform Governance & Legal Policies Direct Link
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { viewModel.navigateTo(Screen.POLICIES) }
                    .testTag("home_policies_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = "Legal Policies",
                        tint = KashmirDeepGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Policy, Terms & Legal Disclosures",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirTextPrimary,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = "Review zero-leakage data protections, COD guarantees, and dispute policies.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KashmirTextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = KashmirTextSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // Interactive Voice-to-Text Search Dialog
    if (showVoiceDialog) {
        VoiceInteractionDialog(
            viewModel = viewModel,
            onDismiss = { showVoiceDialog = false }
        )
    }
}

@Composable
fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(86.dp)
            .clickable { onClick() }
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun HomeJobCard(job: JobEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("home_job_item_${job.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.employerName,
                        style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                    )
                }
                Surface(
                    color = KashmirMint,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${job.paymentAmount} / ${job.paymentPeriod}",
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${job.area}, ${job.district}",
                    style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.People, null, tint = KashmirEmerald, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "${job.workersNeeded} workers needed",
                    style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                )
                if (job.isSeasonal) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(color = KashmirGold.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "SEASONAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeWorkerCard(worker: WorkerProfileEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("home_worker_item_${worker.id}"),
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
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(KashmirEmerald),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = worker.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = KashmirEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = worker.profession,
                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirEmerald, fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${worker.area}, ${worker.district} • ${worker.expectedPayment}",
                    style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                )
            }
            Surface(
                color = KashmirSurfaceGreen,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = KashmirWarmGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${worker.rating}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KashmirDeepGreen
                    )
                }
            }
        }
    }
}

@Composable
fun HomeProductCard(product: ProductEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() }
            .testTag("home_product_item_${product.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.imageUri.isNotBlank()) {
                RozgarAsyncImage(
                    model = product.imageUri,
                    contentDescription = product.title,
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    defaultPlaceholderIcon = if (product.isSecondHand) Icons.Default.Autorenew else Icons.Default.ShoppingBag
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KashmirSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (product.isSecondHand) Icons.Default.Autorenew else Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = KashmirEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (product.isSecondHand) KashmirAmber.copy(alpha = 0.2f) else KashmirMint,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (product.isSecondHand) "SECOND-HAND (${product.condition})" else product.condition,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (product.isSecondHand) KashmirSaffron else KashmirDeepGreen,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = product.district,
                        style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                    )
                }
            }
            Text(
                text = "₹${product.price.toInt()}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = KashmirDeepGreen
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSearchModal(
    onDismiss: () -> Unit,
    onQuerySelected: (String) -> Unit
) {
    var manualVoiceQuery by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Search in Kashmir",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = KashmirDeepGreen
                )
            )
            Text(
                text = "Search jobs, local artisans, or marketplace items by voice",
                style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Pulsing Mic Visual
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(KashmirMint),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KashmirDeepGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = KashmirGold,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KashmirEmerald)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Listening... Speak or tap a search prompt",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = KashmirEmerald
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Popular Voice Queries in Kashmir
            Text(
                text = "Popular Voice Searches in Kashmir:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = KashmirTextMuted
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val sampleQueries = listOf(
                "Apple picking jobs in Shopian",
                "Walnut wood carvers in Srinagar",
                "Sozni embroidery artisans in Budgam",
                "Second-hand inverter with battery",
                "General physician in Anantnag",
                "Authentic Kashmiri Pashmina Shawl",
                "Senior electrician in Pulwama",
                "Plumber in Baramulla"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleQueries.take(4)) { query ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KashmirSurfaceGreen,
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        modifier = Modifier.clickable { onQuerySelected(query) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VolumeUp, null, tint = KashmirDeepGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = query,
                                fontSize = 12.sp,
                                color = KashmirDeepGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sampleQueries.drop(4)) { query ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KashmirSurfaceGreen,
                        border = BorderStroke(1.dp, KashmirCardBorder),
                        modifier = Modifier.clickable { onQuerySelected(query) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VolumeUp, null, tint = KashmirDeepGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = query,
                                fontSize = 12.sp,
                                color = KashmirDeepGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Or enter / edit transcribed query
            OutlinedTextField(
                value = manualVoiceQuery,
                onValueChange = { manualVoiceQuery = it },
                label = { Text("Transcribed Text") },
                placeholder = { Text("Say or type what you're looking for...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (manualVoiceQuery.isNotBlank()) {
                        IconButton(onClick = { onQuerySelected(manualVoiceQuery) }) {
                            Icon(Icons.Default.Search, "Search", tint = KashmirDeepGreen)
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = KashmirTextSecondary)
                }
                if (manualVoiceQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onQuerySelected(manualVoiceQuery) },
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Search", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

