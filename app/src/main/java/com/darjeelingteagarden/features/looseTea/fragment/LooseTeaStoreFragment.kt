package com.darjeelingteagarden.features.looseTea.fragment

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.style.TextDecoration
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
import com.darjeelingteagarden.features.cart.model.LooseTeaCartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaSampleCartItem
import com.darjeelingteagarden.features.looseTea.activity.LooseTeaDetailsActivity
import com.darjeelingteagarden.features.looseTea.model.LooseTea
import com.darjeelingteagarden.features.looseTea.model.LooseTeaCatalog
import com.darjeelingteagarden.features.looseTea.viewModel.AvailableFilters
import com.darjeelingteagarden.features.looseTea.viewModel.FilterState
import com.darjeelingteagarden.features.looseTea.viewModel.FilterType
import com.darjeelingteagarden.features.looseTea.viewModel.LooseTeaStoreViewModel
import com.darjeelingteagarden.util.formatPaiseToRupees
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class LooseTeaStoreFragment : Fragment() {

    val store: MutableList<LooseTeaCatalog> = mutableListOf()

    val colorPrimary = Color(0xFF00CCAA)

    private val viewModel: LooseTeaStoreViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LooseTeaStoreViewModel(MyRetrofitClient.looseTeaApi) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme { // Assuming you have a MaterialTheme defined
                    // Collect all states from the ViewModel
                    val catalog by viewModel.looseTeaCatalog.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    val filterState by viewModel.filterState.collectAsState()
                    val availableFilters by viewModel.availableFilters.collectAsState()

                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        // Pass everything to the main screen
                        LooseTeaStoreScreen(
                            catalog = catalog,
                            filterState = filterState,
                            availableFilters = availableFilters,
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onPricePerKgRangeChange = { min, max -> viewModel.updatePriceRangePerKg(min, max) },
                            onPricePerBagRangeChange = { min, max -> viewModel.updatePriceRangePerBag(min, max) },
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
    fun LooseTeaStoreScreen(
        catalog: List<LooseTeaCatalog>,
        filterState: FilterState,
        availableFilters: AvailableFilters,
        onSearchQueryChange: (String) -> Unit,
        onPricePerKgRangeChange: (Int?, Int?) -> Unit,
        onPricePerBagRangeChange: (Int?, Int?) -> Unit,
        onFilterToggle: (FilterType, String) -> Unit,
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
                        .height(46.dp) // You can safely make this as slim as you want now
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp), // Only applying horizontal padding
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Box overlays the Placeholder Text and the actual BasicTextField
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (filterState.searchQuery.isEmpty()) {
                            Text(
                                text = "Search loose tea...",
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

            // --- Active Filter Count Indicator (Optional but good UX) ---
            val activeFilterCount = listOf(
                filterState.selectedGrades, filterState.selectedCategories,
                filterState.selectedStandards, filterState.selectedBrands,
                filterState.selectedOrigins, filterState.selectedAppearances
            ).sumOf { if (it.isNotEmpty()) 1 else 0 } +
                    (if (filterState.minPricePerKg != null || filterState.maxPricePerKg != null) 1 else 0) +
                    (if (filterState.minPricePerBag != null || filterState.maxPricePerBag != null) 1 else 0)

            if (activeFilterCount > 0) {
                Text(
                    text = "Filters Active: $activeFilterCount",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // --- The Tea List ---
            // (Assuming you have this built, passing the catalog to it)
            TeaCatalogScreen(store = catalog, modifier = Modifier.weight(1f))
        }

        // --- The Filter Popup ---
        if (showFilterDialog) {
            FilterDialog(
                filterState = filterState,
                availableFilters = availableFilters,
                onDismiss = { showFilterDialog = false },
                onPricePerKgRangeChange = onPricePerKgRangeChange,
                onPricePerBagRangeChange = onPricePerBagRangeChange,
                onFilterToggle = onFilterToggle,
                onClearFilters = onClearFilters
            )
        }
    }

    @Composable
    fun TeaCatalogScreen(store: List<LooseTeaCatalog>, modifier: Modifier) {

        LazyColumn {

            items(store) { catalog ->

                Column(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        text = catalog.category,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(4.dp)
                    )

                    CatalogRow(catalog.items)

                }
            }
        }
    }

    @Composable
    fun CatalogRow(items: List<LooseTea>) {

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
                    LooseTeaCard(tea)
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
    fun LooseTeaCard(looseTea: LooseTea) {

        val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        val primaryTextColor = MaterialTheme.colorScheme.onSurface

        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val cardWidth = screenWidth * 0.88f

        Card(
            modifier = Modifier
                .width(cardWidth)
                .padding(6.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardBackgroundColor
            ),
            onClick = {
                val intent = Intent(activity as Context, LooseTeaDetailsActivity::class.java)
                intent.putExtra("looseTeaDetails", looseTea)
                startActivity(intent)
            }
//            border = BorderStroke(1.dp, Color.Gray),
        ) {

            Row(modifier = Modifier.fillMaxWidth()) {

                // 1. Wrap the image in a Box to allow stacking
                Box(
                    modifier = Modifier
                        .weight(0.35f)
                        .padding(4.dp),
                    contentAlignment = Alignment.TopCenter // Default alignment for children
                ) {
                    AsyncImage(
                        model = looseTea.featuredImage,
                        contentDescription = "product image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth() // Let it fill the Box width
                            .clip(RoundedCornerShape(8.dp))
                            .heightIn(min = 100.dp, max = 120.dp)
                    )

                    // 2. Add your RatingBadge on top
                    // Using a negative offset helps it "pop" slightly outside or sit perfectly on the edge
                    OriginBadge(
                        origin = looseTea.origin
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(4.dp)
                ) {
                    Text(
                        text = looseTea.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = primaryTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (looseTea.pricePerKg.sellingPrice != looseTea.pricePerKg.originalPrice){
                            Text(
                                text = "₹${formatPaiseToRupees(looseTea.pricePerKg.originalPrice)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = secondaryTextColor,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = "₹${formatPaiseToRupees(looseTea.pricePerKg.sellingPrice)} /kg",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        if (looseTea.pricePerBag.sellingPrice != looseTea.pricePerBag.originalPrice){
                            Text(
                                text = "₹${formatPaiseToRupees(looseTea.pricePerBag.originalPrice)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = secondaryTextColor,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        Text(
                            text = "₹${formatPaiseToRupees(looseTea.pricePerBag.sellingPrice)} /bag",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Grade: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = looseTea.grade,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Bag Size: ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = looseTea.bagWeight.quantity.toString() + " " + looseTea.bagWeight.unit,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                if (looseTea.lotNumber != null && looseTea.lotNumber != ""){
                    Text(
                        text = "Lot No.: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = looseTea.lotNumber,
                        style = MaterialTheme.typography.titleSmall
                    )
                }

                if (looseTea.lotSize != null && looseTea.lotSize.quantity != 0) {
                    Text(
                        text = " | ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Lot Size: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = looseTea.lotSize.quantity.toString() + " " + convertUnitString(looseTea.lotSize.unit),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

//            Row(modifier = Modifier
//                .fillMaxWidth()
//                .padding(4.dp)) {
//                RatingBadge(looseTea.qualityRating.infusion.toString(), "Infusion")
//                RatingBadge(looseTea.qualityRating.color.toString(), "Color")
//                RatingBadge(looseTea.qualityRating.strongness.toString(), "Strongness")
//
//            }
//
//            Row(modifier = Modifier
//                .fillMaxWidth()
//                .padding(4.dp)) {
//                RatingBadge(looseTea.qualityRating.thickness.toString(), "Thickness")
//                RatingBadge(looseTea.qualityRating.aroma.toString(), "Aroma")
//                RatingBadge(looseTea.qualityRating.briskness.toString(), "Briskness")
//            }

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
                    cartMap["loose_tea_${looseTea.id}"] as? LooseTeaCartItem

                val sampleCartItem =
                    cartMap["loose_tea_sample_${looseTea.id}"] as? LooseTeaSampleCartItem

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
                                text = "${sampleCartItem.quantity * 10} gram",
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
                                LooseTeaSampleCartItem(
                                    productId = looseTea.id,
                                    quantity = 0, // handled internally
                                    productDetails = looseTea
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
                                LooseTeaCartItem(
                                    productId = looseTea.id,
                                    quantity = 0,
                                    productDetails = looseTea
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

    private fun convertUnitString(str: String): String{
        return if (str == "g"){
            "gram"
        } else {
            str
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FilterDialog(
        filterState: FilterState,
        availableFilters: AvailableFilters,
        onDismiss: () -> Unit,
        onPricePerKgRangeChange: (Int?, Int?) -> Unit,
        onPricePerBagRangeChange: (Int?, Int?) -> Unit,
        onFilterToggle: (FilterType, String) -> Unit,
        onClearFilters: () -> Unit
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Allows it to take more width
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f) // Don't take full screen, acts like a large popup
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
                        // --- Price Range Slider (per kg) ---
                        // Convert paise to Rupees for UI
                        val minRsPerKg = (availableFilters.minPossiblePricePerKg / 100f).coerceAtLeast(0f)
                        val maxRsPerKg = (availableFilters.maxPossiblePricePerKg / 100f).coerceAtLeast(1f)

                        var sliderPositionPerKg by remember(filterState.minPricePerKg, filterState.maxPricePerKg) {
                            mutableStateOf(
                                (filterState.minPricePerKg?.div(100f) ?: minRsPerKg) ..
                                        (filterState.maxPricePerKg?.div(100f) ?: maxRsPerKg)
                            )
                        }

                        Text("Price Range per kg (₹)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "₹${sliderPositionPerKg.start.roundToInt()} - ₹${sliderPositionPerKg.endInclusive.roundToInt()}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        RangeSlider(
                            value = sliderPositionPerKg,
                            onValueChange = { sliderPositionPerKg = it },
                            valueRange = minRsPerKg..maxRsPerKg,
                            onValueChangeFinished = {
                                // Convert Rupees back to paise for ViewModel
                                val newMinPaise = (sliderPositionPerKg.start * 100).roundToInt()
                                val newMaxPaise = (sliderPositionPerKg.endInclusive * 100).roundToInt()
                                onPricePerKgRangeChange(newMinPaise, newMaxPaise)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // --- Price Range Slider (per bag) ---

                        val minRsPerBag = (availableFilters.minPossiblePricePerBag / 100f).coerceAtLeast(0f)
                        val maxRsPerBag = (availableFilters.maxPossiblePricePerBag / 100f).coerceAtLeast(1f)

                        var sliderPositionPerBag by remember(filterState.minPricePerBag, filterState.maxPricePerBag) {
                            mutableStateOf(
                                (filterState.minPricePerBag?.div(100f) ?: minRsPerBag) ..
                                        (filterState.maxPricePerBag?.div(100f) ?: maxRsPerBag)
                            )
                        }

                        Text("Price Range per bag (₹)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "₹${sliderPositionPerBag.start.roundToInt()} - ₹${sliderPositionPerBag.endInclusive.roundToInt()}",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        RangeSlider(
                            value = sliderPositionPerBag,
                            onValueChange = { sliderPositionPerBag = it },
                            valueRange = minRsPerBag..maxRsPerBag,
                            onValueChangeFinished = {
                                // Convert Rupees back to paise for ViewModel
                                val newMinPaise = (sliderPositionPerBag.start * 100).roundToInt()
                                val newMaxPaise = (sliderPositionPerBag.endInclusive * 100).roundToInt()
                                onPricePerBagRangeChange(newMinPaise, newMaxPaise)
                            },
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // --- Dynamic Filter Groups ---
                        FilterGroup(
                            title = "Category",
                            options = availableFilters.categories,
                            selectedOptions = filterState.selectedCategories,
                            onToggle = { onFilterToggle(FilterType.CATEGORY, it) }
                        )

                        FilterGroup(
                            title = "Grade",
                            options = availableFilters.grades,
                            selectedOptions = filterState.selectedGrades,
                            onToggle = { onFilterToggle(FilterType.GRADE, it) }
                        )

                        FilterGroup(
                            title = "Brand",
                            options = availableFilters.brands,
                            selectedOptions = filterState.selectedBrands,
                            onToggle = { onFilterToggle(FilterType.BRAND, it) }
                        )

                        FilterGroup(
                            title = "Origin",
                            options = availableFilters.origins,
                            selectedOptions = filterState.selectedOrigins,
                            onToggle = { onFilterToggle(FilterType.ORIGIN, it) }
                        )

                        FilterGroup(
                            title = "Standard",
                            options = availableFilters.standards,
                            selectedOptions = filterState.selectedStandards,
                            onToggle = { onFilterToggle(FilterType.STANDARD, it) }
                        )

                        FilterGroup(
                            title = "Appearance",
                            options = availableFilters.appearances,
                            selectedOptions = filterState.selectedAppearances,
                            onToggle = { onFilterToggle(FilterType.APPEARANCE, it) }
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

}