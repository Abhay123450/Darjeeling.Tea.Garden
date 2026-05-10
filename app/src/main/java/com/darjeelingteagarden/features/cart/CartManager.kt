package com.darjeelingteagarden.features.cart

import androidx.lifecycle.MutableLiveData
import com.darjeelingteagarden.features.cart.model.CartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaCartItem
import com.darjeelingteagarden.features.cart.model.LooseTeaSampleCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaCartItem
import com.darjeelingteagarden.features.cart.model.PackagedTeaSampleCartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object CartManager {

    private val _cart =
        MutableStateFlow<Map<String, CartItem>>(emptyMap())

    val cart: StateFlow<Map<String, CartItem>> = _cart

    val cartCount = MutableLiveData(0)

    private fun update(newMap: Map<String, CartItem>) {
        _cart.value = newMap
        cartCount.postValue(newMap.size)
    }

    // ----------------------------
    // ADD ITEM (generic)
    // ----------------------------
    fun add(item: CartItem) {
        val current = _cart.value.toMutableMap()

        val key = item.uniqueCartId
        val existing = current[key]

        val updatedItem = if (existing != null) {
            increaseQuantity(existing)
        } else {
            item.copyWithQuantity(1)
        }

        current[key] = updatedItem
        update(current)
    }

    // ----------------------------
    // REMOVE ITEM (generic)
    // ----------------------------
    fun remove(item: CartItem) {
        val current = _cart.value.toMutableMap()
        val key = item.uniqueCartId

        val existing = current[key] ?: return

        if (existing.quantity > 1) {
            current[key] = decreaseQuantity(existing)
        } else {
            current.remove(key)
        }

        update(current)
    }

    // ----------------------------
    // INCREASE / DECREASE HELPERS
    // ----------------------------
    private fun increaseQuantity(item: CartItem): CartItem {
        return when (item) {
            is LooseTeaCartItem ->
                item.copy(quantity = item.quantity + 1)

            is LooseTeaSampleCartItem ->
                item.copy(quantity = item.quantity + 1)

            is PackagedTeaCartItem ->
                item.copy(quantity = item.quantity + 1)

            is PackagedTeaSampleCartItem ->
                item.copy(quantity = item.quantity + 1)
        }
    }

    private fun decreaseQuantity(item: CartItem): CartItem {
        return when (item) {
            is LooseTeaCartItem ->
                item.copy(quantity = item.quantity - 1)

            is LooseTeaSampleCartItem ->
                item.copy(quantity = item.quantity - 1)

            is PackagedTeaCartItem ->
                item.copy(quantity = item.quantity - 1)

            is PackagedTeaSampleCartItem ->
                item.copy(quantity = item.quantity - 1)
        }
    }

    // ----------------------------
    // INITIAL ITEM CREATION HELPERS
    // ----------------------------
    private fun CartItem.copyWithQuantity(qty: Int): CartItem {
        return when (this) {
            is LooseTeaCartItem -> this.copy(quantity = qty)
            is LooseTeaSampleCartItem -> this.copy(quantity = qty)
            is PackagedTeaCartItem -> this.copy(quantity = qty)
            is PackagedTeaSampleCartItem -> this.copy(quantity = qty)
        }
    }

    // ----------------------------
    // OPTIONAL: CLEAR CART
    // ----------------------------
    fun clear() {
        _cart.value = emptyMap()
    }
}