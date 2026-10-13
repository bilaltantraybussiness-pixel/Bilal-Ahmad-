package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.VerificationRequestEntity
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Int = 18,
    showText: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E88E5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Verified Profile",
                tint = Color.White,
                modifier = Modifier.size((size * 0.7f).dp)
            )
        }
        if (showText) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Verified",
                fontSize = (size * 0.65f).sp,
                color = Color(0xFF1E88E5),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationBadgeDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val userVerificationRequest by viewModel.currentUserVerificationRequest.collectAsState()

    var upiRefNumber by remember { mutableStateOf("") }
    var selectedIdType by remember { mutableStateOf(ID_PROOF_TYPES.first()) }
    var idProofNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var idDropdownExpanded by remember { mutableStateOf(false) }

    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var copiedUpi by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("verification_badge_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Blue Checkmark
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VerifiedBadge(size = 28)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Blue Verified Badge",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = KashmirTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Verification Status Banner
                if (currentUser?.isVerified == true) {
                    Surface(
                        color = KashmirMint,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            VerifiedBadge(size = 24)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Your Account is Officially Verified!", fontWeight = FontWeight.Bold, color = KashmirDeepGreen, fontSize = 13.sp)
                                Text("The Blue Verified Badge is active across all your postings and applications.", fontSize = 11.sp, color = KashmirTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else if (userVerificationRequest?.status == "PENDING_VERIFICATION") {
                    Surface(
                        color = KashmirGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, KashmirGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HourglassTop, null, tint = KashmirForestGreen, modifier = Modifier.size(26.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Verification Under Review", fontWeight = FontWeight.Bold, color = KashmirForestGreen, fontSize = 13.sp)
                                Text("Ref: ${userVerificationRequest?.upiReferenceId} (₹49 Fee). Admin is verifying transaction authenticity.", fontSize = 11.sp, color = KashmirTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Value Proposition Cards
                Surface(
                    color = KashmirBackground,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, KashmirCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Why Get Verified on Rozgar Kashmir?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = KashmirDeepGreen)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Blue Checkmark next to your name", fontSize = 12.sp, color = KashmirTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = KashmirEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3x Higher client inquiries and trust rating", fontSize = 12.sp, color = KashmirTextPrimary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, null, tint = KashmirForestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Anti-fraud validation and verified worker card", fontSize = 12.sp, color = KashmirTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Official UPI Payment Information Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = KashmirMint),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("One-Time Verification Fee:", fontSize = 12.sp, color = KashmirTextSecondary)
                            Text("₹49.00", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = KashmirDeepGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = KashmirEmerald.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Official Admin UPI ID:", fontSize = 11.sp, color = KashmirTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.dp, KashmirCardBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(OFFICIAL_UPI_ID, fontWeight = FontWeight.Bold, color = KashmirDeepGreen, fontSize = 13.sp)

                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", OFFICIAL_UPI_ID))
                                    copiedUpi = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (copiedUpi) "Copied!" else "Copy", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pay via GPay, PhonePe, Paytm, or BHIM to $OFFICIAL_UPI_ID then enter your 12-digit UTR/Reference number below.",
                            fontSize = 10.sp,
                            color = KashmirTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Form Fields
                Text(
                    text = "Submit Transaction Details for Verification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = KashmirTextPrimary,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 12-digit UTR Reference Input
                OutlinedTextField(
                    value = upiRefNumber,
                    onValueChange = { upiRefNumber = it },
                    label = { Text("12-Digit UPI Ref / UTR Number") },
                    placeholder = { Text("e.g. 428190345678") },
                    leadingIcon = { Icon(Icons.Default.Receipt, null, tint = KashmirEmerald) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_reference_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ID Proof Dropdown
                ExposedDropdownMenuBox(
                    expanded = idDropdownExpanded,
                    onExpandedChange = { idDropdownExpanded = !idDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedIdType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ID Document Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = idDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = idDropdownExpanded,
                        onDismissRequest = { idDropdownExpanded = false }
                    ) {
                        ID_PROOF_TYPES.forEach { idType ->
                            DropdownMenuItem(
                                text = { Text(idType) },
                                onClick = {
                                    selectedIdType = idType
                                    idDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ID Proof Number Input
                OutlinedTextField(
                    value = idProofNumber,
                    onValueChange = { idProofNumber = it },
                    label = { Text("ID Document Number (or last 4 digits)") },
                    placeholder = { Text("e.g. XXXX-XXXX-1234 or Artisan No.") },
                    leadingIcon = { Icon(Icons.Default.Badge, null, tint = KashmirEmerald) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("id_proof_number_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes / Remarks
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Shop or Workshop Name (Optional)") },
                    placeholder = { Text("Any detail to help expedite verification") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Error Message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", color = KashmirError, fontSize = 12.sp, textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Verification Button
                Button(
                    onClick = {
                        if (currentUser == null) {
                            errorMessage = "Please login before submitting verification request."
                            return@Button
                        }
                        if (upiRefNumber.trim().length < 6) {
                            errorMessage = "Please enter a valid UPI transaction reference / UTR number."
                            return@Button
                        }
                        if (idProofNumber.trim().isBlank()) {
                            errorMessage = "Please enter your ID document number."
                            return@Button
                        }

                        isSubmitting = true
                        errorMessage = null

                        viewModel.submitVerificationRequest(
                            upiRef = upiRefNumber.trim(),
                            idType = selectedIdType,
                            idNumber = idProofNumber.trim(),
                            notes = notes.trim()
                        ) { success, msg ->
                            isSubmitting = false
                            if (success) {
                                onDismiss()
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    enabled = !isSubmitting && currentUser?.isVerified != true,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_verification_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submit for Admin Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
