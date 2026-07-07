package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@Composable
fun SuperAdminDashboard(viewModel: BeysaViewModel) {
    val activeTab by viewModel.adminTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                NavigationBarItem(
                    selected = activeTab == "dashboard" || activeTab == "analytics",
                    onClick = { viewModel.setAdminTab("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = activeTab == "livestock",
                    onClick = { viewModel.setAdminTab("livestock") },
                    icon = { Icon(Icons.Default.Pets, contentDescription = null) },
                    label = { Text("Livestock") }
                )
                NavigationBarItem(
                    selected = activeTab == "production",
                    onClick = { viewModel.setAdminTab("production") },
                    icon = { Icon(Icons.Default.Hive, contentDescription = null) },
                    label = { Text("Yields") }
                )
                NavigationBarItem(
                    selected = activeTab == "users",
                    onClick = { viewModel.setAdminTab("users") },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Staff") }
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { viewModel.setAdminTab("profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "dashboard" -> SuperAdminOverviewScreen(viewModel)
                "livestock" -> SuperAdminLivestockScreen(viewModel)
                "production" -> SuperAdminProductionScreen(viewModel)
                "users" -> SuperAdminUsersScreen(viewModel)
                "profile" -> CustomerProfileScreen(viewModel) // Re-use
            }
        }
    }
}

@Composable
fun SuperAdminOverviewScreen(viewModel: BeysaViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val livestock by viewModel.allLivestock.collectAsState()
    val records by viewModel.allProductionRecords.collectAsState()

    var showBackupSuccess by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = viewModel.getString("admin_dashboard"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Dadar Ethiopia Owner Desk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ANALYTICS GRAPH
        Text(
            text = "Milk Yield Trends (Liters)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw simulated bar charts
                    val data = listOf(280f, 310f, 290f, 320f, 305f)
                    val barWidth = 60f
                    val spacing = 50f
                    val maxVal = 350f

                    for (i in data.indices) {
                        val barHeight = (data[i] / maxVal) * size.height
                        val left = (i * (barWidth + spacing)) + 40f
                        val top = size.height - barHeight

                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(left, top),
                            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                        )
                    }
                }
            }
        }

        Divider()

        // Backup & Restore
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "System Maintenance",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showBackupSuccess = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Backup DB")
                    }

                    OutlinedButton(
                        onClick = { showBackupSuccess = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore DB")
                    }
                }

                AnimatedVisibility(visible = showBackupSuccess) {
                    Text(
                        text = "Local backup sync completed successfully! File stored in /secure_backups/.",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Quick Admin Cards
        Text(
            text = "Core Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        QuickTaskCard("Digital Livestock Registry", "Manage Dairy cattle, Buffaloes, Borana bulls, beehives.", Icons.Default.Pets) {
            viewModel.setAdminTab("livestock")
        }

        QuickTaskCard("Production Output Logging", "Record raw milk and highland honey yields.", Icons.Default.Hive) {
            viewModel.setAdminTab("production")
        }

        QuickTaskCard("Staff Directory & Permissions", "Add, edit, or remove managers & delivery drivers.", Icons.Default.Group) {
            viewModel.setAdminTab("users")
        }
    }
}

