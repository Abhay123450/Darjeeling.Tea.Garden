package com.darjeelingteagarden.repository

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Product
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import org.json.JSONObject

object StoreDataSingleton {

    var loadingStoreItem = false

    fun getStoreItems(context: Context){

        loadingStoreItem = true

        val queue = Volley.newRequestQueue(context)

        val url = context.getString(R.string.homeUrl) + "api/v1/products"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")
                        Log.i("store data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                context,"No product found", Toast.LENGTH_LONG
                            ).show()
                        }

                        AppDataSingleton.clearStoreItemList()

                        for (i in 0 until data.length()){
                            val productInfo = data.getJSONObject(i)

                            val discountedPrice = if (productInfo.getBoolean("discount")){
                                productInfo.getInt("discountedPrice")
                            }else{
                                productInfo.getInt("originalPrice")
                            }
                            val productObject = Product(
                                productInfo.getString("_id"),
                                productInfo.getString("name"),
                                productInfo.getInt("originalPrice"),
                                discountedPrice,
                                productInfo.optDouble("samplePrice"),
                                productInfo.optInt("sampleQuantity"),
                                productInfo.getString("grade"),
                                productInfo.getString("lotNumber"),
                                productInfo.getInt("bagSize"),
                                productInfo.getString("mainImage"),
                                productInfo.getBoolean("discount")
                            )
                            AppDataSingleton.addStoreItem(productObject)
                        }
//                        productList = AppDataSingleton.getStoreItemList

                        loadingStoreItem = false
                        AppDataSingleton.loadCart(context)
//                        if (isCartItemLoaded && isStoreItemLoaded){
//                            populateRecyclerView(AppDataSingleton.getStoreItemList)
//                            storeSwipeRefreshLayout.isRefreshing = false
//                        }

                    }
                    else{
                        Toast.makeText(
                            context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                        loadingStoreItem = false
                    }

//                    storeSwipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        context,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                    loadingStoreItem = false
                }

            },
            Response.ErrorListener {

                loadingStoreItem = false
                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")

//                if (!isLoggedIn){
//                    val intent = Intent(activity as Context, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        activity as Context,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
//                    requireActivity().finish()
//                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(context)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

//                storeSwipeRefreshLayout.isRefreshing = false

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