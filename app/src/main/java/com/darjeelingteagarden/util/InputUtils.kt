package com.darjeelingteagarden.util

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.google.android.material.textfield.TextInputLayout

fun TextInputLayout.markRequired(label: String) {

    val hintText = "$label *"
    val spannable = SpannableString(hintText)

    spannable.setSpan(
        ForegroundColorSpan(Color.RED),
        hintText.length - 1,
        hintText.length,
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    hint = spannable
}

