package com.darjeelingteagarden.features.packagedTea.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

enum class ArticleType {
    @SerializedName("pouch")
    POUCH,
    @SerializedName("jar")
    JAR,
    @SerializedName("box")
    BOX
}

@Parcelize
data class Weight(
    val unit: String,
    val quantity: Number
): Parcelable

@Parcelize
data class Price(
    val mrpPerArticle: Int // in paise
): Parcelable

@Parcelize
data class PackagedTea(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val featuredImage: String,
    val images: MutableList<String>,
    val description: String,
    val category: String,
    val articleType: ArticleType,
    val articleWeight: Weight,
    val corrugatedBoxWeight: Weight?, // not for pouch
    val lariSize: Int?, // number of pouch in a lari, only for pouch
    val bundleSize: Int?, // number of lari in a bundle, only for pouch
    val bagSize: Int,
    val brandName: String,
    val mrp: Int, // in paise
    val samplePrice: Int, // in paise
    val searchTerms: String?,
    val priority: Int,
    val isListed: Boolean,
    val createdAt: String?,
    val updatedAt: String?
): Parcelable
