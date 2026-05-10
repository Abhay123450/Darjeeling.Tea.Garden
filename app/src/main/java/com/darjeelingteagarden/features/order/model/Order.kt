package com.darjeelingteagarden.features.order.model

import android.os.Parcelable
import com.darjeelingteagarden.model.Address
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class ItemInfo(
    val productType: String,
    val productId: String,
    val productName: String,
    val isSample: Boolean,
    val orderQuantity: Int,
    val currencyUnit: String,
    val price: Int,
    val status: String
): Parcelable

@Parcelize
data class Order(
    val _id: String,
    val orderId: String,
    val from: String,
    val to: List<String>,
    val orderDate: Date,
    val paymentDone: Boolean,
    val itemsPrice: Int,
    val nonSampleItemsPrice: Int,
    val cgst: Int,
    val sgst: Int,
    val igst: Int,
    val totalTax: Int,
    val totalPrice: Int,
    val discount: Int,
    val amountPayable: Int,
    val currentStatus: String,
    val itemCount: Int,
    val items: List<ItemInfo>,
    val address: Address
): Parcelable
