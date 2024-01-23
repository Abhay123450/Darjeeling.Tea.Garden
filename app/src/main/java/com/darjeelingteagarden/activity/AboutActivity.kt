package com.darjeelingteagarden.activity

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ScrollView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.darjeelingteagarden.BuildConfig
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityAboutBinding
import com.darjeelingteagarden.databinding.ActivitySampleOrderDetailsBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    private var isHome = true;

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        webView = binding.webView

        setUpToolBar()

        showView(binding.scrollMain)

        binding.appVersion.text = "version ${BuildConfig.VERSION_NAME}"

        webView.webViewClient = object: WebViewClient(){

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.rlProgress.visibility = View.VISIBLE
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
            openInWebView("https://darjeelingteagarden.com/about-us/")
            isHome = false
            changeToolbarTitle(getString(R.string.about))
        }
//        Disclaimer
        binding.btnDisclaimer.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/disclaimer/")
            isHome = false
            changeToolbarTitle(getString(R.string.disclaimer))
        }
//        Privacy Policy
        binding.btnPrivacyPolicy.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/privacy-policy-2/")
            isHome = false
            changeToolbarTitle(getString(R.string.privacy_policy))
        }
//        Terms and Condition
        binding.btnTermsAndCondition.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/terms-and-condition/")
            isHome = false
            changeToolbarTitle(getString(R.string.terms_and_condition))
        }
//        Intellectual Property
        binding.btnIntellectualProperty.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/intellectual-property/")
            isHome = false
            changeToolbarTitle(getString(R.string.intellectual_property))
        }
//        Payment Info
        binding.btnPaymentInfo.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/payment-and-logistics-partner-information/")
            isHome = false
            changeToolbarTitle(getString(R.string.payment_and_logistics_partner_information))
        }
//        Shipping Policy
        binding.btnShippingPolicy.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/shipping-policy/")
            isHome = false
            changeToolbarTitle(getString(R.string.shipping_policy))
        }
//        Return Policy
        binding.btnReturnPolicy.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/return-policy/")
            isHome = false
            changeToolbarTitle(getString(R.string.return_refund_and_cancellation_policy))
        }
//        Process FLow
        binding.btnProcessFlow.setOnClickListener {
            hideAll()
            openInWebView("https://darjeelingteagarden.com/process-flow/")
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

        binding.scrollMain.visibility = View.GONE
    }

    private fun showView(view: ScrollView){
        view.visibility = View.VISIBLE
    }

}