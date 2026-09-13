package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuickNestDatabase
import com.example.data.model.Property
import com.example.data.model.PropertyVisit
import com.example.data.repository.PropertyRepositoryImpl
import com.example.domain.usecase.GetPropertiesUseCase
import com.example.domain.usecase.ToggleSavePropertyUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedViewModel(
    application: Application,
    private val getPropertiesUseCase: GetPropertiesUseCase = GetPropertiesUseCase(
        PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao())
    ),
    private val toggleSaveUseCase: ToggleSavePropertyUseCase = ToggleSavePropertyUseCase(
        PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao())
    )
) : AndroidViewModel(application) {

    private val repository = PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao())

    val savedProperties: StateFlow<List<Property>> = getPropertiesUseCase.getSaved()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVisits: StateFlow<List<PropertyVisit>> = repository.allVisits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            toggleSaveUseCase(property)
        }
    }

    fun updateVisitStatus(visitId: String, status: String) {
        viewModelScope.launch {
            repository.updateVisitStatus(visitId, status)
        }
    }

    fun deleteVisit(visitId: String) {
        viewModelScope.launch {
            repository.deleteVisit(visitId)
        }
    }
}
