package com.darjeelingteagarden.activity

import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.User
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.viewModel.AppDataViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class LauncherActivity : AppCompatActivity() {

    private lateinit var mAppDataViewModel: AppDataViewModel

    private lateinit var sharedPreferences: SharedPreferences

    private var token: String? = ""
    private var isLoggedIn = false
    private var lastLoginTime: Long = 0

    var itemListLoaded = false
    var userInfoLoaded = false
    var openedActivity = false
    var cartListLoaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAppDataViewModel = ViewModelProvider(this)[AppDataViewModel::class.java]

        startup()

//        checkForUpdates()
//        Handler(Looper.getMainLooper()).postDelayed({
//            startAnotherActivity()
//        }, 2000)
    }

    private fun startup(){

        if (ConnectionManager().isOnline(this@LauncherActivity)){
            login()
        }
        else{
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this@LauncherActivity)
                .setTitle("No Internet")
                .setMessage("Internet connection not available")
                .setCancelable(false)
                .setPositiveButton("Retry"){dialog, int ->
                    startup()
                }
                .setNegativeButton("EXIT"){dialog, int ->
                    this.finishAffinity()
                }

            materialAlertDialogBuilder.show()

        }

    }

    private fun login(){

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        token = sharedPreferences.getString("token", null).toString()
        lastLoginTime = sharedPreferences.getLong("date", 0)

        val lastLogin = Date().time - lastLoginTime

        Log.i("auth token is === ", token.toString())
        Log.i("last login time is === ", lastLoginTime.toString())
        Log.i("last login is === ", lastLogin.toString())

        if (isLoggedIn && token != "null"){

            if (lastLogin > 1000 * 60 * 60 * 24 * 14){ // 14 days
                isLoggedIn = false
                startAnotherActivity()
            }
            else{
                AppDataSingleton.setAuthToken(token.toString())
                Log.i("auth token is === ", AppDataSingleton.getAuthToken)

                getUserDetails()
                getStoreItems()
//            getCartItems()
            }

        }
        else{

            startAnotherActivity()

        }

    }

    private fun startAnotherActivity(){

        val intent: Intent = if (isLoggedIn && token != null && itemListLoaded){
            Intent(this@LauncherActivity, MainActivity::class.java)
        } else {
            Intent(this@LauncherActivity, LoginActivity::class.java)
        }

        Log.i("before start activity", intent.toString())
        Log.i("isLoggedIn", isLoggedIn.toString())
        Log.i("token", intent.toString())
        startActivity(intent)
        println("after start activity")
        finish()

    }

    private fun getUserDetails(){

        val queue = Volley.newRequestQueue(this@LauncherActivity)

        val url = getString(R.string.homeUrl) + "api/v1/user"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONObject("data")

                        val user = User(
                            data.getString("userId"),
                            data.getString("name"),
                            data.getString("role"),
                            data.getLong("phoneNumber"),
                            data.getString("email"),
                        )

                        println("user is $user")
                        AppDataSingleton.setUserInfo(user)

                        userInfoLoaded = true

                        if (userInfoLoaded && itemListLoaded && !openedActivity){
                            openedActivity = true
                            startAnotherActivity()
                        }

                    }
                    else{
                        Toast.makeText(
                            this@LauncherActivity,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

//                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        this@LauncherActivity,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
//                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                Toast.makeText(
                    this@LauncherActivity,"An error occurred", Toast.LENGTH_LONG
                ).show()
//                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")

//                if (!isLoggedIn){
//                    val intent = Intent(this@LauncherActivity, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        this@LauncherActivity,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
////                    requireActivity().finish()
//                }
//                Log.i("Volley error response", response.toString())
//                MaterialAlertDialogBuilder(this@LauncherActivity)
//                    .setTitle("Message")
//                    .setMessage(response.getString("message").toString())
//                    .setNeutralButton("OK") { _, _ -> }
//                    .show()

//                storeSwipeRefreshLayout.isRefreshing = false

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

    private fun getCartItems(){

        val queue = Volley.newRequestQueue(this@LauncherActivity)

        val url = getString(R.string.homeUrl) + "api/v1/user/cart"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONObject("data").getJSONArray("cart")

                        Log.i("cart data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                this@LauncherActivity,"Cart is empty", Toast.LENGTH_LONG
                            ).show()
                        }
                        else{

                            for (i in 0 until data.length()){
                                val cartInfo = data.getJSONObject(i)
                                val cartObject = Cart(
                                    cartInfo.getString("productId"),
                                    "name of product",
                                    999,
//                                cartInfo.getString("name"),
//                                cartInfo.getJSONObject("productId").getInt("discountedPrice"),
                                    cartInfo.getInt("quantity")
                                )

//                            cartList.add(cartObject)
//                            mAppDataViewModel.cartItemList.add(cartObject)
                                AppDataSingleton.addCartItem(cartObject)

                            }

                            cartListLoaded = true

                        }

                        if (cartListLoaded && itemListLoaded){
                            startAnotherActivity()
                        }

                    }
                    else{
                        Toast.makeText(
                            this@LauncherActivity,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

//                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        this@LauncherActivity,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
//                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                val isLoggedIn = response.getBoolean("isLoggedIn")

                if (!isLoggedIn){
                    val intent = Intent(this@LauncherActivity, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(
                        this@LauncherActivity,"Please login to continue", Toast.LENGTH_LONG
                    ).show()
//                    requireActivity().finish()
                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(this@LauncherActivity)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()

//                storeSwipeRefreshLayout.isRefreshing = false

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

    private fun getStoreItems(){

        val queue = Volley.newRequestQueue(this@LauncherActivity)

        val url = getString(R.string.homeUrl) + "api/v1/products"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")
                        Log.i("store data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                this@LauncherActivity,"No product found", Toast.LENGTH_LONG
                            ).show()
                        }
                        else{

                            for (i in 0 until data.length()){
                                val productInfo = data.getJSONObject(i)
                                val productObject = Product(
                                    productInfo.getString("_id"),
                                    productInfo.getString("name"),
                                    productInfo.getInt("originalPrice"),
                                    productInfo.getInt("discountedPrice"),
                                    productInfo.getString("grade"),
                                    productInfo.getLong("lotNumber"),
                                    productInfo.getInt("bagSize"),
                                    productInfo.getString("mainImage")
                                )
//                            productList.add(productObject)
                                AppDataSingleton.addStoreItem(productObject)

                            }

                            itemListLoaded = true

                        }


//                        Log.i("after loading store:", mAppDataViewModel.storeItemList.toString())

                        if (itemListLoaded && userInfoLoaded && !openedActivity){
                            openedActivity = true
                            startAnotherActivity()
                        }
//                        isProductListReceived = true
//                        mStoreItemsViewModel.itemList = productList
//                        if (isProductListReceived && isOnCreateViewCompleted){
//                            populateRecyclerView()
//                        }
                    }
                    else{
                        Toast.makeText(
                            this@LauncherActivity,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

//                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        this@LauncherActivity,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
//                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                Toast.makeText(
                    this@LauncherActivity,"An error occurred: ", Toast.LENGTH_LONG
                ).show()

//                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")
//
//                if (!isLoggedIn){
//                    val intent = Intent(this@LauncherActivity, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        this@LauncherActivity,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
////                    requireActivity().finish()
//                }
//                Log.i("Volley error response", response.toString())
//                MaterialAlertDialogBuilder(this@LauncherActivity)
//                    .setTitle("Message")
//                    .setMessage(response.getString("message").toString())
//                    .setNeutralButton("OK") { _, _ -> }
//                    .show()

//                storeSwipeRefreshLayout.isRefreshing = false

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
