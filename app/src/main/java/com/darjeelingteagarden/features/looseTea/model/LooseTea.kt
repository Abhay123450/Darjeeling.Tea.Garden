package com.darjeelingteagarden.features.looseTea.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Weight(
    val unit: String,
    val quantity: Number
): Parcelable

@Parcelize
@Serializable
data class QualityRating(
    val infusion: Number,
    val color: Number,
    val thickness: Number,
    val aroma: Number,
    val strongness: Number,
    val briskness: Number
): Parcelable

@Parcelize
@Serializable
data class Price(
    val originalPrice: Int, // in paise
    val sellingPrice: Int, // in paise
    val discountAmount: Int, // in paise
    val discountPercentage: Number
): Parcelable

@Parcelize
@Serializable
data class LooseTea(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val grade: String,
    val category: String,
    val description: String,
    val featuredImage: String,
    val images: MutableList<String>,
    val priority: Int,
    val lotNumber: String?,
    val lotSize: Weight?,
    val origin: String,
    val qualityRating: QualityRating,
    val appearance: String,
    val standard: String,
    val bagWeight: Weight,
    val brand: String,
    val pricePerKg: Price,
    val pricePerBag: Price,
    val hasDiscount: Boolean,
    val samplePrice: Int,
    val sampleSize: Weight
): Parcelable
