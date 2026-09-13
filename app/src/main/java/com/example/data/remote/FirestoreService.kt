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
                location = doc.getString("location") ?: "Kottakuppam",
                approximateArea = doc.getString("approximateArea") ?: "",
                distanceKm = doc.getDouble("distanceKm") ?: 1.0,
                bedrooms = doc.getLong("bedrooms")?.toInt() ?: 2,
                bathrooms = doc.getLong("bathrooms")?.toInt() ?: 2,
                areaSqFt = doc.getLong("areaSqFt")?.toInt() ?: 1000,
                urgencyScore = doc.getLong("urgencyScore")?.toInt() ?: 50,
                verificationLevel = doc.getLong("verificationLevel")?.toInt() ?: 1,
                imageResName = doc.getString("imageResName") ?: "prop_1",
                featuresList = (doc.get("featuresList") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                suitableFor = doc.getString("suitableFor") ?: "All",
                leaseDurationMonths = doc.getLong("leaseDurationMonths")?.toInt(),
                isDepositRefundable = doc.getBoolean("isDepositRefundable") ?: true,
                ownerName = doc.getString("ownerName") ?: "Owner",
                ownerPhone = doc.getString("ownerPhone") ?: "",
                ownerType = doc.getString("ownerType") ?: "Owner",
                isSaved = false,
                viewsCount = doc.getLong("viewsCount")?.toInt() ?: 0,
                savedCount = doc.getLong("savedCount")?.toInt() ?: 0,
                messagesCount = doc.getLong("messagesCount")?.toInt() ?: 0,
                visitRequestsCount = doc.getLong("visitRequestsCount")?.toInt() ?: 0,
                interestedBuyersCount = doc.getLong("interestedBuyersCount")?.toInt() ?: 0,
                mapLat = doc.getDouble("mapLat") ?: 11.9754,
                mapLng = doc.getDouble("mapLng") ?: 79.8360,
                isPrivate = doc.getBoolean("isPrivate") ?: false,
                status = doc.getString("status") ?: "AVAILABLE"
            )
        }.getOrNull()
    }

    /**
     * One-time fetch of all properties from Firestore for manual pull-to-refresh
     */
    suspend fun fetchPropertiesOnce(): List<Property> = runCatching {
        val db = firestore ?: return@runCatching emptyList()
        val snapshot = db.collection("properties").get().await()
        snapshot.documents.mapNotNull { parseDocToProperty(it) }
    }.getOrDefault(emptyList())

    /**
     * Stream properties in real-time from Firestore collection "properties"
     */
    fun streamProperties(): Flow<List<Property>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("properties")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreService", "Listen failed: ${error.message}")
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

        val hasLocation = preferences.location.isNotBlank() &&
                !preferences.location.equals("All", ignoreCase = true) &&
                !preferences.location.equals("All Locations", ignoreCase = true)

        val hasType = preferences.propertyType.isNotBlank() &&
                !preferences.propertyType.equals("All", ignoreCase = true) &&
                !preferences.propertyType.equals("All Types", ignoreCase = true)

        val hasBudget = preferences.maxBudget > 0L && preferences.maxBudget < Long.MAX_VALUE

        val candidateDocs = runCatching {
            var q: Query = collection

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
                    collection.whereEqualTo("location", preferences.location).get().await().documents
                } else if (hasBudget) {
                    val upperLimit = (preferences.maxBudget * 1.25).toLong()
                    collection.whereLessThanOrEqualTo("price", upperLimit).get().await().documents
                } else {
                    collection.get().await().documents
                }
            }.getOrElse {
                runCatching { collection.get().await().documents }.getOrDefault(emptyList())
            }
        }

        val parsedProperties = candidateDocs.mapNotNull { parseDocToProperty(it) }

        // If strict query yielded fewer than 3 properties, supplement with broader Firestore properties
        // so the Smart Match algorithm can score and surface best alternatives
        if (parsedProperties.size < 3) {
            val allSnapshot = runCatching { collection.limit(20).get().await() }.getOrNull()
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
    suspend fun saveProperty(property: Property): Result<Unit> = runCatching {
        val db = firestore ?: throw IllegalStateException("Firestore not initialized")
        val data = hashMapOf(
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
            "featuresList" to property.featuresList,
            "suitableFor" to property.suitableFor,
            "leaseDurationMonths" to property.leaseDurationMonths,
            "isDepositRefundable" to property.isDepositRefundable,
            "ownerName" to property.ownerName,
            "ownerPhone" to property.ownerPhone,
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
     * Save a scheduled visit to Firestore
     */
    suspend fun saveVisit(visit: PropertyVisit): Result<Unit> = runCatching {
        val db = firestore ?: return@runCatching
        val data = hashMapOf(
            "id" to visit.id,
            "propertyId" to visit.propertyId,
            "propertyTitle" to visit.propertyTitle,
            "location" to visit.location,
            "buyerName" to visit.buyerName,
            "date" to visit.date,
            "timeSlot" to visit.timeSlot,
            "status" to visit.status,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection("visits").document(visit.id).set(data, SetOptions.merge()).await()
    }
}
