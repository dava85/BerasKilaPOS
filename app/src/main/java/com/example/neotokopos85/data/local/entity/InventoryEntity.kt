package com.example.neotokopos85.data.local.entity

data class InventoryEntity(

    val id: String = "",
    val name: String = "",

    // stok per varian
    val variants: Map<String, Int> = emptyMap(),

    // stok eceran dari karung 25
    val eceranStockKg: Double = 0.0,

    // konversi kg ke liter
    val kgPerLiter: Double = 0.7,

    val prices: Map<String, Long> = emptyMap()

) {

    companion object {

        const val KARUNG25 = "karung25"
        const val KARUNG10 = "karung10"
        const val KARUNG5 = "karung5"
        const val PCS = "pcs"
    }

    fun getStock(variant: String): Int {
        return variants[variant] ?: 0
    }

    fun hasVariant(variant: String): Boolean {
        return variants.containsKey(variant)
    }

    fun hasKarung25(): Boolean {
        return variants.containsKey(KARUNG25)
    }

    fun canSellEceran(): Boolean {
        return hasKarung25()
    }

    fun eceranLiter(): Double {
        return if (kgPerLiter == 0.0) 0.0 else eceranStockKg / kgPerLiter
    }
}