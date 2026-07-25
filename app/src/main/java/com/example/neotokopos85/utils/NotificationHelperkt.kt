package com.example.neotokopos85.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.neotokopos85.MainActivity
import com.example.neotokopos85.R

object NotificationHelper {

    const val CHANNEL_ID = "beraskila_stock_channel"
    private const val GROUP_ID = "stock_group"

    /**
     * CREATE CHANNEL
     */
    fun createChannel(context: Context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri =
                Uri.parse("android.resource://${context.packageName}/${R.raw.alert_stock}")

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "Low Stock Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description = "Notifikasi stok hampir habis"

                setSound(soundUri, audioAttributes)

                enableVibration(true)

                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    /**
     * NOTIF SINGLE PRODUCT
     */
    fun showLowStockNotification(
        context: Context,
        productName: String,
        productId: String
    ) {

        val intent = Intent(context, MainActivity::class.java).apply {

            putExtra("screen", "inventory_low")
            putExtra("productId", productId)

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            productId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_toko)
            .setContentTitle("⚠ Stok Hampir Habis")
            .setContentText("$productName hampir habis")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$productName hampir habis. Segera lakukan restock.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_ID)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Android 13 permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat
            .from(context)
            .notify(productId.hashCode(), notification)
    }

    /**
     * GROUPED LOW STOCK NOTIFICATION
     */
    fun showGroupedLowStockNotification(
        context: Context,
        products: List<String>
    ) {

        if (products.isEmpty()) return

        val manager = NotificationManagerCompat.from(context)

        // Android 13 permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("screen", "inventory_low")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // CHILD NOTIFICATIONS
        products.forEach { product ->

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_toko)
                .setContentTitle("⚠ Stok Hampir Habis")
                .setContentText(product)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(GROUP_ID)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            manager.notify(product.hashCode(), notification)
        }

        // SUMMARY NOTIFICATION
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_toko)
            .setContentTitle("⚠ ${products.size} Produk perlu restock")
            .setContentText("Tarik untuk melihat detail")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(GROUP_ID)
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(9999, summaryNotification)
    }
}