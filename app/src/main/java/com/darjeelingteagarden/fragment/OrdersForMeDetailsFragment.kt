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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.MyDownlineActivity
import com.darjeelingteagarden.adapter.OrderDetailsItemListAdapter
import com.darjeelingteagarden.adapter.OrderStatusHistoryRecyclerAdapter
import com.darjeelingteagarden.adapter.OrdersForMeDetailsRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentOrderDetailsBinding
import com.darjeelingteagarden.databinding.FragmentOrdersForMeDetailsBinding
import com.darjeelingteagarden.model.ItemDetails
import com.darjeelingteagarden.model.OrderStatusHistory
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import java.math.RoundingMode

class OrdersForMeDetailsFragment : Fragment() {

    private lateinit var binding: FragmentOrdersForMeDetailsBinding

    private lateinit var mContext: Context
    private lateinit var orderId: String
    private var fromUserId = ""
    private var orderDetailsLoaded = false

    private lateinit var recyclerViewItemList: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var itemListRecyclerAdapter: OrdersForMeDetailsRecyclerAdapter

    // Order Status History
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
        binding =  FragmentOrdersForMeDetailsBinding.inflate(inflater, container, false)

//        binding.swipeRefreshOrdersForMeDetails.isRefreshing = true

        recyclerViewItemList = binding.recyclerViewItemList
        layoutManager = LinearLayoutManager(mContext)

        recyclerViewOrderTimeline = binding.recyclerViewOrdersStatusTimeline
        layoutManagerOrderTimeline = LinearLayoutManager(mContext)

        orderId = AppDataSingleton.getOrderForMeId
        Log.i("order for me id", orderId)
        getOrderDetails(orderId)

