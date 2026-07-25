package com.example.neotokopos85.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderHistoryScreen(
    orderViewModel: OrderViewModel,
    navController: NavController
) {

    val orders = orderViewModel.allOrders.collectAsState(initial = emptyList()).value

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        items(orders) { order ->

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text("Customer: ${order.customerName}")
                    Text("Total: Rp ${order.totalPrice}")
                    Text("Items: ${order.totalItems}")

                    val formattedDate = SimpleDateFormat(
                        "dd MMM yyyy HH:mm",
                        Locale.getDefault()
                    ).format(Date(order.timestamp))

                    Text("Date: $formattedDate")
                }
            }
        }
    }
}