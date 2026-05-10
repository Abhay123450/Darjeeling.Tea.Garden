package com.darjeelingteagarden.features.looseTea.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darjeelingteagarden.features.looseTea.api.LooseTeaApiService
import com.darjeelingteagarden.features.looseTea.model.LooseTea
import com.darjeelingteagarden.features.looseTea.model.LooseTeaCatalog
import com.darjeelingteagarden.util.normalizeAndCapitalize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*


// 1. Define a state class to hold all current filter selections
data class FilterState(
    val searchQuery: String = "",
    val minPricePerKg: Int? = null, // in paise
    val maxPricePerKg: Int? = null, // in paise
    val minPricePerBag: Int? = null, // in paise
    val maxPricePerBag: Int? = null, // in paise
    val selectedGrades: Set<String> = emptySet(),
    val selectedCategories: Set<String> = emptySet(),
    val selectedStandards: Set<String> = emptySet(),
    val selectedBrands: Set<String> = emptySet(),
    val selectedOrigins: Set<String> = emptySet(),
    val selectedAppearances: Set<String> = emptySet()
)

// 2. Define a class to hold all possible filter options for your UI dropdowns
data class AvailableFilters(
    val grades: Set<String> = emptySet(),
    val categories: Set<String> = emptySet(),
    val standards: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val origins: Set<String> = emptySet(),
    val appearances: Set<String> = emptySet(),
    val minPossiblePricePerKg: Int = 0,
    val maxPossiblePricePerKg: Int = 0,
    val minPossiblePricePerBag: Int = 0,
    val maxPossiblePricePerBag: Int = 0
)

enum class FilterType {
    GRADE, CATEGORY, STANDARD, BRAND, ORIGIN, APPEARANCE
}

class LooseTeaStoreViewModel(private val apiService: LooseTeaApiService): ViewModel() {

    // Original list remains completely untouched
    private val _looseTeaList = MutableStateFlow<List<LooseTea>>(emptyList())
    val looseTeaList = _looseTeaList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Holds the current user selections
    private val _filterState = MutableStateFlow(FilterState())
    val filterState = _filterState.asStateFlow()

