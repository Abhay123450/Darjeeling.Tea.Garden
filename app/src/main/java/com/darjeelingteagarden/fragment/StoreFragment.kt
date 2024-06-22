package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.ProductDetailsActivity
import com.darjeelingteagarden.adapter.StoreRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentStoreBinding
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.repository.StoreDataSingleton
import com.darjeelingteagarden.util.ResizeTransformation
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.security.AllPermission
import java.util.Timer

class StoreFragment : Fragment() {

    lateinit var storeSwipeRefreshLayout: SwipeRefreshLayout

    private lateinit var textInputEditTextSearchBar: TextInputEditText

    private lateinit var recyclerViewStore: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private var productList = mutableListOf<Product>()

    private lateinit var materialAlertDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var customAlertDialogView: View

    //filter options
    private lateinit var rangeSliderPrice: RangeSlider
    private lateinit var chipGroupSort: ChipGroup
    private lateinit var autoCompleteTextViewCategory: AutoCompleteTextView
    private lateinit var autoCompleteTextViewGrade: AutoCompleteTextView
    private lateinit var txtMinPrice: TextView
    private lateinit var txtMaxPrice: TextView

    var isProductListReceived = false
    var isOnCreateViewCompleted = false

    var token = ""

    private var isStoreItemLoaded = false
    private var isCartItemLoaded = false

    private var viewingProductDetails: Boolean = false
    private var imageList: ArrayList<SlideModel> = arrayListOf()
    private var imageUriArray: ArrayList<String> = arrayListOf()

    //filter Options
    private var filterMinPrice = 0
    private var filterMaxPrice = 10000
    private var filterAscending: Boolean? = null
    private var filterCheckedChipId = View.NO_ID
    private var filterGrade: String = "All"


//    private lateinit var mAppDataViewModel: AppDataViewModel
//    private var productList = mutableListOf<Product>()
    private var cartList = mutableListOf<Cart>()
    private var storeLoaded = false
    private var isFragmentResumed = false

    lateinit var recyclerAdapter: StoreRecyclerAdapter

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private lateinit var binding: FragmentStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_store, container, false)
        binding = FragmentStoreBinding.inflate(inflater, container, false)
//        mAppDataViewModel = ViewModelProvider(this)[AppDataViewModel::class.java]
//        productList = mAppDataViewModel.storeItemList
//        cartList = mAppDataViewModel.cartItemList
//        productList = AppDataSingleton.getStoreItemList
        cartList = CartDataSingleton.cartList
        token = AppDataSingleton.getAuthToken

        textInputEditTextSearchBar = binding.storeSearchText
        storeSwipeRefreshLayout = binding.swipeRefreshStore
        recyclerViewStore = binding.recyclerViewStore
        layoutManager = LinearLayoutManager(activity)

//        Log.i("StoreFragProductList", productList.toString())
        Log.i("StoreFragCartList", cartList.toString())
