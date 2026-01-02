package com.example.basicchat.utils

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

fun getFileName(context: Context, uri: Uri): String {
    var name = "document"

    try {
        val cursor: Cursor? =
            context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    val value = it.getString(index)
                    if (!value.isNullOrBlank()) {
                        name = value
                    }
                }
            }
        }
    } catch (e: Exception) {
        // SAFETY: never crash app because of file name
        e.printStackTrace()
    }

    return name
}
