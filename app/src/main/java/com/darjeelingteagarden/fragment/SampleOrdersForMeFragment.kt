package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.SampleOrderDetailsActivity
import com.darjeelingteagarden.adapter.OrdersForMeRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleOrdersForMeRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentSampleOrdersForMeBinding
import com.darjeelingteagarden.model.OrderForMe
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate

class SampleOrdersForMeFragment : Fragment() {

    private lateinit var binding: FragmentSampleOrdersForMeBinding
    private lateinit var mContext: Context

    private val sortList = mutableListOf("Newest First", "Oldest First", "Low Order Value", "High Order Value")
    private var status = "Active"
    private var currentPage = 1
    private var sort = 1
    private var limit = 10
    private var totalOrders = 0

    private lateinit var recyclerViewSampleOrderForMe: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var sampleOrdersForMeRecyclerAdapter: SampleOrdersForMeRecyclerAdapter

    private var sampleOrdersForMeList = mutableListOf<OrderForMe>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSampleOrdersForMeBinding.inflate(inflater, container, false)

        recyclerViewSampleOrderForMe = binding.recyclerViewSampleOrdersForMe
        layoutManager = LinearLayoutManager(mContext)



        binding.autoCompleteTextViewSampleOrderStatus.setOnItemClickListener { adapterView, view, i, l ->
            status = adapterView.getItemAtPosition(i).toString()
            currentPage = 1
            SampleDataSingleton.clearSampleOrderForMeList()
            binding.rlProgressBar.visibility = View.VISIBLE
            getSampleOrdersForMe(currentPage)
        }

        binding.autoCompleteTextViewSort.setOnItemClickListener { adapterView, view, i, l ->
            sort = i + 1
            currentPage = 1
            SampleDataSingleton.clearSampleOrderForMeList()
            binding.rlProgressBar.visibility = View.VISIBLE
            getSampleOrdersForMe(currentPage)
        }

        binding.btnLoadMore.setOnClickListener {
            getSampleOrdersForMe(++currentPage)
            it.visibility = View.GONE
            binding.progressBarLoadMore.visibility = View.VISIBLE
        }

        binding.swipeRefreshSampleOrdersForMe.setOnRefreshListener {
            getSampleOrdersForMe(currentPage)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        initializeFilterDropdown()
        initializeSortDropdown()


        if (sampleOrdersForMeList.size == 0){
            getSampleOrdersForMe(currentPage)
        }
        else{
            populateRecyclerView(sampleOrdersForMeList)
        }
    }

    private fun initializeFilterDropdown(){

        val statusList = arrayListOf("All", "Active", "Delivered")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, statusList)
        binding.autoCompleteTextViewSampleOrderStatus.setAdapter(arrayAdapter)

    }

    private fun initializeSortDropdown(){
        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, sortList)
        binding.autoCompleteTextViewSort.setAdapter(arrayAdapter)
    }

    private fun populateRecyclerView(list: MutableList<OrderForMe>){

        sampleOrdersForMeRecyclerAdapter = SampleOrdersForMeRecyclerAdapter(mContext, list){
            val intent = Intent(mContext, SampleOrderDetailsActivity::class.java)
            intent.putExtra("sampleOrderId", it)
            startActivity(intent)
        }
        recyclerViewSampleOrderForMe.adapter  = sampleOrdersForMeRecyclerAdapter
        recyclerViewSampleOrderForMe.layoutManager = layoutManager
        binding.rlProgressBar.visibility = View.GONE
        binding.progressBarLoadMore.visibility = View.GONE
        binding.swipeRefreshSampleOrdersForMe.isRefreshing = true

    }

    private fun populateAdditionalData(){

        sampleOrdersForMeRecyclerAdapter.notifyItemRangeInserted((currentPage - 1) * limit + 1, limit)
        binding.rlProgressBar.visibility = View.GONE
        binding.progressBarLoadMore.visibility = View.GONE

        var text = ""

        if (totalOrders > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalOrders orders"
            binding.btnLoadMore.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalOrders of $totalOrders orders"
            binding.btnLoadMore.visibility = View.GONE
        }

        binding.txtInfo.text = text

    }


    private fun getSampleOrdersForMe(page: Int){

        binding.swipeRefreshSampleOrdersForMe.isRefreshing = true

        val query = if (status == "Active"){
            "?status=Active"
        } else if (status == "Delivered") {
            "?page=$page&limit=$limit&sort=$sort&status=Delivered"
        }
        else{
            "?page=$page&limit=$limit&sort=$sort"
        }

        val queue = Volley.newRequestQueue(mContext)
//        val query = "?page=$page&limit=$limit"
//        val query = "?status=Active"
        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder/sampleOrdersForMe" + query

        Log.i("orders for me url::: ", url)


        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")
                    Log.i("success is ::", success.toString())

                    if (success){

                        val data = it.getJSONArray("data")
                        val totalOrdersForMe = data.length()

                        if (status != "Active"){
                            totalOrders = it.getInt("totalOrders")

                            var text = ""

                            if (totalOrders > currentPage * limit){
                                text = "Showing 1 - ${currentPage * limit} of $totalOrders orders"
                                binding.btnLoadMore.visibility = View.VISIBLE
                            } else {
                                text = "Showing 1 - $totalOrders of $totalOrders orders"
                                binding.btnLoadMore.visibility = View.GONE
                            }

                            binding.txtInfo.text = text
                        }
                        else{
                            binding.btnLoadMore.visibility = View.GONE
                            binding.txtInfo.text = "$totalOrdersForMe Active Order(s)"
                        }

                        Log.i("data string is ::", data.toString())

                        if (totalOrdersForMe == 0){
                            binding.rlProgressBar.visibility = View.GONE
                            binding.swipeRefreshSampleOrdersForMe.isRefreshing = false
                            Toast.makeText(mContext, "No Active Sample Orders !", Toast.LENGTH_LONG).show()
                        }
                        else{

                            for (i in 0 until totalOrdersForMe){

                                val orderForMe = data.getJSONObject(i)
                                val from = orderForMe.getJSONObject("from")

                                val fromAddress = "${from.getString("city")}, ${from.getString("state")}"

                                val dataObject = OrderForMe(
                                    orderForMe.getString("_id"),
                                    from.getString("_id"),
                                    from.getString("name"),
                                    from.getString("role"),
                                    fromAddress,
                                    orderForMe.getString("orderDate").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    orderForMe.getInt("itemCount"),
                                    orderForMe.getInt("totalPrice"),
                                    orderForMe.getString("currentStatus")
                                )

                                sampleOrdersForMeList.add(dataObject)

                            }

                            if (currentPage == 1){
                                populateRecyclerView(sampleOrdersForMeList)
                            }
                            else{
                                populateAdditionalData()
                            }
                            binding.swipeRefreshSampleOrdersForMe.isRefreshing = false
                        }

                    }
                    else {
                        Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG).show()
                        binding.swipeRefreshSampleOrdersForMe.isRefreshing = false
                    }

                }catch (e: Exception){
                    Log.i("exception is ", e.toString())
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                    binding.swipeRefreshSampleOrdersForMe.isRefreshing = false
                }
            },
            Response.ErrorListener {
                binding.swipeRefreshSampleOrdersForMe.isRefreshing = false
                Toast.makeText(mContext, "Response error", Toast.LENGTH_LONG).show()
//                val response = JSONObject(String(it.networkResponse.data))
//                Log.i("error listener", response.toString())
//                Toast.makeText(mContext, "An error occurred: $response", Toast.LENGTH_LONG).show()
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