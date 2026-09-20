package com.example.data.remote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Registers this installation for recipient-only FCM notifications. */
object DeviceRegistration {
    suspend fun register(uid: String) {
        if (uid.isBlank()) return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .collection("devices").document(token)
                .set(mapOf("fcmToken" to token, "updatedAt" to System.currentTimeMillis()))
                .await()
        }.onFailure { Log.w("DeviceRegistration", "Unable to register FCM token", it) }
    }
}
