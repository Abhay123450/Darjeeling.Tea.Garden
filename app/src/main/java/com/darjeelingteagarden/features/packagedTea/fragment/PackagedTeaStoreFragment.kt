package com.darjeelingteagarden.features.packagedTea.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil3.compose.AsyncImage
import com.darjeelingteagarden.common.MyRetrofitClient
import com.darjeelingteagarden.features.cart.CartManager
import com.darjeelingteagarden.features.cart.model.PackagedTeaCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaSampleCartItem
import com.darjeelingteagarden.features.packagedTea.activity.PackagedTeaDetailsActivity
import com.darjeelingteagarden.features.packagedTea.model.ArticleType
import com.darjeelingteagarden.features.packagedTea.model.PackagedTea
import com.darjeelingteagarden.features.packagedTea.model.PackagedTeaCatalog
import com.darjeelingteagarden.features.packagedTea.viewModel.AvailablePackagedFilters
import com.darjeelingteagarden.features.packagedTea.viewModel.PackagedFilterState
import com.darjeelingteagarden.features.packagedTea.viewModel.PackagedFilterType
import com.darjeelingteagarden.features.packagedTea.viewModel.PackagedTeaStoreViewModel
import com.darjeelingteagarden.util.formatPaiseToRupees
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PackagedTeaStoreFragment : Fragment() {

    val colorPrimary = Color(0xFF00CCAA)
    val cardBackgroundColor = Color(0x1100CCAA)

    private val viewModel: PackagedTeaStoreViewModel by viewModels{
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PackagedTeaStoreViewModel(MyRetrofitClient.packagedTeaApi) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                MaterialTheme {
                    val catalog by viewModel.packagedTeaCatalog.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    val filterState by viewModel.filterState.collectAsState()
                    val availableFilters by viewModel.availableFilters.collectAsState()

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    else {
                        PackagedTeaStoreScreen(
                            catalog = catalog,
                            filterState = filterState,
                            availableFilters = availableFilters,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onPriceRangeChange = { min, max -> viewModel.updatePriceRange(min, max) },
                            onFilterToggle = { type, value -> viewModel.toggleFilterSelection(type, value) },
                            onClearFilters = { viewModel.clearAllFilters() }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PackagedTeaStoreScreen(
        catalog: List<PackagedTeaCatalog>,
        filterState: PackagedFilterState,
        availableFilters: AvailablePackagedFilters,
        onSearchQueryChange: (String) -> Unit,
        onPriceRangeChange: (Int?, Int?) -> Unit,
        onFilterToggle: (PackagedFilterType, String) -> Unit,
        onClearFilters: () -> Unit
    ) {
        var showFilterDialog by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize()) {
            // --- Search & Filter Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Custom Sleek Search Bar ---
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (filterState.searchQuery.isEmpty()) {
                            Text(
                                text = "Search packaged tea...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        BasicTextField(
                            value = filterState.searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // --- Filter Icon ---
                IconButton(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Open Filters",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // --- Active Filter Count Indicator ---
            val activeFilterCount = listOf(
                filterState.selectedArticleTypes,
                filterState.selectedCategories,
                filterState.selectedBrands,
                filterState.selectedWeights
            ).sumOf { if (it.isNotEmpty()) 1 else 0 } +
                    (if (filterState.minPrice != null || filterState.maxPrice != null) 1 else 0)

            if (activeFilterCount > 0) {
                Text(
                    text = "Filters Active: $activeFilterCount",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // --- The Tea List ---
            // Assuming you have a catalog component adapted for PackagedTea
            TeaCatalogScreen(store = catalog)
        }

        // --- The Filter Popup ---
        if (showFilterDialog) {
            PackagedFilterDialog(
                filterState = filterState,
                availableFilters = availableFilters,
                onDismiss = { showFilterDialog = false },
                onPriceRangeChange = onPriceRangeChange,
                onFilterToggle = onFilterToggle,
                onClearFilters = onClearFilters
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PackagedFilterDialog(
        filterState: PackagedFilterState,
        availableFilters: AvailablePackagedFilters,
        onDismiss: () -> Unit,
        onPriceRangeChange: (Int?, Int?) -> Unit,
        onFilterToggle: (PackagedFilterType, String) -> Unit,
        onClearFilters: () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filters", style = MaterialTheme.typography.titleLarge)
                        TextButton(onClick = onClearFilters) {
                            Text("Clear All")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Scrollable Filter Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // --- Price Range Slider (MRP per Article) ---
                        val minRs = (availableFilters.minPossiblePrice / 100f).coerceAtLeast(0f)
                        val maxRs = (availableFilters.maxPossiblePrice / 100f).coerceAtLeast(1f)

                        var sliderPosition by remember(filterState.minPrice, filterState.maxPrice) {
                            mutableStateOf(
                                (filterState.minPrice?.div(100f) ?: minRs) ..
                                        (filterState.maxPrice?.div(100f) ?: maxRs)
                            )
                        }

                        Text("Price Range (MRP ₹)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "₹${sliderPosition.start.roundToInt()} - ₹${sliderPosition.endInclusive.roundToInt()}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        RangeSlider(
                            value = sliderPosition,
                            onValueChange = { sliderPosition = it },
                            valueRange = minRs..maxRs,
                            onValueChangeFinished = {
                                val newMinPaise = (sliderPosition.start * 100).roundToInt()
                                val newMaxPaise = (sliderPosition.endInclusive * 100).roundToInt()
                                onPriceRangeChange(newMinPaise, newMaxPaise)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // --- Dynamic Filter Groups ---

                        // 1. Article Types (Enum requires a slight map to string for UI display)
                        FilterGroup(
                            title = "Packaging Type",
                            options = availableFilters.articleTypes.map { it.lowercase().replaceFirstChar { c -> c.uppercase() } }.toSet(),
                            selectedOptions = filterState.selectedArticleTypes.map { it.lowercase().replaceFirstChar { c -> c.uppercase() } }.toSet(),
                            onToggle = { selectedString ->
                                // Map string back to Enum name for the ViewModel
                                val enumName = ArticleType.entries.first { it.name.equals(selectedString, ignoreCase = true) }.name
                                onFilterToggle(PackagedFilterType.ARTICLE_TYPE, enumName)
                            }
                        )

                        // 2. Categories
                        FilterGroup(
                            title = "Category",
                            options = availableFilters.categories,
                            selectedOptions = filterState.selectedCategories,
                            onToggle = { onFilterToggle(PackagedFilterType.CATEGORY, it) }
                        )

                        // 3. Brands
                        FilterGroup(
                            title = "Brand",
                            options = availableFilters.brands,
                            selectedOptions = filterState.selectedBrands,
                            onToggle = { onFilterToggle(PackagedFilterType.BRAND, it) }
                        )

                        // 4. Weights (String formatted in ViewModel: "250 gram", "1 kg")
                        FilterGroup(
                            title = "Weight",
                            options = availableFilters.weights,
                            selectedOptions = filterState.selectedWeights,
                            onToggle = { onFilterToggle(PackagedFilterType.WEIGHT, it) }
                        )
                    }

                    // Footer (Apply Button)
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }
    }

    // Helper Composable for creating horizontal scrollable chips
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FilterGroup(
        title: String,
        options: Set<String>,
        selectedOptions: Set<String>,
        onToggle: (String) -> Unit
    ) {
        if (options.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options.toList()) { option ->
                    FilterChip(
                        selected = selectedOptions.contains(option),
                        onClick = { onToggle(option) },
                        label = { Text(option) }
                    )
                }
            }
        }
    }

    @Composable
    fun TeaCatalogScreen(store: List<PackagedTeaCatalog>) {

        LazyColumn {

            items(store) { catalog ->

                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        text = catalog.category,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(4.dp)
                    )

                    CatalogRow(catalog.items)

                }
            }
        }
    }

    @Composable
    fun CatalogRow(items: List<PackagedTea>) {

        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        // Wrap the frequently changing state reads in derivedStateOf
        val showLeftArrow by remember {
            derivedStateOf {
                listState.canScrollBackward
            }
        }

        val showRightArrow by remember {
            derivedStateOf {
                listState.canScrollForward
            }
        }

        Box {
            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(items) { tea ->
                    PackagedTeaCard(tea)
                }
            }

            if (showLeftArrow) {
                IconButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(
                                (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                ) {
                    Icon(Icons.Filled.ChevronLeft, tint = Color.White, contentDescription = "Scroll Left")
                }
            }

            if (showRightArrow) {
                IconButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(
                                listState.firstVisibleItemIndex + 1
                            )
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Filled.ChevronRight, tint = Color.White, contentDescription = "Scroll Right")
                }
            }
        }
    }

    @Composable
    fun PackagedTeaCard(packagedTea: PackagedTea) {
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val cardWidth = screenWidth * 0.88f

        Card(
            modifier = Modifier
                .width(cardWidth)
                .padding(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            onClick = {
                val intent = Intent(activity as Context, PackagedTeaDetailsActivity::class.java)
                intent.putExtra("packagedTeaDetails", packagedTea)
                startActivity(intent)
            }
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // Image Box
                Box(
                    modifier = Modifier.weight(0.35f).padding(8.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    AsyncImage(
                        model = packagedTea.featuredImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).height(120.dp)
                    )
                }

                // Details Column
                Column(
                    modifier = Modifier.weight(0.65f).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = packagedTea.brandName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = packagedTea.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // MRP Display with specific formatting
                    Text(
                        text = "MRP: ₹${formatPaiseToRupees(packagedTea.mrp)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Article Weight with Unit Conversion
                    val formattedUnit = formatWeightUnit(packagedTea.articleWeight.unit)
                    Text(
                        text = "Net Weight: ${packagedTea.articleWeight.quantity} $formattedUnit",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Conditional Corrugated Box Weight
                    if (packagedTea.corrugatedBoxWeight != null) {
                        val cbUnit = formatWeightUnit(packagedTea.corrugatedBoxWeight.unit)
                        Text(
                            text = "Box Weight: ${packagedTea.corrugatedBoxWeight.quantity} $cbUnit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    // Pouch Specifics
                    if (packagedTea.articleType == ArticleType.POUCH) {
                        Text(
                            text = "Lari: ${packagedTea.lariSize} | Bundle: ${packagedTea.bundleSize}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }

                    Text(
                        text = "Bag Size: ${packagedTea.bagSize} units",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sharedHeight = 40.dp

                val cartMap by CartManager.cart.collectAsState()

                val productCartItem =
                    cartMap["packaged_tea_${packagedTea.id}"] as? PackagedTeaCartItem

                val sampleCartItem =
                    cartMap["packaged_tea_sample_${packagedTea.id}"] as? PackagedTeaSampleCartItem

                // ----------------------------
                // SAMPLE BUTTON / STEPPER
                // ----------------------------
                if (sampleCartItem != null) {

                    Row(
                        modifier = Modifier
                            .weight(0.6f)
                            .height(sharedHeight)
                            .border(
                                BorderStroke(1.dp, colorPrimary),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {

                        IconButton(
                            onClick = {
                                CartManager.remove(sampleCartItem)
                            },
                            modifier = Modifier.weight(0.2f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${sampleCartItem.quantity} ${sampleCartItem.productDetails.articleType}",
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = {
                                CartManager.add(sampleCartItem)
                            },
                            modifier = Modifier.weight(0.2f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                } else {

                    Button(
                        onClick = {
                            CartManager.add(
                                PackagedTeaSampleCartItem(
                                    productId = packagedTea.id,
                                    quantity = 0, // handled internally
                                    productDetails = packagedTea
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = colorPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.6f)
                            .height(sharedHeight),
                        border = BorderStroke(1.dp, colorPrimary)
                    ) {
                        Text(text = "Ask for Sample")
                    }
                }

                // ----------------------------
                // PRODUCT BUTTON / STEPPER
                // ----------------------------
                if (productCartItem != null) {

                    Row(
                        modifier = Modifier
                            .weight(0.4f)
                            .height(sharedHeight)
                            .border(
                                BorderStroke(1.dp, colorPrimary),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {

                        IconButton(
                            onClick = {
                                CartManager.remove(productCartItem)
                            },
                            modifier = Modifier.weight(0.3f)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Remove")
                        }

                        Box(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${productCartItem.quantity}",
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(
                            onClick = {
                                CartManager.add(productCartItem)
                            },
                            modifier = Modifier.weight(0.3f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                } else {

                    Button(
                        onClick = {
                            CartManager.add(
                                PackagedTeaCartItem(
                                    productId = packagedTea.id,
                                    quantity = 0,
                                    productDetails = packagedTea
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(0.4f)
                            .height(sharedHeight)
                    ) {
                        Text(text = "ADD")
                    }
                }
            }
        }
    }

    fun formatWeightUnit(unit: String): String {
        return when (unit.lowercase()) {
            "g", "gm" -> "gram"
            "kg" -> "kg"
            else -> unit
        }
    }

    @Composable
    fun OriginBadge(
        origin: String,
        containerColor: Color = Color.White,
        contentColor: Color = Color.Black
    ){
        Surface(
            modifier = Modifier
                .height(24.dp), // Slightly shorter than the circle for visual balance
            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp, topStart = 8.dp, bottomStart = 8.dp),
            color = containerColor,
            contentColor = contentColor,
            border = BorderStroke(1.dp, colorPrimary)
        ) {
            Box(
                modifier = Modifier.padding(start = 6.dp, end = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = origin.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

}