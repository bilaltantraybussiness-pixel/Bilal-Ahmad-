package com.example.ui.components

import android.Manifest
import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.LocalizationManager
import com.example.util.PhoneAuthHelper
import com.example.util.PhoneAuthState
import com.example.util.SpeechToTextManager
import com.example.viewmodel.RozgarViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay

enum class VoiceActiveTarget {
    NONE,
    PHONE_INPUT,
    OTP_INPUT,
    NAME_INPUT
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPermissionsApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun PhoneAuthVoiceComponent(
    viewModel: RozgarViewModel,
    modifier: Modifier = Modifier,
    onAuthSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val currentLang by LocalizationManager.currentLanguage.collectAsState()
    val phoneAuthState by viewModel.phoneAuthState.collectAsState()
    val phoneAuthNumber by viewModel.phoneAuthNumber.collectAsState()
    val isNewUser by viewModel.isNewPhoneUser.collectAsState()

    // Internal Form States
    var rawPhone by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regDistrict by remember { mutableStateOf(KASHMIR_DISTRICTS.first()) }
    var regArea by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }

    var roleWorker by remember { mutableStateOf(true) }
    var roleEmployer by remember { mutableStateOf(false) }
    var roleSeller by remember { mutableStateOf(false) }
    var roleBuyer by remember { mutableStateOf(true) }

    var localErrorMessage by remember { mutableStateOf("") }
    var localSuccessMessage by remember { mutableStateOf("") }

    // Resend countdown timer
    var resendCountdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    // Speech To Text State
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val speechManager = remember { SpeechToTextManager(context) }
    val isListening by speechManager.isListening.collectAsState()
    val recognizedText by speechManager.recognizedText.collectAsState()
    val partialText by speechManager.partialText.collectAsState()
    val rmsDb by speechManager.rmsDb.collectAsState()

    var activeVoiceTarget by remember { mutableStateOf(VoiceActiveTarget.NONE) }

    DisposableEffect(speechManager) {
        onDispose {
            speechManager.destroy()
        }
    }

    // Countdown tick effect when OTP is sent
    LaunchedEffect(phoneAuthState) {
        if (phoneAuthState is PhoneAuthState.CodeSent) {
            resendCountdown = 60
            canResend = false
            while (resendCountdown > 0) {
                delay(1000L)
                resendCountdown--
            }
            canResend = true
        }
    }

    // Voice recognition text processor
    LaunchedEffect(recognizedText) {
        if (recognizedText.isNotBlank()) {
            when (activeVoiceTarget) {
                VoiceActiveTarget.PHONE_INPUT -> {
                    val digits = PhoneAuthHelper.parseSpokenDigits(recognizedText)
                    if (digits.isNotEmpty()) {
                        rawPhone = digits.take(10)
                        localSuccessMessage = "Voice recognized: $digits"
                    }
                }
                VoiceActiveTarget.OTP_INPUT -> {
                    val digits = PhoneAuthHelper.parseSpokenDigits(recognizedText)
                    if (digits.isNotEmpty()) {
                        otpCode = digits.take(6)
                        localSuccessMessage = "Voice OTP: $digits"
                        if (digits.length >= 6) {
                            // Auto-verify if 6 digits received
                            viewModel.verifyPhoneOtpCode(
                                otpCode = digits.take(6),
                                onSuccess = { isNew, _ ->
                                    if (!isNew) {
                                        onAuthSuccess()
                                    }
                                },
                                onError = { localErrorMessage = it }
                            )
                        }
                    }
                }
                VoiceActiveTarget.NAME_INPUT -> {
                    val cleaned = recognizedText.trim().replaceFirstChar { it.uppercase() }
                    if (cleaned.isNotBlank()) {
                        regFullName = cleaned
                    }
                }
                VoiceActiveTarget.NONE -> {}
            }
            activeVoiceTarget = VoiceActiveTarget.NONE
        }
    }

    // Microphone Pulse Animation
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

