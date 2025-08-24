import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.basicchat.screens.ChatBoxScreen
import com.example.basicchat.screens.SignUpScreen
import com.example.basicchat.screens.UserScreen
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object SignUp : Screen("signup")
    object User : Screen("user")

    object ChatBox : Screen("chatbox/{userId}/{userName}") {
        fun createRoute(userId: String, userName: String) = "chatbox/$userId/$userName"
    }
}

@Composable
fun AppNav(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()

    // ✅ Auto-login effect
    LaunchedEffect(auth.currentUser) {
        if (auth.currentUser != null) {
            navController.navigate(Screen.User.route) {
                popUpTo(0) // clear backstack
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (auth.currentUser == null) Screen.SignUp.route else Screen.User.route
    ) {
        // ---------------- SIGNUP SCREEN ----------------
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.User.route) {
                        popUpTo(0) // clear backstack
                    }
                }
            )
        }

        // ---------------- USER SCREEN ----------------
        composable(Screen.User.route) {
            UserScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId, userName ->
                    navController.navigate(Screen.ChatBox.createRoute(userId, userName))
                },
                onSignOutClick = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // ---------------- CHATBOX SCREEN ----------------
        composable(
            route = Screen.ChatBox.route,
            arguments = listOf(
                navArgument("userId") { defaultValue = "" },
                navArgument("userName") { defaultValue = "User" }
            )
        ) { backStackEntry ->
            val otherUserId = backStackEntry.arguments?.getString("userId") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
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
