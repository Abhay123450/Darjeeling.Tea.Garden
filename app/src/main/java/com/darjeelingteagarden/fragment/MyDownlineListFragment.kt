package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.FragmentMyDownlineListBinding
import com.darjeelingteagarden.repository.AppDataSingleton

class MyDownlineListFragment : Fragment() {

    private lateinit var binding: FragmentMyDownlineListBinding
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

        val binding = FragmentMyDownlineListBinding.inflate(inflater, container, false)



        return binding.root
    }

    private fun initializeFilterDropdown(){

        val userRoles = mutableListOf("All", "Super Stockist", "Distributor", "Dealer", "Wholesaler", "Retailer")

        val arrayAdapter = ArrayAdapter(mContext, R.layout.dropdown_item, userRoles)
        binding.autoCompleteTextUserRole.setAdapter(arrayAdapter)
    }

    private fun getMyDownline(){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/user/myDownline"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                    }

                }
                catch (e: Exception){

                }

            },
            Response.ErrorListener {

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