package com.darjeelingteagarden.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.*
import java.text.SimpleDateFormat
import java.util.Date

object AppDataSingleton {

    //Auth token
    private var authToken = ""
    val getAuthToken: String get() { return authToken}
    fun setAuthToken(token: String){
        authToken = token
    }
    fun isLoggedIn(): Boolean{
        return authToken != ""
    }

    //product id to use in product details page
    private var currentProductId = ""
    val getCurrentProductId: String get() { return currentProductId}
    fun setCurrentProductId(productId: String){
        currentProductId = productId
    }

    var currentProductIndex = -1

    //Product Category List
//    private lateinit var productCategoryList: MutableList<String>
//    val getProductCategoryList: MutableList<String> get() { return productCategoryList}
//    fun addProductCategory(category: String){
//        productCategoryList.add(category)
//    }
//    fun clearProductCategoryList(){
//        productCategoryList = mutableListOf()
//    }

    //Product Grade List
    private var productGradeList = mutableListOf<String>("All")
    val getProductGradeList: MutableList<String> get() { return productGradeList}
    fun addProductGrade(grade: String){
        productGradeList.add(grade)
    }
    fun clearProductGradeList(){
        productGradeList = mutableListOf()
    }

    //order id to show in order details page
    private var orderId: String = ""
    val getOrderId: String get() { return orderId }
    fun setOrderId(id: String){
        orderId = id
    }

    private var orderForMeId: String = ""
    val getOrderForMeId: String get() { return orderForMeId }
    fun setOrderForMeId(id: String){
        orderForMeId = id
    }

    //store items list to show in store tab in main activity
    private var storeItemList = mutableListOf<Product>()
    val getStoreItemList: MutableList<Product> get() { return storeItemList }
    fun addStoreItem(product: Product){
        storeItemList.add(product)
    }
    fun clearStoreItemList(){
        storeItemList = mutableListOf()
    }
    fun getProductNameById(id: String): String{
        return storeItemList.find {
            it.productId == id
        }?.productName.toString()
    }

    fun getProductById(id: String): Product?{
        return storeItemList.find {
            it.productId == id
        }
    }

    //filter options


    private lateinit var sharedPreferences: SharedPreferences
    private fun initializeSharedPreferences(context: Context){
        sharedPreferences = context.getSharedPreferences("DarjeelingTeaGardenCart", AppCompatActivity.MODE_PRIVATE)
    }
    //cart items list to show in cart tab in main activity
    private var cartItemList = mutableListOf<Cart>()
    val getCartItemList: MutableList<Cart> get() { return cartItemList }
//    val cartList: LiveData<MutableList<Cart>> get() { return cartItemList}
    fun addCartItem(cart: Cart): Boolean{
        val itemAdded = getCartItemByProductId(cart.productId)
        if (itemAdded == null){
            cartItemList.add(cart)
            return true
        }
        return false
    }

    private fun getIndex(cart: Cart): Int{
        return cartItemList.indexOf(cart)
    }

    fun getIndexByProductId(productId: String): Int{
        val item = getCartItemByProductId(productId)
        if (item != null){
            return getIndex(item)
        }
        return -1
    }

    private fun getCartItemByProductId(productId: String): Cart?{
        return cartItemList.find { it.productId == productId }
    }

    fun increaseQuantity(index: Int){
        if (cartItemList[index].isSample){
            cartItemList[index].sampleQuantity++
        }
        else{
            cartItemList[index].quantity++
        }
        Log.i("index increase quantity", index.toString())
//        cartItemList.find { it.productId == productId }?.quantity = (cartItemList.find { it.productId == productId }?.quantity!!) + 1
    }
    fun decreaseQuantity(index: Int){
        if (cartItemList[index].isSample){
            if (cartItemList[index].sampleQuantity > 1) cartItemList[index].sampleQuantity-- else cartItemList.removeAt(index)
        }
        else{
            if (cartItemList[index].quantity > 1) cartItemList[index].quantity-- else cartItemList.removeAt(index)
        }
    }

    fun increaseSampleQuantity(index: Int){
        cartItemList[index].isSample = true
        cartItemList[index].sampleQuantity++
    }

    fun decreaseSampleQuantity(index: Int){
        if (cartItemList[index].sampleQuantity > 1) cartItemList[index].sampleQuantity-- else cartItemList.removeAt(index)
    }

    fun getQuantityByProductId(productId: String): Int{
        val item = getCartItemByProductId(productId)
        if (item != null) {
            return item.quantity
        }
        return -1
    }

