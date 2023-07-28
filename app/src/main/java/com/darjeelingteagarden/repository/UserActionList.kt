package com.darjeelingteagarden.repository

class UserActionList {
    fun getAllUserActions(): ArrayList<String>{

        val userActionList = arrayListOf<String>(
            "My Orders",
            "Orders For Me",
            "Request For Sample",
            "Track Sample Order",
            "Sample Order History",
            "My Downline",
            "My Stock",
            "Offers and Coupons",
        )

        return userActionList
    }
}