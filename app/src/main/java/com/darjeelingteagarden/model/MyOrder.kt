package com.darjeelingteagarden.model

data class MyOrder(
    val orderId: String,
    val orderDate: String,
    val totalItems: Int,
    val totalPrice: Double,
    val currentStatus: String
)
