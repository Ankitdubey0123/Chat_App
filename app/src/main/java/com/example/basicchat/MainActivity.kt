package com.example.basicchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.cloudinary.android.MediaManager
import com.example.basicchat.navigation.AppNav
import com.example.basicchat.ui.theme.BasicChatTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCloudinary()

        enableEdgeToEdge()

        setContent {
            BasicChatTheme {
                val navController = rememberNavController()
                AppNav(navController)
            }
        }
    }

    private fun initCloudinary() {
        try {
            MediaManager.init(
                this,
                mapOf(
                    "cloud_name" to "dhalkze92"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
