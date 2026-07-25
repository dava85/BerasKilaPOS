package com.example.neotokopos85.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.neotokopos85.data.local.entity.ExpenseEntity
import com.example.neotokopos85.data.local.entity.SetoranEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CashViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses

    private val _setorans = MutableStateFlow<List<SetoranEntity>>(emptyList())
    val setorans: StateFlow<List<SetoranEntity>> = _setorans

    init {
        listenCash()
    }

    private fun listenCash() {

        if (listener != null) return

        listener = db.collection("cash_transactions")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                val exp = mutableListOf<ExpenseEntity>()
                val set = mutableListOf<SetoranEntity>()

                for (doc in snapshot.documents) {

                    val type = doc.getString("type") ?: continue
                    val amount = doc.getDouble("amount") ?: 0.0
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    if (type == "expense") {
                        exp.add(
                            ExpenseEntity(
                                name = doc.getString("name") ?: "",
                                amount = amount,
                                timestamp = timestamp
                            )
                        )
                    } else {
                        set.add(
                            SetoranEntity(
                                amount = amount,
                                timestamp = timestamp
                            )
                        )
                    }
                }

                _expenses.value = exp
                _setorans.value = set
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    fun addExpense(name: String, amount: Double) {
        val data = hashMapOf(
            "type" to "expense",
            "name" to name,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("cash_transactions").add(data)
    }

    fun addSetoran(amount: Double) {
        val data = hashMapOf(
            "type" to "setoran",
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("cash_transactions").add(data)
    }
}