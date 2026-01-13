package com.darjeelingteagarden.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.core.view.setMargins
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.fragment.ProductDetailsFragment
import com.darjeelingteagarden.fragment.StoreFragment
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.model.Product
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
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
        val rlOriginalPrice: RelativeLayout = view.findViewById(R.id.rlOriginalPrice)
        val textProductDiscountedPrice: TextView = view.findViewById<TextView>(R.id.txtDiscountPrice)
        val txtGrade: TextView = view.findViewById(R.id.txtGrade)
        val txtLotNumber: TextView = view.findViewById(R.id.txtLotNumber)
        val txtBagSize: TextView = view.findViewById(R.id.txtBagSize)
        val btnAddToCart: Button = view.findViewById<Button>(R.id.btnAddToCart)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val llChangeQuantity: LinearLayout = view.findViewById<LinearLayout>(R.id.llChangeQuantity)
        val btnAddToSampleCart: Button = view.findViewById<Button>(R.id.btnAddToSampleCart)
        val txtSampleQuantity: TextView = view.findViewById<TextView>(R.id.txtSampleQuantity)
        val txtIncreaseSampleQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseSampleQuantity)
        val txtDecreaseSampleQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseSampleQuantity)
        val llChangeSampleQuantity: LinearLayout = view.findViewById<LinearLayout>(R.id.llChangeSampleQuantity)
        val rlAddToSampleCart: RelativeLayout = view.findViewById(R.id.rlAddToSampleCart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_store_single_row, parent, false)

        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {

        if (position == productList.size){
            holder.rlParent.visibility = View.INVISIBLE
            return
        }
        else{
            holder.rlParent.visibility = View.VISIBLE
        }

        val product: Product = productList[position]

        holder.textProductName.text = product.productName
        holder.txtGrade.text = product.grade
        holder.txtLotNumber.text = product.lotNumber.toString()
        holder.txtBagSize.text = product.bagSize.toString()
        Log.i("before replace :: ", product.imageUrl)
        //val imageUrl = context.getString(R.string.homeUrl) + product.imageUrl.replace("\\", "")
//        Log.i("image url :: ", imageUrl)

        if (product.discount && product.discountedPrice != product.originalPrice){
            holder.textProductOriginalPrice.text = product.originalPrice.toString()
            holder.textProductDiscountedPrice.text = product.discountedPrice.toString()
            holder.rlOriginalPrice.visibility = View.VISIBLE
        }
        else{
            holder.textProductDiscountedPrice.text = product.originalPrice.toString()
            holder.rlOriginalPrice.visibility = View.GONE
        }

        Picasso.get().load(
            product.imageUrl
        ).fit().centerCrop().into(holder.imgItemImage)
        Log.i("product image url :: ", product.imageUrl)

        val itemFoundInCart = cartList.find { it.productId == product.productId }
        val itemFoundInSampleCart = SampleDataSingleton.getCartItemList.find { it.productId == product.productId }

        val sampleQuantity = if (product.sampleQuantity != 0){
            product.sampleQuantity
        }else{
            10
        }

        val price = product.discountedPrice ?: product.originalPrice
//        val cartItem = Cart(
//            product.productId,
//            product.productName,
//            price,
//            product.grade,
//            product.lotNumber,
//            product.bagSize,
//            1,
//            false,
//            product.samplePrice,
//            1
//        )

//        NEW CART LOGIC
        if (CartDataSingleton.productFoundInCart(product.productId)){
            holder.btnAddToCart.visibility = View.GONE
            holder.llChangeQuantity.visibility = View.VISIBLE
            holder.txtQuantity.text = CartDataSingleton.getProductCartItem(product.productId)?.quantity.toString()
        }
        else{
            holder.btnAddToCart.visibility = View.VISIBLE
            holder.llChangeQuantity.visibility = View.GONE
        }

        if (CartDataSingleton.sampleFoundInCart(product.productId)){
            holder.btnAddToSampleCart.visibility = View.GONE
            holder.llChangeSampleQuantity.visibility = View.VISIBLE
            holder.txtSampleQuantity.text =
                "${CartDataSingleton.getSampleCartItem(product.productId)?.sampleQuantity?.times(10)} gram"
        }
        else{
            holder.btnAddToSampleCart.visibility = View.VISIBLE
            holder.llChangeSampleQuantity.visibility = View.GONE
        }


//        if (itemFoundInCart != null){
//            holder.btnAddToCart.visibility = View.GONE
//            holder.llChangeQuantity.visibility = View.VISIBLE
//            holder.txtQuantity.text = itemFoundInCart.quantity.toString()
//        }else {
//            holder.btnAddToCart.visibility = View.VISIBLE
//            holder.llChangeQuantity.visibility = View.GONE
//        }
//
//        if (itemFoundInSampleCart != null){
//            holder.btnAddToSampleCart.visibility = View.GONE
//            holder.llChangeSampleQuantity.visibility = View.VISIBLE
//            holder.txtSampleQuantity.text = "${itemFoundInSampleCart.sampleQuantity * 10} gram"
//        }else {
//            holder.btnAddToSampleCart.visibility = View.VISIBLE
//            holder.llChangeSampleQuantity.visibility = View.GONE
//        }

        if (product.samplePrice == 0.0){
            holder.rlAddToSampleCart.visibility = View.GONE
        }
        else{
            holder.rlAddToSampleCart.visibility = View.VISIBLE
        }

        Log.i("holder", holder.toString())

        holder.btnAddToCart.setOnClickListener {
            holder.txtQuantity.text = "1"
            holder.llChangeQuantity.visibility = View.VISIBLE
            it.visibility = View.GONE
            CartDataSingleton.addProductToCart(product)
            Toast.makeText(context, "Product added to cart", Toast.LENGTH_SHORT).show()
//            AppDataSingleton.addCartItem(cartItem)
//            Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())

        }

        holder.txtIncreaseQuantity.setOnClickListener {

            CartDataSingleton.increaseProductQuantity(product.productId)
            notifyItemChanged(position)

//            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
//            if (itemIndex != -1){
//                AppDataSingleton.increaseQuantity(itemIndex)
////            cartItem.quantity++
//                notifyItemChanged(position)
//                Log.i("increase quantity", AppDataSingleton.getCartItemList.toString())
//            }

        }

        holder.txtDecreaseQuantity.setOnClickListener {

            CartDataSingleton.decreaseProductQuantity(product.productId)
            notifyItemChanged(position)

//            val itemIndex = AppDataSingleton.getIndexByProductId(product.productId)
//            val quantity = AppDataSingleton.getQuantityByProductId(product.productId)
//            if (quantity == 1){
//                holder.btnAddToCart.visibility = View.VISIBLE
//                holder.llChangeQuantity.visibility = View.GONE
//            }
//            if (itemIndex != -1){
//                AppDataSingleton.decreaseQuantity(itemIndex)
//            }
//            notifyItemChanged(position)

//            itemIndex = AppDataSingleton.getIndexByProductId(product.productId)

//            AppDataSingleton.decreaseQuantity(itemIndex)
//            if (cartItem.quantity > 1){
//                cartItem.quantity--
//            }
//            notifyItemChanged(position)
//            Log.i("decrease quantity", AppDataSingleton.getCartItemList.toString())
        }

        holder.btnAddToSampleCart.setOnClickListener {

            holder.txtSampleQuantity.text = "10 gram"
            holder.llChangeSampleQuantity.visibility = View.VISIBLE
            it.visibility = View.GONE
            CartDataSingleton.addSampleToCart(product)
            Toast.makeText(context, "Sample added to cart", Toast.LENGTH_SHORT).show()
//            cartItem.isSample = true
//            SampleDataSingleton.addCartItem(cartItem)
//            Log.i("sample cart", SampleDataSingleton.getCartItemList.toString())

        }

        holder.txtIncreaseSampleQuantity.setOnClickListener {

            CartDataSingleton.increaseSampleQuantity(product.productId)
            notifyItemChanged(position)

//            val itemIndex = SampleDataSingleton.getIndexByProductId(product.productId)
//            if (itemIndex != -1){
//                SampleDataSingleton.increaseQuantity(itemIndex)
//                notifyItemChanged(position)
//                Log.i("increase sampleQuantity", SampleDataSingleton.getCartItemList.toString())
//            }

        }

        holder.txtDecreaseSampleQuantity.setOnClickListener {

            CartDataSingleton.decreaseSampleQuantity(product.productId)
            notifyItemChanged(position)

//            val itemIndex = SampleDataSingleton.getIndexByProductId(product.productId)
//            if (itemIndex != -1){
//                SampleDataSingleton.decreaseQuantity(itemIndex)
//            }
//            notifyItemChanged(position)
//
//            Log.i("decrease sampleQuantity", SampleDataSingleton.getCartItemList.toString())
        }

        holder.rlParent.setOnClickListener {
            AppDataSingleton.setCurrentProductId(product.productId)
            viewDetails(product)
            AppDataSingleton.currentProductIndex = position
        }

    }

    override fun getItemCount(): Int {
        return productList.size + 1
    }

}