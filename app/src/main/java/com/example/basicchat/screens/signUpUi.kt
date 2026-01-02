package com.example.basicchat.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basicchat.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.basicchat.R

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val username = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val gradientColors = listOf(Color(0xFF4C90AF), Color(0xFF4F6906))

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken) {
                    onAuthSuccess()
                }
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error: ${e.message}", e)
        }
    }
    Spacer(modifier = Modifier.height(35.dp))
    Box(modifier = Modifier.fillMaxSize()) {

        Image(painter = painterResource(R.drawable.bgchat),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(), // 1. Tells image to take up all Box space
            contentScale = ContentScale.Crop
        )
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top =33.dp)
        ) {
            OutlinedTextField(value = username.value,
                onValueChange = { username.value = it },
                label = { Text("Username") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            OutlinedTextField(value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Email") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
            OutlinedTextField(value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            ElevatedButton  (onClick = { viewModel.signUp(username.value, email.value, password.value) { onAuthSuccess() } },
                     modifier = Modifier.height(40.dp),
                     colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEEB),
                             contentColor = Color.White),
                        elevation = ButtonDefaults.elevatedButtonElevation(
                            defaultElevation = 6.dp,  // Normal state shadow
                            pressedElevation = 12.dp, // Higher shadow when tapped
                            hoveredElevation = 8.dp
                        )
                ) {
                Text("SIGN-UP")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(onClick = { viewModel.signIn(email.value, password.value) { onAuthSuccess() } }) {
                Text("LOGIN")
            }

            Spacer(modifier = Modifier.height(10.dp))

            val webClientId = stringResource(R.string.default_web_client_id)
            Button(onClick = {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                launcher.launch(googleSignInClient.signInIntent)
            }) { Text("Sign In With Google") }

            if (uiState.isLoading) CircularProgressIndicator()
            uiState.error?.let { Text(it, color = Color.Red) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val isLoading = false
    val error: String? = null

    Box(
        modifier = Modifier
            .fillMaxSize()
          //  .background(brush = Brush.verticalGradient(gradientColors))
    ) {
//        Spacer(modifier = Modifier.height(35.dp))
//        Image(painter = painterResource(R.drawable.bgchat),
//            contentDescription = null,
//           // modifier = Modifier.fillMaxSize(), // 1. Tells image to take up all Box space
//            contentScale = ContentScale.Crop
//        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Chat App", color = Color.White, fontSize = 30.sp)
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(value = email.value,
                onValueChange = { email.value = it },
                label = { Text("Username") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            OutlinedTextField(
                value = password.value,
                onValueChange = { password.value = it },
                label = { Text("Password") },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { /* Preview: do nothing */ },
                modifier = Modifier.width(120.dp)
            ) {
                Text(text = "SIGN-UP")
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* Preview: do nothing */ },
                modifier = Modifier.width(120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text(text = "LOGIN", color = Color.White)
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { /* Preview: do nothing */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RectangleShape,
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier.width(250.dp).height(50.dp)
            ) {
                Text(text = "Sign In With Google", color = Color.Black, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))

        }
    }
}

