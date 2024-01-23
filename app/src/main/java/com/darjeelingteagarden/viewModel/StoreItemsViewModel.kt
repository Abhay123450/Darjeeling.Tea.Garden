package com.darjeelingteagarden.viewModel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.LoginActivity
import com.darjeelingteagarden.model.Product
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject

class StoreItemsViewModel(application: Application) : AndroidViewModel(application) {

    val dummyData: MutableList<Product> = mutableListOf( Product("abcd", "name", 499, 449, 49, 10, "grade", "2", 1,"image", true))

    var itemList: MutableList<Product> = mutableListOf()
    val storeItemList: MutableLiveData<MutableList<Product>> by lazy {
        MutableLiveData<MutableList<Product>>().also {
            dummyData
        }
    }


    private fun getStoreItems(url: String){



        val queue = Volley.newRequestQueue(getApplication())

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
                                getApplication(),"No product found", Toast.LENGTH_LONG
                            ).show()
                        }

                        for (i in 0 until data.length()){
                            val productInfo = data.getJSONObject(i)
                            val productObject = Product(
                                productInfo.getString("_id"),
                                productInfo.getString("name"),
                                productInfo.getInt("originalPrice"),
                                5678,
                                productInfo.optInt("samplePrice"),
                                10,
//                                productInfo.getInt("discountedPrice"),
                                productInfo.getString("grade"),
                                productInfo.getString("lotNumber"),
                                productInfo.getInt("bagSize"),
                                productInfo.getJSONArray("images").getString(0),
                                productInfo.getBoolean("discount")
                            )
                            itemList.add(productObject)
                        }

                        Log.i("store item list", storeItemList.toString())

//                        if (isProductListReceived && isOnCreateViewCompleted){
//                            populateRecyclerView()
//                        }
                    }
                    else{
                        Toast.makeText(
                            getApplication(),"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

                }catch (e: Exception){
                    Toast.makeText(
                        getApplication(),"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                }

            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                val isLoggedIn = response.getBoolean("isLoggedIn")

                if (!isLoggedIn){
//                    val intent = Intent(getApplication(), LoginActivity::class.java)
//                    startActivity(intent)
                    Toast.makeText(
                        getApplication(),"Please login to continue", Toast.LENGTH_LONG
                    ).show()
//                    requireActivity().finish()
                }
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(getApplication())
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = "token"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }


}