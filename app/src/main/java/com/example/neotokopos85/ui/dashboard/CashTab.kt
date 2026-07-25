package com.example.neotokopos85.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush

import com.example.neotokopos85.ui.viewmodel.*
import com.example.neotokopos85.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashTab(
    orderViewModel: OrderViewModel,
    cashViewModel: CashViewModel
) {

    val orders by orderViewModel.allOrders.collectAsStateWithLifecycle()
    val expenses by cashViewModel.expenses.collectAsStateWithLifecycle()
    val setorans by cashViewModel.setorans.collectAsStateWithLifecycle()

    var selectedRange by remember {
        mutableStateOf(getWeekRange(System.currentTimeMillis()))
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showInputSheet by remember { mutableStateOf(false) }

    val startWeek = selectedRange.first
    val endWeek = selectedRange.second

    // 🔥 SORT BY TIME
    val filteredExpenses =
        expenses
            .filter { it.timestamp in startWeek..endWeek }
            .sortedByDescending { it.timestamp }

    val filteredSetorans =
        setorans
            .filter { it.timestamp in startWeek..endWeek }
            .sortedByDescending { it.timestamp }

    val allTransactions =
        (
                filteredExpenses.map {
                    CashItem(it.name, it.amount, true, it.timestamp)
                } +
                        filteredSetorans.map {
                            CashItem("Setoran", it.amount, false, it.timestamp)
                        }
                ).sortedByDescending { it.timestamp }

    val weeklyOrders = remember(orders, startWeek, endWeek) {
        orders.filter {
            it.timestamp >= startWeek && it.timestamp <= endWeek
        }
    }
    val totalSales = weeklyOrders.sumOf { it.totalPrice }
    val totalExpense = filteredExpenses.sumOf { it.amount }
    val totalSetoran = filteredSetorans.sumOf { it.amount }

    val saldo = totalSales - totalExpense - totalSetoran

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // ================= HEADER =================
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = {
                        val prev = startWeek - 604800000
                        selectedRange = getWeekRange(prev)
                        selectedDateMillis = prev
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures {
                                showDatePicker = true
                            }
                        }
                    ) {

                        Text(
                            "Periode",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            formatRange(startWeek, endWeek),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = {
                        val next = startWeek + 604800000
                        selectedRange = getWeekRange(next)
                        selectedDateMillis = next
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ================= SALDO =================
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {

                Column(
                    modifier = Modifier
                        .padding(18.dp)
                ) {

                    Text(
                        "Kas Mingguan",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        rupiah(saldo),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        SummaryItem("Penjualan", totalSales, Color(0xFF2E7D32))
                        SummaryItem("Pengeluaran", totalExpense, Color(0xFFC62828))
                        SummaryItem("Setoran", totalSetoran, Color(0xFF1565C0))

                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= RIWAYAT =================
            val pagerState = rememberPagerState(pageCount = { 2 })

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                repeat(2) { index ->

                    val color =
                        if (pagerState.currentPage == index)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.LightGray

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(8.dp)
                            .background(color, RoundedCornerShape(50))
                    )
                }
            }
            val pemasukanTransactions =
                weeklyOrders.map {
                    CashItem(
                        title = "Penjualan",
                        amount = it.totalPrice,
                        isMinus = false,
                        timestamp = it.timestamp
                    )
                }.sortedByDescending { it.timestamp }

            val pengeluaranTransactions =
                (
                        filteredExpenses.map {
                            CashItem(it.name, it.amount, true, it.timestamp)
                        } +
                                filteredSetorans.map {
                                    CashItem("Setoran", it.amount, true, it.timestamp)
                                }
                        ).sortedByDescending { it.timestamp }

            Text(
                if (pagerState.currentPage == 0) "Pemasukan" else "Pengeluaran",
                fontWeight = FontWeight.Bold,
                color =
                    if (pagerState.currentPage == 0)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFFE53935)
            )

            Spacer(Modifier.height(8.dp))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                val list =
                    if (page == 0) pemasukanTransactions
                    else pengeluaranTransactions

                if (list.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada transaksi")
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top,
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {

                        val groupedByDate: Map<String, List<CashItem>> =
                            list.groupBy { formatDateOnly(it.timestamp) }

                        items(groupedByDate.entries.toList()) { entry ->

                            if (entry.value.isNotEmpty()) {
                                DailyCashGroup(entry.key, entry.value)
                            }

                        }

                    }

                }
            }
        }

        FloatingActionButton(
            onClick = { showInputSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {

            Text("+")
        }

        if (showInputSheet) {

            CashInputSheet(
                onDismiss = { showInputSheet = false },
                onSubmit = { name, amount, isExpense ->

                    if (isExpense) {
                        cashViewModel.addExpense(name, amount)
                    } else {
                        cashViewModel.addSetoran(amount)
                    }
                }
            )
        }
        if (showDatePicker) {

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {

                            selectedRange = getWeekRange(selectedDateMillis)

                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDatePicker = false }
                    ) {
                        Text("Batal")
                    }
                }
            ) {

                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis
                )

                DatePicker(
                    state = datePickerState
                )

                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                }
            }
        }
    }
}


