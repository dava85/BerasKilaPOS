package com.example.neotokopos85.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

object Alert {

    lateinit var snackbarHostState: SnackbarHostState

    var loading by mutableStateOf(false)
    var confirmMessage by mutableStateOf<String?>(null)
    var confirmAction: (() -> Unit)? = null

    fun success(message: String) {
        show(message)
    }

    fun error(message: String) {
        show(message)
    }

    fun loading(show: Boolean) {
        loading = show
    }

    fun confirm(
        message: String,
        onConfirm: () -> Unit
    ) {
        confirmMessage = message
        confirmAction = onConfirm
    }

    private fun show(message: String) {

        if (::snackbarHostState.isInitialized) {

            snackbarHostState
                .currentSnackbarData
                ?.dismiss()

            kotlinx.coroutines.CoroutineScope(
                kotlinx.coroutines.Dispatchers.Main
            ).launch {

                snackbarHostState.showSnackbar(message)
            }
        }
    }
}

@Composable
fun GlobalSnackbarHost(
    content: @Composable () -> Unit
) {

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Alert.snackbarHostState = snackbarHostState
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            content()

            /**
             * 🔥 LOADING GLOBAL
             */
            if (Alert.loading) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {

                    Card {

                        Column(
                            modifier = Modifier
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            CircularProgressIndicator()

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Loading...")
                        }
                    }
                }
            }

            /**
             * 🔥 CONFIRM GLOBAL
             */
            Alert.confirmMessage?.let { message ->

                AlertDialog(
                    onDismissRequest = {
                        Alert.confirmMessage = null
                    },
                    title = {
                        Text("Konfirmasi")
                    },
                    text = {
                        Text(message)
                    },
                    confirmButton = {

                        TextButton(
                            onClick = {

                                Alert.confirmAction?.invoke()

                                Alert.confirmMessage = null
                            }
                        ) {
                            Text("Ya")
                        }
                    },
                    dismissButton = {

                        TextButton(
                            onClick = {
                                Alert.confirmMessage = null
                            }
                        ) {
                            Text("Batal")
                        }
                    }
                )
            }
        }
    }
}