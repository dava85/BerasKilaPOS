package com.example.neotokopos85.ui.dashboard

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import com.example.neotokopos85.utils.exportMonthlySalesPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale


enum class ChartType {

    PRODUCT,
    DAILY,
    WEEKLY,
    MONTHLY
}

@Composable
fun StatistikTab(
    orderViewModel: OrderViewModel
) {

    var selectedChart by remember { mutableStateOf(ChartType.DAILY) }
    var isExporting by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 100.dp)
        ) {

            DashboardStatsSection(orderViewModel)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                FilterChip(
                    selected = selectedChart == ChartType.DAILY,
                    onClick = { selectedChart = ChartType.DAILY },
                    label = { Text("Harian") }
                )

                FilterChip(
                    selected = selectedChart == ChartType.WEEKLY,
                    onClick = { selectedChart = ChartType.WEEKLY },
                    label = { Text("Minggu") }
                )

                FilterChip(
                    selected = selectedChart == ChartType.MONTHLY,
                    onClick = { selectedChart = ChartType.MONTHLY },
                    label = { Text("Bulanan") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedChart) {
                ChartType.DAILY -> SalesChartSection(orderViewModel)
                ChartType.WEEKLY -> WeeklyChartSection(orderViewModel)
                ChartType.PRODUCT -> ProductChartSection(orderViewModel)
                ChartType.MONTHLY -> MonthlyChartSection(orderViewModel)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isExporting = true

                    scope.launch(Dispatchers.IO) {
                        try {

                            val file = exportMonthlySalesPdf(
                                context,
                                orderViewModel.allOrders.value
                            )

                            withContext(Dispatchers.Main) {

                                if (file != null) {

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )

                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "application/pdf")
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }

                                    context.startActivity(intent)

                                    Toast.makeText(
                                        context,
                                        "PDF berhasil dibuat",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Tidak ada data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                        } catch (_: Exception) {

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Gagal export PDF",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        } finally {

                            withContext(Dispatchers.Main) {
                                isExporting = false
                            }
                        }
                    }
                },
                enabled = !isExporting,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {

                if (isExporting) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Membuat PDF...")
                    }

                } else {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(Icons.Default.Receipt, null)

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            "Export PDF Laporan",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // LOADING OVERLAY
        if (isExporting) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {

                Card(
                    shape = RoundedCornerShape(24.dp)
                ) {

                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        CircularProgressIndicator()

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Sedang membuat laporan...",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ProductChartSection(orderViewModel: OrderViewModel) {

    val productSales by orderViewModel.productSales.collectAsState()

    if (productSales.isEmpty()) {
        Text("Belum ada data produk")
        return
    }

    val maxValue = productSales.maxOfOrNull { it.second } ?: 0
    if (maxValue == 0) return

    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(productSales) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, tween(900))
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {

        val padding = 80f
        val chartHeight = size.height - 60f
        val barWidth = (size.width - padding * 2) / productSales.size * 0.6f
        val spaceBetween =
            ((size.width - padding * 2) / productSales.size)

        drawLine(
            color = Color.Gray,
            start = Offset(padding, chartHeight),
            end = Offset(size.width - padding, chartHeight),
            strokeWidth = 4f
        )

        productSales.forEachIndexed { index, item ->

            val percentage = item.second.toFloat() / maxValue
            val barHeight =
                percentage * chartHeight * animatedProgress.value

            val x = padding + index * spaceBetween

            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(x, chartHeight - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(24f)
            )
        }
    }
}

@Composable
fun SalesChartSection(orderViewModel: OrderViewModel) {

    val orders by orderViewModel.allOrders.collectAsState()

    // Ambil 7 hari terakhir (tanggal real)
    val last7Days = remember(orders) {

        val list = mutableListOf<Pair<String, Int>>()

        for (i in 6 downTo 0) {

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, -i)

            val day = cal.get(Calendar.DAY_OF_MONTH)

            val value = orders
                .filter {

                    val c = Calendar.getInstance()
                    c.timeInMillis = it.timestamp

                    c.get(Calendar.DAY_OF_MONTH) == day &&
                            c.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                            c.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                }
                .sumOf { it.totalPrice.toInt() }

            list.add(day.toString() to value)
        }

        list
    }

    fun formatShort(value: Int): String {
        return when {
            value >= 1_000_000 -> "${value / 1_000_000}M"
            value >= 1_000 -> "${value / 1_000}K"
            else -> value.toString()
        }
    }

    val maxValue = last7Days.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {

            val padding = 40f
            val chartHeight = size.height - 60f

            val spacing = (size.width - padding * 2) / last7Days.size
            val barWidth = spacing * 0.6f

            // GRID
            for (i in 1..4) {

                val y = chartHeight - (chartHeight / 4 * i)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 2f
                )
            }

            // AXIS
            drawLine(
                color = Color.Gray,
                start = Offset(padding, chartHeight),
                end = Offset(size.width - padding, chartHeight),
                strokeWidth = 4f
            )

            last7Days.forEachIndexed { index, data ->

                val value = data.second
                val percentage = value.toFloat() / maxValue

                val barHeight = percentage * chartHeight

                val x = padding + index * spacing + (spacing - barWidth) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
                    ),
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(18f)
                )

                // VALUE DI ATAS BAR
                drawIntoCanvas { canvas ->

                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 28f
                        isFakeBoldText = true
                    }

                    canvas.nativeCanvas.drawText(
                        formatShort(value),
                        x + barWidth / 2,
                        chartHeight - barHeight - 8f,
                        paint
                    )
                }
            }
        }

        // LABEL BAR (tanggal real, dekat dengan bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            last7Days.forEach {

                Text(
                    text = it.first,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // PENJELASAN
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            last7Days.forEach {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text("Tanggal ${it.first}")

                    Text(
                        "Rp %,d".format(it.second),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}
@Composable
fun WeeklyChartSection(orderViewModel: OrderViewModel) {

    val orders by orderViewModel.allOrders.collectAsState()

    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val weekSales = MutableList(4) { 0 }

    orders.forEach {

        val cal = Calendar.getInstance()
        cal.timeInMillis = it.timestamp

        if (cal.get(Calendar.MONTH) == currentMonth &&
            cal.get(Calendar.YEAR) == currentYear
        ) {

            val day = cal.get(Calendar.DAY_OF_MONTH)

            val index = when (day) {
                in 1..7 -> 0
                in 8..14 -> 1
                in 15..21 -> 2
                else -> 3
            }

            weekSales[index] += it.totalPrice.toInt()
        }
    }

    val chartData = listOf(
        "1-7" to weekSales[0],
        "8-14" to weekSales[1],
        "15-21" to weekSales[2],
        "22-$lastDayOfMonth" to weekSales[3]
    )

    fun formatShort(value: Int): String {
        return when {
            value >= 1_000_000 -> "${value / 1_000_000}M"
            value >= 1_000 -> "${value / 1_000}K"
            else -> value.toString()
        }
    }

    val maxValue = chartData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {

            val padding = 40f
            val chartHeight = size.height - 60f

            val spacing = (size.width - padding * 2) / chartData.size
            val barWidth = spacing * 0.6f

            // GRID
            for (i in 1..4) {

                val y = chartHeight - (chartHeight / 4 * i)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 2f
                )
            }

            // AXIS
            drawLine(
                color = Color.Gray,
                start = Offset(padding, chartHeight),
                end = Offset(size.width - padding, chartHeight),
                strokeWidth = 4f
            )

            chartData.forEachIndexed { index, data ->

                val value = data.second
                val percentage = value.toFloat() / maxValue

                val barHeight = percentage * chartHeight

                val x = padding + index * spacing + (spacing - barWidth) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
                    ),
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(18f)
                )

                // VALUE DI ATAS BAR
                drawIntoCanvas { canvas ->

                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 28f
                        isFakeBoldText = true
                    }

                    canvas.nativeCanvas.drawText(
                        formatShort(value),
                        x + barWidth / 2,
                        chartHeight - barHeight - 8f,
                        paint
                    )
                }
            }
        }

        // LABEL BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            chartData.forEach {

                Text(
                    text = it.first,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // PENJELASAN
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            chartData.forEach {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text("Tanggal ${it.first}")

                    Text(
                        "Rp %,d".format(it.second),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}
@Composable
fun MonthlyChartSection(orderViewModel: OrderViewModel) {

    val monthlySales by orderViewModel.monthlySales.collectAsState()

    val calendar = Calendar.getInstance()

    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    calendar.add(Calendar.MONTH, -1)

    val lastMonth = calendar.get(Calendar.MONTH)
    val lastYear = calendar.get(Calendar.YEAR)

    val currentKey = String.format(
        Locale("id", "ID"),
        "%04d-%02d",
        currentYear,
        currentMonth + 1
    )

    val lastKey = String.format(
        Locale("id", "ID"),
        "%04d-%02d",
        lastYear,
        lastMonth + 1
    )

    val currentValue =
        monthlySales.find { it.first == currentKey }?.second ?: 0

    val lastValue =
        monthlySales.find { it.first == lastKey }?.second ?: 0

    val monthNames = listOf(
        "Jan","Feb","Mar","Apr","Mei","Jun",
        "Jul","Agu","Sep","Okt","Nov","Des"
    )

    val chartData = listOf(
        monthNames[lastMonth] to lastValue,
        monthNames[currentMonth] to currentValue
    )

    fun formatShort(value: Int): String {
        return when {
            value >= 1_000_000 -> "${value / 1_000_000}M"
            value >= 1_000 -> "${value / 1_000}K"
            else -> value.toString()
        }
    }

    val maxValue = chartData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {

            val padding = 40f
            val chartHeight = size.height - 60f

            val spacing = (size.width - padding * 2) / chartData.size
            val barWidth = spacing * 0.6f

            // GRID
            for (i in 1..4) {

                val y = chartHeight - (chartHeight / 4 * i)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(size.width - padding, y),
                    strokeWidth = 2f
                )
            }

            // AXIS
            drawLine(
                color = Color.Gray,
                start = Offset(padding, chartHeight),
                end = Offset(size.width - padding, chartHeight),
                strokeWidth = 4f
            )

            chartData.forEachIndexed { index, data ->

                val value = data.second
                val percentage = value.toFloat() / maxValue

                val barHeight = percentage * chartHeight

                val x = padding + index * spacing + (spacing - barWidth) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
                    ),
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(18f)
                )

                // VALUE DI ATAS BAR
                drawIntoCanvas { canvas ->

                    val paint = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 28f
                        isFakeBoldText = true
                    }

                    canvas.nativeCanvas.drawText(
                        formatShort(value),
                        x + barWidth / 2,
                        chartHeight - barHeight - 8f,
                        paint
                    )
                }
            }
        }

        // LABEL BULAN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            chartData.forEach {

                Text(
                    text = it.first,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // PENJELASAN
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            chartData.forEach {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text("Bulan ${it.first}")

                    Text(
                        "Rp %,d".format(it.second),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
    }
}

@Composable
fun DashboardStatsSection(orderViewModel: OrderViewModel) {

    val grossRevenue by orderViewModel.grossRevenue.collectAsState(initial = 0)
    val netRevenue by orderViewModel.netRevenue.collectAsState(initial = 0)
    val totalDiscount by orderViewModel.totalDiscount.collectAsState(initial = 0)

    val todayRevenue = orderViewModel.getTodayRevenue()
    val todayDiscount = orderViewModel.getTodayDiscount()
    val todayNet = todayRevenue - todayDiscount

    val todayOrders = orderViewModel.getTodayOrders()
    val todayItems = orderViewModel.getTodayItemsSold()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 🔹 ROW KOTOR & BERSIH
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = "Pendapatan Kotor",
                value = "Rp %,d".format(grossRevenue),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Pendapatan Bersih",
                value = "Rp %,d".format(netRevenue),
                modifier = Modifier.weight(1f)
            )
        }

        // 🔹 CARD HARI INI
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text("Pendapatan Hari Ini - Diskon")

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "Rp %,d - Rp %,d = Rp %,d".format(
                        todayRevenue,
                        todayDiscount,
                        todayNet
                    ),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 🔹 ROW ORDER & ITEM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            StatCard(
                title = "Order Hari Ini",
                value = todayOrders.toString(),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Item Terjual",
                value = todayItems.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}