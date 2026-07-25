package com.example.neotokopos85.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavController
import com.example.neotokopos85.R
import com.example.neotokopos85.ui.viewmodel.AuthViewModel
import com.example.neotokopos85.ui.components.Alert
import androidx.compose.animation.core.*
import kotlin.math.roundToInt
import com.example.neotokopos85.ui.theme.Cinzel


@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    navController: NavController
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val role by authViewModel.userRole.collectAsState()

    /**
     * SHAKE ANIMATION
     */
    var shakeTrigger by remember { mutableIntStateOf(0) }
    val shake = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {

        if (shakeTrigger == 0) return@LaunchedEffect

        shake.snapTo(0f)

        shake.animateTo(16f, tween(40))
        shake.animateTo(-16f, tween(40))
        shake.animateTo(12f, tween(40))
        shake.animateTo(-12f, tween(40))
        shake.animateTo(0f, tween(40))
    }

    /**
     * LOGO FLOATING ANIMATION
     */
    val infinite = rememberInfiniteTransition()

    val logoOffset by infinite.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    /**
     * AUTO LOGIN
     */
    LaunchedEffect(role) {
        if (!role.isNullOrEmpty()) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFF4E7D4),
                        Color(0xFFE9D4B8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            /**
             * FLOATING LOGO
             */
            Image(
                painter = painterResource(id = R.drawable.logo_toko),
                contentDescription = "Logo",
                modifier = Modifier
                    .offset(y = logoOffset.dp)
                    .size(90.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "BerasKila",
                fontFamily = Cinzel,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "POS & Inventory System",
                fontSize = 14.sp,
                color = Color(0xFF8D6E63),
            )

            Spacer(modifier = Modifier.height(32.dp))

            /**
             * GLASSMORPHISM CARD
             */
            Card(
                modifier = Modifier
                    .offset { IntOffset(shake.value.roundToInt(), 0) },
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(12.dp),
                shape = RoundedCornerShape(28.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    /**
                     * EMAIL FIELD
                     */
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            emailError = false
                        },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null)
                        },
                        isError = emailError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    /**
                     * PASSWORD FIELD
                     */
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordError = false
                        },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null)
                        },
                        trailingIcon = {

                            IconButton(
                                onClick = { showPassword = !showPassword }
                            ) {

                                Icon(
                                    if (showPassword)
                                        Icons.Default.Visibility
                                    else
                                        Icons.Default.VisibilityOff,
                                    null
                                )
                            }
                        },
                        visualTransformation =
                            if (showPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                        isError = passwordError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    /**
                     * LOGIN BUTTON
                     */
                    Button(
                        onClick = {

                            if (email.isBlank()) {
                                emailError = true
                                shakeTrigger++
                                Alert.error("Email harus diisi")
                                return@Button
                            }

                            if (password.isBlank()) {
                                passwordError = true
                                shakeTrigger++
                                Alert.error("Password harus diisi")
                                return@Button
                            }

                            isLoading = true

                            authViewModel.login(
                                email,
                                password,

                                onSuccess = {

                                    isLoading = false

                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },

                                onError = { error ->

                                    isLoading = false
                                    passwordError = true

                                    shakeTrigger++

                                    Alert.error(error)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(50),
                        enabled = !isLoading
                    ) {

                        if (isLoading) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )

                        } else {

                            Text("Login")
                        }
                    }
                }
            }
        }
    }
}