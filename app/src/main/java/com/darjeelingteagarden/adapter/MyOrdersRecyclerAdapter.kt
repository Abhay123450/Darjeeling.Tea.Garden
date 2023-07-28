package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.fragment.OrderDetailsFragment
import com.darjeelingteagarden.model.MyOrder
import com.darjeelingteagarden.repository.AppDataSingleton
import com.google.android.material.card.MaterialCardView

class MyOrdersRecyclerAdapter(
    val context: Context,
    private val myOrdersList: MutableList<MyOrder>,
    private val navController: NavController
): RecyclerView.Adapter<MyOrdersRecyclerAdapter.MyOrdersViewHolder>() {

    class MyOrdersViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardParent: MaterialCardView = view.findViewById(R.id.cardParent)
        val txtOrderedOn: TextView = view.findViewById(R.id.txtOrderedOn)
        val txtTotalItems: TextView = view.findViewById(R.id.txtTotalItems)
        val txtTotalPrice: TextView = view.findViewById(R.id.txtTotalPrice)
        val txtOrderStatusDelivered: TextView = view.findViewById(R.id.txtOrderStatusDelivered)
        val txtOrderStatusActive: TextView = view.findViewById(R.id.txtOrderStatusActive)
        val txtOrderStatusCancelled: TextView = view.findViewById(R.id.txtOrderStatusCancelled)
        val txtOrderStatusPartiallyDelivered: TextView = view.findViewById(R.id.txtOrderStatusPartiallyDelivered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyOrdersViewHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.recycler_view_my_orders_single_row, parent, false)
        return MyOrdersViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyOrdersViewHolder, position: Int) {
        val myOrder: MyOrder = myOrdersList[position]

        holder.txtTotalItems.text = myOrder.totalItems.toString()
        holder.txtTotalPrice.text = myOrder.totalPrice.toString()
        holder.txtOrderedOn.text = myOrder.orderDate

        when(myOrder.currentStatus){
            "Active" -> holder.txtOrderStatusActive.visibility = View.VISIBLE
            "Cancelled" -> holder.txtOrderStatusCancelled.visibility = View.VISIBLE
            "Partially Delivered" -> holder.txtOrderStatusPartiallyDelivered.visibility = View.VISIBLE
            "Delivered" -> holder.txtOrderStatusDelivered.visibility = View.VISIBLE
//            "Created" ->
        }

        holder.cardParent.setOnClickListener {

            AppDataSingleton.setOrderId(myOrder.orderId)

            navController.navigate(R.id.action_myOrdersFragment_to_orderDetailsFragment)

//            val activity = it.context as AppCompatActivity
//            activity.supportFragmentManager.beginTransaction().
//                replace(R.id.fragmentContainerView, OrderDetailsFragment()).
//                addToBackStack(OrderDetailsFragment::class.toString()).
//                commit()
        }


    }

    override fun getItemCount(): Int {
        return myOrdersList.size
    }
}