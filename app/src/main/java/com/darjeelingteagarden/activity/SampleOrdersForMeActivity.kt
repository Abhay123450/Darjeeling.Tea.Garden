package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivitySampleOrderDetailsBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrdersForMeBinding

class SampleOrdersForMeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleOrdersForMeBinding

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleOrdersForMeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        toolbar = binding.toolbarSampleOrdersForMeActivity
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
                R.id.sampleOrdersForMeFragment -> "Sample Orders For me"
                R.id.sampleOrdersForMeDetailsFragment -> "Sample Order For Me - Details"
                else -> "Sample Orders For Me"
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