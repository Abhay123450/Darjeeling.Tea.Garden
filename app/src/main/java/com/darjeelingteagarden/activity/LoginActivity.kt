package com.darjeelingteagarden.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityLoginBinding
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.lang.Exception
import java.util.Date

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //val loginUrl = getString(R.string.loginUrl)
    var registered = false
    lateinit var userId: String
    lateinit var password: String

    private var rememberMe = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        val loginUrl = getString(R.string.loginUrl)

        if (intent != null){
            registered = intent.getBooleanExtra("registered", false)
            userId = intent.getStringExtra("userId").toString()
            if (userId != "null"){
                MaterialAlertDialogBuilder(this@LoginActivity)
                    .setTitle("Registered Successfully")
                    .setMessage("Your User Id is $userId. \nRemember it for logging in to your account.")
                    .setNeutralButton("OK"){dialogue, which ->
                    }
                    .show()

                binding.textInputEditTextUserId.setText(userId)
            }

        }
        else{
            val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
            val userId: String? = sharedPreferences.getString("userId", null)
            val password: String? = sharedPreferences.getString("password", null)
            if (userId != null && password != null){
                binding.textInputEditTextUserId.setText(userId)
                binding.textInputEditTextPassword.setText(password)
            }
        }

        binding.rememberMe.setOnCheckedChangeListener { buttonView, isChecked ->
            rememberMe = isChecked
        }

        binding.textInputEditTextUserId.doOnTextChanged { text, start, before, count ->

            if (text!!.isEmpty()){
                binding.textInputLayoutUserId.error = "User Id is required"
            }
            else if (text.length < 7){
                binding.textInputLayoutUserId.error = "Enter a valid User Id"
            }
            else{
                binding.textInputLayoutUserId.error = null
            }

        }

        binding.textInputEditTextPassword.doOnTextChanged { text, start, before, count ->

            if(text!!.isEmpty()){
                binding.textInputLayoutPassword.error = "Password is required"
            }
            else {
                binding.textInputLayoutPassword.error = null
            }
        }

        binding.btnForgotPassword.setOnClickListener {

            val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java)
            startActivity(intent)

        }

        binding.btnRegisterAsBusinessPartner.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {

            binding.btnLogin.visibility = View.GONE
            binding.progressBarLogin.visibility = View.VISIBLE

            userId = binding.textInputEditTextUserId.text.toString()
            password = binding.textInputEditTextPassword.text.toString()

            if (!(binding.textInputLayoutUserId.error == null && binding.textInputLayoutPassword.error == null)){
                Toast.makeText(this@LoginActivity, "Please enter valid credentials", Toast.LENGTH_LONG).show()
                binding.btnLogin.visibility = View.VISIBLE
                binding.progressBarLogin.visibility = View.INVISIBLE
            }else{

                if (ConnectionManager().isOnline(this@LoginActivity)){

                    //Toast.makeText(this@LoginActivity, "Internet Connection available", Toast.LENGTH_LONG).show()

                    val queue = Volley.newRequestQueue(this@LoginActivity)

                    val jsonParams = JSONObject()
                    jsonParams.put("userId", userId)
                    jsonParams.put("password", password)

                    val jsonObjectRequest = object : JsonObjectRequest(Method.POST, loginUrl, jsonParams,
                        Response.Listener {

                            try {

                                val success = it.getBoolean("success")
                                if (success){

                                    val user = it.getJSONObject("user")

                                    val accountVerified =
                                        user.getBoolean("phoneNumberVerified") &&
                                                user.getBoolean("emailVerified")

                                    if(!accountVerified){

//                                        val user = it.getJSONObject("user")

                                        val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                                        intent.putExtra("registered", true)
                                        intent.putExtra("phoneNumber", user.getLong("phoneNumber"))
                                        intent.putExtra("email", user.getString("email"))
                                        intent.putExtra("userId", user.getString("userId"))
                                        startActivity(intent)

                                        return@Listener
                                    }



                                    binding.txtMessage.text = it.getString("message")
                                    Log.i("response", it.getJSONObject("user").toString())

                                    val token = it.getString("token")
//                                    val user = it.getJSONObject("user")
                                    val userRole = user.getString("role")

                                    val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isLoggedIn", true)
                                    editor.putString("token", token)
                                    editor.putString("role", userRole)
                                    editor.putLong("date", Date().time)
                                    if (rememberMe){
                                        editor.putString("userId", userId)
                                        editor.putString("password", password)
                                    }
                                    editor.apply()

                                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()

                                    if (intent.getBooleanExtra("resume", false)){
                                        AppDataSingleton.setAuthToken(token)
                                        finish()
                                        return@Listener
                                    }

                                    val intent = Intent(this@LoginActivity, LauncherActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()

                                } else {

                                    binding.txtMessage.text = it.getString("message")
                                    binding.btnLogin.visibility = View.VISIBLE
                                    binding.progressBarLogin.visibility = View.INVISIBLE

                                }

                            } catch (e: Exception){
                                Toast.makeText(this@LoginActivity, "try catch exception $e", Toast.LENGTH_LONG).show()
                                binding.btnLogin.visibility = View.VISIBLE
                                binding.progressBarLogin.visibility = View.INVISIBLE
                            }


                        },
                        Response.ErrorListener {

                            val response = JSONObject(String(it.networkResponse.data))
                            binding.txtMessage.text = response.getString("message")
                            binding.txtMessage.setTextColor(Color.RED)
                            Log.i("Response error data: ", String(it.networkResponse.data))
                            Toast.makeText(this@LoginActivity, "An error occurred: ${String(it.networkResponse.data)}", Toast.LENGTH_LONG).show()
                            binding.btnLogin.visibility = View.VISIBLE
                            binding.progressBarLogin.visibility = View.INVISIBLE

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
                else{
                    Toast.makeText(this@LoginActivity, "Internet Connection NOT available", Toast.LENGTH_LONG).show()
                    binding.btnLogin.visibility = View.VISIBLE
                    binding.progressBarLogin.visibility = View.INVISIBLE
                }

            }

        }

    }
}