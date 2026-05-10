package com.darjeelingteagarden.features.packagedTea.activity

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.darjeelingteagarden.common.MyRetrofitClient
import com.darjeelingteagarden.components.AsyncImageCarousel
import com.darjeelingteagarden.features.cart.CartManager
import com.darjeelingteagarden.features.cart.fragment.CartActivity
import com.darjeelingteagarden.features.cart.model.PackagedTeaCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaSampleCartItem
import com.darjeelingteagarden.features.packagedTea.activity.ui.theme.DarjeelingTeaGardenTheme
import com.darjeelingteagarden.features.packagedTea.model.PackagedTea
import com.darjeelingteagarden.util.formatPaiseToRupees
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val PrimaryTeal = Color(0xFF00CCAA)

class PackagedTeaDetailsActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packagedTea = intent.getParcelableExtra("packagedTeaDetails", PackagedTea::class.java)
        val packagedTeaId = intent.getStringExtra("packagedTeaId").takeIf { it != "" }

        Log.i("packagedTeaId", packagedTeaId.toString())
        Log.i("packagedTea", packagedTea.toString())

        setContent {
            DarjeelingTeaGardenTheme {
                PackagedTeaDetailsStateContainer(
                    initialTea = packagedTea,
                    teaId = packagedTeaId,
                    onBackClick = { finish() },
                    onCartClick = {
                        startActivity(Intent(this@PackagedTeaDetailsActivity, CartActivity::class.java))
                    }
                )
            }
        }
    }
}

@Composable
fun PackagedTeaDetailsStateContainer(
    initialTea: PackagedTea?,
    teaId: String?,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    // UI States matching your example structure
    var packagedTeaState by remember { mutableStateOf<PackagedTea?>(initialTea) }
    var isLoading by remember { mutableStateOf(initialTea == null && teaId != null) }
    var isError by remember { mutableStateOf(initialTea == null && teaId == null) }

    // Fetch from API if we have an ID but no Parcelable data
    LaunchedEffect(initialTea, teaId) {
        if (initialTea == null && teaId != null) {
            isLoading = true
            try {
                val fetchedTea = withContext(Dispatchers.IO) {
                    MyRetrofitClient.packagedTeaApi.getPackagedTeaById(teaId)
                }

                Log.i("fetchedTea", fetchedTea.toString())

                if (fetchedTea != null) {
                    // Assuming your response wrapper has a .data property like your example
                    packagedTeaState = fetchedTea.data
                } else {
                    isError = true
                }
            } catch (e: Exception) {
                Log.e("PackagedTeaDetails", "Network fetch failed", e)
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
                CircularProgressIndicator(color = PrimaryTeal)
            }
        }
        isError || packagedTeaState == null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error loading data.", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Go Back")
                    }
                }
            }
        }
        packagedTeaState != null -> {
            PackagedTeaDetailsScreen(
                packagedTea = packagedTeaState!!,
                onBackClick = onBackClick,
                onCartClick = onCartClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagedTeaDetailsScreen(
    packagedTea: PackagedTea,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val cartCount by CartManager.cartCount.observeAsState(0)
    val cartMap by CartManager.cart.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Packaged Tea Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    CartIconButton(cartCount = cartCount, onClick = onCartClick)
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = packagedTea.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            ProductImageCarousel(
                featuredImage = packagedTea.featuredImage,
                otherImages = packagedTea.images
            )

            ProductDetailsSection(packagedTea)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            CartActionsSection(packagedTea, cartMap)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            ProductDescriptionSection(description = packagedTea.description)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartIconButton(cartCount: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
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

@Composable
fun ProductImageCarousel(featuredImage: String, otherImages: List<String>) {
    val images = remember(featuredImage, otherImages) {
        listOf(featuredImage) + otherImages
    }

    Box(
        modifier = Modifier
            .height(200.dp)
            .padding(bottom = 10.dp)
    ) {
        AsyncImageCarousel(images)
    }
}

@Composable
fun ProductDetailsSection(packagedTea: PackagedTea) {
    InfoRow("MRP", "₹${formatPaiseToRupees(packagedTea.mrp)}")
    InfoRow("Sample Price", "₹${formatPaiseToRupees(packagedTea.samplePrice)}")
    InfoRow("Brand", packagedTea.brandName)
    InfoRow("Category", packagedTea.category)
    InfoRow("Type", packagedTea.articleType.name)
    InfoRow("Weight", "${packagedTea.articleWeight.quantity} ${packagedTea.articleWeight.unit}")
    InfoRow("Bag Size", packagedTea.bagSize.toString())

    packagedTea.lariSize?.let { InfoRow("Lari Size", it.toString()) }
    packagedTea.bundleSize?.let { InfoRow("Bundle Size", it.toString()) }
    packagedTea.corrugatedBoxWeight?.let {
        InfoRow("Box Weight", "${it.quantity} ${it.unit}")
    }
}

@Composable
fun CartActionsSection(packagedTea: PackagedTea, cartMap: Map<String, Any>) {
    val productCartItem = cartMap["packaged_tea_${packagedTea.id}"] as? PackagedTeaCartItem
    val sampleCartItem = cartMap["packaged_tea_sample_${packagedTea.id}"] as? PackagedTeaSampleCartItem
    val sharedHeight = 40.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sample Action
        Box(modifier = Modifier.weight(0.6f).height(sharedHeight)) {
            if (sampleCartItem != null) {
                QuantityStepper(
                    quantityText = "${sampleCartItem.quantity} ${sampleCartItem.productDetails.articleType}",
                    onRemove = { CartManager.remove(sampleCartItem) },
                    onAdd = { CartManager.add(sampleCartItem) }
                )
            } else {
                OutlinedButton(
                    onClick = {
                        CartManager.add(
                            PackagedTeaSampleCartItem(
                                productId = packagedTea.id,
                                quantity = 0,
                                productDetails = packagedTea
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, PrimaryTeal),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryTeal)
                ) {
                    Text("Ask for Sample")
                }
            }
        }

        // Product Action
        Box(modifier = Modifier.weight(0.4f).height(sharedHeight)) {
            if (productCartItem != null) {
                QuantityStepper(
                    quantityText = productCartItem.quantity.toString(),
                    onRemove = { CartManager.remove(productCartItem) },
                    onAdd = { CartManager.add(productCartItem) }
                )
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
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text("ADD")
                }
            }
        }
    }
}

@Composable
fun QuantityStepper(
    quantityText: String,
    onRemove: () -> Unit,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .border(BorderStroke(1.dp, PrimaryTeal), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onRemove, modifier = Modifier.weight(0.3f)) {
            Icon(Icons.Default.Remove, contentDescription = "Remove", tint = PrimaryTeal)
        }
        Text(
            text = quantityText,
            modifier = Modifier.weight(0.4f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(onClick = onAdd, modifier = Modifier.weight(0.3f)) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = PrimaryTeal)
        }
    }
}

@Composable
fun ProductDescriptionSection(description: String) {
    Text(
        text = "Product Description",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
fun ErrorScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error loading data")
    }
}
