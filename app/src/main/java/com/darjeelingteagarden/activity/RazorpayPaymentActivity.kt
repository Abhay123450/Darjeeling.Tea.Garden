package com.darjeelingteagarden.activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityRazorpayPaymentBinding
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.repository.SampleDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject

class RazorpayPaymentActivity : AppCompatActivity(), PaymentResultWithDataListener, ExternalWalletListener {

    private lateinit var binding: ActivityRazorpayPaymentBinding

    private val TAG = RazorpayPaymentActivity::class.java.simpleName

    private var apiKeyId = ""

    private var sampleOrder = false
    private var orderId = ""

    private var itemTotal = 0.0
    private var totalAmount = 0.0
    private var totalTax = 0.0
    private var discount = 0.0

    private var paymentOrderId = ""

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRazorpayPaymentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent != null){
            sampleOrder = intent.getBooleanExtra("sampleOrder", false)
            orderId = intent.getStringExtra("orderId").toString()
            apiKeyId = intent.getStringExtra("apiKeyId").toString()
            itemTotal = intent.getDoubleExtra("itemTotal", 0.0)
            totalTax = intent.getDoubleExtra("totalTax", 0.0)
//            sgst = intent.getDoubleExtra("sgst", 0.0)
//            igst = intent.getDoubleExtra("igst", 0.0)
            totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        }

        queue = Volley.newRequestQueue(this)

        /*
        * To ensure faster loading of the Checkout form,
        * call this method as early as possible in your checkout flow
        * */
        Checkout.preload(applicationContext)
        val co = Checkout()
        // apart from setting it in AndroidManifest.xml, keyId can also be set
        // programmatically during runtime
        co.setKeyID(apiKeyId)

        updatePaymentDetails(
            itemTotal, totalTax, discount, totalAmount
        )

        binding.toolbarRazorpayPaymentActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        binding.btnPayNow.setOnClickListener {
            if (ConnectionManager().isOnline(this)) {
                createOrder(orderId)
            }
        }

        binding.btnPlaceOrder.setOnClickListener {
            if (ConnectionManager().isOnline(this)){
                placeOrder(orderId)
            }
        }

//        if (sampleOrder){
//            binding.btnPayByCash.visibility = View.GONE
//            binding.cardApplyCoupon.visibility = View.GONE
//        }

        binding.btnApplyCoupon.setOnClickListener {
            if (ConnectionManager().isOnline(this)){

                applyCoupon(binding.textInputEditTextCoupon.text.toString(), orderId)
                binding.progressApplyCoupon.visibility = View.VISIBLE

            }
        }

        binding.imgRemoveCoupon.setOnClickListener {
            if (ConnectionManager().isOnline(this)){
                removeCoupon(orderId)
            }
        }

        binding.btnOK.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        binding.btnRetryPayment.setOnClickListener {
            retryPayment()
        }

    }

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Log.i("paymentData ok", paymentData?.data.toString())
        if (paymentData != null) {
            verifyPayment(
                orderId,
                sampleOrder,
                paymentData.paymentId,
                paymentData.orderId,
                paymentData.signature
            )
        }
        else{
            binding.llPaymentFailed.visibility = View.VISIBLE
            binding.llProgressBar.visibility = View.GONE
        }
