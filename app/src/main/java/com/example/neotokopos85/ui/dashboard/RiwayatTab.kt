package com.example.neotokopos85.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.data.local.entity.OrderEntity
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import com.example.neotokopos85.utils.rupiah
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun TransactionHistoryCard(
    order: OrderEntity,
    onClick: () -> Unit
) {

    val dateFormat = remember {
        SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
    }

    val timeFormat = remember {
        SimpleDateFormat("HH:mm", Locale("in", "ID"))
    }

    val date = remember(order.timestamp) {
        Date(order.timestamp)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {

                Text(
                    text = order.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${dateFormat.format(date)} • ${timeFormat.format(date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${order.totalItems} item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = rupiah(order.totalPrice.toInt()),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
@Composable
fun TransactionHistoryTab(
    orderViewModel: OrderViewModel,
    navController: NavController
) {

    val orders by orderViewModel.historyOrders.collectAsState()

    val totalCount by orderViewModel.totalTransactionCount.collectAsState()

    var selectedFilter by remember { mutableStateOf("Semua") }

    LaunchedEffect(selectedFilter) {
        orderViewModel.loadFirstHistoryPage(selectedFilter)
        orderViewModel.loadTotalTransactionCount(selectedFilter)
    }

    val now = remember { Calendar.getInstance() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf("Semua","Hari ini","Minggu ini","Bulan ini").forEach { filter ->

                val selected = selectedFilter == filter

                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(50),
                    color = if (selected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else
                        Color.Transparent,
                    border = BorderStroke(
                        1.dp,
                        if (selected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {

                    Text(
                        text = filter,
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 6.dp
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color =
                            if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "$totalCount transaksi",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val lastItem = orders.lastOrNull()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(
                items = orders,
                key = { it.id }
            ) { order ->

                if (order == lastItem) {
                    LaunchedEffect(order.id) {
                        orderViewModel.loadNextHistoryPage(selectedFilter)
                    }
                }

                TransactionHistoryCard(
                    order = order,
                    onClick = {
                        navController.navigate("transaction_detail/${order.id}")
                    }
                )
            }
        }
    }
}