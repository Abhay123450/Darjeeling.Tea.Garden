package com.darjeelingteagarden.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityPayuPaymentBinding
import com.darjeelingteagarden.model.OrderInfo
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.CartDataSingleton
import com.darjeelingteagarden.util.RandomGenerator
import com.payu.base.models.ErrorResponse
import com.payu.base.models.OrderDetails
import com.payu.base.models.PayUPaymentParams
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.models.PayUCheckoutProConfig
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener
import org.json.JSONObject
import java.util.Date

class PayuPaymentActivity : BaseActivity() {

    private lateinit var binding: ActivityPayuPaymentBinding

    private lateinit var orderInfo: OrderInfo

    private val BASE_URL: String by lazy {
        getString(R.string.homeUrl)
    }

    companion object {
        private const val TAG = "PaymentActivity"
        private const val MERCHANT_KEY = "hBk1nP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPayuPaymentBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent != null){
            orderInfo = OrderInfo(
                    intent.getBooleanExtra("sampleOrder", false),
                    intent.getStringExtra("orderId").toString(),
                    intent.getStringExtra("apiKeyId").toString(),
                    intent.getDoubleExtra("itemTotal", 0.0),
                    intent.getDoubleExtra("discount", 0.0),
                    intent.getDoubleExtra("totalTax", 0.0),
                    intent.getDoubleExtra("totalAmount", 0.0)
                    )
        }

        viewPaymentDetails(orderInfo)

        Log.i("order info payupage", orderInfo.toString())

        binding.toolbarPayuPaymentActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        binding.btnOK.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        binding.btnRetryPayment.setOnClickListener {
            retryPayment()
        }

