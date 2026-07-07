package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@Composable
fun ManagerDashboard(viewModel: BeysaViewModel) {
    val activeTab by viewModel.managerTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { viewModel.setManagerTab("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Overview") }
                )
                NavigationBarItem(
                    selected = activeTab == "products",
                    onClick = { viewModel.setManagerTab("products") },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = null) },
                    label = { Text("Products") }
                )
                NavigationBarItem(
                    selected = activeTab == "orders",
                    onClick = { viewModel.setManagerTab("orders") },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    label = { Text("Dispatch") }
                )
                NavigationBarItem(
                    selected = activeTab == "translations",
                    onClick = { viewModel.setManagerTab("translations") },
                    icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                    label = { Text("Texts") }
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { viewModel.setManagerTab("profile") },
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
                "dashboard" -> ManagerOverviewScreen(viewModel)
                "products" -> ManagerProductsScreen(viewModel)
                "orders" -> ManagerOrdersScreen(viewModel)
                "translations" -> ManagerTranslationsScreen(viewModel)
                "profile" -> CustomerProfileScreen(viewModel) // Re-use
            }
        }
    }
}

@Composable
fun ManagerOverviewScreen(viewModel: BeysaViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }.size
    val totalRevenue = orders.filter { it.status == OrderStatus.DELIVERED }.sumOf { it.totalAmount }
    val activeDrivers = users.filter { it.role == UserRole.DRIVER }.size

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
                    text = viewModel.getString("manager_dashboard"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Dadar Ethiopia Administration",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.Agriculture,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Analytics Cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Pending Orders", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = pendingOrders.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "Delivery Drivers", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = activeDrivers.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Total Completed Delivery Revenue", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "$totalRevenue ETB",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Manager Actions List
        Text(
            text = "Quick Tasks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        QuickTaskCard("Manage Catalog Inventory", "Add fresh milk, honey, goats, or edit stock quantities.", Icons.Default.AddBox) {
            viewModel.setManagerTab("products")
        }

        QuickTaskCard("Dispatch Pending Shipments", "Assign incoming shopping cart orders to drivers.", Icons.Default.DepartureBoard) {
            viewModel.setManagerTab("orders")
        }

        QuickTaskCard("Edit App Localization Texts", "Modify translation values for Ethiopia local languages.", Icons.Default.GTranslate) {
            viewModel.setManagerTab("translations")
        }
    }
}

