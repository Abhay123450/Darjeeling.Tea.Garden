package com.darjeelingteagarden.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val addressId: String,
    val name: String,
    val phoneNumber: String,
    val alternatePhoneNumber: String,
    val addressLine1: String,
    val addressLine2: String,
    val landmark: String,
    val postalCode: String,
    val state: String,
    val city: String,
    val country: String,
    val isDefault: Boolean,
    val isSelected: Boolean
): Parcelable
