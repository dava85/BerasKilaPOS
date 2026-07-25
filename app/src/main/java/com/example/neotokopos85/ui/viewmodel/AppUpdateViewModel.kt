package com.example.neotokopos85.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.neotokopos85.utils.AppUpdateState
import com.google.firebase.firestore.FirebaseFirestore

class AppUpdateViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    var state = mutableStateOf(AppUpdateState())
        private set

    fun checkUpdate(currentVersion: Int) {

        db.collection("app_config")
            .document("version")
            .get()
            .addOnSuccessListener { doc ->

                val latest =
                    doc.getLong("latest_version")?.toInt() ?: return@addOnSuccessListener

                if (latest > currentVersion) {

                    state.value = state.value.copy(
                        updateAvailable = true,
                        apkUrl = doc.getString("apk_url") ?: "",
                        message = doc.getString("message") ?: "Update tersedia"
                    )
                }
            }
    }

    fun setDownloading(progress: Int) {

        state.value = state.value.copy(
            downloading = true,
            progress = progress
        )
    }
}