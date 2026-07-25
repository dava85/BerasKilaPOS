package com.example.neotokopos85.ui.dashboard

import android.widget.Toast
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.neotokopos85.ui.viewmodel.*
import com.example.neotokopos85.data.local.entity.InventoryEntity
import com.example.neotokopos85.data.firebase.ProductFirestore
import com.example.neotokopos85.ui.theme.WhiteTransparent
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.neotokopos85.data.local.entity.OrderItem
import com.example.neotokopos85.ui.components.Alert
import com.example.neotokopos85.ui.components.Confirm
import com.example.neotokopos85.utils.rupiah
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures


@Composable
fun KasirTab(
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    inventoryViewModel: InventoryViewModel
) {

    var showDialog by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    var showCartDetail by remember { mutableStateOf(false) }

    val products by productViewModel.products.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val inventories by inventoryViewModel.inventoryList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(
                    bottom = if (cartItems.isNotEmpty()) 75.dp else 0.dp
                )
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari produk...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WhiteTransparent,
                    unfocusedContainerColor = WhiteTransparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Box(Modifier.fillMaxSize()) {

                // EMPTY STATE
                androidx.compose.animation.AnimatedVisibility(
                    visible = filteredProducts.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Produk tidak ditemukan")
                    }
                }

                // LIST
                androidx.compose.animation.AnimatedVisibility(
                    visible = filteredProducts.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {

                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(24.dp)
                    ) {

                        val sortedProducts = remember(filteredProducts, inventories) {
                            filteredProducts.sortedBy { product ->
                                val inv = inventories.find { it.id == product.inventoryId }

                                if (inv != null) {
                                    ((inv.variants["karung25"] ?: 0) * 25) +
                                            ((inv.variants["karung10"] ?: 0) * 10) +
                                            ((inv.variants["karung5"] ?: 0) * 5) +
                                            inv.eceranStockKg
                                } else 0.0
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp)
                        ) {

                            itemsIndexed(sortedProducts) { index, product ->

                                val cardColor = if (index % 2 == 0) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    Color(0xFFF3F3F3)
                                }

                                val inventory =
                                    inventories.find { it.id == product.inventoryId }

                                val totalKarung =
                                    (inventory?.variants?.get("karung25") ?: 0) +
                                            (inventory?.variants?.get("karung10") ?: 0) +
                                            (inventory?.variants?.get("karung5") ?: 0)

                                val stockStatus = when {
                                    totalKarung <= 5 -> "LOW"
                                    totalKarung <= 15 -> "MEDIUM"
                                    else -> "NORMAL"
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    elevation = CardDefaults.cardElevation(1.5.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = cardColor
                                    )
                                ) {

                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        ProductTableRow(
                                            product = product,
                                            inventories = inventories,
                                            cartItems = cartItems,
                                            cartViewModel = cartViewModel,
                                            stockStatus = stockStatus
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (cartItems.isNotEmpty()) {

            val total = cartItems.sumOf {

                val price = it.manualPrice ?: (it.product.prices[it.variantType] ?: 0)
                val subtotal = price * it.quantity

                subtotal - it.discount
            }

            val itemCount = cartItems.sumOf { it.quantity }

            var lastTap by remember { mutableStateOf(0L) }

            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                val now = System.currentTimeMillis()
                                if (now - lastTap > 300) { // debounce
                                    lastTap = now
                                    showCartDetail = true
                                }
                            }
                        )
                    },
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // kiri
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$itemCount item")
                        Text(rupiah(total), color = MaterialTheme.colorScheme.primary)
                    }

                    // kanan (aman, tidak ketabrak)
                    Button(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text("Konfirmasi")
                    }
                }
            }
        }
        @OptIn(ExperimentalMaterial3Api::class)
        if (showCartDetail) {

            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            ModalBottomSheet(
                onDismissRequest = { showCartDetail = false },
                sheetState = sheetState
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {

                    Text(
                        text = "Rincian Pembelian",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {

                        items(
                            items = cartItems,
                            key = { it.product.id + it.variantType }
                        ) { cartItem ->

                            val price = cartItem.manualPrice
                                ?: (cartItem.product.prices[cartItem.variantType] ?: 0)

                            val subtotal = price * cartItem.quantity

                            var discountText by remember {
                                mutableStateOf(
                                    if (cartItem.discount == 0) ""
                                    else cartItem.discount.toString()
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    Column {

                                        Text(cartItem.product.name)

                                        val variantLabel =
                                            if (cartItem.variantType == "nominal")
                                                "${cartItem.variantWeight} kg"
                                            else
                                                cartItem.variantType

                                        Text(
                                            "$variantLabel x ${cartItem.quantity}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End
                                    ) {

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {

                                            Text(rupiah(subtotal))

                                            IconButton(
                                                onClick = {

                                                    cartViewModel.removeItem(
                                                        cartItem.product,
                                                        cartItem.variantType
                                                    )

                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Hapus Item"
                                                )
                                            }
                                        }

                                        if (cartItem.discount > 0) {

                                            Text(
                                                "- ${rupiah(cartItem.discount)}",
                                                color = MaterialTheme.colorScheme.error,
                                                style = MaterialTheme.typography.bodySmall
                                            )

                                            Text(
                                                rupiah(subtotal - cartItem.discount),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = discountText,
                                    onValueChange = { value ->

                                        val numbersOnly = value.filter { it.isDigit() }

                                        discountText = numbersOnly

                                        val discountInt = numbersOnly.toIntOrNull() ?: 0

                                        cartViewModel.updateDiscount(
                                            cartItem.product,
                                            cartItem.variantType,
                                            discountInt
                                        )
                                    },
                                    label = { Text("Diskon") },
                                    prefix = { Text("Rp ") },
                                    singleLine = true,
                                    modifier = Modifier.width(150.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Divider()
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val totalDiscount = cartItems.sumOf { it.discount }

                    val finalTotal = cartItems.sumOf {

                        val price =
                            it.manualPrice ?: (it.product.prices[it.variantType] ?: 0)
                        val subtotal = price * it.quantity

                        subtotal - it.discount
                    }

                    Text(
                        text = "Total Kotor: ${
                            rupiah(
                                cartItems.sumOf {
                                    val price =
                                        it.manualPrice ?: (it.product.prices[it.variantType] ?: 0)
                                    price * it.quantity
                                })
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Total Diskon: ${rupiah(totalDiscount)}",
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "Total Bayar: ${rupiah(finalTotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showCartDetail = false
                            showDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Konfirmasi Transaksi")
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
    LaunchedEffect(showDialog) {

        if (showDialog) {

            Confirm.title = "Konfirmasi Transaksi"
            Confirm.message = "Simpan transaksi ini?"

            Confirm.onConfirm = {

                val totalPrice = cartItems.sumOf { item ->
                    val price = item.manualPrice ?: (item.product.prices[item.variantType] ?: 0)
                    val subtotal = price * item.quantity
                    subtotal - item.discount
                }

                val totalItems = cartItems.sumOf { it.quantity }

                inventoryViewModel.reduceStockAfterPayment(
                    cartItems = cartItems,

                    onSuccess = {

                        val orderItems = cartItems.map { cartItem ->

                            val price =
                                cartItem.manualPrice
                                    ?: (cartItem.product.prices[cartItem.variantType] ?: 0)

                            val subtotal = (price * cartItem.quantity).toDouble()

                            OrderItem(
                                productName = cartItem.product.name,
                                variantKg = cartItem.variantType,
                                quantity = cartItem.quantity,
                                subtotal = subtotal,
                                discount = cartItem.discount.toDouble()
                            )
                        }

                        orderViewModel.saveOrder(
                            totalPrice = totalPrice,
                            totalItems = totalItems,
                            name = "Customer",
                            address = "-",
                            items = orderItems
                        )

                        cartViewModel.clearCart()

                        showSuccess = true
                    },

                    onFailure = { error: String ->

                        showErrorMessage = error
                    }
                )
            }

            Confirm.show = true

            showDialog = false
        }
    }



    LaunchedEffect(showErrorMessage) {

        showErrorMessage?.let { error ->

            Alert.error(error)

            showErrorMessage = null
        }
    }



    if (showSuccess) {

        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSuccess = false
        }

        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            androidx.compose.animation.AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp,
                    color = MaterialTheme.colorScheme.primary
                ) {

                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        AnimatedSuccessCheck(
                            modifier = Modifier.size(80.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Pembelian Berhasil!",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTableRow(
    product: ProductFirestore,
    inventories: List<InventoryEntity>,
    cartItems: List<CartItem>,
    cartViewModel: CartViewModel,
    stockStatus: String
) {

    var expanded by remember { mutableStateOf(false) }
    var showNominalSheet by remember { mutableStateOf(false) }

    val inventory = inventories.firstOrNull {
        it.id == product.inventoryId
    }

    val kgPerLiter = inventory?.kgPerLiter ?: 0.7

    val karung25 = inventory?.variants?.get("karung25") ?: 0
    val eceran = inventory?.eceranStockKg ?: 0.0

    // ======================
    // HITUNG ECERAN REALTIME
    // ======================

    val cartKg = cartItems
        .filter { it.product.inventoryId == product.inventoryId }
        .sumOf {

            when (it.variantType) {

                "kg" -> it.quantity.toDouble()

                "0.5kg" -> it.quantity * 0.5

                "liter" -> it.quantity * kgPerLiter

                "nominal" -> it.variantWeight * it.quantity

                else -> 0.0
            }
        }

    var totalKg = (karung25 * 25) + eceran
    totalKg -= cartKg

    if (totalKg < 0) totalKg = 0.0

    val displayKarung = (totalKg / 25).toInt()
    val displayEceran = (totalKg % 25).toInt()
    val availableKg = totalKg

    // ======================
    // VARIANT
    // ======================


    val variants = product.prices
        .filter { it.value > 0 }
        .keys
        .toList()

    var selectedVariant by remember(product.id) {
        mutableStateOf(variants.firstOrNull() ?: "")
    }

    fun shortVariant(v: String): String {

        return when (v) {

            "karung25" -> "K25"
            "karung10" -> "K10"
            "karung5" -> "K5"

            else -> v
        }
    }

    // ======================
    // STOK PER VARIAN
    // ======================
    val cartQty = cartItems
        .filter {
            it.product.id == product.id &&
                    it.variantType == selectedVariant
        }
        .sumOf { it.quantity }

    val variantStock = when (selectedVariant) {

        "pcs" -> {
            val stock = inventory?.variants?.get("pcs") ?: 0
            (stock - cartQty).coerceAtLeast(0)
        }

        "karung25" -> {
            val stock = inventory?.variants?.get("karung25") ?: 0
            (stock - cartQty).coerceAtLeast(0)
        }

        "karung10" -> {
            val stock = inventory?.variants?.get("karung10") ?: 0
            (stock - cartQty).coerceAtLeast(0)
        }

        "karung5" -> {
            val stock = inventory?.variants?.get("karung5") ?: 0
            (stock - cartQty).coerceAtLeast(0)
        }

        "kg" -> totalKg.toInt()

        "0.5kg" -> (totalKg / 0.5).toInt()

        "liter" -> (totalKg / kgPerLiter).toInt()

        "nominal" -> totalKg.toInt()

        else -> 0
    }

    // ======================
    // STATUS WARNA STOK
    // ======================

    val badgeColor = when {

        variantStock == 0 -> Color.Red

        variantStock <= 10 -> Color(0xFFFFC107)

        else -> Color(0xFF4CAF50)
    }

    val quantity = cartItems
        .filter {
            it.product.id == product.id &&
                    it.variantType == selectedVariant
        }
        .sumOf { it.quantity }

    val hasKarung25 = (inventory?.variants?.get("karung25") ?: 0) > 0

    val canNominal = hasKarung25 && totalKg > 0

    val canAdd = when (selectedVariant) {

        "pcs" ->
            quantity < (inventory?.variants?.get("pcs") ?: 0)

        "karung25" ->
            quantity < (inventory?.variants?.get("karung25") ?: 0)

        "karung10" ->
            quantity < (inventory?.variants?.get("karung10") ?: 0)

        "karung5" ->
            quantity < (inventory?.variants?.get("karung5") ?: 0)

        "kg" ->
            totalKg >= 1

        "0.5kg" ->
            totalKg >= 0.5

        "liter" ->
            totalKg >= kgPerLiter

        "nominal" ->
            totalKg > 0

        else -> false
    }

    val price = product.prices[selectedVariant] ?: 0

    val isEceran =
        selectedVariant in listOf("kg","0.5kg","liter","nominal")

    val stokText = if (isEceran) {

        "stok : ${displayKarung}Krg + ${displayEceran}kg"

    } else {

        "stok : $variantStock"
    }

    // ======================
    // UI
    // ======================

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(
                    badgeColor,RoundedCornerShape(2.dp)
                )
        )

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    product.name,
                    fontWeight = FontWeight.Medium
                )

                Text(rupiah(price))
            }

            Text(
                text = stokText,
                style = MaterialTheme.typography.bodySmall,
                color = badgeColor
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // KIRI (variant)
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    OutlinedButton(
                        onClick = { showNominalSheet = true },
                        enabled = canNominal,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text("Nominal")
                    }

                    Spacer(Modifier.width(6.dp))

                    Box {

                        OutlinedButton(
                            onClick = { expanded = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(shortVariant(selectedVariant))
                            Icon(Icons.Default.ArrowDropDown,null)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            variants.forEach { variant ->
                                DropdownMenuItem(
                                    text = { Text(shortVariant(variant)) },
                                    onClick = {
                                        selectedVariant = variant
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // KANAN (quantity)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton({
                        cartViewModel.decreaseQuantity(product, selectedVariant)
                    }) {
                        Icon(Icons.Default.Delete,null)
                    }

                    Text(
                        quantity.toString(),
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            if (canAdd) {
                                cartViewModel.addToCart(product, selectedVariant,1.0)
                            }
                        },
                        enabled = canAdd
                    ) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        }
    }

    Divider()

    // ======================
    // NOMINAL BOTTOMSHEET
    // ======================

    val context = LocalContext.current
    if(showNominalSheet){

        var priceText by remember { mutableStateOf("") }
        var weightText by remember { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { showNominalSheet = false }
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {

                Text(
                    "Tambah Nominal",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("Harga") },
                    prefix = { Text("Rp ") }
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Kg") }
                )

                Spacer(Modifier.height(16.dp))

                Button(

                    modifier = Modifier.fillMaxWidth(),

                    onClick = {

                        val price = priceText.toIntOrNull() ?: 0
                        val weight = weightText.toDoubleOrNull() ?: 0.0

                        if (price > 0 && weight > 0) {

                            if (weight > totalKg) {

                                Toast
                                    .makeText(
                                        context,
                                        "Stok tidak cukup",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()

                                return@Button
                            }

                            cartViewModel.addNominal(
                                product,
                                weight,
                                price
                            )

                            showNominalSheet = false
                        }
                    }

                ) {
                    Text("Tambah")
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
@Composable
fun AnimatedSuccessCheck(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onPrimary
) {

    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )
    }

    Canvas(
        modifier = modifier.size(80.dp)
    ) {

        val strokeWidth = 8f

        // Lingkaran
        drawCircle(
            color = color.copy(alpha = 0.2f),
            style = Stroke(width = strokeWidth)
        )

        // Path centang
        val path = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.55f)
            lineTo(size.width * 0.45f, size.height * 0.7f)
            lineTo(size.width * 0.75f, size.height * 0.35f)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(
                        animationProgress.value * 300f,
                        300f
                    ),
                    0f
                )
            )
        )
    }
}