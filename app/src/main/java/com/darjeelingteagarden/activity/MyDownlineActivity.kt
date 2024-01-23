package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityMyDownlineBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrderBinding
import com.darjeelingteagarden.model.MyDownline
import com.darjeelingteagarden.repository.AppDataSingleton

class MyDownlineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyDownlineBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyDownlineBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.toolbarMyDownlineActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener{_, destination, _ ->
            Log.i("Destination: ", destination.toString())
            val title = when(destination.id){
                R.id.myDownlineListFragment -> "My Downline"
                R.id.myDownlineUserDetailsFragment -> "User Details"
                else -> "My Downline"
            }
            changeToolbarTitle(title)
        }

    }

    private fun setupToolbar(){
        setSupportActionBar(binding.toolbarMyDownlineActivity)
        supportActionBar?.title = "My Downline"
    }

    private fun changeToolbarTitle(title: String){
        supportActionBar?.title = title
    }

    fun goBack(){
        onBackPressedDispatcher.onBackPressed()
    }

}