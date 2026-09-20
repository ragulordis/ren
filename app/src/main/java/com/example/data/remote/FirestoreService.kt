package com.example.data.remote

import android.util.Log
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.PropertyVisit
import com.example.data.model.SellingSpeed
import com.example.data.model.UserPreferences
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }
            .onFailure { Log.w("FirestoreService", "Firebase Firestore not yet initialized: ${it.message}") }
            .getOrNull()
    }

    private fun parseDocToProperty(doc: DocumentSnapshot): Property? {
        return runCatching {
            val ownerId = doc.getString("ownerId")?.takeIf { it.isNotBlank() } ?: return@runCatching null
            Property(
                id = doc.getString("id") ?: doc.id,
                ownerId = ownerId,
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                listingType = runCatching { ListingType.valueOf(doc.getString("listingType") ?: "BUY") }.getOrDefault(ListingType.BUY),
                sellingSpeed = runCatching { SellingSpeed.valueOf(doc.getString("sellingSpeed") ?: "NORMAL") }.getOrDefault(SellingSpeed.NORMAL),
                category = runCatching { PropertyCategory.valueOf(doc.getString("category") ?: "BUY") }.getOrDefault(PropertyCategory.BUY),
                propertyType = doc.getString("propertyType") ?: "Apartment",
                price = doc.getLong("price") ?: 0L,
                originalPrice = doc.getLong("originalPrice") ?: (doc.getLong("price") ?: 0L),
                marketEstimate = doc.getLong("marketEstimate") ?: (doc.getLong("price") ?: 0L),
                location = doc.getString("location") ?: "",
                approximateArea = doc.getString("approximateArea") ?: "",
                distanceKm = doc.getDouble("distanceKm") ?: 0.0,
                bedrooms = doc.getLong("bedrooms")?.toInt() ?: 0,
                bathrooms = doc.getLong("bathrooms")?.toInt() ?: 0,
                areaSqFt = doc.getLong("areaSqFt")?.toInt() ?: 0,
                urgencyScore = doc.getLong("urgencyScore")?.toInt() ?: 0,
                verificationLevel = doc.getLong("verificationLevel")?.toInt() ?: 0,
                imageResName = doc.getString("imageResName") ?: "",
                imageUrls = (doc.get("imageUrls") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                furnishing = doc.getString("furnishing") ?: "",
                securityDeposit = doc.getLong("securityDeposit") ?: 0L,
                maintenanceAmount = doc.getLong("maintenanceAmount") ?: 0L,
                isMaintenanceIncluded = doc.getBoolean("isMaintenanceIncluded") ?: false,
                availableFrom = doc.getString("availableFrom") ?: "",
                nearbyLandmark = doc.getString("nearbyLandmark") ?: "",
                tenantPreferences = (doc.get("tenantPreferences") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                featuresList = (doc.get("featuresList") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                suitableFor = doc.getString("suitableFor") ?: "All",
                leaseDurationMonths = doc.getLong("leaseDurationMonths")?.toInt(),
                isDepositRefundable = doc.getBoolean("isDepositRefundable") ?: true,
                ownerName = doc.getString("ownerName") ?: "",
                ownerPhone = doc.getString("ownerPhone") ?: "",
                ownerType = doc.getString("ownerType") ?: "Owner",
                isSaved = false,
                viewsCount = doc.getLong("viewsCount")?.toInt() ?: 0,
                savedCount = doc.getLong("savedCount")?.toInt() ?: 0,
                messagesCount = doc.getLong("messagesCount")?.toInt() ?: 0,
                visitRequestsCount = doc.getLong("visitRequestsCount")?.toInt() ?: 0,
                interestedBuyersCount = doc.getLong("interestedBuyersCount")?.toInt() ?: 0,
                mapLat = doc.getDouble("mapLat") ?: 0.0,
                mapLng = doc.getDouble("mapLng") ?: 0.0,
                isPrivate = doc.getBoolean("isPrivate") ?: false,
                status = doc.getString("status") ?: "AVAILABLE"
            )
        }.getOrNull()
    }

    /**
     * One-time fetch of all properties from Firestore for manual pull-to-refresh.
     * Throws IllegalStateException or FirebaseException on failure so callers can distinguish errors from empty data.
     */
    suspend fun fetchPropertiesOnce(): List<Property> {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val snapshot = db.collection("properties")
            .whereEqualTo("moderationStatus", "ACTIVE")
            .get().await()
        return snapshot.documents.mapNotNull { parseDocToProperty(it) }
    }

    /**
     * Stream properties in real-time from Firestore collection "properties"
     */
    fun streamProperties(): Flow<List<Property>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val listener = db.collection("properties")
            .whereEqualTo("moderationStatus", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreService", "Listen failed: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val properties = snapshot.documents.mapNotNull { parseDocToProperty(it) }
                    trySend(properties)
                }
            }

        awaitClose { listener.remove() }
    }

    /**
     * Executes targeted Firestore queries against the "properties" collection based on user preferences.
     * Implements a multi-tier query strategy:
     * - Tier 1: Compound Firestore query on location, propertyType, and budget threshold
     * - Tier 2: Graceful fallback query on location or price range
     * - Tier 3: Fetch candidate documents from Firestore for ranking
     */
    suspend fun querySmartMatchProperties(preferences: UserPreferences): List<Property> = runCatching {
        val db = firestore ?: return@runCatching emptyList()
        val collection = db.collection("properties")
        // Firestore rules intentionally permit public collection queries only for
        // moderator-approved listings, so every marketplace query includes this.
        val activeCollection = collection.whereEqualTo("moderationStatus", "ACTIVE")

        val hasLocation = preferences.location.isNotBlank() &&
                !preferences.location.equals("All", ignoreCase = true) &&
                !preferences.location.equals("All Locations", ignoreCase = true)

        val hasType = preferences.propertyType.isNotBlank() &&
                !preferences.propertyType.equals("All", ignoreCase = true) &&
                !preferences.propertyType.equals("All Types", ignoreCase = true)

        val hasBudget = preferences.maxBudget > 0L && preferences.maxBudget < Long.MAX_VALUE

        val candidateDocs = runCatching {
            var q: Query = activeCollection

            if (hasLocation) {
                q = q.whereEqualTo("location", preferences.location)
            }
            if (hasType) {
                q = q.whereEqualTo("propertyType", preferences.propertyType)
            }
            if (hasBudget) {
                val upperLimit = (preferences.maxBudget * 1.2).toLong()
                q = q.whereLessThanOrEqualTo("price", upperLimit)
            }

            q.get().await().documents
        }.getOrElse { error ->
            Log.w("FirestoreService", "Targeted query failed (${error.message}), applying secondary Firestore query")
            runCatching {
                if (hasLocation) {
                    activeCollection.whereEqualTo("location", preferences.location).get().await().documents
                } else if (hasBudget) {
                    val upperLimit = (preferences.maxBudget * 1.25).toLong()
                    activeCollection.whereLessThanOrEqualTo("price", upperLimit).get().await().documents
                } else {
                    activeCollection.get().await().documents
                }
            }.getOrElse {
                runCatching { activeCollection.get().await().documents }.getOrDefault(emptyList())
            }
        }

        val parsedProperties = candidateDocs.mapNotNull { parseDocToProperty(it) }

        // If strict query yielded fewer than 3 properties, supplement with broader Firestore properties
        // so the Smart Match algorithm can score and surface best alternatives
        if (parsedProperties.size < 3) {
            val allSnapshot = runCatching { activeCollection.limit(20).get().await() }.getOrNull()
            val allDocs = allSnapshot?.documents?.mapNotNull { parseDocToProperty(it) } ?: emptyList()
            (parsedProperties + allDocs).distinctBy { it.id }
        } else {
            parsedProperties
        }
    }.getOrElse { error ->
        Log.e("FirestoreService", "Error in querySmartMatchProperties: ${error.message}")
        emptyList()
    }

    /**
     * Upload or update a property in Firestore
     */
    suspend fun saveProperty(property: Property, isNewListing: Boolean = false): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val data = hashMapOf<String, Any?>(
            "id" to property.id,
            "ownerId" to property.ownerId,
            "title" to property.title,
            "description" to property.description,
            "listingType" to property.listingType.name,
            "sellingSpeed" to property.sellingSpeed.name,
            "category" to property.category.name,
            "propertyType" to property.propertyType,
            "price" to property.price,
            "originalPrice" to property.originalPrice,
            "marketEstimate" to property.marketEstimate,
            "location" to property.location,
            "approximateArea" to property.approximateArea,
            "distanceKm" to property.distanceKm,
            "bedrooms" to property.bedrooms,
            "bathrooms" to property.bathrooms,
            "areaSqFt" to property.areaSqFt,
            "urgencyScore" to property.urgencyScore,
            "verificationLevel" to property.verificationLevel,
            "imageResName" to property.imageResName,
            "imageUrls" to property.imageUrls,
            "furnishing" to property.furnishing,
            "securityDeposit" to property.securityDeposit,
            "maintenanceAmount" to property.maintenanceAmount,
            "isMaintenanceIncluded" to property.isMaintenanceIncluded,
            "availableFrom" to property.availableFrom,
            "nearbyLandmark" to property.nearbyLandmark,
            "tenantPreferences" to property.tenantPreferences,
            "featuresList" to property.featuresList,
            "suitableFor" to property.suitableFor,
            "leaseDurationMonths" to property.leaseDurationMonths,
            "isDepositRefundable" to property.isDepositRefundable,
            "ownerName" to property.ownerName,
            "ownerType" to property.ownerType,
            "viewsCount" to property.viewsCount,
            "savedCount" to property.savedCount,
            "messagesCount" to property.messagesCount,
            "visitRequestsCount" to property.visitRequestsCount,
            "interestedBuyersCount" to property.interestedBuyersCount,
            "mapLat" to property.mapLat,
            "mapLng" to property.mapLng,
            "isPrivate" to property.isPrivate,
            "status" to property.status,
            "updatedAt" to System.currentTimeMillis()
        )
        if (isNewListing) data["moderationStatus"] = "PENDING"
        db.collection("properties").document(property.id)
            .set(data, SetOptions.merge())
            .await()
    }

    /**
     * Delete property from Firestore
     */
    suspend fun deleteProperty(propertyId: String): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        db.collection("properties").document(propertyId).delete().await()
    }

    /**
     * Update property listing lifecycle status in Firestore
     */
    suspend fun updatePropertyStatus(propertyId: String, status: String): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        db.collection("properties").document(propertyId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    /**
     * Save a scheduled visit to Firestore.
     * Includes buyerId (auth UID) and sellerId (property ownerId) required by Firestore security rules:
     *   allow write: if request.auth.uid == request.resource.data.buyerId;
     */
    suspend fun saveVisit(visit: PropertyVisit): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val data = hashMapOf(
            "id" to visit.id,
            "propertyId" to visit.propertyId,
            "propertyTitle" to visit.propertyTitle,
            "location" to visit.location,
            "buyerId" to visit.buyerId,
            "sellerId" to visit.sellerId,
            "buyerName" to visit.buyerName,
            "date" to visit.date,
            "timeSlot" to visit.timeSlot,
            "status" to visit.status,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("visits").document(visit.id).set(data, SetOptions.merge()).await()
    }

    /**
     * Update visit lifecycle status in Firestore.
     */
    suspend fun updateVisitStatus(visitId: String, status: String): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        db.collection("visits").document(visitId)
            .update(
                mapOf(
                    "status" to status,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    /**
     * Resolves the authoritative conversation document ID.
     * When a buyerId is present and differs from the property ownerId, scopes conversation to propertyId_buyerId.
     * Otherwise falls back to propertyId for backward compatibility.
     */
    fun resolveConversationId(propertyId: String, participantId: String = ""): String {
        return if (participantId.isNotBlank()) {
            "${propertyId}_${participantId}"
        } else {
            propertyId
        }
    }

    /**
     * Sync a chat message to Firestore at /conversations/{conversationId}/messages/{msgId}.
     * Ensures parent conversation document exists with authoritative participants (property owner + buyer).
     */
    suspend fun saveChatMessage(
        propertyId: String,
        message: com.example.data.model.ChatMessage,
        buyerId: String = ""
    ): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val senderId = message.senderId.ifBlank {
            runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: ""
        }

        // Authoritative participant derivation: resolve property ownerId from backend
        val propDoc = runCatching { db.collection("properties").document(propertyId).get().await() }.getOrNull()
        val ownerId = propDoc?.getString("ownerId") ?: ""

        val effectiveBuyerId = buyerId.ifBlank {
            if (senderId != ownerId) senderId else ""
        }
        val conversationDocId = if (effectiveBuyerId.isNotBlank()) {
            resolveConversationId(propertyId, effectiveBuyerId)
        } else {
            propertyId
        }

        // Ensure parent conversation exists and has authoritative participants (owner + sender)
        val convRef = db.collection("conversations").document(conversationDocId)
        val convDoc = runCatching { convRef.get().await() }.getOrNull()
        if (convDoc == null || !convDoc.exists()) {
            val participants = mutableSetOf<String>()
            if (ownerId.isNotBlank()) participants.add(ownerId)
            if (senderId.isNotBlank()) participants.add(senderId)
            if (effectiveBuyerId.isNotBlank()) participants.add(effectiveBuyerId)

            convRef.set(mapOf(
                "id" to conversationDocId,
                "propertyId" to propertyId,
                "sellerId" to ownerId,
                "buyerId" to effectiveBuyerId,
                "participants" to participants.toList(),
                "lastUpdatedAt" to System.currentTimeMillis()
            ), SetOptions.merge()).await()
        }

        val data = hashMapOf(
            "id" to message.id,
            "propertyId" to propertyId,
            "conversationId" to conversationDocId,
            "senderId" to senderId,
            "senderName" to message.senderName,
            "message" to message.message,
            "time" to message.time,
            "timestamp" to System.currentTimeMillis()
        )
        convRef.collection("messages")
            .document(message.id)
            .set(data, SetOptions.merge())
            .await()
    }

    /**
     * Submit a property report to Firestore at /reports/{reportId}.
     * This is the only persistent backend record visible to moderators.
     */
    suspend fun saveReport(
        propertyId: String,
        propertyTitle: String,
        reason: String,
        details: String,
        reportId: String,
        reporterId: String = ""
    ): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val resolvedReporterId = reporterId.ifBlank {
            runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: ""
        }
        val data = hashMapOf(
            "id" to reportId,
            "propertyId" to propertyId,
            "propertyTitle" to propertyTitle,
            "reason" to reason,
            "details" to details,
            "reporterId" to resolvedReporterId,
            "status" to "PENDING_REVIEW",
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("reports").document(reportId).set(data, SetOptions.merge()).await()
    }

    suspend fun blockUser(blockedUserId: String): Result<Unit> = runCatching {
        require(blockedUserId.isNotBlank()) { "A user is required" }
        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Sign in required")
        require(currentUid != blockedUserId) { "You cannot block yourself" }
        firestore!!.collection("users").document(currentUid).collection("blocks")
            .document(blockedUserId)
            .set(mapOf("blockedAt" to System.currentTimeMillis()))
            .await()
    }

    /**
     * Stream real-time chat messages from Firestore for cross-device delivery.
     * Messages are ordered by timestamp ascending so the conversation thread renders correctly.
     * Fails fast by closing the Flow with an error if Firestore is uninitialized or the listener encounters an error.
     */
    fun streamChatMessages(
        propertyId: String,
        buyerId: String = ""
    ): kotlinx.coroutines.flow.Flow<List<com.example.data.model.ChatMessage>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val currentUid = runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        val effectiveBuyerId = buyerId.ifBlank { currentUid ?: "" }
        val conversationDocId = if (effectiveBuyerId.isNotBlank()) {
            resolveConversationId(propertyId, effectiveBuyerId)
        } else {
            propertyId
        }

        val listener = db.collection("conversations")
            .document(conversationDocId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreService", "Chat stream failed for $conversationDocId: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            val senderId = doc.getString("senderId") ?: ""
                            val isMine = if (currentUid != null && senderId.isNotBlank()) {
                                senderId == currentUid
                            } else {
                                doc.getBoolean("isFromMe") ?: false
                            }
                            com.example.data.model.ChatMessage(
                                id = doc.getString("id") ?: doc.id,
                                propertyId = doc.getString("propertyId") ?: propertyId,
                                senderId = senderId,
                                senderName = doc.getString("senderName") ?: "User",
                                message = doc.getString("message") ?: "",
                                time = doc.getString("time") ?: "Just now",
                                isFromMe = isMine
                            )
                        }.getOrNull()
                    }
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }
}
