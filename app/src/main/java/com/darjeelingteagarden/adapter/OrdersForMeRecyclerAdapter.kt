package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.OrderForMe
import com.darjeelingteagarden.repository.AppDataSingleton
import com.google.android.material.card.MaterialCardView

class OrdersForMeRecyclerAdapter(
    val context: Context,
    private val ordersForMeList: MutableList<OrderForMe>,
    private val navController: NavController
): RecyclerView.Adapter<OrdersForMeRecyclerAdapter.OrdersForMeViewHolder>() {

    class OrdersForMeViewHolder(view: View): RecyclerView.ViewHolder(view){
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersForMeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_orders_for_me_single_row, parent, false)
        return OrdersForMeViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersForMeViewHolder, position: Int) {
        val orderForMe: OrderForMe = ordersForMeList[position]

        holder.txtOrderDate.text = orderForMe.orderDate
        holder.txtFromName.text = orderForMe.fromName
        holder.txtFromRole.text = orderForMe.fromRole
        holder.txtFromAddress.text = orderForMe.fromAddress
        holder.txtTotalItems.text = orderForMe.totalItems.toString()
        holder.txtTotalAmount.text = orderForMe.totalPrice.toString()

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
            AppDataSingleton.setOrderForMeId(orderForMe.orderId)
            navController.navigate(R.id.action_ordersForMeFragment_to_ordersForMeDetailsFragment)
        }
    }

    override fun getItemCount(): Int {
        return ordersForMeList.size
    }

}