package com.darjeelingteagarden.fragment

import android.content.Context
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
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.OrdersForMeRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentOrdersForMeBinding
import com.darjeelingteagarden.model.MyOrder
import com.darjeelingteagarden.model.OrderForMe
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import org.json.JSONObject

class OrdersForMeFragment : Fragment() {

    private lateinit var binding: FragmentOrdersForMeBinding
    private lateinit var mContext: Context

    private var ordersForMeList: MutableList<OrderForMe> = mutableListOf()

    private val sortList = mutableListOf("Newest First", "Oldest First", "Low Order Value", "High Order Value")
    private var status = "Active"
    private var currentPage = 1
    private var sort = 1
    private var limit = 10
    private var totalOrders = 0

    private lateinit var recyclerViewOrderForMe: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var ordersForMeRecyclerAdapter: OrdersForMeRecyclerAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOrdersForMeBinding.inflate(inflater, container, false)

        recyclerViewOrderForMe = binding.recyclerViewOrdersForMe
        layoutManager = LinearLayoutManager(mContext)

        binding.autoCompleteTextViewOrderStatus.setOnItemClickListener { adapterView, view, i, l ->
            status = adapterView.getItemAtPosition(i).toString()
            currentPage = 1
//            AppDataSingleton.clearOrderForMeList()
            ordersForMeList.clear()
            binding.rlProgressBar.visibility = View.VISIBLE
            getOrdersForMe(currentPage)
        }

        binding.autoCompleteTextViewSort.setOnItemClickListener { adapterView, view, i, l ->
            sort = i + 1
            currentPage = 1
//            AppDataSingleton.clearOrderForMeList()
            ordersForMeList.clear()
            binding.rlProgressBar.visibility = View.VISIBLE
            getOrdersForMe(currentPage)
        }

        binding.btnLoadMore.setOnClickListener {
            getOrdersForMe(++currentPage)
            it.visibility = View.GONE
            binding.progressBarLoadMore.visibility = View.VISIBLE
        }

        binding.swipeRefreshOrdersForMe.setOnRefreshListener {
            binding.swipeRefreshOrdersForMe.isRefreshing = true
//            AppDataSingleton.clearOrderForMeList()
            ordersForMeList.clear()
            getOrdersForMe(currentPage)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        initializeFilterDropdown()
        initializeSortDropdown()

        if (ordersForMeList.size == 0){
            getOrdersForMe(currentPage)
        }
        else{
            populateRecyclerView(ordersForMeList)
            binding.swipeRefreshOrdersForMe.isRefreshing = false
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        AppDataSingleton.clearOrderForMeList()
    }

    private fun initializeFilterDropdown(){

        val statusList = arrayListOf("Active", "History")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, statusList)
        binding.autoCompleteTextViewOrderStatus.setAdapter(arrayAdapter)

    }

    private fun initializeSortDropdown(){
        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, sortList)
        binding.autoCompleteTextViewSort.setAdapter(arrayAdapter)
    }

    private fun populateRecyclerView(list: MutableList<OrderForMe>){

        ordersForMeRecyclerAdapter = OrdersForMeRecyclerAdapter(mContext, list, findNavController())
        recyclerViewOrderForMe.adapter  = ordersForMeRecyclerAdapter
        recyclerViewOrderForMe.layoutManager = layoutManager
        binding.rlProgressBar.visibility = View.GONE
        binding.progressBarLoadMore.visibility = View.GONE
        binding.swipeRefreshOrdersForMe.isRefreshing = true

    }

    private fun populateAdditionalData(){

        ordersForMeRecyclerAdapter.notifyItemRangeInserted((currentPage - 1) * limit + 1, limit)
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

    private fun getOrdersForMe(page: Int){

        binding.swipeRefreshOrdersForMe.isRefreshing = true

        val query = if (status == "Active"){
            "?status=Active&sort=$sort"
        } else {
            "?page=$page&limit=$limit&sort=$sort"
        }

        val queue = Volley.newRequestQueue(mContext)
//        val query = "?page=$page&limit=$limit"
//        val query = "?status=Active"
        val url = getString(R.string.homeUrl) + "api/v1/orders/ordersForMe" + query

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
                            binding.swipeRefreshOrdersForMe.isRefreshing = false
                            Toast.makeText(mContext, "No Active Orders !", Toast.LENGTH_LONG).show()
                        }
                        else{

                            for (i in 0 until totalOrdersForMe){

                                val orderForMe = data.getJSONObject(i)
                                val from = orderForMe.getJSONObject("from")

                                val dataObject = OrderForMe(
                                    orderForMe.getString("_id"),
                                    from.getString("_id"),
                                    from.getString("name"),
                                    from.getString("role"),
                                    from.getString("addressLineOne"),
                                    orderForMe.getString("orderDate").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    orderForMe.getInt("itemCount"),
                                    orderForMe.getInt("amountPayable"),
                                    orderForMe.getString("currentStatus")
                                )

                                ordersForMeList.add(dataObject)

                            }

                            if (currentPage == 1){
                                populateRecyclerView(ordersForMeList)
                            }
                            else{
                                populateAdditionalData()
                            }
                            binding.swipeRefreshOrdersForMe.isRefreshing = false
                        }

                    }
                    else {
                        Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG).show()
                        binding.swipeRefreshOrdersForMe.isRefreshing = false
                    }

                }catch (e: Exception){
                    Log.i("exception is ", e.toString())
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                    binding.swipeRefreshOrdersForMe.isRefreshing = false
                }
            },
            Response.ErrorListener {
                binding.swipeRefreshOrdersForMe.isRefreshing = false
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