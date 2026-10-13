package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.RozgarViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    viewModel: RozgarViewModel,
    onDismiss: () -> Unit
) {
    var selectedAuthTab by remember { mutableIntStateOf(0) } // 0: Phone (Voice & OTP), 1: Login, 2: Register, 3: Forgot Password, 4: Demo Accounts

    // Login state
    var loginPhone by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Register state
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regDistrict by remember { mutableStateOf(KASHMIR_DISTRICTS.first()) }
    var regArea by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var roleWorker by remember { mutableStateOf(true) }
    var roleEmployer by remember { mutableStateOf(false) }
    var roleSeller by remember { mutableStateOf(false) }
    var roleBuyer by remember { mutableStateOf(true) }
    var regSecurityAnswer by remember { mutableStateOf("Kashmir") }

    // Recovery state
    var recPhone by remember { mutableStateOf("") }
    var recAnswer by remember { mutableStateOf("") }
    var recNewPassword by remember { mutableStateOf("") }

    var statusMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rozgar Kashmir Account",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KashmirDeepGreen
                            )
                        )
                        Text(
                            text = "Privacy Guaranteed: Phone numbers are never shown publicly.",
                            style = MaterialTheme.typography.labelSmall.copy(color = KashmirTextSecondary)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                ScrollableTabRow(
                    selectedTabIndex = selectedAuthTab,
                    edgePadding = 0.dp,
                    containerColor = KashmirMint.copy(alpha = 0.5f),
                    indicator = {},
                    divider = {}
                ) {
                    val tabs = listOf("Phone (Voice)", "Sign In (Password)", "Register", "Recovery", "Quick Demo Accounts")
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedAuthTab == idx,
                            onClick = {
                                selectedAuthTab = idx
                                statusMessage = ""
                            },
                            text = {
                                Text(
                                    label,
                                    fontWeight = if (selectedAuthTab == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAuthTab == idx) KashmirDeepGreen else KashmirTextMuted
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedAuthTab) {
                0 -> {
                    // PHONE AUTH (VOICE & OTP)
                    item {
                        PhoneAuthVoiceComponent(
                            viewModel = viewModel,
                            onAuthSuccess = onDismiss
                        )
                    }
                }

                1 -> {
                    // LOGIN
                    item {
                        OutlinedTextField(
                            value = loginPhone,
                            onValueChange = { loginPhone = it },
                            label = { Text("Registered Phone Number") },
                            placeholder = { Text("e.g. 9419011111") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_phone_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.login(loginPhone, loginPassword) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) {
                                        onDismiss()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Log In", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        TextButton(
                            onClick = { selectedAuthTab = 3 },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Forgot password? Recover here", color = KashmirEmerald, fontSize = 12.sp)
                        }
                    }
                }

                2 -> {
                    // REGISTER
                    item {
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("Phone Number * (Private & Secure)") },
                            placeholder = { Text("e.g. 9419XXXXXX") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password *") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Select Your Kashmir District *", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(KASHMIR_DISTRICTS) { dist ->
                                FilterChip(
                                    selected = regDistrict == dist,
                                    onClick = { regDistrict = dist },
                                    label = { Text(dist, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regArea,
                            onValueChange = { regArea = it },
                            label = { Text("Area / Town / Village *") },
                            placeholder = { Text("e.g. Lal Chowk, Sopore, Khanabal") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Account Roles (Select all that apply)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = roleWorker, onCheckedChange = { roleWorker = it })
                            Text("Worker / Artisan", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Checkbox(checked = roleEmployer, onCheckedChange = { roleEmployer = it })
                            Text("Employer", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = roleSeller, onCheckedChange = { roleSeller = it })
                            Text("Seller", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Checkbox(checked = roleBuyer, onCheckedChange = { roleBuyer = it })
                            Text("Buyer", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regSecurityAnswer,
                            onValueChange = { regSecurityAnswer = it },
                            label = { Text("Security Question: Favorite Kashmir valley?") },
                            placeholder = { Text("e.g. Kashmir") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (regName.isBlank() || regPhone.isBlank() || regPassword.isBlank() || regArea.isBlank()) {
                                    statusMessage = "Please complete all mandatory fields."
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
                                    fullName = regName,
                                    phone = regPhone,
                                    pass = regPassword,
                                    district = regDistrict,
                                    area = regArea,
                                    email = regEmail,
                                    roles = rolesList.joinToString(","),
                                    securityAnswer = regSecurityAnswer
                                ) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) {
                                        onDismiss()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_submit_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Account", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                3 -> {
                    // RECOVERY
                    item {
                        Text(
                            text = "Forgot Password Recovery",
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Verify your security answer to set a new password.",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = recPhone,
                            onValueChange = { recPhone = it },
                            label = { Text("Registered Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = recAnswer,
                            onValueChange = { recAnswer = it },
                            label = { Text("Security Answer (e.g. Kashmir)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = recNewPassword,
                            onValueChange = { recNewPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.recoverPassword(recPhone, recAnswer, recNewPassword) { success, msg ->
                                    statusMessage = msg
                                    isError = !success
                                    if (success) {
                                        selectedAuthTab = 1
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = KashmirDeepGreen)
                        ) {
                            Text("Reset Password")
                        }
                    }
                }

                4 -> {
                    // DEMO ACCOUNT SWITCHER
                    item {
                        Text(
                            text = "Select Persona to Test All Roles & Admin",
                            fontWeight = FontWeight.Bold,
                            color = KashmirDeepGreen
                        )
                        Text(
                            text = "Instantly switch active user session to test different perspectives across Kashmir.",
                            fontSize = 12.sp,
                            color = KashmirTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val demoAccounts = listOf(
                            Triple("1", "Admin Rozgar Kashmir (Full Admin Panel)", "Phone: 9906000000 • Roles: ADMIN, WORKER, EMPLOYER"),
                            Triple("2", "Ghulam Mohammad Rather (Artisan / Worker)", "Phone: 9419011111 • Walnut Carver, Srinagar"),
                            Triple("3", "Mohammad Shafi Dar (Seasonal Orchardist)", "Phone: 9797022222 • Apple Picker & Crew Lead, Sopore"),
                            Triple("4", "Parveena Jan (Pashmina Embroidery Artisan)", "Phone: 9622033333 • Tilla & Sozni Crafts, Budgam"),
                            Triple("5", "Dr. Adil Hussain (Medical Doctor)", "Phone: 9596044444 • Physician & Clinic, Anantnag"),
                            Triple("7", "Bashir Ahmad Wani (Employer / Orchardist)", "Phone: 9906066666 • Apple Estates, Shopian")
                        )

                        demoAccounts.forEach { (id, title, desc) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.switchUser(id.toLong())
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = KashmirMint.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = KashmirDeepGreen)
                                    Text(desc, fontSize = 11.sp, color = KashmirTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            if (statusMessage.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = statusMessage,
                        color = if (isError) KashmirError else KashmirSuccess,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
