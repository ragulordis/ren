package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.PropertyVisit
import com.example.data.model.SellingSpeed

@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["location"]),
        Index(value = ["category"]),
        Index(value = ["listingType"]),
        Index(value = ["price"])
    ]
)
data class PropertyEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val listingType: String,
    val sellingSpeed: String,
    val category: String,
    val propertyType: String,
    val price: Long,
    val originalPrice: Long,
    val marketEstimate: Long,
    val location: String,
    val approximateArea: String,
    val distanceKm: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqFt: Int,
    val urgencyScore: Int,
    val verificationLevel: Int,
    val imageResName: String,
    val featuresCsv: String,
    val suitableFor: String,
    val leaseDurationMonths: Int?,
    val isDepositRefundable: Boolean,
    val ownerName: String,
    val ownerPhone: String,
    val ownerType: String,
    val isSaved: Boolean,
    val viewsCount: Int,
    val savedCount: Int,
    val messagesCount: Int,
    val visitRequestsCount: Int,
    val interestedBuyersCount: Int,
    val mapLat: Double,
    val mapLng: Double,
    val isPrivate: Boolean,
    val status: String
) {
    fun toDomain(): Property {
        return Property(
            id = id,
            title = title,
            description = description,
            listingType = runCatching { ListingType.valueOf(listingType) }.getOrDefault(ListingType.BUY),
            sellingSpeed = runCatching { SellingSpeed.valueOf(sellingSpeed) }.getOrDefault(SellingSpeed.NORMAL),
            category = runCatching { PropertyCategory.valueOf(category) }.getOrDefault(PropertyCategory.BUY),
            propertyType = propertyType,
            price = price,
            originalPrice = originalPrice,
            marketEstimate = marketEstimate,
            location = location,
            approximateArea = approximateArea,
            distanceKm = distanceKm,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            areaSqFt = areaSqFt,
            urgencyScore = urgencyScore,
            verificationLevel = verificationLevel,
            imageResName = imageResName,
            featuresList = featuresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            suitableFor = suitableFor,
            leaseDurationMonths = leaseDurationMonths,
            isDepositRefundable = isDepositRefundable,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            ownerType = ownerType,
            isSaved = isSaved,
            viewsCount = viewsCount,
            savedCount = savedCount,
            messagesCount = messagesCount,
            visitRequestsCount = visitRequestsCount,
            interestedBuyersCount = interestedBuyersCount,
            mapLat = mapLat,
            mapLng = mapLng,
            isPrivate = isPrivate,
            status = status
        )
    }

    companion object {
        fun fromDomain(property: Property): PropertyEntity {
            return PropertyEntity(
                id = property.id,
                title = property.title,
                description = property.description,
                listingType = property.listingType.name,
                sellingSpeed = property.sellingSpeed.name,
                category = property.category.name,
                propertyType = property.propertyType,
                price = property.price,
                originalPrice = property.originalPrice,
                marketEstimate = property.marketEstimate,
                location = property.location,
                approximateArea = property.approximateArea,
                distanceKm = property.distanceKm,
                bedrooms = property.bedrooms,
                bathrooms = property.bathrooms,
                areaSqFt = property.areaSqFt,
                urgencyScore = property.urgencyScore,
                verificationLevel = property.verificationLevel,
                imageResName = property.imageResName,
                featuresCsv = property.featuresList.joinToString(","),
                suitableFor = property.suitableFor,
                leaseDurationMonths = property.leaseDurationMonths,
                isDepositRefundable = property.isDepositRefundable,
                ownerName = property.ownerName,
                ownerPhone = property.ownerPhone,
                ownerType = property.ownerType,
                isSaved = property.isSaved,
                viewsCount = property.viewsCount,
                savedCount = property.savedCount,
                messagesCount = property.messagesCount,
                visitRequestsCount = property.visitRequestsCount,
                interestedBuyersCount = property.interestedBuyersCount,
                mapLat = property.mapLat,
                mapLng = property.mapLng,
                isPrivate = property.isPrivate,
                status = property.status
            )
        }
    }
}

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
