package com.example.neotokopos85.ui.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.viewmodel.CartViewModel

@Composable
fun ManagementScreen(
    cartViewModel: CartViewModel,
    navController: NavController
) {

    val soldItems by cartViewModel.cartItems.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Management / Penjualan",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {

            items(soldItems) { item ->

                val price =
                    item.product.prices[item.variantType] ?: 0

                val subtotal =
                    price * item.quantity

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(text = item.product.name)

                        Text(text = "Varian: ${item.variantType}")

                        Text(text = "Qty: ${item.quantity}")

                        Text(text = "Harga: Rp $price")

                        Text(text = "Subtotal: Rp $subtotal")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Cetak Nota */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cetak Nota")
        }
    }
}