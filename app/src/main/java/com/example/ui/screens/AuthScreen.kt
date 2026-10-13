package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: RozgarViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Phone Auth (Voice & OTP), 1: Login, 2: Register, 3: Recovery, 4: Demo Personas

    // Login Form State
    var loginPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regDistrict by remember { mutableStateOf(KASHMIR_DISTRICTS.first()) }
    var regArea by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var roleWorker by remember { mutableStateOf(true) }
    var roleEmployer by remember { mutableStateOf(false) }
    var roleSeller by remember { mutableStateOf(false) }
    var roleBuyer by remember { mutableStateOf(true) }
    var regSecurityAnswer by remember { mutableStateOf("Kashmir") }

    // Password Recovery State
    var recPhone by remember { mutableStateOf("") }
    var recAnswer by remember { mutableStateOf("") }
    var recNewPassword by remember { mutableStateOf("") }
    var recPasswordVisible by remember { mutableStateOf(false) }

    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (currentUser != null) "Account & Session" else "Sign In or Register",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rozgar Kashmir Authentication",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(KashmirBackground)
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Brand & Heritage Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KashmirDeepGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KashmirGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = "Emblem",
                                tint = KashmirDeepGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "ROZGAR KASHMIR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.1.sp
                            )
                        )

                        Text(
                            text = "Connecting Skills With Opportunities Across 10 Districts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = KashmirMint,
                                fontSize = 11.sp
                            )
                        )

                        // Strict Phone Privacy Guarantee Pill
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = KashmirForestGreen,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, KashmirGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Shield, null, tint = KashmirGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Privacy Shield: Phone numbers are private and NEVER displayed publicly.",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // If user is already logged in, show active session card
            currentUser?.let { user ->
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(KashmirEmerald),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.fullName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(
                                            "Active Session • ${user.district}",
                                            fontSize = 12.sp,
                                            color = KashmirEmerald,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Surface(
                                    color = KashmirMint,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "LOGGED IN",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = KashmirDeepGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = KashmirCardBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Registered Phone: ${user.phoneNumber} (Private & Secure)",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                            Text(
                                text = "Roles: ${user.roles}",
                                fontSize = 12.sp,
                                color = KashmirTextSecondary
                            )
                            val loginDate = remember(user.lastLoginAt) {
                                SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(user.lastLoginAt))
                            }
                            Text(
                                text = "Session Started: $loginDate",
                                fontSize = 11.sp,
                                color = KashmirTextMuted
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.navigateTo(Screen.HOME) },
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Go to Home", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.logout()
                                        statusMessage = "Logged out successfully."
                                        isError = false
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KashmirError),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Tab Switcher (Sign In / Register / Recovery / Demo Personas)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                ScrollableTabRow(
                    selectedTabIndex = selectedAuthTab,
                    edgePadding = 0.dp,
                    containerColor = KashmirMint.copy(alpha = 0.6f),
                    indicator = {},
                    divider = {},
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    val tabs = listOf("Phone (Voice & OTP)", "Password Login", "Password Register", "Forgot Password", "Quick Demo")
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedAuthTab == idx,
                            onClick = {
                                selectedAuthTab = idx
                                statusMessage = ""
                            },
                            text = {
                                Text(
                                    text = label,
                                    fontWeight = if (selectedAuthTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAuthTab == idx) KashmirDeepGreen else KashmirTextMuted,
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier
                                .padding(3.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedAuthTab == idx) Color.White else Color.Transparent)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            when (selectedAuthTab) {
                0 -> {
                    // TAB 0: FIREBASE PHONE AUTHENTICATION (VOICE & OTP)
                    item {
                        PhoneAuthVoiceComponent(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                viewModel.navigateTo(Screen.HOME)
                            }
                        )
                    }
                }

                1 -> {
                    // TAB 1: LOGIN SCREEN (PASSWORD)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Sign In to Your Account",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Enter your registered Kashmir phone number and password.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = loginPhone,
                                    onValueChange = { loginPhone = it },
                                    label = { Text("Phone Number *") },
                                    placeholder = { Text("e.g. 9419011111") },
                                    leadingIcon = {
                                        Text(
                                            "+91 ",
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirDeepGreen,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_screen_login_phone"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = { loginPassword = it },
                                    label = { Text("Password *") },
                                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = KashmirEmerald) },
                                    trailingIcon = {
                                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_screen_login_password"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { selectedAuthTab = 2 }) {
                                        Text("Forgot Password?", color = KashmirEmerald, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        if (loginPhone.isBlank() || loginPassword.isBlank()) {
                                            statusMessage = "Please enter both phone number and password."
                                            isError = true
                                            return@Button
                                        }
                                        viewModel.login(loginPhone.trim(), loginPassword) { success, msg ->
                                            statusMessage = msg
                                            isError = !success
                                            if (success) {
                                                viewModel.navigateTo(Screen.HOME)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_screen_login_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Login, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Log In with Password Hashing", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("New to Rozgar Kashmir?", fontSize = 12.sp, color = KashmirTextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Create an Account",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen,
                                        modifier = Modifier.clickable { selectedAuthTab = 2 }
                                    )
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: REGISTRATION SCREEN (PASSWORD)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Create Your Free Account",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Register with name, phone, password, and Kashmir district.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = regName,
                                    onValueChange = { regName = it },
                                    label = { Text("Full Name *") },
                                    placeholder = { Text("e.g. Bashir Ahmad Rather") },
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = KashmirEmerald) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_reg_name_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = regPhone,
                                    onValueChange = { regPhone = it },
                                    label = { Text("Phone Number * (Private)") },
                                    placeholder = { Text("e.g. 9419XXXXXX") },
                                    leadingIcon = {
                                        Text(
                                            "+91 ",
                                            fontWeight = FontWeight.Bold,
                                            color = KashmirDeepGreen,
                                            modifier = Modifier.padding(start = 12.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_reg_phone_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = { regPassword = it },
                                    label = { Text("Create Password * (SHA-256 Hashed)") },
                                    visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = KashmirEmerald) },
                                    trailingIcon = {
                                        IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                            Icon(
                                                imageVector = if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_reg_password_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // District Selector Carousel
                                Text(
                                    text = "Select District in Kashmir *",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = KashmirTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(KASHMIR_DISTRICTS) { dist ->
                                        FilterChip(
                                            selected = regDistrict == dist,
                                            onClick = { regDistrict = dist },
                                            label = { Text(dist, fontSize = 11.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = KashmirMint,
                                                selectedLabelColor = KashmirDeepGreen
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = regArea,
                                    onValueChange = { regArea = it },
                                    label = { Text("Area / Town / Village *") },
                                    placeholder = { Text("e.g. Lal Chowk, Zainapora, Sopore Mandi, Khanabal") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = KashmirEmerald) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_reg_area_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = { regEmail = it },
                                    label = { Text("Email Address (Optional)") },
                                    placeholder = { Text("e.g. you@example.com") },
                                    leadingIcon = { Icon(Icons.Default.Email, null, tint = KashmirEmerald) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Roles Selection
                                Text(
                                    text = "Account Roles (One account can perform all roles)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = KashmirTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = roleWorker, onCheckedChange = { roleWorker = it })
                                        Text("Worker / Job Seeker", fontSize = 12.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = roleEmployer, onCheckedChange = { roleEmployer = it })
                                        Text("Employer", fontSize = 12.sp)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = roleSeller, onCheckedChange = { roleSeller = it })
                                        Text("Seller", fontSize = 12.sp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = roleBuyer, onCheckedChange = { roleBuyer = it })
                                        Text("Buyer", fontSize = 12.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Security Question for Recovery
                                OutlinedTextField(
                                    value = regSecurityAnswer,
                                    onValueChange = { regSecurityAnswer = it },
                                    label = { Text("Security Recovery Question") },
                                    placeholder = { Text("Favorite Kashmir valley or birthplace (e.g. Kashmir)") },
                                    supportingText = { Text("Used for password recovery if you forget your password.") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        if (regName.isBlank() || regPhone.isBlank() || regPassword.isBlank() || regArea.isBlank()) {
                                            statusMessage = "Please fill in all mandatory fields (Name, Phone, Password, Area)."
                                            isError = true
                                            return@Button
                                        }
                                        val rolesList = mutableListOf<String>()
                                        if (roleWorker) rolesList.add("WORKER")
                                        if (roleEmployer) rolesList.add("EMPLOYER")
                                        if (roleSeller) rolesList.add("SELLER")
                                        if (roleBuyer) rolesList.add("BUYER")
                                        if (rolesList.isEmpty()) rolesList.add("BUYER")

                                        viewModel.register(
                                            fullName = regName.trim(),
                                            phone = regPhone.trim(),
                                            pass = regPassword,
                                            district = regDistrict,
                                            area = regArea.trim(),
                                            email = regEmail.takeIf { it.isNotBlank() },
                                            roles = rolesList.joinToString(","),
                                            securityAnswer = regSecurityAnswer
                                        ) { success, msg ->
                                            statusMessage = msg
                                            isError = !success
                                            if (success) {
                                                viewModel.navigateTo(Screen.HOME)
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_reg_submit_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.HowToReg, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Register Free Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: PASSWORD RECOVERY (FORGOT PASSWORD)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Forgot Password Recovery",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "Verify your registered phone and security answer to set a new password.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = recPhone,
                                    onValueChange = { recPhone = it },
                                    label = { Text("Registered Phone Number *") },
                                    placeholder = { Text("e.g. 9419011111") },
                                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = KashmirEmerald) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = recAnswer,
                                    onValueChange = { recAnswer = it },
                                    label = { Text("Security Answer *") },
                                    placeholder = { Text("e.g. Kashmir") },
                                    leadingIcon = { Icon(Icons.Default.QuestionAnswer, null, tint = KashmirEmerald) },
                                    supportingText = { Text("Answer to: 'What is your birthplace or favorite Kashmir valley?'") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = recNewPassword,
                                    onValueChange = { recNewPassword = it },
                                    label = { Text("New Password *") },
                                    visualTransformation = if (recPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.LockReset, null, tint = KashmirEmerald) },
                                    trailingIcon = {
                                        IconButton(onClick = { recPasswordVisible = !recPasswordVisible }) {
                                            Icon(
                                                imageVector = if (recPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        if (recPhone.isBlank() || recAnswer.isBlank() || recNewPassword.isBlank()) {
                                            statusMessage = "Please provide your phone number, security answer, and new password."
                                            isError = true
                                            return@Button
                                        }
                                        viewModel.recoverPassword(recPhone.trim(), recAnswer.trim(), recNewPassword) { success, msg ->
                                            statusMessage = msg
                                            isError = !success
                                            if (success) {
                                                selectedAuthTab = 1
                                                loginPhone = recPhone.trim()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reset & Hash New Password", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: DEMO PERSONAS
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "Quick Demo Personas",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KashmirDeepGreen
                                    )
                                )
                                Text(
                                    text = "One-tap session sign-in to test different roles and admin features.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = KashmirTextSecondary)
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val personas = listOf(
                                    Triple("1", "Admin Rozgar Kashmir (Full Telemetry)", "Phone: 9906000000 • Roles: ADMIN, WORKER, EMPLOYER • Srinagar"),
                                    Triple("2", "Ghulam Mohammad Rather (Artisan / Worker)", "Phone: 9419011111 • Walnut Wood Carver • Srinagar (Downtown)"),
                                    Triple("3", "Mohammad Shafi Dar (Seasonal Orchardist)", "Phone: 9797022222 • Apple Picker & Crew Lead • Baramulla (Sopore)"),
                                    Triple("4", "Parveena Jan (Handicraft Artisan)", "Phone: 9622033333 • Tilla & Sozni Needlework • Budgam (Kanihama)"),
                                    Triple("5", "Dr. Adil Hussain (Medical Professional)", "Phone: 9596044444 • General Physician • Anantnag"),
                                    Triple("7", "Bashir Ahmad Wani (Employer / Orchardist)", "Phone: 9906066666 • Apple Estates • Shopian")
                                )

                                personas.forEach { (id, title, desc) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.switchUser(id.toLong())
                                                viewModel.navigateTo(Screen.HOME)
                                            },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = KashmirMint.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(KashmirEmerald),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = title.take(1),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                                                Text(desc, fontSize = 11.sp, color = KashmirTextSecondary)
                                            }
                                            Icon(Icons.Default.ArrowForward, null, tint = KashmirDeepGreen, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Status feedback message
            if (statusMessage.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = if (isError) KashmirError.copy(alpha = 0.15f) else KashmirMint,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isError) KashmirError else KashmirEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isError) KashmirError else KashmirDeepGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage,
                                color = if (isError) KashmirError else KashmirDeepGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
