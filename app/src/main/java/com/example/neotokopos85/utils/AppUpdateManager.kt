package com.example.neotokopos85.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore

object AppUpdateManager {

    fun checkUpdate(context: Context, currentVersion: Int) {

        FirebaseFirestore.getInstance()
            .collection("app_config")
            .document("version")
            .get()
            .addOnSuccessListener { doc ->

                val latestVersion =
                    doc.getLong("latest_version")?.toInt() ?: return@addOnSuccessListener

                val apkUrl =
                    doc.getString("apk_url") ?: return@addOnSuccessListener

                val message =
                    doc.getString("message") ?: "Update tersedia"

                if (latestVersion > currentVersion) {

                    showUpdateDialog(context, apkUrl, message)
                }
            }
    }

    private fun showUpdateDialog(
        context: Context,
        apkUrl: String,
        message: String
    ) {

        AlertDialog.Builder(context)
            .setTitle("Update Aplikasi")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Update") { _, _ ->

                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(apkUrl)
                context.startActivity(intent)
            }
            .show()
    }
}