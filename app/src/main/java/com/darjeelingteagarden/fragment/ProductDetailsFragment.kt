package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.FragmentProductDetailsBinding
import com.darjeelingteagarden.model.MyOrder
import com.darjeelingteagarden.repository.AppDataSingleton
import org.json.JSONObject

class ProductDetailsFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailsBinding

    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
//        return inflater.inflate(R.layout.fragment_product_details, container, false)

        getProductDetails(AppDataSingleton.getCurrentProductId)

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(mContext)
        }

        return binding.root
    }

    private fun getProductDetails(productId: String){

        val queue = Volley.newRequestQueue(mContext)
        val url = getString(R.string.homeUrl) + "api/v1/product/$productId"

        Log.i("my orders url::: ", url)


        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")
                    Log.i("success is ::", success.toString())

                    if (success){

                        val data = it.getJSONObject("data")

                        Log.i("data string is ::", data.toString())

                        binding.txtProductName.text = data.getString("name")
                        binding.txtProductDescription.text = data.getString("description")
                        binding.txtItemOriginalPrice.text = data.getInt("originalPrice").toString()
                        binding.txtDiscountPrice.text = data.getInt("discountedPrice").toString()
                        binding.txtGrade.text = data.getString("grade")

                        binding.rlProgressProductDetails.visibility = View.GONE

                    }
                    else {
                        Toast.makeText(mContext, "An error occurred", Toast.LENGTH_LONG).show()
                    }

                }catch (e: Exception){
                    Log.i("exception is ", e.toString())
                    Toast.makeText(mContext, "An error occurred: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                val response = JSONObject(String(it.networkResponse.data))
                Log.i("error listener", response.toString())
//                Toast.makeText(mContext, "An error occurred: $response", Toast.LENGTH_LONG).show()
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

}