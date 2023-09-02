package com.darjeelingteagarden.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.SampleOrderDetailsItemListRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleOrderStatusHistoryRecyclerAdapter
import com.darjeelingteagarden.databinding.ActivityProductDetailsBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrderDetailsBinding
import com.darjeelingteagarden.model.SampleOrderItemDetails
import com.darjeelingteagarden.model.StatusHistory
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class SampleOrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySampleOrderDetailsBinding

    //Sample Order details
    private lateinit var recyclerViewSampleOrderDetails: RecyclerView
    private lateinit var layoutManagerSampleOrderDetails: RecyclerView.LayoutManager
    private lateinit var recyclerAdapterSampleOrderDetails: SampleOrderDetailsItemListRecyclerAdapter
    private var sampleDetailsItemList = mutableListOf<SampleOrderItemDetails>()

    //Sample Order Status History
    private lateinit var recyclerViewSampleSampleOrderTimeline: RecyclerView
    private lateinit var layoutManagerSampleOrderTimeline: RecyclerView.LayoutManager
    private lateinit var recyclerAdapterSampleOrderStatusHistory: SampleOrderStatusHistoryRecyclerAdapter
    private var sampleStatusHistory = mutableListOf<StatusHistory>()

    private var orderType: String? = null // (order/sampleOrder)
    private var sampleOrderId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySampleOrderDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        recyclerViewSampleOrderDetails = binding.recyclerViewSampleOrderDetails
        layoutManagerSampleOrderDetails = LinearLayoutManager(this@SampleOrderDetailsActivity)

        recyclerViewSampleSampleOrderTimeline = binding.recyclerViewSampleOrdersStatusTimeline
        layoutManagerSampleOrderTimeline = LinearLayoutManager(this@SampleOrderDetailsActivity)


        if (intent != null) {
            sampleOrderId = intent.getStringExtra("sampleOrderId")
        }

        if (sampleOrderId != null) {
            getSampleOrderDetails(sampleOrderId.toString())
        }

        Log.i("sampleorderid", sampleOrderId.toString())

        if (AppDataSingleton.getUserInfo.role.equals("Admin", ignoreCase = true)){
            binding.llAdminSampleStatus.visibility = View.VISIBLE
        }
        else{
            binding.llAdminSampleStatus.visibility = View.GONE
        }

        binding.btnUpdateStatus.setOnClickListener {

            if (!binding.textInputEditTextSampleStatus.text.isNullOrEmpty() && sampleOrderId != null){
                updateSampleOrderStatus(sampleOrderId.toString(), binding.textInputEditTextSampleStatus.text.toString())
            }
            else{
                Toast.makeText(
                    this, "Please enter status", Toast.LENGTH_LONG
                ).show()
            }

        }

        binding.btnDeliver.setOnClickListener {

            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Delivery")
                .setMessage("Mark this order as delivered ?")
                .setPositiveButton("Yes"){ _,_ ->
                    deliverSampleOrder(sampleOrderId.toString())
                }
                .setNegativeButton("No"){dialog,_ ->
                    dialog.dismiss()
                }
                .setCancelable(true)
                .show()

        }

    }

    private fun populateRecyclerViewItemDetails(itemList: MutableList<SampleOrderItemDetails>) {
        recyclerAdapterSampleOrderDetails =
            SampleOrderDetailsItemListRecyclerAdapter(this, itemList)
        recyclerViewSampleOrderDetails.adapter = recyclerAdapterSampleOrderDetails
        recyclerViewSampleOrderDetails.layoutManager = layoutManagerSampleOrderDetails
    }

    private fun populateRecyclerViewSampleOrderTimeline(statusList: MutableList<StatusHistory>){
        recyclerAdapterSampleOrderStatusHistory =
            SampleOrderStatusHistoryRecyclerAdapter(this, statusList)
        recyclerViewSampleSampleOrderTimeline.adapter = recyclerAdapterSampleOrderStatusHistory
        recyclerViewSampleSampleOrderTimeline.layoutManager = layoutManagerSampleOrderTimeline
    }

    private fun getSampleOrderDetails(orderId: String) {

        binding.rlProgressSampleOrderDetails.visibility = View.VISIBLE

        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder/$orderId"

        val queue = Volley.newRequestQueue(this@SampleOrderDetailsActivity)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success) {

                        sampleDetailsItemList = mutableListOf()
                        sampleStatusHistory = mutableListOf()

                        val data = it.getJSONObject("data")

                        binding.txtOrderId.text = data.getString("_id")
                        binding.txtOrderedOn.text =
                            data.getString("orderDate").toDate()!!.formatTo("dd MMM yyy HH:mm")
                        val currentStatus = data.getString("currentStatus")
//                        binding.txtOrderStatus.text = currentStatus

                        val statusHistory = data.getJSONArray("statusHistory")

                        for (i in 0 until statusHistory.length()){

                            sampleStatusHistory.add(
                                StatusHistory(
                                    statusHistory.getJSONObject(i).getString("status"),
                                    statusHistory.getJSONObject(i).getString("updatedOn")
                                        .toDate()!!.formatTo("dd MMM yyy HH:mm")
                                )
                            )

                        }

                        binding.txtItemsPrice.text = data.getDouble("itemsPrice").toString()
                        binding.txtTax.text = data.getDouble("totalTax").toString()
                        binding.txtTotal.text = data.getDouble("totalPrice").toString()

                        val itemList = data.getJSONArray("items")

                        for (i in 0 until itemList.length()) {

                            val item = itemList.getJSONObject(i)

                            sampleDetailsItemList.add(
                                SampleOrderItemDetails(
                                    item.getString("sampleId"),
                                    "Sample name !!",
                                    item.getDouble("price"),
                                    item.getInt("orderQuantity")
                                )
                            )

                        }

                        Log.i("SampleOrderDetailItems", sampleDetailsItemList.toString())

                        populateRecyclerViewItemDetails(sampleDetailsItemList)
                        populateRecyclerViewSampleOrderTimeline(sampleStatusHistory)
                        binding.rlProgressSampleOrderDetails.visibility = View.GONE

                    } else {
                        binding.rlProgressSampleOrderDetails.visibility = View.GONE
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this@SampleOrderDetailsActivity,
                        "An error occurred: $e",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.rlProgressSampleOrderDetails.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    this@SampleOrderDetailsActivity,
                    "An Error Occurred",
                    Toast.LENGTH_LONG
                ).show()
                binding.rlProgressSampleOrderDetails.visibility = View.GONE
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

    private fun updateSampleOrderStatus(orderId: String, status: String){

        val queue = Volley.newRequestQueue(this@SampleOrderDetailsActivity)

        val url = "${getString(R.string.homeUrl)}api/v1/sampleOrder/updateStatus/${orderId}"

        val jsonBody = JSONObject()
        jsonBody.put("status", status)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.PUT,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        binding.textInputEditTextSampleStatus.setText("")

                        Toast.makeText(
                            this, "Status updated successfully", Toast.LENGTH_LONG
                        ).show()

                    }else{
                        Toast.makeText(
                            this, "cannot update status", Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(
                        this, "cannot update status : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    this, "cannot update status (error)", Toast.LENGTH_LONG
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

    private fun deliverSampleOrder(orderId: String){

        val queue = Volley.newRequestQueue(this@SampleOrderDetailsActivity)

        val url = "${getString(R.string.homeUrl)}api/v1/sampleOrder/deliver/${orderId}"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.PUT,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        binding.textInputEditTextSampleStatus.setText("")

                        Toast.makeText(
                            this, "Sample Order delivered successfully", Toast.LENGTH_LONG
                        ).show()

                    }else{
                        Toast.makeText(
                            this, "cannot deliver", Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(
                        this, "cannot deliver : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    this, "cannot deliver (error)", Toast.LENGTH_LONG
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