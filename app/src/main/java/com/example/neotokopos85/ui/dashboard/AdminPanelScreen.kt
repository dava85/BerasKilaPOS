package com.example.neotokopos85.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.neotokopos85.ui.viewmodel.OrderViewModel

@Composable
fun AdminPanelScreen(orderViewModel: OrderViewModel) {

    // Start realtime listener
    LaunchedEffect(Unit) {
        orderViewModel.startListeningOrders()
    }

    val grossRevenue by orderViewModel.grossRevenue.collectAsState(initial = 0)
    val totalOrders by orderViewModel.totalOrders.collectAsState()
    val totalItems by orderViewModel.totalItemsSold.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.headlineMedium
        )

        // =============================
        // TOTAL REVENUE
        // =============================

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Revenue")
                Text(
                    text = "Rp $grossRevenue",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // =============================
        // TOTAL ORDERS
        // =============================

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Orders")
                Text(
                    text = "$totalOrders",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        // =============================
        // TOTAL ITEMS SOLD
        // =============================

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Items Sold")
                Text(
                    text = "$totalItems",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Realtime Data dari Firebase",
            style = MaterialTheme.typography.titleMedium
        )
    }
}