package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PropertyVisit

typealias PropertyEntity = com.example.data.local.entity.PropertyEntity

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val location: String,
    val buyerName: String,
    val date: String,
    val timeSlot: String,
    val status: String
) {
    fun toDomain(): PropertyVisit = PropertyVisit(
        id = id,
        propertyId = propertyId,
        propertyTitle = propertyTitle,
        location = location,
        buyerName = buyerName,
        date = date,
        timeSlot = timeSlot,
        status = status
    )

    companion object {
        fun fromDomain(visit: PropertyVisit) = VisitEntity(
            id = visit.id,
            propertyId = visit.propertyId,
            propertyTitle = visit.propertyTitle,
            location = visit.location,
            buyerName = visit.buyerName,
            date = visit.date,
            timeSlot = visit.timeSlot,
            status = visit.status
        )
    }
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val senderName: String,
    val message: String,
    val time: String,
    val isFromMe: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toDomain() = com.example.data.model.ChatMessage(
        id = id,
        propertyId = propertyId,
        senderName = senderName,
        message = message,
        time = time,
        isFromMe = isFromMe
    )

    companion object {
        fun fromDomain(msg: com.example.data.model.ChatMessage, timestamp: Long = System.currentTimeMillis()) = ChatMessageEntity(
            id = msg.id,
            propertyId = msg.propertyId,
            senderName = msg.senderName,
            message = msg.message,
            time = msg.time,
            isFromMe = msg.isFromMe,
            timestamp = timestamp
        )
    }
}

@Entity(tableName = "property_reports")
data class PropertyReportEntity(
    @PrimaryKey val id: String,
    val propertyId: String,
    val propertyTitle: String,
    val reason: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_alerts")
data class SearchAlertEntity(
    @PrimaryKey val id: String,
    val location: String,
    val propertyType: String,
    val maxPrice: Long,
    val createdAt: Long = System.currentTimeMillis()
)
