package com.example.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.NotificationDao
import com.example.data.mapper.NotificationMapper
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface NotificationRepository {
    val allNotifications: Flow<List<AppNotification>>
    val unreadCount: Flow<Int>
    fun getRecentNotifications(limit: Int = 5): Flow<List<AppNotification>>

    suspend fun sendNotification(
        title: String,
        message: String,
        type: NotificationType,
        propertyId: String? = null,
        targetLocation: String? = null,
        actionText: String? = null
    )

    suspend fun markAsRead(id: String)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(id: String)
    suspend fun clearAll()
    suspend fun initializeNotificationsIfEmpty()
}

class NotificationRepositoryImpl(
    private val notificationDao: NotificationDao,
    private val context: Context
) : NotificationRepository {

    companion object {
        const val CHANNEL_ID = "quicknest_alerts"
        const val CHANNEL_NAME = "QuickNest Alerts"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Real-time alerts for property price drops, visits, and chat inquiries"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    override val allNotifications: Flow<List<AppNotification>> =
        notificationDao.getAllNotifications().map { entities ->
            entities.map { NotificationMapper.entityToDomain(it) }
        }

    override val unreadCount: Flow<Int> =
        notificationDao.getUnreadCount()

    override fun getRecentNotifications(limit: Int): Flow<List<AppNotification>> =
        notificationDao.getRecentNotifications(limit).map { entities ->
            entities.map { NotificationMapper.entityToDomain(it) }
        }

    override suspend fun sendNotification(
        title: String,
        message: String,
        type: NotificationType,
        propertyId: String?,
        targetLocation: String?,
        actionText: String?
    ) {
        val notification = AppNotification(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            propertyId = propertyId,
            targetLocation = targetLocation,
            actionText = actionText
        )

        notificationDao.insertNotification(NotificationMapper.domainToEntity(notification))
        postSystemNotification(notification)
    }

    private fun postSystemNotification(notification: AppNotification) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                if (notification.propertyId != null) {
                    putExtra("NOTIFICATION_PROPERTY_ID", notification.propertyId)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notification.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            val manager = NotificationManagerCompat.from(context)
            if (manager.areNotificationsEnabled()) {
                manager.notify(notification.id.hashCode(), builder.build())
            }
        } catch (_: SecurityException) {
            // Permission not granted or restricted in environment; in-app notification remains stored and active
        } catch (_: Throwable) {
            // Safe fallback
        }
    }

    override suspend fun markAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationDao.markAllAsRead()
    }

    override suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotification(id)
    }

    override suspend fun clearAll() {
        notificationDao.clearAllNotifications()
    }

    override suspend fun initializeNotificationsIfEmpty() {
        if (notificationDao.getNotificationCount() == 0) {
            val welcomeNotifications = listOf(
                AppNotification(
                    id = "notif-welcome-1",
                    title = "Welcome to Ren alerts",
                    message = "You will receive instant real-time alerts for verified listings, price drops, and owner chat messages.",
                    type = NotificationType.SYSTEM_UPDATE,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                    isRead = false,
                    actionText = "Explore"
                ),
                AppNotification(
                    id = "notif-welcome-2",
                    title = "Real-Time Sync Activated ⚡",
                    message = "Connected to Google Cloud Firestore. Local offline cache and real-time listeners are active.",
                    type = NotificationType.SYSTEM_UPDATE,
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                    isRead = true,
                    actionText = "Status"
                )
            )
            notificationDao.insertNotifications(welcomeNotifications.map { NotificationMapper.domainToEntity(it) })
        }
    }
}
