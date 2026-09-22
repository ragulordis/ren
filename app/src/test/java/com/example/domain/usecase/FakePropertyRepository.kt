package com.example.domain.usecase

import com.example.data.model.ChatMessage
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.model.SmartMatchEngine
import com.example.data.model.SmartMatchResult
import com.example.data.model.UserPreferences
import com.example.data.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

open class FakePropertyRepository : PropertyRepository {
    val addedProperties = mutableListOf<Property>()
    var properties: List<Property> = emptyList()
    val savedPropertyIds = mutableSetOf<String>()
    val visits = mutableListOf<PropertyVisit>()
    val chatMessages = mutableListOf<ChatMessage>()

    override val allProperties: Flow<List<Property>>
        get() = flowOf(properties + addedProperties)

    override val savedProperties: Flow<List<Property>>
        get() = flowOf((properties + addedProperties).filter { savedPropertyIds.contains(it.id) || it.isSaved })

    override val allVisits: Flow<List<PropertyVisit>>
        get() = flowOf(visits)

    override suspend fun ensureInitialized(seedLocalDevIfEmpty: Boolean) {}

    override suspend fun refreshFromFirestore(): Result<Int> = Result.success(0)

    override suspend fun syncWithFirestore(remoteList: List<Property>) {}

    override suspend fun addProperty(property: Property) {
        addedProperties.add(property)
    }

    override suspend fun updateProperty(property: Property) {
        val index = addedProperties.indexOfFirst { it.id == property.id }
        if (index != -1) {
            addedProperties[index] = property
        }
    }

    override suspend fun deleteProperty(propertyId: String) {
        addedProperties.removeAll { it.id == propertyId }
    }

    override suspend fun updatePropertyStatus(propertyId: String, status: String) {
        val index = addedProperties.indexOfFirst { it.id == propertyId }
        if (index != -1) {
            addedProperties[index] = addedProperties[index].copy(status = status)
        }
    }

    override suspend fun toggleSave(propertyId: String, currentSaved: Boolean) {
        if (currentSaved) {
            savedPropertyIds.remove(propertyId)
        } else {
            savedPropertyIds.add(propertyId)
        }
    }

    override suspend fun scheduleVisit(visit: PropertyVisit) {
        visits.add(visit)
    }

    override suspend fun updateVisitStatus(visitId: String, status: String) {
        val index = visits.indexOfFirst { it.id == visitId }
        if (index != -1) {
            visits[index] = visits[index].copy(status = status)
        }
    }

    override suspend fun deleteVisit(visitId: String) {
        visits.removeAll { it.id == visitId }
    }

    override fun getChatMessagesForProperty(propertyId: String): Flow<List<ChatMessage>> {
        return flowOf(chatMessages.filter { it.propertyId == propertyId })
    }

    override fun streamChatMessages(propertyId: String, buyerId: String): Flow<List<ChatMessage>> {
        return flowOf(chatMessages.filter { it.propertyId == propertyId })
    }

    override suspend fun insertChatMessage(message: ChatMessage, buyerId: String) {
        chatMessages.add(message)
    }

    override suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String) {}

    override suspend fun blockUser(userId: String) {}

    override fun getPropertiesByCategory(category: String): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { it.category.name.equals(category, ignoreCase = true) })
    }

    override fun getPropertiesByListingType(listingType: String): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { it.listingType.name.equals(listingType, ignoreCase = true) })
    }

    override fun getPropertiesByLocation(location: String): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { it.location.contains(location, ignoreCase = true) })
    }

    override fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { it.price in minPrice..maxPrice })
    }

    override fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long,
        maxPrice: Long
    ): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { prop ->
            (location == null || prop.location.contains(location, ignoreCase = true)) &&
            (category == null || prop.category.name.equals(category, ignoreCase = true)) &&
            prop.price in minPrice..maxPrice
        })
    }

    override fun searchProperties(query: String): Flow<List<Property>> {
        return flowOf((properties + addedProperties).filter { it.title.contains(query, ignoreCase = true) })
    }

    override suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long) {}

    override suspend fun getSmartMatchSuggestions(preferences: UserPreferences): List<SmartMatchResult> {
        return (properties + addedProperties).map { prop ->
            SmartMatchResult(
                property = prop,
                matchScore = SmartMatchEngine.calculateScore(prop, preferences)
            )
        }.sortedByDescending { it.matchScore.overallPercentage }
    }
}
