package com.darjeelingteagarden.util

fun normalizeAndCapitalize(input: String): String {
    return input
        .trim()
        .split(Regex("\\s+")) // handles multiple spaces, tabs, etc.
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }
}