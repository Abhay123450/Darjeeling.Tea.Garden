package com.darjeelingteagarden.repository

import androidx.lifecycle.MutableLiveData
import com.darjeelingteagarden.features.cart.model.CartItem

object CartSingleton {
//    var cart = mutableListOf<CartItem>()
//
//    val cartSize = MutableLiveData<Int>()
//
//    private fun updateCartSize(){
//        cartSize.postValue(cart.size)
//    }
//
//    fun addProductToCart(cartItem: CartItem){
//
//        val existingItem = cart.find {
//            it.productId == cartItem.productId && it.itemType == cartItem.itemType
//        }
//
//        if (existingItem == null){
//            cart.add(cartItem)
//        } else {
//            val index = cart.indexOf(existingItem)
//            cart[index] = existingItem.copy(quantity = 1, isProduct = true)
//        }
//    }
}