@Composable
fun QuickTaskCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Column {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ManagerProductsScreen(viewModel: BeysaViewModel) {
    val products by viewModel.allProducts.collectAsState()
    var isAddingOrEditing by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    // Inputs for Add/Edit
    var nameEnInput by remember { mutableStateOf("") }
    var nameAmInput by remember { mutableStateOf("") }
    var nameAfInput by remember { mutableStateOf("") }
    var descEnInput by remember { mutableStateOf("") }
    var descAmInput by remember { mutableStateOf("") }
    var descAfInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var stockInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Fresh Milk") }
    var deliveryEnInput by remember { mutableStateOf("Delivered live via specialized livestock truck.") }

    val categoryList = listOf("Fresh Milk", "Yogurt", "Honey", "Goats", "Borana Cattle", "Buffalo (Jaamusii)")

    if (isAddingOrEditing) {
        // Form Layout
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
                    text = if (editingProduct == null) "Add New Farm Product" else "Edit Product",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Category selector
            Text(text = "Category", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categoryList.take(3).forEach { cat ->
                    FilterChip(
                        selected = categoryInput == cat,
                        onClick = { categoryInput = cat },
                        label = { Text(cat) }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categoryList.drop(3).forEach { cat ->
                    FilterChip(
                        selected = categoryInput == cat,
                        onClick = { categoryInput = cat },
                        label = { Text(cat) }
                    )
                }
            }

            OutlinedTextField(value = priceInput, onValueChange = { priceInput = it }, label = { Text("Price (ETB)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stockInput, onValueChange = { stockInput = it }, label = { Text("Stock Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Divider()

            Text(text = "Multi-Language Strings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(value = nameEnInput, onValueChange = { nameEnInput = it }, label = { Text("Product Name (English)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nameAmInput, onValueChange = { nameAmInput = it }, label = { Text("Product Name (Amharic - አማርኛ)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nameAfInput, onValueChange = { nameAfInput = it }, label = { Text("Product Name (Afaan Oromoo)") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(value = descEnInput, onValueChange = { descEnInput = it }, label = { Text("Description (English)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descAmInput, onValueChange = { descAmInput = it }, label = { Text("Description (Amharic)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descAfInput, onValueChange = { descAfInput = it }, label = { Text("Description (Afaan Oromoo)") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    val price = priceInput.toDoubleOrNull() ?: 0.0
                    val stock = stockInput.toIntOrNull() ?: 0
                    val product = Product(
                        id = editingProduct?.id ?: 0,
                        nameEn = nameEnInput,
                        nameAm = nameAmInput,
                        nameAf = nameAfInput,
                        descriptionEn = descEnInput,
                        descriptionAm = descAmInput,
                        descriptionAf = descAfInput,
                        category = categoryInput,
                        price = price,
                        stockQuantity = stock,
                        deliveryInfoEn = deliveryEnInput,
                        deliveryInfoAm = "ማድረሻ መረጃ",
                        deliveryInfoAf = "Geejjiba"
                    )

                    viewModel.saveProduct(product)
                    isAddingOrEditing = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Product Details", fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // List Layout
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
                    text = "Product Catalog Builder",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        editingProduct = null
                        nameEnInput = ""
                        nameAmInput = ""
                        nameAfInput = ""
                        descEnInput = ""
                        descAmInput = ""
                        descAfInput = ""
                        priceInput = ""
                        stockInput = ""
                        categoryInput = "Fresh Milk"
                        isAddingOrEditing = true
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(products) { product ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.nameEn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Category: ${product.category}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Text(
                                    text = "Price: ${product.price} ETB | Stock: ${product.stockQuantity}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = {
                                    editingProduct = product
                                    nameEnInput = product.nameEn
                                    nameAmInput = product.nameAm
                                    nameAfInput = product.nameAf
                                    descEnInput = product.descriptionEn
                                    descAmInput = product.descriptionAm
                                    descAfInput = product.descriptionAf
                                    priceInput = product.price.toString()
                                    stockInput = product.stockQuantity.toString()
                                    categoryInput = product.category
                                    isAddingOrEditing = true
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }

                                IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
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
fun ManagerOrdersScreen(viewModel: BeysaViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val users by viewModel.allUsers.collectAsState()

    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING }
    val drivers = users.filter { it.role == UserRole.DRIVER }

    var selectedOrderForAssign by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Incoming Orders Dispatcher",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        if (selectedOrderForAssign != null) {
            // Driver assignment dialog card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Assign Drivers to Order #${selectedOrderForAssign!!.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(text = "Select a verified Ethiopia driver below:", style = MaterialTheme.typography.bodySmall)

                    if (drivers.isEmpty()) {
                        Text(text = "No drivers available. Pre-populate a driver first.", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(130.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(drivers) { d ->
                                Surface(
                                    onClick = {
                                        viewModel.assignDriverToOrder(selectedOrderForAssign!!.id, d.username)
                                        selectedOrderForAssign = null
                                    },
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
                                        Text(text = d.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                        Text(text = d.phoneNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { selectedOrderForAssign = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        // Pending lists
        Text(
            text = "Pending Orders Queue (${pendingOrders.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        if (pendingOrders.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Text("No pending cargo orders in Dadar queue.")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingOrders) { order ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Order #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "${order.totalAmount} ETB", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                            }

                            Text(text = "Customer: ${order.customerUsername}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Address: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                            Button(
                                onClick = { selectedOrderForAssign = order },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("dispatch_order_btn_${order.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AirportShuttle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Assign & Dispatch", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManagerTranslationsScreen(viewModel: BeysaViewModel) {
    val localizations by viewModel.allLocalizations.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editingLoc by remember { mutableStateOf<Localization?>(null) }

    var englishVal by remember { mutableStateOf("") }
    var amharicVal by remember { mutableStateOf("") }
    var afaanVal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = viewModel.getString("language_manager"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        if (isEditing && editingLoc != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit Text Key: ${editingLoc!!.stringKey}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = englishVal,
                        onValueChange = { englishVal = it },
                        label = { Text("English Value") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amharicVal,
                        onValueChange = { amharicVal = it },
                        label = { Text("Amharic Value (አማርኛ)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = afaanVal,
                        onValueChange = { afaanVal = it },
                        label = { Text("Afaan Oromoo Value") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            val loc = Localization(editingLoc!!.stringKey, englishVal, amharicVal, afaanVal)
                            viewModel.saveLocalization(loc)
                            isEditing = false
                        }) {
                            Text("Save Translation")
                        }
                    }
                }
            }
        }

        Text(
            text = "App String Localizations Directory",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(localizations) { loc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            editingLoc = loc
                            englishVal = loc.englishValue
                            amharicVal = loc.amharicValue
                            afaanVal = loc.afaanOromooValue
                            isEditing = true
                        },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Key: ${loc.stringKey}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "En: ${loc.englishValue}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Am: ${loc.amharicValue}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(text = "Af: ${loc.afaanOromooValue}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
