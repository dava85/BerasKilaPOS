package com.example.neotokopos85.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // =============================
    // PRODUCT STATE
    // =============================

    private val _products = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val products: StateFlow<List<Map<String, Any>>> = _products

    private var productListener: ListenerRegistration? = null

    fun startListeningProducts() {
        productListener = firestore.collection("products")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }
                    _products.value = list
                }
            }
    }

    fun addProduct(
        name: String,
        price: Double,
        stock: Double
    ) {

        val docRef = firestore.collection("products").document()
        val productId = docRef.id

        val product = hashMapOf(
            "id" to productId,   // ⬅ simpan id juga di dalam field
            "name" to name,
            "price" to price,
            "stock" to stock,
            "timestamp" to System.currentTimeMillis()
        )

        docRef.set(product)
    }

    fun deleteProduct(documentId: String) {
        firestore.collection("products")
            .document(documentId)
            .delete()
    }

    fun updateProduct(
        documentId: String,
        name: String,
        price: Double,
        stock: Double
    ) {
        val updateData = hashMapOf(
            "name" to name,
            "price" to price,
            "stock" to stock
        )

        firestore.collection("products")
            .document(documentId)
            .update(updateData as Map<String, Any>)
    }

    // =============================
    // REDUCE STOCK
    // =============================

    fun reduceStock(
        documentId: String,
        quantityKg: Double,
        onFailure: (String) -> Unit
    ) {

        val productRef = firestore.collection("products").document(documentId)

        firestore.runTransaction { transaction ->

            val snapshot = transaction.get(productRef)
            val currentStock = snapshot.getDouble("stock") ?: 0.0

            if (currentStock < quantityKg) {
                throw Exception("Stok tidak cukup")
            }

            val newStock = currentStock - quantityKg

            transaction.update(productRef, "stock", newStock)
        }
            .addOnFailureListener {
                onFailure(it.message ?: "Gagal kurangi stok")
            }
    }
    // =============================
    // SALES
    // =============================

    private val _sales = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val sales: StateFlow<List<Map<String, Any>>> = _sales

    private var salesListener: ListenerRegistration? = null

    fun startListeningSales() {
        salesListener = firestore.collection("sales")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }
                    _sales.value = list
                }
            }
    }

    fun insertSale(
        productId: String,
        quantity: Double,
        totalPrice: Double
    ) {

        val sale = hashMapOf(
            "productId" to productId,
            "quantity" to quantity,
            "totalPrice" to totalPrice,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("sales").add(sale)
    }

    fun clearListeners() {
        productListener?.remove()
        salesListener?.remove()
    }

}