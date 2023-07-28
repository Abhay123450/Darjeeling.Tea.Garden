package com.darjeelingteagarden.database

import androidx.lifecycle.LiveData

class CartRepository(private val cartDao: CartDao) {

    val readCartData: LiveData<List<CartEntity>> = cartDao.getCartItems()

    suspend fun addItem(cartEntity: CartEntity){
        cartDao.addToCart(cartEntity)
    }

    fun itemExistsInCart(product_id: String){
        cartDao.getItemById(product_id)
    }
}