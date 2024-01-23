package com.darjeelingteagarden.model

data class ProductDetails(
    val productId: String,
    val productName: String,
    val originalPrice: Int,
    val discountedPrice: Int,
    val samplePrice: Int,
    val sampleQuantity: Int,
    val grade: String,
    val lotNumber: String,
    val bagSize: Int,
    val imageUrl: String,
    val discount: Boolean,
    val description: String,
    val images: MutableList<String>
)
