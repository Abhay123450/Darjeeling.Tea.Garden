package com.darjeelingteagarden.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.User

object UserDataSingleton {

    fun getUserDetails(context: Context){

        Log.i("user details", "starting request")
        Log.i("user details", "auth token: ${AppDataSingleton.getAuthToken}")

        val queue = Volley.newRequestQueue(context)

        val url = context.getString(R.string.homeUrl) + "api/v1/user"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        Log.i("user details", "request success $it")

                        val data = it.getJSONObject("data")

                        val user = User(
                            data.optString("userId"),
                            data.getString("name"),
                            data.optString("role"),
                            data.getLong("phoneNumber"),
                            data.optString("email"),
                        )

                        Log.i("userInfo LauncherActivity", "user is $user")
                        AppDataSingleton.setUserInfo(user)

                    }
                    else{
                        Toast.makeText(
                            context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(
                        context,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                }

            },
            Response.ErrorListener {

                val response = VolleySingleton.extractVolleyErrorResponseBody(it)
                Log.i("Volley error response", response.toString())

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

}