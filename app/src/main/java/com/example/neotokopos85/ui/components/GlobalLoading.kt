package com.example.neotokopos85.ui.components

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object Loading {

    var visible by mutableStateOf(false)

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}

@Composable
fun GlobalLoading() {

    if (Loading.visible) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            Card {

                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.width(16.dp))

                    Text("Memproses...")
                }
            }
        }
    }
}