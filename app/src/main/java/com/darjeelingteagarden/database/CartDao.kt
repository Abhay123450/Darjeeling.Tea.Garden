package com.darjeelingteagarden.database
//
//import androidx.lifecycle.LiveData
//import androidx.room.*
//
//@Dao
//interface CartDao {
//
//    @Insert
//    suspend fun addToCart(cartEntity: CartEntity)
//
//    @Update
//    suspend fun changeQuantity(cartEntity: CartEntity)
//
//    @Delete
//    fun removeFromCart(cartEntity: CartEntity)
//
//    @Query("SELECT * FROM cart")
//    fun getCartItems(): LiveData<List<CartEntity>>
//
//    @Query("SELECT * FROM cart WHERE product_id = :product_id")
//    fun getItemById(product_id: String): CartEntity
//}