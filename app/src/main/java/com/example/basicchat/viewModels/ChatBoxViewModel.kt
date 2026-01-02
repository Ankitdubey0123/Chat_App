package com.example.basicchat.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicchat.models.Message
import com.example.basicchat.models.repository.ChatRepository
import com.example.basicchat.models.repository.CloudinaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository(),
    private val cloudinaryRepository: CloudinaryRepository = CloudinaryRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // ---------------- LOAD MESSAGES ----------------
    fun loadMessages(currentUserId: String, otherUserId: String) {
        chatRepository.loadMessages(currentUserId, otherUserId) {
            _messages.value = it
        }
    }

    // ---------------- SEND TEXT ----------------
    fun sendTextMessage(fromId: String, toId: String, text: String) {
        viewModelScope.launch {
            try {
                val message = Message(
                    fromId = fromId,
                    toId = toId,
                    text = text,
                    type = "text",
                    timestamp = System.currentTimeMillis()
                )
                chatRepository.sendMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---------------- SEND IMAGE ----------------
    fun sendImageMessage(
        context: Context,
        fromId: String,
        toId: String,
        imageUri: Uri
    ) {
        viewModelScope.launch {
            try {
                val imageUrl = cloudinaryRepository.uploadFile(
                    context,
                    imageUri,
                    folder = "chat_images",
                    uploadPreset = "chat_upload"
                )

                val message = Message(
                    fromId = fromId,
                    toId = toId,
                    text = "",
                    fileUrl = imageUrl,
                    type = "image",
                    timestamp = System.currentTimeMillis()
                )

                chatRepository.sendMessage(message)

            } catch (e: Exception) {
                e.printStackTrace()   // 🔥 prevents app crash
            }
        }
    }

    // ---------------- SEND DOCUMENT ----------------
    fun sendDocumentMessage(
        context: Context,
        fromId: String,
        toId: String,
        fileUri: Uri,
        fileName: String
    ) {
        viewModelScope.launch {
            try {
                val fileUrl = cloudinaryRepository.uploadFile(
                    context,
                    fileUri,
                    folder = "chat_documents",
                    uploadPreset = "chat_upload"
                )

                val message = Message(
                    fromId = fromId,
                    toId = toId,
                    text = "",
                    fileUrl = fileUrl,
                    fileName = fileName,
                    type = "document",
                    timestamp = System.currentTimeMillis()
                )

                chatRepository.sendMessage(message)

            } catch (e: Exception) {
                e.printStackTrace()   // 🔥 prevents app crash
            }
        }
    }
}
