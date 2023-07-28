package com.darjeelingteagarden.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.darjeelingteagarden.model.*

object AppDataSingleton {

    //Auth token
    private var authToken = ""
    val getAuthToken: String get() { return authToken}
    fun setAuthToken(token: String){
        authToken = token
    }

    //product id to use in product details page
    private var currentProductId = ""
    val getCurrentProductId: String get() { return currentProductId}
    fun setCurrentProductId(productId: String){
        currentProductId = productId
    }

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

    //filter options

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
        cartItemList[index].quantity++
        Log.i("index increase quantity", index.toString())
//        cartItemList.find { it.productId == productId }?.quantity = (cartItemList.find { it.productId == productId }?.quantity!!) + 1
    }
    fun decreaseQuantity(index: Int){
        if (cartItemList[index].quantity > 1) cartItemList[index].quantity-- else cartItemList.removeAt(index)
    }

    fun getQuantityByProductId(productId: String): Int{
        val item = getCartItemByProductId(productId)
        if (item != null) {
            return item.quantity
        }
        return -1
    }

    fun clearCart(){
        cartItemList = mutableListOf()
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
}