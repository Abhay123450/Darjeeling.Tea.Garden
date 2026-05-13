package com.darjeelingteagarden.features.cart.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialogDefaults.titleContentColor
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.BundleCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import coil3.compose.AsyncImage
import com.darjeelingteagarden.activity.PayuPaymentActivity
import com.darjeelingteagarden.common.MyRetrofitClient
import com.darjeelingteagarden.features.cart.CartManager
import com.darjeelingteagarden.features.cart.fragment.ui.theme.DarjeelingTeaGardenTheme
import com.darjeelingteagarden.features.cart.model.CartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaCartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaSampleCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaSampleCartItem
import com.darjeelingteagarden.features.cart.model.toDto
import com.darjeelingteagarden.features.looseTea.activity.LooseTeaDetailsActivity
import com.darjeelingteagarden.features.order.api.CreateOrderRequest
import com.darjeelingteagarden.features.packagedTea.activity.PackagedTeaDetailsActivity
import com.darjeelingteagarden.fragment.AddressBottomSheet
import com.darjeelingteagarden.model.Address
import com.darjeelingteagarden.util.formatPaiseToRupees
import kotlinx.coroutines.launch

class CartActivity : FragmentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DarjeelingTeaGardenTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Cart",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            navigationIcon = {
                                IconButton (onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    CartScreen(
                        onContinue = {
                            AddressBottomSheet().show(supportFragmentManager, "AddressBottomSheet")
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        supportFragmentManager.setFragmentResultListener(
            "address_result",
            this
        ) { _, bundle ->
            // BundleCompat handles the API 33 check automatically
            val address = BundleCompat.getParcelable(bundle, "address", Address::class.java)
            Toast.makeText(this, "Selected address: $address", Toast.LENGTH_SHORT).show()
            lifecycleScope.launch {
                Toast.makeText(this@CartActivity, "Creating order...", Toast.LENGTH_SHORT).show()
                Log.i("address is :::: ", address.toString())
                createOrder(address)
            }
        }

    }

    @Composable
    fun CartScreen(
        onContinue: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val cartMap by CartManager.cart.collectAsState()
        val items = cartMap.values.toList()

        val totalPrice = remember(items) { items.sumOf { it.totalPrice() } }

        Box(modifier = modifier.fillMaxSize()) {

            if (items.isEmpty()) {
                EmptyCart()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp), // space for bottom bar
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.uniqueCartId }) { item ->
                        CartItemCard(item = item)
                    }
                }
            }

            BottomBar(
                totalPrice = totalPrice,
                onContinue = onContinue,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    @Composable
    fun CartItemCard(item: CartItem) {

        val isSample = item is LooseTeaSampleCartItem || item is PackagedTeaSampleCartItem

        val (title, imageUrl, unitPrice) = when (item) {

            is LooseTeaCartItem -> Triple(
                item.productDetails.name,
                item.productDetails.featuredImage,
                item.productDetails.pricePerBag.sellingPrice
            )

            is LooseTeaSampleCartItem -> Triple(
                item.productDetails.name,
                item.productDetails.featuredImage,
                item.productDetails.samplePrice
            )

            is PackagedTeaCartItem -> Triple(
                item.productDetails.name,
                item.productDetails.featuredImage,
                item.productDetails.mrp
            )

            is PackagedTeaSampleCartItem -> Triple(
                item.productDetails.name,
                item.productDetails.featuredImage,
                item.productDetails.samplePrice
            )
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp)) {

                // LEFT COLUMN: Image + Badges
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable{
                                when (item) {
                                    is LooseTeaCartItem -> {
                                        val intent = Intent(
                                            this@CartActivity,
                                            LooseTeaDetailsActivity::class.java
                                        )
                                        intent.putExtra("looseTeaId", item.productDetails.id)
                                        startActivity(intent)
                                    }
                                    is PackagedTeaCartItem -> {
                                        val intent = Intent(
                                            this@CartActivity,
                                            PackagedTeaDetailsActivity::class.java
                                        )
                                        intent.putExtra("packagedTeaId", item.productDetails.id)
                                        startActivity(intent)
                                    }
                                    is LooseTeaSampleCartItem -> {
                                        val intent = Intent(
                                            this@CartActivity,
                                            LooseTeaDetailsActivity::class.java
                                        )
                                        intent.putExtra("looseTeaId", item.productDetails.id)
                                        startActivity(intent)
                                    }
                                    is PackagedTeaSampleCartItem -> {
                                        val intent = Intent(
                                            this@CartActivity,
                                            PackagedTeaDetailsActivity::class.java
                                        )
                                        intent.putExtra("packagedTeaId", item.productDetails.id)
                                        startActivity(intent)
                                    }
                                }
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // BADGES moved here
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Badge(
                            text = if (item is LooseTeaCartItem || item is LooseTeaSampleCartItem) "Loose Tea" else "Packaged Tea",
                            color = if (item is LooseTeaCartItem || item is LooseTeaSampleCartItem) Color(0xFF00CCAA) else Color(0xFF4CAF50)
                        )
                        if (isSample) {
                            Badge(text = "SAMPLE", color = Color(0xFFFF9800))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // RIGHT COLUMN: Title, Price, and Stepper Row
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.SemiBold)

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = formatPaiseToRupees(unitPrice),
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(12.dp))

                    // STEPPER + TOTAL PRICE ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuantityStepper(item)

                        Text(
                            text = formatPaiseToRupees(item.totalPrice()),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun QuantityStepper(item: CartItem) {

        Row(
            modifier = Modifier
                .height(32.dp)
                .border(1.dp, Color.Gray, RoundedCornerShape(6.dp)),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = { CartManager.remove(item) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }

            Text(
                text = item.quantity.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
                onClick = { CartManager.add(item) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }

    @Composable
    fun BottomBar(
        totalPrice: Int,
        onContinue: () -> Unit,
        modifier: Modifier = Modifier
    ) {

        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(modifier = Modifier.weight(1f)) {
                    Text("Total", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = formatPaiseToRupees(totalPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue to Payment")
                }
            }
        }
    }

    @Composable
    fun Badge(text: String, color: Color = Color(0xFF4CAF50)) {
        Box(
            modifier = Modifier
                .background(color.copy(alpha = 0.10f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }

    @Composable
    fun EmptyCart() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Your cart is empty")
        }
    }

    fun CartItem.totalPrice(): Int {
        return when (this) {

            is LooseTeaCartItem ->
                quantity * productDetails.pricePerBag.sellingPrice

            is LooseTeaSampleCartItem ->
                quantity * productDetails.samplePrice

            is PackagedTeaCartItem ->
                quantity * productDetails.mrp * productDetails.bagSize

            is PackagedTeaSampleCartItem ->
                quantity * productDetails.samplePrice
        }
    }

    private suspend fun createOrder(address: Address?){
        if (address == null){
            Toast.makeText(this, "Please select an address", Toast.LENGTH_SHORT).show()
            return
        }
        val cartList = CartManager.cart.value.values.toList()
        Log.i("cart list is :::: ", cartList.toString())
        val cartListDto = cartList.map { it.toDto() }

        val createOrderReq = CreateOrderRequest(
            cartListDto,
            address
        )
        Log.i("create order request", createOrderReq.toString())
        try {
            val response = MyRetrofitClient.orderApi.createOrder(createOrderReq)
            Log.i("create order response >>>", response.toString())
            if (response.success){
                val orderDetails = response.data
                orderDetails.address.addressId = ""
                orderDetails.address.name = ""
                orderDetails.address.country = "India"
                Toast.makeText(this, "Order created successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PayuPaymentActivity::class.java)
                intent.putExtra("orderDetails", response)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Order creation failed", Toast.LENGTH_SHORT).show()
            }
        }
        catch (e: Exception){
            Log.e("error creating order", e.toString())
        }

    }

}