    // Extracts unique values from the original list to populate your UI Dropdowns/Sliders
    val availableFilters = _looseTeaList.map { teas ->
        AvailableFilters(
            grades = teas.map { it.grade }.toSet(),
            categories = teas.map { it.category }.toSet(),
            standards = teas.map { it.standard }.toSet(),
            brands = teas.map { it.brand }.toSet(),
            origins = teas.map { it.origin }.toSet(),
            appearances = teas.map { it.appearance }.toSet(),
            minPossiblePricePerKg = teas.minOfOrNull { it.pricePerKg.sellingPrice } ?: 0,
            maxPossiblePricePerKg = teas.maxOfOrNull { it.pricePerKg.sellingPrice } ?: Int.MAX_VALUE,
            minPossiblePricePerBag = teas.minOfOrNull { it.pricePerBag.sellingPrice } ?: 0,
            maxPossiblePricePerBag = teas.maxOfOrNull { it.pricePerBag.sellingPrice } ?: Int.MAX_VALUE
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvailableFilters())

    // REACTIVE CATALOG: Automatically updates whenever the data OR the filters change
    val looseTeaCatalog: StateFlow<List<LooseTeaCatalog>> = combine(
        _looseTeaList,
        _filterState
    ) { teas, state ->
        val filteredTeas = teas.filter { tea ->

            // Search matches name, description, or category
            val matchesSearch = state.searchQuery.isBlank() ||
                    tea.name.contains(state.searchQuery, ignoreCase = true) ||
                    tea.description.contains(state.searchQuery, ignoreCase = true) ||
                    tea.category.contains(state.searchQuery, ignoreCase = true) ||
                    tea.grade.contains(state.searchQuery, ignoreCase = true) ||
                    tea.standard.contains(state.searchQuery, ignoreCase = true) ||
                    tea.brand.contains(state.searchQuery, ignoreCase = true) ||
                    tea.origin.contains(state.searchQuery, ignoreCase = true) ||
                    tea.appearance.contains(state.searchQuery, ignoreCase = true) ||
                    tea.lotNumber?.contains(state.searchQuery, ignoreCase = true) == true

            // Price filtering per kg (using sellingPrice in paise)
            val matchesMinPricePerKg = state.minPricePerKg?.let { tea.pricePerKg.sellingPrice >= it } ?: true
            val matchesMaxPricePerKg = state.maxPricePerKg?.let { tea.pricePerKg.sellingPrice <= it } ?: true

            // Price filtering per bag (using sellingPrice in paise)
            val matchesMinPricePerBag = state.minPricePerBag?.let { tea.pricePerBag.sellingPrice >= it } ?: true
            val matchesMaxPricePerBag = state.maxPricePerBag?.let { tea.pricePerBag.sellingPrice <= it } ?: true

            // Set filtering (If a set is empty, it means "All" are selected)
            val matchesGrade = state.selectedGrades.isEmpty() || state.selectedGrades.contains(tea.grade)
            val matchesCategory = state.selectedCategories.isEmpty() || state.selectedCategories.contains(tea.category)
            val matchesStandard = state.selectedStandards.isEmpty() || state.selectedStandards.contains(tea.standard)
            val matchesBrand = state.selectedBrands.isEmpty() || state.selectedBrands.contains(tea.brand)
            val matchesOrigin = state.selectedOrigins.isEmpty() || state.selectedOrigins.contains(tea.origin)
            val matchesAppearance = state.selectedAppearances.isEmpty() || state.selectedAppearances.contains(tea.appearance)

            matchesSearch && matchesMinPricePerKg && matchesMaxPricePerKg
                    && matchesMinPricePerBag && matchesMaxPricePerBag && matchesGrade &&
                    matchesCategory && matchesStandard && matchesBrand && matchesOrigin &&
                    matchesAppearance
        }

        // Convert the filtered list to the catalog format
        looseTeaListToCatalog(filteredTeas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchLooseTeas()
    }

    private fun fetchLooseTeas(){
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getLooseTeas()
                if (response.success) {
                    _looseTeaList.value = response.data
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching loose teas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Modified to return a list instead of updating a MutableStateFlow directly
    private fun looseTeaListToCatalog(list: List<LooseTea>): List<LooseTeaCatalog> {
        val catalogMap = mutableMapOf<String, MutableList<LooseTea>>()
        for (looseTea in list) {
            val category = looseTea.category
            catalogMap.getOrPut(category) { mutableListOf() }.add(looseTea)
        }

        return catalogMap.map { (category, items) ->
            LooseTeaCatalog(
                normalizeAndCapitalize(category), // Assuming this exists elsewhere in your code
                items.sortedBy { it.priority }
            )
        }
    }

    // --- UI EVENT FUNCTIONS ---

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updatePriceRangePerKg(min: Int?, max: Int?) {
        _filterState.update { it.copy(minPricePerKg = min, maxPricePerKg = max) }
    }

    fun updatePriceRangePerBag(min: Int?, max: Int?) {
        _filterState.update { it.copy(minPricePerBag = min, maxPricePerBag = max) }
    }

    // Toggles a string value inside the corresponding filter set
    fun toggleFilterSelection(type: FilterType, value: String) {
        _filterState.update { state ->
            when (type) {
                FilterType.GRADE -> state.copy(selectedGrades = state.selectedGrades.toggle(value))
                FilterType.CATEGORY -> state.copy(selectedCategories = state.selectedCategories.toggle(value))
                FilterType.STANDARD -> state.copy(selectedStandards = state.selectedStandards.toggle(value))
                FilterType.BRAND -> state.copy(selectedBrands = state.selectedBrands.toggle(value))
                FilterType.ORIGIN -> state.copy(selectedOrigins = state.selectedOrigins.toggle(value))
                FilterType.APPEARANCE -> state.copy(selectedAppearances = state.selectedAppearances.toggle(value))
            }
        }
    }

    fun clearAllFilters() {
        _filterState.value = FilterState() // Resets back to default
    }

    // Extension function to easily add/remove an item from a set
    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) minus(value) else plus(value)
    }
}