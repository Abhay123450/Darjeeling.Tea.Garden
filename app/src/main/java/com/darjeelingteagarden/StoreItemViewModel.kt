package com.darjeelingteagarden

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.activity.LoginActivity
import com.darjeelingteagarden.model.Cart
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class StoreItemViewModel: ViewModel() {

    val cartList = arrayListOf<Cart>()

    private fun getCartItems(context: Context){

        val queue = Volley.newRequestQueue(context)

        val url = context.getString(R.string.homeUrl) + "api/v1/user/cart"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")
                        Log.i("cart data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                context,"Cart is empty", Toast.LENGTH_LONG
                            ).show()
                        }

//                        for (i in 0 until data.length()){
//                            val cartInfo = data.getJSONObject(i)
//                            val cartObject = Cart(
//                                cartInfo.getJSONObject("productId").getString("_id"),
//                                cartInfo.getString("name"),
//                                cartInfo.getJSONObject("productId").getInt("discountedPrice"),
//                                cartInfo.getInt("quantity")
//                            )
//
//                            cartList.add(cartObject)
//
//                        }


                    }
                    else{
                        Toast.makeText(
                            context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

//                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        context,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
//                    storeSwipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                val isLoggedIn = response.getBoolean("isLoggedIn")

                if (!isLoggedIn){
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    Toast.makeText(
                        context,"Please login to continue", Toast.LENGTH_LONG
                    ).show()

                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(context)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()

//                storeSwipeRefreshLayout.isRefreshing = false

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = ""
//                headers["auth-token"] = token
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

}