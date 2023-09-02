package com.darjeelingteagarden.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.core.text.HtmlCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.FragmentNewsDetailsBinding
import com.darjeelingteagarden.databinding.FragmentNewsListBinding
import com.darjeelingteagarden.model.NewsDetails
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import com.squareup.picasso.Picasso

class NewsDetailsFragment : Fragment() {

    private lateinit var mContext: Context

    private lateinit var binding: FragmentNewsDetailsBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsDetailsBinding.inflate(inflater, container, false)

        if (ConnectionManager().isOnline(mContext)){

            if (NotificationDataSingleton.notificationToOpen){
                loadNews(NotificationDataSingleton.resourceId.toString())
                NotificationDataSingleton.notificationToOpen = false
            }
            else{
                loadNews(AppDataSingleton.getCurrentNewsId)
            }


        }

        return binding.root

    }

    private fun loadNews(id: String){

        val queue = Volley.newRequestQueue(mContext)

        val newsUrl = "${getString(R.string.homeUrl)}/api/v1/news/${id}"

        val newRequest = object : JsonObjectRequest(
            Method.GET,
            newsUrl,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val newsObject = it.getJSONObject("data")

                        val newsDetails = NewsDetails(
                            newsObject.getString("_id"),
                            newsObject.getString("title"),
                            newsObject.getString("date"),
                            newsObject.getString("description"),
                            newsObject.getString("image"),
                        )

                        binding.txtNewsTitle.text = newsDetails.newsTitle
                        binding.txtNewsDate.text = newsDetails.newsDate.toDate()!!.formatTo("dd MMM YYYY HH:MM")

                        Picasso.get().load(newsDetails.newsImage).into(
                            binding.imgNewsImage
                        )

                        binding.txtNewsContent.text = HtmlCompat.fromHtml(
                            newsDetails.newsContent, HtmlCompat.FROM_HTML_MODE_LEGACY
                        )

                    }

                }
                catch (e: Exception){

                    Toast.makeText(
                        mContext,"An error occurred: $e", Toast.LENGTH_LONG
                    ).show()

                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    mContext,"An error occurred", Toast.LENGTH_LONG
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

        queue.add(newRequest)

    }

}