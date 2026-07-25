package com.example.neotokopos85.ui.riwayat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    orderId: String?,
    orderViewModel: OrderViewModel,
    onBackClick: () -> Unit
) {

    val orders by orderViewModel.allOrders.collectAsState()
    val order = orders.find { it.id == orderId }

    if (order == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Transaksi tidak ditemukan")
        }
        return
    }

    val currentOrder = order

    val totalDiscount =
        currentOrder.items.sumOf { (it.discount ?: 0).toInt() }

    val dateFormat =
        SimpleDateFormat("dd MMM yyyy HH:mm", Locale("in","ID"))

    Scaffold(

        topBar = {

            TopAppBar(
                windowInsets = WindowInsets(0),
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)

        ) {

            // ================= INFO =================

            item {

                Column {

                    InfoRow(
                        "Tanggal",
                        dateFormat.format(Date(currentOrder.timestamp))
                    )

                    Spacer(Modifier.height(6.dp))

                    InfoRow(
                        "Nama",
                        currentOrder.customerName
                    )

                    Spacer(Modifier.height(6.dp))

                    InfoRow(
                        "Alamat",
                        currentOrder.customerAddress
                    )

                    Spacer(Modifier.height(6.dp))

                    InfoRow(
                        "Total Item",
                        currentOrder.totalItems.toString()
                    )

                    Spacer(Modifier.height(10.dp))

                    HorizontalDivider()
                }
            }

            // ================= HEADER =================

            item {

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Produk",
                        modifier = Modifier.weight(2f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Qty",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Subtotal",
                        modifier = Modifier.weight(1.2f),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Diskon",
                        modifier = Modifier.weight(1.2f),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))

                HorizontalDivider()
            }

            // ================= LIST ITEM =================

            itemsIndexed(currentOrder.items) { index, item ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {

                    Column(
                        modifier = Modifier.weight(2f)
                    ) {

                        Text(item.productName)

                        Text(
                            item.variantKg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        item.quantity.toString(),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        "Rp %,d".format(item.subtotal.toInt()),
                        modifier = Modifier.weight(1.2f)
                    )

                    Text(
                        "- Rp %,d".format(item.discount.toInt()),
                        modifier = Modifier.weight(1.2f),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                HorizontalDivider(thickness = 0.3.dp)
            }

            // ================= TOTAL =================

            item {

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text("Total Diskon")

                    Text(
                        "- Rp %,d".format(totalDiscount),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        "TOTAL BAYAR",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Rp %,d".format(currentOrder.totalPrice.toInt()),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun InfoRow(title: String, value: String) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            title,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            value,
            fontWeight = FontWeight.Medium
        )
    }
}