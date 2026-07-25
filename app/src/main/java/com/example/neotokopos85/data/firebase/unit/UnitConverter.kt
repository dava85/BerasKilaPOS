package com.example.neotokopos85.data.firebase.unit

object UnitConverter {

    fun toKg(unit: String, quantity: Double): Double {
        return when (unit) {
            "KG" -> quantity
            "LITER" -> quantity * 0.8
            "KARUNG" -> quantity * 25.0
            else -> quantity
        }
    }
}