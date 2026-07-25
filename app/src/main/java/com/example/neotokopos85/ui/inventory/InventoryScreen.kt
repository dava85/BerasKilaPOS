package com.example.neotokopos85.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.neotokopos85.ui.viewmodel.InventoryViewModel
import com.example.neotokopos85.data.local.entity.InventoryEntity
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.neotokopos85.ui.components.Alert
import com.example.neotokopos85.ui.components.Confirm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBackClick: () -> Unit,
    viewModel: InventoryViewModel,
    highlightId: String? = null
) {

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0.dp),
                title = { Text("Inventory") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            TabRow(selectedTabIndex = selectedTab) {

                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("List") }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Tambah") }
                )
            }

            when (selectedTab) {
                0 -> InventoryList(viewModel, highlightId)
                1 -> InventoryForm(viewModel)
            }
        }
    }
}

@Composable
fun InventoryList(
    viewModel: InventoryViewModel,
    highlightId: String?
) {

    val inventory by viewModel.inventoryList.collectAsState()
    val listState = rememberLazyListState()

    var selectedItem by remember { mutableStateOf<InventoryEntity?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }

    var showRestockDialog by remember { mutableStateOf(false) }

    LaunchedEffect(inventory, highlightId) {

        val index = inventory.indexOfFirst { it.id == highlightId }

        if (index >= 0) {
            listState.animateScrollToItem(index)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        items(
            items = inventory,
            key = { it.id }
        ) { item ->

            val totalStock = item.variants.values.sum()

            val status =
                when {
                    totalStock == 0 -> "EMPTY"
                    totalStock <= 3 -> "CRITICAL"
                    totalStock <= 8 -> "LOW"
                    else -> null
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor =
                        if (item.id == highlightId)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )

                        status?.let {
                            BadgeStock(it)
                        }
                    }

                    Divider()

                    Text(
                        "Stok",
                        style = MaterialTheme.typography.labelMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column {

                            item.variants.forEach { (variant, _) ->
                                Text(variant)
                            }

                            if (item.hasKarung25() && item.eceranStockKg > 0)
                                Text("Eceran")
                        }

                        Column(horizontalAlignment = Alignment.End) {

                            item.variants.forEach { (_, stock) ->
                                Text(stock.toString())
                            }

                            if (item.hasKarung25() && item.eceranStockKg > 0)
                                Text("%.1f Kg".format(item.eceranStockKg))
                        }
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        OutlinedButton(
                            onClick = {
                                selectedItem = item
                                showRestockDialog = true
                            }
                        ) {
                            Text("Restock")
                        }

                        OutlinedButton(
                            onClick = {
                                selectedItem = item
                                showEditDialog = true
                            }
                        ) {
                            Text("Edit")
                        }

                        OutlinedButton(
                            onClick = {

                                Confirm.delete {

                                    Alert.loading(true)

                                    viewModel.deleteInventory(item.id)

                                    Alert.loading(false)

                                    Alert.success("Inventory berhasil dihapus")
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

    if (showRestockDialog && selectedItem != null) {

        var pcs by remember { mutableStateOf("") }
        var karung25 by remember { mutableStateOf("") }
        var karung10 by remember { mutableStateOf("") }
        var karung5 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRestockDialog = false },

            confirmButton = {
                TextButton(
                    onClick = {

                        viewModel.restockVariant(
                            selectedItem!!.id,
                            "pcs",
                            pcs.toIntOrNull() ?: 0
                        )

                        viewModel.restockVariant(
                            selectedItem!!.id,
                            "karung25",
                            karung25.toIntOrNull() ?: 0
                        )

                        viewModel.restockVariant(
                            selectedItem!!.id,
                            "karung10",
                            karung10.toIntOrNull() ?: 0
                        )

                        viewModel.restockVariant(
                            selectedItem!!.id,
                            "karung5",
                            karung5.toIntOrNull() ?: 0
                        )

                        Alert.success("Restock berhasil")

                        showRestockDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showRestockDialog = false }
                ) {
                    Text("Batal")
                }
            },

            title = {
                Text("Restock ${selectedItem!!.name}")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = pcs,
                        onValueChange = { pcs = it },
                        label = { Text("Tambah PCS") }
                    )

                    OutlinedTextField(
                        value = karung25,
                        onValueChange = { karung25 = it },
                        label = { Text("Tambah Karung 25") }
                    )

                    OutlinedTextField(
                        value = karung10,
                        onValueChange = { karung10 = it },
                        label = { Text("Tambah Karung 10") }
                    )

                    OutlinedTextField(
                        value = karung5,
                        onValueChange = { karung5 = it },
                        label = { Text("Tambah Karung 5") }
                    )
                }
            }
        )
    }

    if (showEditDialog && selectedItem != null) {

        var pcs by remember { mutableStateOf(selectedItem!!.getStock("pcs").toString()) }
        var eceran by remember { mutableStateOf(selectedItem!!.eceranStockKg.toString()) }
        var karung25 by remember { mutableStateOf(selectedItem!!.getStock("karung25").toString()) }
        var karung10 by remember { mutableStateOf(selectedItem!!.getStock("karung10").toString()) }
        var karung5 by remember { mutableStateOf(selectedItem!!.getStock("karung5").toString()) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },

            confirmButton = {
                TextButton(
                    onClick = {

                        viewModel.updateStock(
                            id = selectedItem!!.id,
                            pcs = pcs.toIntOrNull() ?: 0,
                            eceran = eceran.toDoubleOrNull() ?: 0.0,
                            karung25 = karung25.toIntOrNull() ?: 0,
                            karung10 = karung10.toIntOrNull() ?: 0,
                            karung5 = karung5.toIntOrNull() ?: 0
                        )

                        Alert.success("Stok berhasil diperbarui")

                        showEditDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Batal")
                }
            },

            title = {
                Text("Edit Stok ${selectedItem!!.name}")
            },

            text = {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedTextField(
                        value = pcs,
                        onValueChange = { pcs = it },
                        label = { Text("PCS") }
                    )

                    OutlinedTextField(
                        value = eceran,
                        onValueChange = { eceran = it },
                        label = { Text("Eceran Kg") }
                    )

                    OutlinedTextField(
                        value = karung25,
                        onValueChange = { karung25 = it },
                        label = { Text("Karung 25") }
                    )

                    OutlinedTextField(
                        value = karung10,
                        onValueChange = { karung10 = it },
                        label = { Text("Karung 10") }
                    )

                    OutlinedTextField(
                        value = karung5,
                        onValueChange = { karung5 = it },
                        label = { Text("Karung 5") }
                    )
                }
            }
        )
    }
}

@Composable
fun InventoryForm(viewModel: InventoryViewModel) {

    var name by remember { mutableStateOf("") }
    var pcs by remember { mutableStateOf("") }
    var eceranStock by remember { mutableStateOf("") }
    var karung25 by remember { mutableStateOf("") }
    var karung10 by remember { mutableStateOf("") }
    var karung5 by remember { mutableStateOf("") }
    var kgPerLiter by remember { mutableStateOf("0.7") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Text(
            text = "Tambah Inventory",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Barang") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = pcs,
            onValueChange = { pcs = it },
            label = { Text("Stok PCS") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = eceranStock,
            onValueChange = { eceranStock = it },
            label = { Text("Stok Eceran (Kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = karung25,
            onValueChange = { karung25 = it },
            label = { Text("Stok Karung 25") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = karung10,
            onValueChange = { karung10 = it },
            label = { Text("Stok Karung 10") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = karung5,
            onValueChange = { karung5 = it },
            label = { Text("Stok Karung 5") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                if (name.isBlank()) {
                    Alert.error("Nama inventory harus diisi")
                    return@Button
                }

                val variants = mutableMapOf<String, Int>()

                pcs.toIntOrNull()?.takeIf { it > 0 }?.let { variants["pcs"] = it }
                karung25.toIntOrNull()?.takeIf { it > 0 }?.let { variants["karung25"] = it }
                karung10.toIntOrNull()?.takeIf { it > 0 }?.let { variants["karung10"] = it }
                karung5.toIntOrNull()?.takeIf { it > 0 }?.let { variants["karung5"] = it }

                viewModel.addInventory(
                    name = name,
                    variants = variants,
                    eceranStockKg = eceranStock.toDoubleOrNull() ?: 0.0,
                    kgPerLiter = kgPerLiter.toDoubleOrNull() ?: 0.7
                )

                Alert.success("Inventory berhasil ditambahkan")

                name = ""
                pcs = ""
                eceranStock = ""
                karung25 = ""
                karung10 = ""
                karung5 = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan")
        }
    }
}

@Composable
fun BadgeStock(status: String) {

    val color =
        when (status) {
            "CRITICAL" -> MaterialTheme.colorScheme.error
            "LOW" -> Color(0xFFFF9800)
            "EMPTY" -> Color.Red
            else -> Color.Gray
        }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.12f)
    ) {

        Text(
            text = status,
            color = color,
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 2.dp
            ),
            style = MaterialTheme.typography.labelSmall
        )
    }
}