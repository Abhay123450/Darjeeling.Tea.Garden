package com.darjeelingteagarden.model

data class OrderInfo(
    val isSampleOrder: Boolean,
    val orderId: String,
    val apiKeyId: String,
    val itemTotal: Double,
    val discount: Double,
    val totalTax: Double,
    val totalAmount: Double
)
