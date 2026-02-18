package com.darjeelingteagarden.repository

import android.content.Context
import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

class VolleySingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleySingleton(context).also { INSTANCE = it }
            }

        fun extractVolleyErrorResponseBody(volleyError: VolleyError): String?{
            var body: String?
            val networkResponse: NetworkResponse? = volleyError.networkResponse

            if (networkResponse != null && networkResponse.data != null) {
                try {
                    // Attempt to decode the byte array with UTF-8 encoding
                    body = String(networkResponse.data, charset(StandardCharsets.UTF_8.name()))


                    // You can also get the status code
                    val statusCode = networkResponse.statusCode
                    Log.e(
                        "Volley Error",
                        "Status Code: $statusCode, Response Body: $body"
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    // Handle the unlikely case where UTF-8 is not supported
                    body = String(networkResponse.data) // Fallback to default
                }
                return body
            } else {
                // Handle cases where networkResponse or data might be null (e.g., NetworkError, TimeoutError, ParseError)
                Log.e("Volley Error", "Error without network response: $volleyError")
                return null
            }
        }
    }

    val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
}