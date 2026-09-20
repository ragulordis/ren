package com.example.data.repository

import com.example.data.local.PropertyDao
import com.example.data.local.PropertyEntity
import com.example.data.local.VisitEntity
import com.example.data.mapper.PropertyMapper
import com.example.data.model.ChatMessage
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.model.SmartMatchEngine
import com.example.data.model.SmartMatchResult
import com.example.data.model.UserPreferences
import com.example.data.remote.FirestoreService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Domain-facing repository interface defining contract for Property, Visit, and Chat operations.
 */
interface PropertyRepository {
    val allProperties: Flow<List<Property>>
    val savedProperties: Flow<List<Property>>
    val allVisits: Flow<List<PropertyVisit>>

    suspend fun ensureInitialized(seedLocalDevIfEmpty: Boolean = false)
    suspend fun syncWithFirestore(remoteProperties: List<Property>)
    suspend fun refreshFromFirestore(): Result<Int>
    suspend fun toggleSave(propertyId: String, currentSaved: Boolean)
    suspend fun addProperty(property: Property)
    suspend fun scheduleVisit(visit: PropertyVisit)
    suspend fun updateVisitStatus(visitId: String, status: String)
    suspend fun deleteVisit(visitId: String)
    fun getChatMessagesForProperty(propertyId: String): Flow<List<ChatMessage>>
    /** Stream real-time chat from the buyer-scoped Firestore conversation ({propertyId}_{buyerId}). */
    fun streamChatMessages(propertyId: String, buyerId: String): Flow<List<ChatMessage>>
    suspend fun insertChatMessage(message: ChatMessage, buyerId: String = "")
    suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String)
    suspend fun updatePropertyStatus(propertyId: String, status: String)
    suspend fun deleteProperty(propertyId: String)
    suspend fun updateProperty(property: Property)
    fun getPropertiesByCategory(category: String): Flow<List<Property>>
    fun getPropertiesByListingType(listingType: String): Flow<List<Property>>
    fun getPropertiesByLocation(location: String): Flow<List<Property>>
    fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>>
    fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long = 0L,
        maxPrice: Long = Long.MAX_VALUE
    ): Flow<List<Property>>
    fun searchProperties(query: String): Flow<List<Property>>
    suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long)
    suspend fun getSmartMatchSuggestions(preferences: UserPreferences): List<SmartMatchResult>
}

/**
 * Default implementation of PropertyRepository coordinating Room local cache and Firestore remote store.
 */
