package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class NewsRecyclerAdapter(
    val context: Context,
    private val newsList: MutableList<News>,
    private val openNews: (news: News) -> Unit
): RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardParent: MaterialCardView = view.findViewById(R.id.cardNewsListSingle)
        val imgNewsImage: ImageView = view.findViewById(R.id.imgNewsImage)
        val txtNewsTitle: TextView = view.findViewById(R.id.txtNewsTitle)
        val txtNewsDate: TextView = view.findViewById(R.id.txtNewsDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_news_list_single_row, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news: News = newsList[position]

        Picasso.get().load(news.newsImage).fit().centerCrop().into(holder.imgNewsImage)

        holder.txtNewsTitle.text = news.newsTitle
        holder.txtNewsDate.text = news.newsDate

        holder.cardParent.setOnClickListener {
            openNews(news)
        }

    }

    override fun getItemCount(): Int {
        return newsList.size
    }

}