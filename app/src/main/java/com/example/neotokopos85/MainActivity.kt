package com.example.neotokopos85

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.FirebaseApp

import com.example.neotokopos85.ui.navigation.AppNavGraph
import com.example.neotokopos85.ui.theme.NeoTokoTheme
import com.example.neotokopos85.ui.viewmodel.*
import com.example.neotokopos85.data.firebase.repository.FirestoreProductRepository
import com.example.neotokopos85.utils.NotificationHelper
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.neotokopos85.utils.LowStockWorker
import com.example.neotokopos85.ui.components.*
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)

        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }


        // 🔔 Create notification channel
        NotificationHelper.createChannel(this)


        // 🔔 Background worker cek stok
        val workRequest =
            PeriodicWorkRequestBuilder<LowStockWorker>(
                30,
                TimeUnit.MINUTES
            ).build()

        WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                "low_stock_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        val screen = intent.getStringExtra("screen") ?: "dashboard"
        val productId = intent.getStringExtra("productId") ?: ""

        val firestoreRepository = FirestoreProductRepository()
        val productFactory = ProductViewModelFactory(firestoreRepository)

        setContent {
            NeoTokoTheme {

                val updateViewModel: AppUpdateViewModel = viewModel()

                LaunchedEffect(Unit) {
                    updateViewModel.checkUpdate(versionCode)
                }

                UpdateDialog(updateViewModel)

                GlobalSnackbarHost {

                    val productViewModel: ProductViewModel =
                        viewModel(factory = productFactory)

                    val cartViewModel: CartViewModel = viewModel()
                    val orderViewModel: OrderViewModel = viewModel()
                    val inventoryViewModel: InventoryViewModel = viewModel()
                    val cashViewModel: CashViewModel = viewModel()

                    AppNavGraph(
                        productViewModel = productViewModel,
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel,
                        inventoryViewModel = inventoryViewModel,
                        startScreen = screen,
                        cashViewModel = cashViewModel,
                        productId = productId
                    )

                    GlobalLoading()

                    GlobalConfirmDialog()
                }
            }

            // 🔔 Android 13+ permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {

                    requestPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
            }
        }
    }
}