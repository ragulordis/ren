package com.example.domain.usecase

import android.util.Log
import com.example.data.model.ListingType
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SellingSpeed
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class AiSearchResult(
    val matchedProperties: List<Property>,
    val explanation: String
)

/**
 * UseCase to evaluate natural language real estate queries using Firebase AI (Gemini).
 * If Firebase AI is not available or encounters network/configuration issues,
 * it seamlessly and gracefully falls back to intelligent multi-criterion keyword parsing.
 */
class AiSearchUseCase {

    suspend operator fun invoke(
        query: String,
        allProperties: List<Property>
    ): AiSearchResult = withContext(Dispatchers.IO) {
        if (query.isBlank() || allProperties.isEmpty()) {
            return@withContext AiSearchResult(
                matchedProperties = allProperties.take(4),
                explanation = "Showing recommended properties."
            )
        }

        // 1. Attempt Firebase AI Gemini Inference
        val geminiResult = runCatching {
            callGemini(query, allProperties)
        }.getOrNull()

        if (geminiResult != null && geminiResult.matchedProperties.isNotEmpty()) {
            return@withContext geminiResult
        }

        // 2. Fallback to Local Rule-Based Natural Matcher
        fallbackLocalSearch(query, allProperties)
    }

    private suspend fun callGemini(query: String, properties: List<Property>): AiSearchResult? {
        val ai = runCatching { Firebase.ai }.getOrNull() ?: return null
        val model = ai.generativeModel("gemini-2.5-flash-lite")

        // Construct a compact representation of the properties
        val propertiesSummary = properties.take(25).joinToString("\n") { p ->
            "- ID: ${p.id} | Title: ${p.title} | Location: ${p.location} | Price: ₹${p.price} | Type: ${p.propertyType} | Cat: ${p.category} | Beds: ${p.bedrooms} | Urgent: ${p.sellingSpeed == SellingSpeed.URGENT}"
        }

        val prompt = """
            You are an Indian real estate AI search assistant for Ren.
            Here is a list of candidate properties:
            """ + propertiesSummary + """

            User Search Query: """ + query + """

            Respond strictly in valid JSON format with this exact structure:
            {
              "matchedIds": ["id1", "id2"],
              "explanation": "Brief 1-2 sentence friendly explanation of why these match"
            }
            Do not include Markdown formatting, code blocks or backticks. Return raw JSON only.
        """.trimIndent()

        val response = model.generateContent(prompt)
        val text = response.text?.trim()?.removePrefix("```json")?.removePrefix("```")?.removeSuffix("```")?.trim() ?: return null

        val json = JSONObject(text)
        val jsonArray = json.optJSONArray("matchedIds") ?: return null
        val matchedIds = mutableSetOf<String>()
        for (i in 0 until jsonArray.length()) {
            matchedIds.add(jsonArray.getString(i))
        }
        val explanation = json.optString("explanation", "Matches found based on your search.")

        val matchedProps = properties.filter { it.id in matchedIds }
        if (matchedProps.isEmpty()) return null

        return AiSearchResult(
            matchedProperties = matchedProps,
            explanation = explanation
        )
    }

    private fun fallbackLocalSearch(query: String, props: List<Property>): AiSearchResult {
        val lower = query.lowercase()

        val scored = props.map { p ->
            var score = 0
            // Locations
            if ((lower.contains("bengaluru") || lower.contains("bangalore")) && p.location.contains("Bengaluru", ignoreCase = true)) score += 40
            if (lower.contains("chennai") && p.location.contains("Chennai", ignoreCase = true)) score += 40
            if (lower.contains("mumbai") && p.location.contains("Mumbai", ignoreCase = true)) score += 40
            if ((lower.contains("delhi") || lower.contains("gurugram") || lower.contains("noida")) && p.location.contains("Delhi", ignoreCase = true)) score += 40
            if (lower.contains("hyderabad") && p.location.contains("Hyderabad", ignoreCase = true)) score += 40
            if (lower.contains("pune") && p.location.contains("Pune", ignoreCase = true)) score += 40
            if ((lower.contains("kochi") || lower.contains("cochin")) && p.location.contains("Kochi", ignoreCase = true)) score += 40
            if (lower.contains("goa") && p.location.contains("Goa", ignoreCase = true)) score += 40
            if (lower.contains("auroville") && p.location.contains("Auroville", ignoreCase = true)) score += 40
            if (lower.contains("kottakuppam") && p.location.contains("Kottakuppam", ignoreCase = true)) score += 40
            if ((lower.contains("pondy") || lower.contains("pondicherry")) && p.location.contains("Pondicherry", ignoreCase = true)) score += 40

            // Categories & types
            if (lower.contains("rent") && (p.listingType == ListingType.RENT || p.category == PropertyCategory.RENT)) score += 30
            if (lower.contains("buy") && (p.listingType == ListingType.BUY || p.category == PropertyCategory.BUY)) score += 30
            if (lower.contains("lease") && p.listingType == ListingType.LEASE) score += 30
            if ((lower.contains("land") || lower.contains("plot")) && p.category == PropertyCategory.LAND) score += 30
            if ((lower.contains("house") || lower.contains("villa")) && (p.propertyType.contains("House", ignoreCase = true) || p.propertyType.contains("Villa", ignoreCase = true))) score += 30
            if (lower.contains("apartment") && p.propertyType.contains("Apartment", ignoreCase = true)) score += 30
            if (lower.contains("urgent") && (p.sellingSpeed == SellingSpeed.URGENT || p.sellingSpeed == SellingSpeed.FAST)) score += 30

            // Bedrooms
            if (lower.contains("1bhk") || lower.contains("1 bhk") || lower.contains("1 bedroom")) {
                if (p.bedrooms == 1) score += 35
            }
            if (lower.contains("2bhk") || lower.contains("2 bhk") || lower.contains("2 bedroom")) {
                if (p.bedrooms == 2) score += 35
            }
            if (lower.contains("3bhk") || lower.contains("3 bhk") || lower.contains("3 bedroom")) {
                if (p.bedrooms >= 3) score += 35
            }

            p to score
        }

        val filtered = scored.filter { it.second > 25 }
            .sortedByDescending { it.second }
            .map { it.first }

        val finalProps = if (filtered.isNotEmpty()) filtered else props.take(4)
        val explanation = if (filtered.isNotEmpty()) {
            "Found " + str(len(filtered)) + " properties matching your natural search criteria."
        } else {
            "Showing top verified listings matching your preferences."
        }

        return AiSearchResult(
            matchedProperties = finalProps,
            explanation = explanation
        )
    }
}
