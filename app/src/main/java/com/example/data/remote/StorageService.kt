package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Service managing property photo uploads to Firebase Cloud Storage.
 * Stores files under `property_images/{propertyId}/{uuid}.jpg`.
 * Returns public HTTPS download URL on success, or fails gracefully if offline.
 */
class StorageService(
    customStorage: FirebaseStorage? = null
) {
    private val storage: FirebaseStorage? by lazy {
        customStorage ?: runCatching { FirebaseStorage.getInstance() }
            .onFailure { Log.w("StorageService", "FirebaseStorage not initialized: ${it.message}") }
            .getOrNull()
    }

    suspend fun uploadPropertyPhoto(
        propertyId: String,
        imageUri: Uri,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val fbStorage = storage ?: throw IllegalStateException("Firebase Storage is not available")

            val fileUuid = UUID.randomUUID().toString()
            val storageRef = fbStorage.reference.child("property_images/$propertyId/$fileUuid.jpg")

            val ownerId = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: ""
            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .setCustomMetadata("propertyId", propertyId)
                .setCustomMetadata("ownerId", ownerId)
                .build()

            // Open stream from ContentResolver to handle content:// uris reliably
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IllegalArgumentException("Cannot open stream for URI: $imageUri")

            inputStream.use { stream ->
                storageRef.putStream(stream, metadata).await()
            }

            val downloadUrl = storageRef.downloadUrl.await().toString()
            Log.d("StorageService", "Successfully uploaded property photo: $downloadUrl")
            downloadUrl
        }
    }

    suspend fun uploadPropertyPhotos(
        propertyId: String,
        imageUris: List<Uri>,
        context: Context
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            imageUris.distinct().map { imageUri ->
                uploadPropertyPhoto(propertyId, imageUri, context).getOrThrow()
            }
        }
    }
}