//        Log.i("mAppDataViewBinding PL", mAppDataViewModel.storeItemList.toString())

        storeSwipeRefreshLayout.isRefreshing = true
        isFragmentResumed = true

        if (StoreDataSingleton.loadingStoreItem){
            loadStoreItems()
        }
        else{
            if (productList.size <= 0){
                productList = AppDataSingleton.getStoreItemList
                if (productList.size <= 0){
                    StoreDataSingleton.getStoreItems(mContext)
                    loadStoreItems()
                }
            }
            if (!storeLoaded){
                populateRecyclerView(productList)
            }
        }

        textInputEditTextSearchBar.doOnTextChanged { text, start, before, count ->

            Log.i("search text", text.toString())

            if (count > 0){
                searchStore(text.toString())
            }
            else{
                populateRecyclerView(AppDataSingleton.getStoreItemList)
            }

        }

        binding.imgFilter.setOnClickListener {
            customAlertDialogView = LayoutInflater
                .from(mContext)
                .inflate(R.layout.layout_filter_dialogue, null, false)

            showFilterDialog()
        }

        storeSwipeRefreshLayout.setOnRefreshListener{
            storeSwipeRefreshLayout.isRefreshing = true
            isProductListReceived = false
            AppDataSingleton.clearStoreItemList()
            StoreDataSingleton.getStoreItems(mContext)
            loadStoreItems()
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

//        binding.imgBtnClose.setOnClickListener {
//            binding.rlProductDetails.visibility = View.GONE
//        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        isFragmentResumed = true
        if (AppDataSingleton.currentProductIndex > -1){
            recyclerAdapter.notifyItemChanged(AppDataSingleton.currentProductIndex)
        }
//        if (AppDataSingleton.getStoreItemList.size == 0){
//            while (StoreDataSingleton.loadingStoreItem){
//                continue
//            }
//            if (AppDataSingleton.getStoreItemList.size == 0){
//                storeSwipeRefreshLayout.isRefreshing = true
//                getStoreItems()
//            }
//            else{
//                populateRecyclerView(AppDataSingleton.getStoreItemList)
//                storeSwipeRefreshLayout.isRefreshing = false
//            }
//
////            getCartItems()
//        }
    }

    override fun onPause() {
        super.onPause()
        isFragmentResumed = false
    }

    private fun loadStoreItems(){
//        launch {
        if (StoreDataSingleton.loadingStoreItem){
            val timer = object : CountDownTimer(120000L, 500){
                override fun onTick(millisUntilFinished: Long) {
                    if (!StoreDataSingleton.loadingStoreItem){
                        if (isFragmentResumed) {
                            productList = AppDataSingleton.getStoreItemList
                            populateRecyclerView(productList)
                            storeSwipeRefreshLayout.isRefreshing = false
//                        Log.i("storefrag timer", millisUntilFinished.toString())
                            cancel()
                        }
                    }
                }

                override fun onFinish() {

                }

            }
            timer.start()
        }
        else{
            if (AppDataSingleton.getStoreItemList.size <= 0){
                storeSwipeRefreshLayout.isRefreshing = true
                AppDataSingleton.clearStoreItemList()
                StoreDataSingleton.getStoreItems(mContext)
                loadStoreItems()
            }
            else{
                productList = AppDataSingleton.getStoreItemList
                populateRecyclerView(productList)
                storeSwipeRefreshLayout.isRefreshing = false
            }
        }

    }

    private fun showFilterDialog(){

        rangeSliderPrice = customAlertDialogView.findViewById(R.id.rangeSliderPrice)
        chipGroupSort = customAlertDialogView.findViewById(R.id.sortChipGroup)
        txtMinPrice = customAlertDialogView.findViewById(R.id.txtPriceMin)
        txtMaxPrice = customAlertDialogView.findViewById(R.id.txtPriceMax)

        rangeSliderPrice.addOnChangeListener { slider, value, fromUser ->
            txtMinPrice.text = slider.values[0].toInt().toString()
            txtMaxPrice.text = slider.values[1].toInt().toString()
        }

        rangeSliderPrice.values = mutableListOf(filterMinPrice.toFloat(), filterMaxPrice.toFloat())
        if (filterCheckedChipId != View.NO_ID){
            chipGroupSort.check(filterCheckedChipId)
        }

        autoCompleteTextViewGrade = customAlertDialogView.findViewById(R.id.autoCompleteTextViewFilterGrade)

        val gradeArray = AppDataSingleton.getProductGradeList
        val gradeArrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, gradeArray)

        if (filterGrade != "All"){
            autoCompleteTextViewGrade.setText(filterGrade, false)
        }

//        autoCompleteTextViewCategory.setAdapter(categoryArrayAdapter)

        autoCompleteTextViewGrade.setAdapter(gradeArrayAdapter)

        materialAlertDialogBuilder = MaterialAlertDialogBuilder(activity as Context)
            .setView(customAlertDialogView)
            .setCancelable(false)
            .setPositiveButton("Apply"){dialog, int ->

                val rangeSliderValues = rangeSliderPrice.values
                val minPrice = rangeSliderValues[0]
                val maxPrice = rangeSliderValues[1]

                //category
//                var category = autoCompleteTextViewCategory.text.toString()

                //garde
                var grade = autoCompleteTextViewGrade.text.toString()

//                if (category == "All"){
//                    category = ""
//                }

                if (grade == "All"){
                    grade =""
                }

                var sort: Int? = null

                if (chipGroupSort.checkedChipId != View.NO_ID) {
                    val checkedChip =
                        customAlertDialogView.findViewById<Chip>(chipGroupSort.checkedChipId)
                    filterCheckedChipId = checkedChip.id

                    if (checkedChip.text == getString(R.string.low_to_high)) {
                        sort = 0
                        filterAscending = true
                    } else if (checkedChip.text == getString(R.string.high_to_low)) {
                        sort = 1
                        filterAscending = false
                    }

                }
                else {
                    filterAscending = null
                }

                Log.i("min max Price before ", "$minPrice || $maxPrice")

                applyFilter(minPrice.toInt(), maxPrice.toInt(), sort, "", grade)
                filterMinPrice = minPrice.toInt()
                filterMaxPrice = maxPrice.toInt()
                filterGrade = grade

            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Reset"){ dialog, view ->

                rangeSliderPrice.values = mutableListOf(0f, 10000f)
                chipGroupSort.clearCheck()
                autoCompleteTextViewGrade.setText(autoCompleteTextViewGrade.adapter.getItem(0).toString())


                filterMinPrice = 0
                filterMaxPrice = 10000
                filterAscending = null
                filterCheckedChipId = View.NO_ID
                filterGrade = "All"

                productList = AppDataSingleton.getStoreItemList
                populateRecyclerView(productList)

            }

        materialAlertDialogBuilder.show()

    }

