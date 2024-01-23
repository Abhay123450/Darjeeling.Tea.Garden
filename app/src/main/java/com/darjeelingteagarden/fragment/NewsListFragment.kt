package com.darjeelingteagarden.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.LoginActivity
import com.darjeelingteagarden.adapter.NewsRecyclerAdapter
import com.darjeelingteagarden.databinding.FragmentNewsListBinding
import com.darjeelingteagarden.databinding.FragmentSampleHistoryBinding
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate

class NewsListFragment : Fragment() {

    private lateinit var mContext: Context

    private var totalNews = 0
    private var currentPage = 1
    private val limit = 10

    private var newsList = mutableListOf<News>()

    //recycler adapter
    private lateinit var recyclerViewNews: RecyclerView
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private lateinit var recyclerAdapter: NewsRecyclerAdapter

    private lateinit var binding: FragmentNewsListBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsListBinding.inflate(inflater, container, false)

        if (NotificationDataSingleton.notificationToOpen){
            findNavController().navigate(R.id.action_newsListFragment_to_newsDetailsFragment)
        }

        recyclerViewNews = binding.recyclerViewNewsList
        layoutManager = LinearLayoutManager(mContext)

        if (newsList.isEmpty()){
            getNewsList(currentPage, limit)
        }
        else{
            populateRecyclerView(newsList)
        }

        binding.btnLoadMoreNews.setOnClickListener {
            it.visibility = View.GONE
            binding.progressBarNews.visibility = View.VISIBLE
            getNewsList(++currentPage, limit)
        }

        binding.swipeRefreshNews.setOnRefreshListener {

            newsList = mutableListOf()
            binding.recyclerViewNewsList.visibility = View.GONE
            currentPage = 1
            getNewsList(currentPage, limit)

        }

        return binding.root
    }

    private fun populateRecyclerView(list: MutableList<News>){

        recyclerAdapter = NewsRecyclerAdapter(mContext, list){
            AppDataSingleton.setCurrentNewsId(it.newsId)
            findNavController().navigate(R.id.action_newsListFragment_to_newsDetailsFragment)
        }
        recyclerViewNews.adapter = recyclerAdapter
        recyclerViewNews.layoutManager = layoutManager

        var text = ""

        if (totalNews > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalNews news"
            binding.btnLoadMoreNews.visibility = View.VISIBLE
        }
        else{
            text = "Showing 1 - $totalNews of $totalNews news"
            binding.btnLoadMoreNews.visibility = View.GONE
        }

        binding.txtPage.text = text

        binding.swipeRefreshNews.isRefreshing = false
        binding.progressBarNews.visibility = View.GONE
        binding.recyclerViewNewsList.visibility = View.VISIBLE

    }

    private fun populateAdditionalData(){
        recyclerAdapter.notifyItemRangeInserted((currentPage - 1) * limit + 1, limit)

        var text = ""

        if (totalNews > currentPage * limit){
            text = "Showing 1 - ${currentPage * limit} of $totalNews news"
            binding.btnLoadMoreNews.visibility = View.VISIBLE
        }
        else{
            text = "Showing 1 - $totalNews of $totalNews news"
            binding.btnLoadMoreNews.visibility = View.GONE
        }

        binding.txtPage.text = text

        binding.progressBarNews.visibility = View.GONE
    }

    private fun getNewsList(page: Int, limit: Int){

        val queue = Volley.newRequestQueue(mContext)

        val url = getString(R.string.homeUrl) + "api/v1/news?page=$page&limit=$limit"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        totalNews = it.getInt("itemsCount")

                        if (totalNews > 0){

                            val data = it.getJSONArray("data")

                            for (i in 0 until data.length()){

                                val news = data.getJSONObject(i)

                                newsList.add(News(
                                    news.getString("_id"),
                                    news.getString("title"),
                                    news.getString("date").toDate()!!.formatTo("dd MMM yyyy HH:mm"),
                                    news.getString("image")
                                ))

                            }

                            if (currentPage == 1){
                                populateRecyclerView(newsList)
                            }
                            else{
                                populateAdditionalData()
                            }

                            Log.i("newsList ", newsList.toString())

                        }
                        else {
                            val text = "No news to show"
                            binding.txtPage.text = text
                        }

                        binding.swipeRefreshNews.isRefreshing = false
                        binding.progressBarNews.visibility = View.GONE

                    }

                }catch (e: Exception){
                    binding.swipeRefreshNews.isRefreshing = false
                    binding.progressBarNews.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                binding.swipeRefreshNews.isRefreshing = false
                binding.progressBarNews.visibility = View.GONE
                if (it.networkResponse.statusCode == 401 || it.networkResponse.statusCode == 403){
                    val intent = Intent(mContext, LoginActivity::class.java)
                    intent.putExtra("resume", true)
                    startActivity(intent)
                    return@ErrorListener
                }
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