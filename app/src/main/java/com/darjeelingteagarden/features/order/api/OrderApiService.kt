package com.darjeelingteagarden.features.order.api

import android.os.Parcelable
import com.darjeelingteagarden.features.cart.model.CartItemDTO
import com.darjeelingteagarden.features.order.model.Order
import com.darjeelingteagarden.model.Address
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST


@Serializable
data class CreateOrderRequest(
    val cart: List<CartItemDTO>,
    val address: Address
)

@Parcelize
@Serializable
data class CreateOrderResponse(
    val success: Boolean,
    val data: Order
): Parcelable

interface OrderApiService {
    @POST("/api/v1/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): CreateOrderResponse
}