package com.example.neotokopos85.ui.cart

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.navigation.Screen
import com.example.neotokopos85.ui.viewmodel.CartViewModel
import androidx.compose.runtime.collectAsState

@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    navController: NavController
) {

    val items by cartViewModel.cartItems.collectAsState()

    val totalPrice = items.sumOf { item ->
        val price = item.product.prices[item.variantType] ?: 0
        price * item.quantity
    }

    Scaffold(
        bottomBar = {

            if (items.isNotEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    Text(
                        text = "Total: Rp $totalPrice",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            navController.navigate(Screen.Checkout.route)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Checkout")
                    }
                }
            }
        }
    ) { padding ->

        if (items.isEmpty()) {

            EmptyCartState()

        } else {

            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {

                items(items) { item ->

                    val price =
                        item.product.prices[item.variantType] ?: 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column {

                                Text(item.product.name)

                                Text("Varian: ${item.variantType}")

                                Text("Harga: Rp $price")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Button(
                                    onClick = {

                                        cartViewModel.decreaseQuantity(
                                            item.product,
                                            item.variantType
                                        )
                                    }
                                ) { Text("-") }

                                Text(
                                    text = item.quantity.toString(),
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )

                                Button(
                                    onClick = {

                                        cartViewModel.increaseQuantity(
                                            item.product,
                                            item.variantType
                                        )
                                    }
                                ) { Text("+") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCartState() {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "Keranjang masih kosong 🛒",
            style = MaterialTheme.typography.titleMedium
        )
    }
}