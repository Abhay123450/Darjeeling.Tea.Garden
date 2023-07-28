package com.darjeelingteagarden.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.fragment.CartFragment
import com.darjeelingteagarden.fragment.HomeFragment
import com.darjeelingteagarden.fragment.StoreFragment
import com.darjeelingteagarden.repository.AppDataSingleton
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var toolbar: Toolbar
    var previousMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Setup Toolbar
        toolbar = findViewById(R.id.mainToolbar)
        setUpToolBar()

//        openHome()

        //Set up bottom navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{ _, destination, _ ->
            Log.i("Destination: ", destination.toString())
            title = when(destination.id){
                R.id.homeFragment -> "Home"
                R.id.storeFragment -> "Store"
                R.id.cartFragment -> "Cart"
                else -> "Darjeeling Tea Garden"
            }
            changeToolbarTitle(title.toString())
        }

        getGradeList()

        toolbar.setNavigationOnClickListener{
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }

    }

//    override fun onBackPressed() {
//        val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
//
//        when(fragment){
//            !is HomeFragment -> openHome()
//
//            else -> super.onBackPressed()
//        }
//    }

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

}