        binding.btnPayNow.setOnClickListener {

            val orderDetailList = ArrayList<OrderDetails>()
            CartDataSingleton.cartList.forEach {
                orderDetailList.add(
                    OrderDetails(
                        it.productName,
                        it.discountedPrice.toString())
                )
            }

            val payUCheckoutProConfig = PayUCheckoutProConfig()
            payUCheckoutProConfig.cartDetails = orderDetailList

            payUCheckoutProConfig.merchantName = "Dewill Industries Pvt Ltd"
            payUCheckoutProConfig.merchantLogo = R.drawable.darjeelingteagardenlogo_low
            payUCheckoutProConfig.showMerchantLogo = true
            payUCheckoutProConfig.autoSelectOtp = true

            val txnId = RandomGenerator().generateRandomString(7) + Date().time

            val payUPaymentParams = PayUPaymentParams.Builder()
            .setAmount(orderInfo.totalAmount.toString())
            .setIsProduction(false)  //set is to true for Production and false for UAT
            .setKey(MERCHANT_KEY)
            .setProductInfo("Tea")
            .setPhone(AppDataSingleton.getUserInfo.phoneNumber.toString())
            .setTransactionId(txnId)
            .setFirstName(AppDataSingleton.getUserInfo.name)
            .setEmail("${AppDataSingleton.getUserInfo.phoneNumber}@darjeelingteagarden.com")
            .setSurl("${BASE_URL}/api/v1/payment/payu/success")
            .setFurl("${BASE_URL}/api/v1/payment/payu/failure")
            .setUserCredential("${MERCHANT_KEY}:${AppDataSingleton.getUserInfo.userId}")
            .setAdditionalParams(hashMapOf<String, Any?>("orderId" to orderInfo.orderId, "userId" to AppDataSingleton.getUserInfo.userId)) //Optional, can contain any additional PG params
            .build()

            PayUCheckoutPro.open(
                this,
                payUPaymentParams,
                payUCheckoutProConfig,
                object : PayUCheckoutProListener {

                    override fun onPaymentSuccess(response: Any) {
                        response as HashMap<*, *>
                        val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
                        val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]
                        Log.i("payu success response", payUResponse.toString())
                        Log.i("merchant success response", merchantResponse.toString())

                        binding.llOrderPlacedSuccessfully.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                        Toast.makeText(
                            this@PayuPaymentActivity,
                            "Payment Successful",
                            Toast.LENGTH_LONG
                        ).show()
                        CartDataSingleton.clearCart(this@PayuPaymentActivity)
                        AppDataSingleton.orderPlaced = true
                    }


                    override fun onPaymentFailure(response: Any) {
                        response as HashMap<*, *>
                        val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
                        val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]
                        Log.i("payu failure response", payUResponse.toString())
                        Log.i("merchant failure response", merchantResponse.toString())

                        binding.llPaymentFailed.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                        Toast.makeText(
                            this@PayuPaymentActivity,
                            "Payment Failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onPaymentCancel(isTxnInitiated:Boolean) {
                        Log.i("payu cancel response", isTxnInitiated.toString())
                        Toast.makeText(
                            this@PayuPaymentActivity,
                            "Payment Cancelled",
                            Toast.LENGTH_LONG).show()
                        binding.llPaymentFailed.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                    }

                    override fun onError(errorResponse: ErrorResponse) {
                        if (errorResponse.errorMessage != null && errorResponse.errorMessage!!.isNotEmpty())
                            Log.e(TAG, "SDK Error: ${errorResponse.errorMessage}")
                        Toast.makeText(
                            this@PayuPaymentActivity,
                            errorResponse.errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        binding.llPaymentFailed.visibility = View.VISIBLE
                        binding.llProgressBar.visibility = View.GONE
                    }

                    override fun setWebViewProperties(webView: WebView?, bank: Any?) {
                        //For setting webview properties, if any. Check Customized Integration section for more details on this
                    }

                    override fun generateHash(
                        map: HashMap<String, String?>,
                        hashGenerationListener: PayUHashGenerationListener
                    ) {
                        val hashName = map[PayUCheckoutProConstants.CP_HASH_NAME]
                        val hashString = map[PayUCheckoutProConstants.CP_HASH_STRING]

                        Log.i("hashName", hashName.toString())
                        Log.i("hashString", hashString.toString())

//                        val generatedHash = HashGenerationUtil.HashGenerationUtil.generateHashFromSDK(
//                            hashString.toString(),
//                            MERCHANT_SALT,
//                        )
//
//                        val hashMap = HashMap<String, String?>()
//                        hashMap[hashName as String] = generatedHash
//                        hashGenerationListener.onHashGenerated(hashMap)

//                        val baseUrl = "http://192.168.1.3:3000"
                        val url = "${BASE_URL}/api/v1/payment/payu/hash"

                        // Create the JSON object
                        val jsonParams = JSONObject()
                        jsonParams.put("hashName", hashName)
                        jsonParams.put("hashString", hashString)

                        val jsonRequest = object: JsonObjectRequest(
                            Method.POST, url, jsonParams,
                            { response ->
                                Log.i("getHashResponse", response.toString())
                                // Assuming server returns: {"hash": "calculated_sha512_string"}
                                val generatedHash = response.getString("data")

                                val hashMap = HashMap<String, String?>()
                                hashMap[hashName as String] = generatedHash
                                hashGenerationListener.onHashGenerated(hashMap)
                            },
                            { error ->
                                Log.e("generate hash error", "Error: ${error.toString()}")
                                Toast.makeText(this@PayuPaymentActivity, "An error occurred", Toast.LENGTH_LONG).show()
                            }
                        ){
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Content-Type"] = "application/json"
                                headers["auth-token"] = AppDataSingleton.getAuthToken
                                return headers
                            }
                        }

                        Volley.newRequestQueue(this@PayuPaymentActivity).add(jsonRequest)
                        Log.i("get hash", "sending request")
                    }
                }
            )

        }

        binding.btnPlaceOrder.setOnClickListener {

        }

    }

    private fun viewPaymentDetails(orderInfo: OrderInfo){
        binding.txtItemTotal.text =
            getString(R.string.price_format, orderInfo.itemTotal)

        binding.txtDiscount.text =
            getString(R.string.price_format, orderInfo.discount)

        binding.txtGST.text =
            getString(R.string.price_format, orderInfo.totalTax)

        binding.txtTotal.text =
            getString(R.string.price_format, orderInfo.totalAmount)

    }

    private fun retryPayment(){
        binding.llPaymentDetails.visibility = View.VISIBLE
        binding.llProgressBar.visibility = View.GONE
        binding.llPaymentFailed.visibility =View.GONE
        binding.llOrderPlacedSuccessfully.visibility = View.GONE
    }

}