package com.example.basicchat.models.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryRepository {

    /**
     * Upload image/file to Cloudinary (UNSIGNED upload)
     */
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        folder: String,
        uploadPreset: String
    ): String = withContext(Dispatchers.IO) {

        suspendCancellableCoroutine { cont ->

            MediaManager.get()
                .upload(uri)
                .unsigned(uploadPreset)   // ✅ unsigned preset
                .option("folder", folder)
                .callback(object : com.cloudinary.android.callback.UploadCallback {

                    override fun onStart(requestId: String?) {}

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(
                        requestId: String?,
                        resultData: Map<*, *>
                    ) {
                        val url = resultData["secure_url"] as? String
                        if (url != null) {
                            cont.resume(url)
                        } else {
                            cont.resumeWithException(
                                IllegalStateException("secure_url missing")
                            )
                        }
                    }

                    override fun onError(
                        requestId: String?,
                        error: com.cloudinary.android.callback.ErrorInfo?
                    ) {
                        cont.resumeWithException(
                            RuntimeException(error?.description ?: "Upload failed")
                        )
                    }

                    override fun onReschedule(requestId: String?, error: com.cloudinary.android.callback.ErrorInfo?) {}
                })
                .dispatch()
        }
    }
}
