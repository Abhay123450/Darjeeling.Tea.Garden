package com.darjeelingteagarden.model

data class Cart(
    val productId: String,
    val productName: String,
    val discountedPrice: Int,
    var quantity: Int
)
