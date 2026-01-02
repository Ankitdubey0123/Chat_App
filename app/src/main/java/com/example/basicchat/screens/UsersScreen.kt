package com.example.basicchat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.basicchat.models.ConnectionStatus
import com.example.basicchat.viewmodels.ChatRequestViewModel
import com.example.basicchat.viewmodels.UsersViewModel
import com.google.firebase.auth.FirebaseAuth

// ---------------- TOP BAR ----------------
@Composable
fun TopBar(title: String, onBackClick: () -> Unit, onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(Color(0xFF1E88E5))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        IconButton(onClick = onMenuClick) {
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
fun ChatCard(
    userName: String,
    profileUrl: String?,
    status: ConnectionStatus,
    onChatClick: () -> Unit,
    onSendRequest: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // ✅ SINGLE CIRCLE CONTAINER (IMPORTANT FIX)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF90CAF9)),
                contentAlignment = Alignment.Center
            ) {
                if (!profileUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = profileUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                val statusText = when (status) {
                    ConnectionStatus.NONE -> ""
                    ConnectionStatus.SENT -> "Request sent"
                    ConnectionStatus.RECEIVED -> "Sent you a request"
                    ConnectionStatus.ACCEPTED -> "Connected"
                    ConnectionStatus.REJECTED -> "Request rejected"
                    else -> ""
                }

                if (statusText.isNotEmpty()) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        color = if (status == ConnectionStatus.REJECTED) Color.Red else Color.Gray
                    )
                }
            }

            when (status) {
                ConnectionStatus.NONE -> Button(
                    onClick = onSendRequest,
                    shape = RoundedCornerShape(20.dp)
                ) { Text("Send") }

                ConnectionStatus.SENT -> OutlinedButton(
                    onClick = {},
                    enabled = false
                ) { Text("Pending") }

                ConnectionStatus.RECEIVED -> Row {
                    TextButton(onClick = onAccept) { Text("Accept") }
                    TextButton(onClick = onReject) {
                        Text("Reject", color = Color.Red)
                    }
                }

                ConnectionStatus.ACCEPTED -> Button(
                    onClick = onChatClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Chat", color = Color.White) }

                else -> {}
            }
        }
    }
}

// ---------------- MAIN SCREEN ----------------
@Composable
fun UserScreen(
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUserClick: (String, String) -> Unit,
    usersViewModel: UsersViewModel = viewModel(),
    requestViewModel: ChatRequestViewModel = viewModel()
) {
    val users by usersViewModel.users.collectAsState()
    val incomingRequests by requestViewModel.incomingRequests.collectAsState()
    val outgoingRequests by requestViewModel.outgoingRequests.collectAsState()

    val me = FirebaseAuth.getInstance().currentUser
    val currentUserId = me?.uid ?: ""

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            requestViewModel.listenIncomingRequests(currentUserId)
            requestViewModel.listenOutgoingRequests(currentUserId)
        }
    }

    val usersWithStatus = users.map { userUi ->
        val incoming = incomingRequests.find { it.fromId == userUi.user.uid }
        val outgoing = outgoingRequests.find { it.toId == userUi.user.uid }

        val status = when {
            incoming?.status == "pending" -> ConnectionStatus.RECEIVED
            outgoing?.status == "pending" -> ConnectionStatus.SENT
            incoming?.status == "accepted" || outgoing?.status == "accepted" -> ConnectionStatus.ACCEPTED
            incoming?.status == "rejected" || outgoing?.status == "rejected" -> ConnectionStatus.REJECTED
            else -> ConnectionStatus.NONE
        }

        userUi.copy(status = status)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = me?.displayName ?: me?.email?.substringBefore("@") ?: "You",
                onBackClick = onBackClick,
                onMenuClick = onProfileClick
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(usersWithStatus) { userUi ->
                ChatCard(
                    userName = userUi.user.name,
                    profileUrl = userUi.user.profileImageUrl,
                    status = userUi.status,
                    onChatClick = { onUserClick(userUi.user.uid, userUi.user.name) },
                    onSendRequest = { usersViewModel.sendRequest(userUi.user.uid) },
                    onAccept = { usersViewModel.acceptRequest(userUi.user.uid) },
                    onReject = { usersViewModel.rejectRequest(userUi.user.uid) }
                )
            }
        }
    }
}
