package com.example.neotokopos85.data.local.entity

data class ExpenseEntity(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)