        binding.btnUpdateStatus.setOnClickListener {
            it.isEnabled = false
            MaterialAlertDialogBuilder(mContext)
                .setTitle("Update Status")
                .setMessage(binding.textInputEditTextOrderStatus.text.toString())
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, int ->
                    updateOrderStatus(orderId, binding.textInputEditTextOrderStatus.text.toString(), it)
                }
                .setNegativeButton("No"){dialog, int ->
                    dialog.dismiss()
                    it.isEnabled = true
                }.show()

        }

        binding.btnDeliverAll.setOnClickListener {
            it.isEnabled = false
            MaterialAlertDialogBuilder(mContext)
                .setTitle("Deliver All")
                .setMessage("Are you sure you want to all the items?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, int ->
                    deliverAll(orderId, it)
                }
                .setNegativeButton("No"){dialog, int ->
                    dialog.dismiss()
                    it.isEnabled = true
                }.show()


        }

        binding.card2.setOnClickListener {
            if (orderDetailsLoaded){
                AppDataSingleton.myDownlineUserId = fromUserId
                AppDataSingleton.showUserDetails = true
                val intent = Intent(mContext, MyDownlineActivity::class.java)
                startActivity(intent)
            }

        }

        binding.btnMarkAsPaid.setOnClickListener {
            it.isEnabled = false
            MaterialAlertDialogBuilder(mContext)
                .setTitle("Mark as Paid")
                .setMessage("Are you sure you want to mark this order as Paid?")
                .setCancelable(true)
                .setPositiveButton("Yes"){dialog, int ->
                    markAsPaid(orderId, it)
                }
                .setNegativeButton("No"){dialog, int ->
                    dialog.dismiss()
                    it.isEnabled = true
                }.show()
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

        return binding.root
    }

    private fun populateRecyclerView(itemList: MutableList<ItemDetails>){

        itemListRecyclerAdapter = OrdersForMeDetailsRecyclerAdapter(mContext, itemList){productId, quantity, productName, isSample ->
            deliverItem(AppDataSingleton.getOrderForMeId, productId, quantity, productName, isSample)
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

    private fun deliverItem(orderId: String, productId: String, quantity: Int, productName: String, isSample: Boolean){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/orders/deliver"

        val jsonParams = JSONObject()
        jsonParams.put("orderId", orderId)
        jsonParams.put("productId", productId)
        jsonParams.put("quantity", quantity)
        jsonParams.put("productName", productName)
        jsonParams.put("isSample", isSample)

        Log.d("deliverJsonParams", jsonParams.toString())

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try {

                    if (it.getBoolean("success")){

                        val orderDetails = it.getJSONObject("data")

                        if (orderDetails.getString("currentStatus") == "Delivered"){
                            binding.textInputEditTextOrderStatus.visibility = View.GONE
                            binding.btnUpdateStatus.visibility = View.GONE
                            binding.btnDeliverAll.visibility = View.GONE

                            val currentStatus = orderDetails.getString("currentStatus")
                            binding.txtOrderStatus.text = currentStatus
                        }

                        val items = orderDetails.getJSONArray("items")

                        itemList = mutableListOf()

                        for (i in 0 until items.length()){

                            val item = items.getJSONObject(i)

                            var receiveQuantity: Int? = 0
                            var receiveTime = ""

                            if (item.getString("status") == "Active"){
                                receiveQuantity = item.optJSONObject("sellerAcknowledgment")?.optInt("quantityDelivered")
                            } else if (item.getString("status") == "Delivered"){
                                receiveTime = item.getJSONObject("sellerAcknowledgment").getString("acknowledgedAt")
                                receiveTime = receiveTime.toDate()!!.formatTo("dd MMM yyy HH:mm")
                            }

                            if (receiveQuantity == null){
                                receiveQuantity = 0
                            }

                            itemList.add(
                                ItemDetails(
                                    item.getString("productId"),
                                    item.getString("productName"),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
//                                    item.getInt("recievedQuantity"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
                                )
                            )
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

                    }

                }catch (e: Exception){
                    Toast.makeText(mContext, "An error occurred; $e", Toast.LENGTH_LONG ).show()
                    populateRecyclerView(itemList)
                }
            },
            Response.ErrorListener {
                populateRecyclerView(itemList)
                val response = JSONObject(String(it.networkResponse.data))
                Log.d("err res deliver", response.getString("message"))
                Toast.makeText(mContext, response.getString("message"), Toast.LENGTH_LONG).show()
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

                        val orderBy = orderDetails.getJSONObject("from")
                        fromUserId = orderBy.getString("userId")
                        binding.txtFromName.text = orderBy.getString("name")
                        binding.txtFromRole.text = orderBy.getString("role")
                        binding.txtFromAddress.text =
                            orderBy.optString("addressLineOne") + "\n" + orderBy.optString("addressLineTwo")

                        Log.i("Order details :: ", orderDetails.toString())

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus == "Delivered"){
                            binding.llAdminSampleStatus.visibility = View.GONE
                        }

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = currentStatus

                        val paymentDone = orderDetails.getBoolean("paymentDone")
                        if (paymentDone){
                            binding.txtPaymentStatus.text = "PAID"
                            binding.btnMarkAsPaid.visibility = View.GONE
                        }
                        else{
                            binding.txtPaymentStatus.text = "PENDING"
                            binding.btnMarkAsPaid.visibility = View.VISIBLE
                        }

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")

                        binding.txtItemsPrice.text = String.format("%.2f", orderDetails.getDouble("itemsPrice"))
                        binding.txtDiscount.text = String.format("%.2f", orderDetails.getDouble("discount"))
                        binding.txtTax.text = String.format("%.2f", orderDetails.getDouble("totalTax"))
                        binding.txtTotal.text = String.format("%.2f", orderDetails.getDouble("amountPayable"))

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
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
                                )
                            )
                        }

                        if (currentStatus == "Active" && activeOrders && !deliveredOrders){
                            binding.txtOrderStatus.text = "ACTIVE"
                        }else if (!activeOrders && deliveredOrders){
                            binding.txtOrderStatus.text = "DELIVERED"
                        }else if (activeOrders && deliveredOrders){
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

//                        binding.swipeRefreshOrdersForMeDetails.isRefreshing = false
                        if (
                            !currentStatus.equals("Delivered", ignoreCase = true) &&
                            AppDataSingleton.getUserInfo.role.equals("admin", ignoreCase = true)
                        ){
                            binding.llAdminSampleStatus.visibility = View.VISIBLE
                        }

                        orderDetailsLoaded = true

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

    private fun updateOrderStatus(orderId: String, status: String, btn: View){

        val queue = Volley.newRequestQueue(mContext)

        val url = "${getString(R.string.homeUrl)}api/v1/orders/updateStatus/${orderId}"

        val jsonBody = JSONObject()
        jsonBody.put("status", status)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.PUT,
            url,
            jsonBody,
            Response.Listener {
                try {

                    btn.isEnabled = true

                    val success = it.getBoolean("success")

                    if (success){

                        val orderDetails = it.getJSONObject("data")

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus == "Delivered"){
                            binding.llAdminSampleStatus.visibility = View.GONE
                        }

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

                        binding.textInputEditTextOrderStatus.setText("")

                        Toast.makeText(
                            mContext, "Status updated successfully", Toast.LENGTH_LONG
                        ).show()

                    }else{
                        Toast.makeText(
                            mContext, "cannot update status", Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e: Exception){
                    btn.isEnabled = true
                    Toast.makeText(
                        mContext, "cannot update status : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                btn.isEnabled = true
                Toast.makeText(
                    mContext, "cannot update status (error)", Toast.LENGTH_LONG
                ).show()
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

    private fun deliverAll(orderId: String, btn: View){

        val queue = Volley.newRequestQueue(mContext)

        val url = "${getString(R.string.homeUrl)}api/v1/orders/deliverAll"

        val jsonBody = JSONObject()
        jsonBody.put("orderId", orderId)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        btn.visibility = View.GONE

                        binding.textInputEditTextOrderStatus.visibility = View.GONE
                        binding.btnUpdateStatus.visibility = View.GONE
                        binding.btnDeliverAll.visibility = View.GONE

                        val orderDetails = it.getJSONObject("data")

                        Log.i("Order details :: ", orderDetails.toString())

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus == "Delivered"){
                            binding.llAdminSampleStatus.visibility = View.GONE
                        }

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = currentStatus

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")


                        val items = orderDetails.getJSONArray("items")

                        var activeOrders = false
                        var deliveredOrders = false

                        itemList = mutableListOf()

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
                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
                                )
                            )
                        }

                        if (activeOrders && !deliveredOrders){
                            binding.txtOrderStatus.text = "ACTIVE"
                        }else if (!activeOrders && deliveredOrders){
                            binding.txtOrderStatus.text = "DELIVERED"
                        }else if (activeOrders && deliveredOrders){
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

//                        binding.swipeRefreshOrdersForMeDetails.isRefreshing = false
                        if (
                            !currentStatus.equals("Delivered", ignoreCase = true) &&
                            AppDataSingleton.getUserInfo.role.equals("admin", ignoreCase = true)
                        ){
                            binding.llAdminSampleStatus.visibility = View.VISIBLE
                        }

                    }else{
                        Toast.makeText(
                            mContext, "cannot update status", Toast.LENGTH_LONG
                        ).show()
                    }

                    btn.isEnabled = true

                }catch (e: Exception){
                    btn.isEnabled = true
                    Toast.makeText(
                        mContext, "cannot update status : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                btn.isEnabled = true
                Toast.makeText(
                    mContext, "cannot update status (error)", Toast.LENGTH_LONG
                ).show()
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

    private fun markAsPaid(orderId: String, btn: View){

        val queue = Volley.newRequestQueue(mContext)

        val url = "${getString(R.string.homeUrl)}api/v1/orders/paid"

        val jsonBody = JSONObject()
        jsonBody.put("orderId", orderId)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        btn.visibility = View.GONE

                        val orderDetails = it.getJSONObject("data")

                        Log.i("Order details :: ", orderDetails.toString())

                        val currentStatus = orderDetails.getString("currentStatus")

                        if (currentStatus == "Delivered"){
                            binding.llAdminSampleStatus.visibility = View.GONE
                        }

                        binding.txtOrderId.text = orderDetails.getString("_id")
                        binding.txtOrderStatus.text = currentStatus

                        binding.txtOrderedOn.text = orderDetails.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")

                        val paymentDone = orderDetails.getBoolean("paymentDone")
                        if (paymentDone){
                            binding.txtPaymentStatus.text = "PAID"
                        }
                        else{
                            binding.txtPaymentStatus.text = "PENDING"
                        }

                        val items = orderDetails.getJSONArray("items")

                        var activeOrders = false
                        var deliveredOrders = false

                        itemList = mutableListOf()

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
                                    AppDataSingleton.getProductNameById(item.getString("productId")),
                                    item.getInt("price"),
                                    item.getInt("orderQuantity"),
                                    item.getString("status"),
                                    receiveQuantity,
                                    receiveTime,
                                    item.optBoolean("isSample")
                                )
                            )
                        }

                        if (activeOrders && !deliveredOrders){
                            binding.txtOrderStatus.text = "ACTIVE"
                        }else if (!activeOrders && deliveredOrders){
                            binding.txtOrderStatus.text = "DELIVERED"
                        }else if (activeOrders && deliveredOrders){
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

//                        binding.swipeRefreshOrdersForMeDetails.isRefreshing = false
                        if (
                            !currentStatus.equals("Delivered", ignoreCase = true) &&
                            AppDataSingleton.getUserInfo.role.equals("admin", ignoreCase = true)
                        ){
                            binding.llAdminSampleStatus.visibility = View.VISIBLE
                        }

                    }else{
                        Toast.makeText(
                            mContext, "cannot update status", Toast.LENGTH_LONG
                        ).show()
                    }

                    btn.isEnabled = true

                }catch (e: Exception){
                    btn.isEnabled = true
                    Toast.makeText(
                        mContext, "cannot update status : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                btn.isEnabled = true
                Toast.makeText(
                    mContext, "cannot update status.", Toast.LENGTH_LONG
                ).show()
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