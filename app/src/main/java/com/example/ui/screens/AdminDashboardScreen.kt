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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab(val label: String) {
    OVERVIEW("Overview"),
    SYSTEM_HEALTH("System Health & Readiness ✅"),
    LIVE_CONTENT("Live Content & Demo Manager"),
    COMMISSIONS("Commissions & Rates"),
    LEDGER("Central Ledger & Payouts"),
    ORDERS("Fulfillment & Orders"),
    RESTAURANTS("Restaurants & Kitchens"),
    SHOPS("Shops & Merchants"),
    PROPERTIES("Properties & Rentals"),
    VERIFICATIONS("Verifications (₹49)"),
    MEMBERSHIPS("Store Memberships"),
    APPLICATIONS("Job Applications"),
    LITERATURE("Kashmiri Literature"),
    USERS("User Mgmt (Phones)"),
    LOGIN_ACTIVITY("Login Activity"),
    JOBS("Jobs Moderation"),
    PRODUCTS("Products Moderation"),
    REPORTS("Reports & Safety"),
    AUDIT_LOGS("Admin Audit Logs"),
    DONATIONS("Donations")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: RozgarViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allJobs by viewModel.allJobs.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allReports by viewModel.allReports.collectAsState()
    val allDonations by viewModel.allDonations.collectAsState()
    val loginActivities by viewModel.loginActivities.collectAsState()
    val verificationRequests by viewModel.verificationRequests.collectAsState()
    val kashmiriContributions by viewModel.kashmiriContributions.collectAsState()
    val reportedReviews by viewModel.reportedReviews.collectAsState()
    val platformSettings by viewModel.platformSettings.collectAsState()
    val centralLedgerEntries by viewModel.centralLedgerEntries.collectAsState()
    val allPayoutRequests by viewModel.allPayoutRequests.collectAsState()
    val totalCommissionEarned by viewModel.totalCommissionEarned.collectAsState()
    val allShops by viewModel.allShops.collectAsState()
    val allRestaurants by viewModel.activeRestaurants.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allProperties by viewModel.activeProperties.collectAsState()
    val allApplications by viewModel.allApplications.collectAsState()
    val allMemberships by viewModel.allMemberships.collectAsState()
    val adminAuditLogs by viewModel.adminAuditLogs.collectAsState()

    var activeAdminTab by remember { mutableStateOf(AdminTab.OVERVIEW) }
    var userSearchQuery by remember { mutableStateOf("") }

    // Security Gate check: Restricted strictly to authorized administrator email/credentials
    val authorizedAdminEmails = listOf("bilaltantraypersonal@gmail.com", "admin@rozgarkashmir.in")
    val isAdmin = (currentUser?.roles?.contains("ADMIN") == true) ||
        (currentUser?.email in authorizedAdminEmails || currentUser?.phoneNumber in listOf("9906000000", "9419099999"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Rozgar Kashmir Admin", fontWeight = FontWeight.Bold)
                        Text(
                            "Secure Platform Administration & Telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirMint)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        if (!isAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Security, null, tint = KashmirError, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Access Restricted", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    "You must be logged in as an authorized Rozgar Kashmir Administrator (${authorizedAdminEmails.first()}) to access the administrative control portal.",
                    color = KashmirTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Button(
                    onClick = { viewModel.navigateTo(com.example.viewmodel.Screen.LOGIN) },
                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Admin Login (Email / Number / Username)")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Horizontal Admin Tab Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    items(AdminTab.entries) { tab ->
                        FilterChip(
                            selected = activeAdminTab == tab,
                            onClick = { activeAdminTab = tab },
                            label = { Text(tab.label, fontWeight = if (activeAdminTab == tab) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KashmirDeepGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            when (activeAdminTab) {
                AdminTab.OVERVIEW -> {
                    // TAB 0: DASHBOARD STATISTICS (as specified by user prompt)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Platform Statistics & Key Metrics",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                if (allUsers.isEmpty()) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StatCardSkeleton(Modifier.weight(1f))
                                        StatCardSkeleton(Modifier.weight(1f))
                                        StatCardSkeleton(Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        StatCardSkeleton(Modifier.weight(1f))
                                        StatCardSkeleton(Modifier.weight(1f))
                                        StatCardSkeleton(Modifier.weight(1f))
                                    }
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("Total Users", "${allUsers.size}", Modifier.weight(1f))
                                        AdminStatBox("Workers", "${allUsers.count { it.roles.contains("WORKER") }}", Modifier.weight(1f))
                                        AdminStatBox("Employers", "${allUsers.count { it.roles.contains("EMPLOYER") }}", Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("Active Jobs", "${allJobs.count { it.status == "ACTIVE" }}", Modifier.weight(1f))
                                        AdminStatBox("Total Jobs", "${allJobs.size}", Modifier.weight(1f))
                                        AdminStatBox("Products", "${allProducts.size}", Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("₹49 Verification", "${verificationRequests.size}", Modifier.weight(1f))
                                        AdminStatBox("Kashmiri Literature", "${kashmiriContributions.size}", Modifier.weight(1f))
                                        AdminStatBox("Reports", "${allReports.count { it.status == "PENDING" }}", Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("Donations", "₹${allDonations.sumOf { it.amount }.toInt()}", Modifier.weight(1f))
                                        AdminStatBox("Flagged Reviews", "${reportedReviews.size}", Modifier.weight(1f))
                                        AdminStatBox("Login Events", "${loginActivities.size}", Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("Orders", "${allOrders.size}", Modifier.weight(1f))
                                        AdminStatBox("Restaurants", "${allRestaurants.size}", Modifier.weight(1f))
                                        AdminStatBox("Shops", "${allShops.size}", Modifier.weight(1f))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AdminStatBox("Properties", "${allProperties.size}", Modifier.weight(1f))
                                        AdminStatBox("Commission", "₹${totalCommissionEarned?.toInt() ?: 0}", Modifier.weight(1f))
                                        AdminStatBox("Payouts", "${allPayoutRequests.size}", Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.SYSTEM_HEALTH -> {
                    item {
                        AdminSystemHealthContent(viewModel = viewModel)
                    }
                }

                AdminTab.LIVE_CONTENT -> {
                    item {
                        AdminLiveContentContent(viewModel = viewModel, currentSettings = platformSettings)
                    }
                }

                AdminTab.COMMISSIONS -> {
                    // COMMISSION & BUSINESS SETTINGS
                    adminCommissionSettingsTab(viewModel = viewModel, settings = platformSettings)
                }

                AdminTab.LEDGER -> {
                    // CENTRAL TRANSACTION & SETTLEMENT LEDGER
                    adminCentralLedgerTab(
                        viewModel = viewModel,
                        ledgerEntries = centralLedgerEntries,
                        payoutRequests = allPayoutRequests,
                        totalCommission = totalCommissionEarned ?: 0.0
                    )
                }

                AdminTab.ORDERS -> {
                    // ALL MARKETPLACE & FOOD ORDERS
                    adminOrdersTab(
                        viewModel = viewModel,
                        orders = allOrders
                    )
                }

                AdminTab.RESTAURANTS -> {
                    // RESTAURANTS & KITCHENS MODERATION
                    adminRestaurantsTab(
                        viewModel = viewModel,
                        restaurants = allRestaurants
                    )
                }

                AdminTab.SHOPS -> {
                    // SHOPS & MERCHANTS MODERATION
                    adminShopsTab(
                        viewModel = viewModel,
                        shops = allShops
                    )
                }

                AdminTab.PROPERTIES -> {
                    // PROPERTY LISTINGS MODERATION
                    adminPropertiesTab(
                        viewModel = viewModel,
                        properties = allProperties
                    )
                }

                AdminTab.VERIFICATIONS -> {
                    // VERIFICATIONS (₹49 PAID BLUE BADGE WITH UPI VERIFICATION)
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VerifiedBadge(size = 20)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "₹49 Blue Badge Verification Requests: Verify UPI transaction reference (UTR) and ID proof before activating the official verified badge.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    if (verificationRequests.isEmpty()) {
                        item {
                            Text("No verification requests pending.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(verificationRequests) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, if (req.status == "PENDING_VERIFICATION") KashmirGold else KashmirCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            VerifiedBadge(size = 18)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(req.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        KashmirBadge(
                                            text = req.status,
                                            containerColor = if (req.status == "APPROVED") KashmirMint else KashmirGold.copy(alpha = 0.3f),
                                            contentColor = KashmirDeepGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Registered Phone: ${req.userPhone} (Admin Only)", fontSize = 12.sp, color = KashmirTextSecondary)
                                    Text("Verification Fee: ₹${req.feeAmount.toInt()} via UPI", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirDeepGreen)
                                    Text("UPI Reference / UTR: ${req.upiReferenceId}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirForestGreen)
                                    Text("ID Proof: ${req.idProofType} ${if (req.idProofNumber.isNotBlank()) "(${req.idProofNumber})" else ""}", fontSize = 11.sp, color = KashmirTextMuted)
                                    if (req.notes.isNotBlank()) {
                                        Text("User Notes: ${req.notes}", fontSize = 11.sp, color = KashmirTextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (req.status != "APPROVED") {
                                            Button(
                                                onClick = { viewModel.adminApproveVerification(req.id, req.userId, "Approved by admin via verified UPI UTR ${req.upiReferenceId}") },
                                                colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.Verified, null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Activate Blue Badge", fontSize = 11.sp)
                                            }
                                        }
                                        if (req.status != "REJECTED") {
                                            OutlinedButton(
                                                onClick = { viewModel.adminRejectVerification(req.id, "Invalid UPI transaction reference") },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Reject", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.MEMBERSHIPS -> {
                    // STORE & RESTAURANT PAID MEMBERSHIPS (₹49 / ₹99)
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CardMembership, null, tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Store & Restaurant Paid Memberships: Audit active subscriptions, payment refs, and membership validity across J&K merchants.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    if (allMemberships.isEmpty()) {
                        item {
                            Text("No store membership subscriptions recorded yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(allMemberships) { sub ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KashmirCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(sub.shopName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        KashmirBadge(
                                            text = sub.status,
                                            containerColor = if (sub.status == "ACTIVE") KashmirMint else KashmirGold.copy(alpha = 0.3f),
                                            contentColor = KashmirDeepGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Merchant: ${sub.userName} • Plan: ${sub.planType}", fontSize = 12.sp, color = KashmirTextSecondary)
                                    Text("Amount: ₹${sub.amountPaid.toInt()} via ${sub.paymentMethod} • Ref: ${sub.paymentRef}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KashmirDeepGreen)
                                    Text(
                                        "Valid until: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(sub.endDate))}",
                                        fontSize = 11.sp,
                                        color = KashmirTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                AdminTab.APPLICATIONS -> {
                    // ALL JOB APPLICATIONS OVERSIGHT
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AssignmentInd, null, tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Central Job Applications: Monitor employment matches, worker resumes, and hiring stages across all 20 Jammu & Kashmir districts.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    if (allApplications.isEmpty()) {
                        item {
                            Text("No job applications submitted yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(allApplications) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KashmirCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(app.applicantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Job: ${app.jobTitle} • Profession: ${app.applicantProfession}", fontSize = 12.sp, color = KashmirDeepGreen)
                                            Text("District: ${app.applicantDistrict} • Expected: ${app.expectedPay}", fontSize = 11.sp, color = KashmirTextSecondary)
                                        }
                                        KashmirBadge(text = app.status)
                                    }
                                    if (app.coverNote.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("\"${app.coverNote}\"", fontSize = 11.sp, color = KashmirTextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (app.status != "ACCEPTED") {
                                            Button(
                                                onClick = {
                                                    viewModel.updateApplicationStatus(app.id, "ACCEPTED", app.applicantUserId, app.jobTitle)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Text("Approve / Hire", fontSize = 10.sp)
                                            }
                                        }
                                        if (app.status != "REJECTED") {
                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.updateApplicationStatus(app.id, "REJECTED", app.applicantUserId, app.jobTitle)
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                            ) {
                                                Text("Reject", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.LITERATURE -> {
                    // KASHMIRI LITERATURE & BOOK REVIEW
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoStories, null, tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kashmiri Language & Book Review: Review published writings, poetry, articles, and book marketplace submissions.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    if (kashmiriContributions.isEmpty()) {
                        item {
                            Text("No Kashmiri contributions submitted yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(kashmiriContributions) { contrib ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KashmirCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(contrib.titleKashmiri, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = KashmirDeepGreen)
                                            if (contrib.titleEnglish.isNotBlank()) {
                                                Text(contrib.titleEnglish, fontSize = 12.sp, color = KashmirTextSecondary)
                                            }
                                        }
                                        KashmirBadge(text = contrib.type)
                                    }
                                    Text("Author: ${contrib.authorName} • Genre: ${contrib.genre}", fontSize = 11.sp, color = KashmirTextMuted)
                                    if (contrib.isBook) {
                                        Text("Book Price: ${if (contrib.isPaid) "₹${contrib.price.toInt()}" else "Free Access"} • Pages: ${contrib.pageCount}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KashmirEmerald)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(contrib.content.take(120) + if (contrib.content.length > 120) "..." else "", fontSize = 11.sp, color = KashmirTextSecondary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.adminModerateContribution(contrib.id, "APPROVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Approve", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminModerateContribution(contrib.id, "FEATURED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Feature", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminModerateContribution(contrib.id, "REJECTED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.USERS -> {
                    // USER MANAGEMENT WITH REGISTERED PHONE NUMBERS (ADMIN-ONLY ACCESS)
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Security, null, tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Authorized Admin View: Registered phone numbers are strictly visible here only. User passwords are encrypted SHA-256 hashes and never exposed.",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }

                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search by name, district, or phone...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    val filteredUsers = allUsers.filter { u ->
                        userSearchQuery.isBlank() ||
                            u.fullName.contains(userSearchQuery, ignoreCase = true) ||
                            u.phoneNumber.contains(userSearchQuery) ||
                            u.district.contains(userSearchQuery, ignoreCase = true)
                    }

                    items(filteredUsers) { userItem ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(userItem.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        // PRIVATE PHONE NUMBER SHOWN ONLY TO AUTHORIZED ADMIN HERE
                                        Text(
                                            text = "Phone (Admin View): ${userItem.phoneNumber}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = KashmirDeepGreen
                                        )
                                        Text(
                                            text = "District: ${userItem.district} (${userItem.area}) • Roles: ${userItem.roles}",
                                            fontSize = 11.sp,
                                            color = KashmirTextSecondary
                                        )
                                    }

                                    Surface(
                                        color = if (userItem.isBanned) KashmirError.copy(alpha = 0.2f) else KashmirMint,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (userItem.isBanned) "BANNED" else "ACTIVE",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = if (userItem.isBanned) KashmirError else KashmirDeepGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            viewModel.adminUpdateUserStatus(userItem.id, banned = !userItem.isBanned, verified = userItem.isVerified)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (userItem.isBanned) KashmirEmerald else KashmirError
                                        ),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (userItem.isBanned) "Restore Account" else "Suspend / Ban", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.adminUpdateUserStatus(userItem.id, banned = userItem.isBanned, verified = !userItem.isVerified)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = KashmirWarmGold),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(if (userItem.isVerified) "Unverify" else "Verify Badge", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.LOGIN_ACTIVITY -> {
                    // LOGIN ACTIVITY LOGS (Only Admins can view phone numbers & activity)
                    item {
                        Text(
                            text = "Security & Login Activity Telemetry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Records user registration and authentication attempts across J&K",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(loginActivities) { activity ->
                        val dateStr = remember(activity.timestamp) {
                            SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(activity.timestamp))
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(activity.userName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Phone: ${activity.phoneNumber} • District: ${activity.district}", fontSize = 12.sp, color = KashmirDeepGreen)
                                    Text(dateStr, fontSize = 10.sp, color = KashmirTextMuted)
                                }
                                Surface(
                                    color = if (activity.activityType == "REGISTER") KashmirWarmGold.copy(alpha = 0.2f) else KashmirMint,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = activity.activityType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = if (activity.activityType == "REGISTER") KashmirWarmGold else KashmirDeepGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                AdminTab.JOBS -> {
                    // JOBS MODERATION
                    items(allJobs) { job ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(job.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    KashmirBadge(text = job.status)
                                }
                                Text("Employer: ${job.employerName} • ${job.area}, ${job.district}", fontSize = 12.sp, color = KashmirTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (job.status == "ACTIVE") {
                                        Button(
                                            onClick = { viewModel.adminModerateJob(job.id, "CLOSED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirTextMuted),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Close Job", fontSize = 11.sp)
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.adminModerateJob(job.id, "MODERATED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Remove / Hide", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.PRODUCTS -> {
                    // MARKETPLACE MODERATION
                    items(allProducts) { product ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(product.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text("₹${product.price.toInt()}", fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                                }
                                Text("Seller: ${product.sellerName} • Status: ${product.status}", fontSize = 12.sp, color = KashmirTextSecondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row {
                                    Button(
                                        onClick = { viewModel.adminModerateProduct(product.id, "MODERATED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Remove Product Listing", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.REPORTS -> {
                    // REPORTS & REVIEWS MANAGEMENT
                    if (reportedReviews.isNotEmpty()) {
                        item {
                            Text("Flagged Reviews (${reportedReviews.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirError)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        items(reportedReviews) { rev ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KashmirError.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Review by: ${rev.authorName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("★ ${rev.rating}/5", color = KashmirGold, fontWeight = FontWeight.Bold)
                                    }
                                    Text("Target: ${rev.targetType} - ${rev.targetTitle}", fontSize = 11.sp, color = KashmirEmerald)
                                    Text("\"${rev.reviewText}\"", fontSize = 12.sp, color = KashmirTextPrimary)
                                    Text("Report Reason: ${rev.reportReason}", fontSize = 11.sp, color = KashmirError, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.adminUpdateReviewStatus(rev.id, "ACTIVE") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Dismiss Report", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminUpdateReviewStatus(rev.id, "REMOVED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Remove Review", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("General Platform Reports", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = KashmirDeepGreen)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    if (allReports.isEmpty()) {
                        item {
                            Text("No reports submitted.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(allReports) { report ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${report.reportedType}: ${report.targetTitle}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            color = when (report.status) {
                                                "RESOLVED" -> KashmirMint
                                                "REJECTED" -> KashmirTextMuted.copy(alpha = 0.2f)
                                                else -> KashmirWarmGold.copy(alpha = 0.2f)
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                report.status,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text("Reason: ${report.reason} • By: ${report.reporterName}", fontSize = 12.sp, color = KashmirTextSecondary)
                                    if (report.details.isNotBlank()) {
                                        Text("Details: ${report.details}", fontSize = 12.sp, color = KashmirTextMuted)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.adminUpdateReport(report.id, "RESOLVED", "Action taken by admin") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Mark Resolved", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminUpdateReport(report.id, "REJECTED", "Dismissed after review") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirTextMuted),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.DONATIONS -> {
                    // DONATIONS & SUPPORT REVIEW
                    if (allDonations.isEmpty()) {
                        item {
                            Text("No donations submitted yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(allDonations) { donation ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("₹${donation.amount.toInt()} from ${donation.donorName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        KashmirBadge(text = donation.status)
                                    }
                                    Text("Ref ID: ${donation.referenceId}", fontSize = 12.sp, color = KashmirDeepGreen, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.adminUpdateDonation(donation.id, "VERIFIED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirEmerald),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Verify & Approve", fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = { viewModel.adminUpdateDonation(donation.id, "REJECTED") },
                                            colors = ButtonDefaults.buttonColors(containerColor = KashmirError),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Reject", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.AUDIT_LOGS -> {
                    // SYSTEM & ADMINISTRATIVE AUDIT TELEMETRY
                    item {
                        Surface(
                            color = KashmirSurfaceGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ManageHistory, null, tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Immutable Admin Audit Trail: Real-time telemetry tracking sensitive administrative actions (user suspensions, refunds, verifications, commission updates).",
                                    fontSize = 11.sp,
                                    color = KashmirDeepGreen
                                )
                            }
                        }
                    }

                    if (adminAuditLogs.isEmpty()) {
                        item {
                            Text("No audit log events recorded yet.", modifier = Modifier.padding(20.dp), color = KashmirTextSecondary)
                        }
                    } else {
                        items(adminAuditLogs) { log ->
                            val dateStr = remember(log.timestamp) {
                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, KashmirCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                                        Text(dateStr, fontSize = 10.sp, color = KashmirTextMuted)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("By Admin: ${log.adminName} (ID: ${log.adminUserId})", fontSize = 11.sp, color = KashmirTextSecondary)
                                    Text("Target: ${log.targetEntity} #${log.targetId}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = KashmirForestGreen)
                                    Text(log.details, fontSize = 11.sp, color = KashmirTextPrimary)
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
fun AdminStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        color = KashmirMint.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = KashmirDeepGreen)
            Text(label, fontSize = 10.sp, color = KashmirTextSecondary, maxLines = 1)
        }
    }
}
