package com.darjeelingteagarden.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.SampleOrderDetailsActivity
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.model.OrderForMe
import com.darjeelingteagarden.model.SampleOrder
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.google.android.material.card.MaterialCardView

class SampleOrdersForMeRecyclerAdapter(
    private val context: Context,
    private val sampleOrderForMeList: List<OrderForMe>,
    private val sampleDetails: (sampleId: String) -> Unit
): RecyclerView.Adapter<SampleOrdersForMeRecyclerAdapter.SampleOrderForMeViewHolder>() {

    class SampleOrderForMeViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardOrderForMe: MaterialCardView = view.findViewById(R.id.cardOrderForMe)
        val txtOrderDate: TextView = view.findViewById(R.id.txtOrderDate)
        val txtFromName: TextView = view.findViewById(R.id.txtFromName)
        val txtFromRole: TextView = view.findViewById(R.id.txtFromRole)
        val txtFromAddress: TextView = view.findViewById(R.id.txtFromAddress)
        val txtTotalItems: TextView = view.findViewById(R.id.txtTotalItems)
        val txtTotalAmount: TextView = view.findViewById(R.id.txtTotalAmount)
        val txtOrderStatusDelivered: TextView = view.findViewById(R.id.txtOrderStatusDelivered)
        val txtOrderStatusActive: TextView = view.findViewById(R.id.txtOrderStatusActive)
        val txtOrderStatusCancelled: TextView = view.findViewById(R.id.txtOrderStatusCancelled)
        val txtOrderStatusPartiallyDelivered: TextView = view.findViewById(R.id.txtOrderStatusPartiallyDelivered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleOrderForMeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_sample_orders_for_me_single_row, parent, false)
        return SampleOrderForMeViewHolder(view)
    }

    override fun getItemCount(): Int {
        return sampleOrderForMeList.size
    }

    override fun onBindViewHolder(holder: SampleOrderForMeViewHolder, position: Int) {
        val orderForMe: OrderForMe = sampleOrderForMeList[position]

        holder.txtOrderDate.text = orderForMe.orderDate
        holder.txtFromName.text = orderForMe.fromName
        holder.txtFromRole.text = orderForMe.fromRole
        holder.txtFromAddress.text = orderForMe.fromAddress
        holder.txtTotalItems.text = orderForMe.totalItems.toString()
        holder.txtTotalAmount.text = String.format("%.2f", orderForMe.totalPrice)

        holder.txtOrderStatusActive.visibility = View.GONE
        holder.txtOrderStatusCancelled.visibility = View.GONE
        holder.txtOrderStatusPartiallyDelivered.visibility = View.GONE
        holder.txtOrderStatusDelivered.visibility = View.GONE

        when(orderForMe.orderStatus){
            "Active" -> {
                holder.txtOrderStatusActive.visibility = View.VISIBLE
                holder.cardOrderForMe.strokeColor = ContextCompat.getColor(context, R.color.blue)
            }
            "Cancelled" -> {
                holder.txtOrderStatusCancelled.visibility = View.VISIBLE
                holder.cardOrderForMe.strokeColor = ContextCompat.getColor(context, R.color.red)
            }
            "Partially Delivered" -> {
                holder.txtOrderStatusPartiallyDelivered.visibility = View.VISIBLE
                holder.cardOrderForMe.strokeColor = ContextCompat.getColor(context, R.color.yellow)
            }
            "Delivered" -> {
                holder.txtOrderStatusDelivered.visibility = View.VISIBLE
                holder.cardOrderForMe.strokeColor = ContextCompat.getColor(context, R.color.colorPrimary)
            }
//            "Created" ->
        }

        holder.cardOrderForMe.setOnClickListener {
//            SampleDataSingleton.setSampleOrderForMeId(orderForMe.orderId)
//            navController.navigate(R.id.action_sampleOrdersForMeFragment_to_sampleOrdersForMeDetailsFragment)
//            val intent = Intent(context, SampleOrderDetailsActivity::class.java)
//            intent.putExtra("sampleId", orderForMe.orderId)
            sampleDetails(orderForMe.orderId)
        }
    }
}