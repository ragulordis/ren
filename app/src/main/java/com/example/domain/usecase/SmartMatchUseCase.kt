package com.example.domain.usecase

import com.example.data.model.SmartMatchResult
import com.example.data.model.UserPreferences
import com.example.data.repository.PropertyRepository

/**
 * UseCase to invoke the hyperlocal Smart Match engine.
 */
class SmartMatchUseCase(
    private val repository: PropertyRepository
) {
    suspend operator fun invoke(preferences: UserPreferences): List<SmartMatchResult> {
        return repository.getSmartMatchSuggestions(preferences)
    }
}