//    category: String, grade: String
    private fun applyFilter(minPrice: Int, maxPrice: Int, sort: Int?, category: String, grade: String){

        Log.i("min max Price sort ", "$minPrice || $maxPrice || $sort")

//        val filteredList: MutableList<Product> = AppDataSingleton.getStoreItemList.filter {

        productList = AppDataSingleton.getStoreItemList
        productList = productList.filter {

            it.discountedPrice in minPrice..maxPrice
                    &&
            it.grade.contains(grade, ignoreCase = true)

        } as MutableList<Product>

        //sort according to price
        if (sort == 0){//ascending
            productList.sortBy {
                it.discountedPrice
            }
        }
        else if (sort == 1){//descending
            productList.sortByDescending {
                it.discountedPrice
            }
        }

        Log.i("filtered List :::", productList.toString())

        populateRecyclerView(productList)

    }

    private fun searchStore(searchText: String){

        val filteredList: MutableList<Product> = AppDataSingleton.getStoreItemList.filter {
            it.productName.contains(searchText, ignoreCase = true) ||
            it.grade.contains(searchText, ignoreCase = true)
        } as MutableList<Product>

        Log.i("filtered List search :", filteredList.toString())

        populateRecyclerView(filteredList)

    }

    private fun populateRecyclerView(productList: MutableList<Product>){
        storeLoaded = true
        Log.i("store recycleradapterPL", productList.toString())
        Log.i("store recycleradapterCL", cartList.toString())
        Log.i("product list size", productList.size.toString())
        Log.i("cart list size", cartList.size.toString())
        recyclerAdapter = StoreRecyclerAdapter(mContext, productList, cartList, findNavController()){

//            getProductDetails(it.productId)
            val intent = Intent(mContext, ProductDetailsActivity::class.java)
            intent.putExtra("productId", it.productId)
            startActivity(intent)
        }
        recyclerViewStore.adapter = recyclerAdapter
        recyclerViewStore.layoutManager = layoutManager
        recyclerViewStore.addItemDecoration(
            DividerItemDecoration(
                recyclerViewStore.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )
        storeSwipeRefreshLayout.isRefreshing = false
    }

//    fun addItemToCart(productId: String, productName: String, originalPrice: Int, discountedPrice: Int, category: String, grade: String, quantity: Int){
//        val cartItem = CartEntity(
//            0,
//            productId,
//            productName,
//            originalPrice,
//            discountedPrice,
//            category,
//            grade,
//            quantity
//        )
//
//        mCartViewModel.addToCart(cartItem)
//
//        Toast.makeText(
//            activity,
//            "Added to cart",
//            Toast.LENGTH_SHORT
//        ).show()
//    }

    private fun getCartItems(){

        val queue = Volley.newRequestQueue(activity as Context)

        val url = getString(R.string.homeUrl) + "api/v1/user/cart"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")
                        Log.i("cart data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                activity as Context,"Cart is empty", Toast.LENGTH_LONG
                            ).show()
                        }

//                        for (i in 0 until data.length()){
//                            val cartInfo = data.getJSONObject(i)
//                            val cartObject = Cart(
//                                cartInfo.getJSONObject("productId").getString("_id"),
//                                cartInfo.getString("name"),
//                                cartInfo.getJSONObject("productId").getInt("discountedPrice"),
//                                cartInfo.getInt("quantity")
//                            )
//
//                            AppDataSingleton.addCartItem(cartObject)
////                            cartList.add(cartObject)
//
//                        }
//
//                        isCartItemLoaded = true

                        if (isCartItemLoaded && isStoreItemLoaded){
                            populateRecyclerView(AppDataSingleton.getStoreItemList)
                            storeSwipeRefreshLayout.isRefreshing = false
                        }

                        cartList = CartDataSingleton.cartList

                    }
                    else{
                        Toast.makeText(
                            activity as Context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        activity as Context,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")

//                if (!isLoggedIn){
//                    val intent = Intent(activity as Context, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        activity as Context,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
//                    requireActivity().finish()
//                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(activity as Context)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()

                storeSwipeRefreshLayout.isRefreshing = false

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = token
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun getStoreItems(){

        val queue = Volley.newRequestQueue(activity as Context)

        val url = getString(R.string.homeUrl) + "api/v1/products"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")
                        Log.i("store data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                activity as Context,"No product found", Toast.LENGTH_LONG
                            ).show()
                        }

                        AppDataSingleton.clearStoreItemList()

                        for (i in 0 until data.length()){
                            val productInfo = data.getJSONObject(i)

                            val discountedPrice = if (productInfo.getBoolean("discount")){
                                productInfo.getInt("discountedPrice")
                            }else{
                                productInfo.getInt("originalPrice")
                            }
                            val productObject = Product(
                                productInfo.getString("_id"),
                                productInfo.getString("name"),
                                productInfo.getInt("originalPrice"),
                                discountedPrice,
                                productInfo.optDouble("samplePrice"),
                                10,
                                productInfo.getString("grade"),
                                productInfo.getString("lotNumber"),
                                productInfo.getInt("bagSize"),
                                productInfo.getString("mainImage"),
                                productInfo.getBoolean("discount")
                            )
                            AppDataSingleton.addStoreItem(productObject)
                        }
//                        productList = AppDataSingleton.getStoreItemList

                        isStoreItemLoaded = true
                        if (isCartItemLoaded && isStoreItemLoaded){
                            populateRecyclerView(AppDataSingleton.getStoreItemList)
                            recyclerAdapter.notifyDataSetChanged()
                            storeSwipeRefreshLayout.isRefreshing = false
                        }

                    }
                    else{
                        Toast.makeText(
                            activity as Context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        activity as Context,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")

//                if (!isLoggedIn){
//                    val intent = Intent(activity as Context, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        activity as Context,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
//                    requireActivity().finish()
//                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(activity as Context)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()

                storeSwipeRefreshLayout.isRefreshing = false

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = token
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

//    private fun getProductDetails(id: String){
//
//        binding.rlProductDetails.visibility = View.VISIBLE
//        viewingProductDetails = true
//
//        val queue = Volley.newRequestQueue(mContext)
//
//        val url = getString(R.string.homeUrl) + "api/v1/product/$id"
//
//        val jsonObjectRequest = object: JsonObjectRequest(
//            Method.GET,
//            url,
//            null,
//            Response.Listener {
//                try {
//
//                    val success = it.getBoolean("success")
//
//                    if (success){
//
//                        imageList = arrayListOf()
//                        imageUriArray = arrayListOf()
//
//                        val productDetails = it.getJSONObject("data")
//
//                        val mainImage = it.getString("mainImage")
//
//                        imageList.add(SlideModel(mainImage))
//                        imageUriArray.add(mainImage)
//
//                        val images = productDetails.getJSONArray("images")
//
//                        for (i in 0 until images.length()){
//                            imageList.add(SlideModel(images.getString(i)))
//                            imageUriArray.add(images.getString(i))
//                        }
//
//                        binding.imgSliderProductImg.setImageList(imageList)
//
//                        binding.imgSliderProductImg.setItemClickListener(object : ItemClickListener{
//
//                            override fun doubleClick(position: Int) {
//                                Log.i("doubleClicked on", "imgSlider")
//
//                                StfalconImageViewer.Builder(mContext, imageUriArray){view, image ->
//
//                                    Picasso.get().load(image).transform(ResizeTransformation(1920)).into(view)
//
//                                }.allowSwipeToDismiss(false).withStartPosition(position).show()
//
//                            }
//
//                            override fun onItemSelected(position: Int) {
//                                Log.i("clicked on", "imgSlider")
//
//                                StfalconImageViewer.Builder(mContext, imageUriArray){view, image ->
//
//                                    Picasso.get().load(image).into(view)
//
//                                }.allowSwipeToDismiss(false).withStartPosition(position).show()
//
//                            }
//
//                        })
//
//                    }
//                    else{
//                        binding.rlProductDetails.visibility = View.GONE
//                        viewingProductDetails = false
//                    }
//
//                }catch (e: Exception){
//                    binding.rlProductDetails.visibility = View.GONE
//                    viewingProductDetails = false
//                }
//            },
//            Response.ErrorListener {
//                binding.rlProductDetails.visibility = View.GONE
//                viewingProductDetails = false
//            }
//        ){
//            override fun getHeaders(): MutableMap<String, String> {
//                val headers = HashMap<String, String>()
//                headers["Content-Type"] = "application/json"
//                headers["auth-token"] = AppDataSingleton.getAuthToken
//                return headers
//            }
//        }
//
//        queue.add(jsonObjectRequest)
//
//    }

}