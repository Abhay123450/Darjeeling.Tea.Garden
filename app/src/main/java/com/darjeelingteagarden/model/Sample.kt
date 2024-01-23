package com.darjeelingteagarden.model

data class Sample(
    val sampleId: String,
    val sampleName: String,
    val samplePrice: Int,
    val sampleLot: String,
    val sampleBagSize: Int,
    val sampleGrade: String,
    val sampleImageUrl: String
)
