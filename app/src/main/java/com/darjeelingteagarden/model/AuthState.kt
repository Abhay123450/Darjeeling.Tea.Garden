package com.darjeelingteagarden.model

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object LoginWithOtp : AuthState() // User exists, move to OTP
    object VerifyOtp: AuthState() // User doesn't exist, verify otp and create account
    object NewUser : AuthState()    // Complete registration by asking details - Name etc
    object Success : AuthState()    // Finished!
    data class Error(val message: String) : AuthState()
}