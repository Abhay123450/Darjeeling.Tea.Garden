package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.room.util.appendPlaceholders
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityNewsBinding
import com.darjeelingteagarden.databinding.ActivityProductDetailsBinding
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.ProductDetails
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.ResizeTransformation
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailsBinding

    private var productId: String? = null
    private var productName: String? = null
    private var price = 0
    private lateinit var cartItem: Cart
    private lateinit var productDetails: ProductDetails
    private lateinit var product: Product

    private var imageList = arrayListOf<SlideModel>()
    private var imageUriArray = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rlProgressProductDetails.visibility = View.VISIBLE

        binding.toolbarProductDetailsActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (intent != null){
            productId = intent.getStringExtra("productId")
        }
        Log.i("product id is : ", productId.toString())

        if (productId != null){
            getProductDetails(productId.toString())
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        binding.btnAddToCart.setOnClickListener {
//            binding.txtQuantity.text = "1"
//            binding.llChangeQuantity.visibility = View.VISIBLE
//            it.visibility = View.GONE
            CartDataSingleton.addProductToCart(product)
            isItemInCart()
//            AppDataSingleton.addCartItem(cartItem)
            Toast.makeText(this, "Product added to cart", Toast.LENGTH_SHORT).show()
        }

        binding.txtIncreaseQuantity.setOnClickListener {

            CartDataSingleton.increaseProductQuantity(product.productId)
            isItemInCart()
//            val quantity = CartDataSingleton.getProductCartItem(product.productId)?.quantity
//            if (quantity != null){
//                binding.txtQuantity.text = quantity.toString()
//            }
//            val itemIndex = AppDataSingleton.getIndexByProductId(productId.toString())
//            if (itemIndex != -1){
//                AppDataSingleton.increaseQuantity(itemIndex)
//                binding.txtQuantity.text = "1"
//            }
//            val itemFoundInCart = AppDataSingleton.getCartItemList.find { it.productId == productId }
//
//            if (itemFoundInCart != null){
//                binding.txtQuantity.text = itemFoundInCart.quantity.toString()
//            }

        }

        binding.txtDecreaseQuantity.setOnClickListener {
            CartDataSingleton.decreaseProductQuantity(product.productId)
            isItemInCart()
//            if (CartDataSingleton.productFoundInCart(product.productId)){
//                val quantity = CartDataSingleton.getProductCartItem(product.productId)?.quantity
//                if (quantity != null){
//                    binding.txtQuantity.text = quantity.toString()
//                }
//                else{
//                    binding.btnAddToCart.visibility = View.VISIBLE
//                    binding.llChangeQuantity.visibility = View.GONE
//                }
//            }
//            else{
//                binding.btnAddToCart.visibility = View.VISIBLE
//                binding.llChangeQuantity.visibility = View.GONE
//                Toast.makeText(this, "Product removed from cart", Toast.LENGTH_SHORT).show()
//            }
//            val itemIndex = AppDataSingleton.getIndexByProductId(productId.toString())
//            if (itemIndex != -1){
//                AppDataSingleton.decreaseQuantity(itemIndex)
//            }
//
//            val itemFoundInCart = AppDataSingleton.getCartItemList.find { it.productId == productId }
//            if (itemFoundInCart != null){
//                binding.txtQuantity.text = itemFoundInCart.quantity.toString()
//            }
//            else{
//                binding.btnAddToCart.visibility = View.VISIBLE
//                binding.llChangeQuantity.visibility = View.GONE
//                Toast.makeText(this, "Product removed from cart", Toast.LENGTH_SHORT).show()
//            }

        }

        binding.btnAddToSampleCart.setOnClickListener {
//            binding.txtSampleQuantity.text = "10 gram"
//            binding.llChangeSampleQuantity.visibility = View.VISIBLE
//            it.visibility = View.GONE
            CartDataSingleton.addSampleToCart(product)
            isItemInCart()
//            cartItem.isSample = true
//            SampleDataSingleton.addCartItem(cartItem)
            Toast.makeText(this, "Sample added to cart", Toast.LENGTH_SHORT).show()
        }

        binding.txtIncreaseSampleQuantity.setOnClickListener {

            CartDataSingleton.increaseSampleQuantity(product.productId)
            isItemInCart()
//            val sampleQuantity = CartDataSingleton.getSampleCartItem(product.productId)?.sampleQuantity
//            if (sampleQuantity != null){
//                binding.txtSampleQuantity.text = "${sampleQuantity * 10} gram"
//            }
//            OLD
////            val itemIndex = SampleDataSingleton.getIndexByProductId(productId.toString())
//            if (itemIndex != -1){
//                SampleDataSingleton.increaseQuantity(itemIndex)
////                binding.txtSampleQuantity.text = "1"
//            }
//            val itemFoundInCart = SampleDataSingleton.getCartItemList.find { it.productId == productId }
//
//            if (itemFoundInCart != null){
//                binding.txtQuantity.text = "${itemFoundInCart.quantity * productDetails.sampleQuantity} gram"
//            }
        }

        binding.txtDecreaseSampleQuantity.setOnClickListener {

            CartDataSingleton.decreaseSampleQuantity(product.productId)
            isItemInCart()
//            if (CartDataSingleton.sampleFoundInCart(product.productId)){
//                val sampleQuantity = CartDataSingleton.getSampleCartItem(product.productId)?.sampleQuantity
//                if (sampleQuantity != null){
//                    binding.txtQuantity.text = sampleQuantity.toString()
//                }
//                else{
//                    binding.btnAddToCart.visibility = View.VISIBLE
//                    binding.llChangeQuantity.visibility = View.GONE
//                }
//            }
//            else{
//                binding.btnAddToCart.visibility = View.VISIBLE
//                binding.llChangeQuantity.visibility = View.GONE
//                Toast.makeText(this, "Product removed from cart", Toast.LENGTH_SHORT).show()
//            }

//            val itemIndex = SampleDataSingleton.getIndexByProductId(productId.toString())
//            if (itemIndex != -1){
//                SampleDataSingleton.decreaseQuantity(itemIndex)
//            }
//
//            val itemFoundInCart = SampleDataSingleton.getCartItemList.find { it.productId == productId }
//            if (itemFoundInCart != null){
//                binding.txtSampleQuantity.text = "${itemFoundInCart.quantity * productDetails.sampleQuantity} gram"
//            }
//            else{
//                binding.btnAddToCart.visibility = View.VISIBLE
//                binding.llChangeQuantity.visibility = View.GONE
//                Toast.makeText(this, "Sample removed from cart", Toast.LENGTH_SHORT).show()
//            }
        }

    }

    private fun isItemInCart(){

        if (CartDataSingleton.productFoundInCart(productDetails.productId)){
            val quantity = CartDataSingleton.getProductCartItem(productDetails.productId)?.quantity
            binding.btnAddToCart.visibility = View.GONE
            binding.llChangeQuantity.visibility = View.VISIBLE
            binding.txtQuantity.text = quantity.toString()
        }
        else{
            binding.btnAddToCart.visibility = View.VISIBLE
            binding.llChangeQuantity.visibility = View.GONE
        }

        if (CartDataSingleton.sampleFoundInCart(productDetails.productId)){
            val sampleQuantity = CartDataSingleton.getSampleCartItem(productDetails.productId)?.sampleQuantity
            if (sampleQuantity != null) {
                binding.btnAddToSampleCart.visibility = View.GONE
                binding.llChangeSampleQuantity.visibility = View.VISIBLE
                binding.txtSampleQuantity.text = "${sampleQuantity * 10} gram"
            }
        }
        else{
            binding.btnAddToSampleCart.visibility = View.VISIBLE
            binding.llChangeSampleQuantity.visibility = View.GONE
        }

        if (productDetails.samplePrice == 0){
            binding.rlAddToSampleCart.visibility = View.GONE
        }
        else if (productDetails.samplePrice > 0){
            binding.rlAddToSampleCart.visibility = View.VISIBLE
        }

//        val itemFoundInCart = AppDataSingleton.getCartItemList.find { it.productId == productId }
//        val itemFoundInSampleCart = SampleDataSingleton.getCartItemList.find { it.productId == productId }

//        val sampleQuantity = if (product.sampleQuantity != 0){
//            product.sampleQuantity
//        }else{
//            10
//        }

//        cartItem = Cart(
//            productId.toString(),
//            productName.toString(),
//            price,
//            product.grade,
//            product.lotNumber,
//            product.bagSize,
//            1,
//            false,
//            product.samplePrice,
//            1
//        )

//        if (itemFoundInCart != null){
//            binding.btnAddToCart.visibility = View.GONE
//            binding.llChangeQuantity.visibility = View.VISIBLE
//            binding.txtQuantity.text = itemFoundInCart.quantity.toString()
//        }
//        else{
//            binding.btnAddToCart.visibility = View.VISIBLE
//            binding.llChangeQuantity.visibility = View.GONE
//        }
//
//        if (itemFoundInSampleCart != null){
//            binding.btnAddToCart.visibility = View.GONE
//            binding.llChangeQuantity.visibility = View.VISIBLE
//            binding.txtQuantity.text = "${itemFoundInSampleCart.sampleQuantity * product.sampleQuantity} gram"
//        }
//        else{
//            binding.btnAddToCart.visibility = View.VISIBLE
//            binding.llChangeQuantity.visibility = View.GONE
//        }

    }

