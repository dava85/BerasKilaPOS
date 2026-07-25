package com.example.neotokopos85.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.neotokopos85.ui.viewmodel.ProductViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    productId: String,
    productViewModel: ProductViewModel,
    navController: NavController
) {

    val products by productViewModel.products.collectAsState()
    val product = products.find { it.id == productId }

    val currencyFormatter =
        NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        product?.let {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    AsyncImage(
                        model = it.imageUrl,
                        contentDescription = it.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = it.name,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 Harga berdasarkan varian
                Text(
                    text = "Harga Kg: ${
                        currencyFormatter.format(
                            it.prices["kg"] ?: 0
                        )
                    }"
                )

                Text(
                    text = "Harga Liter: ${
                        currencyFormatter.format(
                            it.prices["liter"] ?: 0
                        )
                    }"
                )

                Text(
                    text = "Karung 5 Kg: ${
                        currencyFormatter.format(
                            it.prices["karung5"] ?: 0
                        )
                    }"
                )

                Text(
                    text = "Karung 10 Kg: ${
                        currencyFormatter.format(
                            it.prices["karung10"] ?: 0
                        )
                    }"
                )

                Text(
                    text = "Karung 25 Kg: ${
                        currencyFormatter.format(
                            it.prices["karung25"] ?: 0
                        )
                    }"
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Kembali")
                }
            }
        }
    }
}