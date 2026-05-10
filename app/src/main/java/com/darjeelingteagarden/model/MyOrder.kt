package com.darjeelingteagarden.model

data class MyOrder(
    val _id: String,
    val orderId: String?,
    val orderDate: String,
    val totalItems: Int,
    val totalPrice: Double,
    val currentStatus: String
)
