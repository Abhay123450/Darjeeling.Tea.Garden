package com.darjeelingteagarden.features.packagedTea.model

data class PackagedTeaCatalog(
    val category: String,
    val items: MutableList<PackagedTea>
)
