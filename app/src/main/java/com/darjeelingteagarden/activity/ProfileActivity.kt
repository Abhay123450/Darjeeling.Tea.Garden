package com.darjeelingteagarden.activity

import android.app.ActivityManager
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityManagerCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityProfileBinding
import com.darjeelingteagarden.fragment.ProfileMainFragment
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.razorpay.Checkout
import org.json.JSONObject

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var toolbar: Toolbar

    private lateinit var logoutAlertDialogView: View
    //Logout Options
    private lateinit var btnLogoutThis: MaterialButton
    private lateinit var btnLogoutAll: MaterialButton
    private lateinit var btnCancelLogout: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        showUserInfo()

        //Setup Toolbar
        toolbar = findViewById(R.id.profileToolbar)
        setUpToolBar()

        //st up back button
        toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        binding.cardPersonalInfo.setOnClickListener {

            val intent = Intent(this@ProfileActivity, UserDetailsActivity::class.java)
            startActivity(intent)

        }

        binding.btnMyOrders.setOnClickListener {

            val intent = Intent(this@ProfileActivity, MyOrdersActivity::class.java)
            startActivity(intent)

        }

        binding.btnOrdersForMe.setOnClickListener {

            val intent = Intent(this@ProfileActivity, OrdersForMeActivity::class.java)
            startActivity(intent)

        }

        binding.btnSampleOrder.setOnClickListener {

            val intent = Intent(this@ProfileActivity, SampleOrderActivity::class.java)
            startActivity(intent)

        }

        binding.btnSampleOrderForMe.setOnClickListener {

            val intent = Intent(this@ProfileActivity, SampleOrdersForMeActivity::class.java)
            startActivity(intent)
        }

        binding.btnNews.setOnClickListener {

            val intent = Intent(this@ProfileActivity, NewsActivity::class.java)
            startActivity(intent)

        }

        binding.btnVideos.setOnClickListener {

            val intent = Intent(this@ProfileActivity, VideosActivity::class.java)
            startActivity(intent)

        }

        binding.btnMyDownline.setOnClickListener {

            val intent = Intent(this, MyDownlineActivity::class.java)
            startActivity(intent)

        }

        binding.btnAbout.setOnClickListener {

            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.btnFAQ.setOnClickListener {

            val intent = Intent(this, FaqActivity::class.java)
            startActivity(intent)

        }

        binding.btnLogout.setOnClickListener {

            logoutAlertDialogView = LayoutInflater
                .from(this)
                .inflate(R.layout.layout_logout, null, false)

            btnLogoutThis = logoutAlertDialogView.findViewById(R.id.btnLogoutThis)
            btnLogoutAll = logoutAlertDialogView.findViewById(R.id.btnLogoutAll)
            btnCancelLogout = logoutAlertDialogView.findViewById(R.id.btnCancelLogout)


            val dialog = MaterialAlertDialogBuilder(this@ProfileActivity)
                .setView(logoutAlertDialogView)
                .create()

            btnLogoutThis.setOnClickListener {
                logout(false)
                dialog.dismiss()
            }

            btnLogoutAll.setOnClickListener {
                logout(true)
                dialog.dismiss()
            }

            btnCancelLogout.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()

        }

    }

    private fun showUserInfo(){
        Log.i("user info ::: ", AppDataSingleton.getUserInfo.toString())
        val user = AppDataSingleton.getUserInfo
        binding.txtUserName.text = user.name
        binding.txtUserRole.text = user.role
        binding.txtUserEmail.text = user.email
        binding.txtUserId.text = user.userId
        binding.txtUserPhoneNumber.text = user.phoneNumber.toString()

        if (user.role == "Retailer"){
            binding.btnOrdersForMe.visibility = View.GONE
        }

        if (!user.role.equals("Admin", ignoreCase = true)){
            binding.btnSampleOrderForMe.visibility = View.GONE
        }
    }

    private fun setUpToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Profile"
    }

    private fun changeToolbarTitle(name: String){
        supportActionBar?.title = name
    }

    private fun logout(all: Boolean){

        if (!ConnectionManager().isOnline(this)){
            AppDataSingleton.noInternet(this)
        }

        binding.rlProgress.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(this)

        val url = if (all){
            "${getString(R.string.homeUrl)}api/v1/user/logoutAll"
        } else {
            "${getString(R.string.homeUrl)}api/v1/user/logout"
        }

        val jsonBody = JSONObject()
        jsonBody.put("deviceToken", getFcmToken())

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try{

                    val success = it.getBoolean("success")

                    if (success){
                        Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show()
                    }

                    val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    //clear cart
                    AppDataSingleton.clearCart(this)

                    //clear razorpay data
                    Checkout.clearUserData(this)

                    //open login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                    //finish all previous activities
                    finishAffinity()

                }
                catch(e: Exception){
                    val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    //clear cart
                    CartDataSingleton.clearCart(this)

                    //clear razorpay data
                    Checkout.clearUserData(this)

                    //open login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

//                    val service = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//                    service.clearApplicationUserData()

                    //finish all previous activities
                    finishAffinity()
                }
            },
            Response.ErrorListener {
                val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                //clear cart
                CartDataSingleton.clearCart(this)

                //clear razorpay data
                Checkout.clearUserData(this)

                //open login screen
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)

//                val service = getSystemService(ACTIVITY_SERVICE) as ActivityManager
//                service.clearApplicationUserData()

                //finish all previous activities
                finishAffinity()
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

    private fun getFcmToken(): String?{

        var token: String? = null

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get FCM registration token
            token = task.result
        })

        return token

    }
}