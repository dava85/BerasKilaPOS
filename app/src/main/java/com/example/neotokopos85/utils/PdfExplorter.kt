package com.example.neotokopos85.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.neotokopos85.R
import com.example.neotokopos85.data.local.entity.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

fun exportMonthlySalesPdf(
    context: Context,
    orders: List<OrderEntity>
): File? {

    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    val rupiah = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("in", "ID"))

    var headerDrawn = false

    val monthlyOrders = orders.filter {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.timestamp
        cal.get(Calendar.MONTH) == currentMonth &&
                cal.get(Calendar.YEAR) == currentYear
    }

    if (monthlyOrders.isEmpty()) {
        return null
    }

    val pageWidth = 595
    val pageHeight = 842

    val document = PdfDocument()

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var page = document.startPage(pageInfo)
    var canvas = page.canvas

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.strokeWidth = 1.2f
    paint.textSize = 12f

    var y = 60f
    val rowHeight = 26f

    fun drawWatermark() {
        val watermark = BitmapFactory.decodeResource(context.resources, R.drawable.logo_toko)
        val scaled = Bitmap.createScaledBitmap(watermark, 420, 420, true)
        val wmPaint = Paint().apply { alpha = 30 }

        canvas.drawBitmap(
            scaled,
            (pageWidth - 420) / 2f,
            (pageHeight - 420) / 2f,
            wmPaint
        )
    }

    fun newPage() {
        document.finishPage(page)

        pageNumber++

        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas

        drawWatermark()

        y = 60f
        headerDrawn = false
    }

    drawWatermark()

    val logo = BitmapFactory.decodeResource(context.resources, R.drawable.logo_toko)
    val scaledLogo = Bitmap.createScaledBitmap(logo, 60, 60, true)

    canvas.drawBitmap(scaledLogo, 40f, y - 20f, null)

    paint.typeface = Typeface.DEFAULT_BOLD
    paint.textSize = 22f
    canvas.drawText("BERASKILA", 110f, y, paint)

    paint.typeface = Typeface.DEFAULT
    paint.textSize = 14f

    y += 30f
    canvas.drawText("Laporan Penjualan Bulanan", 110f, y, paint)

    y += 20f
    canvas.drawLine(40f, y + 10f, pageWidth - 40f, y + 10f, paint)

    y += 30f

    val monthText = SimpleDateFormat("MMMM yyyy", Locale("in", "ID")).format(Date())
    canvas.drawText(monthText, 40f, y, paint)

    y += 40f

    val dailyGross = mutableMapOf<Int, Double>()
    val dailyDiscount = mutableMapOf<Int, Double>()
    val dailyNet = mutableMapOf<Int, Double>()

    monthlyOrders.forEach { order ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = order.timestamp
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val discount = order.items.sumOf { it.discount }
        val net = order.totalPrice
        val gross = net + discount

        dailyGross[day] = (dailyGross[day] ?: 0.0) + gross
        dailyDiscount[day] = (dailyDiscount[day] ?: 0.0) + discount
        dailyNet[day] = (dailyNet[day] ?: 0.0) + net
    }

    val c1 = 40f
    val c2 = 90f
    val c3 = 190f
    val c4 = 320f
    val c5 = 430f
    val c6 = pageWidth - 40f

    fun drawDailyRow(no: String, tgl: String, kotor: String, disc: String, net: String) {

        canvas.drawLine(c1, y, c6, y, paint)

        canvas.drawLine(c1, y, c1, y + rowHeight, paint)
        canvas.drawLine(c2, y, c2, y + rowHeight, paint)
        canvas.drawLine(c3, y, c3, y + rowHeight, paint)
        canvas.drawLine(c4, y, c4, y + rowHeight, paint)
        canvas.drawLine(c5, y, c5, y + rowHeight, paint)
        canvas.drawLine(c6, y, c6, y + rowHeight, paint)

        canvas.drawText(no, c1 + 6, y + 18, paint)
        canvas.drawText(tgl, c2 + 6, y + 18, paint)
        canvas.drawText(kotor, c3 + 6, y + 18, paint)
        canvas.drawText(disc, c4 + 6, y + 18, paint)
        canvas.drawText(net, c5 + 6, y + 18, paint)

        // garis bawah row
        canvas.drawLine(c1, y + rowHeight, c6, y + rowHeight, paint)

        y += rowHeight
    }


    val today = calendar.get(Calendar.DAY_OF_MONTH)

    fun drawDailyHeader() {

        paint.typeface = Typeface.DEFAULT_BOLD

        canvas.drawLine(c1, y, c6, y, paint)

        canvas.drawLine(c1, y, c1, y + rowHeight, paint)
        canvas.drawLine(c2, y, c2, y + rowHeight, paint)
        canvas.drawLine(c3, y, c3, y + rowHeight, paint)
        canvas.drawLine(c4, y, c4, y + rowHeight, paint)
        canvas.drawLine(c5, y, c5, y + rowHeight, paint)
        canvas.drawLine(c6, y, c6, y + rowHeight, paint)

        canvas.drawText("No", c1 + 6, y + 18, paint)
        canvas.drawText("Tanggal", c2 + 6, y + 18, paint)
        canvas.drawText("Total Kotor", c3 + 6, y + 18, paint)
        canvas.drawText("Diskon", c4 + 6, y + 18, paint)
        canvas.drawText("Total Bersih", c5 + 6, y + 18, paint)

        y += rowHeight

        paint.typeface = Typeface.DEFAULT
    }

    drawDailyHeader()

    for (day in 1..today) {

        if (y + rowHeight > pageHeight - 100) {

            // tutup tabel halaman lama
            canvas.drawLine(c1, y, c6, y, paint)

            newPage()

            // header tabel digambar ulang
            drawDailyHeader()
        }

        val cal = Calendar.getInstance()
        cal.set(currentYear, currentMonth, day)

        val gross = dailyGross[day] ?: 0.0
        val discount = dailyDiscount[day] ?: 0.0
        val net = dailyNet[day] ?: 0.0

        drawDailyRow(
            day.toString(),
            dateFormat.format(cal.time),
            "Rp ${rupiah.format(gross)}",
            "Rp ${rupiah.format(discount)}",
            "Rp ${rupiah.format(net)}"
        )
    }

    canvas.drawLine(c1, y, c6, y, paint)

    val totalNet = dailyNet.values.sum()

    y += 20f
    paint.typeface = Typeface.DEFAULT_BOLD
    canvas.drawText("TOTAL BULAN : Rp ${rupiah.format(totalNet)}", 40f, y, paint)

    y += 40f
    canvas.drawText("Ringkasan Produk Terjual", 40f, y, paint)

    y += 30f

    val summary = calculateProductSummaryGrouped(monthlyOrders)

    val p1 = 40f
    val p2 = 230f
    val p3 = 340f
    val p4 = 400f
    val p5 = pageWidth - 40f
    val h = 24f

    fun drawHeader() {

        paint.typeface = Typeface.DEFAULT_BOLD

        canvas.drawLine(p1, y, p5, y, paint)

        canvas.drawLine(p1, y, p1, y + h, paint)
        canvas.drawLine(p2, y, p2, y + h, paint)
        canvas.drawLine(p3, y, p3, y + h, paint)
        canvas.drawLine(p4, y, p4, y + h, paint)
        canvas.drawLine(p5, y, p5, y + h, paint)

        canvas.drawText("produk", p1 + 6, y + 16, paint)
        canvas.drawText("varian", p2 + 6, y + 16, paint)
        canvas.drawText("item", p3 + 6, y + 16, paint)
        canvas.drawText("total", p4 + 6, y + 16, paint)

        y += h

        paint.typeface = Typeface.DEFAULT
        headerDrawn = true
    }

    fun drawRow(prod: String, varian: String, item: String, total: String) {

        if (prod.isEmpty()) {
            canvas.drawLine(p2, y, p5, y, paint)
        } else {
            canvas.drawLine(p1, y, p5, y, paint)
        }

        canvas.drawLine(p1, y, p1, y + h, paint)
        canvas.drawLine(p2, y, p2, y + h, paint)
        canvas.drawLine(p3, y, p3, y + h, paint)
        canvas.drawLine(p4, y, p4, y + h, paint)
        canvas.drawLine(p5, y, p5, y + h, paint)

        canvas.drawText(prod, p1 + 6, y + 16, paint)
        canvas.drawText(varian, p2 + 6, y + 16, paint)
        canvas.drawText(item, p3 + 6, y + 16, paint)
        canvas.drawText(total, p4 + 6, y + 16, paint)

        y += h
    }

    summary.forEach { (product, variants) ->

        val neededHeight = (variants.size + 2) * h

        if (y + neededHeight > pageHeight - 60) {

            // tutup tabel halaman lama
            canvas.drawLine(p1, y, p5, y, paint)

            newPage()

            // gambar header lagi di halaman baru
            drawHeader()
        }

        if (!headerDrawn) {
            drawHeader()
        }

        var first = true
        var subtotal = 0.0

        variants.forEach { (variant, data) ->

            val qty = data.first
            val total = data.second

            subtotal += total

            drawRow(
                if (first) product else "",
                variant,
                "x$qty",
                "Rp ${rupiah.format(total)}"
            )

            first = false
        }

        paint.typeface = Typeface.DEFAULT_BOLD
        drawRow("Subtotal", "", "", "Rp ${rupiah.format(subtotal)}")
        paint.typeface = Typeface.DEFAULT
    }

    canvas.drawLine(p1, y, p5, y, paint)

    document.finishPage(page)

    val file = File(
        context.getExternalFilesDir(null),
        "Laporan_Bulanan_Beraskila.pdf"
    )

    document.writeTo(FileOutputStream(file))
    document.close()

    return file
}
fun calculateProductSummaryGrouped(
    orders: List<OrderEntity>
): Map<String, Map<String, Pair<Int, Double>>> {

    val result = mutableMapOf<String, MutableMap<String, Pair<Int, Double>>>()

    orders.forEach { order ->
        order.items.forEach { item ->

            val productMap = result.getOrPut(item.productName) { mutableMapOf() }

            val current = productMap[item.variantKg]

            val qty = (current?.first ?: 0) + item.quantity
            val total = (current?.second ?: 0.0) + (item.subtotal - item.discount)

            productMap[item.variantKg] = qty to total
        }
    }

    return result
}