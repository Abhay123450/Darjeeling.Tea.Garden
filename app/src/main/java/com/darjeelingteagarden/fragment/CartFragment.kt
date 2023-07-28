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
import com.darjeelingteagarden.databinding.FragmentCartBinding
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.CartItem
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONArray
import org.json.JSONObject

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding

    private var cartItemList = mutableListOf<Cart>()

    private lateinit var recyclerViewCart: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: CartRecyclerAdapter

    var authToken = ""

    var totalAmount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
//        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        binding = FragmentCartBinding.inflate(inflater, container, false)

        authToken = AppDataSingleton.getAuthToken


        recyclerViewCart = binding.recyclerViewCart
        layoutManager = LinearLayoutManager(activity)

//        cartItemList = mAppDataViewModel.cartItemList

        cartItemList = AppDataSingleton.getCartItemList
        Log.i("cart item list ::", cartItemList.toString())

        populateRecyclerView()
        calculateTotalAmount()

        binding.btnContinueToPayment.setOnClickListener {

            if (ConnectionManager().isOnline(activity as Context)){
                createOrder()
            }

        }



//        recyclerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){
//
//            override fun onChanged() {
//                super.onChanged()
//                val cartItemList = AppDataSingleton.getCartItemList
//                cartItemList.forEach {
//                    totalAmount += it.discountedPrice * it.quantity
//                }
//                Log.i("total Amount", totalAmount.toString())
//                (getString(R.string.rupee_symbol) + " " + totalAmount.toString()).also { binding.txtTotalAmount.text = it }
//            }
//
//        })
        if (AppDataSingleton.getCartItemList.size != 0){
            binding.llEmptyCart.visibility = View.GONE
        }
        else{
            binding.llEmptyCart.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (AppDataSingleton.getCartItemList.size != 0){
            binding.llEmptyCart.visibility = View.GONE
        }
        else{
            binding.llEmptyCart.visibility = View.VISIBLE
        }
    }

    private fun calculateTotalAmount(){
        totalAmount = 0
        cartItemList.forEach {
            totalAmount += it.discountedPrice * it.quantity
        }
        (getString(R.string.rupee_symbol) + " " + totalAmount.toString()).also { binding.txtTotalAmount.text = it }
    }

    private fun populateRecyclerView(){

        recyclerAdapter = CartRecyclerAdapter(activity as Context, AppDataSingleton.getCartItemList){
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

//        recyclerAdapter = StoreRecyclerAdapter(activity as Context, productList, cartList)
//        recyclerViewStore.adapter = recyclerAdapter
//        recyclerViewStore.layoutManager = layoutManager
//        recyclerViewStore.addItemDecoration(
//            DividerItemDecoration(
//                recyclerViewStore.context,
//                (layoutManager as LinearLayoutManager).orientation
//            )
//        )
//        storeSwipeRefreshLayout.isRefreshing = false

    }

    private fun createOrder(){

        Log.i("auth-token", AppDataSingleton.getAuthToken)

        val cartItems = JSONArray()

        val cartItemList = AppDataSingleton.getCartItemList as ArrayList<Cart> /* = java.util.ArrayList<com.darjeelingteagarden.model.Cart> */

        cartItemList.forEach {

            val jsonObject = JSONObject()
            jsonObject.put("productId", it.productId)
            jsonObject.put("quantity", it.quantity)

            cartItems.put(jsonObject)
        }

        val url = getString(R.string.homeUrl) + "api/v1/orders"

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
//                        val cgst = orderDetails.getDouble("cgst")
//                        val sgst = orderDetails.getDouble("sgst")
//                        val igst = orderDetails.getDouble("igst")
                        val totalAmount = orderDetails.getDouble("totalPrice")


                        val intent = Intent(activity as Context, PaymentActivity::class.java)
                        intent.putExtra("orderId", orderId)
                        intent.putExtra("itemTotal", itemTotal)
                        intent.putExtra("totalTax", totalTax)
                        intent.putExtra("totalAmount", totalAmount)
                        startActivity(intent)

                    }
                    else{
                        Toast.makeText(
                            activity as Context, "An Error Occurred", Toast.LENGTH_LONG
                        ).show()
                    }
                }
                catch (e: Exception){
                    Toast.makeText(
                        activity as Context, "Exception: $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {


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
                headers["auth-token"] = authToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    fun getCartItems(){

        val url = getString(R.string.homeUrl) + "api/v1/user/cart"

        val queue = Volley.newRequestQueue(activity as Context)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

            },
            Response.ErrorListener {

            }
        ){

        }

    }

}