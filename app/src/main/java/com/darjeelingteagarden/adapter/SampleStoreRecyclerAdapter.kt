package com.darjeelingteagarden.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Cart
import com.darjeelingteagarden.model.News
import com.darjeelingteagarden.model.Sample
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso

class SampleStoreRecyclerAdapter(
    val context: Context,
    private val sampleList: MutableList<Sample>,
    private val viewSampleImages: (sample: Sample) -> Unit
): RecyclerView.Adapter<SampleStoreRecyclerAdapter.SampleStoreViewHolder>() {

    class SampleStoreViewHolder(view: View): RecyclerView.ViewHolder(view){
        val cardParentSample: MaterialCardView = view.findViewById(R.id.cardParentSample)
        val imgSampleImage: ImageView = view.findViewById(R.id.imgSampleImage)
        val txtSampleName: TextView = view.findViewById(R.id.txtSampleName)
        val txtSampleLot: TextView = view.findViewById(R.id.txtSampleLot)
        val txtSampleGrade: TextView = view.findViewById(R.id.txtSampleGrade)
        val txtSampleBagSize: TextView = view.findViewById(R.id.txtBagSize)
        val txtSamplePrice: TextView = view.findViewById(R.id.txtSamplePrice)
        val btnAddToCart: Button = view.findViewById<Button>(R.id.btnAddToCart)
        val txtQuantity: TextView = view.findViewById<TextView>(R.id.txtQuantity)
        val txtIncreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtIncreaseQuantity)
        val txtDecreaseQuantity: TextView = view.findViewById<TextView>(R.id.txtDecreaseQuantity)
        val llChangeQuantity: LinearLayout = view.findViewById<LinearLayout>(R.id.llChangeQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleStoreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_view_sample_store_single_row, parent, false)
        return SampleStoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: SampleStoreViewHolder, position: Int) {
        val sample: Sample = sampleList[position]
        holder.txtSampleName.text = sample.sampleName
        holder.txtSampleLot.text = sample.sampleLot.toString()
        holder.txtSampleBagSize.text = sample.sampleBagSize.toString()
        holder.txtSampleGrade.text = sample.sampleGrade
        holder.txtSamplePrice.text = sample.samplePrice.toString()

//        val imageUrl = context.getString(R.string.homeUrl) + sample.sampleImageUrl.replace("\\", "")
        Picasso.get().load(
            sample.sampleImageUrl
        ).fit().centerCrop().into(holder.imgSampleImage)

        val itemFoundInCart = SampleDataSingleton.getCartItemList.find {
            it.productId == sample.sampleId
        }

        val cartItem = Cart(
            sample.sampleId,
            sample.sampleName,
            sample.samplePrice,
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

        holder.btnAddToCart.setOnClickListener {
            holder.txtQuantity.text = "1"
            holder.llChangeQuantity.visibility = View.VISIBLE
            it.visibility = View.GONE

            SampleDataSingleton.addCartItem(cartItem)
            Log.i("increase quantity", SampleDataSingleton.getCartItemList.toString())

        }

        holder.txtIncreaseQuantity.setOnClickListener {

            val itemIndex = SampleDataSingleton.getIndexByProductId(sample.sampleId)
            if (itemIndex != -1){
                SampleDataSingleton.increaseQuantity(itemIndex)
                notifyItemChanged(position)
                Log.i("increase quantity", SampleDataSingleton.getCartItemList.toString())
            }

        }

        holder.txtDecreaseQuantity.setOnClickListener {
            val itemIndex = SampleDataSingleton.getIndexByProductId(sample.sampleId)
            if (itemIndex != -1){
                SampleDataSingleton.decreaseQuantity(itemIndex)
            }
            notifyItemChanged(position)
            Log.i("decrease quantity", SampleDataSingleton.getCartItemList.toString())
        }

        holder.cardParentSample.setOnClickListener {
            viewSampleImages(sample)
        }

    }

    override fun getItemCount(): Int {
        return sampleList.size
    }

}