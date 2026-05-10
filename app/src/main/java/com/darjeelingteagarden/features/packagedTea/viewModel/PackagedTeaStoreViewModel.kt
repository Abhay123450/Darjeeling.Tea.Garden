package com.darjeelingteagarden.features.packagedTea.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darjeelingteagarden.features.packagedTea.api.PackagedTeaApiService
import com.darjeelingteagarden.features.packagedTea.model.PackagedTea
import com.darjeelingteagarden.features.packagedTea.model.PackagedTeaCatalog
import com.darjeelingteagarden.util.normalizeAndCapitalize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PackagedFilterState(
    val searchQuery: String = "",
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val selectedArticleTypes: Set<String> = emptySet(),
    val selectedCategories: Set<String> = emptySet(),
    val selectedBrands: Set<String> = emptySet(),
    val selectedWeights: Set<String> = emptySet()
)

data class AvailablePackagedFilters(
    val articleTypes: Set<String> = emptySet(),
    val categories: Set<String> = emptySet(),
    val brands: Set<String> = emptySet(),
    val weights: Set<String> = emptySet(),
    val minPossiblePrice: Int = 0,
    val maxPossiblePrice: Int = 0
)

enum class PackagedFilterType {
    ARTICLE_TYPE, CATEGORY, BRAND, WEIGHT
}

class PackagedTeaStoreViewModel(private val apiService: PackagedTeaApiService) : ViewModel() {

    private val _packagedTeaList = MutableStateFlow<List<PackagedTea>>(emptyList())
    val packagedTeaList = _packagedTeaList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _filterState = MutableStateFlow(PackagedFilterState())
    val filterState = _filterState.asStateFlow()

    // Extracts available options based on current inventory
    val availableFilters = _packagedTeaList.map { teas ->
        AvailablePackagedFilters(
            articleTypes = teas.map { it.articleType.name }.toSet(),
            categories = teas.map { it.category }.toSet(),
            brands = teas.map { it.brandName }.toSet(),
            weights = teas.map { "${it.articleWeight.quantity} ${it.articleWeight.unit}" }.toSet(),
            minPossiblePrice = teas.minOfOrNull { it.mrp } ?: 0,
            maxPossiblePrice = teas.maxOfOrNull { it.mrp } ?: Int.MAX_VALUE
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AvailablePackagedFilters())

    // The Reactive Catalog
    val packagedTeaCatalog: StateFlow<List<PackagedTeaCatalog>> = combine(
        _packagedTeaList,
        _filterState
    ) { teas, state ->
        val filteredTeas = teas.filter { tea ->

            // 1. Search Query
            val matchesSearch = state.searchQuery.isBlank() ||
                    tea.name.contains(state.searchQuery, ignoreCase = true) ||
                    tea.brandName.contains(state.searchQuery, ignoreCase = true) ||
                    tea.category.contains(state.searchQuery, ignoreCase = true) ||
                    tea.articleType.name.contains(state.searchQuery, ignoreCase = true) ||
                    tea.articleWeight.unit.contains(state.searchQuery, ignoreCase = true) ||
                    tea.description.contains(state.searchQuery, ignoreCase = true) ||
                    (tea.searchTerms?.contains(state.searchQuery, ignoreCase = true) ?: false)

            // 2. Price Filtering
            val matchesMinPrice = state.minPrice?.let { tea.mrp >= it } ?: true
            val matchesMaxPrice = state.maxPrice?.let { tea.mrp <= it } ?: true

            // 3. Category/Brand/Type Sets
            val matchesCategory = state.selectedCategories.isEmpty() || state.selectedCategories.contains(tea.category)
            val matchesBrand = state.selectedBrands.isEmpty() || state.selectedBrands.contains(tea.brandName)
            val matchesType = state.selectedArticleTypes.isEmpty() || state.selectedArticleTypes.contains(tea.articleType.name)

            // 4. Weight Filtering (matching against formatted string)
            val weightStr = "${tea.articleWeight.quantity} ${tea.articleWeight.unit}"
            val matchesWeight = state.selectedWeights.isEmpty() || state.selectedWeights.contains(weightStr)

            matchesSearch && matchesMinPrice && matchesMaxPrice &&
                    matchesCategory && matchesBrand && matchesType && matchesWeight
        }

        packagedTeaListToCatalog(filteredTeas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchPackagedTeas()
    }

    private fun fetchPackagedTeas() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPackagedTeas()
                Log.d("PackagedVM::::", "Response: $response")
                if (response.success) {
                    _packagedTeaList.value = response.data
                }
            } catch (e: Exception) {
                Log.e("PackagedVM", "Error fetching teas", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun packagedTeaListToCatalog(list: List<PackagedTea>): List<PackagedTeaCatalog> {
        return list.groupBy { it.category }
            .map { (category, items) ->
                PackagedTeaCatalog(
                    category = normalizeAndCapitalize(category),
                    items = items.sortedBy { it.priority } as MutableList<PackagedTea>
                )
            }
    }

    // --- UI EVENTS ---

    fun updateSearchQuery(query: String) {
        _filterState.update { it.copy(searchQuery = query) }
    }

    fun updatePriceRange(min: Int?, max: Int?) {
        _filterState.update { it.copy(minPrice = min, maxPrice = max) }
    }

    fun toggleFilterSelection(type: PackagedFilterType, value: String) {
        _filterState.update { state ->
            when (type) {
                PackagedFilterType.ARTICLE_TYPE -> state.copy(selectedArticleTypes = state.selectedArticleTypes.toggle(value))
                PackagedFilterType.CATEGORY -> state.copy(selectedCategories = state.selectedCategories.toggle(value))
                PackagedFilterType.BRAND -> state.copy(selectedBrands = state.selectedBrands.toggle(value))
                PackagedFilterType.WEIGHT -> state.copy(selectedWeights = state.selectedWeights.toggle(value))
            }
        }
    }

    fun clearAllFilters() {
        _filterState.value = PackagedFilterState()
    }

    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) minus(value) else plus(value)
    }
}