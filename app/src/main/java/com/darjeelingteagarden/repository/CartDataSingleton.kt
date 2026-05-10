package com.darjeelingteagarden.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product

object CartDataSingleton {

    var cartList = mutableStateListOf<Cart>()

    val totalCartItems = MutableLiveData<Int>()

    private fun updateCartCount() {
        // You can change this to cartList.sumOf { it.quantity } if you want total quantity
        totalCartItems.postValue(cartList.size)
    }

    fun addProductToCart(product: Product){
        val existingItem = cartList.find {
            it.productId == product.productId
        }

        if (existingItem == null){
            cartList.add(
                Cart(
                    product.productId,
                    product.productName,
                    product.discountedPrice,
                    product.grade,
                    product.lotNumber,
                    product.bagSize,
                    true,
                    1,
                    false,
                    product.samplePrice,
                    0
                )
            )
        } else {
            // To trigger a UI update on an internal property change,
            // you often need to replace the element or use a wrapper State
            val index = cartList.indexOf(existingItem)
            cartList[index] = existingItem.copy(quantity = 1, isProduct = true)
        }

//        cartList.forEach {
//            if (it.productId == product.productId){
//                it.isProduct = true
//                it.quantity = 1
//                return@forEach
//            }
//        }

        updateCartCount()

    }

    fun increaseProductQuantity(productId: String){
        val index = cartList.indexOfFirst { it.productId == productId }
        if (index != -1) {
            val item = cartList[index]
            cartList[index] = item.copy(quantity = item.quantity + 1)
        }
        updateCartCount()
    }

    fun decreaseProductQuantity(productId: String){
        val index = cartList.indexOfFirst { it.productId == productId }
        if (index != -1) {
            val item = cartList[index]
            if (item.quantity > 1) {
                cartList[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cartList.removeAt(index)
            }
        }
        updateCartCount()
    }

    fun productFoundInCart(productId: String): Boolean{
        cartList.forEach {
            if (it.productId == productId && it.isProduct){
                return true
            }
        }
        return false
    }

    fun getProductCartItem(productId: String): Cart?{
        for (cart in  cartList){
            if (cart.productId == productId && cart.isProduct){
                return cart
            }
        }
        return null
    }

    fun addSampleToCart(product: Product){
        val existingItem = cartList.find {
            it.productId == product.productId
        }

        if (existingItem == null){
            cartList.add(
                Cart(
                    product.productId,
                    product.productName,
                    product.discountedPrice,
                    product.grade,
                    product.lotNumber,
                    product.bagSize,
                    false,
                    0,
                    true,
                    product.samplePrice,
                    1
                )
            )
        } else{
            val index = cartList.indexOf(existingItem)
            cartList[index] = existingItem.copy(isSample = true, sampleQuantity = 1)
        }

//        cartList.forEach {
//            if (it.productId == product.productId){
//                it.isSample = true
//                it.sampleQuantity = 1
//            }
//        }

        updateCartCount()

    }

    fun increaseSampleQuantity(productId: String){
        cartList.forEach {
            if (it.productId == productId){
                it.sampleQuantity++
                it.isSample = true
            }
            return@forEach
        }
        updateCartCount()
    }

    fun decreaseSampleQuantity(productId: String){
        var index = -1
        cartList.forEachIndexed { i, cart ->
            if (cart.productId == productId ){
                if (cart.sampleQuantity > 1){
                    cart.sampleQuantity--
                }
                else if (!cart.isProduct){
                    index = i
                }
                else{
                    cart.isSample = false
                    cart.sampleQuantity = 0
                }
                return@forEachIndexed
            }
        }
        if (index != -1){
            cartList.removeAt(index)
        }
        updateCartCount()
    }

    fun sampleFoundInCart(productId: String): Boolean{
        cartList.forEach {
            if (it.productId == productId && it.isSample){
                return true
            }
        }
        return false
    }

    fun getSampleCartItem(productId: String): Cart?{
        for (cart in  cartList){
            if (cart.productId == productId && cart.isSample){
                return cart
            }
        }
        return null
    }

    private lateinit var sharedPreferences: SharedPreferences
    private fun initializeSharedPreferences(context: Context){
        sharedPreferences = context.getSharedPreferences("DarjeelingTeaGardenCart", AppCompatActivity.MODE_PRIVATE)
    }

    fun clearCart(context: Context){
        cartList = mutableStateListOf()
        initializeSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        updateCartCount()
    }

}