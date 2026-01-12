package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.ItemDetails
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.model.SampleOrder
import com.google.android.material.card.MaterialCardView

class SampleHistoryRecyclerAdapter(
    val context: Context,
    private val sampleHistoryList: MutableList<SampleOrder>,
    val itemClick: (sampleOrder: SampleOrder) -> Unit
): RecyclerView.Adapter<SampleHistoryRecyclerAdapter.SampleHistoryViewHolder>() {

    class SampleHistoryViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardParent: MaterialCardView = view.findViewById(R.id.cardParent)
        val txtOrderedOn: TextView = view.findViewById(R.id.txtOrderedOn)
        val txtTotalItems: TextView = view.findViewById(R.id.txtTotalItems)
        val txtTotalPrice: TextView = view.findViewById(R.id.txtTotalPrice)
        val txtOrderStatusDelivered: TextView = view.findViewById(R.id.txtOrderStatusDelivered)
        val txtOrderStatusActive: TextView = view.findViewById(R.id.txtOrderStatusActive)
        val txtOrderStatusCancelled: TextView = view.findViewById(R.id.txtOrderStatusCancelled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_sample_history_single_row, parent, false)
        return SampleHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SampleHistoryViewHolder, position: Int) {
        val sampleOrder: SampleOrder = sampleHistoryList[position]

        holder.txtTotalItems.text = sampleOrder.totalItems.toString()
        holder.txtTotalPrice.text = sampleOrder.totalPrice.toString()
        holder.txtOrderedOn.text = sampleOrder.orderDate

        holder.txtOrderStatusActive.visibility = View.GONE
        holder.txtOrderStatusCancelled.visibility = View.GONE
        holder.txtOrderStatusDelivered.visibility = View.GONE

        when(sampleOrder.currentStatus){
            "Active" -> holder.txtOrderStatusActive.visibility = View.VISIBLE
            "Cancelled" -> holder.txtOrderStatusCancelled.visibility = View.VISIBLE
            "Delivered" -> holder.txtOrderStatusDelivered.visibility = View.VISIBLE
        }

        holder.cardParent.setOnClickListener {
            itemClick(sampleOrder)
        }
    }

    override fun getItemCount(): Int {
        return sampleHistoryList.size
    }

}