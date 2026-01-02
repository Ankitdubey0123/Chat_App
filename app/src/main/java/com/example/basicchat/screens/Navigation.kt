package com.example.basicchat.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.basicchat.screens.ChatBoxScreen
import com.example.basicchat.screens.ProfileScreen
import com.example.basicchat.screens.SignUpScreen
import com.example.basicchat.screens.UserScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object SignUp : Screen("signup")
    object User : Screen("user")
    object Profile : Screen("profile")
    object ChatBox : Screen("chatbox/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String): String {
            return "chatbox/$userId/${Uri.encode(userName)}"
        }
    }
}

@Composable
fun AppNav(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()

    NavHost(
        navController = navController,
        startDestination = if (auth.currentUser == null) Screen.SignUp.route else Screen.User.route
    ) {

        // ---------------- SIGNUP ----------------
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.User.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ---------------- USER LIST ----------------
        composable(Screen.User.route) {
            UserScreen(
                onBackClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onUserClick = { userId, userName ->
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        navController.navigate(Screen.ChatBox.createRoute(userId, userName))
                    }
                }
            )
        }

        // ---------------- PROFILE SCREEN ----------------
        composable(Screen.Profile.route) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            ProfileScreen(
                userName = currentUser?.displayName ?: "You",
                profileImageUrl = "", // fetch from Firestore if available
                //context = LocalContext.current,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.User.route) { inclusive = true }
                    }
                }
            )
        }

        // ---------------- CHAT BOX ----------------
        composable(
            route = Screen.ChatBox.route,
            arguments = listOf(
                navArgument("userId") { defaultValue = "" },
                navArgument("userName") { defaultValue = "User" }
            )
        ) { backStackEntry ->

            val otherUserId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = Uri.decode(backStackEntry.arguments?.getString("userName") ?: "User")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            ChatBoxScreen(
                userName = userName,
                currentUserId = currentUserId,
                otherUserId = otherUserId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
