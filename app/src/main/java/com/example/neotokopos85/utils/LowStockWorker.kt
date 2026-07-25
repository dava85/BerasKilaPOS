package com.example.neotokopos85.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks

class LowStockWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {

        val firestore = FirebaseFirestore.getInstance()
        val lowStockList = mutableListOf<String>()

        return try {

            val result = Tasks.await(
                firestore.collection("inventory").get()
            )

            for (doc in result) {

                val productName = doc.getString("name") ?: "Produk"
                val id = doc.id

                val variants = doc.get("variants") as? Map<String, Any> ?: emptyMap()

                variants.forEach { (variant, value) ->

                    val stock = when (value) {
                        is Long -> value.toInt()
                        is Double -> value.toInt()
                        else -> 0
                    }

                    val key = "${id}_$variant"

                    val threshold = when (variant) {
                        "karung25" -> 2
                        "karung10" -> 3
                        "karung5" -> 5
                        "pcs" -> 10
                        else -> 5
                    }

                    if (stock in 1..threshold) {

                        if (LowStockTracker.shouldNotify(applicationContext, key)) {

                            val variantLabel = when (variant) {
                                "karung25" -> "Karung 25kg"
                                "karung10" -> "Karung 10kg"
                                "karung5" -> "Karung 5kg"
                                "pcs" -> "Pcs"
                                "kg" -> "Kg"
                                "liter" -> "Liter"
                                else -> variant
                            }

                            lowStockList.add("$productName $variantLabel tersisa $stock")
                        }

                    } else {

                        LowStockTracker.reset(applicationContext, key)
                    }
                }
            }

            if (lowStockList.isNotEmpty()) {

                NotificationHelper.showGroupedLowStockNotification(
                    applicationContext,
                    lowStockList
                )
            }

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()
            Result.retry()
        }
    }
}