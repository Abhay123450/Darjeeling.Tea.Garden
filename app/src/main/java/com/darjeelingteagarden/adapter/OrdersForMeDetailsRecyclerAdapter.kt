package com.darjeelingteagarden.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.ProductDetailsActivity
import com.darjeelingteagarden.model.ItemDetails

class OrdersForMeDetailsRecyclerAdapter(
    val context: Context,
    private val itemsList: MutableList<ItemDetails>,
    val deliverItemClick: (productId: String, quantity: Int, productName: String, isSample: Boolean) -> Unit
): RecyclerView.Adapter<OrdersForMeDetailsRecyclerAdapter.OrdersForMeDetailsViewHolder>() {

    class OrdersForMeDetailsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtOrderStatusDelivered: TextView = view.findViewById(R.id.txtOrderStatusDelivered)
        val etQuantity: EditText = view.findViewById(R.id.etQuantity)
        val txtQuantity: TextView = view.findViewById(R.id.txtQuantity)
        val txtQuantityDelivered: TextView = view.findViewById(R.id.txtQuantityDelivered)
        val llQuantityDelivered: LinearLayout = view.findViewById(R.id.llQuantityDelivered)
        val btnDeliverItem: Button = view.findViewById(R.id.btnDeliverItem)
        val btnDeliverItemOK: Button = view.findViewById(R.id.btnDeliverItemOK)
        val btnDeliverItemCancel: Button = view.findViewById(R.id.btnDeliverItemCancel)
        val progressBarDeliver: ProgressBar = view.findViewById(R.id.progressBarDeliver)
        val btnWaiting: Button = view.findViewById(R.id.btnWaiting)
        val llDeliver: LinearLayout = view.findViewById(R.id.llDeliver)
        val txtItemTotalPrice: TextView = view.findViewById(R.id.txtItemTotalPrice)
        val llItemTotalPrice: LinearLayout = view.findViewById(R.id.llItemTotalPrice)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val txtItemIsSample: TextView = view.findViewById(R.id.txtItemIsSample)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersForMeDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_orders_for_me_details_single_row, parent, false)
        return OrdersForMeDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrdersForMeDetailsViewHolder, position: Int) {
        val itemDetails: ItemDetails = itemsList[position]

        var sample = ""
        var quantity = ""
        if (itemDetails.isSample){
            sample = "[Sample] "
            quantity = "${itemDetails.itemQuantity * 10} gram"
        }
        else{
            quantity = itemDetails.itemQuantity.toString()
        }


        holder.txtItemName.text = "$sample${itemDetails.itemName}"
        holder.txtQuantity.text = quantity
        holder.etQuantity.setText(itemDetails.itemQuantity.toString())
        holder.txtItemPrice.text = itemDetails.itemPrice.toString()

        holder.txtItemTotalPrice.text = (itemDetails.itemQuantity * itemDetails.itemPrice).toString()
        holder.etQuantity.visibility = View.GONE
        holder.llDeliver.visibility = View.GONE
        holder.llQuantityDelivered.visibility = View.GONE

        if (itemDetails.receiveQuantity != 0){
            holder.txtQuantityDelivered.text = itemDetails.receiveQuantity.toString()
            holder.llQuantityDelivered.visibility = View.VISIBLE
        }

        Log.d("recive Quantity", itemDetails.receiveQuantity.toString())

        if (itemDetails.itemStatus == "Active"){
            holder.btnDeliverItem.visibility = View.VISIBLE
            holder.txtOrderStatusDelivered.visibility = View.GONE
            holder.btnWaiting.visibility = View.GONE
        }
        else if (itemDetails.itemStatus == "Waiting"){
            holder.btnWaiting.visibility = View.VISIBLE
            holder.txtOrderStatusDelivered.visibility = View.GONE
            holder.btnDeliverItem.visibility = View.GONE
        }
        else if (itemDetails.itemStatus == "Delivered"){
            holder.txtOrderStatusDelivered.visibility = View.VISIBLE
            holder.btnWaiting.visibility = View.GONE
            holder.btnDeliverItem.visibility = View.GONE
        }

        holder.txtItemName.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("productId", itemDetails.id)
            context.startActivity(intent)
        }

        holder.btnDeliverItem.setOnClickListener {
            it.visibility = View.GONE
            holder.llDeliver.visibility = View.VISIBLE
            holder.etQuantity.visibility = View.VISIBLE
            holder.llItemTotalPrice.visibility = View.GONE
        }

        holder.btnDeliverItemOK.setOnClickListener {
            holder.progressBarDeliver.visibility = View.VISIBLE
            holder.llDeliver.visibility = View.GONE
            deliverItemClick(itemDetails.id, holder.etQuantity.text.toString().toInt(), itemDetails.itemName, itemDetails.isSample)
        }

        holder.btnDeliverItemCancel.setOnClickListener {
            holder.llDeliver.visibility = View.GONE
            holder.btnDeliverItem.visibility = View.VISIBLE
            holder.etQuantity.visibility = View.GONE
            holder.txtQuantity.visibility = View.VISIBLE
            holder.llItemTotalPrice.visibility = View.VISIBLE
        }

    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

}