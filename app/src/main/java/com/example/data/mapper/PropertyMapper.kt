package com.example.data.mapper

import com.example.data.local.entity.PropertyEntity
import com.example.data.remote.dto.PropertyDto
import com.example.domain.model.ListingType
import com.example.domain.model.Property
import com.example.domain.model.PropertyCategory
import com.example.domain.model.SellingSpeed

object PropertyMapper {

    fun PropertyDto.toDomain(): Property {
        return Property(
            id = id,
            ownerId = ownerId,
            title = title,
            description = description,
            listingType = runCatching { ListingType.valueOf(listingType) }.getOrDefault(ListingType.BUY),
            sellingSpeed = runCatching { SellingSpeed.valueOf(sellingSpeed) }.getOrDefault(SellingSpeed.NORMAL),
            category = runCatching { PropertyCategory.valueOf(category) }.getOrDefault(PropertyCategory.BUY),
            propertyType = propertyType,
            price = price,
            originalPrice = if (originalPrice > 0) originalPrice else price,
            marketEstimate = if (marketEstimate > 0) marketEstimate else price,
            location = location,
            approximateArea = approximateArea,
            distanceKm = distanceKm,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            areaSqFt = areaSqFt,
            urgencyScore = urgencyScore,
            verificationLevel = verificationLevel,
            imageResName = imageResName,
            featuresList = featuresList,
            suitableFor = suitableFor,
            leaseDurationMonths = leaseDurationMonths,
            isDepositRefundable = isDepositRefundable,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            ownerType = ownerType,
            isSaved = false,
            viewsCount = viewsCount,
            savedCount = savedCount,
            messagesCount = messagesCount,
            visitRequestsCount = visitRequestsCount,
            interestedBuyersCount = interestedBuyersCount,
            mapLat = mapLat,
            mapLng = mapLng,
            isPrivate = isPrivate,
            status = status,
            ownerEmail = ownerEmail.ifBlank {
                "${ownerName.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }}@ren.in"
            }
        )
    }

    fun Property.toDto(): PropertyDto {
        return PropertyDto(
            id = id,
            ownerId = ownerId,
            title = title,
            description = description,
            listingType = listingType.name,
            sellingSpeed = sellingSpeed.name,
            category = category.name,
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
            featuresList = featuresList,
            suitableFor = suitableFor,
            leaseDurationMonths = leaseDurationMonths,
            isDepositRefundable = isDepositRefundable,
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            ownerType = ownerType,
            viewsCount = viewsCount,
            savedCount = savedCount,
            messagesCount = messagesCount,
            visitRequestsCount = visitRequestsCount,
            interestedBuyersCount = interestedBuyersCount,
            mapLat = mapLat,
            mapLng = mapLng,
            isPrivate = isPrivate,
            status = status,
            ownerEmail = ownerEmail
        )
    }

    fun entityToDomain(entity: PropertyEntity): Property {
        return Property(
            id = entity.id,
            ownerId = entity.ownerId,
            title = entity.title,
            description = entity.description,
            listingType = runCatching { ListingType.valueOf(entity.listingType) }.getOrDefault(ListingType.BUY),
            sellingSpeed = runCatching { SellingSpeed.valueOf(entity.sellingSpeed) }.getOrDefault(SellingSpeed.NORMAL),
            category = runCatching { PropertyCategory.valueOf(entity.category) }.getOrDefault(PropertyCategory.BUY),
            propertyType = entity.propertyType,
            price = entity.price,
            originalPrice = entity.originalPrice,
            marketEstimate = entity.marketEstimate,
            location = entity.location,
            approximateArea = entity.approximateArea,
            distanceKm = entity.distanceKm,
            bedrooms = entity.bedrooms,
            bathrooms = entity.bathrooms,
            areaSqFt = entity.areaSqFt,
            urgencyScore = entity.urgencyScore,
            verificationLevel = entity.verificationLevel,
            imageResName = entity.imageResName,
            featuresList = entity.featuresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            suitableFor = entity.suitableFor,
            leaseDurationMonths = entity.leaseDurationMonths,
            isDepositRefundable = entity.isDepositRefundable,
            ownerName = entity.ownerName,
            ownerPhone = entity.ownerPhone,
            ownerType = entity.ownerType,
            isSaved = entity.isSaved,
            viewsCount = entity.viewsCount,
            savedCount = entity.savedCount,
            messagesCount = entity.messagesCount,
            visitRequestsCount = entity.visitRequestsCount,
            interestedBuyersCount = entity.interestedBuyersCount,
            mapLat = entity.mapLat,
            mapLng = entity.mapLng,
            isPrivate = entity.isPrivate,
            status = entity.status
        )
    }

    fun PropertyEntity.toDomain(): Property = entityToDomain(this)

    fun domainToEntity(property: Property): PropertyEntity {
        return PropertyEntity(
            id = property.id,
            ownerId = property.ownerId,
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

    fun Property.toEntity(): PropertyEntity = domainToEntity(this)
}
