package com.darjeelingteagarden.util

import kotlin.math.abs

fun formatPaiseToRupees(paise: Int): String {
    val sign = if (paise < 0) "-" else ""
    val absPaise = abs(paise)

    val rupees = absPaise / 100
    val remainingPaise = absPaise % 100

    return if (remainingPaise == 0) {
        "$sign$rupees"
    } else {
        val paisePart = remainingPaise.toString().padStart(2, '0')
        "$sign$rupees.$paisePart"
    }
}