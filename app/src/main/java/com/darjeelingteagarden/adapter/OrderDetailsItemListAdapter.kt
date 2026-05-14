package com.darjeelingteagarden.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.ProductDetailsActivity
import com.darjeelingteagarden.features.looseTea.activity.LooseTeaDetailsActivity
import com.darjeelingteagarden.features.packagedTea.activity.PackagedTeaDetailsActivity
import com.darjeelingteagarden.model.ItemDetails
import com.darjeelingteagarden.util.formatPaiseToRupees
import com.google.android.material.card.MaterialCardView

class OrderDetailsItemListAdapter(
    val context: Context,
    private val itemsList: MutableList<ItemDetails>,
    val receiveItemClick: (item: ItemDetails) -> Unit
): RecyclerView.Adapter<OrderDetailsItemListAdapter.OrderDetailsViewHolder>() {

    class OrderDetailsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardParent: MaterialCardView = view.findViewById(R.id.cardParent)
        val txtItemType: TextView = view.findViewById(R.id.txtItemType)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtOrderStatusDelivered: TextView = view.findViewById(R.id.txtOrderStatusDelivered)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val txtItemQuantity: TextView = view.findViewById(R.id.txtItemQuantity)
        val txtItemQuantityDelivered: TextView = view.findViewById(R.id.txtItemQuantityDelivered)
        val llQuantityDelivered: LinearLayout = view.findViewById(R.id.llQuantityDelivered)
        val txtItemTotalPrice: TextView = view.findViewById(R.id.txtItemTotalPrice)
        val btnReceiveItem: Button = view.findViewById(R.id.btnReceiveItem)
        val txtDeliveredOn: TextView = view.findViewById(R.id.txtDeliveredOn)
        val txtDeliveredBy: TextView = view.findViewById(R.id.txtDeliveredBy)
        val progressBtnReceive: ProgressBar = view.findViewById(R.id.progressBtnReceive)
        val rlReceiveItem: RelativeLayout = view.findViewById(R.id.rlReceiveItem)
        val rlDeliveryInfo: RelativeLayout = view.findViewById(R.id.rlDeliveryInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_details_item_list_single_row, parent, false)
        return OrderDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailsViewHolder, position: Int) {
        val itemDetails: ItemDetails = itemsList[position]
        Log.i("item details my orders", itemDetails.toString())
        var itemInfo = ""
        if (itemDetails.productType !== null){
            if (itemDetails.productType == "looseTea"){
                itemInfo = "Loose Tea"
            }
            else if (itemDetails.productType == "packagedTea"){
                itemInfo = "Packaged Tea"
            }
        }
        var quantity = ""
        var itemPrice = ""
        if (itemDetails.isSample){
            itemInfo = if (itemInfo == ""){
                "Sample"
            } else {
                "$itemInfo • Sample"
            }
            quantity = "${itemDetails.itemQuantity * 10} gram"
            itemPrice = if (itemDetails.currencyUnit == "paise"){
                "${formatPaiseToRupees(itemDetails.itemPrice)} / 10 gram"
            } else {
                "₹${itemDetails.itemPrice} / 10 gram"
            }
        }
        else{
            quantity = "${itemDetails.itemQuantity} bag"
            itemPrice = when (itemDetails.productType) {
                "looseTea" -> {
                    if (itemDetails.currencyUnit == "paise"){
                        "${formatPaiseToRupees(itemDetails.itemPrice)} / bag"
                    } else {
                        "₹${itemDetails.itemPrice} / bag"
                    }
                }
                "packagedTea" -> {
                    if (itemDetails.currencyUnit == "paise"){
                        "${formatPaiseToRupees(itemDetails.itemPrice)} / bag"
                    } else {
                        "₹${itemDetails.itemPrice} / bag"
                    }
                }
                else -> {
                    itemDetails.itemPrice.toString()
                }
            }
        }

        holder.txtItemType.text = itemInfo
        holder.txtItemName.text = itemDetails.itemName
        holder.txtItemPrice.text = itemPrice
        holder.txtItemQuantity.text = quantity
        holder.txtItemTotalPrice.text = if (itemDetails.currencyUnit == "paise"){
            formatPaiseToRupees(itemDetails.itemPrice * itemDetails.itemQuantity)
        } else {
            String.format("%.2f", (itemDetails.itemPrice * itemDetails.itemQuantity).toDouble())
        }
        holder.llQuantityDelivered.visibility = View.GONE

        if (itemDetails.receiveQuantity != 0){
            holder.txtItemQuantityDelivered.text = itemDetails.receiveQuantity.toString()
            holder.llQuantityDelivered.visibility = View.VISIBLE
        }

        if (itemDetails.itemStatus == "Delivered"){
            holder.rlDeliveryInfo.visibility = View.VISIBLE
            holder.rlReceiveItem.visibility = View.GONE
            holder.txtDeliveredOn.text = itemDetails.receiveTime
            holder.txtOrderStatusDelivered.visibility = View.VISIBLE
        }
        else if (itemDetails.itemStatus == "Waiting"){
            val text = "Receive Item | Quantity - ${itemDetails.receiveQuantity}"
            holder.btnReceiveItem.text = text
            holder.rlDeliveryInfo.visibility = View.GONE
            holder.rlReceiveItem.visibility = View.VISIBLE
            holder.txtOrderStatusDelivered.visibility = View.GONE
        }
        else {
            holder.rlDeliveryInfo.visibility = View.GONE
            holder.rlReceiveItem.visibility = View.GONE
            holder.txtOrderStatusDelivered.visibility = View.GONE
        }

        holder.cardParent.setOnClickListener {
            if (itemDetails.productType == "looseTea"){
                val intent = Intent(context, LooseTeaDetailsActivity::class.java)
                intent.putExtra("looseTeaId", itemDetails.id)
                context.startActivity(intent)
            }
            else if (itemDetails.productType == "packagedTea"){
                val intent = Intent(context, PackagedTeaDetailsActivity::class.java)
                intent.putExtra("packagedTeaId", itemDetails.id)
                context.startActivity(intent)
            }
            else{
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra("productId", itemDetails.id)
                context.startActivity(intent)
            }
        }

        holder.btnReceiveItem.setOnClickListener {
            it.visibility = View.GONE
            holder.progressBtnReceive.visibility = View.VISIBLE
            receiveItemClick(itemDetails)
        }

    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

}