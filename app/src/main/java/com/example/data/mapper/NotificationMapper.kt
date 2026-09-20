package com.example.data.mapper

import com.example.data.local.entity.NotificationEntity
import com.example.data.model.AppNotification
import com.example.data.model.NotificationType

object NotificationMapper {

    fun entityToDomain(entity: NotificationEntity): AppNotification {
        return AppNotification(
            id = entity.id,
            title = entity.title,
            message = entity.message,
            type = NotificationType.fromString(entity.type),
            timestamp = entity.timestamp,
            isRead = entity.isRead,
            propertyId = entity.propertyId,
            targetLocation = entity.targetLocation,
            actionText = entity.actionText
        )
    }

    fun domainToEntity(domain: AppNotification): NotificationEntity {
        return NotificationEntity(
            id = domain.id,
            title = domain.title,
            message = domain.message,
            type = domain.type.name,
            timestamp = domain.timestamp,
            isRead = domain.isRead,
            propertyId = domain.propertyId,
            targetLocation = domain.targetLocation,
            actionText = domain.actionText
        )
    }
}