//    private fun AddToCartFunctionality(){
//
//        val product = AppDataSingleton.getProductById(productId.toString())
//
//        val itemFoundInCart = AppDataSingleton.getCartItemList.find { it.productId == product.productId }
//
//        val price = product.discountedPrice ?: product.originalPrice
//        val cartItem = Cart(
//            product.productId,
//            product.productName,
//            price,
//            1
//        )
//
//        if (itemFoundInCart != null){
//            holder.btnAddToCart.visibility = View.GONE
//            holder.llChangeQuantity.visibility = View.VISIBLE
//            holder.txtQuantity.text = itemFoundInCart.quantity.toString()
//        }else {
//            holder.btnAddToCart.visibility = View.VISIBLE
//            holder.llChangeQuantity.visibility = View.GONE
//        }
//
//        Log.i("holder", holder.toString())
//
//        holder.btnAddToCart.setOnClickListener {
//            holder.txtQuantity.text = "1"
//            holder.llChangeQuantity.visibility = View.VISIBLE
//            it.visibility = View.GONE
//
//            AppDataSingleton.addCartItem(cartItem)
//            Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())
//
//        }
//
//        holder.txtIncreaseQuantity.setOnClickListener {
//
//            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
//            if (itemIndex != -1){
//                AppDataSingleton.increaseQuantity(itemIndex)
////            cartItem.quantity++
//                notifyItemChanged(position)
//                Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())
//            }
//
//        }
//
//        holder.txtDecreaseQuantity.setOnClickListener {
//            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
////            val quantity = AppDataSingleton.getQuantityByProductId(product.productId)
////            if (quantity == 1){
////                holder.btnAddToCart.visibility = View.VISIBLE
////                holder.llChangeQuantity.visibility = View.GONE
////            }
//            if (itemIndex != -1){
//                AppDataSingleton.decreaseQuantity(itemIndex)
//            }
//            notifyItemChanged(position)
//
////            itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
//
////            AppDataSingleton.decreaseQuantity(itemIndex)
////            if (cartItem.quantity > 1){
////                cartItem.quantity--
////            }
////            notifyItemChanged(position)
//            Log.i("decrease quantity", AppDataSingleton.getCartItemList.toString())
//        }
//    }

    private fun populateProductDetails(product: ProductDetails){

        binding.txtDetailsProductName.text = product.productName
        binding.txtDetailsProductDescription.text = product.description
        binding.txtGrade.text = product.grade
        binding.txtLotNumber.text = product.lotNumber
        binding.txtBagSize.text = product.bagSize.toString()

        val discount = product.discount
        val originalPrice = product.originalPrice
        val discountedPrice = product.discountedPrice

        if (discount && originalPrice != discountedPrice){
            binding.txtItemOriginalPrice.text = originalPrice.toString()
            binding.txtDiscountPrice.text = discountedPrice.toString()
            price = discountedPrice
        }
        else{
            binding.txtDiscountPrice.text = originalPrice.toString()
            binding.rlOriginalPrice.visibility = View.GONE
            price = originalPrice
        }

        if (product.samplePrice == 0){
            binding.rlAddToSampleCart.visibility = View.GONE
            binding.txtSamplePrice.visibility = View.GONE
            binding.txtSamplePriceText.visibility = View.GONE
            binding.txtSamplePriceRupeeSymbol.visibility = View.GONE
            binding.txtSamplePriceUnit.visibility = View.GONE
        }
        else{
            binding.txtSamplePrice.text = product.samplePrice.toString()
            binding.txtSamplePrice.visibility = View.VISIBLE
            binding.txtSamplePriceText.visibility = View.VISIBLE
            binding.txtSamplePriceRupeeSymbol.visibility = View.VISIBLE
            binding.txtSamplePriceUnit.visibility = View.VISIBLE
            binding.rlAddToSampleCart.visibility = View.VISIBLE
        }
    }

    private fun getProductDetails(id: String){

        binding.rlProgressProductDetails.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(this@ProductDetailsActivity)

        val url = getString(R.string.homeUrl) + "api/v1/product/$id"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        imageList = arrayListOf()
                        imageUriArray = mutableListOf()

                        val productDetailsObject = it.getJSONObject("data")

                        val imagesList = mutableListOf<String>()

                        Log.i("product details is  : ", productDetailsObject.toString())

                        val mainImage = productDetailsObject.getString("mainImage")

                        imageList.add(SlideModel(mainImage))
                        imageUriArray.add(mainImage)
                        imagesList.add(mainImage)

                        val images = productDetailsObject.getJSONArray("images")

                        for (i in 0 until images.length()){
                            imageList.add(SlideModel(images.getString(i)))
                            imageUriArray.add(images.getString(i))
                            imagesList.add(images.getString(i))
                        }

                        productDetails = ProductDetails(
                            productDetailsObject.getString("_id"),
                            productDetailsObject.getString("name"),
                            productDetailsObject.getInt("originalPrice"),
                            productDetailsObject.getInt("discountedPrice"),
                            productDetailsObject.optInt("samplePrice"),
                            productDetailsObject.optInt("sampleQuantity"),
                            productDetailsObject.getString("grade"),
                            productDetailsObject.getString("lotNumber"),
                            productDetailsObject.getInt("bagSize"),
                            productDetailsObject.getString("mainImage"),
                            productDetailsObject.optBoolean("discount"),
                            productDetailsObject.optString("description"),
                            imagesList
                        )

                        product = Product(
                            productDetailsObject.getString("_id"),
                            productDetailsObject.getString("name"),
                            productDetailsObject.getInt("originalPrice"),
                            productDetailsObject.getInt("discountedPrice"),
                            productDetailsObject.optInt("samplePrice"),
                            productDetailsObject.optInt("sampleQuantity"),
                            productDetailsObject.getString("grade"),
                            productDetailsObject.getString("lotNumber"),
                            productDetailsObject.getInt("bagSize"),
                            productDetailsObject.getString("mainImage"),
                            productDetailsObject.optBoolean("discount"),
                        )

                        binding.imgSliderProductImg.setImageList(imageList)

                        binding.imgSliderProductImg.setItemClickListener(object :
                            ItemClickListener {

                            override fun doubleClick(position: Int) {
                                Log.i("doubleClicked on", "imgSlider")

                                StfalconImageViewer.Builder(this@ProductDetailsActivity, imageUriArray){ view, image ->

                                    Picasso.get().load(image).transform(ResizeTransformation(1080)).into(view)

                                }.allowSwipeToDismiss(false).withStartPosition(position).show()

                            }

                            override fun onItemSelected(position: Int) {
                                Log.i("clicked on", "imgSlider")

                                StfalconImageViewer.Builder(this@ProductDetailsActivity, imageUriArray){ view, image ->

                                    Picasso.get().load(image).transform(ResizeTransformation(1080)).into(view)

                                }.allowSwipeToDismiss(false).withStartPosition(position).show()

                            }

                        })

                        productId = productDetailsObject.getString("_id")
                        productName = productDetailsObject.getString("name")

                        populateProductDetails(productDetails)

                        binding.rlProgressProductDetails.visibility = View.GONE

                        isItemInCart()

                    }
                    else{
                        Toast.makeText(this, "Product details not found", Toast.LENGTH_LONG).show()
                        binding.rlProgressProductDetails.visibility = View.GONE
                    }

                }catch (e: Exception){
                    Toast.makeText(this, "Product details not found", Toast.LENGTH_LONG).show()
                    binding.rlProgressProductDetails.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Product details not found", Toast.LENGTH_LONG).show()
                binding.rlProgressProductDetails.visibility = View.GONE
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

}