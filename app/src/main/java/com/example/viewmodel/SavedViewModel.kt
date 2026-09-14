package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.repository.PropertyRepository
import com.example.domain.usecase.GetPropertiesUseCase
import com.example.domain.usecase.ToggleSavePropertyUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    private val propertyRepository: PropertyRepository,
    private val getPropertiesUseCase: GetPropertiesUseCase,
    private val toggleSaveUseCase: ToggleSavePropertyUseCase
) : ViewModel() {

    val savedProperties: StateFlow<List<Property>> = getPropertiesUseCase.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visits: StateFlow<List<PropertyVisit>> = propertyRepository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            toggleSaveUseCase(property)
        }
    }

    fun updateVisitStatus(visitId: String, status: String) {
        viewModelScope.launch {
            propertyRepository.updateVisitStatus(visitId, status)
        }
    }

    fun cancelVisit(visitId: String) {
        viewModelScope.launch {
            propertyRepository.updateVisitStatus(visitId, "Cancelled")
        }
    }

    fun deleteVisit(visitId: String) {
        viewModelScope.launch {
            propertyRepository.deleteVisit(visitId)
        }
    }
}
