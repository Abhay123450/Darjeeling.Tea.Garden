package com.darjeelingteagarden.features.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

object AppAnalytics {
    // Gets the global FirebaseAnalytics instance
    private val analytics: FirebaseAnalytics = Firebase.analytics

    /**
     * Logs a standard Screen View event.
     * Useful if you want manual control over screen tracking (especially in Jetpack Compose).
     */
    fun logScreenView(screenName: String, screenClass: String = "MainActivity") {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    /**
     * Logs a generic button click.
     */
    fun logButtonClick(buttonId: String, buttonName: String) {
        analytics.logEvent("button_click") {
            param("button_id", buttonId)
            param("button_name", buttonName)
        }
    }

    /**
     * Logs a highly customized event with multiple parameters.
     */
    fun logCustomEvent(eventName: String, params: Map<String, String>? = null) {
        analytics.logEvent(eventName) {
            params?.forEach { (key, value) ->
                param(key, value)
            }
        }
    }

    /**
     * Sets a User Property.
     * Use this to categorize users (e.g., "premium_user", "dark_mode_enabled").
     */
    fun setUserProperty(propertyName: String, propertyValue: String) {
        analytics.setUserProperty(propertyName, propertyValue)
    }

    /**
     * Sets the User ID for tracking cross-platform or cross-device sessions.
     * Call this after a user successfully logs in.
     */
    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }

}