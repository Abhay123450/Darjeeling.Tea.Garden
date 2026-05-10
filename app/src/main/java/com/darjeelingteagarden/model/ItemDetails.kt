package com.darjeelingteagarden.model

data class ItemDetails(
    val id: String,
    val productType: String?,
    val itemName: String,
    val itemPrice: Int,
    val currencyUnit: String,
    val itemQuantity: Int,
    val itemStatus: String,
    val receiveQuantity: Int,
    val receiveTime: String,
    val isSample: Boolean
)
