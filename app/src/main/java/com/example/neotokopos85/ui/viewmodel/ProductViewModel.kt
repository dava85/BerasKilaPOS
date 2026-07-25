package com.example.neotokopos85.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neotokopos85.data.firebase.ProductFirestore
import com.example.neotokopos85.data.firebase.repository.FirestoreProductRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: FirestoreProductRepository
) : ViewModel() {

    val products: StateFlow<List<ProductFirestore>> =
        repository.products

    init {
        repository.startListeningProducts()
    }

    // =============================
    // CREATE PRODUCT
    // =============================
    fun addProduct(
        name: String,
        category: String,
        imageUrl: String,
        prices: Map<String, Int>,
        inventoryId: String
    ) {

        viewModelScope.launch {

            val product = ProductFirestore(
                name = name,
                category = category,
                imageUrl = imageUrl,
                prices = prices,
                inventoryId = inventoryId
            )

            repository.addProduct(product)
        }
    }

    // =============================
    // UPDATE PRODUCT
    // =============================
    fun updateProduct(
        id: String,
        name: String,
        category: String,
        imageUrl: String,
        prices: Map<String, Int>,
        inventoryId: String
    ) {

        viewModelScope.launch {

            val product = ProductFirestore(
                id = id,
                name = name,
                category = category,
                imageUrl = imageUrl,
                prices = prices,
                inventoryId = inventoryId
            )

            repository.updateProduct(product)
        }
    }

    // =============================
    // DELETE PRODUCT
    // =============================
    fun deleteProduct(id: String) {

        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    // =============================
    // CLEANUP
    // =============================
    override fun onCleared() {
        super.onCleared()
        repository.removeListener()
    }
}