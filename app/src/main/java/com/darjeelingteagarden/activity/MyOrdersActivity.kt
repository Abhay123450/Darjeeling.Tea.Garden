package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityMyOrdersBinding
import com.darjeelingteagarden.fragment.MyOrdersFragment
import com.darjeelingteagarden.fragment.OrderDetailsFragment

class MyOrdersActivity : BaseActivity() {

    private lateinit var toolbar: Toolbar

    private val orderDetailsFragment = OrderDetailsFragment()
    private val myOrdersFragment = MyOrdersFragment()

    private lateinit var navController: NavController

    private lateinit var binding: ActivityMyOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toolbar = view.findViewById(R.id.toolbarMyOrdersActivity)
        setupToolbar()

        toolbar.setNavigationOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener{_, destination, _ ->
            Log.i("Destination: ", destination.toString())
            val title = when(destination.id){
                R.id.myOrdersFragment -> "My Orders"
                R.id.orderDetailsFragment -> "Order Details"
                else -> "My Orders"
            }
            changeToolbarTitle(title)
        }

//        supportFragmentManager
//            .beginTransaction()
//            .replace(R.id.fragmentContainerView, myOrdersFragment)
//            .commit()

    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Orders"
    }

    private fun changeToolbarTitle(title: String){
        supportActionBar?.title = title
    }

}