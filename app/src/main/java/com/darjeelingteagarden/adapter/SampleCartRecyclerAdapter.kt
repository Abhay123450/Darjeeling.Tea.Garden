package com.darjeelingteagarden.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.repository.SampleDataSingleton

class SampleCartRecyclerAdapter(
    val context: Context,
    private val cartList: MutableList<Cart>,
    val cartChanged: () -> Unit
): RecyclerView.Adapter<SampleCartRecyclerAdapter.SampleCartViewHolder>()  {

    class SampleCartViewHolder(view: View): RecyclerView.ViewHolder(view){

        val txtProductName: TextView = view.findViewById<TextView>(R.id.txtProductName)
        val txtProductPrice: TextView = view.findViewById<TextView>(R.id.txtProductPrice)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val txtItemAmount: TextView = view.findViewById<TextView>(R.id.txtItemAmount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleCartRecyclerAdapter.SampleCartViewHolder {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.recycler_view_cart_single_row, parent, false)
        return SampleCartRecyclerAdapter.SampleCartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: SampleCartRecyclerAdapter.SampleCartViewHolder, position: Int) {

        val cartItem = cartList[position]
        holder.txtProductName.text = cartItem.productName
        holder.txtProductPrice.text = cartItem.discountedPrice.toString()
        holder.txtQuantity.text = cartItem.quantity.toString()
        holder.txtItemAmount.text = (cartItem.discountedPrice * cartItem.quantity).toString()

        holder.txtIncreaseQuantity.setOnClickListener {
            SampleDataSingleton.increaseQuantity(position)
            notifyItemChanged(position)
            cartChanged()
        }

        holder.txtDecreaseQuantity.setOnClickListener {

            if (SampleDataSingleton.getQuantityByProductId(cartItem.productId) > 1){
                SampleDataSingleton.decreaseQuantity(position)
                notifyItemChanged(position)
            }
            else {
                SampleDataSingleton.decreaseQuantity(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartList.size)
            }

            cartChanged()

        }

    }
    
}