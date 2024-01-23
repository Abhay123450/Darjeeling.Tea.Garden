package com.darjeelingteagarden.model

data class UserDetails(
    val _id: String,
    val userId: String,
    val name: String,
    val role: String,
    val phoneNumber: Long,
    val email: String,
    val addressLineOne: String,
    val addressLineTwo: String,
    val pincode: Int,
    val city: String,
    val state: String,
    val firmName: String,
    val gstin: String,
    val inviteCode: String
)
