package com.darjeelingteagarden.util

import android.util.Patterns

class InputValidator {

    fun validatePhoneNumber(phoneNumber: String): Boolean{

        if(phoneNumber.matches(Regex("\\d{10}"))){
            return true
        }
        return false

    }

    fun validateEmailAddress(email: String?): Boolean{
        if (email == null) return false
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateGSTINNumber(gstin: String): Boolean{

        if (gstin.length != 15) return false

        return true
    }

    fun validatePincode(pincode: Int): Boolean{
        if(pincode < 100000 || pincode > 999999) return false
        return true
    }

    fun validateOTP(otp: String): Boolean{
        val otpNum = otp.toIntOrNull()
        if (otpNum != null && otpNum >= 100000 && otpNum <= 999999){
            return true
        }
        return false
    }


}