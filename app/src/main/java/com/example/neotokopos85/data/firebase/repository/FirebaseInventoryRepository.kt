package com.example.neotokopos85.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.neotokopos85.ui.viewmodel.CartItem

class FirestoreInventoryRepository {

    private val db = FirebaseFirestore.getInstance()

    // =============================
    // REDUCE MULTIPLE STOCK
    // =============================
    suspend fun reduceMultipleStock(
        cartItems: List<CartItem>
    ) {

        db.runTransaction { transaction ->

            cartItems.forEach { item ->

                val inventoryRef = db.collection("inventory")
                    .document(item.product.inventoryId)

                val snapshot = transaction.get(inventoryRef)

                var eceranStock =
                    snapshot.getDouble("eceranStockKg") ?: 0.0

                var karung25 =
                    snapshot.getLong("stockKarung25") ?: 0

                var karung10 =
                    snapshot.getLong("stockKarung10") ?: 0

                var karung5 =
                    snapshot.getLong("stockKarung5") ?: 0

                when (item.variantType) {

                    // =============================
                    // KARUNG 25
                    // =============================
                    "karung25" -> {

                        if (karung25 < item.quantity) {
                            throw Exception("Stok karung 25 tidak cukup")
                        }

                        transaction.update(
                            inventoryRef,
                            "stockKarung25",
                            karung25 - item.quantity
                        )
                    }

                    // =============================
                    // KARUNG 10
                    // =============================
                    "karung10" -> {

                        if (karung10 < item.quantity) {
                            throw Exception("Stok karung 10 tidak cukup")
                        }

                        transaction.update(
                            inventoryRef,
                            "stockKarung10",
                            karung10 - item.quantity
                        )
                    }

                    // =============================
                    // KARUNG 5
                    // =============================
                    "karung5" -> {

                        if (karung5 < item.quantity) {
                            throw Exception("Stok karung 5 tidak cukup")
                        }

                        transaction.update(
                            inventoryRef,
                            "stockKarung5",
                            karung5 - item.quantity
                        )
                    }

                    // =============================
                    // KG / LITER
                    // =============================
                    "kg", "liter" -> {

                        val kgPerLiter =
                            snapshot.getDouble("kgPerLiter") ?: 0.7

                        val soldKg =
                            if (item.variantType == "liter")
                                item.quantity * kgPerLiter
                            else
                                item.quantity.toDouble()

                        // 🔥 AUTO BUKA KARUNG
                        while (eceranStock < soldKg) {

                            if (karung25 <= 0) {
                                throw Exception("Stok habis")
                            }

                            eceranStock += 25
                            karung25 -= 1
                        }

                        transaction.update(
                            inventoryRef,
                            "stockKarung25",
                            karung25
                        )

                        transaction.update(
                            inventoryRef,
                            "eceranStockKg",
                            eceranStock - soldKg
                        )
                    }
                }
            }

        }.await()
    }
}