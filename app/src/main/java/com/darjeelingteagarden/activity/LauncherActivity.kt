package com.darjeelingteagarden.activity

import android.app.Activity
import android.app.Application
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.User
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.repository.StoreDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.viewModel.AppDataViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
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

    //open notification
    private var isNotificationOpened = false
    private var activityToOpen: String? = ""
    private var resourceId: String? = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            startup()
        } else {
            // TODO: Inform user that that your app will not show notifications.
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this@LauncherActivity)
                .setTitle("Permission Required")
                .setMessage("Notification permission is required in order to use the app.")
                .setCancelable(false)
                .setPositiveButton("Allow"){dialog, int ->
                    askNotificationPermission()
                }
                .setNegativeButton("EXIT"){dialog, int ->
                    this.finishAffinity()
                }

            materialAlertDialogBuilder.show()
        }
    }

    //for update
    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.IMMEDIATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        mAppDataViewModel = ViewModelProvider(this)[AppDataViewModel::class.java]

        if (intent != null){
            activityToOpen = intent.getStringExtra("activityToOpen")
            resourceId = intent.getStringExtra("resourceId")
            isNotificationOpened = true

            if (activityToOpen != null){
                NotificationDataSingleton.notificationToOpen = true
                NotificationDataSingleton.activityToOpen = activityToOpen
            }

            if (resourceId != null){
                NotificationDataSingleton.resourceId = resourceId
            }

            Log.d("datapayload activity", activityToOpen.toString())
            Log.d("resource id activity", resourceId.toString())

        }

        askNotificationPermission()

//        checkForUpdates()
//        Handler(Looper.getMainLooper()).postDelayed({
//            startAnotherActivity()
//        }, 2000)
    }

    override fun onResume() {
        super.onResume()
        checkUpdateStatus()
    }

    private fun startup(){

        if (ConnectionManager().isOnline(this@LauncherActivity)){
            Log.d("startup fired", "")
            checkForUpdates()
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

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

                startup()

            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        else{
            startup()
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

            if (lastLogin > (1000 * 60 * 60 * 24 * 14)){ // 14 days
                isLoggedIn = false
                startAnotherActivity()
            }
            else{
                AppDataSingleton.setAuthToken(token.toString())
                Log.i("auth token is === ", AppDataSingleton.getAuthToken)

                getUserDetails()
//                StoreDataSingleton.getStoreItems(applicationContext)
//                getStoreItems()
//            getCartItems()
            }

        }
        else{

            startAnotherActivity()

        }

    }

    private fun startAnotherActivity(){

        val intent: Intent = if (isLoggedIn && token != null){
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
                            data.optString("email"),
                        )

                        println("user is $user")
                        AppDataSingleton.setUserInfo(user)

                        userInfoLoaded = true

                        startAnotherActivity()

//                        if (userInfoLoaded && itemListLoaded && !openedActivity){
//                            openedActivity = true
//
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
                    this@LauncherActivity,"An error occurred", Toast.LENGTH_LONG
                ).show()
//                val response = JSONObject(String(it.networkResponse.data))
                if (it.networkResponse.statusCode == 401){
                    isLoggedIn = false
                    startAnotherActivity()
                }
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

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ){
        Log.i("result code ", it.resultCode.toString())
        if (it.resultCode == RESULT_OK){
            login()
        }
        else{
            MaterialAlertDialogBuilder(this@LauncherActivity)
                .setTitle("Update Required")
                .setMessage("Please update the app to continue using.")
                .setCancelable(false)
                .setPositiveButton("Update"){dialog, int ->
                    checkForUpdates()
                }
                .setNegativeButton("Exit"){dialog, int ->
                    this.finishAffinity()
                }.show()
        }
    }

    private fun checkForUpdates(){

        Log.d("checkForUpdates fired", "")

        appUpdateManager.appUpdateInfo.addOnSuccessListener {info ->
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType){
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                else -> false
            }

            Log.d("isUpdateAvailable", isUpdateAvailable.toString())
            Log.d("isUpdateAllowed", isUpdateAllowed.toString())
            if (isUpdateAvailable && isUpdateAllowed){
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
            else{
                login()
            }
        }.addOnFailureListener {
            Log.d("check Update Failed", it.message.toString())
            login()
        }

    }

    private fun checkUpdateStatus(){

        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->

                if (appUpdateInfo.updateAvailability()
                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                ) {
                    // If an in-app update is already running, resume the update.
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activityResultLauncher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            }
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

//                            for (i in 0 until data.length()){
//                                val cartInfo = data.getJSONObject(i)
//                                val cartObject = Cart(
//                                    cartInfo.getString("productId"),
//                                    "name of product",
//                                    999,
////                                cartInfo.getString("name"),
////                                cartInfo.getJSONObject("productId").getInt("discountedPrice"),
//                                    cartInfo.getInt("quantity")
//                                )
//
////                            cartList.add(cartObject)
////                            mAppDataViewModel.cartItemList.add(cartObject)
//                                AppDataSingleton.addCartItem(cartObject)
//
//                            }

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

                                val discountedPrice = if (productInfo.getBoolean("discount")){
                                    productInfo.getInt("discountedPrice")
                                }else{
                                    productInfo.getInt("originalPrice")
                                }

                                val productObject = Product(
                                    productInfo.getString("_id"),
                                    productInfo.getString("name"),
                                    productInfo.getInt("originalPrice"),
                                    discountedPrice,
                                    productInfo.optDouble("samplePrice"),
                                    10,
                                    productInfo.getString("grade"),
                                    productInfo.getString("lotNumber"),
                                    productInfo.getInt("bagSize"),
                                    productInfo.getString("mainImage"),
                                    productInfo.getBoolean("discount")
                                )
//                            productList.add(productObject)
                                AppDataSingleton.addStoreItem(productObject)

                            }

                        }

                        itemListLoaded = true
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
