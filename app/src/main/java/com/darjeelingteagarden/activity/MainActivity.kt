package com.darjeelingteagarden.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityMainBinding
import com.darjeelingteagarden.fragment.HomeFragment
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.repository.StoreDataSingleton
import com.darjeelingteagarden.repository.UserDataSingleton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import org.json.JSONObject

class MainActivity : BaseActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var toolbar: Toolbar
    lateinit var binding: ActivityMainBinding

    private var cartBadgeTextView: TextView? = null

    //for notification
    private var activityToOpen: String? = ""
    private var resourceId: String? = ""

    private lateinit var sharedPreferences: SharedPreferences

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_toolbar_menu, menu)

        val cartItem = menu?.findItem(R.id.action_cart)
        val actionView = cartItem?.actionView
        cartBadgeTextView = actionView?.findViewById(R.id.cart_badge)

        actionView?.setOnClickListener {
            onOptionsItemSelected(cartItem)
        }

        CartDataSingleton.totalCartItems.observe(this){
            setupBadge(it)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cart -> {
                startActivity(Intent(this@MainActivity, CartActivity::class.java))
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup Toolbar
        toolbar = findViewById(R.id.mainToolbar)
        setUpToolBar()

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)

        StoreDataSingleton.fetchStoreItems(this)

        if (CartDataSingleton.totalCartItems.value == null) {
            CartDataSingleton.totalCartItems.value = CartDataSingleton.cartList.size
        }

        //Set up bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.main_bottom_nav)

        val isLoggedIn = AppDataSingleton.isLoggedIn()
        val menu = bottomNavigationView.menu

        if (isLoggedIn){
            navGraph.setStartDestination(R.id.homeFragment)
            menu.findItem(R.id.homeFragment)?.isVisible = true
        }
        else{
            navGraph.setStartDestination(R.id.storeFragment)
            menu.findItem(R.id.homeFragment)?.isVisible = false
        }

        navController.graph = navGraph

        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            Log.i("Destination: ", destination.toString())
            title = when(destination.id){
                R.id.homeFragment -> "Home"
                R.id.storeFragment -> "Store"
                else -> "Darjeeling Tea Garden"
            }
            changeToolbarTitle(title.toString())
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        getGradeList()

        toolbar.setNavigationOnClickListener{
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }

        if (NotificationDataSingleton.notificationToOpen){

            val newIntent = when(NotificationDataSingleton.activityToOpen){
                "news" -> {
                    Intent(this, NewsActivity::class.java)
                }

                "orderDetails" -> {
                    Intent(this, MyOrdersActivity::class.java)
                }

                "sampleOrderDetails" -> {
                    Intent(this, SampleOrderDetailsActivity::class.java)
                }

                "ordersForMe" -> {
                    Intent(this, OrdersForMeActivity::class.java)
                }

                "sampleOrdersForMe" -> {
                    Intent(this, SampleOrdersForMeActivity::class.java)
                }

                "videos" -> {
                    Intent(this, VideosActivity::class.java)
                }

                "users" -> {
                    Intent(this, MyDownlineActivity::class.java)
                }

                "productDetails" -> {
                    Intent(this, ProductDetailsActivity:: class.java)
                }

                else -> {
                    null
                }
            }

            if (newIntent != null ){
                startActivity(newIntent)
            }

        }

        askNotificationPermission()

        supportFragmentManager.setFragmentResultListener("auth_key", this) { requestKey, bundle ->
            if (requestKey == "auth_key") {
                val isLoggedIn = bundle.getBoolean("isLoggedIn")
                if (isLoggedIn) {
                    // --- SUCCESS! TRIGGER YOUR REFRESH LOGIC HERE ---
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT)
                        .show()
                    refreshActivityData(navGraph, menu)
                }
            }
            Log.i("auth result", bundle.toString())
        }

    }

    private fun setupBadge(count: Int) {
        if (count == 0) {
            cartBadgeTextView?.visibility = View.GONE
        } else {
            cartBadgeTextView?.text = count.toString()
            cartBadgeTextView?.visibility = View.VISIBLE
        }
    }

    private fun refreshActivityData(navGraph: androidx.navigation.NavGraph, menu: Menu){
        UserDataSingleton.getUserDetails(this)
        navGraph.setStartDestination(R.id.homeFragment)
        menu.findItem(R.id.homeFragment)?.isVisible = true
        bottomNavigationView.invalidate()
        bottomNavigationView.requestLayout()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
                updateFcmToken()

                subscribeToTopic("news")

                createNotificationChannel()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        else{
            updateFcmToken()

            subscribeToTopic("news")

            createNotificationChannel()
        }
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = "darjeeling_tea_garden"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                getString(R.string.notification_channel_id),
                channelName,
                importance
            )
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun subscribeToTopic(topic: String){
        Firebase.messaging.subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if(!task.isSuccessful){
                    msg = "Subscribe Failed"
                }
                Log.d("subscribe to topic", msg)
//                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFcmToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
//                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val newToken = task.result
            Log.i("fcm token MainAct", newToken)

            sendTokenToServer(newToken)

//            val token = sharedPreferences.getString("fcm-token", null)
//
//            if (token == null || token != newToken){
//                sendTokenToServer(newToken)
//            }
//            else {
//                val tokenUpdated = sharedPreferences.getBoolean("fcm-token-updated", false)
//
//                if (!tokenUpdated){
//                    sendTokenToServer(token)
//                }
//
//            }


//            Toast.makeText(this, newToken, Toast.LENGTH_SHORT).show()
        })

    }

    private fun setUpToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Home"
    }

    private fun changeToolbarTitle(name: String){
        supportActionBar?.title = name
    }

    private fun openHome(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, HomeFragment())
            .commit()

        changeToolbarTitle("Home")
    }

    private fun getGradeList(){

        val queue = Volley.newRequestQueue(this@MainActivity)

        val url = "${getString(R.string.homeUrl)}api/v1/product/grades"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val gradeList = it.getJSONArray("data")

                        for(i in 0 until gradeList.length()){
                            AppDataSingleton.addProductGrade(gradeList.getJSONObject(i).getString("name"))
                        }

                    }
                    else{
                        Toast.makeText(this, "Grade list not loaded", Toast.LENGTH_LONG).show()

                    }

                }catch (e: Exception){
                    Toast.makeText(this, "Grade list not loaded: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Grade list not loaded. Response error", Toast.LENGTH_LONG).show()
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

    private fun sendTokenToServer(token: String){

            val queue = Volley.newRequestQueue(this)

            val url = "${getString(R.string.homeUrl)}api/v1/user/deviceToken"

            val jsonBody = JSONObject()
            jsonBody.put("deviceToken", token)

            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST,
                url,
                jsonBody,
                Response.Listener {
                    try{

                        val success = it.getBoolean("success")

                        if (success){
//                            Toast.makeText(this, it.getString("message"), Toast.LENGTH_SHORT).show()

                            val editor = sharedPreferences.edit()
                            editor.putBoolean("fcm-token-updated", true)
                            editor.apply()

                        }

                    }
                    catch (e: Exception){
//                        Toast.makeText(this, "exception ${ e.toString() }", Toast.LENGTH_SHORT).show()
                        Log.d("fcmUpdateExc", e.toString())
                    }
                },
                Response.ErrorListener {
                    Log.d("fcmUpdateErr", JSONObject(String(it.networkResponse.data)).getString("message"))
//                    Toast.makeText(this, "exception response error", Toast.LENGTH_SHORT).show()
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