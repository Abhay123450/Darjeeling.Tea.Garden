package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.StatusHistory
import com.darjeelingteagarden.util.formatTo
import com.darjeelingteagarden.util.toDate

class SampleOrderStatusHistoryRecyclerAdapter(
    private val context: Context,
    private var statusHistory: List<StatusHistory>
): RecyclerView.Adapter<SampleOrderStatusHistoryRecyclerAdapter.StatusHistoryViewHolder>(){

    class StatusHistoryViewHolder(view: View): RecyclerView.ViewHolder(view){

        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtStatusDate: TextView = view.findViewById(R.id.txtStatusDate)
        val imgCompleted: ImageView = view.findViewById(R.id.imgCompleted)
        val imgOngoing: ImageView = view.findViewById(R.id.imgOngoing)
        val lineUp: View = view.findViewById(R.id.lineUp)
        val lineDown: View = view.findViewById(R.id.lineDown)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_status_timeline_single_row, parent, false)
        return StatusHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return statusHistory.size
    }

    override fun onBindViewHolder(holder: StatusHistoryViewHolder, position: Int) {
        val status: StatusHistory = statusHistory[position]

        if (statusHistory.size == 1){
            holder.lineDown.visibility = View.INVISIBLE
            holder.lineUp.visibility = View.INVISIBLE
            holder.imgOngoing.visibility = View.VISIBLE
            holder.imgCompleted.visibility = View.INVISIBLE
        }
        else if (position == 0){
            holder.lineUp.visibility = View.INVISIBLE
            holder.imgOngoing.visibility = View.INVISIBLE
            holder.imgCompleted.visibility = View.VISIBLE
        }
        else if (position == statusHistory.size - 1){
            holder.lineDown.visibility = View.INVISIBLE
            holder.imgOngoing.visibility = View.VISIBLE
            holder.imgCompleted.visibility = View.INVISIBLE
        }
        else{
            holder.imgOngoing.visibility = View.INVISIBLE
            holder.imgCompleted.visibility = View.VISIBLE
        }

        holder.txtStatus.text = status.status
        holder.txtStatusDate.text = status.date

        val statusList = arrayListOf("Order delivered", "Order Delivered", "Delivered", "Cancelled", "Order Cancelled", "Order cancelled")

        if (statusList.contains(status.status)){
            holder.imgOngoing.visibility = View.INVISIBLE
            holder.imgCompleted.visibility = View.VISIBLE
        }

    }
}