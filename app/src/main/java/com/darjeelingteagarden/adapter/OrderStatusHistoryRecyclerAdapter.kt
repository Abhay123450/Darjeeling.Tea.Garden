package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.OrderStatusHistory
import com.darjeelingteagarden.model.StatusHistory

class OrderStatusHistoryRecyclerAdapter(
    private val context: Context,
    private val statusHistory: List<OrderStatusHistory>
): RecyclerView.Adapter<OrderStatusHistoryRecyclerAdapter.OrderStatusHistoryViewHolder>() {

    class OrderStatusHistoryViewHolder(view: View): RecyclerView.ViewHolder(view){

        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val txtStatusDate: TextView = view.findViewById(R.id.txtStatusDate)
        val imgCompleted: ImageView = view.findViewById(R.id.imgCompleted)
        val imgOngoing: ImageView = view.findViewById(R.id.imgOngoing)
        val lineUp: View = view.findViewById(R.id.lineUp)
        val lineDown: View = view.findViewById(R.id.lineDown)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderStatusHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_order_status_timeline, parent, false)
        return OrderStatusHistoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return statusHistory.size
    }

    override fun onBindViewHolder(holder: OrderStatusHistoryViewHolder, position: Int) {
        val status: OrderStatusHistory = statusHistory[position]

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

        if (status.status == "Delivered"){
            holder.imgOngoing.visibility = View.INVISIBLE
            holder.imgCompleted.visibility = View.VISIBLE
        }
    }

}