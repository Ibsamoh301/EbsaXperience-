package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Order
import com.example.data.OrderStatus

@Composable
fun DriverDashboard(viewModel: BeysaViewModel) {
    val activeTab by viewModel.driverTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding()) {
                NavigationBarItem(
                    selected = activeTab == "deliveries" || activeTab == "active_delivery",
                    onClick = { viewModel.setDriverTab("deliveries") },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null) },
                    label = { Text("Active Deliveries") }
                )
                NavigationBarItem(
                    selected = activeTab == "history",
                    onClick = { viewModel.setDriverTab("history") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Delivery History") }
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { viewModel.setDriverTab("profile") },
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
                "deliveries" -> DriverDeliveriesScreen(viewModel)
                "active_delivery" -> DriverActiveDeliveryScreen(viewModel)
                "history" -> DriverHistoryScreen(viewModel)
                "profile" -> CustomerProfileScreen(viewModel) // Re-use profile view
            }
        }
    }
}

@Composable
fun DriverDeliveriesScreen(viewModel: BeysaViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Driver's assigned orders that are not finished
    val assignedList = orders.filter {
        it.assignedDriverUsername == currentUser?.username &&
                (it.status == OrderStatus.ASSIGNED || it.status == OrderStatus.PENDING)
    }

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
            Column {
                Text(
                    text = viewModel.getString("driver_dashboard"),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Welcome, ${currentUser?.name ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.LocalShipping,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = viewModel.getString("assigned_deliveries"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        if (assignedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Great! You have no pending deliveries assigned.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(assignedList) { order ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Order #${order.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = order.status.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = order.deliveryAddress,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountBox,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Customer Username: ${order.customerUsername}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Text(
                                text = "Total Amount to Collect: ${order.totalAmount} ETB",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    viewModel.selectOrder(order)
                                    viewModel.setDriverTab("active_delivery")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("start_delivery_btn_${order.id}"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Navigate & Complete Delivery", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverActiveDeliveryScreen(viewModel: BeysaViewModel) {
    val order by viewModel.selectedOrder.collectAsState()
    if (order == null) return

    val item = order!!

    // Signature paths
    val signaturePoints = remember { mutableStateListOf<Offset>() }
    var proofPinEntered by remember { mutableStateOf("") }
    var isPhotoTaken by remember { mutableStateOf(false) }
    var showProofModal by remember { mutableStateOf(false) }
    var selectedProofType by remember { mutableStateOf("Signature") } // "Signature", "PIN", "Photo"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { viewModel.setDriverTab("deliveries") }) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
            Text(
                text = "Order #${item.id} Navigation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // SIMULATED GOOGLE MAPS CANVAS
        // Let's draw an awesome interactive looking map!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw grid/streets representing Dadar, Ethiopia
                    drawRect(color = Color(0xFFE8F5E9)) // Light green fields
                    // Streets
                    drawLine(color = Color.White, start = Offset(0f, 150f), end = Offset(size.width, 150f), strokeWidth = 30f)
                    drawLine(color = Color.White, start = Offset(0f, 350f), end = Offset(size.width, 350f), strokeWidth = 30f)
                    drawLine(color = Color.White, start = Offset(200f, 0f), end = Offset(200f, size.height), strokeWidth = 30f)
                    drawLine(color = Color.White, start = Offset(500f, 0f), end = Offset(500f, size.height), strokeWidth = 30f)

                    // Draw route path
                    val routePath = Path().apply {
                        moveTo(200f, 350f)
                        lineTo(500f, 350f)
                        lineTo(500f, 150f)
                    }
                    drawPath(path = routePath, color = Color(0xFF1E88E5), style = Stroke(width = 10f))

                    // Draw Beysa Farm start pin
                    drawCircle(color = Color.DarkGray, radius = 15f, center = Offset(200f, 350f))
                    // Draw Customer delivery destination pin
                    drawCircle(color = Color.Red, radius = 15f, center = Offset(500f, 150f))
                }

                // UI elements overlapping on the map
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Route to Dadar Customer Residence (4.2 km)",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Turn Right onto Dadar Main St",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Contact Customer Row
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recipient: ${item.customerUsername}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Address: ${item.deliveryAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { /* Simulated call */ }) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { /* Simulated SMS */ }) {
                        Icon(Icons.Default.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Divider()

        // Capture Proof interface selector
        Text(
            text = "Select Delivery Verification Method:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Signature", "PIN", "Camera").forEach { type ->
                val isSelected = selectedProofType == type
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { selectedProofType = type },
                    label = { Text(type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Proof Capture Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            when (selectedProofType) {
                "Signature" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Have customer sign in the box below:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { signaturePoints.clear() }) {
                                Text("Clear")
                            }
                        }

                        // Drawing Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        signaturePoints.add(change.position)
                                    }
                                }
                        ) {
                            if (signaturePoints.size > 1) {
                                for (i in 0 until signaturePoints.size - 1) {
                                    drawLine(
                                        color = Color.Black,
                                        start = signaturePoints[i],
                                        end = signaturePoints[i + 1],
                                        strokeWidth = 5f
                                    )
                                }
                            }
                        }
                    }
                }

                "PIN" -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Pin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Customer security PIN verification:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = proofPinEntered,
                            onValueChange = { proofPinEntered = it },
                            placeholder = { Text("Enter customer 4-digit PIN") },
                            modifier = Modifier.width(200.dp)
                        )
                    }
                }

                "Camera" -> {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isPhotoTaken) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (isPhotoTaken) Color.Green else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = if (isPhotoTaken) "Proof Photo Captured Successfully!" else "Snap a picture of the agricultural package delivery:",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )

                        Button(onClick = { isPhotoTaken = true }) {
                            Text(if (isPhotoTaken) "Retake Photo" else "Snap Proof Photo")
                        }
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    viewModel.updateOrderStatus(item.id, OrderStatus.CANCELLED)
                    viewModel.setDriverTab("deliveries")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel Delivery", color = Color.Red)
            }

            Button(
                onClick = {
                    // Save delivery status
                    val signatureStr = if (signaturePoints.isNotEmpty()) "signature_captured" else null
                    val pinStr = if (proofPinEntered.isNotEmpty()) proofPinEntered else null
                    val photoStr = if (isPhotoTaken) "photo_taken" else null

                    viewModel.updateOrderStatus(
                        orderId = item.id,
                        status = OrderStatus.DELIVERED,
                        photo = photoStr,
                        sig = signatureStr,
                        pin = pinStr
                    )
                    viewModel.setDriverTab("deliveries")
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("complete_delivery_btn"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Done, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Complete", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DriverHistoryScreen(viewModel: BeysaViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Filter successful runs
    val history = orders.filter {
        it.assignedDriverUsername == currentUser?.username && it.status == OrderStatus.DELIVERED
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Delivery History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No finished deliveries logged.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { order ->
                    Card(
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Order #${order.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = "Delivered",
                                    color = Color.Green,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Delivered to: ${order.deliveryAddress}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Collected: ${order.totalAmount} ETB",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
