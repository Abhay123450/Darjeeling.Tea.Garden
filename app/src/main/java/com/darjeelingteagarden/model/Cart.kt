package com.darjeelingteagarden.model

data class Cart(
    val productId: String,
    val productName: String,
    val discountedPrice: Int,
    val grade: String,
    val lotNumber: String,
    val bagSize: Int,
    var isProduct: Boolean,
    var quantity: Int,
    var isSample: Boolean,
    val samplePrice: Int,
    var sampleQuantity: Int
)