    fun startVoiceInput(target: VoiceActiveTarget) {
        localErrorMessage = ""
        localSuccessMessage = ""
        if (!audioPermissionState.status.isGranted) {
            audioPermissionState.launchPermissionRequest()
            return
        }
        activeVoiceTarget = target
        val langCode = when (currentLang) {
            com.example.util.AppLanguage.URDU -> "ur-IN"
            com.example.util.AppLanguage.KASHMIRI -> "ur-IN"
            com.example.util.AppLanguage.ENGLISH -> "en-IN"
        }
        speechManager.startListening(langCode)
    }

    fun stopVoiceInput() {
        speechManager.stopListening()
        activeVoiceTarget = VoiceActiveTarget.NONE
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, KashmirCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with Firebase Auth & Voice Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(KashmirMint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = "Phone Auth",
                            tint = KashmirDeepGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Phone Number Authentication",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = "Firebase Auth + Voice-First Support",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirEmerald)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = KashmirMint,
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = KashmirForestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Voice Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = KashmirForestGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voice Listening Live Status Banner
            AnimatedVisibility(visible = isListening) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = KashmirEmerald.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(KashmirEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Listening",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Listening... Speak clearly in English, Urdu or Kashmiri",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = KashmirDeepGreen
                            )
                            if (partialText.isNotBlank()) {
                                Text(
                                    text = "\"$partialText\"",
                                    fontSize = 12.sp,
                                    color = KashmirTextPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        IconButton(
                            onClick = { stopVoiceInput() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Voice", tint = KashmirTextSecondary)
                        }
                    }
                }
            }

            // Error Message Display
            if (localErrorMessage.isNotBlank() || phoneAuthState is PhoneAuthState.Error) {
                val errorText = localErrorMessage.ifBlank {
                    (phoneAuthState as? PhoneAuthState.Error)?.message ?: ""
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = KashmirError.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, KashmirError.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = KashmirError, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorText, color = KashmirError, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            localErrorMessage = ""
                            viewModel.resetPhoneAuth()
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = KashmirError, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            // Success Feedback Display
            if (localSuccessMessage.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = KashmirMint,
                    border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = KashmirDeepGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localSuccessMessage, color = KashmirDeepGreen, fontSize = 12.sp)
                    }
                }
            }

            // Main Flow Switcher based on PhoneAuthState & isNewUser
            when {
                // STEP 3: NEW USER REGISTRATION PROFILE COMPLETION
                phoneAuthState is PhoneAuthState.Verified && isNewUser -> {
                    Text(
                        text = "Complete Your Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KashmirTextPrimary
                    )
                    Text(
                        text = "Your phone number $phoneAuthNumber is verified! Just add your name and roles to start.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name with Voice Mic button
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = { regFullName = it },
                        label = { Text("Full Name (نام / ناو)") },
                        placeholder = { Text("e.g. Farooq Ahmad Bhat") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = KashmirEmerald) },
                        trailingIcon = {
                            IconButton(onClick = { startVoiceInput(VoiceActiveTarget.NAME_INPUT) }) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Speak Name",
                                    tint = if (isListening && activeVoiceTarget == VoiceActiveTarget.NAME_INPUT) KashmirGold else KashmirEmerald
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // District Selector
                    var districtExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = districtExpanded,
                        onExpandedChange = { districtExpanded = !districtExpanded }
                    ) {
                        OutlinedTextField(
                            value = regDistrict,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("District in Jammu & Kashmir") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = districtExpanded,
                            onDismissRequest = { districtExpanded = false }
                        ) {
                            KASHMIR_DISTRICTS.forEach { district ->
                                DropdownMenuItem(
                                    text = { Text(district) },
                                    onClick = {
                                        regDistrict = district
                                        districtExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regArea,
                        onValueChange = { regArea = it },
                        label = { Text("Area / Tehsil / Village (Optional)") },
                        placeholder = { Text("e.g. Lal Chowk, Sopore, Tral") },
                        leadingIcon = { Icon(Icons.Default.Map, null, tint = KashmirEmerald) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "I want to use Rozgar Kashmir as:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = KashmirTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = roleWorker,
                            onClick = { roleWorker = !roleWorker },
                            label = { Text("Worker / Artisan (کاریگر)") },
                            leadingIcon = if (roleWorker) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                        )
                        FilterChip(
                            selected = roleEmployer,
                            onClick = { roleEmployer = !roleEmployer },
                            label = { Text("Employer / Hirer (مالک)") },
                            leadingIcon = if (roleEmployer) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                        )
                        FilterChip(
                            selected = roleSeller,
                            onClick = { roleSeller = !roleSeller },
                            label = { Text("Artisan Seller (دکاندار)") },
                            leadingIcon = if (roleSeller) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                        )
                        FilterChip(
                            selected = roleBuyer,
                            onClick = { roleBuyer = !roleBuyer },
                            label = { Text("Customer / Buyer") },
                            leadingIcon = if (roleBuyer) { { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) } } else null
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (regFullName.isBlank()) {
                                localErrorMessage = "Please enter or speak your name."
                                return@Button
                            }
                            val rolesList = mutableListOf<String>()
                            if (roleWorker) rolesList.add("WORKER")
                            if (roleEmployer) rolesList.add("EMPLOYER")
                            if (roleSeller) rolesList.add("SELLER")
                            if (roleBuyer) rolesList.add("BUYER")
                            if (rolesList.isEmpty()) rolesList.add("WORKER")

                            viewModel.completePhoneUserRegistration(
                                fullName = regFullName,
                                district = regDistrict,
                                area = regArea,
                                roles = rolesList.joinToString(","),
                                email = regEmail
                            ) { success, msg ->
                                if (success) {
                                    localSuccessMessage = msg
                                    onAuthSuccess()
                                } else {
                                    localErrorMessage = msg
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("complete_registration_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                // STEP 2: ENTER 6-DIGIT OTP
                phoneAuthState is PhoneAuthState.CodeSent || phoneAuthState is PhoneAuthState.VerifyingCode -> {
                    val codeSentState = phoneAuthState as? PhoneAuthState.CodeSent
                    val phoneSentTo = codeSentState?.phoneNumber ?: phoneAuthNumber

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enter 6-Digit OTP Code",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = KashmirTextPrimary
                            )
                            Text(
                                text = "Sent via SMS to $phoneSentTo",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                        }

                        TextButton(onClick = { viewModel.resetPhoneAuth() }) {
                            Text("Change Number", color = KashmirEmerald, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // OTP TextField with Voice Mic button
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = {
                            if (it.length <= 6) {
                                otpCode = it.filter { ch -> ch.isDigit() }
                                localErrorMessage = ""
                            }
                        },
                        label = { Text("6-Digit Code (کوڈ)") },
                        placeholder = { Text("e.g. 123456") },
                        leadingIcon = { Icon(Icons.Default.Key, null, tint = KashmirEmerald) },
                        trailingIcon = {
                            IconButton(onClick = { startVoiceInput(VoiceActiveTarget.OTP_INPUT) }) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Speak 6-digit OTP",
                                    tint = if (isListening && activeVoiceTarget == VoiceActiveTarget.OTP_INPUT) KashmirGold else KashmirEmerald
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_code_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice instruction / prompt helper
                    Surface(
                        color = KashmirMint.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.VolumeUp, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tip: Tap mic & speak \"1 2 3 4 5 6\" or enter digits manually.",
                                fontSize = 11.sp,
                                color = KashmirDeepGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick test OTP chip
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Emulator / Demo Helper: ", fontSize = 11.sp, color = KashmirTextSecondary)
                        AssistChip(
                            onClick = { otpCode = "123456" },
                            label = { Text("Use OTP 123456", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Bolt, null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Verify OTP Button
                    val isVerifying = phoneAuthState is PhoneAuthState.VerifyingCode
                    Button(
                        onClick = {
                            if (otpCode.length < 6) {
                                localErrorMessage = "Please enter the full 6-digit verification code."
                                return@Button
                            }
                            viewModel.verifyPhoneOtpCode(
                                otpCode = otpCode,
                                onSuccess = { isNew, user ->
                                    if (!isNew && user != null) {
                                        localSuccessMessage = "Welcome back, ${user.fullName}!"
                                        onAuthSuccess()
                                    }
                                },
                                onError = { localErrorMessage = it }
                            )
                        },
                        enabled = !isVerifying,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("verify_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify Code & Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Resend countdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canResend) {
                            TextButton(
                                onClick = {
                                    if (activity != null) {
                                        viewModel.requestPhoneVerification(
                                            activity = activity,
                                            rawPhone = rawPhone,
                                            onCodeSent = { localSuccessMessage = "Verification code resent." },
                                            onError = { localErrorMessage = it }
                                        )
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp), tint = KashmirEmerald)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resend Code via SMS", color = KashmirEmerald, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "Resend SMS code in ${resendCountdown}s",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                        }
                    }
                }

                // STEP 1: ENTER PHONE NUMBER (IDLE / SENDING)
                else -> {
                    Text(
                        text = "Sign In / Register with Phone",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = KashmirTextPrimary
                    )
                    Text(
                        text = "Speak or enter your 10-digit mobile number. We'll send an SMS verification code.",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Phone input field with country code prefix & Mic button
                    OutlinedTextField(
                        value = rawPhone,
                        onValueChange = {
                            if (it.length <= 13) {
                                rawPhone = it
                                localErrorMessage = ""
                            }
                        },
                        label = { Text("Mobile Number (فون نمبر)") },
                        placeholder = { Text("e.g. 9419011111 or 7006123456") },
                        prefix = {
                            Text(
                                text = "+91 ",
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Call, contentDescription = null, tint = KashmirEmerald)
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { startVoiceInput(VoiceActiveTarget.PHONE_INPUT) },
                                modifier = Modifier.testTag("voice_phone_mic_button")
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Speak Phone Number",
                                    tint = if (isListening && activeVoiceTarget == VoiceActiveTarget.PHONE_INPUT) KashmirGold else KashmirEmerald
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Audio & Voice prompt guidance card
                    Surface(
                        color = KashmirMint.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voice-First: Tap mic and say your 10 digits in English, Urdu or Kashmiri.",
                                fontSize = 11.sp,
                                color = KashmirDeepGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Demo / Kashmir Test Numbers
                    Text(
                        text = "Quick Demo Personas (1-Tap):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = KashmirTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        AssistChip(
                            onClick = {
                                rawPhone = "9419011111"
                                viewModel.useSimulatedTestPhone("9419011111")
                            },
                            label = { Text("Ghulam (Srinagar)", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                rawPhone = "9797022222"
                                viewModel.useSimulatedTestPhone("9797022222")
                            },
                            label = { Text("Shafi (Baramulla)", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                rawPhone = "9876543210"
                                viewModel.useSimulatedTestPhone("9876543210")
                            },
                            label = { Text("New User (Test)", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Send OTP Button
                    val isSending = phoneAuthState is PhoneAuthState.SendingCode
                    Button(
                        onClick = {
                            val cleanDigits = rawPhone.filter { it.isDigit() }
                            if (cleanDigits.length < 10) {
                                localErrorMessage = "Please enter or speak a valid 10-digit mobile number."
                                return@Button
                            }
                            if (activity != null) {
                                viewModel.requestPhoneVerification(
                                    activity = activity,
                                    rawPhone = rawPhone,
                                    onCodeSent = {
                                        localSuccessMessage = "Verification code sent successfully!"
                                    },
                                    onError = { localErrorMessage = it }
                                )
                            } else {
                                // Fallback for headless testing
                                viewModel.useSimulatedTestPhone(rawPhone)
                            }
                        },
                        enabled = !isSending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("send_otp_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Verification Code (SMS)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