class PropertyRepositoryImpl(
    private val dao: PropertyDao,
    private val firestoreService: FirestoreService = FirestoreService()
) : PropertyRepository {

    override val allProperties: Flow<List<Property>> = dao.getAllProperties().map { entities ->
        entities.map { PropertyMapper.entityToDomain(it) }
    }

    override val savedProperties: Flow<List<Property>> = dao.getSavedProperties().map { entities ->
        entities.map { PropertyMapper.entityToDomain(it) }
    }

    override val allVisits: Flow<List<PropertyVisit>> = dao.getAllVisits().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun ensureInitialized(seedLocalDevIfEmpty: Boolean) {
        // Remove any residual mock/sample properties from local database
        dao.deleteMockProperties()

        // Fetch real properties from Firestore if local cache is empty
        val count = dao.getCount()
        if (count == 0) {
            runCatching {
                val remoteProperties = firestoreService.fetchPropertiesOnce()
                if (remoteProperties.isNotEmpty()) {
                    dao.insertProperties(remoteProperties.map { PropertyMapper.domainToEntity(it) })
                }
            }
        }
    }

    override suspend fun syncWithFirestore(remoteProperties: List<Property>) {
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyMapper.domainToEntity(it) })
        }
    }

    override suspend fun refreshFromFirestore(): Result<Int> = runCatching {
        val remoteProperties = firestoreService.fetchPropertiesOnce()
        if (remoteProperties.isNotEmpty()) {
            dao.insertProperties(remoteProperties.map { PropertyMapper.domainToEntity(it) })
            remoteProperties.size
        } else {
            // Keep existing local seed/cached data intact
            dao.getCount()
        }
    }

    override suspend fun toggleSave(propertyId: String, currentSaved: Boolean) {
        dao.updateSavedStatus(propertyId, !currentSaved)
    }

    override suspend fun addProperty(property: Property) {
        dao.insertProperty(PropertyMapper.domainToEntity(property))
        firestoreService.saveProperty(property)
    }

    override suspend fun scheduleVisit(visit: PropertyVisit) {
        dao.insertVisit(VisitEntity.fromDomain(visit))
        firestoreService.saveVisit(visit)
    }

    override suspend fun updateVisitStatus(visitId: String, status: String) {
        val targetStatus = com.example.domain.model.VisitStatus.fromString(status)
        val currentVisit = dao.getVisitByIdSync(visitId)?.toDomain()
        if (currentVisit != null) {
            val currentStatus = com.example.domain.model.VisitStatus.fromString(currentVisit.status)
            require(com.example.domain.model.VisitStatus.isValidTransition(currentStatus, targetStatus)) {
                "Illegal visit status transition from ${currentStatus.name} to ${targetStatus.name}"
            }
        }
        dao.updateVisitStatus(visitId, targetStatus.value)
        firestoreService.updateVisitStatus(visitId, targetStatus.value)
    }

    override suspend fun deleteVisit(visitId: String) {
        dao.deleteVisit(visitId)
    }

    override fun getChatMessagesForProperty(propertyId: String): Flow<List<ChatMessage>> {
        return dao.getMessagesForProperty(propertyId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun streamChatMessages(propertyId: String, buyerId: String): Flow<List<ChatMessage>> {
        return firestoreService.streamChatMessages(propertyId, buyerId)
    }

    override suspend fun insertChatMessage(message: ChatMessage, buyerId: String) {
        dao.insertMessage(com.example.data.local.ChatMessageEntity.fromDomain(message))
        // Pass explicit buyerId; fall back to message.senderId when not provided
        val effectiveBuyerId = buyerId.ifBlank { message.senderId }
        firestoreService.saveChatMessage(message.propertyId, message, effectiveBuyerId)
    }

    override suspend fun reportProperty(propertyId: String, propertyTitle: String, reason: String, details: String) {
        val reportId = "rep-${System.currentTimeMillis()}"
        val report = com.example.data.local.PropertyReportEntity(
            id = reportId,
            propertyId = propertyId,
            propertyTitle = propertyTitle,
            reason = reason,
            details = details
        )
        dao.insertReport(report)
        firestoreService.saveReport(propertyId, propertyTitle, reason, details, reportId)
    }

    override suspend fun updatePropertyStatus(propertyId: String, status: String) {
        val targetStatus = com.example.domain.model.ListingStatus.fromString(status)
        val currentProperty = dao.getPropertyByIdSync(propertyId)?.let { PropertyMapper.entityToDomain(it) }
        if (currentProperty != null) {
            val currentStatus = com.example.domain.model.ListingStatus.fromString(currentProperty.status)
            require(com.example.domain.model.ListingStatus.isValidTransition(currentStatus, targetStatus)) {
                "Illegal listing status transition from ${currentStatus.name} to ${targetStatus.name}"
            }
        }
        dao.updatePropertyStatus(propertyId, targetStatus.value)
        firestoreService.updatePropertyStatus(propertyId, targetStatus.value)
    }

    override suspend fun deleteProperty(propertyId: String) {
        dao.deleteProperty(propertyId)
        firestoreService.deleteProperty(propertyId)
    }

    override suspend fun updateProperty(property: Property) {
        dao.updateProperty(PropertyMapper.domainToEntity(property))
        firestoreService.saveProperty(property)
    }

    override fun getPropertiesByCategory(category: String): Flow<List<Property>> =
        dao.getPropertiesByCategory(category).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override fun getPropertiesByListingType(listingType: String): Flow<List<Property>> =
        dao.getPropertiesByListingType(listingType).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override fun getPropertiesByLocation(location: String): Flow<List<Property>> =
        dao.getPropertiesByLocation(location).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override fun getPropertiesByPriceRange(minPrice: Long, maxPrice: Long): Flow<List<Property>> =
        dao.getPropertiesByPriceRange(minPrice, maxPrice).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override fun getPropertiesFiltered(
        location: String?,
        category: String?,
        minPrice: Long,
        maxPrice: Long
    ): Flow<List<Property>> =
        dao.getPropertiesFiltered(location, category, minPrice, maxPrice).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override fun searchProperties(query: String): Flow<List<Property>> =
        dao.searchProperties(query).map { list -> list.map { PropertyMapper.entityToDomain(it) } }

    override suspend fun addSearchAlert(location: String, propertyType: String, maxPrice: Long) {
        dao.insertSearchAlert(
            com.example.data.local.SearchAlertEntity(
                id = "alert-${System.currentTimeMillis()}",
                location = location,
                propertyType = propertyType,
                maxPrice = maxPrice
            )
        )
    }

    /**
     * Executes Smart Match pipeline:
     * 1. Query Firestore properties matching user preferences (budget, location, property type)
     * 2. If Firestore query returns candidates, caches them in Room
     * 3. Fallbacks to local Room properties if Firestore is offline
     * 4. Evaluates Smart Match scoring algorithm on candidate properties
     * 5. Returns ranked list of SmartMatchResults
     */
    override suspend fun getSmartMatchSuggestions(
        preferences: UserPreferences
    ): List<SmartMatchResult> {
        val firestoreCandidates = firestoreService.querySmartMatchProperties(preferences)

        val isDirectFromFirestore = firestoreCandidates.isNotEmpty()

        val candidates = if (isDirectFromFirestore) {
            runCatching {
                dao.insertProperties(firestoreCandidates.map { PropertyMapper.domainToEntity(it) })
            }
            firestoreCandidates
        } else {
            // Local fallback when network / Firestore is offline or empty
            dao.getAllPropertiesSync().map { PropertyMapper.entityToDomain(it) }
        }

        return candidates.map { prop ->
            val score = SmartMatchEngine.calculateScore(prop, preferences)
            SmartMatchResult(
                property = prop,
                matchScore = score,
                isFromFirestore = isDirectFromFirestore
            )
        }.sortedByDescending { it.matchScore.overallPercentage }
    }
}
