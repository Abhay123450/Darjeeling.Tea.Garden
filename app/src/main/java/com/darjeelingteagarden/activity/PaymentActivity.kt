package com.darjeelingteagarden.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityPaymentBinding
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.google.android.material.button.MaterialButton
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() {

    lateinit var paytmOrder: PaytmOrder
    lateinit var transactionManager: TransactionManager
    var txnToken = ""

    private var sampleOrder = false
    private var orderId = ""
    var itemTotal = 0.0
    var totalAmount = 0.0
    var totaltax = 0.0
//    var sgst = 0.0
//    var igst = 0.0

    private val mid = "hfSMlH96808400399764"

    private val ActivityRequestCode = 2

    private lateinit var binding: ActivityPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent != null){
            sampleOrder = intent.getBooleanExtra("sampleOrder", false)
            orderId = intent.getStringExtra("orderId").toString()
            itemTotal = intent.getDoubleExtra("itemTotal", 0.0)
            totaltax = intent.getDoubleExtra("totalTax", 0.0)
//            sgst = intent.getDoubleExtra("sgst", 0.0)
//            igst = intent.getDoubleExtra("igst", 0.0)
            totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        }

        if (sampleOrder){
            binding.btnPayByCash.visibility = View.GONE
            binding.cardApplyCoupon.visibility = View.GONE
        }

        updatePaymentDetails(itemTotal, totaltax, 0.00, totalAmount)

        binding.btnPayNow.setOnClickListener {

            if (ConnectionManager().isOnline(this@PaymentActivity)){

                processingPayment()

                if (totalAmount.toInt() != 0){

                    if (sampleOrder){
                        getSampleTransactionToken()
                    }
                    else{
                        getTransactionToken()
                    }

                }
                else {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Amount is Rs. 0",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }

        }

        binding.btnPayByCash.setOnClickListener {

            cashOnDelivery()

        }

        binding.btnApplyCoupon.setOnClickListener {
            if (ConnectionManager().isOnline(this@PaymentActivity)){

                applyCoupon()
                binding.progressApplyCoupon.visibility = View.VISIBLE

            }
        }

        binding.imgRemoveCoupon.setOnClickListener {
            if (ConnectionManager().isOnline(this)){
                removeCoupon()
            }
        }

        binding.btnOK.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnRetryPayment.setOnClickListener {
            retryPayment()
        }

    }

    private fun processingPayment(){
        binding.llPaymentDetails.visibility = View.GONE
        binding.llProgressBar.visibility = View.VISIBLE
    }

    private fun paymentSuccessful(){
        binding.llOrderPlacedSuccessfully.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
    }

    private fun paymentFailed(){
        binding.llPaymentFailed.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
    }

    private fun retryPayment(){
        binding.llPaymentDetails.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
        binding.llPaymentFailed.visibility =View.GONE
        binding.llOrderPlacedSuccessfully.visibility = View.GONE
    }

    private fun orderPlacedSuccessfully(){

    }

    private fun updatePaymentDetails(
        itemTotal: Double, gst: Double, discount: Double, total: Double
    ){
        binding.txtItemTotal.text = itemTotal.toString()
        binding.txtTotal.text = total.toString()
        binding.txtDiscount.text = discount.toString()
        binding.txtGST.text = gst.toString()

    }

    private fun cashOnDelivery(){

        val queue = Volley.newRequestQueue(this@PaymentActivity)

        val url = getString(R.string.homeUrl) + "api/v1/orders/cashOnDelivery"

        val jsonParams = JSONObject()
        jsonParams.put("orderId", orderId)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){
                        paymentSuccessful()
                    }

                }
                catch (e: Exception){
                    Toast.makeText(this, "An error occurred. Please try again $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "An error occurred. Please try again", Toast.LENGTH_LONG).show()
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

    private fun checkPaymentStatus(){

        val url = if (sampleOrder){
            getString(R.string.homeUrl) + "api/v1/payment/sampleOrder/status/" + orderId
        } else {
            getString(R.string.homeUrl) + "api/v1/payment/status/" + orderId
        }

//        val url = getString(R.string.homeUrl) + "api/v1/payment/status/" + orderId

        val queue = Volley.newRequestQueue(this@PaymentActivity)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val data = it.getJSONObject("data")
                        val status = data.getString("status")

                        if (status == "TXN_SUCCESS"){

                            paymentSuccessful()

                        }
                        else{

                            paymentFailed()

                        }

                    }

                }
                catch (e: Exception){
                    Toast.makeText(this, "An error occurred :: $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
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

    private fun applyCoupon(){

        val queue = Volley.newRequestQueue(this@PaymentActivity)

        val url = getString(R.string.homeUrl) + "api/v1/order/applyCoupon"

        val jsonBody = JSONObject()
        jsonBody.put("couponCode", binding.textInputEditTextCoupon.text.toString())
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

    private fun removeCoupon(){

        val queue = Volley.newRequestQueue(this@PaymentActivity)

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

    private fun initiateTransaction(){

        paytmOrder = PaytmOrder(
            orderId, mid, txnToken, totalAmount.toString(), "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=$orderId"
        )

        transactionManager = TransactionManager(
            paytmOrder, object : PaytmPaymentTransactionCallback{
                override fun onTransactionResponse(p0: Bundle?) {

                    checkPaymentStatus()

                    Toast.makeText(
                        this@PaymentActivity,
                        "On Transaction Response :: ${p0.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun networkNotAvailable() {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Network not available",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onErrorProceed(p0: String?) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "On Error Proceed :: ${p0.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun clientAuthenticationFailed(p0: String?) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Client auth failed :: ${p0.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun someUIErrorOccurred(p0: String?) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "UI Error :: ${p0.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onErrorLoadingWebPage(p0: Int, p1: String?, p2: String?) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "On Error Loading web page p0 :: $p0 ;; p1 :: $p1 ;; p2 :: $p2",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onBackPressedCancelTransaction() {
                    Toast.makeText(
                        this@PaymentActivity,
                        "Back button pressed - Transaction Cancelled !!",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onTransactionCancel(p0: String?, p1: Bundle?) {
                    Toast.makeText(
                        this@PaymentActivity,
                        "On Transaction Cancel :: ${p0.toString()} ;; p1 :: ${p1.toString()}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        )

        startTransaction()
//        transactionManager.setShowPaymentUrl("https://securegw-stage.paytm.in/theia/api/v1/showPaymentPage")
//        transactionManager.startTransaction(this@PaymentActivity, 2)

    }

    private fun startTransaction(){

        transactionManager.setShowPaymentUrl("https://securegw-stage.paytm.in/theia/api/v1/showPaymentPage")
        transactionManager.startTransaction(this@PaymentActivity, ActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ActivityRequestCode && data != null){

            checkPaymentStatus()

//            if (response != null) {
                Toast.makeText(
                    this@PaymentActivity,
                    data.getStringExtra("response"),
                    Toast.LENGTH_LONG
                ).show()
//            }
//            data.getStringExtra("nativeSdkForMerchantMessage")
        }
        else {
            Toast.makeText(
                this@PaymentActivity,
                "Payment failed !!",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun getTransactionToken(){

        val url = getString(R.string.homeUrl) + "api/v1/payment/initiateTransaction"

        val jsonBody = JSONObject()
        jsonBody.put("orderId", orderId)

        val queue = Volley.newRequestQueue(this@PaymentActivity)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        txnToken = it.getString("data")

                        initiateTransaction()

                    }
                    else{

                        Toast.makeText(
                            this, "An error occurred", Toast.LENGTH_LONG
                        ).show()

                    }

                }
                catch (e: Exception){
                    Toast.makeText(
                        this, "error exception: $e", Toast.LENGTH_LONG
                    ).show()
                }


            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                Toast.makeText(
                    this, response.getString("message"), Toast.LENGTH_LONG
                ).show()

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

    private fun getSampleTransactionToken(){

        val url = getString(R.string.homeUrl) + "api/v1/payment/initiateSampleTransaction"

        val jsonBody = JSONObject()
        jsonBody.put("orderId", orderId)

        val queue = Volley.newRequestQueue(this@PaymentActivity)

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        txnToken = it.getString("data")

                        initiateTransaction()

                    }
                    else{

                        Toast.makeText(
                            this, "An error occurred", Toast.LENGTH_LONG
                        ).show()

                    }

                }
                catch (e: Exception){
                    Toast.makeText(
                        this, "error exception: $e", Toast.LENGTH_LONG
                    ).show()
                }


            },
            Response.ErrorListener {

                val response = JSONObject(String(it.networkResponse.data))
                Toast.makeText(
                    this, response.getString("message"), Toast.LENGTH_LONG
                ).show()

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