package com.darjeelingteagarden.features.cart.model

import com.darjeelingteagarden.features.looseTea.model.LooseTea
import com.darjeelingteagarden.features.packagedTea.model.PackagedTea
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface CartItem {
    val productId: String
    val quantity: Int
    val uniqueCartId: String
}

@Serializable
@SerialName("LOOSE_TEA")
data class LooseTeaCartItem(
    override val productId: String,
    override val quantity: Int,
    val productDetails: LooseTea,
): CartItem {
    override val uniqueCartId: String
        get() = "loose_tea_$productId"
}

@Serializable
@SerialName("LOOSE_TEA_SAMPLE")
data class LooseTeaSampleCartItem(
    override val productId: String,
    override val quantity: Int,
    val productDetails: LooseTea,
): CartItem {
    override val uniqueCartId: String
        get() = "loose_tea_sample_$productId"
}

@Serializable
@SerialName("PACKAGED_TEA")
data class PackagedTeaCartItem(
    override val productId: String,
    override val quantity: Int,
    val productDetails: PackagedTea,
): CartItem {
    override val uniqueCartId: String
        get() = "packaged_tea_$productId"
}

@Serializable
@SerialName("PACKAGED_TEA_SAMPLE")
data class PackagedTeaSampleCartItem(
    override val productId: String,
    override val quantity: Int,
    val productDetails: PackagedTea,
): CartItem {
    override val uniqueCartId: String
        get() = "packaged_tea_sample_$productId"
}

@Serializable
data class CartItemDTO(
    val itemType: String,
    val productId: String,
    val quantity: Int
)

fun CartItem.toDto(): CartItemDTO {
    // Determine the type string based on the specific subclass
    val type = when (this) {
        is LooseTeaCartItem -> "LOOSE_TEA"
        is LooseTeaSampleCartItem -> "LOOSE_TEA_SAMPLE"
        is PackagedTeaCartItem -> "PACKAGED_TEA"
        is PackagedTeaSampleCartItem -> "PACKAGED_TEA_SAMPLE"
    }

    return CartItemDTO(
        itemType = type,
        productId = this.productId,
        quantity = this.quantity
    )
}