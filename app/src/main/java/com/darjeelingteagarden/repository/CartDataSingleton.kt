package com.darjeelingteagarden.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product

object CartDataSingleton {

    var cartList = mutableListOf<Cart>()

//    fun getCartList(): MutableList<Cart>{
//        return cartList
//    }

    fun addProductToCart(product: Product){
        val itemExists = cartList.find {
            it.productId == product.productId
        }

        if (itemExists == null){
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
        }

        cartList.forEach {
            if (it.productId == product.productId){
                it.isProduct = true
                it.quantity = 1
                return@forEach
            }
        }

    }

    fun increaseProductQuantity(productId: String){
        cartList.forEach {
            if (it.productId == productId){
                it.quantity = it.quantity + 1
                it.isProduct = true
            }
            return@forEach
        }
    }

    fun decreaseProductQuantity(productId: String){
        var index = -1
        cartList.forEachIndexed { i, cart ->
            if (cart.productId == productId ){
                if (cart.quantity > 1){
                    cart.quantity--
                }
                else if (!cart.isSample){
                    index = i
                }
                else{
                    cart.isProduct = false
                    cart.quantity = 0
                }
                return@forEachIndexed
            }
        }
        Log.d("index", index.toString())
        if (index != -1){
            cartList.removeAt(index)
        }
    }

    fun productFoundInCart(productId: String): Boolean{
        cartList.forEach {
            if (it.productId == productId && it.isProduct){
                return true
                return@forEach
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
        val itemExists = cartList.find {
            it.productId == product.productId
        }

        if (itemExists == null){
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
        }

        cartList.forEach {
            if (it.productId == product.productId){
                it.isSample = true
                it.sampleQuantity = 1
            }
        }

    }

    fun increaseSampleQuantity(productId: String){
        cartList.forEach {
            if (it.productId == productId){
                it.sampleQuantity++
                it.isSample = true
            }
            return@forEach
        }
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
    }

    fun sampleFoundInCart(productId: String): Boolean{
        cartList.forEach {
            if (it.productId == productId && it.isSample){
                return true
                return@forEach
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
        cartList = mutableListOf()
        initializeSharedPreferences(context)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}