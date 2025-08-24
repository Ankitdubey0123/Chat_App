package com.example.basicchat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basicchat.viewmodels.UsersViewModel
import com.example.basicchat.models.User
import com.google.firebase.auth.FirebaseAuth

// ---------------- TOP BAR ----------------
@Composable
fun TopBar(
    title: String,
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFF19B6D2))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 22.sp,
            color = Color.White
        )
        IconButton(onClick = onSignOutClick) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.White
            )
        }
    }
}

// ---------------- USER CARD ----------------
@Composable
fun ChatCard(userName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = userName,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ---------------- MAIN SCREEN ----------------
@Composable
fun UserScreen(
    onBackClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onUserClick: (String, String) -> Unit,
    usersViewModel: UsersViewModel = viewModel()
) {
    val users by usersViewModel.users.collectAsState()

    // Build a friendly title from Firebase user
    val me = FirebaseAuth.getInstance().currentUser
    val title = when {
        !me?.displayName.isNullOrBlank() -> me!!.displayName!!
        !me?.email.isNullOrBlank() -> me!!.email!!.substringBefore("@")
        else -> "You"
    }

    Scaffold(
        topBar = {
            TopBar(
                title = title,
                onBackClick = onBackClick,
                onSignOutClick = {
                    usersViewModel.signOut()
                    onSignOutClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                users.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No users available", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(users) { user: User ->
                            ChatCard(
                                userName = user.name,
                                onClick = { onUserClick(user.uid, user.name) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- PREVIEW ----------------
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserScreenPreview() {
    val dummyUsers = listOf(
        User(uid = "1", name = "User1", email = "u1@test.com"),
        User(uid = "2", name = "User2", email = "u2@test.com"),
        User(uid = "3", name = "User3", email = "u3@test.com"),
    )

    Column {
        TopBar(title = "Preview User", onBackClick = {}, onSignOutClick = {})
        LazyColumn {
            items(dummyUsers) { user ->
                ChatCard(userName = user.name, onClick = {})
            }
        }
    }
}
