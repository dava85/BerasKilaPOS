package com.example.neotokopos85.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.neotokopos85.ui.viewmodel.AppUpdateViewModel
import com.example.neotokopos85.utils.ApkDownloader
import com.example.neotokopos85.utils.installApk

@Composable
fun UpdateDialog(viewModel: AppUpdateViewModel) {

    val state = viewModel.state.value
    val context = LocalContext.current

    if (!state.updateAvailable) return

    AlertDialog(
        onDismissRequest = {},

        title = {
            Text("Update tersedia")
        },

        text = {

            Column {

                Text(state.message)

                if (state.downloading) {

                    Spacer(Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = state.progress / 100f
                    )

                    Text("${state.progress}%")
                }
            }
        },

        confirmButton = {

            if (!state.downloading) {

                Button(
                    onClick = {

                        ApkDownloader.download(
                            context,
                            state.apkUrl,

                            onProgress = {
                                viewModel.setDownloading(it)
                            },

                            onFinish = { file ->
                                installApk(context, file)
                            }
                        )
                    }
                ) {
                    Text("Download Update")
                }
            }
        }
    )
}