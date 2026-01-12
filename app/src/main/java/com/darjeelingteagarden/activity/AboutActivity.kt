package com.darjeelingteagarden.activity

import android.content.Context
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.BuildConfig
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityAboutBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrderDetailsBinding
import com.darjeelingteagarden.model.About
import com.darjeelingteagarden.repository.AppDataSingleton

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    private var isHome = true;

    private lateinit var webView: WebView

    private lateinit var about: About

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        webView = binding.webView

        getAbout(this)

        setUpToolBar()

        showView(binding.scrollMain)

        binding.appVersion.text = "version ${BuildConfig.VERSION_NAME}"

        binding.fabCallNow.setOnClickListener {
            AppDataSingleton.callNow(this)
        }

        webView.webViewClient = object: WebViewClient(){

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
//                binding.rlProgress.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.rlProgress.visibility = View.GONE
            }
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (isHome){
                    finish()
                }
                else{
                    hideAll()
                    showView(binding.scrollMain)
                    isHome = true
                    changeToolbarTitle(getString(R.string.about_and_privacy_policy))
                }
            }

        })

        binding.AboutToolbar.setNavigationOnClickListener {

            if (isHome){
                finish()
            }
            else{
                hideAll()
                showView(binding.scrollMain)
                isHome = true
                changeToolbarTitle(getString(R.string.about_and_privacy_policy))
            }

        }

//        About
        binding.btnAbout.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/about-us/")
            renderInWebView(about.about)
            isHome = false
            changeToolbarTitle(getString(R.string.about))
        }
//        Disclaimer
        binding.btnDisclaimer.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/disclaimer/")
            renderInWebView(about.disclaimer)
            isHome = false
            changeToolbarTitle(getString(R.string.disclaimer))
        }
//        Privacy Policy
        binding.btnPrivacyPolicy.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/privacy-policy-2/")
            renderInWebView(about.privacyPolicy)
            isHome = false
            changeToolbarTitle(getString(R.string.privacy_policy))
        }
//        Terms and Condition
        binding.btnTermsAndCondition.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/terms-and-condition/")
            renderInWebView(about.termsAndCondition)

            isHome = false
            changeToolbarTitle(getString(R.string.terms_and_condition))
        }
//        Intellectual Property
        binding.btnIntellectualProperty.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/intellectual-property/")
            renderInWebView(about.intellectualProperty)

            isHome = false
            changeToolbarTitle(getString(R.string.intellectual_property))
        }
//        Payment Info
        binding.btnPaymentInfo.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/payment-and-logistics-partner-information/")
            renderInWebView(about.paymentInfo)

            isHome = false
            changeToolbarTitle(getString(R.string.payment_and_logistics_partner_information))
        }
//        Shipping Policy
        binding.btnShippingPolicy.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/shipping-policy/")
            renderInWebView(about.shippingPolicy)
            isHome = false
            changeToolbarTitle(getString(R.string.shipping_policy))
        }
//        Return Policy
        binding.btnReturnPolicy.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/return-policy/")
            renderInWebView(about.returnPolicy)
            isHome = false
            changeToolbarTitle(getString(R.string.return_refund_and_cancellation_policy))
        }
//        Process FLow
        binding.btnProcessFlow.setOnClickListener {
            hideAll()
//            openInWebView("https://darjeelingteagarden.com/process-flow/")
            renderInWebView(about.processFlow)
            isHome = false
            changeToolbarTitle(getString(R.string.process_flow))
        }

    }

    private fun setUpToolBar(){
        setSupportActionBar(binding.AboutToolbar)
        supportActionBar?.title = "About"
    }

    private fun changeToolbarTitle(name: String){
        supportActionBar?.title = name
    }

    private fun openInWebView(url: String){
        binding.rlProgress.visibility = View.VISIBLE
        binding.scrollMain.visibility = View.GONE
        webView.loadUrl(url)
//        binding.scrollViewWebView.visibility = View.VISIBLE
        binding.rlWebView.visibility = View.VISIBLE
    }

    private fun renderInWebView(html: String) {
        binding.scrollMain.visibility = View.GONE
        webView.loadData(html, "text/html", "UTF-8")
//        binding.scrollViewWebView.visibility = View.VISIBLE
        binding.rlWebView.visibility = View.VISIBLE
    }

    private fun hideAll(){
        binding.scrollAbout.visibility = View.GONE
        binding.scrollDisclaimer.visibility = View.GONE
        binding.scrollPrivacyPolicy.visibility = View.GONE
        binding.scrollTermsAndCondition.visibility = View.GONE
        binding.scrollPaymentInfo.visibility = View.GONE
        binding.scrollShippingPolicy.visibility = View.GONE
        binding.scrollReturnPolicy.visibility = View.GONE
        binding.scrollProcessFlow.visibility = View.GONE

        binding.rlWebView.visibility = View.GONE
//        binding.scrollViewWebView.visibility = View.GONE

        binding.scrollMain.visibility = View.GONE
    }

    private fun showView(view: ScrollView){
        view.visibility = View.VISIBLE
    }

    private fun getAbout(context: Context){

        binding.rlProgress.visibility = View.VISIBLE

        val queue = Volley.newRequestQueue(context)
        val url = getString(R.string.homeUrl) + "api/v1/about"

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (!success){
                        Toast.makeText(this, "And error occurred.", Toast.LENGTH_LONG).show()
                        binding.rlProgress.visibility = View.GONE
                        return@Listener
                    }

                    val data = it.getJSONArray("data").getJSONObject(0)

                    about = About(
                        data.getString("about"),
                        data.getString("disclaimer"),
                        data.getString("privacyPolicy"),
                        data.getString("termsAndCondition"),
                        data.getString("intellectualProperty"),
                        data.getString("returnRefundAndCancellationPolicy"),
                        data.getString("shippingPolicy"),
                        data.getString("paymentAndLogisticsPartnerInformation"),
                        data.getString("processFlow"),
                    )

                    binding.rlProgress.visibility = View.GONE

                }catch (e: Exception){
                    Log.d("about err volley", e.toString())
                    Toast.makeText(this, "And error occurred.", Toast.LENGTH_LONG).show()
                    binding.rlProgress.visibility = View.GONE
                }
            },
            Response.ErrorListener {
                Toast.makeText(this, "And error occurred.", Toast.LENGTH_LONG).show()
                binding.rlProgress.visibility = View.GONE
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