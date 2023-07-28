package com.darjeelingteagarden.model

data class User(
    val userId: String,
    val name: String,
    val role: String,
    val phoneNumber: Long,
    val email: String
)
