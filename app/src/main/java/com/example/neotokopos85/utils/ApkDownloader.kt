package com.example.neotokopos85.utils

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ApkDownloader {

    fun download(
        context: Context,
        url: String,
        onProgress: (Int) -> Unit,
        onFinish: (File) -> Unit
    ) {

        Thread {

            val connection = URL(url).openConnection()
            connection.connect()

            val length = connection.contentLength

            val input = connection.getInputStream()

            val file = File(
                context.getExternalFilesDir(null),
                "update.apk"
            )

            val output = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var total = 0
            var count: Int

            while (input.read(buffer).also { count = it } != -1) {

                total += count

                val progress = (total * 100 / length)

                onProgress(progress)

                output.write(buffer, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            onFinish(file)

        }.start()
    }
}