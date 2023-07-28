package com.darjeelingteagarden.model

data class Product(
    val productId: String,
    val productName: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val grade: String,
    val lotNumber: Long,
    val bagSize: Int,
    val imageUrl: String
)
