package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityNewsBinding
import com.darjeelingteagarden.databinding.ActivityProductDetailsBinding
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ResizeTransformation
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer

class ProductDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailsBinding

    private var productId: String? = null

    private var imageList = arrayListOf<SlideModel>()
    private var imageUriArray = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.rlProgressProductDetails.visibility = View.VISIBLE

        binding.toolbarProductDetailsActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (intent != null){
            productId = intent.getStringExtra("productId")

        }
        Log.i("product id is : ", productId.toString())

        if (productId != null){
            getProductDetails(productId.toString())
        }

    }

    private fun getProductDetails(id: String){

        binding.rlProgressProductDetails.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(this@ProductDetailsActivity)

        val url = getString(R.string.homeUrl) + "api/v1/product/$id"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        imageList = arrayListOf()
                        imageUriArray = mutableListOf()

                        val productDetails = it.getJSONObject("data")

                        Log.i("product details is  : ", productDetails.toString())

                        val mainImage = productDetails.getString("mainImage")

                        imageList.add(SlideModel(mainImage))
                        imageUriArray.add(mainImage)

                        val images = productDetails.getJSONArray("images")

                        for (i in 0 until images.length()){
                            imageList.add(SlideModel(images.getString(i)))
                            imageUriArray.add(images.getString(i))
                        }

                        binding.imgSliderProductImg.setImageList(imageList)

                        binding.imgSliderProductImg.setItemClickListener(object :
                            ItemClickListener {

                            override fun doubleClick(position: Int) {
                                Log.i("doubleClicked on", "imgSlider")

                                StfalconImageViewer.Builder(this@ProductDetailsActivity, imageUriArray){ view, image ->

                                    Picasso.get().load(image).transform(ResizeTransformation(1080)).into(view)

                                }.allowSwipeToDismiss(false).withStartPosition(position).show()

                            }

                            override fun onItemSelected(position: Int) {
                                Log.i("clicked on", "imgSlider")

                                StfalconImageViewer.Builder(this@ProductDetailsActivity, imageUriArray){ view, image ->

                                    Picasso.get().load(image).transform(ResizeTransformation(1080)).into(view)

                                }.allowSwipeToDismiss(false).withStartPosition(position).show()

                            }

                        })

                        binding.txtDetailsProductName.text = productDetails.getString("name")
                        binding.txtDetailsProductDescription.text = productDetails.getString("description")

                        binding.rlProgressProductDetails.visibility = View.GONE

                    }
                    else{
                        println("product details not found")
                        binding.rlProgressProductDetails.visibility = View.GONE
                    }

                }catch (e: Exception){
                    println("product details not found")
                    binding.rlProgressProductDetails.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                println("product details not found")
                binding.rlProgressProductDetails.visibility = View.GONE
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

}