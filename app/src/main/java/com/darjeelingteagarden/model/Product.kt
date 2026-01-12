package com.darjeelingteagarden.model

data class Product(
    val productId: String,
    val productName: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val samplePrice: Double,
    val sampleQuantity: Int,
    val grade: String,
    val lotNumber: String,
    val bagSize: Int,
    val imageUrl: String,
    val discount: Boolean
)
