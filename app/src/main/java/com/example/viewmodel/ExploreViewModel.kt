package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.QuickNestDatabase
import com.example.data.model.BudgetFilter
import com.example.data.model.Property
import com.example.data.model.PropertyCategory
import com.example.data.model.SortOption
import com.example.data.repository.PropertyRepositoryImpl
import com.example.domain.usecase.FilterCriteria
import com.example.domain.usecase.FilterPropertiesUseCase
import com.example.domain.usecase.GetPropertiesUseCase
import com.example.domain.usecase.ToggleSavePropertyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(
    application: Application,
    private val getPropertiesUseCase: GetPropertiesUseCase = GetPropertiesUseCase(
        PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao())
    ),
    private val filterPropertiesUseCase: FilterPropertiesUseCase = FilterPropertiesUseCase(),
    private val toggleSaveUseCase: ToggleSavePropertyUseCase = ToggleSavePropertyUseCase(
        PropertyRepositoryImpl(QuickNestDatabase.getInstance(application).propertyDao())
    )
) : AndroidViewModel(application) {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PropertyCategory.ALL)
    val selectedCategory: StateFlow<PropertyCategory> = _selectedCategory.asStateFlow()

    private val _selectedPropertyType = MutableStateFlow<String?>(null)
    val selectedPropertyType: StateFlow<String?> = _selectedPropertyType.asStateFlow()

    private val _selectedLocation = MutableStateFlow("All")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private val _selectedBudget = MutableStateFlow(BudgetFilter.ALL)
    val selectedBudget: StateFlow<BudgetFilter> = _selectedBudget.asStateFlow()

    private val _selectedBedrooms = MutableStateFlow(0)
    val selectedBedrooms: StateFlow<Int> = _selectedBedrooms.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(SortOption.URGENCY)
    val selectedSortOption: StateFlow<SortOption> = _selectedSortOption.asStateFlow()

    private val _verifiedOnly = MutableStateFlow(false)
    val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

    private val _urgentOnly = MutableStateFlow(false)
    val urgentOnly: StateFlow<Boolean> = _urgentOnly.asStateFlow()

    private val _minPrice = MutableStateFlow<Long?>(null)
    val minPrice: StateFlow<Long?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Long?>(null)
    val maxPrice: StateFlow<Long?> = _maxPrice.asStateFlow()

    private val _showFilterSheet = MutableStateFlow(false)
    val showFilterSheet: StateFlow<Boolean> = _showFilterSheet.asStateFlow()

    private val allPropertiesFlow: StateFlow<List<Property>> = getPropertiesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProperties: StateFlow<List<Property>> = combine(
        allPropertiesFlow,
        _searchQuery,
        _selectedCategory,
        _selectedPropertyType,
        _selectedLocation,
        _selectedBudget,
        _selectedBedrooms,
        _selectedSortOption,
        _verifiedOnly,
        _urgentOnly,
        _minPrice,
        _maxPrice
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val properties = args[0] as List<Property>
        val criteria = FilterCriteria(
            query = args[1] as String,
            category = args[2] as PropertyCategory,
            propertyType = args[3] as? String,
            location = args[4] as String,
            budget = args[5] as BudgetFilter,
            bedrooms = args[6] as Int,
            sortOption = args[7] as SortOption,
            verifiedOnly = args[8] as Boolean,
            urgentOnly = args[9] as Boolean,
            minPrice = args[10] as? Long,
            maxPrice = args[11] as? Long
        )
        filterPropertiesUseCase(properties, criteria)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: PropertyCategory) { _selectedCategory.value = category }
    fun setPropertyType(type: String?) { _selectedPropertyType.value = type }
    fun setLocation(loc: String) { _selectedLocation.value = loc }
    fun setBudget(filter: BudgetFilter) { _selectedBudget.value = filter }
    fun setBedrooms(bhk: Int) { _selectedBedrooms.value = bhk }
    fun setSortOption(sort: SortOption) { _selectedSortOption.value = sort }
    fun setVerifiedOnly(v: Boolean) { _verifiedOnly.value = v }
    fun setUrgentOnly(u: Boolean) { _urgentOnly.value = u }
    fun openFilterSheet() { _showFilterSheet.value = true }
    fun closeFilterSheet() { _showFilterSheet.value = false }

    fun toggleSave(property: Property) {
        viewModelScope.launch {
            toggleSaveUseCase(property)
        }
    }

    fun resetFilters() {
        _selectedCategory.value = PropertyCategory.ALL
        _selectedPropertyType.value = null
        _selectedLocation.value = "All"
        _selectedBudget.value = BudgetFilter.ALL
        _selectedBedrooms.value = 0
        _selectedSortOption.value = SortOption.URGENCY
        _verifiedOnly.value = false
        _urgentOnly.value = false
        _minPrice.value = null
        _maxPrice.value = null
    }
}
