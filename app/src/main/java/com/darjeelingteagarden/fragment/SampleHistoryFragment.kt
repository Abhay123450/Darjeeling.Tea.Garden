package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.LoginActivity
import com.darjeelingteagarden.activity.SampleOrderDetailsActivity
import com.darjeelingteagarden.adapter.MyOrdersRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleHistoryRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleOrderDetailsItemListRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentSampleHistoryBinding
import com.darjeelingteagarden.model.MyOrder
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.model.SampleOrder
import com.darjeelingteagarden.model.SampleOrderItemDetails
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate

class SampleHistoryFragment : Fragment() {

    private lateinit var mContext: Context

    //Sample Order History
    private lateinit var recyclerViewSampleHistory: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: SampleHistoryRecyclerAdapter

    //Sample Order details
    private lateinit var recyclerViewSampleOrderDetails: RecyclerView
    private lateinit var layoutManagerSampleOrderDetails: RecyclerView.LayoutManager
    private lateinit var recyclerAdapterSampleOrderDetails: SampleOrderDetailsItemListRecyclerAdapter
    private var sampleDetailsItemList = mutableListOf<SampleOrderItemDetails>()

    private var totalSampleOrders = 0
    private val limit = 10

    private lateinit var binding: FragmentSampleHistoryBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSampleHistoryBinding.inflate(inflater, container, false)

        recyclerViewSampleHistory = binding.recyclerViewSampleHistory
        layoutManager = LinearLayoutManager(mContext)
        recyclerViewSampleOrderDetails = binding.recyclerViewSampleOrderDetails
        layoutManagerSampleOrderDetails = LinearLayoutManager(mContext)

        if (SampleDataSingleton.getSampleHistoryList.size == 0){
            SampleDataSingleton.currentPage = 1
            getSampleHistory(SampleDataSingleton.currentPage)
        }
        else {
            populateRecyclerView(SampleDataSingleton.getSampleHistoryList)
        }

        binding.imgClose.setOnClickListener {
            binding.cardSampleOrderDetails.visibility = View.GONE
        }
        
        binding.btnLoadMoreSampleOrders.setOnClickListener { 
            
            getSampleHistory(++SampleDataSingleton.currentPage)
            
        }

