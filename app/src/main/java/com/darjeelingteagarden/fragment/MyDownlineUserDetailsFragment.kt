package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.FragmentMyDownlineUserDetailsBinding
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.util.ConnectionManager

class MyDownlineUserDetailsFragment : Fragment() {

    private lateinit var binding: FragmentMyDownlineUserDetailsBinding
    private lateinit var mContext: Context

    private lateinit var queue: RequestQueue

    private var userId = ""
    private var user_id = ""

    var latitude = 0.0
    var longitude = 0.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyDownlineUserDetailsBinding.inflate(inflater, container, false)

        queue = Volley.newRequestQueue(mContext)

        if (NotificationDataSingleton.notificationToOpen &&
            NotificationDataSingleton.activityToOpen  == "users" &&
            NotificationDataSingleton.resourceId != null
        ){
            NotificationDataSingleton.notificationToOpen = false
            getUserDetails(NotificationDataSingleton.resourceId)
        }
        else if (AppDataSingleton.myDownlineUserId != ""){

            if (AppDataSingleton.showUserDetails){
                AppDataSingleton.showUserDetails = false
                AppDataSingleton.myDownlineGoBack = true
//                val entry = parentFragmentManager.getBackStackEntryAt(
//                    parentFragmentManager.backStackEntryCount - 1
//                )
//                Log.d("entry name", entry.name.toString())
//                Log.i("entry id", entry.id.toString())
//                parentFragmentManager.popBackStack(entry.name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }


            if(ConnectionManager().isOnline(mContext)){
                getUserDetails(AppDataSingleton.myDownlineUserId)
            }
            else{
                AppDataSingleton.noInternet(mContext)
            }
        }

        binding.btnReleaseDue.setOnClickListener {
            if(ConnectionManager().isOnline(mContext)){
                releaseCredit(user_id)
            }
            else{
                AppDataSingleton.noInternet(mContext)
            }
        }

        binding.btnViewLocationInMap.setOnClickListener {
            val geoUri = "http://maps.google.com/maps?q=loc:$latitude,$longitude (user)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geoUri))
            startActivity(intent)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (AppDataSingleton.myDownlineGoBack){
                    AppDataSingleton.myDownlineGoBack = false
                    activity?.finish()
                }
                else{
                    isEnabled = false
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            }

        })

        return binding.root
    }

    private fun getUserDetails(userId: String?){

        if (userId == null){
            return
        }

        val url = "${getString(R.string.homeUrl)}api/v1/user/myDownline/$userId"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (!success){
                        return@Listener
                    }

                    val user = it.getJSONObject("data")

                    user_id = user.getString("_id")

                    binding.txtUserId.text = user.getString("userId")
                    binding.txtUserName.text = user.getString("name")
                    binding.txtUserRole.text = user.optString("role")
                    binding.txtUserPhoneNumber.text = user.getLong("phoneNumber").toString()
                    binding.txtUserEmail.text = user.getString("email")
                    binding.txtUserAddressLine1.text = user.optString("addressLineOne")
                    binding.txtUserAddressLine2.text = user.optString("addressLineTwo")
                    binding.txtUserAddressCity.text = user.getString("city")
                    binding.txtUserAddressState.text = user.getString("state")
                    binding.txtUserAddressPincode.text = user.getString("pincode")

                    if (AppDataSingleton.getUserInfo.role.equals("admin", ignoreCase = true)){

                        val location = user.getJSONObject("location")
                        latitude = location.getDouble("latitude")
                        longitude = location.getDouble("longitude")


                        val dueBalance = user.getDouble("credit")
                        binding.txtUserDueBalance.text = dueBalance.toString()
                        if (dueBalance > 0){
                            binding.btnReleaseDue.visibility = View.GONE
                        }

                        binding.cardBalanceDue.visibility = View.VISIBLE
                        binding.cardUserLocation.visibility = View.VISIBLE

                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(mContext, "An error occurrede.", Toast.LENGTH_LONG).show()
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

    private fun releaseCredit(userId: String){

        val url = "${getString(R.string.homeUrl)}api/v1/admin/user/releaseCredit/$userId"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        binding.txtUserDueBalance.text =
                            it.getJSONObject("data").getDouble("credit").toString()
                        binding.btnReleaseDue.visibility = View.GONE

                    }

                }
                catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {
                Toast.makeText(mContext, "An error occurred.", Toast.LENGTH_LONG).show()
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