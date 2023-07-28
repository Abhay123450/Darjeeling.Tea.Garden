package com.darjeelingteagarden.model

data class OrderForMe(
    val orderId: String,
    val fromUserId: String,
    val fromName: String,
    val fromRole: String,
    val fromAddress: String,
    val orderDate: String,
    val totalItems: Int,
    val totalPrice: Int,
    val orderStatus: String
)
