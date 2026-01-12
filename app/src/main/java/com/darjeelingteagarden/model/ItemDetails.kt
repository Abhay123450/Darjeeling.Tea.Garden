package com.darjeelingteagarden.model

data class ItemDetails(
    val id: String,
    val itemName: String,
    val itemPrice: Int,
    val itemQuantity: Int,
    val itemStatus: String,
    val receiveQuantity: Int,
    val receiveTime: String,
    val isSample: Boolean
)
