package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.MyOrdersActivity
import com.darjeelingteagarden.activity.OrdersForMeActivity
import com.darjeelingteagarden.activity.SampleOrderActivity
import com.darjeelingteagarden.databinding.FragmentHomeBinding
import com.darjeelingteagarden.repository.AppDataSingleton

class HomeFragment : Fragment(){

    private lateinit var mContext: Context

    private lateinit var queue: RequestQueue

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.swipeRefreshHome.isRefreshing = false

        queue = Volley.newRequestQueue(mContext)

        getActiveMyOrdersCount()
        getActiveOrdersForMeCount()
        getActiveSampleOrdersCount()

        binding.swipeRefreshHome.setOnRefreshListener {

            getActiveMyOrdersCount()
            getActiveOrdersForMeCount()

        }

        binding.btnViewMyOrders.setOnClickListener {
            val intent = Intent(mContext, MyOrdersActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewOrdersForMe.setOnClickListener {
            val intent = Intent(mContext, OrdersForMeActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewMySampleOrders.setOnClickListener {
            val intent = Intent(mContext, SampleOrderActivity::class.java)
            intent.putExtra("orderHistory", true)
            startActivity(intent)
        }

        binding.btnCallNow.setOnClickListener {
            openDialPad(mContext, "7007789842")
        }

        binding.btnWhatsappNow.setOnClickListener {
            openWhatsapp(mContext, "7007789842")
        }

        return binding.root
    }

    private fun openDialPad(context: Context, phoneNum: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:+91$phoneNum")
        context.startActivity(intent)
    }

    private fun openWhatsapp(context: Context, phoneNum: String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("http://api.whatsapp.com/send?phone=+91$phoneNum")
        context.startActivity(intent)
    }

    private fun getActiveMyOrdersCount(){

        binding.progressMyOrders.visibility = View.VISIBLE
        binding.txtMyOrdersText.visibility = View.INVISIBLE

//        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/myOrdersCount"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    binding.progressMyOrders.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success){

                        val totalOrders = it.getInt("data")

                        if(totalOrders == 0){

                            binding.btnViewMyOrders.visibility = View.GONE

                        }else{

                            binding.btnViewMyOrders.visibility = View.VISIBLE
                            "You have $totalOrders active order(s)".also { text ->
                                binding.txtMyOrdersText.text = text
                            }

                        }

                        binding.swipeRefreshHome.isRefreshing = false
                        binding.txtMyOrdersText.visibility = View.VISIBLE

                    }



                }catch (e: Exception){

                    binding.swipeRefreshHome.isRefreshing = false
                    binding.txtMyOrdersText.visibility = View.VISIBLE

                }

            },
            Response.ErrorListener {

                binding.progressMyOrders.visibility = View.GONE
                binding.swipeRefreshHome.isRefreshing = false
                binding.txtMyOrdersText.visibility = View.VISIBLE

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }


        }

        queue.add(jsonObjectRequest)

    }

    private fun getActiveOrdersForMeCount(){

        binding.progressOrdersForMe.visibility = View.VISIBLE
        binding.txtOrdersForMeText.visibility = View.INVISIBLE

//        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/ordersForMeCount"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    binding.progressOrdersForMe.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success){

                        val totalOrders = it.getInt("data")

                        if (totalOrders == 0){

                            binding.btnViewOrdersForMe.visibility = View.GONE

                        }
                        else {

                            binding.btnViewOrdersForMe.visibility = View.VISIBLE
                            "There are $totalOrders active order(s)".also { text ->
                                binding.txtOrdersForMeText.text = text
                            }

                        }

                        binding.txtOrdersForMeText.visibility = View.VISIBLE


                    }

                }catch (e: Exception){

                    binding.txtOrdersForMeText.visibility = View.VISIBLE

                }

            },
            Response.ErrorListener {

                binding.txtOrdersForMeText.visibility = View.VISIBLE

            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun getActiveSampleOrdersCount(){

        binding.progressSampleOrders.visibility = View.VISIBLE
        binding.txtSampleOrderText.visibility = View.GONE

//        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder/activeCount"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    binding.progressSampleOrders.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success){

                        val totalOrders = it.getInt("data")

                        if (totalOrders == 0){

                            binding.btnViewMySampleOrders.visibility = View.GONE

                        }else{

                            binding.btnViewMySampleOrders.visibility = View.VISIBLE
                            "There are $totalOrders active sample order(s)".also { text ->
                                binding.txtSampleOrderText.text = text
                            }

                        }

                    }

                    binding.txtSampleOrderText.visibility = View.VISIBLE

                }catch (e: Exception){

                    binding.txtSampleOrderText.visibility = View.VISIBLE

                }

            },
            Response.ErrorListener {

                binding.txtSampleOrderText.visibility = View.VISIBLE

            }
        ) {
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