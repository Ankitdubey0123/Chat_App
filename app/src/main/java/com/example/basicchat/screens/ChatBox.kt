package com.example.basicchat.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.basicchat.utils.getFileName
import com.example.basicchat.viewmodels.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBoxScreen(
    userName: String,
    currentUserId: String,
    otherUserId: String,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val chatViewModel: ChatViewModel = viewModel()
    val messages by chatViewModel.messages.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showAttachMenu by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            selectedUri = it
            selectedType = "image"
            selectedFileName = null
        }
    }

    val docPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let {
            selectedUri = it
            selectedType = "document"
            selectedFileName = getFileName(context, it)
        }
    }

    LaunchedEffect(otherUserId) {
        chatViewModel.loadMessages(currentUserId, otherUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                // Selected preview
                if (selectedUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedType == "image") {
                                AsyncImage(
                                    model = selectedUri,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp)
                                )
                            } else {
                                Text(selectedFileName ?: "Document")
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = {
                                selectedUri = null
                                selectedType = null
                                selectedFileName = null
                            }) {
                                Text("❌")
                            }
                        }
                    }
                }

                // Message input row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { showAttachMenu = true }) {
                            Icon(Icons.Default.AddCircle, contentDescription = "Attach")
                        }

                        DropdownMenu(
                            expanded = showAttachMenu,
                            onDismissRequest = { showAttachMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Image") },
                                onClick = {
                                    showAttachMenu = false
                                    imagePicker.launch("image/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Document") },
                                onClick = {
                                    showAttachMenu = false
                                    docPicker.launch("*/*")
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message") }
                    )

                    IconButton(onClick = {
                        when {
                            selectedUri != null && selectedType == "image" -> {
                                chatViewModel.sendImageMessage(
                                    context,
                                    currentUserId,
                                    otherUserId,
                                    selectedUri!!
                                )
                            }
                            selectedUri != null && selectedType == "document" -> {
                                chatViewModel.sendDocumentMessage(
                                    context,
                                    currentUserId,
                                    otherUserId,
                                    selectedUri!!,
                                    selectedFileName ?: "Document"
                                )
                            }
                            messageText.isNotBlank() -> {
                                chatViewModel.sendTextMessage(
                                    currentUserId,
                                    otherUserId,
                                    messageText
                                )
                            }
                        }
                        messageText = ""
                        selectedUri = null
                        selectedType = null
                        selectedFileName = null
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = true,
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(messages.reversed()) { msg ->
                val isMe = msg.fromId == currentUserId

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .padding(6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isMe) Color(0xFFDCF8C6) else Color.White)
                            .padding(8.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        when (msg.type) {
                            "text" -> Text(msg.text)

                            "image" -> AsyncImage(
                                model = msg.fileUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        // Open full image in default viewer
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(msg.fileUrl)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                            )

                            "document" -> Text(
                                text = msg.fileName ?: "Document",
                                color = Color.Blue,
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(msg.fileUrl), "*/*")
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
