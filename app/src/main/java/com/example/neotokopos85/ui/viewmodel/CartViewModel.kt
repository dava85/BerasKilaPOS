package com.example.neotokopos85.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.neotokopos85.data.firebase.ProductFirestore
import kotlinx.coroutines.flow.update

// =========================
// CART ITEM
// =========================
data class CartItem(
    val product: ProductFirestore,
    val variantType: String,   // kg, liter, karung5, karung10, karung25
    val variantWeight: Double, // berat dalam kg
    val quantity: Int = 1,
    val discount: Int = 0,
    val manualPrice: Int? = null
)

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    // =========================
    // TAMBAH KE CART
    // =========================
    fun addToCart(
        product: ProductFirestore,
        variantType: String,
        variantWeight: Double
    ) {

        val existing = _cartItems.value.find {
            it.product.id == product.id &&
                    it.variantType == variantType
        }

        if (existing != null) {

            _cartItems.value = _cartItems.value.map {

                if (it.product.id == product.id &&
                    it.variantType == variantType
                ) {
                    it.copy(quantity = it.quantity + 1)
                } else it
            }

        } else {

            val newItem = CartItem(
                product = product,
                variantType = variantType,
                variantWeight = variantWeight,
                quantity = 1
            )

            _cartItems.value = _cartItems.value + newItem
        }
    }

    // =========================
    // TAMBAH JUMLAH
    // =========================
    fun increaseQuantity(
        product: ProductFirestore,
        variantType: String
    ) {

        _cartItems.value = _cartItems.value.map {

            if (it.product.id == product.id &&
                it.variantType == variantType
            ) {
                it.copy(quantity = it.quantity + 1)
            } else it
        }
    }

    // =========================
    // KURANG JUMLAH
    // =========================
    fun decreaseQuantity(
        product: ProductFirestore,
        variantType: String
    ) {

        _cartItems.value = _cartItems.value.mapNotNull {

            if (it.product.id == product.id &&
                it.variantType == variantType
            ) {

                val newQty = it.quantity - 1

                if (newQty > 0) {
                    it.copy(quantity = newQty)
                } else {
                    null
                }

            } else it
        }
    }

    // =========================
    // CLEAR CART
    // =========================
    fun clearCart() {
        _cartItems.value = emptyList()
    }

    // =========================
    // TOTAL HARGA
    // =========================
    fun getTotalPrice(): Double {

        return _cartItems.value.sumOf { item ->

            val price = item.product.prices[item.variantType] ?: 0

            val subtotal = price * item.quantity

            subtotal - item.discount
        }.toDouble()
    }

    // =========================
    // TOTAL ITEM
    // =========================
    fun getTotalItems(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }

    // =========================
    // TOTAL KG TERJUAL
    // =========================
    fun getTotalKg(): Double {

        return _cartItems.value.sumOf {
            it.variantWeight * it.quantity
        }
    }

    // =========================
    // UPDATE DISCOUNT
    // =========================
    fun updateDiscount(
        product: ProductFirestore,
        variantType: String,
        discount: Int
    ) {

        _cartItems.value = _cartItems.value.map {

            if (it.product.id == product.id &&
                it.variantType == variantType
            ) {

                val price = it.product.prices[it.variantType] ?: 0

                val subtotal = price * it.quantity

                val safeDiscount =
                    if (discount > subtotal) subtotal
                    else discount

                it.copy(discount = safeDiscount)

            } else it
        }
    }
    fun addNominal(
        product: ProductFirestore,
        weight: Double,
        price: Int
    ) {

        val item = CartItem(
            product = product,
            variantType = "nominal",
            variantWeight = weight,
            quantity = 1,
            manualPrice = price
        )

        _cartItems.update { it + item }
    }
    fun removeItem(
        product: ProductFirestore,
        variantType: String
    ) {

        _cartItems.update { list ->

            list.filterNot {

                it.product.id == product.id &&
                        it.variantType == variantType
            }
        }
    }
}