package com.darjeelingteagarden.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.adapter.NewsRecyclerAdapter
import com.darjeelingteagarden.adapter.VideosRecyclerAdapter
import com.darjeelingteagarden.databinding.ActivityVideosBinding
import com.darjeelingteagarden.model.Video
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import java.lang.Exception

class VideosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideosBinding

    private lateinit var queue: RequestQueue

    private var videosList = mutableListOf<Video>()
    private var language = "All"
    private var page = 1
    private var limit = 10

    //recycler adapter
    private lateinit var recyclerViewVideos: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: VideosRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideosBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        queue = Volley.newRequestQueue(this)

        recyclerViewVideos = binding.videosRecyclerView
        layoutManager = LinearLayoutManager(this)

        binding.videosToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.autoCompleteTextViewVideoLanguage.setOnItemClickListener { parent, view, position, id ->
            language = parent.getItemAtPosition(position).toString()
            page = 1
            getVideosList(page, limit, language, null)
        }

        binding.btnLoadMoreVideos.setOnClickListener {
            getVideosList(++page, limit, language, it)
        }

        getVideosList(page, limit, language, null)

//        binding.videosWebView.settings.javaScriptEnabled = true
//
//        binding.videosWebView.webViewClient = object: WebViewClient(){
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean {
//                Log.d("url host", "host is ${Uri.parse(request?.url.toString()).host}")
//                if (Uri.parse(request?.url.toString()).host == "darjeelingteagarden.com"){
//                    return false
//                }
//                Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString())).apply {
//                    startActivity(this)
//                }
//                return true
//            }
//        }
//
//        binding.videosWebView.loadUrl("https://www.darjeelingteagarden.com/blog")
//
//        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true){
//            override fun handleOnBackPressed() {
//                if (binding.videosWebView.canGoBack()){
//                    binding.videosWebView.goBack()
//                }
//                else{
//                    finish()
//                }
//            }
//        })

    }

    override fun onResume() {
        super.onResume()
        initializeLanguageDropdown()
    }

    private fun initializeLanguageDropdown(){

        val languageList = arrayListOf("All", "English", "Hindi")

        val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, languageList)
        binding.autoCompleteTextViewVideoLanguage.setAdapter(arrayAdapter)

    }

    private fun populateRecyclerView() {
        recyclerAdapter = VideosRecyclerAdapter(this, videosList)
        recyclerViewVideos.adapter = recyclerAdapter
        recyclerViewVideos.layoutManager = layoutManager
    }

    private fun populateAdditionalData(){
        recyclerAdapter.notifyItemRangeInserted((page - 1) * limit + 1, limit)
    }

    private fun getVideosList(page: Int, limit: Int, language: String, view: View?){

        if(view != null){
            view.visibility = View.GONE
        }
        binding.rlProgressVideos.visibility = View.VISIBLE

        val url = "${getString(R.string.homeUrl)}api/v1/videos?page=$page&limit=$limit&language=$language"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONArray("data")

                        if (page == 1){
                            videosList.clear()
                        }

                        for (i in 0 until data.length()){

                            val video = data.getJSONObject(i)

                            videosList.add(
                                Video(
                                    video.getString("_id"),
                                    video.getString("title"),
                                    video.getString("date").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    video.optString("language"),
                                    video.getString("ytVideoLink"),
                                )
                            )

                        }

                        if (page == 1){
                            populateRecyclerView()
                        }
                        else{
                            populateAdditionalData()
                        }

                        if (data.length() < limit){
                            binding.btnLoadMoreVideos.visibility = View.GONE
                        }
                        else{
                            binding.btnLoadMoreVideos.visibility = View.VISIBLE
                        }

                    }

                    binding.rlProgressVideos.visibility = View.GONE

                }catch (e: Exception){
                    if (view != null) {
                        view.visibility = View.VISIBLE
                    }
                    binding.rlProgressVideos.visibility = View.GONE
                    Toast.makeText(
                        this,"An error occurred : $e", Toast.LENGTH_LONG
                    ).show()
                }
            },
            Response.ErrorListener {
                if (view != null) {
                    view.visibility = View.VISIBLE
                }
                binding.rlProgressVideos.visibility = View.GONE
                Toast.makeText(
                    this,"No videos", Toast.LENGTH_LONG
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