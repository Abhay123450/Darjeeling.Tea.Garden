package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.ItemDetails
import com.darjeelingteagarden.model.SampleOrderItemDetails

class SampleOrderDetailsItemListRecyclerAdapter(
    val context: Context,
    private val itemsList: MutableList<SampleOrderItemDetails>
): RecyclerView.Adapter<SampleOrderDetailsItemListRecyclerAdapter.SampleOrderDetailsViewHolder>() {

    class SampleOrderDetailsViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtGrade: TextView = view.findViewById(R.id.txtGrade)
        val txtLot: TextView = view.findViewById(R.id.txtLot)
        val txtItemPrice: TextView = view.findViewById(R.id.txtItemPrice)
        val txtItemQuantity: TextView = view.findViewById(R.id.txtItemQuantity)
        val txtItemTotalPrice: TextView = view.findViewById(R.id.txtItemTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleOrderDetailsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_sample_order_details_item_list_single_row, parent, false)
        return SampleOrderDetailsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SampleOrderDetailsViewHolder, position: Int) {
        val itemDetails: SampleOrderItemDetails = itemsList[position]

        holder.txtItemName.text = itemDetails.sampleName
        holder.txtGrade.text = itemDetails.grade
        holder.txtLot.text = itemDetails.lot
        holder.txtItemPrice.text = itemDetails.samplePrice.toString()
        holder.txtItemQuantity.text = itemDetails.quantity.toString()
        holder.txtItemTotalPrice.text = (itemDetails.samplePrice * itemDetails.quantity).toString()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

}