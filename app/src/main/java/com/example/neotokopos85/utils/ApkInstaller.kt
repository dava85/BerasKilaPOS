package com.example.neotokopos85.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun installApk(context: Context, file: File) {

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )

    val intent = Intent(Intent.ACTION_VIEW)

    intent.setDataAndType(
        uri,
        "application/vnd.android.package-archive"
    )

    intent.flags =
        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_ACTIVITY_NEW_TASK

    context.startActivity(intent)
}