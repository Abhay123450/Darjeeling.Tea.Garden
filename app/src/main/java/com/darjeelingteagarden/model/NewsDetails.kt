package com.darjeelingteagarden.model

data class NewsDetails(
    val newsId: String,
    val newsTitle: String,
    val newsDate: String,
    val newsContent: String,
    val newsImage: String,
    val ytVideo: String?
)
