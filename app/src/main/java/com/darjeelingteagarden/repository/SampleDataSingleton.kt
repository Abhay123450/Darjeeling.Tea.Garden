package com.darjeelingteagarden.repository

import android.util.Log
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.OrderForMe
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.model.SampleOrder

object SampleDataSingleton {

    var historyShown = false

    //store items list to show in store tab in main activity
    private var storeItemList = mutableListOf<Sample>()
    val getStoreItemList: MutableList<Sample> get() { return storeItemList }
    fun addStoreItem(sample: Sample){
        storeItemList.add(sample)
    }
    fun clearStoreItemList(){
        storeItemList = mutableListOf()
    }
    fun getProductNameById(id: String): String{
        return storeItemList.find {
            it.sampleId == id
        }?.sampleName.toString()
    }

    //cart items list to show in cart tab in main activity
    private var cartItemList = mutableListOf<Cart>()
    val getCartItemList: MutableList<Cart> get() { return cartItemList }

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
        cartItemList[index].sampleQuantity++
        Log.i("index increase quantity", index.toString())
//        cartItemList.find { it.productId == productId }?.quantity = (cartItemList.find { it.productId == productId }?.quantity!!) + 1
    }
    fun decreaseQuantity(index: Int){
        if (cartItemList[index].sampleQuantity > 1) cartItemList[index].sampleQuantity-- else cartItemList.removeAt(index)
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

    //Sample history
    var currentPage = 1
    private var sampleOrderList = mutableListOf<SampleOrder>()
    val getSampleHistoryList: MutableList<SampleOrder> get() { return sampleOrderList }
    fun addSampleOrder(sampleOrder: SampleOrder){
        sampleOrderList.add(sampleOrder)
    }
    fun clearSampleOrderList(){
        sampleOrderList = mutableListOf()
    }

    //Sample Orders for me
    private var sampleOrdersForMeList = mutableListOf<OrderForMe>()
    val getSampleOrdersForMeList: MutableList<OrderForMe> get() { return sampleOrdersForMeList }
    fun addToSampleOrdersForMeList(orderForMe: OrderForMe){
        sampleOrdersForMeList.add(orderForMe)
    }
    fun clearSampleOrderForMeList(){
        sampleOrdersForMeList = mutableListOf<OrderForMe>()
    }

    private var sampleOrderForMeId: String = ""
    val getSampleOrderForMeId: String get() { return sampleOrderForMeId }
    fun setSampleOrderForMeId(id: String){
        sampleOrderForMeId = id
    }

}