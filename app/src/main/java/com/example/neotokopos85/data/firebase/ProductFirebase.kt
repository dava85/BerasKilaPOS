package com.example.neotokopos85.data.firebase

data class ProductFirestore(

    val id: String = "",

    val name: String = "",

    val category: String = "",

    val imageUrl: String = "",

    // 🔥 Harga berdasarkan varian
    val prices: Map<String, Int> = emptyMap(),

    // 🔥 Inventory yang terhubung
    val inventoryId: String = ""

)