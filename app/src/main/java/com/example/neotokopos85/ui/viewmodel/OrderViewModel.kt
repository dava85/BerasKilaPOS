package com.example.neotokopos85.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.neotokopos85.data.local.entity.OrderEntity
import com.example.neotokopos85.data.local.entity.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.Query

class OrderViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    // ================= STATE =================

    private val _allOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val allOrders: StateFlow<List<OrderEntity>> = _allOrders

    // Pendapatan Kotor (sebelum diskon)
    private val _grossRevenue = MutableStateFlow(0)
    val grossRevenue: StateFlow<Int> = _grossRevenue

    // Pendapatan Bersih (setelah diskon)
    private val _netRevenue = MutableStateFlow(0)
    val netRevenue: StateFlow<Int> = _netRevenue

    // Total Diskon
    private val _totalDiscount = MutableStateFlow(0)
    val totalDiscount: StateFlow<Int> = _totalDiscount

    private val _totalOrders = MutableStateFlow(0)
    val totalOrders: StateFlow<Int> = _totalOrders

    private val _totalItemsSold = MutableStateFlow(0)
    val totalItemsSold: StateFlow<Int> = _totalItemsSold

    private val _weeklySales = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Int>>> = _weeklySales

    private val _dailySales = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val dailySales: StateFlow<List<Pair<String, Int>>> = _dailySales

    private val _monthlySales = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val monthlySales: StateFlow<List<Pair<String, Int>>> = _monthlySales

    private val _productSales = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val productSales: StateFlow<List<Pair<String, Int>>> = _productSales

    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private val pageSize = 100

    private var isLoading = false

    private val _historyOrders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val historyOrders: StateFlow<List<OrderEntity>> = _historyOrders

    private val _totalTransactionCount = MutableStateFlow(0)
    val totalTransactionCount = _totalTransactionCount

    // ================= FIRESTORE =================

    fun startListeningOrders() {

        listener?.remove()

        listener = firestore.collection("orders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                val orderList = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(OrderEntity::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }

                _allOrders.value = orderList

                calculateStatistics(orderList)
                calculateDailySales(orderList)
                calculateMonthlySales(orderList)
            }
    }

    // ================= SAVE ORDER =================

    fun saveOrder(
        totalPrice: Int,
        totalItems: Int,
        name: String,
        address: String,
        items: List<OrderItem>
    ) {

        val orderRef = firestore.collection("orders").document()

        val order = OrderEntity(
            id = orderRef.id,
            customerName = name,
            customerAddress = address,
            totalPrice = totalPrice.toDouble(), // NET
            totalItems = totalItems,
            timestamp = System.currentTimeMillis(),
            items = items
        )

        orderRef.set(order)
            .addOnFailureListener {
                println("Gagal menyimpan order: ${it.message}")
            }
    }

    fun deleteOrder(documentId: String) {
        firestore.collection("orders")
            .document(documentId)
            .delete()
    }

    // ================= STATISTICS =================

    private fun calculateStatistics(orderList: List<OrderEntity>) {

        val currentMonth = formatMonth(System.currentTimeMillis())

        val monthlyOrders = orderList.filter {
            formatMonth(it.timestamp) == currentMonth
        }

        val netRevenueValue =
            monthlyOrders.sumOf { it.totalPrice }.roundToInt()

        val totalDiscountValue =
            monthlyOrders.sumOf { order ->
                order.items.sumOf { it.discount }
            }.roundToInt()

        val grossRevenueValue = netRevenueValue + totalDiscountValue

        val totalItems = monthlyOrders.sumOf { it.totalItems }

        _netRevenue.value = netRevenueValue
        _grossRevenue.value = grossRevenueValue
        _totalDiscount.value = totalDiscountValue
        _totalItemsSold.value = totalItems
        _totalOrders.value = monthlyOrders.size

        calculateWeeklySales(monthlyOrders)
        calculateProductSales(monthlyOrders)
    }

    // ================= DAILY SALES =================

    private fun calculateDailySales(orderList: List<OrderEntity>) {

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val map = mutableMapOf<String, Int>()

        orderList.forEach { order ->

            val cal = Calendar.getInstance()
            cal.timeInMillis = order.timestamp

            if (
                cal.get(Calendar.MONTH) == currentMonth &&
                cal.get(Calendar.YEAR) == currentYear
            ) {

                val day = cal.get(Calendar.DAY_OF_MONTH)
                val key = day.toString()

                map[key] = (map[key] ?: 0) + order.totalPrice.toInt()
            }
        }

        _dailySales.value = map.toList().sortedBy { it.first.toInt() }
    }

    // ================= WEEKLY SALES =================

    private fun calculateWeeklySales(monthlyOrders: List<OrderEntity>) {

        val weekSales = MutableList(4) { 0 }

        monthlyOrders.forEach { order ->

            val cal = Calendar.getInstance()
            cal.timeInMillis = order.timestamp

            val day = cal.get(Calendar.DAY_OF_MONTH)

            val weekIndex = when (day) {
                in 1..7 -> 0
                in 8..14 -> 1
                in 15..21 -> 2
                else -> 3
            }

            weekSales[weekIndex] += order.totalPrice.toInt()
        }

        _weeklySales.value = listOf(
            "M1" to weekSales[0],
            "M2" to weekSales[1],
            "M3" to weekSales[2],
            "M4" to weekSales[3]
        )
    }

    // ================= MONTHLY SALES =================

    private fun calculateMonthlySales(orderList: List<OrderEntity>) {

        val map = mutableMapOf<String, Int>()

        orderList.forEach { order ->

            val month = formatMonth(order.timestamp)

            map[month] = (map[month] ?: 0) + order.totalPrice.toInt()
        }

        _monthlySales.value = map.toList().sortedBy { it.first }
    }

    // ================= PRODUCT SALES =================

    private fun calculateProductSales(monthlyOrders: List<OrderEntity>) {

        _productSales.value = monthlyOrders
            .flatMap { it.items }
            .groupBy { it.productName }
            .map { entry ->
                entry.key to entry.value.sumOf { it.quantity }
            }
            .sortedByDescending { it.second }
    }

    // ================= TODAY SUMMARY =================

    fun getTodayRevenue(): Int {

        val today = formatDate(System.currentTimeMillis())

        val todayOrders = _allOrders.value
            .filter { formatDate(it.timestamp) == today }

        val net = todayOrders.sumOf { it.totalPrice }

        val discount = todayOrders.sumOf { order ->
            order.items.sumOf { it.discount }
        }

        return (net + discount).roundToInt()
    }

    fun getTodayOrders(): Int {

        val today = formatDate(System.currentTimeMillis())

        return _allOrders.value.count {
            formatDate(it.timestamp) == today
        }
    }

    fun getTodayItemsSold(): Int {

        val today = formatDate(System.currentTimeMillis())

        return _allOrders.value
            .filter { formatDate(it.timestamp) == today }
            .sumOf { it.totalItems }
    }

    fun getTodayDiscount(): Int {

        val today = formatDate(System.currentTimeMillis())

        return _allOrders.value
            .filter { formatDate(it.timestamp) == today }
            .sumOf { order ->
                order.items.sumOf { it.discount }
            }
            .roundToInt()
    }

    fun getTodayDetails(): List<OrderEntity> {

        val today = formatDate(System.currentTimeMillis())

        return _allOrders.value.filter {
            formatDate(it.timestamp) == today
        }
    }



    // ================= FORMATTER =================
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    private fun formatMonth(timestamp: Long): String {
        return monthFormatter.format(Date(timestamp))
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
    fun loadFirstHistoryPage(filter: String) {

        if (isLoading) return
        isLoading = true

        val query = buildQuery(filter)
            .limit(pageSize.toLong())

        query.get()
            .addOnSuccessListener { snapshot ->

                val orders = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(OrderEntity::class.java)?.copy(id = doc.id)
                }

                _historyOrders.value = orders
                lastDocument = snapshot.documents.lastOrNull()

                isLoading = false
            }
    }
    fun loadNextHistoryPage(filter: String) {

        if (isLoading) return

        val last = lastDocument ?: return
        isLoading = true

        val query = buildQuery(filter)
            .startAfter(last)
            .limit(pageSize.toLong())

        query.get()
            .addOnSuccessListener { snapshot ->

                val moreOrders = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(OrderEntity::class.java)?.copy(id = doc.id)
                }

                _historyOrders.value = _historyOrders.value + moreOrders
                lastDocument = snapshot.documents.lastOrNull()

                isLoading = false
            }
    }

    fun loadTotalTransactionCount(filter: String) {

        val query = buildQuery(filter)

        query.count()
            .get(AggregateSource.SERVER)
            .addOnSuccessListener {
                _totalTransactionCount.value = it.count.toInt()
            }
    }

    private fun buildQuery(filter: String): Query {

        var query: Query = firestore.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)

        when (filter) {

            "Hari ini" -> {

                val start = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                    set(Calendar.MILLISECOND,0)
                }

                query = query.whereGreaterThanOrEqualTo(
                    "timestamp",
                    start.timeInMillis
                )
            }

            "Minggu ini" -> {

                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                    set(Calendar.MILLISECOND,0)
                }

                query = query.whereGreaterThanOrEqualTo(
                    "timestamp",
                    start.timeInMillis
                )
            }

            "Bulan ini" -> {

                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH,1)
                    set(Calendar.HOUR_OF_DAY,0)
                    set(Calendar.MINUTE,0)
                    set(Calendar.SECOND,0)
                    set(Calendar.MILLISECOND,0)
                }

                query = query.whereGreaterThanOrEqualTo(
                    "timestamp",
                    start.timeInMillis
                )
            }
        }

        return query
    }
}