    fun clearCart(context: Context){
        cartItemList = mutableListOf()
        initializeSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun saveCart(context: Context){
        if (CartDataSingleton.cartList.size <= 0) return
        initializeSharedPreferences(context)
        val editor = sharedPreferences.edit()
        val cartProductIdList = mutableSetOf<String>()
        val sampleCartProductIdList = mutableSetOf<String>()
        CartDataSingleton.cartList.forEach { item ->

            if (item.isProduct){
                cartProductIdList.add(item.productId)
                editor.putInt(item.productId, item.quantity)
            }

            if (item.isSample){
                sampleCartProductIdList.add(item.productId)
                editor.putInt("${item.productId}sample", item.sampleQuantity)
            }

        }
//        SampleDataSingleton.getCartItemList.forEach { item ->
//            sampleCartProductIdList.add(item.productId)
//            editor.putInt("${item.productId}sample", item.sampleQuantity)
//        }
        editor.putStringSet("cartItems", cartProductIdList)
        editor.putStringSet("sampleCartItems", sampleCartProductIdList)
        editor.apply()

    }

    fun loadCart(context: Context){
        if (CartDataSingleton.cartList.size > 0){
            return
        }
        initializeSharedPreferences(context)
        val cartItemsId = sharedPreferences.getStringSet("cartItems", null) ?: return
        val sampleCartItemsId = sharedPreferences.getStringSet("sampleCartItems", null) ?: return
        cartItemsId.forEach { itemId ->
            val product = getProductById(itemId) ?: return@forEach
            val quantity = sharedPreferences.getInt(itemId, 1)
            val price = product.discountedPrice ?: product.originalPrice
            CartDataSingleton.addProductToCart(product)
            for (i in 0 until  quantity + 1){
                CartDataSingleton.increaseProductQuantity(product.productId)
            }
//            val cart = Cart(product.productId, product.productName, price, product.grade, product.lotNumber, product.bagSize, quantity, false, product.samplePrice, 0)
//            addCartItem(cart)
        }
        sampleCartItemsId.forEach { itemId ->
            val product = getProductById(itemId) ?: return@forEach
            val quantity = sharedPreferences.getInt("${itemId}sample", 1)
            val price = product.samplePrice
            CartDataSingleton.addSampleToCart(product)
            for (i in 0 until quantity){
                CartDataSingleton.increaseSampleQuantity(product.productId)
            }
//            val cart = Cart(product.productId, product.productName, 0, product.grade, product.lotNumber, product.bagSize, 0, true, price, quantity)
//            SampleDataSingleton.addCartItem(cart)
        }
    }


    //User info
    private var user = User("", "", "", 0, "")
    val getUserInfo: User get() { return user}
    fun setUserInfo(userInfo: User){
        user = userInfo
    }

    //My Orders
    var currentPageMyOrders = 1
    var totalMyOrders = 0
    private var myOrdersList = mutableListOf<MyOrder>()
    val getMyOrdersList: MutableList<MyOrder> get() { return myOrdersList }
    fun addToMyOrdersList(myOrder: MyOrder){
        myOrdersList.add(myOrder)
    }
    fun clearMyOrdersList(){
        myOrdersList = mutableListOf()
        currentPageMyOrders = 1
        totalMyOrders = 0
    }

    //Orders For Me
    private var ordersForMeList = mutableListOf<OrderForMe>()
    val getOrdersForMeList: MutableList<OrderForMe> get() { return ordersForMeList }
    fun addToOrdersForMeList(orderForMe: OrderForMe){
        ordersForMeList.add(orderForMe)
    }
    fun clearOrderForMeList(){
        ordersForMeList = mutableListOf()
    }

    //News

    private var newsList: MutableList<News> = mutableListOf()
    val getNewsList: MutableList<News> get() { return newsList}

    private var currentNewsId = ""
    val getCurrentNewsId: String get() { return currentNewsId}
    fun setCurrentNewsId(id: String){
        currentNewsId = id
    }

    var showUserDetails = false
    var myDownlineUserId = ""
    var myDownlineGoBack = false

    fun noInternet(context: Context){
        Toast.makeText(context, "No Internet", Toast.LENGTH_LONG).show()
    }

    fun callNow(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:+917307180148")
        context.startActivity(intent)
    }

    var orderPlaced = false

    //for refreshing home page data
    var lastRefreshed = Date().time
    var homePageRefreshed = false
    fun shouldRefreshHomePage(): Boolean{
        if (Date().after(Date(lastRefreshed + 3 * 60 * 1000))){
            return true
        }
        return false
    }

}