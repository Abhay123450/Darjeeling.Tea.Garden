package com.darjeelingteagarden.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityRegisterBinding
import com.darjeelingteagarden.databinding.ActivityResetPasswordBinding
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.util.InputValidator
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding

    var otpSent = false
    var otpVerified = false
    var passwordChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textInputEditTextPhoneNumber.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty() || !InputValidator().validatePhoneNumber(text.toString().toLong())){
                binding.textInputLayoutPhoneNumber.error = "Please enter valid phone number"
            }
            else {
                binding.textInputLayoutPhoneNumber.error = null
            }
        }

        binding.textInputEditTextOTP.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty() || !InputValidator().validateOTP(text.toString().toInt())){
                binding.textInputLayoutOTP.error = "Please enter valid OTP"
            }
            else{
                binding.textInputLayoutOTP.error = null
            }
        }

        binding.textInputEditTextNewPassword.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty() || text.length < 6){
                binding.textInputLayoutNewPassword.error = "Password must be at least 6 characters"
            }
            else{
                binding.textInputLayoutNewPassword.error = null
            }
        }

        binding.textInputEditTextRetypeNewPassword.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty() || text.toString() != binding.textInputEditTextNewPassword.text.toString()){
                binding.textInputLayoutRetypeNewPassword.error = "Password did not match"
            }
            else{
                binding.textInputLayoutRetypeNewPassword.error = null
            }

        }

        binding.btnSubmit.setOnClickListener {

            progressBarVisible()

            if (!ConnectionManager().isOnline(this@ResetPasswordActivity)){
                Toast.makeText(this@ResetPasswordActivity, "Internet Connection NOT available", Toast.LENGTH_LONG).show()
                buttonVisible()
                return@setOnClickListener
            }

            if (binding.textInputLayoutPhoneNumber.error != null){
                binding.txtHelpText.text = getString(R.string.please_enter_valid_phone_number)
                buttonVisible()
                return@setOnClickListener
            }

            if (!otpSent){
                sendOTP(binding.textInputEditTextPhoneNumber.text.toString())
                return@setOnClickListener
            }

            if (binding.textInputLayoutOTP.error != null){
                binding.txtHelpText.text = getString(R.string.please_enter_valid_otp)
                buttonVisible()
                return@setOnClickListener
            }

            if (!otpVerified){
                verifyOTP(
                    binding.textInputEditTextPhoneNumber.text.toString(),
                    binding.textInputEditTextOTP.text.toString()
                )
                return@setOnClickListener
            }

            if (!(binding.textInputLayoutNewPassword.error == null && binding.textInputLayoutRetypeNewPassword.error == null)){
                binding.txtHelpText.text = getString(R.string.please_enter_valid_password)
                buttonVisible()
                return@setOnClickListener
            }

            if (!passwordChanged){
                resetPassword(
                    binding.textInputEditTextPhoneNumber.text.toString(),
                    binding.textInputEditTextOTP.text.toString(),
                    binding.textInputEditTextNewPassword.text.toString()
                )
                return@setOnClickListener
            }

        }

    }

    private fun buttonVisible(){
        binding.btnSubmit.visibility = View.VISIBLE
        binding.progressBarButton.visibility = View.GONE
    }

    private fun progressBarVisible(){
        binding.btnSubmit.visibility = View.GONE
        binding.progressBarButton.visibility = View.VISIBLE
    }

    private fun sendOTP(phoneNumber: String){

        progressBarVisible()

        val url = getString(R.string.homeUrl) + "api/v1/user/forgotPassword"
        val queue = Volley.newRequestQueue(this@ResetPasswordActivity)

        val jsonParams = JSONObject()
        jsonParams.put("phoneNumber", phoneNumber)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")
                    if (success){
                        otpSent = true
                        binding.txtHelpText.text = it.getString("message")

                        binding.relativeLayoutEnterOTP.visibility = View.VISIBLE

                        binding.btnSubmit.text = getString(R.string.verify_otp)
                        buttonVisible()
                    }
                    else{
                        binding.txtHelpText.text = it.getString("message")
                        buttonVisible()
                    }

                }
                catch (e: Exception){
                    binding.txtHelpText.text = getString(R.string.an_error_occurred)
                    buttonVisible()
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                binding.txtHelpText.text = response.getString("message")
                buttonVisible()

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun verifyOTP(phoneNumber: String, otp: String){

        progressBarVisible()

        val url = getString(R.string.homeUrl) + "api/v1/user/verifyOTP"
        val queue = Volley.newRequestQueue(this@ResetPasswordActivity)

        val jsonParams = JSONObject()
        jsonParams.put("phoneNumber", phoneNumber)
        jsonParams.put("otp", otp)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")
                    if (success){
                        otpVerified = true

                        binding.textInputLayoutNewPassword.visibility = View.VISIBLE
                        binding.textInputLayoutRetypeNewPassword.visibility = View.VISIBLE

                        binding.txtHelpText.text = it.getString("message")
                        binding.btnSubmit.text = getString(R.string.submit)
                        buttonVisible()
                    }
                    else{
                        binding.txtHelpText.text = it.getString("message")
                        buttonVisible()
                    }

                }
                catch (e: Exception){
                    binding.txtHelpText.text = getString(R.string.an_error_occurred)
                    buttonVisible()
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                binding.txtHelpText.text = response.getString("message")
                buttonVisible()

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun resetPassword(phoneNumber: String, otp: String, newPassword: String){

        progressBarVisible()

        val url = getString(R.string.homeUrl) + "api/v1/user/resetPassword"
        val queue = Volley.newRequestQueue(this@ResetPasswordActivity)

        val jsonParams = JSONObject()
        jsonParams.put("phoneNumber", phoneNumber)
        jsonParams.put("otp", otp)
        jsonParams.put("newPassword", newPassword)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")
                    if (success){
                        passwordChanged = true
                        binding.txtHelpText.text = it.getString("message")
                        binding.btnSubmit.text = getString(R.string.reset_password)

                        startActivity(
                            Intent(
                                this@ResetPasswordActivity,
                                LoginActivity::class.java
                            )
                        )

                        finishAffinity()

                    }
                    else{
                        binding.txtHelpText.text = it.getString("message")
                        buttonVisible()
                    }

                }
                catch (e: Exception){
                    binding.txtHelpText.text = getString(R.string.an_error_occurred)
                    buttonVisible()
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                binding.txtHelpText.text = response.getString("message")
                buttonVisible()

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

}