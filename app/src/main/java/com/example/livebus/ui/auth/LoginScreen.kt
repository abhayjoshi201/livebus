package com.example.livebus.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun LoginScreen(
    onLoginSuccess: (username: String, role: String) -> Unit,
    authRepository: com.example.livebus.data.AuthRepository
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isDriverMode by remember { mutableStateOf(false) } // false = Commuter, true = Driver
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep Slate background
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GEHU LiveBus",
                    color = Color(0xFFFACC15), // Gold
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "College Transit Tracking",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val activeColor = Color(0xFF3B82F6)
                    val inactiveColor = Color(0xFF475569)

                    Button(
                        onClick = { 
                            isDriverMode = false 
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isDriverMode) activeColor else inactiveColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Commuter", color = Color.White)
                    }

                    Button(
                        onClick = { 
                            isDriverMode = true 
                            errorMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDriverMode) activeColor else inactiveColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Driver Shift", color = Color.White)
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        errorMessage = null
                    },
                    label = { Text(if (isDriverMode) "Username" else "Student / Staff ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isDriverMode) KeyboardType.Text else KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it
                        errorMessage = null
                    },
                    label = { Text(if (isDriverMode) "Password" else "Student / Staff ID (Password)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFEF4444),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFFACC15))
                } else {
                    Button(
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = authRepository.login(username, password)
                                isLoading = false
                                result.fold(
                                    onSuccess = { loginResult ->
                                        onLoginSuccess(loginResult.username, loginResult.role)
                                    },
                                    onFailure = {
                                        errorMessage = it.message ?: "Authentication failed"
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "LOGIN",
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}
