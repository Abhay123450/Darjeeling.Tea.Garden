package com.darjeelingteagarden.model

import android.location.Location

data class LocationInfo(
    val currentLocation: Location?,
    val latitude: Double,
    val longitude: Double
)
