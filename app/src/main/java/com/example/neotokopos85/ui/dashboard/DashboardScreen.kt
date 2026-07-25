package com.example.neotokopos85.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.neotokopos85.ui.viewmodel.OrderViewModel
import com.example.neotokopos85.ui.viewmodel.ProductViewModel
import com.example.neotokopos85.ui.viewmodel.CartViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import com.example.neotokopos85.R
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Search
import com.example.neotokopos85.ui.theme.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.BarChart
import com.example.neotokopos85.ui.theme.Cinzel
import androidx.compose.runtime.remember
import com.example.neotokopos85.ui.viewmodel.InventoryViewModel
import com.example.neotokopos85.data.local.entity.InventoryEntity
import com.example.neotokopos85.ui.viewmodel.AuthViewModel
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.neotokopos85.ui.viewmodel.CashViewModel





@Composable
fun DashboardScreen(
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    cartViewModel: CartViewModel,
    navController: NavController,
    inventoryViewModel: InventoryViewModel,
    cashViewModel: CashViewModel,
    authViewModel: AuthViewModel
) {


    val cartItems by cartViewModel.cartItems.collectAsState()
    val context = LocalContext.current

    fun calculateTotalStockKg(inventory: InventoryEntity): Double {

        val karung25 = (inventory.variants["karung25"] ?: 0) * 25
        val karung10 = (inventory.variants["karung10"] ?: 0) * 10
        val karung5 = (inventory.variants["karung5"] ?: 0) * 5

        return karung25 + karung10 + karung5 + inventory.eceranStockKg
    }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var isExporting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val grossRevenue by orderViewModel.grossRevenue.collectAsState()
    val netRevenue by orderViewModel.netRevenue.collectAsState()
    val totalDiscount by orderViewModel.totalDiscount.collectAsState()

    val role by authViewModel.userRole.collectAsState()

    LaunchedEffect(Unit) {
        orderViewModel.startListeningOrders()
    }

    LaunchedEffect(role) {

        if (role == null) return@LaunchedEffect

        if (role == "logout") {

            navController.navigate("login") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }
    Scaffold(

        containerColor = MaterialTheme.colorScheme.background,

        topBar = {

            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color(0xFFE6C2A6)
            )

            Surface(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFEBD6),
                shadowElevation = 8.dp
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(modifier = Modifier.weight(1f)) {
                        DashboardHeader()
                    }

                    IconButton(
                        onClick = {

                            authViewModel.logout()

                            navController.navigate("login") {
                                popUpTo("dashboard") { inclusive = true }
                            }
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.Black
                        )
                    }
                }
            }
        },

        // ==========================
        // 🔥 BOTTOM BAR TRANSAKSI
        // ==========================
        bottomBar = {

            Column {



                // NAVIGATION BAR TETAP
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {

                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.Search, null) },
                        label = { Text("Kasir") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.BarChart, null) },
                        label = { Text("Statistik") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.Receipt, null) },
                        label = { Text("Riwayat") }
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(Icons.Default.Wallet, null) },
                        label = { Text("Kas") }
                    )

                    if (role == "gudang") {

                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("product") },
                            icon = { Icon(Icons.Default.Add, null) },
                            label = { Text("Tambah") }
                        )

                        NavigationBarItem(
                            selected = false,
                            onClick = { navController.navigate("inventory") },
                            icon = { Icon(Icons.Default.Warehouse, null) },
                            label = { Text("Gudang") }
                        )
                    }
                }
            }
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            GradientTop,
                            GradientBottom
                        )
                    )
                )
                .padding(padding)
        ) {

            when (selectedTab) {

                0 -> {
                    KasirTab(
                        productViewModel = productViewModel,
                        cartViewModel = cartViewModel,
                        orderViewModel = orderViewModel,
                        inventoryViewModel = inventoryViewModel
                    )
                }

                1 -> {
                    StatistikTab(
                        orderViewModel = orderViewModel
                    )
                }

                2 -> {
                    TransactionHistoryTab(
                        orderViewModel = orderViewModel,
                        navController = navController
                    )
                }

                3 -> {
                    CashTab(
                        orderViewModel = orderViewModel,
                        cashViewModel = cashViewModel
                    )
                }
            }

        }
    }

}
@Composable
    fun DashboardHeader() {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_toko),
                contentDescription = "Logo NeoToko",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {

                Text(
                    text = "BerasKila",
                    fontFamily = Cinzel,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color(0xFF5A3E2B) // coklat lembut
                )

                Text(
                    text = "POS & inventory System",
                    fontSize = 11.sp,
                    color = Color(0xFF9C7A5E)
                )
            }
        }
    }