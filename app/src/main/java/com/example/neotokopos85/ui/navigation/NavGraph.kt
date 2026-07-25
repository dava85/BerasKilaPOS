package com.example.neotokopos85.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.neotokopos85.ui.cart.CartScreen
import com.example.neotokopos85.ui.dashboard.DashboardScreen
import com.example.neotokopos85.ui.product.ProductScreen
import com.example.neotokopos85.ui.management.ManagementScreen
import com.example.neotokopos85.ui.detail.DetailScreen
import com.example.neotokopos85.ui.checkout.CheckoutScreen
import com.example.neotokopos85.ui.checkout.SuccessScreen
import com.example.neotokopos85.ui.order.OrderHistoryScreen
import com.example.neotokopos85.ui.dashboard.AdminPanelScreen
import com.example.neotokopos85.ui.inventory.InventoryScreen
import com.example.neotokopos85.ui.auth.LoginScreen
import com.example.neotokopos85.ui.riwayat.TransactionDetailScreen
import com.example.neotokopos85.ui.splash.SplashScreen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

import com.example.neotokopos85.ui.viewmodel.*

@Composable
fun AppNavGraph(
    productViewModel: ProductViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    startScreen: String?,
    productId: String?,
    cashViewModel: CashViewModel,
    inventoryViewModel: InventoryViewModel
) {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val role by authViewModel.userRole.collectAsState()

    /**
     * 🔔 Navigasi dari NOTIFIKASI
     */
    LaunchedEffect(startScreen, role) {

        if (startScreen == "inventory_low" && role != null) {

            if (role == "gudang") {

                navController.navigate(
                    Screen.Inventory.route + "?highlightId=${productId ?: ""}"
                )

            } else {

                navController.navigate(Screen.Dashboard.route)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash",

        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(280)
            ) + fadeIn()
        },

        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(280)
            ) + fadeOut()
        },

        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(280)
            ) + fadeIn()
        },

        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(280)
            ) + fadeOut()
        }
    ) {

        /**
         * SPLASH
         */
        composable("splash") {
            SplashScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        /**
         * LOGIN
         */
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        /**
         * DASHBOARD
         */
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                productViewModel = productViewModel,
                orderViewModel = orderViewModel,
                cartViewModel = cartViewModel,
                navController = navController,
                inventoryViewModel = inventoryViewModel,
                authViewModel = authViewModel,
                cashViewModel = cashViewModel
            )
        }

        /**
         * PRODUCT
         */
        composable(Screen.Product.route) {
            ProductScreen(
                viewModel = productViewModel,
                navController = navController,
                inventoryViewModel = inventoryViewModel
            )
        }

        /**
         * CART
         */
        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel,
                navController
            )
        }

        /**
         * MANAGEMENT
         */
        composable(Screen.Management.route) {
            ManagementScreen(
                cartViewModel,
                navController
            )
        }

        /**
         * DETAIL PRODUK
         */
        composable("detail/{productId}") { backStackEntry ->

            val id =
                backStackEntry.arguments?.getString("productId") ?: ""

            DetailScreen(
                productId = id,
                productViewModel = productViewModel,
                navController = navController
            )
        }

        /**
         * CHECKOUT
         */
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                cartViewModel,
                navController,
                orderViewModel
            )
        }

        /**
         * SUCCESS
         */
        composable(Screen.Success.route) {
            SuccessScreen(navController)
        }

        /**
         * ORDER HISTORY
         */
        composable(Screen.OrderHistory.route) {
            OrderHistoryScreen(
                orderViewModel,
                navController
            )
        }

        /**
         * ADMIN PANEL
         */
        composable(Screen.Admin.route) {
            AdminPanelScreen(orderViewModel)
        }

        /**
         * INVENTORY
         */
        composable(
            route = Screen.Inventory.route + "?highlightId={highlightId}"
        ) { backStackEntry ->

            val highlightId =
                backStackEntry.arguments?.getString("highlightId")

            InventoryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = inventoryViewModel,
                highlightId = highlightId
            )
        }

        /**
         * DETAIL TRANSAKSI
         */
        composable("transaction_detail/{orderId}") { backStackEntry ->

            val orderId =
                backStackEntry.arguments?.getString("orderId")

            TransactionDetailScreen(
                orderId = orderId,
                orderViewModel = orderViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}