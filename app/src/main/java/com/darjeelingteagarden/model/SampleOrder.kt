package com.darjeelingteagarden.model

data class SampleOrder(
    val orderId: String,
    val orderDate: String,
    val totalItems: Int,
    val totalPrice: Int,
    val currentStatus: String
)