@Composable
fun SummaryItem(title: String, amount: Double, color: Color) {

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Text(
            title,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Text(
            rupiah(amount),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun CashItemCard(
    item: CashItem,
    index: Int
) {

    val color = if (item.isMinus) Color(0xFFE53935) else Color(0xFF43A047)

    val cardColor =
        if (index % 2 == 0) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        }

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        )
    ) {

        Row(
            modifier = Modifier
                .padding(14.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(4.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        item.title,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "${if (item.isMinus) "-" else "+"} ${rupiah(item.amount)}",
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    formatDateTime(item.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashInputSheet(
    onDismiss: () -> Unit,
    onSubmit: (String, Double, Boolean) -> Unit
) {

    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isExpense by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                "Input Kas",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(16.dp))

            Row {

                FilterChip(
                    selected = isExpense,
                    onClick = { isExpense = true },
                    label = { Text("Pengeluaran") }
                )

                Spacer(Modifier.width(8.dp))

                FilterChip(
                    selected = !isExpense,
                    onClick = { isExpense = false },
                    label = { Text("Setoran") }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (isExpense) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Pengeluaran") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                label = { Text("Jumlah") },
                prefix = { Text("Rp ") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {

                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0) return@Button

                    val title =
                        if (isExpense) name
                        else "Setoran"

                    onSubmit(title, amount, isExpense)

                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DailyCashGroup(
    date: String,
    items: List<CashItem>
) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    val totalSales =
        items.filter { !it.isMinus }
            .sumOf { it.amount }

    val totalExpense =
        items.filter { it.isMinus }
            .sumOf { it.amount }

    val transactionCount = items.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {

        Card(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .fillMaxHeight()
                            .background(
                                Color(0xFF2E7D32),
                                RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {

                        Text(
                            text = date,
                            fontWeight = FontWeight.Bold
                        )

                        when {
                            totalSales > 0 -> {
                                Text(
                                    text = "${rupiah(totalSales)} • $transactionCount transaksi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF2E7D32).copy(alpha = 0.85f)
                                )
                            }

                            totalExpense > 0 -> {
                                Text(
                                    text = "-${rupiah(totalExpense)} • $transactionCount transaksi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE53935).copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (expanded) "▲" else "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (expanded) {

            Spacer(Modifier.height(4.dp))

            items.forEachIndexed { index, item ->
                CashItemCard(item, index)
            }
        }
    }
}
data class CashItem(
    val title: String,
    val amount: Double,
    val isMinus: Boolean,
    val timestamp: Long
)

fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id"))
    return sdf.format(Date(timestamp))
}

fun formatDateOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
    return sdf.format(Date(timestamp))
}