        return binding.root

    }

    private fun populateRecyclerView(sampleOrderList: MutableList<SampleOrder>){

        recyclerAdapter = SampleHistoryRecyclerAdapter(mContext, sampleOrderList){
            val intent = Intent(mContext, SampleOrderDetailsActivity::class.java)
            intent.putExtra("sampleOrderId", it.orderId)
            startActivity(intent)
        }
        recyclerViewSampleHistory.adapter = recyclerAdapter
        recyclerViewSampleHistory.layoutManager = layoutManager

        binding.progressBarSampleOrders.visibility = View.GONE


        var text = ""

        if (totalSampleOrders > SampleDataSingleton.currentPage * limit){
            text = "Showing 1 - ${SampleDataSingleton.currentPage * limit} of $totalSampleOrders orders"
            binding.btnLoadMoreSampleOrders.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalSampleOrders of $totalSampleOrders orders"
            binding.btnLoadMoreSampleOrders.visibility = View.GONE
        }

        binding.txtPage.text = text

    }

    private fun populateAdditionalData(){
        recyclerAdapter.notifyItemRangeInserted((SampleDataSingleton.currentPage - 1) * limit + 1, limit)
        binding.progressBarSampleOrders.visibility = View.GONE

        var text = ""

        if (totalSampleOrders > SampleDataSingleton.currentPage * limit){
            text = "Showing 1 - ${SampleDataSingleton.currentPage * limit} of $totalSampleOrders orders"
            binding.btnLoadMoreSampleOrders.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalSampleOrders of $totalSampleOrders orders"
            binding.btnLoadMoreSampleOrders.visibility = View.GONE
        }

        binding.txtPage.text = text

    }

    private fun populateRecyclerViewItemDetails(itemList: MutableList<SampleOrderItemDetails>){
        recyclerAdapterSampleOrderDetails = SampleOrderDetailsItemListRecyclerAdapter(mContext, itemList)
        recyclerViewSampleOrderDetails.adapter = recyclerAdapterSampleOrderDetails
        recyclerViewSampleOrderDetails.layoutManager = layoutManagerSampleOrderDetails
    }

    private fun getSampleHistory(page: Int){


        val query = "?page=$page&limit=$limit"

        val queue = Volley.newRequestQueue(mContext)
//
        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder" + query

        Log.i("my orders url::: ", url)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")
                    Log.i("success is ::", success.toString())

                    if (success){

                        totalSampleOrders = it.getInt("itemsCount")

//                        Log.i("data string is ::", data.toString())

                        if (totalSampleOrders == 0){
                            Toast.makeText(mContext, "No Orders !", Toast.LENGTH_LONG).show()
                            binding.progressBarSampleOrders.visibility = View.GONE
                        }
                        else{

                            val data = it.getJSONArray("data")

                            for (i in 0 until data.length()){

                                val order = data.getJSONObject(i)

//                                val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                    LocalDate.parse(order.getString("orderDate"))
//                                } else {
//                                    SimpleDateFormat(,)
//                                }

                                val dataObject = SampleOrder(
                                    order.getString("_id"),
                                    order.getString("orderDate").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    order.getInt("itemCount"),
                                    order.getDouble("totalPrice"),
                                    order.getString("currentStatus")
                                )

                                SampleDataSingleton.addSampleOrder(dataObject)

                            }

                            if (SampleDataSingleton.currentPage == 1){
                                populateRecyclerView(SampleDataSingleton.getSampleHistoryList)
                            }
                            else{
                                populateAdditionalData()
                            }

                        }

                    }
                    else {
                        Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG).show()
                    }

                }catch (e: Exception){
                    Log.i("exception is ", e.toString())
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                if (it.networkResponse.statusCode == 401 || it.networkResponse.statusCode == 403){
                    val intent = Intent(mContext, LoginActivity::class.java)
                    intent.putExtra("resume", true)
                    startActivity(intent)
                    return@ErrorListener
                }
                Toast.makeText(mContext, "An Error Occurred", Toast.LENGTH_LONG).show()
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

//            override fun parseNetworkResponse(response: NetworkResponse?): Response<JSONObject> {
//                return super.parseNetworkResponse(response)
////                return Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
//            }
//
//            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
//                return super.parseNetworkError(volleyError)
//            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun getSampleOrderDetails(orderId: String){

        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder/$orderId"

        val queue = Volley.newRequestQueue(mContext)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        sampleDetailsItemList = mutableListOf()

                        val data = it.getJSONObject("data")

                        binding.txtOrderId.text = data.getString("_id")
                        binding.txtOrderedOn.text = data.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")
                        binding.txtOrderStatus.text = data.getString("currentStatus")

                        binding.txtItemsPrice.text = data.getDouble("itemsPrice").toString()
                        binding.txtTax.text = data.getDouble("totalTax").toString()
                        binding.txtTotal.text = data.getDouble("totalPrice").toString()

                        val itemList = data.getJSONArray("items")

                        for (i in 0 until itemList.length()){

                            val item = itemList.getJSONObject(i)

//                            sampleDetailsItemList.add(
//                                SampleOrderItemDetails(
//                                    item.getString("sampleId"),
//                                    "Sample name !!",
//                                    "",
//                                    "",
//                                    item.getDouble("price"),
//                                    item.getInt("orderQuantity")
//                                )
//                            )

                        }

                        Log.i("SampleOrderDetailItems", sampleDetailsItemList.toString())

                        populateRecyclerViewItemDetails(sampleDetailsItemList)
                        binding.rlProgressOrderDetails.visibility = View.GONE

                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(mContext, "An Error Occurred", Toast.LENGTH_LONG).show()
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















