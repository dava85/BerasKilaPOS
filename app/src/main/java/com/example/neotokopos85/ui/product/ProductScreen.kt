package com.example.neotokopos85.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.viewmodel.ProductViewModel
import com.example.neotokopos85.ui.viewmodel.InventoryViewModel
import androidx.compose.runtime.*
import com.example.neotokopos85.ui.components.Alert
import com.example.neotokopos85.ui.components.Confirm
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    navController: NavController,
    inventoryViewModel: InventoryViewModel
) {

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Tambah Produk", "Daftar Produk")

    var name by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }

    var pricePcs by remember { mutableStateOf("") }
    var priceKg by remember { mutableStateOf("") }
    var priceHalfKg by remember { mutableStateOf("") }
    var priceLiter by remember { mutableStateOf("") }
    var priceKarung5 by remember { mutableStateOf("") }
    var priceKarung10 by remember { mutableStateOf("") }
    var priceKarung25 by remember { mutableStateOf("") }

    val inventories by inventoryViewModel
        .inventoryList
        .collectAsState(initial = emptyList())

    var selectedInventoryId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var editingId by remember { mutableStateOf<String?>(null) }

    val products by viewModel.products.collectAsState(initial = emptyList())

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri?.toString() ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = { Text("Admin Produk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {

                /**
                 * =========================
                 * FORM
                 * =========================
                 */
                0 -> {

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        Text(
                            text =
                                if (editingId == null)
                                    "Tambah Produk"
                                else
                                    "Edit Produk",
                            style = MaterialTheme.typography.titleLarge
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nama Produk") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        /**
                         * INVENTORY
                         */
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {

                            OutlinedTextField(
                                value = inventories
                                    .firstOrNull { it.id == selectedInventoryId }
                                    ?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Pilih Inventory") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {

                                inventories.forEach { inventory ->

                                    DropdownMenuItem(
                                        text = { Text(inventory.name) },
                                        onClick = {
                                            selectedInventoryId = inventory.id
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(
                            "Harga",
                            style = MaterialTheme.typography.labelMedium
                        )

                        OutlinedTextField(
                            value = pricePcs,
                            onValueChange = { pricePcs = it },
                            label = { Text("Harga PCS") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceKg,
                            onValueChange = { priceKg = it },
                            label = { Text("Harga Kg") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = priceHalfKg,
                            onValueChange = { priceHalfKg = it },
                            label = { Text("Harga 0.5 Kg") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceLiter,
                            onValueChange = { priceLiter = it },
                            label = { Text("Harga Liter") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceKarung5,
                            onValueChange = { priceKarung5 = it },
                            label = { Text("Harga Karung 5") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceKarung10,
                            onValueChange = { priceKarung10 = it },
                            label = { Text("Harga Karung 10") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = priceKarung25,
                            onValueChange = { priceKarung25 = it },
                            label = { Text("Harga Karung 25") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {

                                if (name.isBlank()) {
                                    Alert.error("Nama produk harus diisi")
                                    return@Button
                                }

                                if (selectedInventoryId.isBlank()) {
                                    Alert.error("Pilih inventory terlebih dahulu")
                                    return@Button
                                }

                                val prices = mutableMapOf<String, Int>()

                                pricePcs.toIntOrNull()?.let { prices["pcs"] = it }
                                priceKg.toIntOrNull()?.let { prices["kg"] = it }
                                priceHalfKg.toIntOrNull()?.let { prices["0.5kg"] = it }
                                priceLiter.toIntOrNull()?.let { prices["liter"] = it }
                                priceKarung5.toIntOrNull()?.let { prices["karung5"] = it }
                                priceKarung10.toIntOrNull()?.let { prices["karung10"] = it }
                                priceKarung25.toIntOrNull()?.let { prices["karung25"] = it }

                                Alert.loading(true)

                                if (editingId == null) {

                                    viewModel.addProduct(
                                        name = name,
                                        category = "beras",
                                        imageUrl = imageUri,
                                        prices = prices,
                                        inventoryId = selectedInventoryId
                                    )

                                    Alert.success("Produk berhasil ditambahkan")

                                } else {

                                    viewModel.updateProduct(
                                        id = editingId!!,
                                        name = name,
                                        category = "beras",
                                        imageUrl = imageUri,
                                        prices = prices,
                                        inventoryId = selectedInventoryId
                                    )

                                    Alert.success("Produk berhasil diupdate")

                                    editingId = null
                                }

                                Alert.loading(false)

                                name = ""
                                imageUri = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                if (editingId == null)
                                    "Simpan Produk"
                                else
                                    "Update Produk"
                            )
                        }
                    }
                }

                /**
                 * =========================
                 * LIST PRODUK
                 * =========================
                 */
                1 -> {

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {

                        items(
                            items = products,
                            key = { it.id }
                        ) { product ->

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {

                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {

                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val inventory = inventories.firstOrNull {
                                        it.id == product.inventoryId
                                    }

                                    inventory?.let {

                                        Text(
                                            text = "Inventory : ${it.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Divider()

                                    Text(
                                        "Harga",
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {

                                        product.prices["pcs"]?.let {
                                            Text("PCS : Rp $it")
                                        }

                                        product.prices["kg"]?.let {
                                            Text("Kg : Rp $it")
                                        }

                                        product.prices["0.5kg"]?.let {
                                            Text("0.5kg : Rp $it")
                                        }

                                        product.prices["liter"]?.let {
                                            Text("Liter : Rp $it")
                                        }

                                        product.prices["karung5"]?.let {
                                            Text("Karung 5 : Rp $it")
                                        }

                                        product.prices["karung10"]?.let {
                                            Text("Karung 10 : Rp $it")
                                        }

                                        product.prices["karung25"]?.let {
                                            Text("Karung 25 : Rp $it")
                                        }
                                    }

                                    Divider()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {

                                        OutlinedButton(
                                            onClick = {

                                                editingId = product.id

                                                name = product.name
                                                imageUri = product.imageUrl
                                                selectedInventoryId = product.inventoryId

                                                pricePcs = product.prices["pcs"]?.toString() ?: ""
                                                priceKg = product.prices["kg"]?.toString() ?: ""
                                                priceHalfKg = product.prices["0.5kg"]?.toString() ?: ""
                                                priceLiter = product.prices["liter"]?.toString() ?: ""
                                                priceKarung5 = product.prices["karung5"]?.toString() ?: ""
                                                priceKarung10 = product.prices["karung10"]?.toString() ?: ""
                                                priceKarung25 = product.prices["karung25"]?.toString() ?: ""

                                                selectedTab = 0
                                            }
                                        ) {
                                            Text("Edit")
                                        }

                                        OutlinedButton(
                                            onClick = {

                                                Confirm.delete {

                                                    Alert.loading(true)

                                                    viewModel.deleteProduct(product.id)

                                                    Alert.loading(false)

                                                    Alert.success("Produk berhasil dihapus")
                                                }
                                            }
                                        ) {
                                            Text("Hapus")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}