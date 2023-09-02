package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.OrderDetailsItemListAdapter
import com.darjeelingteagarden.adapter.OrderStatusHistoryRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleOrderStatusHistoryRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentOrderDetailsBinding
import com.darjeelingteagarden.model.ItemDetails
import com.darjeelingteagarden.model.OrderStatusHistory
import com.darjeelingteagarden.model.StatusHistory
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import org.json.JSONObject

class OrderDetailsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var orderId: String

    private lateinit var recyclerViewItemList: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var itemListRecyclerAdapter: OrderDetailsItemListAdapter

    //Sample Order Status History
    private lateinit var recyclerViewOrderTimeline: RecyclerView
    private lateinit var layoutManagerOrderTimeline: RecyclerView.LayoutManager
    private lateinit var recyclerAdapterOrderStatusHistory: OrderStatusHistoryRecyclerAdapter
    private var orderStatusHistory = mutableListOf<OrderStatusHistory>()

    private var itemList: MutableList<ItemDetails> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_order_details, container, false)
        binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)

        recyclerViewItemList = binding.recyclerViewItemList
        layoutManager = LinearLayoutManager(mContext)

        recyclerViewOrderTimeline = binding.recyclerViewOrdersStatusTimeline
        layoutManagerOrderTimeline = LinearLayoutManager(mContext)

        binding.rlProgressBarOrderDetails.visibility = View.VISIBLE

        orderId = AppDataSingleton.getOrderId
        Log.i("order for me id", orderId)
        getOrderDetails(orderId)

        return binding.root
    }

    private fun populateRecyclerView(itemList: MutableList<ItemDetails>){

        itemListRecyclerAdapter = OrderDetailsItemListAdapter(mContext, itemList){
            Log.i("itemReceived Clicked", it.toString())
            receiveItem(AppDataSingleton.getOrderId, it.id, it.itemQuantity)
        }
        recyclerViewItemList.adapter = itemListRecyclerAdapter
        recyclerViewItemList.layoutManager = layoutManager

    }

    private fun populateRecyclerViewOrderTimeline(statusList: MutableList<OrderStatusHistory>){
        recyclerAdapterOrderStatusHistory =
            OrderStatusHistoryRecyclerAdapter(mContext, statusList)
        recyclerViewOrderTimeline.adapter = recyclerAdapterOrderStatusHistory
        recyclerViewOrderTimeline.layoutManager = layoutManagerOrderTimeline
    }

    private fun receiveItem(orderId: String, productId: String, quantity: Int){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/receive"

        val jsonParams = JSONObject()
        jsonParams.put("orderId", orderId)
        jsonParams.put("productId", productId)
        jsonParams.put("quantity", quantity)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try {

                    if (it.getBoolean("success")){

                        val orderDetails = it.getJSONObject("data")

                        val items = orderDetails.getJSONArray("items")

                        itemList = mutableListOf()

                        for (i in 0 until items.length()){

                            val item = items.getJSONObject(i)

                            var receiveQuantity = 0
                            var receiveTime = ""

                            if (item.getString("status") == "Waiting"){
                                receiveQuantity = item.getJSONObject("sellerAcknowledgment").getInt("quantityDelivered")
                            } else if (item.getString("status") == "Delivered"){
                                receiveTime = item.getJSONObject("buyerAcknowledgment").getString("acknowledgedAt")
                                receiveTime = receiveTime.toDate()!!.formatTo("dd MMM yyy HH:mm")
                            }

                            itemList.add(
                                ItemDetails(
                                    item.getString("productId"),
                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime
                                )
                            )
                        }

                        populateRecyclerView(itemList)

                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred; $e", Toast.LENGTH_LONG ).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG).show()
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

    private fun getOrderDetails(orderId: String){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/" + orderId

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if(success){

                        val orderDetails = it.getJSONObject("data")

                        Log.i("Order details :: ", orderDetails.toString())

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = orderDetails.getString("currentStatus")

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")

                        binding.txtItemsPrice.text = orderDetails.getInt("itemsPrice").toString()
                        binding.txtDiscount.text = orderDetails.getInt("discount").toString()
                        binding.txtTax.text = orderDetails.getInt("totalTax").toString()
                        binding.txtTotal.text = orderDetails.getInt("amountPayable").toString()

                        val items = orderDetails.getJSONArray("items")

                        var activeOrders = false
                        var deliveredOrders = false

                        for (i in 0 until items.length()){

                            val item = items.getJSONObject(i)

                            var receiveQuantity = 0
                            var receiveTime = ""

                            if (item.getString("status") == "Active"){
                                activeOrders = true
                            }else if (item.getString("status") == "Delivered"){
                                deliveredOrders = true
                            }

//                            if (item.getString("status") == "Waiting"){
//                                receiveQuantity = item.getJSONObject("sellerAcknowledgment").getInt("quantityDelivered")
//                            } else if (item.getString("status") == "Delivered"){
//                                receiveTime = item.getJSONObject("buyerAcknowledgment")
//                                    .getString("acknowledgedAt").toDate()!!.formatTo("dd MMM yyy HH:mm")
////                                receiveTime.toDate()!!.formatTo("dd MMM yyy HH:mm")
//                            }

                            itemList.add(
                                ItemDetails(
                                    item.getString("productId"),
                                    item.getString("productName"),
//                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime
                                )
                            )
                        }

//                        if (activeOrders && !deliveredOrders){
//                            binding.txtOrderStatus.text = "ACTIVE"
//                        }else if (!activeOrders && deliveredOrders){
//                            binding.txtOrderStatus.text = "DELIVERED"
//                        }else if (activeOrders && deliveredOrders){
//                            binding.txtOrderStatus.text = "PARTIALLY DELIVERED"
//                        }
                        if (activeOrders && deliveredOrders){
                            binding.txtOrderStatus.text = "PARTIALLY DELIVERED"
                        }

                        populateRecyclerView(itemList)

                        val statusHistory = orderDetails.getJSONArray("statusHistory")
                        orderStatusHistory = mutableListOf()

                        for (i in 0 until statusHistory.length()){

                            orderStatusHistory.add(
                                OrderStatusHistory(
                                    statusHistory.getJSONObject(i).getString("status"),
                                    statusHistory.getJSONObject(i).getString("updatedOn"),
                                    ""
                                )
                            )

                        }

                        populateRecyclerViewOrderTimeline(orderStatusHistory)

                        binding.rlProgressBarOrderDetails.visibility = View.GONE

                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred; $e", Toast.LENGTH_LONG ).show()
                }
            },
            Response.ErrorListener {

                Log.i("ErrorListener", JSONObject(String(it.networkResponse.data)).toString())

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