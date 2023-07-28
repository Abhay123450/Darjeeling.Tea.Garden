package com.darjeelingteagarden.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.repository.AppDataSingleton

class CartRecyclerAdapter(
    val context: Context,
    private val cartList: MutableList<Cart>,
    val cartChanged: () -> Unit
    ): RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {


    class CartViewHolder(view: View): RecyclerView.ViewHolder(view){

        val txtProductName: TextView = view.findViewById<TextView>(R.id.txtProductName)
        val txtProductPrice: TextView = view.findViewById<TextView>(R.id.txtProductPrice)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val txtItemAmount: TextView = view.findViewById<TextView>(R.id.txtItemAmount)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).
                inflate(R.layout.recycler_view_cart_single_row, parent, false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {

        val cartItem = cartList[position]
        holder.txtProductName.text = cartItem.productName
        holder.txtProductPrice.text = cartItem.discountedPrice.toString()
        holder.txtQuantity.text = cartItem.quantity.toString()
        holder.txtItemAmount.text = (cartItem.discountedPrice * cartItem.quantity).toString()

        holder.txtIncreaseQuantity.setOnClickListener {
            AppDataSingleton.increaseQuantity(position)
            notifyItemChanged(position)
            cartChanged()
        }

        holder.txtDecreaseQuantity.setOnClickListener {

            if (AppDataSingleton.getQuantityByProductId(cartItem.productId) > 1){
                AppDataSingleton.decreaseQuantity(position)
                notifyItemChanged(position)
            }
            else {
                AppDataSingleton.decreaseQuantity(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, cartList.size)
            }

            cartChanged()

        }

    }


}