package com.example.neotokopos85.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import com.example.neotokopos85.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InventoryViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _inventoryList =
        MutableStateFlow<List<InventoryEntity>>(emptyList())

    val inventoryList: StateFlow<List<InventoryEntity>> =
        _inventoryList.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        listenInventoryRealtime()
    }

    // ================= REALTIME LISTENER =================

    private fun listenInventoryRealtime() {

        listenerRegistration = firestore.collection("inventory")
            .addSnapshotListener { result, error ->

                if (error != null) return@addSnapshotListener
                if (result == null) return@addSnapshotListener

                val list = result.documents.map { doc ->

                    val prices =
                        doc.get("prices") as? Map<String, Long> ?: emptyMap()

                    val variants =
                        doc.get("variants") as? Map<String, Long> ?: emptyMap()

                    InventoryEntity(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        variants = variants.mapValues { it.value.toInt() },
                        eceranStockKg = doc.getDouble("eceranStockKg") ?: 0.0,
                        kgPerLiter = doc.getDouble("kgPerLiter") ?: 0.7,
                        prices = prices
                    )
                }

                _inventoryList.value = list
            }
    }

    // ================= RESTOCK =================

    fun restockVariant(
        id: String,
        variant: String,
        qty: Int
    ) {

        if (qty <= 0) return

        firestore.collection("inventory")
            .document(id)
            .update(
                "variants.$variant",
                FieldValue.increment(qty.toLong())
            )
    }

    // ================= JUAL KARUNG =================

    fun reduceKarungStock(
        inventoryId: String,
        variant: String,
        qty: Int
    ) {

        val docRef = firestore.collection("inventory").document(inventoryId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(docRef)

            val variants =
                snapshot.get("variants") as? Map<String, Long> ?: emptyMap()

            val current = variants[variant] ?: 0

            if (current < qty)
                throw Exception("Stok tidak cukup")

            transaction.update(
                docRef,
                "variants.$variant",
                current - qty
            )
        }
    }

    // ================= JUAL ECERAN =================

    fun reduceEceranStock(
        inventoryId: String,
        soldKg: Double
    ) {

        val docRef = firestore.collection("inventory").document(inventoryId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(docRef)

            var eceranStock =
                snapshot.getDouble("eceranStockKg") ?: 0.0

            val variants =
                snapshot.get("variants") as? Map<String, Long> ?: emptyMap()

            var karung25 =
                variants["karung25"] ?: 0

            while (eceranStock < soldKg) {

                if (karung25 <= 0)
                    throw Exception("Stok habis")

                eceranStock += 25
                karung25 -= 1

                transaction.update(
                    docRef,
                    "variants.karung25",
                    karung25
                )
            }

            transaction.update(
                docRef,
                "eceranStockKg",
                eceranStock - soldKg
            )
        }
    }

    // ================= TRANSAKSI KASIR =================

    fun reduceStockAfterPayment(
        cartItems: List<CartItem>,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {

        firestore.runTransaction { transaction ->

            val snapshots = mutableMapOf<String, com.google.firebase.firestore.DocumentSnapshot>()

            // Ambil snapshot inventory sekali per inventoryId
            cartItems.forEach { item ->

                val inventoryId = item.product.inventoryId

                if (!snapshots.containsKey(inventoryId)) {

                    val docRef =
                        firestore.collection("inventory").document(inventoryId)

                    val snapshot = transaction.get(docRef)

                    snapshots[inventoryId] = snapshot
                }
            }

            cartItems.forEach { item ->

                val inventoryId = item.product.inventoryId

                val snapshot = snapshots[inventoryId]!!

                val docRef =
                    firestore.collection("inventory").document(inventoryId)

                val variants =
                    snapshot.get("variants") as? Map<String, Long> ?: emptyMap()

                var eceranStock =
                    snapshot.getDouble("eceranStockKg") ?: 0.0

                var karung25 =
                    variants["karung25"] ?: 0

                when (item.variantType) {

                    // ========================
                    // JUAL KARUNG / PCS
                    // ========================
                    "pcs", "karung25", "karung10", "karung5" -> {

                        val current = variants[item.variantType] ?: 0

                        if (current < item.quantity)
                            throw Exception("Stok ${item.product.name} tidak cukup")

                        transaction.update(
                            docRef,
                            "variants.${item.variantType}",
                            current - item.quantity
                        )
                    }

                    // ========================
                    // JUAL KG / LITER / 0.5KG
                    // ========================
                    "kg", "0.5kg", "liter" -> {

                        val kgPerLiter =
                            snapshot.getDouble("kgPerLiter") ?: 0.7

                        val soldKg =
                            when (item.variantType) {

                                "liter" -> item.quantity * kgPerLiter

                                "0.5kg" -> item.quantity * 0.5

                                else -> item.quantity.toDouble()
                            }

                        while (eceranStock < soldKg) {

                            if (karung25 <= 0)
                                throw Exception("Stok ${item.product.name} habis")

                            eceranStock += 25
                            karung25 -= 1
                        }

                        transaction.update(
                            docRef,
                            "variants.karung25",
                            karung25
                        )

                        transaction.update(
                            docRef,
                            "eceranStockKg",
                            eceranStock - soldKg
                        )
                    }

                    // ========================
                    // NOMINAL (BERAT MANUAL)
                    // ========================
                    "nominal" -> {

                        val soldKg = item.variantWeight

                        while (eceranStock < soldKg) {

                            if (karung25 <= 0)
                                throw Exception("Stok ${item.product.name} habis")

                            eceranStock += 25
                            karung25 -= 1
                        }

                        transaction.update(
                            docRef,
                            "variants.karung25",
                            karung25
                        )

                        transaction.update(
                            docRef,
                            "eceranStockKg",
                            eceranStock - soldKg
                        )
                    }
                }
            }

        }.addOnSuccessListener {

            onSuccess()

        }.addOnFailureListener {

            onFailure(it.message ?: "Gagal update stok")
        }
    }

    // ================= DELETE =================

    fun deleteInventory(id: String) {

        firestore.collection("inventory")
            .document(id)
            .delete()
    }

    // ================= ADD INVENTORY =================

    fun addInventory(
        name: String,
        variants: Map<String, Int>,
        eceranStockKg: Double,
        kgPerLiter: Double
    ) {

        val data = mapOf(
            "name" to name,
            "variants" to variants,
            "eceranStockKg" to eceranStockKg,
            "kgPerLiter" to kgPerLiter,
            "prices" to mapOf(
                "pcs" to 0,
                "kg" to 0,
                "liter" to 0,
                "karung5" to 0,
                "karung10" to 0,
                "karung25" to 0
            )
        )

        firestore.collection("inventory")
            .add(data)
    }

    // ================= UPDATE STOCK =================

    fun updateStock(
        id: String,
        pcs: Int,
        eceran: Double,
        karung25: Int,
        karung10: Int,
        karung5: Int
    ) {

        val variants = mapOf(
            "pcs" to pcs,
            "karung25" to karung25,
            "karung10" to karung10,
            "karung5" to karung5
        )

        firestore.collection("inventory")
            .document(id)
            .update(
                mapOf(
                    "variants" to variants,
                    "eceranStockKg" to eceran
                )
            )
    }

    // ================= CLEANUP =================

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}