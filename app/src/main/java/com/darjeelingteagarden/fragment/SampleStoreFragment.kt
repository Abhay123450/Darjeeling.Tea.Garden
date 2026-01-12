package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.SampleStoreRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentSampleStoreBinding
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.util.ResizeTransformation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer
import okhttp3.internal.notifyAll
import org.json.JSONObject

class SampleStoreFragment : Fragment() {

    private lateinit var mContext: Context

//    private lateinit var binding: FragmentSampleStoreBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: SampleStoreRecyclerAdapter

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var sampleImagesList = mutableListOf<String>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_sample_store, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshSampleStore)
        recyclerView = view.findViewById(R.id.recyclerViewSampleStore)

        layoutManager = LinearLayoutManager(activity)

        if (SampleDataSingleton.getStoreItemList.size == 0){
            getSampleStore()
        }else {
            populateRecyclerView(SampleDataSingleton.getStoreItemList)
        }

        swipeRefreshLayout.setOnRefreshListener {
            SampleDataSingleton.clearStoreItemList()
            recyclerAdapter.notifyDataSetChanged()
            getSampleStore()
        }

        return view
    }

    private fun populateRecyclerView(sampleList: MutableList<Sample>){
        recyclerAdapter = SampleStoreRecyclerAdapter(activity as Context, sampleList){
            getSampleImages(it.sampleId)
        }
        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = layoutManager
    }

    private fun getSampleStore(){

        swipeRefreshLayout.isRefreshing = true

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/sample"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        SampleDataSingleton.clearStoreItemList()

                        val data = it.getJSONArray("data")
                        Log.i("store data array", data.toString())

                        if (data.length() == 0){
                            Toast.makeText(
                                activity as Context,"No product found", Toast.LENGTH_LONG
                            ).show()
                        }

                        for (i in 0 until data.length()){
                            val sampleInfo = data.getJSONObject(i)
                            val sampleObject = Sample(
                                sampleInfo.getString("_id"),
                                sampleInfo.getString("name"),
                                sampleInfo.getInt("price"),
                                sampleInfo.getString("lotNumber"),
                                sampleInfo.getInt("bagSize"),
                                sampleInfo.getString("grade"),
                                sampleInfo.getString("mainImage")
                            )
                            SampleDataSingleton.addStoreItem(sampleObject)
                        }

                        populateRecyclerView(SampleDataSingleton.getStoreItemList)
                        swipeRefreshLayout.isRefreshing = false

                    }
                    else{
                        Toast.makeText(
                            activity as Context,"An error occurred.", Toast.LENGTH_LONG
                        ).show()
                    }

                    swipeRefreshLayout.isRefreshing = false


                }catch (e: Exception){
                    Toast.makeText(
                        mContext,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                    swipeRefreshLayout.isRefreshing = false
                }

            },
            Response.ErrorListener {
                Toast.makeText(
                    activity as Context,"An error occurred", Toast.LENGTH_LONG
                ).show()
//                val response = JSONObject(String(it.networkResponse.data))
//                val isLoggedIn = response.getBoolean("isLoggedIn")

//                if (!isLoggedIn){
//                    val intent = Intent(activity as Context, LoginActivity::class.java)
//                    startActivity(intent)
//                    Toast.makeText(
//                        activity as Context,"Please login to continue", Toast.LENGTH_LONG
//                    ).show()
//                    requireActivity().finish()
//                }
//                Log.i("Volley error response", response.toString())
//                MaterialAlertDialogBuilder(activity as Context)
//                    .setTitle("Message")
//                    .setMessage(response.getString("message").toString())
//                    .setNeutralButton("OK") { _, _ -> }
//                    .show()

                swipeRefreshLayout.isRefreshing = false

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

    private fun getSampleImages(id: String){

//        if (!ConnectionManager().isOnline(mContext)){
//            return
//        }

        swipeRefreshLayout.isRefreshing = true

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/sample/images/$id"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        sampleImagesList = mutableListOf()

                        val images = it.getJSONObject("data").getJSONArray("images")

                        for (i in 0 until images.length()){
                            sampleImagesList.add(images.getString(i))
                        }

                        StfalconImageViewer.Builder(mContext, sampleImagesList){view, image ->

                            Picasso.get().load(image).transform(ResizeTransformation(1920)).into(view)

                        }.allowSwipeToDismiss(false).withStartPosition(0).show()

                    }

                    swipeRefreshLayout.isRefreshing = false

                }catch (e: Exception){
                    Toast.makeText(
                        mContext,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()
                    swipeRefreshLayout.isRefreshing = false
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    mContext,"An error occurred", Toast.LENGTH_LONG
                ).show()
                swipeRefreshLayout.isRefreshing = false
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