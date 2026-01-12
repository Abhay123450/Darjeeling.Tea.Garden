package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Video

class VideosRecyclerAdapter(
    private val context: Context,
    private val videoList: MutableList<Video>
    ): RecyclerView.Adapter<VideosRecyclerAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtVideoTitle: TextView = view.findViewById(R.id.txtVideoTitle)
        val txtVideoDate: TextView = view.findViewById(R.id.txtVideoDate)
        val txtVideoLanguage: TextView = view.findViewById(R.id.txtVideoLanguage)
        val webViewVideo: WebView = view.findViewById(R.id.webViewVideo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_videos_single_row, parent, false)
        return VideoViewHolder(view)
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]

        val webView = holder.webViewVideo
        webView.visibility = View.VISIBLE
        webView.loadData(video.videoUrl, "text/html", "utf-8")
        webView.settings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()

        holder.txtVideoTitle.text = video.title
        holder.txtVideoDate.text = video.date
        holder.txtVideoLanguage.text = video.language
    }
}