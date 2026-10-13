package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(viewModel: RozgarViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    val upiId = "rozgarkashmir@upi"
    val upiDisplayName = "Rozgar Kashmir Support & Infrastructure"

    var donorName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var donationAmount by remember { mutableStateOf("500") }
    var referenceId by remember { mutableStateOf("") }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support Rozgar Kashmir", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = KashmirMint,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.VolunteerActivism,
                                    null,
                                    tint = KashmirDeepGreen,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Keep Rozgar Kashmir Free & Independent",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )

                        Text(
                            text = "100% of community contributions support server hosting, voice messaging bandwidth, and outreach for rural workers across all 10 districts of Jammu & Kashmir.",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Simulated UPI QR Code Graphic Card
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = KashmirSurfaceGreen,
                            border = BorderStroke(1.5.dp, KashmirCardBorder),
                            modifier = Modifier
                                .size(190.dp)
                                .padding(4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCode2,
                                    contentDescription = "UPI QR Code",
                                    tint = KashmirDeepGreen,
                                    modifier = Modifier.size(110.dp)
                                )
                                Text(
                                    text = "SCAN VIA ANY UPI APP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = KashmirDeepGreen,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // UPI ID Details Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = KashmirMint.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Official UPI ID", fontSize = 11.sp, color = KashmirTextSecondary)
                                    Text(
                                        text = upiId,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                    Text(
                                        text = upiDisplayName,
                                        fontSize = 11.sp,
                                        color = KashmirTextMuted
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", upiId))
                                        Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.testTag("copy_upi_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Copy UPI ID", tint = KashmirDeepGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Open UPI App Button
                        OutlinedButton(
                            onClick = {
                                val uri = Uri.parse("upi://pay?pa=$upiId&pn=${Uri.encode(upiDisplayName)}&cu=INR")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No UPI app found. Please copy the UPI ID.", Toast.LENGTH_LONG).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Any UPI App (GPay / PhonePe / Paytm)")
                        }
                    }
                }
            }

            // "I Have Made a Donation" Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Submit Donation Reference",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = "After transferring via your UPI app, submit the transaction ID below. Admin will review and verify your contribution.",
                            style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = donorName,
                            onValueChange = { donorName = it },
                            label = { Text("Donor Name (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = donationAmount,
                            onValueChange = { donationAmount = it },
                            label = { Text("Donated Amount (₹) *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = referenceId,
                            onValueChange = { referenceId = it },
                            label = { Text("UPI UTR / Reference ID *") },
                            placeholder = { Text("e.g. 428910482910") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amt = donationAmount.toDoubleOrNull() ?: 100.0
                                if (referenceId.isNotBlank()) {
                                    viewModel.submitDonation(amt, donorName, referenceId)
                                    showSuccessMessage = true
                                    referenceId = ""
                                }
                            },
                            enabled = referenceId.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_donation_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("I Have Made a Donation", fontWeight = FontWeight.Bold)
                        }

                        if (showSuccessMessage) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = KashmirMint,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Thank you! Your reference ID is submitted for administrator review (Status: Pending Verification).",
                                    fontSize = 12.sp,
                                    color = KashmirDeepGreen,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
