package com.darjeelingteagarden.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton

class CartRecyclerAdapter(
    val context: Context,
    private val cartList: MutableList<Cart>,
    val cartChanged: () -> Unit
    ): RecyclerView.Adapter<CartRecyclerAdapter.CartViewHolder>() {


    class CartViewHolder(view: View): RecyclerView.ViewHolder(view){

        //common
        val txtProductName: TextView = view.findViewById<TextView>(R.id.txtProductName)
        val txtProductGrade: TextView = view.findViewById(R.id.txtProductGrade)
        val txtProductLotNumber: TextView = view.findViewById(R.id.txtProductLotNumber)

        //product cart
        val txtItemTotalBagSize: TextView = view.findViewById(R.id.txtItemTotalBagSize)
        val txtProductPrice: TextView = view.findViewById<TextView>(R.id.txtProductPrice)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val txtItemAmount: TextView = view.findViewById<TextView>(R.id.txtItemAmount)
        val llProductPrice: LinearLayout = view.findViewById(R.id.llProductPrice)
        val rlAddToCart: RelativeLayout = view.findViewById(R.id.rlAddToCart)
        val llBagSizeAndTotalItemAmount: LinearLayout = view.findViewById(R.id.llBagSizeAndTotalItemAmount)

        //sample cart
        val rlSampleCart: RelativeLayout = view.findViewById(R.id.rlSampleCart)
        val txtSamplePrice: TextView = view.findViewById(R.id.txtSamplePrice)
        val txtSampleItemAmount: TextView = view.findViewById(R.id.txtSampleItemAmount)
        val txtDecreaseSampleQuantity: TextView = view.findViewById(R.id.txtDecreaseSampleQuantity)
        val txtSampleQuantity: TextView = view.findViewById(R.id.txtSampleQuantity)
        val txtIncreaseSampleQuantity: TextView = view.findViewById(R.id.txtIncreaseSampleQuantity)
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
        holder.txtProductGrade.text = cartItem.grade
        holder.txtProductLotNumber.text = cartItem.lotNumber

        if (cartItem.isProduct){
            holder.txtProductPrice.text = cartItem.discountedPrice.toString()
            holder.txtQuantity.text = cartItem.quantity.toString()
            holder.txtItemAmount.text = (cartItem.discountedPrice * cartItem.quantity).toString()
            holder.txtItemTotalBagSize.text = "${cartItem.bagSize * cartItem.quantity} kg"
            holder.llProductPrice.visibility = View.VISIBLE
            holder.rlAddToCart.visibility = View.VISIBLE
            holder.llBagSizeAndTotalItemAmount.visibility = View.VISIBLE
        }
        else{
            holder.llProductPrice.visibility = View.GONE
            holder.rlAddToCart.visibility = View.GONE
            holder.llBagSizeAndTotalItemAmount.visibility = View.GONE
        }

        if (cartItem.isSample){
            holder.txtSamplePrice.text = "${cartItem.samplePrice} / 10 gram"
            holder.txtSampleQuantity.text = "${cartItem.sampleQuantity * 10} gram"
            holder.txtSampleItemAmount.text = (cartItem.samplePrice * cartItem.sampleQuantity).toString()
            holder.rlSampleCart.visibility = View.VISIBLE
        }
        else{
            holder.rlSampleCart.visibility = View.GONE
        }

        holder.txtIncreaseQuantity.setOnClickListener {
//            AppDataSingleton.increaseQuantity(position)
            CartDataSingleton.increaseProductQuantity(cartItem.productId)
            notifyItemChanged(position)
            cartChanged()
        }

        holder.txtDecreaseQuantity.setOnClickListener {

//            if (AppDataSingleton.getQuantityByProductId(cartItem.productId) > 1){
//                AppDataSingleton.decreaseQuantity(position)
//                notifyItemChanged(position)
//            }
//            else {
//                AppDataSingleton.decreaseQuantity(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, cartList.size)
//            }
            CartDataSingleton.decreaseProductQuantity(cartItem.productId)
            notifyDataSetChanged()
            cartChanged()

        }

        holder.txtIncreaseSampleQuantity.setOnClickListener {
            CartDataSingleton.increaseSampleQuantity(cartItem.productId)
            notifyItemChanged(position)
            cartChanged()
        }

        holder.txtDecreaseSampleQuantity.setOnClickListener {
            CartDataSingleton.decreaseSampleQuantity(cartItem.productId)
            notifyDataSetChanged()
            cartChanged()
        }

    }


}