package com.example.neotokopos85.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.neotokopos85.R
import com.example.neotokopos85.ui.viewmodel.AuthViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.neotokopos85.ui.theme.Cinzel

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {

    val role by authViewModel.userRole.collectAsState()

    // ================= ANIMATION =================

    val logoScale = remember { Animatable(0.6f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {

        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(800, easing = FastOutSlowInEasing)
        )

        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(800)
        )

        delay(1000)

        if (role != null) {

            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }

        } else {

            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // ================= UI =================

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF4E6),
                        Color(0xFFEAD3B7)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo_toko),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BerasKila",
                fontFamily = Cinzel,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                }
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "POS & Inventory System",
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha.value
                }
            )
        }
    }
}