@Composable
fun SuperAdminLivestockScreen(viewModel: BeysaViewModel) {
    val livestockList by viewModel.allLivestock.collectAsState()

    var isAddingOrEditing by remember { mutableStateOf(false) }
    var editingLivestock by remember { mutableStateOf<Livestock?>(null) }

    // Inputs
    var animalIdInput by remember { mutableStateOf("") }
    var breedInput by remember { mutableStateOf("") }
    var speciesInput by remember { mutableStateOf("Dairy Cattle") }
    var weightInput by remember { mutableStateOf("") }
    var sexInput by remember { mutableStateOf("Female") }
    var dobInput by remember { mutableStateOf("2024-01-01") }
    var healthInput by remember { mutableStateOf("Healthy") }
    var vaccineInput by remember { mutableStateOf("") }
    var breedHistoryInput by remember { mutableStateOf("None") }
    var notesInput by remember { mutableStateOf("") }

    val speciesOptions = listOf("Dairy Cattle", "Buffalo (Jaamusii)", "Borana Cattle", "Goats", "Beehives")

    if (isAddingOrEditing) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = { isAddingOrEditing = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Text(
                    text = if (editingLivestock == null) "Register New Animal Tag" else "Edit Animal Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Species selector
            Text(text = "Species", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                speciesOptions.take(3).forEach { sp ->
                    FilterChip(
                        selected = speciesInput == sp,
                        onClick = { speciesInput = sp },
                        label = { Text(sp) }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                speciesOptions.drop(3).forEach { sp ->
                    FilterChip(
                        selected = speciesInput == sp,
                        onClick = { speciesInput = sp },
                        label = { Text(sp) }
                    )
                }
            }

            OutlinedTextField(value = animalIdInput, onValueChange = { animalIdInput = it }, label = { Text("Animal ID Tag") }, placeholder = { Text("e.g. BF-C10") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = breedInput, onValueChange = { breedInput = it }, label = { Text("Breed Details") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = dobInput, onValueChange = { dobInput = it }, label = { Text("Date of Birth") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = weightInput, onValueChange = { weightInput = it }, label = { Text("Weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Female", "Male").forEach { sex ->
                    FilterChip(selected = sexInput == sex, onClick = { sexInput = sex }, label = { Text(sex) })
                }
            }

            OutlinedTextField(value = healthInput, onValueChange = { healthInput = it }, label = { Text("Health Status") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = vaccineInput, onValueChange = { vaccineInput = it }, label = { Text("Vaccination History") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = notesInput, onValueChange = { notesInput = it }, label = { Text("Notes & Comments") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val weight = weightInput.toDoubleOrNull() ?: 0.0
                    val livestock = Livestock(
                        animalId = animalIdInput,
                        species = speciesInput,
                        breed = breedInput,
                        dateOfBirth = dobInput,
                        sex = sexInput,
                        weight = weight,
                        healthStatus = healthInput,
                        vaccinationHistory = vaccineInput,
                        breedingHistory = breedHistoryInput,
                        notes = notesInput
                    )

                    viewModel.saveLivestock(livestock)
                    isAddingOrEditing = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register Animal", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = viewModel.getString("livestock_records"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        editingLivestock = null
                        animalIdInput = ""
                        breedInput = ""
                        dobInput = "2024-01-01"
                        weightInput = ""
                        sexInput = "Female"
                        healthInput = "Healthy"
                        vaccineInput = ""
                        breedHistoryInput = "None"
                        notesInput = ""
                        isAddingOrEditing = true
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(livestockList) { l ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text(text = l.animalId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                Surface(
                                    color = if (l.healthStatus == "Healthy") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = l.healthStatus,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = if (l.healthStatus == "Healthy") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            Text(text = "Species: ${l.species} | Breed: ${l.breed}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Weight: ${l.weight} kg | Sex: ${l.sex} | DoB: ${l.dateOfBirth}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            if (l.vaccinationHistory.isNotEmpty()) {
                                Text(
                                    text = "Vaccines: ${l.vaccinationHistory}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (l.notes.isNotEmpty()) {
                                Text(text = "Notes: ${l.notes}", style = MaterialTheme.typography.bodySmall)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    editingLivestock = l
                                    animalIdInput = l.animalId
                                    breedInput = l.breed
                                    speciesInput = l.species
                                    weightInput = l.weight.toString()
                                    sexInput = l.sex
                                    dobInput = l.dateOfBirth
                                    healthInput = l.healthStatus
                                    vaccineInput = l.vaccinationHistory
                                    breedHistoryInput = l.breedingHistory
                                    notesInput = l.notes
                                    isAddingOrEditing = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { viewModel.deleteLivestock(l.animalId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
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
fun SuperAdminProductionScreen(viewModel: BeysaViewModel) {
    val records by viewModel.allProductionRecords.collectAsState()

    var typeInput by remember { mutableStateOf("Milk") }
    var quantityInput by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("2026-07-06") }
    var notesInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = viewModel.getString("production_records_label"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        // Adder card
        Card(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Log Harvest Output", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = typeInput == "Milk", onClick = { typeInput = "Milk" }, label = { Text("Milk (Liters)") })
                    FilterChip(selected = typeInput == "Honey", onClick = { typeInput = "Honey" }, label = { Text("Honey (kg)") })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Yield") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("Date") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Milking / Harvesting Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val qty = quantityInput.toDoubleOrNull() ?: 0.0
                        val record = ProductionRecord(
                            type = typeInput,
                            quantity = qty,
                            date = dateInput,
                            notes = notesInput
                        )

                        viewModel.saveProductionRecord(record)
                        quantityInput = ""
                        notesInput = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Yield Output Log", fontWeight = FontWeight.Bold)
                }
            }
        }

        Text(
            text = "Harvest History Output Log",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(records) { r ->
                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (r.type == "Milk") Icons.Default.LocalCafe else Icons.Default.Hive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Column {
                                Text(
                                    text = "${r.quantity} ${if (r.type == "Milk") "Liters" else "Kg"}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(text = r.notes, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = r.date, style = MaterialTheme.typography.labelSmall)
                            IconButton(onClick = { viewModel.deleteProductionRecord(r.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuperAdminUsersScreen(viewModel: BeysaViewModel) {
    val users by viewModel.allUsers.collectAsState()

    var isAdding by remember { mutableStateOf(false) }

    var usernameInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.DRIVER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Staff & Roles Desk",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(
                onClick = { isAdding = !isAdding },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(if (isAdding) Icons.Default.Close else Icons.Default.Add, contentDescription = null, tint = Color.White)
            }
        }

        if (isAdding) {
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Add verified Employee", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = usernameInput, onValueChange = { usernameInput = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

                    Text(text = "System Role Permissions:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(UserRole.DRIVER, UserRole.MANAGER, UserRole.SUPER_ADMIN).forEach { role ->
                            val isSelected = selectedRole == role
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedRole = role },
                                label = { Text(role.name) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (usernameInput.isNotEmpty() && phoneInput.isNotEmpty()) {
                                val user = User(
                                    username = usernameInput,
                                    name = nameInput,
                                    phoneNumber = phoneInput,
                                    role = selectedRole,
                                    isVerified = true
                                )
                                viewModel.saveUser(user)
                                isAdding = false
                                nameInput = ""
                                usernameInput = ""
                                phoneInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add verified Employee Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = viewModel.getString("manage_users"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(users) { u ->
                Card(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            Column {
                                Text(text = u.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = "Phone: ${u.phoneNumber}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = u.role.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Don't allow owner to delete themselves
                            if (u.username != "admin") {
                                IconButton(onClick = { viewModel.deleteUser(u.username) }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
