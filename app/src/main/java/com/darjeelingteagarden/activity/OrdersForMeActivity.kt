package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityMyOrdersBinding
import com.darjeelingteagarden.databinding.ActivityOrdersForMeBinding

class OrdersForMeActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar

    private lateinit var binding: ActivityOrdersForMeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersForMeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toolbar = binding.toolbarOrdersForMeActivity
        setupToolbar()

        toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _->
            Log.i("Destination: ", destination.toString())
            val title = when(destination.id){
                R.id.ordersForMeFragment -> "Orders For me"
                R.id.ordersForMeDetailsFragment -> "Order For Me - Details"
                else -> "Orders For Me"
            }
            changeToolbarTitle(title)
        }



    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Orders For Me"
    }

    private fun changeToolbarTitle(title: String){
        supportActionBar?.title = title
    }
}