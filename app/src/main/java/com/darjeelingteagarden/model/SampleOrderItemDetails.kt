package com.darjeelingteagarden.model

data class SampleOrderItemDetails(
    val sampleId: String,
    val sampleName: String,
    val grade: String,
    val lot: String,
    val bagSize: String,
    val samplePrice: Double,
    val quantity: Int,
)
