package com.example.neotokopos85.data.local.entity

import com.google.firebase.firestore.Exclude

data class OrderEntity(

    @get:Exclude
    var id: String = "",

    var customerName: String = "",
    var customerAddress: String = "",
    var totalPrice: Double = 0.0,
    var totalItems: Int = 0,
    var timestamp: Long = 0L,
    var totalDiscount: Double = 0.0,
    var items: List<OrderItem> = emptyList()
)

data class OrderItem(

    var productName: String = "",
    var variantKg: String = "",
    var quantity: Int = 0,
    var subtotal: Double = 0.0,
    var discount: Double = 0.0

)