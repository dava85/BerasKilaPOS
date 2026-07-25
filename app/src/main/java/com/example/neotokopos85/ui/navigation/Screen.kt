package com.example.neotokopos85.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Product : Screen("product")
    object Cart : Screen("cart")
    object Management : Screen("management")
    object Checkout : Screen("checkout")
    object Success : Screen("success")
    object OrderHistory : Screen("order_history")
    object Admin : Screen("admin")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(id: Int) = "detail/$id"
}
    object Inventory : Screen("inventory")
}
