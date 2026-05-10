package com.darjeelingteagarden.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Address(
    @SerialName("_id")
    var addressId: String?,
    var name: String?,
    val phoneNumber: String,
    val alternatePhoneNumber: String,
    val addressLine1: String,
    val addressLine2: String,
    val landmark: String,
    val postalCode: String,
    val state: String,
    val city: String,
    var country: String? = "India",
    val isDefault: Boolean,
    val isSelected: Boolean
): Parcelable
