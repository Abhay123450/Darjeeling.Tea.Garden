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

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false // Optional: Allows seamless playback
        webView.webChromeClient = WebChromeClient()

        // Wrap the iframe in standard HTML with a viewport meta tag for mobile scaling
        val htmlData = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body { margin: 0; padding: 0; background-color: #000000; }
                iframe { width: 100vw; height: 100vh; border: none; }
            </style>
        </head>
        <body>
            ${video.videoUrl}
        </body>
        </html>
    """.trimIndent()

        // Use loadDataWithBaseURL instead of loadData
        // Passing a YouTube base URL helps prevent strict-origin policy errors
        webView.loadDataWithBaseURL(context.getString(R.string.homeUrl), htmlData, "text/html", "utf-8", null)

        webView.visibility = View.VISIBLE

        holder.txtVideoTitle.text = video.title
        holder.txtVideoDate.text = video.date
        holder.txtVideoLanguage.text = video.language
    }
}