//package com.darjeelingteagarden.database
//
//import android.app.Application
//import androidx.lifecycle.AndroidViewModel
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//class CartViewModel(application: Application): AndroidViewModel(application) {
//
//    val getCartItems: LiveData<List<CartEntity>>
//    private val repository: CartRepository
//
//    init {
//        val cartDao = CartDatabase.getCartDatabase(application).cartDao()
//        repository = CartRepository(cartDao)
//        getCartItems = repository.readCartData
//    }
//
//    fun addToCart(cartEntity: CartEntity){
//        viewModelScope.launch(Dispatchers.IO) {
//            repository.addItem(cartEntity)
//        }
//    }
//
//    fun itemExistsInCart(product_id: String){
//        repository.itemExistsInCart(product_id)
//    }
//}