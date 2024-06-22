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
import android.webkit.URLUtil
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
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class OrderDetailsFragment : Fragment() {

    private lateinit var mContext: Context
    private lateinit var binding: FragmentOrderDetailsBinding
    private lateinit var orderId: String

    private lateinit var recyclerViewItemList: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var itemListRecyclerAdapter: OrderDetailsItemListAdapter

    // Order Status History
    private lateinit var recyclerViewOrderTimeline: RecyclerView
    private lateinit var layoutManagerOrderTimeline: RecyclerView.LayoutManager
    private lateinit var recyclerAdapterOrderStatusHistory: OrderStatusHistoryRecyclerAdapter
    private var orderStatusHistory = mutableListOf<OrderStatusHistory>()

    private var itemList: MutableList<ItemDetails> = mutableListOf()

    private var invoiceLink = ""

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

        if (NotificationDataSingleton.notificationToOpen){
            NotificationDataSingleton.notificationToOpen = false
            orderId = NotificationDataSingleton.resourceId.toString()
            getOrderDetails(orderId)
        }
        else{
            orderId = AppDataSingleton.getOrderId
            Log.i("order for me id", orderId)
            getOrderDetails(orderId)
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

        binding.btnCancelOrder.setOnClickListener {

            MaterialAlertDialogBuilder(mContext)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, int ->
                    cancelOrder(orderId)
                }
                .setNegativeButton("No"){dialog, int ->
                    dialog.dismiss()
                }.show()

        }

        binding.btnInvoice.setOnClickListener {
            if (invoiceLink.isNotBlank()){
                if (!invoiceLink.startsWith("https://") && !invoiceLink.startsWith("http://")){
                    invoiceLink = "https://$invoiceLink"
                }
                val uri = Uri.parse(invoiceLink)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

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

//                            itemList.add(
//                                ItemDetails(
//                                    item.getString("productId"),
//                                    AppDataSingleton.getProductNameById(item.getString("productId")),
//                                    item.getInt("price"),
//                                    item.getInt("orderQuantity"),
//                                    item.getString("status"),
//                                    receiveQuantity,
//                                    receiveTime
//                                )
//                            )
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

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus.equals("Cancelled", ignoreCase = true) || currentStatus.equals("Delivered", ignoreCase = true)){
                            binding.btnCancelOrder.visibility = View.GONE
                        }

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = currentStatus

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")

                        binding.txtItemsPrice.text = String.format("%.2f", orderDetails.getDouble("itemsPrice"))
                        binding.txtDiscount.text = String.format("%.2f", orderDetails.getDouble("discount"))
                        binding.txtTax.text = String.format("%.2f", orderDetails.getDouble("totalTax"))
                        binding.txtTotal.text = String.format("%.2f", orderDetails.getDouble("amountPayable"))

                        val paymentDone = orderDetails.getBoolean("paymentDone")
                        if (paymentDone){
                            binding.txtPaymentStatus.text = "PAID"
                        }
                        else{
                            binding.txtPaymentStatus.text = "PENDING"
                        }

                        invoiceLink = orderDetails.optString("invoiceLink")
//                        invoiceLink = "darjeelingteagarden.com"
                        if (invoiceLink.isNotBlank() && URLUtil.isValidUrl(invoiceLink)){
                            binding.btnInvoice.visibility = View.VISIBLE
                        }
                        else{
                            binding.btnInvoice.visibility = View.GONE
                        }

                        val items = orderDetails.getJSONArray("items")

                        var activeOrders = false
                        var deliveredOrders = false

                        for (i in 0 until items.length()){

                            val item = items.getJSONObject(i)

                            var receiveQuantity: Int? = 0
                            var receiveTime = ""

                            if (item.getString("status") == "Active"){
                                activeOrders = true
                            }else if (item.getString("status") == "Delivered"){
                                deliveredOrders = true
                            }

                            if (item.getString("status") == "Active"){
                                receiveQuantity = item.optJSONObject("sellerAcknowledgment")?.optInt("quantityDelivered")
                            } else if (item.getString("status") == "Delivered"){
                                receiveTime = item.getJSONObject("sellerAcknowledgment")
                                    .getString("acknowledgedAt").toDate()!!.formatTo("dd MMM yyy HH:mm")
//                                receiveTime.toDate()!!.formatTo("dd MMM yyy HH:mm")
                            }

                            if (receiveQuantity == null){
                                receiveQuantity = 0
                            }

                            itemList.add(
                                ItemDetails(
                                    item.getString("productId"),
                                    item.getString("productName"),
//                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
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
                                    statusHistory.getJSONObject(i).getString("updatedOn")
                                        .toDate()!!.formatTo("dd MMM yyyy HH:mm"),
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

    private fun cancelOrder(orderId: String){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/cancel"

        val jsonParams = JSONObject()
        jsonParams.put("orderId", orderId)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if(success){

                        val orderDetails = it.getJSONObject("data")

                        Log.i("Order details :: ", orderDetails.toString())

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus.equals("Cancelled", ignoreCase = true)){
                            binding.btnCancelOrder.visibility = View.GONE
                        }

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = currentStatus

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")

                        binding.txtItemsPrice.text = String.format("%.2f", orderDetails.getDouble("itemsPrice"))
                        binding.txtDiscount.text = String.format("%.2f", orderDetails.getDouble("discount"))
                        binding.txtTax.text = String.format("%.2f", orderDetails.getDouble("totalTax"))
                        binding.txtTotal.text = String.format("%.2f", orderDetails.getDouble("amountPayable"))

                        val items = orderDetails.getJSONArray("items")

                        var activeOrders = false
                        var deliveredOrders = false

                        itemList.clear()

                        for (i in 0 until items.length()){

                            val item = items.getJSONObject(i)

                            var receiveQuantity: Int? = 0
                            var receiveTime = ""

                            if (item.getString("status") == "Active"){
                                activeOrders = true
                            }else if (item.getString("status") == "Delivered"){
                                deliveredOrders = true
                            }

                            if (item.getString("status") == "Active"){
                                receiveQuantity = item.optJSONObject("sellerAcknowledgment")?.optInt("quantityDelivered")
                            } else if (item.getString("status") == "Delivered"){
                                receiveTime = item.getJSONObject("sellerAcknowledgment")
                                    .getString("acknowledgedAt").toDate()!!.formatTo("dd MMM yyy HH:mm")
//                                receiveTime.toDate()!!.formatTo("dd MMM yyy HH:mm")
                            }

                            if (receiveQuantity == null){
                                receiveQuantity = 0
                            }

                            itemList.add(
                                ItemDetails(
                                    item.getString("productId"),
                                    item.getString("productName"),
//                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
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
                                    statusHistory.getJSONObject(i).getString("updatedOn")
                                        .toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    ""
                                )
                            )

                        }

                        populateRecyclerViewOrderTimeline(orderStatusHistory)

                        binding.rlProgressBarOrderDetails.visibility = View.GONE

                    }
                    else{
                        Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG ).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(mContext, "An error occurred.", Toast.LENGTH_LONG ).show()
//                Log.i("ErrorListener", JSONObject(String(it.networkResponse.data)).toString())

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