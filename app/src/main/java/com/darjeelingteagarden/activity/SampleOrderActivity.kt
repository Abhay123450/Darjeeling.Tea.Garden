package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityProfileBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrderBinding
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.repository.SampleDataSingleton

class SampleOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleOrderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleOrderBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerSampleOrder) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavSampleOrder.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            title = when(destination.id){
                R.id.sampleStoreFragment -> "Sample Store"
                R.id.sampleCartFragment -> "Sample Cart"
                R.id.sampleHistoryFragment -> "Sample Order History"
                else -> "Sample Order"
            }
            changeToolbarTitle(title.toString())
        }

        if(intent != null){
            val orderHistory = intent.getBooleanExtra("orderHistory", false)
            if (orderHistory && !SampleDataSingleton.historyShown){
//                navController.navigate(R.id.sam)
                SampleDataSingleton.historyShown = true
            }
        }

        binding.sampleOrderToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        SampleDataSingleton.clearSampleOrderList()
    }

    private fun setUpToolBar(){
        setSupportActionBar(binding.sampleOrderToolbar)
        supportActionBar?.title = "Sample Store"
    }

    private fun changeToolbarTitle(name: String){
        supportActionBar?.title = name
    }
}