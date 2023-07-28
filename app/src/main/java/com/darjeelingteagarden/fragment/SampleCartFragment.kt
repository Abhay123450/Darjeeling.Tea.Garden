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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.PaymentActivity
import com.darjeelingteagarden.adapter.CartRecyclerAdapter
import com.darjeelingteagarden.adapter.SampleCartRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentSampleCartBinding
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import org.json.JSONArray
import org.json.JSONObject

class SampleCartFragment : Fragment() {

    private lateinit var binding: FragmentSampleCartBinding
    private lateinit var mContext: Context

    private lateinit var recyclerViewCart: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: SampleCartRecyclerAdapter

    private var cartItemList = mutableListOf<Cart>()
    var totalAmount = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSampleCartBinding.inflate(inflater, container, false)

        recyclerViewCart = binding.recyclerViewCart
        layoutManager = LinearLayoutManager(mContext)

        cartItemList = SampleDataSingleton.getCartItemList

        populateRecyclerView()
        calculateTotalAmount()

        if (SampleDataSingleton.getCartItemList.size != 0){
            binding.llEmptyCart.visibility = View.GONE
        }
        else{
            binding.llEmptyCart.visibility = View.VISIBLE
        }

        binding.btnContinueToPayment.setOnClickListener {

            it.visibility = View.GONE
            binding.progressContinue.visibility = View.VISIBLE
            createOrder()

        }

        return binding.root
    }

    private fun calculateTotalAmount(){
        totalAmount = 0
        cartItemList.forEach {
            totalAmount += it.discountedPrice * it.quantity
        }
        (getString(R.string.rupee_symbol) + " " + totalAmount.toString()).also { binding.txtTotalAmount.text = it }
    }

    private fun populateRecyclerView(){

        recyclerAdapter =
            SampleCartRecyclerAdapter(mContext, SampleDataSingleton.getCartItemList) {
                calculateTotalAmount()
            }
        recyclerViewCart.adapter = recyclerAdapter
        recyclerViewCart.layoutManager = layoutManager
        recyclerViewCart.addItemDecoration(
            DividerItemDecoration(
                recyclerViewCart.context,
                (layoutManager as LinearLayoutManager).orientation
            )
        )

    }

    private fun createOrder(){

        Log.i("auth-token", AppDataSingleton.getAuthToken)

        val cartItems = JSONArray()

        val cartItemList = SampleDataSingleton.getCartItemList as ArrayList<Cart> /* = java.util.ArrayList<com.darjeelingteagarden.model.Cart> */

        cartItemList.forEach {

            val jsonObject = JSONObject()
            jsonObject.put("productId", it.productId)
            jsonObject.put("quantity", it.quantity)

            cartItems.put(jsonObject)
        }

        val url = getString(R.string.homeUrl) + "api/v1/sampleOrder"

        val jsonBody = JSONObject()
        jsonBody.put("cart", cartItems)

        Log.i("cart jsonBody", jsonBody.toString())

        val queue = Volley.newRequestQueue(activity as Context)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val orderDetails = it.getJSONObject("data")
                        Log.i("orderDetails", orderDetails.toString())
                        val orderId = orderDetails.getString("_id")
                        val itemTotal = orderDetails.getDouble("itemsPrice")
                        val totalTax = orderDetails.getDouble("totalTax")
                        val totalAmount = orderDetails.getDouble("totalPrice")
                        Log.i("total Amount", totalAmount.toString())

                        val intent = Intent(activity as Context, PaymentActivity::class.java)
                        intent.putExtra("sampleOrder", true)
                        intent.putExtra("orderId", orderId)
                        intent.putExtra("itemTotal", itemTotal)
                        intent.putExtra("totalTax", totalTax)
                        intent.putExtra("totalAmount", totalAmount)
                        startActivity(intent)

                        binding.progressContinue.visibility = View.GONE
                        binding.btnContinueToPayment.visibility = View.VISIBLE

                    }
                    else{
                        Toast.makeText(
                            activity as Context, "An Error Occurred", Toast.LENGTH_LONG
                        ).show()
                        binding.progressContinue.visibility = View.GONE
                        binding.btnContinueToPayment.visibility = View.VISIBLE
                    }
                }
                catch (e: Exception){
                    Toast.makeText(
                        activity as Context, "Exception: $e", Toast.LENGTH_LONG
                    ).show()
                    binding.progressContinue.visibility = View.GONE
                    binding.btnContinueToPayment.visibility = View.VISIBLE
                }
            },
            Response.ErrorListener {
                binding.progressContinue.visibility = View.GONE
                binding.btnContinueToPayment.visibility = View.VISIBLE

                val response = JSONObject(String(it.networkResponse.data))
                Log.i("Create order error :: ", response.toString())

                Toast.makeText(
                    activity as Context, response.getString("message"), Toast.LENGTH_LONG
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