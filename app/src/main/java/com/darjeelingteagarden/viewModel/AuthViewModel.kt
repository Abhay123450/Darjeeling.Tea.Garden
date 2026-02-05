package com.darjeelingteagarden.viewModel

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getString
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.AuthState
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.VolleySingleton
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.util.Date


class AuthViewModel : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    fun discoverAuth(context: Context, phoneNumber: String){

        _authState.value = AuthState.Loading

        val url = "${getString(context, R.string.homeUrl)}api/v1/auth/discover"
        val jsonObj = JSONObject()
        jsonObj.put("phoneNumber", phoneNumber)

        Log.i("Auth", "phone number $phoneNumber")

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObj,
            Response.Listener{

                try {

                    val success = it.getBoolean("success")

                    Log.i("Auth", "success: $success")

                    if (success){

                        Log.i("Auth", "it: $it")

                        val isRegistered = it.getBoolean("isUserRegistered")

                        Log.i("Auth", "isRegistered: $isRegistered")

                        if (isRegistered){
                            val isActive = it.getBoolean("isUserActive")
                            val isPhoneNumberVerified = it.getBoolean("isPhoneNumberVerified")
                            if (isActive && isPhoneNumberVerified){
                                _authState.value = AuthState.LoginWithOtp
                            }
                            else if (!isPhoneNumberVerified){
                                _authState.value = AuthState.VerifyOtp
                            }
                            else if (!isActive){
                                _authState.value = AuthState.Idle
                                Toast.makeText(context, "Your account is not active", Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{

                            _authState.value = AuthState.VerifyOtp

                        }

                    }
                    else{
                        val message = it.getString("message")
                        _authState.value = AuthState.Idle
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: Exception){

                    e.printStackTrace()
                    _authState.value = AuthState.Idle
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()

                }

            },
            Response.ErrorListener{
                _authState.value = AuthState.Idle
                val body = extractVolleyErrorResponseBody(it)
                val message = body ?: "An error occurred"
                Log.i("volley response", body.toString())
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

        ){
            override fun getHeaders(): Map<String?, String?>? {
                val headers = HashMap<String?, String?>()
                headers["Content-Type"] = "application/json"
                return super.getHeaders()
            }
        }

        val queue = VolleySingleton.getInstance(context).requestQueue
        queue.add(jsonObjectRequest)
    }

    fun verifyOtpAndLogin(context: Context, phoneNumber: String, otp: String){

        _authState.value = AuthState.Loading

        val url = "${getString(context, R.string.homeUrl)}api/v1/auth/login-with-otp"
        val jsonObj = JSONObject()
        jsonObj.put("phoneNumber", phoneNumber)
        jsonObj.put("otp", otp)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObj,
            Response.Listener{

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        _authState.value = AuthState.Success
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()

                        val token = it.getString("token")
                        saveAuthToken(context, token)
                        AppDataSingleton.setAuthToken(token)

                    }
                    else{
                        val message = it.getString("message")
                        _authState.value = AuthState.LoginWithOtp

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: Exception){

                    e.printStackTrace()
                    _authState.value = AuthState.LoginWithOtp
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()

                }

            },
            Response.ErrorListener{
                _authState.value = AuthState.LoginWithOtp
                val body = extractVolleyErrorResponseBody(it)
                val message = body ?: "An error occurred"
                Log.i("volley response", body.toString())
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

        ){
            override fun getHeaders(): Map<String?, String?>? {
                val headers = HashMap<String?, String?>()
                headers["Content-Type"] = "application/json"
                return super.getHeaders()
            }
        }

        val queue = VolleySingleton.getInstance(context).requestQueue
        queue.add(jsonObjectRequest)
    }

    fun verifyOtp(context: Context, phoneNumber: String, otp: String){

        _authState.value = AuthState.Loading

        val url = "${getString(context, R.string.homeUrl)}api/v1/auth/verify-otp"
        val jsonObj = JSONObject()
        jsonObj.put("phoneNumber", phoneNumber)
        jsonObj.put("otp", otp)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObj,
            Response.Listener{

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        _authState.value = AuthState.NewUser

                    }
                    else{
                        val message = it.getString("message")
                        _authState.value = AuthState.VerifyOtp

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: Exception){

                    e.printStackTrace()
                    _authState.value = AuthState.VerifyOtp
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()

                }

            },
            Response.ErrorListener{
                _authState.value = AuthState.VerifyOtp
                val body = extractVolleyErrorResponseBody(it)
                val message = body ?: "An error occurred"
                Log.i("volley response", body.toString())
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

        ){
            override fun getHeaders(): Map<String?, String?>? {
                val headers = HashMap<String?, String?>()
                headers["Content-Type"] = "application/json"
                return super.getHeaders()
            }
        }

        val queue = VolleySingleton.getInstance(context).requestQueue
        queue.add(jsonObjectRequest)

    }

    fun registerUserWithOtp(context: Context, name: String, businessRevenue: String, phoneNumber: String, otp: String){

        _authState.value = AuthState.Loading

        val url = "${getString(context, R.string.homeUrl)}api/v1/auth/register-with-otp"
        val jsonObj = JSONObject()
        jsonObj.put("phoneNumber", phoneNumber)
        jsonObj.put("otp", otp)
        jsonObj.put("name", name)
        jsonObj.put("businessRevenue", businessRevenue)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObj,
            Response.Listener{

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        _authState.value = AuthState.Success

                        val token = it.getString("token")

                        saveAuthToken(context, token)
                        AppDataSingleton.setAuthToken(token)

                    }
                    else{
                        val message = it.getString("message")
                        _authState.value = AuthState.NewUser

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: Exception){

                    e.printStackTrace()
                    _authState.value = AuthState.NewUser
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()

                }

            },
            Response.ErrorListener{
                _authState.value = AuthState.NewUser
                val body = extractVolleyErrorResponseBody(it)
                val message = body ?: "An error occurred"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

        ){
            override fun getHeaders(): Map<String?, String?>? {
                val headers = HashMap<String?, String?>()
                headers["Content-Type"] = "application/json"
                return super.getHeaders()
            }
        }

        val queue = VolleySingleton.getInstance(context).requestQueue
        queue.add(jsonObjectRequest)

    }

    private fun saveAuthToken(context: Context, token: String){
        val sharedPreferences = context.getSharedPreferences(getString(context, R.string.shared_preference_name), MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("isLoggedIn", true)
            putString("token", token)
            putLong("date", Date().time)
        }
    }

    private fun extractVolleyErrorResponseBody(volleyError: VolleyError): String?{
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