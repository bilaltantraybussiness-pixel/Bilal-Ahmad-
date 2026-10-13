package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: RozgarViewModel,
    onNavigateBack: () -> Unit = { viewModel.navigateBack() },
    onNavigateToRegister: () -> Unit = { viewModel.navigateTo(Screen.AUTH) }
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Phone Auth (Voice/SMS), 1: User Login, 2: Admin Login
    var identifier by remember { mutableStateOf("") } // Email, Number, or Username
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val currentUser by viewModel.currentUser.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Phone Sign In (Voice & OTP)"
                            2 -> "Admin Portal Login"
                            else -> "Sign In to Rozgar Kashmir"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (selectedTab == 2) KashmirForestGreen else KashmirDeepGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand Logo & Emblem
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectedTab == 2) KashmirGold else KashmirDeepGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (selectedTab) {
                        0 -> Icons.Default.PhoneIphone
                        2 -> Icons.Default.AdminPanelSettings
                        else -> Icons.Default.Handshake
                    },
                    contentDescription = "Emblem",
                    tint = if (selectedTab == 2) KashmirDeepGreen else Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ROZGAR KASHMIR",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = KashmirDeepGreen,
                    letterSpacing = 1.2.sp
                )
            )

            Text(
                text = when (selectedTab) {
                    0 -> "Voice-First Phone Authentication (Firebase Auth)"
                    2 -> "Administrative Control & Governance Center"
                    else -> "Artisans, Employers, Workers & Marketplace Portal"
                },
                fontSize = 12.sp,
                color = KashmirTextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Login Mode Switcher Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = KashmirDeepGreen,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, KashmirCardBorder, RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = ""
                    },
                    modifier = Modifier.testTag("phone_login_tab"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneIphone, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Phone (Voice)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = ""
                    },
                    modifier = Modifier.testTag("user_login_tab"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Password", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        errorMessage = ""
                    },
                    modifier = Modifier.testTag("admin_login_tab"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Admin", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (selectedTab == 0) {
                // PHONE AUTHENTICATION (VOICE & OTP)
                PhoneAuthVoiceComponent(
                    viewModel = viewModel,
                    onAuthSuccess = onNavigateBack
                )
            } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (selectedTab == 2) "Admin Credentials" else "Account Sign In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KashmirTextPrimary
                        )
                    )
                    Text(
                        text = if (selectedTab == 2)
                            "Enter Admin email, phone number, or username"
                        else
                            "Sign in with your Email, Phone number, or Username",
                        fontSize = 12.sp,
                        color = KashmirTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email / Phone / Username field
                    OutlinedTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = ""
                        },
                        label = {
                            Text(if (selectedTab == 1) "Admin Email, Phone, or Username" else "Email / Phone / Username")
                        },
                        placeholder = {
                            Text(if (selectedTab == 1) "admin@rozgarkashmir.in or 9906000000" else "e.g. 9419011111 or email@domain.com")
                        },
                        leadingIcon = {
                            Icon(
                                if (selectedTab == 1) Icons.Default.Shield else Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = KashmirEmerald
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_or_phone_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = KashmirEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Error & Success Feedback
                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = KashmirError.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirError.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, tint = KashmirError, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(errorMessage, color = KashmirError, fontSize = 12.sp)
                            }
                        }
                    }

                    if (successMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = KashmirMint,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirEmerald.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, null, tint = KashmirDeepGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(successMessage, color = KashmirDeepGreen, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            if (identifier.isBlank() || password.isBlank()) {
                                errorMessage = "Please enter both identifier and password."
                                return@Button
                            }
                            isLoading = true
                            errorMessage = ""

                            // Optional Firebase Auth attempt if identifier is an email
                            if (identifier.contains("@")) {
                                try {
                                    val auth = FirebaseAuth.getInstance()
                                    auth.signInWithEmailAndPassword(identifier.trim(), password)
                                        .addOnCompleteListener { task ->
                                            // Regardless of Firebase cloud status, verify with local verified database
                                        }
                                } catch (_: Exception) {
                                    // Fallback to local database authentication
                                }
                            }

                            // Primary local Room database authentication
                            viewModel.loginWithIdentifier(
                                identifier = identifier,
                                pass = password,
                                requireAdmin = (selectedTab == 2)
                            ) { success, msg ->
                                isLoading = false
                                if (success) {
                                    successMessage = msg
                                } else {
                                    errorMessage = msg
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 2) KashmirForestGreen else KashmirDeepGreen
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (selectedTab == 2) Icons.Default.AdminPanelSettings else Icons.Default.Login,
                                    null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (selectedTab == 2) "Sign In as Admin" else "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo Fill Shortcuts Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, KashmirCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Quick Demo Credentials Autofill:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KashmirDeepGreen)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedTab = 2
                                identifier = "admin@rozgarkashmir.in"
                                password = "Admin@123"
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🛡️ Fill Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedTab = 1
                                identifier = "9419011111"
                                password = "worker123"
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🔨 Fill Worker", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedTab = 1
                                identifier = "9797022222"
                                password = "worker123"
                                errorMessage = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text("🍏 Fill Employer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            } // End of else (selectedTab != 0)

            Spacer(modifier = Modifier.height(20.dp))

            // Register Prompt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Don't have an account?", fontSize = 13.sp, color = KashmirTextSecondary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Register Here",
                    fontSize = 13.sp,
                    color = KashmirEmerald,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}
