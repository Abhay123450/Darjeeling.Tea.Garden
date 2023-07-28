package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R

class ProfileRecyclerAdapter(val context: Context, val actionList: ArrayList<String>): RecyclerView.Adapter<ProfileRecyclerAdapter.ProfileViewHolder>()  {

    class ProfileViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtActionName: TextView = view.findViewById(R.id.txtActionName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_profile_single_row, parent, false)

        return ProfileRecyclerAdapter.ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val userAction = actionList[position]
        holder.txtActionName.text = userAction
    }

    override fun getItemCount(): Int {
        return actionList.size
    }
}