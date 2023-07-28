package com.darjeelingteagarden.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.database.CartEntity
import com.darjeelingteagarden.database.CartViewModel
import com.darjeelingteagarden.fragment.ProductDetailsFragment
import com.darjeelingteagarden.fragment.StoreFragment
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.repository.AppDataSingleton
import com.squareup.picasso.Picasso

class StoreRecyclerAdapter(
    val context: Context,
    private val productList: MutableList<Product>,
    private val cartList: MutableList<Cart>,
    private val navController: NavController,
    private val viewDetails: (product: Product) -> Unit
): RecyclerView.Adapter<StoreRecyclerAdapter.StoreViewHolder>() {

    class StoreViewHolder(view: View): RecyclerView.ViewHolder(view){
        val rlParent: RelativeLayout = view.findViewById(R.id.rlParent)
        val imgItemImage: ImageView = view.findViewById(R.id.imgItemImage)
        val textProductName: TextView = view.findViewById<TextView>(R.id.txtItemName)
        val textProductOriginalPrice: TextView = view.findViewById<TextView>(R.id.txtItemOriginalPrice)
        val textProductDiscountedPrice: TextView = view.findViewById<TextView>(R.id.txtDiscountPrice)
        val txtGrade: TextView = view.findViewById(R.id.txtGrade)
        val txtLotNumber: TextView = view.findViewById(R.id.txtLotNumber)
        val txtBagSize: TextView = view.findViewById(R.id.txtBagSize)
        val btnAddToCart: Button = view.findViewById<Button>(R.id.btnAddToCart)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val llChangeQuantity: LinearLayout = view.findViewById<LinearLayout>(R.id.llChangeQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_store_single_row, parent, false)

        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val product: Product = productList[position]
        holder.textProductName.text = product.productName
        holder.textProductOriginalPrice.text = product.originalPrice.toString()
        holder.textProductDiscountedPrice.text = product.discountedPrice.toString()
        holder.txtGrade.text = product.grade
        holder.txtLotNumber.text = product.lotNumber.toString()
        holder.txtBagSize.text = product.bagSize.toString()
        Log.i("before replace :: ", product.imageUrl)
        //val imageUrl = context.getString(R.string.homeUrl) + product.imageUrl.replace("\\", "")
//        Log.i("image url :: ", imageUrl)

        Picasso.get().load(
            product.imageUrl
        ).fit().centerCrop().into(holder.imgItemImage)
        Log.i("product image url :: ", product.imageUrl)

        val itemFoundInCart = cartList.find { it.productId == product.productId }
        val cartItem = Cart(
            product.productId,
            product.productName,
            product.discountedPrice,
            1
        )

        if (itemFoundInCart != null){
            holder.btnAddToCart.visibility = View.GONE
            holder.llChangeQuantity.visibility = View.VISIBLE
            holder.txtQuantity.text = itemFoundInCart.quantity.toString()
        }else {
            holder.btnAddToCart.visibility = View.VISIBLE
            holder.llChangeQuantity.visibility = View.GONE
        }

        Log.i("holder", holder.toString())

        holder.btnAddToCart.setOnClickListener {
            holder.txtQuantity.text = "1"
            holder.llChangeQuantity.visibility = View.VISIBLE
            it.visibility = View.GONE

            AppDataSingleton.addCartItem(cartItem)
            Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())

        }

        holder.txtIncreaseQuantity.setOnClickListener {

            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
            if (itemIndex != -1){
                AppDataSingleton.increaseQuantity(itemIndex)
//            cartItem.quantity++
                notifyItemChanged(position)
                Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())
            }

        }

        holder.txtDecreaseQuantity.setOnClickListener {
            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
//            val quantity = AppDataSingleton.getQuantityByProductId(product.productId)
//            if (quantity == 1){
//                holder.btnAddToCart.visibility = View.VISIBLE
//                holder.llChangeQuantity.visibility = View.GONE
//            }
            if (itemIndex != -1){
                AppDataSingleton.decreaseQuantity(itemIndex)
            }
            notifyItemChanged(position)

//            itemIndex = AppDataSingleton.getIndexByProductId(product.productId)

//            AppDataSingleton.decreaseQuantity(itemIndex)
//            if (cartItem.quantity > 1){
//                cartItem.quantity--
//            }
//            notifyItemChanged(position)
            Log.i("decrease quantity", AppDataSingleton.getCartItemList.toString())
        }

        holder.rlParent.setOnClickListener {
            AppDataSingleton.setCurrentProductId(product.productId)
            viewDetails(product)
//            navController.navigate(R.id.action_storeFragment_to_productDetailsFragment)
        }

    }

    override fun getItemCount(): Int {
        return productList.size
    }

}