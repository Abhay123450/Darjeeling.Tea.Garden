package com.darjeelingteagarden.util

import android.util.Patterns

class InputValidator {

    fun validatePhoneNumber(phoneNumber: Long): Boolean{

        if(phoneNumber < 1000000000 || phoneNumber > 9999999999){
            return false
        }

        return true
    }

    fun validateEmailAddress(email: String): Boolean{
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

    fun validateOTP(otp: Int): Boolean{
        if (otp < 100000 || otp > 999999) return false
        return true
    }


}