package com.example.neotokopos85.data.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.example.neotokopos85.data.local.entity.ExpenseEntity
import com.example.neotokopos85.data.local.entity.SetoranEntity

object CashFirestore {

    private val db = FirebaseFirestore.getInstance()

    fun addExpense(expense: ExpenseEntity, weekId: String) {

        val data = hashMapOf(
            "type" to "expense",
            "name" to expense.name,
            "amount" to expense.amount,
            "timestamp" to expense.timestamp,
            "weekId" to weekId
        )

        db.collection("cash_transactions").add(data)
    }

    fun addSetoran(setoran: SetoranEntity, weekId: String) {

        val data = hashMapOf(
            "type" to "setoran",
            "amount" to setoran.amount,
            "timestamp" to setoran.timestamp,
            "weekId" to weekId
        )

        db.collection("cash_transactions").add(data)
    }
}