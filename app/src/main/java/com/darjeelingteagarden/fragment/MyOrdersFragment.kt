package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.LoginActivity
import com.darjeelingteagarden.adapter.MyOrdersRecyclerAdapter
import com.darjeelingteagarden.adapter.StoreRecyclerAdapter
import com.darjeelingteagarden.databinding.ActivityMyOrdersBinding
import com.darjeelingteagarden.databinding.FragmentMyOrdersBinding
import com.darjeelingteagarden.model.MyOrder
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate

class MyOrdersFragment : Fragment() {

    lateinit var mContext: Context

    private var currentPage = 1
    private var status = "All"
    private var sort = 1 //1 > orderDate, 2> -orderDate, 3> amountPayable, 4> -amountPayable
    private val limit = 10
    private var totalOrders = 0

    private val sortList = mutableListOf("Newest First", "Oldest First", "Lowest Order Value", "Highest Order Value")

    private var myOrdersList = mutableListOf<MyOrder>()

    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerViewMyOrders: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var myOrdersRecyclerAdapter: MyOrdersRecyclerAdapter

    private lateinit var binding: FragmentMyOrdersBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false)

        recyclerViewMyOrders = binding.recyclerViewMyOrders
        layoutManager = LinearLayoutManager(mContext)

        if (NotificationDataSingleton.notificationToOpen){
            if (NotificationDataSingleton.activityToOpen == "orderDetails"){
                findNavController().navigate(R.id.action_myOrdersFragment_to_orderDetailsFragment)
            }
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

        binding.autoCompleteTextViewOrderStatus.setOnItemClickListener { adapterView, view, i, l ->
            status = adapterView.getItemAtPosition(i).toString()
            AppDataSingleton.clearMyOrdersList()
            currentPage = 1
            getMyOrders(currentPage)
        }

        binding.autoCompleteTextViewSort.setOnItemClickListener { adapterView, view, i, l ->
            sort = i + 1
            AppDataSingleton.clearMyOrdersList()
            currentPage = 1
            getMyOrders(currentPage)
        }

        binding.btnLoadMoreMyOrders.setOnClickListener {
            getMyOrders(++currentPage)
        }

        return binding.root

    }

    override fun onResume() {
        super.onResume()
        initializeFilterDropdown()
        initializeSortDropdown()

        if (AppDataSingleton.getMyOrdersList.size == 0){
            getMyOrders(currentPage)
        }
        else {
            populateRecyclerView(AppDataSingleton.getMyOrdersList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppDataSingleton.clearMyOrdersList()
    }

    private fun initializeFilterDropdown(){

        val statusList = arrayListOf("All", "Active", "Delivered")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, statusList)
        binding.autoCompleteTextViewOrderStatus.setAdapter(arrayAdapter)

    }

    private fun initializeSortDropdown(){
        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, sortList)
        binding.autoCompleteTextViewSort.setAdapter(arrayAdapter)
    }

    private fun populateAdditionalData(){

        myOrdersRecyclerAdapter.notifyItemRangeInserted((currentPage - 1) * limit + 1, limit)
        binding.progressBarMyOrders.visibility = View.GONE

        var text = ""

        if (totalOrders > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalOrders orders"
            binding.btnLoadMoreMyOrders.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalOrders of $totalOrders orders"
            binding.btnLoadMoreMyOrders.visibility = View.GONE
        }

        binding.txtPage.text = text
    }

    private fun populateRecyclerView(ordersList: MutableList<MyOrder>){

//        recyclerAdapter = StoreRecyclerAdapter(activity as Context, productList, cartList)
//        recyclerViewStore.adapter = recyclerAdapter
//        recyclerViewStore.layoutManager = layoutManager
//        recyclerViewStore.addItemDecoration(
//            DividerItemDecoration(
//                recyclerViewStore.context,
//                (layoutManager as LinearLayoutManager).orientation
//            )
//        )

        myOrdersRecyclerAdapter = MyOrdersRecyclerAdapter(mContext, ordersList, findNavController())
        recyclerViewMyOrders.adapter = myOrdersRecyclerAdapter
        recyclerViewMyOrders.layoutManager = layoutManager
        binding.progressBarMyOrders.visibility = View.GONE

        var text = ""

        if (totalOrders > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalOrders orders"
            binding.btnLoadMoreMyOrders.visibility = View.VISIBLE
        } else {
            text = "Showing 1 - $totalOrders of $totalOrders orders"
            binding.btnLoadMoreMyOrders.visibility = View.GONE
        }

        binding.txtPage.text = text

    }

    private fun getMyOrders(page: Int){

        binding.progressBarMyOrders.visibility = View.VISIBLE
        binding.btnLoadMoreMyOrders.visibility = View.GONE

        var query = ""
        if (status == "All"){
            query = "?page=$page&limit=$limit&sort=$sort"
        }else{
            query = "?page=$page&limit=$limit&sort=$sort&status=$status"
        }

        val queue = Volley.newRequestQueue(mContext)
//
        val url = getString(R.string.homeUrl) + "api/v1/orders/myOrders" + query

        Log.i("my orders url::: ", url)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        totalOrders = it.getInt("itemsCount")

                        AppDataSingleton.totalMyOrders = totalOrders
                        AppDataSingleton.currentPageMyOrders = currentPage

//                        Log.i("data string is ::", data.toString())

                        if (totalOrders == 0){
                            Toast.makeText(mContext, "No Orders !", Toast.LENGTH_LONG).show()
                            binding.progressBarMyOrders.visibility = View.GONE
                            if (::myOrdersRecyclerAdapter.isInitialized){
                                myOrdersRecyclerAdapter.notifyDataSetChanged()
                            }
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

                                val dataObject = MyOrder(
                                    order.getString("_id"),
                                    order.getString("orderDate").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    order.getInt("itemCount"),
                                    order.getDouble("amountPayable"),
                                    order.getString("currentStatus")
                                )

//                                myOrdersList.add(dataObject)
                                AppDataSingleton.addToMyOrdersList(dataObject)

                            }

                            if (currentPage == 1){
                                populateRecyclerView(AppDataSingleton.getMyOrdersList)
                            }
                            else{
                                populateAdditionalData()
                            }

                        }

                    }
                    else {
                        Toast.makeText(mContext, "No Orders found", Toast.LENGTH_LONG).show()
                    }

                    binding.progressBarMyOrders.visibility = View.GONE

                }catch (e: Exception){
                    Log.i("exception is ", e.toString())
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                    binding.progressBarMyOrders.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                binding.progressBarMyOrders.visibility = View.GONE
                if (it.networkResponse.statusCode == 401 || it.networkResponse.statusCode == 403){
                    val intent = Intent(mContext, LoginActivity::class.java)
                    intent.putExtra("resume", true)
                    startActivity(intent)
                    return@ErrorListener
                }
                Toast.makeText(mContext, "No Orders found!", Toast.LENGTH_LONG).show()

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


            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                Log.i("Volley Error -> ", volleyError.toString())
                return super.parseNetworkError(volleyError)
            }
        }

        queue.add(jsonObjectRequest)

    }

}