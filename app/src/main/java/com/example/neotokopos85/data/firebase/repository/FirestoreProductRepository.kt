package com.example.neotokopos85.data.firebase.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.neotokopos85.data.firebase.ProductFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class FirestoreProductRepository {

    private val db = FirebaseFirestore.getInstance()

    private val _products =
        MutableStateFlow<List<ProductFirestore>>(emptyList())
    val products: StateFlow<List<ProductFirestore>> = _products

    private var listener: ListenerRegistration? = null

    // =============================
    // REALTIME LISTENER
    // =============================

    fun startListeningProducts() {

        listener = db.collection("products")
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener

                if (snapshot != null) {

                    val productList = snapshot.documents.mapNotNull { doc ->

                        doc.toObject(ProductFirestore::class.java)
                            ?.copy(id = doc.id)
                    }

                    _products.value = productList
                }
            }
    }

    // =============================
    // CREATE
    // =============================

    suspend fun addProduct(product: ProductFirestore) {

        db.collection("products")
            .add(product)
            .await()
    }

    // =============================
    // UPDATE
    // =============================

    suspend fun updateProduct(product: ProductFirestore) {

        db.collection("products")
            .document(product.id)
            .set(product)
            .await()
    }

    // =============================
    // DELETE
    // =============================

    suspend fun deleteProduct(id: String) {

        db.collection("products")
            .document(id)
            .delete()
            .await()
    }

    // =============================
    // CLEANUP
    // =============================

    fun removeListener() {
        listener?.remove()
    }
}