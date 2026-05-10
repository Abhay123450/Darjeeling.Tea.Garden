package com.darjeelingteagarden.features.looseTea.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.darjeelingteagarden.common.MyRetrofitClient
import com.darjeelingteagarden.components.AsyncImageCarousel
import com.darjeelingteagarden.components.RatingBadge
import com.darjeelingteagarden.features.cart.CartManager
import com.darjeelingteagarden.features.cart.fragment.CartActivity
import com.darjeelingteagarden.features.cart.fragment.ui.theme.DarjeelingTeaGardenTheme
import com.darjeelingteagarden.features.cart.model.LooseTeaCartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaSampleCartItem
import com.darjeelingteagarden.features.looseTea.model.LooseTea
import com.darjeelingteagarden.features.looseTea.model.QualityRating
import com.darjeelingteagarden.util.formatPaiseToRupees
import com.darjeelingteagarden.util.normalizeAndCapitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val ColorPrimary = Color(0xFF00CCAA)

class LooseTeaDetailsActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val looseTea = intent.getParcelableExtra("looseTeaDetails", LooseTea::class.java)

        // Assuming ID is an Int. Change to getStringExtra if your ID is a String.
        val looseTeaId = intent.getStringExtra("looseTeaId").takeIf { it != "" }

        Log.i("looseTeaId", looseTeaId.toString())
        Log.i("looseTea", looseTea.toString())

        setContent {
            DarjeelingTeaGardenTheme {
                LooseTeaDetailsStateContainer(
                    initialTea = looseTea,
                    teaId = looseTeaId,
                    onBackClick = { finish() },
                    onCartClick = {
                        startActivity(Intent(this@LooseTeaDetailsActivity, CartActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun LooseTeaDetailsStateContainer(
    initialTea: LooseTea?,
    teaId: String?, // Adjust type to String if your ID is a String
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    // UI States
    var looseTeaState by remember { mutableStateOf<LooseTea?>(initialTea) }
    var isLoading by remember { mutableStateOf(initialTea == null && teaId != null) }
    var isError by remember { mutableStateOf(initialTea == null && teaId == null) }

    // Fetch from API if we have an ID but no Parcelable data
    LaunchedEffect(initialTea, teaId) {
        if (initialTea == null && teaId != null) {
            isLoading = true
            try {
                // Assuming getLooseTeaById is a suspend function
                val fetchedTea = withContext(Dispatchers.IO) {
                    MyRetrofitClient.looseTeaApi.getLooseTeaById(teaId)
                }

                Log.i("fetchedTea", fetchedTea.toString())

                // If your API returns a Response<LooseTea>, use fetchedTea.body()
                // If it returns the object directly, just use fetchedTea
                if (fetchedTea != null) {
                    looseTeaState = fetchedTea.data
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                Log.e("LooseTeaDetails", "Network fetch failed", e)
                isError = true
            } finally {
                isLoading = false
            }
        }
    }

    // Render based on state
    when {
        isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ColorPrimary)
            }
        }
        isError || looseTeaState == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error loading data.", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrimary),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
        looseTeaState != null -> {
            // Render the screen we broke down previously
            LooseTeaDetailsScreen(
                looseTea = looseTeaState!!,
                onBackClick = onBackClick,
                onCartClick = onCartClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LooseTeaDetailsScreen(
    looseTea: LooseTea,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Scaffold(
        topBar = { LooseTeaTopAppBar(onBackClick, onCartClick) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            LooseTeaHeaderAndImages(looseTea)
            LooseTeaPricing(looseTea)
            LooseTeaSpecs(looseTea)
            LooseTeaRatings(looseTea.qualityRating)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            LooseTeaCartActions(looseTea)
            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
            LooseTeaDescription(looseTea.description)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LooseTeaTopAppBar(onBackClick: () -> Unit, onCartClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "Loose Tea Details",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        actions = {
            val cartCount by CartManager.cartCount.observeAsState(0)
            IconButton(onClick = onCartClick) {
                BadgedBox(
                    badge = {
                        if (cartCount > 0) {
                            Badge { Text(cartCount.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart"
                    )
                }
            }
        }
    )
}

@Composable
fun LooseTeaHeaderAndImages(looseTea: LooseTea) {
    Text(
        text = looseTea.name,
        modifier = Modifier.padding(bottom = 12.dp),
        style = MaterialTheme.typography.titleLarge
    )

    val images = remember(looseTea) {
        mutableListOf(looseTea.featuredImage).apply { addAll(looseTea.images) }
    }

    Box(modifier = Modifier.height(200.dp).padding(bottom = 10.dp)) {
        AsyncImageCarousel(images)
    }
}

@Composable
fun LooseTeaPricing(looseTea: LooseTea) {
    PriceRow(
        originalPrice = looseTea.pricePerKg.originalPrice,
        sellingPrice = looseTea.pricePerKg.sellingPrice,
        suffix = " /kg",
        style = MaterialTheme.typography.bodyLarge
    )
    PriceRow(
        originalPrice = looseTea.pricePerBag.originalPrice,
        sellingPrice = looseTea.pricePerBag.sellingPrice,
        suffix = " /bag",
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun PriceRow(
    originalPrice: Int,
    sellingPrice: Int,
    suffix: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (sellingPrice != originalPrice) {
            Text(
                text = "₹${formatPaiseToRupees(originalPrice)}",
                style = style,
                color = Color.Gray,
                textDecoration = TextDecoration.LineThrough,
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Text(
            text = "₹${formatPaiseToRupees(sellingPrice)}$suffix",
            style = style
        )
    }
}

@Composable
fun LooseTeaSpecs(looseTea: LooseTea) {
    InfoRow("Sample Price", "₹${formatPaiseToRupees(looseTea.samplePrice)}/${looseTea.sampleSize.quantity}${convertUnitString(looseTea.sampleSize.unit)}")
    InfoRow("Grade", looseTea.grade)
    InfoRow("Category", normalizeAndCapitalize(looseTea.category))
    InfoRow("Brand", looseTea.brand)
    InfoRow("Bag Weight", "${looseTea.bagWeight.quantity} ${looseTea.bagWeight.unit}")

    looseTea.lotNumber?.let { InfoRow("Lot Number", it) }
    looseTea.lotSize?.let { InfoRow("Lot Size", "${it.quantity} ${it.unit}") }

    InfoRow("Origin", normalizeAndCapitalize(looseTea.origin))
    InfoRow("Appearance", normalizeAndCapitalize(looseTea.appearance))
    InfoRow("Standard", normalizeAndCapitalize(looseTea.standard))
}

@Composable
fun LooseTeaRatings(rating: QualityRating) {
    Text(
        text = "Tea Rating",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp)
    )

    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        RatingBadge(rating.infusion.toString(), "Infusion")
        RatingBadge(rating.color.toString(), "Color")
        RatingBadge(rating.strongness.toString(), "Strongness")
    }

    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        RatingBadge(rating.thickness.toString(), "Thickness")
        RatingBadge(rating.aroma.toString(), "Aroma")
        RatingBadge(rating.briskness.toString(), "Briskness")
    }
}

@Composable
fun LooseTeaCartActions(looseTea: LooseTea) {
    val cartMap by CartManager.cart.collectAsState()
    val productCartItem = cartMap["loose_tea_${looseTea.id}"] as? LooseTeaCartItem
    val sampleCartItem = cartMap["loose_tea_sample_${looseTea.id}"] as? LooseTeaSampleCartItem

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val sharedHeight = 40.dp

        // Sample Actions
        Box(modifier = Modifier.weight(0.6f).height(sharedHeight)) {
            if (sampleCartItem != null) {
                StepperControl(
                    countText = "${sampleCartItem.quantity * 10} gram",
                    onRemove = { CartManager.remove(sampleCartItem) },
                    onAdd = { CartManager.add(sampleCartItem) }
                )
            } else {
                OutlinedButton(
                    onClick = {
                        CartManager.add(
                            LooseTeaSampleCartItem(looseTea.id, 0, looseTea)
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ColorPrimary),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = ColorPrimary
                    )
                ) {
                    Text("Ask for Sample")
                }
            }
        }

        // Product Actions
        Box(modifier = Modifier.weight(0.4f).height(sharedHeight)) {
            if (productCartItem != null) {
                StepperControl(
                    countText = "${productCartItem.quantity}",
                    onRemove = { CartManager.remove(productCartItem) },
                    onAdd = { CartManager.add(productCartItem) }
                )
            } else {
                Button(
                    onClick = {
                        CartManager.add(
                            LooseTeaCartItem(looseTea.id, 0, looseTea)
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("ADD")
                }
            }
        }
    }
}

@Composable
fun StepperControl(
    countText: String,
    onRemove: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .border(BorderStroke(1.dp, ColorPrimary), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRemove, modifier = Modifier.weight(0.3f)) {
            Icon(Icons.Default.Remove, contentDescription = "Remove")
        }
        Text(
            text = countText,
            modifier = Modifier.weight(0.4f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onAdd, modifier = Modifier.weight(0.3f)) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
fun LooseTeaDescription(description: String) {
    Text(
        text = "Product Description",
        style = MaterialTheme.typography.titleMedium
    )
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

fun convertUnitString(str: String): String {
    return if (str == "g") "gram" else str
}