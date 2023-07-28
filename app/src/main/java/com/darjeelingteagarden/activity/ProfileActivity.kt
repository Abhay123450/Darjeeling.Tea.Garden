package com.darjeelingteagarden.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityProfileBinding
import com.darjeelingteagarden.fragment.ProfileMainFragment
import com.darjeelingteagarden.repository.AppDataSingleton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var toolbar: Toolbar

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

        binding.btnLogout.setOnClickListener {

            MaterialAlertDialogBuilder(this@ProfileActivity)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout ?")
                .setPositiveButton("Yes"){ _, _ ->

                    val sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()

                    //open login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)

                    //finish all previous activities
                    finishAffinity()

                }
                .setNegativeButton("No"){_, _ -> }
                .show()

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
}