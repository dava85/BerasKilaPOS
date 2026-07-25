package com.example.neotokopos85.ui.components

import androidx.compose.runtime.*
import androidx.compose.material3.*

object Confirm {

    fun reset() {
        show = false
        onConfirm = null
    }

    var show by mutableStateOf(false)

    var title by mutableStateOf("")
    var message by mutableStateOf("")

    var confirmText by mutableStateOf("OK")
    var dismissText by mutableStateOf("Batal")

    var onConfirm: (() -> Unit)? = null

    /**
     * 🔥 Konfirmasi umum
     */
    fun show(
        title: String,
        message: String,
        confirmText: String = "OK",
        dismissText: String = "Batal",
        action: () -> Unit
    ) {

        this.title = title
        this.message = message
        this.confirmText = confirmText
        this.dismissText = dismissText
        this.onConfirm = action

        show = true
    }

    /**
     * 🔥 Shortcut khusus hapus
     */
    fun delete(action: () -> Unit) {

        show(
            title = "Hapus Data",
            message = "Data akan dihapus permanen",
            confirmText = "Hapus",
            dismissText = "Batal",
            action = action
        )
    }
}

@Composable
fun GlobalConfirmDialog() {

    val isActive = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            isActive.value = false
        }
    }

    if (Confirm.show && isActive.value) {

        AlertDialog(
            onDismissRequest = {
                Confirm.show = false
            },
            title = { Text(Confirm.title) },
            text = { Text(Confirm.message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        Confirm.onConfirm?.invoke()
                        Confirm.show = false
                    }
                ) {
                    Text(Confirm.confirmText)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        Confirm.show = false
                    }
                ) {
                    Text(Confirm.dismissText)
                }
            }
        )
    }
}