//        binding.llOrderPlacedSuccessfully.visibility = View.VISIBLE
//        binding.llProgressBar.visibility = View.GONE
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Log.i("paymentData err", p2?.data.toString())
        binding.llPaymentFailed.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {

    }

    private fun updatePaymentDetails(
        itemTotal: Double, gst: Double, discount: Double, total: Double
    ){
        binding.txtItemTotal.text = String.format("%.2f", itemTotal)
        binding.txtTotal.text = String.format("%.2f", total)
        binding.txtDiscount.text = String.format("%.2f", discount)
        binding.txtGST.text = String.format("%.2f", gst)

    }

    private fun retryPayment(){
        binding.llPaymentDetails.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
        binding.llPaymentFailed.visibility =View.GONE
        binding.llOrderPlacedSuccessfully.visibility = View.GONE
    }

    private fun startPayment(orderId: String, amount: Number, email: String, phoneNumber: Number) {
        /*
        *  You need to pass the current activity to let Razorpay create CheckoutActivity
        * */
        val activity: Activity = this
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name","Darjeeling Tea Garden")
            options.put("description","Order Payment")
            //You can omit the image option to fetch the image from the dashboard
//            options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.jpg")
            options.put("theme.color", "#00CCAA");
            options.put("currency","INR");
            options.put("order_id", orderId);
            options.put("amount",amount)//pass amount in currency subunits

            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email", email)
            prefill.put("contact", phoneNumber)

            options.put("prefill",prefill)
            co.setImage(R.drawable.darjeelingteagardenlogo_low)
            co.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun createOrder(orderId: String){

        binding.llProgressBar.visibility = View.VISIBLE

        val url = "${getString(R.string.homeUrl)}api/v1/payment/order"

        val json = JSONObject()
        json.put("orderId", orderId)
        json.put("sampleOrder", sampleOrder)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            json,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        binding.llProgressBar.visibility = View.VISIBLE

                        val data = it.getJSONObject("data")

                        Log.i("order created data: ", data.toString())

                        startPayment(
                            data.getString("id"),
                            data.getInt("amount"),
                            AppDataSingleton.getUserInfo.email,
                            AppDataSingleton.getUserInfo.phoneNumber
                        )

                    }

                }
                catch (e: Exception){
                    Toast.makeText(this, "Exception: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "Error ", Toast.LENGTH_LONG).show()
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

    private fun verifyPayment(
        orderId: String,
        sampleOrder: Boolean,
        razorpayPaymentId: String,
        razorpayOrderId: String,
        razorpaySignature: String
    ){

        val url = "${getString(R.string.homeUrl)}api/v1/payment/verifyPayment"

        val data = JSONObject()
        data.put("orderId", orderId)
        data.put("sampleOrder", sampleOrder)
        data.put("razorpayOrderId", razorpayOrderId)
        data.put("razorpayPaymentId", razorpayPaymentId)
        data.put("razorpaySignature", razorpaySignature)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            data,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

//                        if (sampleOrder){
//                            SampleDataSingleton.clearCart()
//                        }else{
//                            AppDataSingleton.clearCart(applicationContext)
//                        }
                        CartDataSingleton.clearCart(this)
                        AppDataSingleton.orderPlaced = true

                        binding.llOrderPlacedSuccessfully.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE

                    }
                    else{
                        binding.llPaymentFailed.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                    }
                }
                catch (e: Exception){
                    binding.llPaymentFailed.visibility = View.VISIBLE
                    binding.llProgressBar.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                binding.llPaymentFailed.visibility = View.VISIBLE
                binding.llProgressBar.visibility = View.GONE
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

    private fun applyCoupon(couponCode: String, orderId: String){

        val url = getString(R.string.homeUrl) + "api/v1/order/applyCoupon"

        val jsonBody = JSONObject()
        jsonBody.put("couponCode", couponCode)
        jsonBody.put("orderId", orderId)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val orderDetails = it.getJSONObject("data")

                        totalAmount = orderDetails.getDouble("amountPayable")

                        updatePaymentDetails(
                            orderDetails.getDouble("itemsPrice"),
                            orderDetails.getDouble("totalTax"),
                            orderDetails.getDouble("discount"),
                            orderDetails.getDouble("amountPayable"),
                        )

                        binding.progressApplyCoupon.visibility = View.INVISIBLE
                        binding.cardApplyCoupon.visibility = View.GONE
                        binding.cardCouponApplied.visibility = View.VISIBLE

                    }

                }
                catch (e: Exception){
                    Log.i("json exception: ", e.toString())
                    binding.progressApplyCoupon.visibility = View.INVISIBLE
                }
            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                binding.textInputLayoutCoupon.error = response.getString("message")
                binding.progressApplyCoupon.visibility = View.INVISIBLE

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

    private fun removeCoupon(orderId: String){

        val url = getString(R.string.homeUrl) + "api/v1/order/removeCoupon"

        val jsonBody = JSONObject()
        jsonBody.put("orderId", orderId)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val orderDetails = it.getJSONObject("data")

                        totalAmount = orderDetails.getDouble("amountPayable")

                        updatePaymentDetails(
                            orderDetails.getDouble("itemsPrice"),
                            orderDetails.getDouble("totalTax"),
                            orderDetails.getDouble("discount"),
                            orderDetails.getDouble("amountPayable"),
                        )

                        binding.progressApplyCoupon.visibility = View.INVISIBLE
                        binding.cardApplyCoupon.visibility = View.VISIBLE
                        binding.cardCouponApplied.visibility = View.GONE

                    }
                    else{
                        binding.progressApplyCoupon.visibility = View.INVISIBLE
                    }

                }
                catch (e: Exception){
                    Log.i("json exception: ", e.toString())
                    binding.progressApplyCoupon.visibility = View.INVISIBLE
                }
            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                binding.textInputLayoutCoupon.error = response.getString("message")
                binding.progressApplyCoupon.visibility = View.INVISIBLE

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

    private fun placeOrder(orderId: String){

        val url = if(sampleOrder){
            "${getString(R.string.homeUrl)}api/v1/sampleOrder/placeOrder"
        }
        else{
            "${getString(R.string.homeUrl)}api/v1/orders/placeOrder"
        }

        val jsonObject = JSONObject()
        jsonObject.put("orderId", orderId)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonObject,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

//                        if (sampleOrder){
//                            SampleDataSingleton.clearCart()
//                        }else{
//                            AppDataSingleton.clearCart(applicationContext)
//                        }
                        CartDataSingleton.clearCart(this)
                        AppDataSingleton.orderPlaced = true

                        binding.llOrderPlacedSuccessfully.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE

                    }
                    else{
                        binding.llPaymentFailed.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                    }
                }
                catch (e: Exception){
                    binding.llPaymentFailed.visibility = View.VISIBLE
                    binding.llProgressBar.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                binding.llPaymentFailed.visibility = View.VISIBLE
                binding.llProgressBar.visibility = View.GONE
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

    private fun redirectToTrackOrder(sampleOrder: Boolean){



    }

}