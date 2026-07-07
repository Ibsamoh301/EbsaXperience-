package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AppLanguage

@Composable
fun LoginScreen(
    viewModel: BeysaViewModel,
    onNavigateToRegister: () -> Unit
) {
    val phoneInput by viewModel.loginPhoneInput.collectAsState()
    val otpInput by viewModel.loginOtpInput.collectAsState()
    val isOtpRequested by viewModel.isOtpRequested.collectAsState()
    val otpError by viewModel.otpError.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Language selector row at the top of the box
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Afaan Oromoo",
                    modifier = Modifier
                        .clickable { viewModel.setLanguage(AppLanguage.AFAAN_OROMOO) }
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (currentLang == AppLanguage.AFAAN_OROMOO) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (currentLang == AppLanguage.AFAAN_OROMOO) FontWeight.Bold else FontWeight.Normal
                )
                Text(text = "|", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                Text(
                    text = "አማርኛ",
                    modifier = Modifier
                        .clickable { viewModel.setLanguage(AppLanguage.AMHARIC) }
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (currentLang == AppLanguage.AMHARIC) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (currentLang == AppLanguage.AMHARIC) FontWeight.Bold else FontWeight.Normal
                )
                Text(text = "|", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                Text(
                    text = "English",
                    modifier = Modifier
                        .clickable { viewModel.setLanguage(AppLanguage.ENGLISH) }
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (currentLang == AppLanguage.ENGLISH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = if (currentLang == AppLanguage.ENGLISH) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Branding Icon / Illustration
            Image(
                painter = painterResource(id = R.drawable.img_beysa_logo_1783379814431),
                contentDescription = "BEYSA FARM Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )

            Text(
                text = viewModel.getString("app_title"),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title_text")
            )

            Text(
                text = viewModel.getString("login_subtitle"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Phone Input
            OutlinedTextField(
                value = phoneInput,
                onValueChange = { viewModel.setPhoneInput(it) },
                label = { Text(viewModel.getString("phone_label")) },
                placeholder = { Text("09xxxxxxxx") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phone_input"),
                shape = RoundedCornerShape(12.dp)
            )

            AnimatedVisibility(
                visible = isOtpRequested,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { viewModel.setOtpInput(it) },
                        label = { Text(viewModel.getString("otp_label")) },
                        placeholder = { Text("1234 (Simulated)") },
                        leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "Simulated SMS OTP sent to $phoneInput. Enter '1234' to authorize.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (otpError != null) {
                Text(
                    text = otpError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Button(
                onClick = {
                    if (isOtpRequested) {
                        viewModel.verifyOtpAndLogin()
                    } else {
                        viewModel.requestOtp()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_action_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isOtpRequested) Icons.Default.Login else Icons.Default.Send,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOtpRequested) viewModel.getString("btn_verify_otp") else viewModel.getString("btn_get_otp"),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("register_navigation_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = viewModel.getString("btn_register"))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick demo login card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Dadar Ethiopia Demo Simulators",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Tap a simulated phone SIM below to fill credentials and auto-login to that specific security role instantly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        demoSIMButton("Customer (Ibsan)", "0911111111", Icons.Default.ShoppingBag) {
                            viewModel.setPhoneInput("0911111111")
                            viewModel.requestOtp()
                        }
                        demoSIMButton("Delivery Driver (Kebede)", "0922222222", Icons.Default.LocalShipping) {
                            viewModel.setPhoneInput("0922222222")
                            viewModel.requestOtp()
                        }
                        demoSIMButton("Manager (Tolera)", "0933333333", Icons.Default.Assignment) {
                            viewModel.setPhoneInput("0933333333")
                            viewModel.requestOtp()
                        }
                        demoSIMButton("Super Admin/Owner (Chala)", "0944444444", Icons.Default.AdminPanelSettings) {
                            viewModel.setPhoneInput("0944444444")
                            viewModel.requestOtp()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun demoSIMButton(
    roleLabel: String,
    phoneNumber: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Column {
                    Text(text = roleLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text(text = phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: BeysaViewModel,
    onNavigateToLogin: () -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = viewModel.getString("register_title"),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(viewModel.getString("name_label")) },
                placeholder = { Text("e.g. Ibsan") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = usernameInput,
                onValueChange = { usernameInput = it },
                label = { Text(viewModel.getString("username_label")) },
                placeholder = { Text("e.g. ibsan30") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text(viewModel.getString("phone_label")) },
                placeholder = { Text("09xxxxxxxx") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (validationError != null) {
                Text(
                    text = validationError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (nameInput.isBlank() || phoneInput.isBlank()) {
                        validationError = "Name and Phone Number are required!"
                    } else {
                        viewModel.registerNewCustomer(usernameInput, nameInput, phoneInput)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Complete Registration", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Sign In")
            }
        }
    }
}
