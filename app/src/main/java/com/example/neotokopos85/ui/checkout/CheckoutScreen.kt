package com.example.neotokopos85.ui.checkout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.viewmodel.CartViewModel
import com.example.neotokopos85.ui.navigation.Screen
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import com.example.neotokopos85.data.local.entity.OrderItem

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    navController: NavController,
    orderViewModel: OrderViewModel
) {

    val cartItems by cartViewModel.cartItems.collectAsState()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val totalPrice = cartViewModel.getTotalPrice()
    val totalItems = cartViewModel.getTotalItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // =============================
        // CART LIST
        // =============================

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(cartItems) { item ->

                val price =
                    item.product.prices[item.variantType] ?: 0

                Text(
                    "${item.product.name} - ${item.variantType} x${item.quantity} (Rp $price)"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Total: Rp $totalPrice")

        Spacer(modifier = Modifier.height(16.dp))

        // =============================
        // INPUT NAME
        // =============================

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = null
            },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // =============================
        // INPUT ADDRESS
        // =============================

        OutlinedTextField(
            value = address,
            onValueChange = {
                address = it
                errorMessage = null
            },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        // =============================
        // ERROR MESSAGE
        // =============================

        errorMessage?.let {

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // =============================
        // PLACE ORDER BUTTON
        // =============================

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                when {

                    cartItems.isEmpty() -> {
                        errorMessage = "Cart is empty!"
                    }

                    name.isBlank() -> {
                        errorMessage = "Please enter your name"
                    }

                    address.isBlank() -> {
                        errorMessage = "Please enter your address"
                    }

                    else -> {

                        // 🔥 Convert CartItem → OrderItem
                        val orderItems = cartItems.map { cartItem ->

                            val price =
                                cartItem.product.prices[cartItem.variantType] ?: 0

                            val subtotal =
                                price * cartItem.quantity

                            OrderItem(
                                productName = cartItem.product.name,
                                variantKg = cartItem.variantType,
                                quantity = cartItem.quantity,
                                subtotal = subtotal.toDouble()
                            )
                        }

                        orderViewModel.saveOrder(
                            totalPrice = totalPrice.toInt(),
                            totalItems = totalItems,
                            name = name,
                            address = address,
                            items = orderItems
                        )

                        cartViewModel.clearCart()

                        navController.navigate(Screen.Success.route) {
                            popUpTo(0)
                        }
                    }
                }
            }
        ) {
            Text("Place Order")
        }
    }
}