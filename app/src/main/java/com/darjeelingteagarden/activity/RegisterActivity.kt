package com.darjeelingteagarden.activity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityRegisterBinding
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.util.InputValidator
import com.darjeelingteagarden.util.LocationPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {

    //for location
//    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //location manager geeks for geeks
    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    lateinit var locationByNetwork: Location
    lateinit var locationByGps: Location

    var permissionDenied = false

//    lateinit var txtCreateAnAccount: TextView
//    lateinit var textInputLayoutRole: TextInputLayout
//    lateinit var autoCompleteTextViewRole: AutoCompleteTextView
//    lateinit var textInputLayoutPincode: TextInputLayout
//    lateinit var textInputEditTextPincode: TextInputEditText
//    lateinit var progressBarPincode: ProgressBar
//    lateinit var autoCompleteTextViewStates: AutoCompleteTextView
//    lateinit var autoCompleteTextViewCity: AutoCompleteTextView

    private lateinit var binding: ActivityRegisterBinding

    private var errorList = arrayListOf("name", "role", "phoneNumber", "email", "firmName", "address", "pincode")

    var phoneNumber: Long = 0
    var email = ""
    private lateinit var smsOtp: Number
    private lateinit var emailOtp: Number
    lateinit var userId: String
    var registered = false
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (intent != null){
            registered = intent.getBooleanExtra("registered", false)
            if (registered){
                phoneNumber = intent.getLongExtra("phoneNumber", 0)
                email = intent.getStringExtra("email").toString()
                userId = intent.getStringExtra("userId").toString()
            }
        }

        if(registered){
            sendOTP(phoneNumber.toString(), email, null)
            binding.svForm.visibility = View.GONE
            binding.llStep2.visibility = View.VISIBLE
            binding.llLoading.visibility = View.GONE
        }
        else{
            binding.svForm.visibility = View.VISIBLE
            binding.llStep2.visibility = View.GONE
            binding.llLoading.visibility = View.GONE
        }

        //fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@RegisterActivity)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationByGps = Location(LocationManager.GPS_PROVIDER)
        locationByNetwork = Location(LocationManager.NETWORK_PROVIDER)

        binding.autoCompleteTextViewRole.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrEmpty()){
                if (!errorList.contains("role")){
                    errorList.add("role")
                }
            }
            else{
                errorList.remove("role")
            }
        }

        binding.textInputEditTextPhoneNumber.doOnTextChanged { text, start, before, count ->

            if (text!!.isNotEmpty() && InputValidator().validatePhoneNumber(text!!.trim().toString().toLong())){
                binding.textInputLayoutPhoneNumber.error = null
                errorList.remove("phoneNumber")
                phoneNumber = text.trim().toString().toLong()
            }
            else{
                if (!errorList.contains("phoneNumber")){
                    errorList.add("phoneNumber")
                }
                binding.textInputLayoutPhoneNumber.error = "Phone number must be of 10 digits only"
            }
        }

        binding.textInputEditTextEmail.doOnTextChanged { text, start, before, count ->
            if (InputValidator().validateEmailAddress((text!!.toString()))){
                errorList.remove("email")
                binding.textInputLayoutEmail.error = null
                email = text.trim().toString()
            }
            else{
                if (!errorList.contains("email")){
                    errorList.add("email")
                }
                binding.textInputLayoutEmail.error = "Enter a valid email"
            }
        }

        binding.textInputEditTextGSTINNumber.doOnTextChanged { text, start, before, count ->
            if (InputValidator().validateGSTINNumber(text!!.toString())){
                binding.textInputLayoutGSTINNumber.error = null
                errorList.remove("gstin")
            }
            else{
                if (!errorList.contains("gstin")){
                    errorList.add("gstin")
                }
                binding.textInputLayoutGSTINNumber.error = "Enter a valid GSTIN number"
            }
        }

        binding.textInputEditTextName.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrBlank() || text.length < 3){
                binding.textInputLayoutName.error = "Enter your name"
                if (!errorList.contains("name")){
                    errorList.add("name")
                }
            }
            else{
                errorList.remove("name")
                binding.textInputLayoutName.error = null
            }
        }

//        binding.textInputEditTextInviteCode.doOnTextChanged { text, start, before, count ->
//            if (text.isNullOrEmpty() || text.length < 6){
//                binding.textInputLayoutInviteCode.error = "Enter invite code"
//                if (!errorList.contains("inviteCode")){
//                    errorList.add("inviteCode")
//                }
//            }
//            else {
//                errorList.remove("inviteCode")
//                binding.textInputLayoutInviteCode.error =  null
//            }
//        }

        binding.textInputEditTextFirmName.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrBlank() || text.length < 3){
                binding.textInputLayoutFirmName.error = "Enter firm name"
                if (!errorList.contains("firmName")){
                    errorList.add("firmName")
                }
            }
            else{
                errorList.remove("firmName")
                binding.textInputLayoutFirmName.error = null
            }
        }

        binding.textInputEditTextAddressLine1.doOnTextChanged { text, start, before, count ->
            if (text.isNullOrBlank() || text.length < 4){
                if (!errorList.contains("address")){
                    errorList.add("address")
                }
                binding.textInputLayoutAddressLine1.error = "Enter your address"
            }
            else{
                binding.textInputLayoutAddressLine1.error = null
                errorList.remove("address")
            }
        }

        binding.textInputEditTextPincode.doOnTextChanged { text, _, _, count ->

            if(count > 0 && InputValidator().validatePincode(text!!.toString().toInt())){

                errorList.remove("pincode")
                binding.textInputLayoutPincode.error = null

                if (ConnectionManager().isOnline(this@RegisterActivity)){

                    binding.progressBarPincode.visibility = ProgressBar.VISIBLE
                    getCityAndStateByPincode(text.toString().toInt())

                }
            }
            else {

                if (!errorList.contains("pincode")){
                    errorList.add("pincode")
                }

                binding.textInputLayoutPincode.error = "Pincode must be of 6 digits only"
//                autoCompleteTextViewStates.setText("")
//                autoCompleteTextViewCity.setText("")
//                binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
                binding.autoCompleteTextViewStates.setText("")
                binding.autoCompleteTextViewCity.setText("")

            }
        }

        binding.btnSubmit.setOnClickListener {

            Log.i("ErrorList", errorList.toString())
            getCurrentLocation()

            if (ConnectionManager().isOnline(this@RegisterActivity)){

                if (errorList.isEmpty()){
                    binding.llLoading.visibility = View.VISIBLE
                    onSubmitRegister()
                }
                else{
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter valid details",
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
            else{
                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setTitle("No Internet")
                builder.setMessage("Internet Connection not available. Please enable internet")
                builder.setNeutralButton("OK"){ _, _ ->            }
                builder.setCancelable(false).create().show()
            }

        }

        binding.textInputEditTextSmsOTP.doOnTextChanged { text, start, before, count ->
            if (InputValidator().validateOTP(text.toString().toInt())){
                smsOtp = text.toString().toInt()
                binding.textInputLayoutSmsOTP.error = null
            }
            else{
                binding.textInputLayoutSmsOTP.error = "OTP must be of 6 digits only"
            }
        }

        binding.textInputEditTextEmailOTP.doOnTextChanged { text, start, before, count ->
            if (InputValidator().validateOTP(text.toString().toInt())){
                emailOtp = text.toString().toInt()
                binding.textInputLayoutEmailOTP.error = null
            }
            else{
                binding.textInputLayoutEmailOTP.error = "OTP must be of 6 digits only"
            }
        }

        binding.txtPrivacyPolicyInfo.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.btnResendSmsOTP.setOnClickListener {
            sendOTP(phoneNumber.toString(), null, it)
        }

        binding.btnResendEmailOTP.setOnClickListener {
            sendOTP(null, email, it)
        }

        binding.btnSubmitOTP.setOnClickListener {

            if (binding.textInputLayoutEmailOTP.error != null || binding.textInputLayoutSmsOTP.error != null){
                return@setOnClickListener
            }

            if (ConnectionManager().isOnline(this@RegisterActivity)){

                binding.txtLoading.text = getString(R.string.verifying_otp)
                binding.llLoading.visibility = View.VISIBLE
                verifyOTP()

            }
            else{
                val builder = AlertDialog.Builder(this@RegisterActivity)
                builder.setTitle("No Internet")
                builder.setMessage("Internet Connection not available. Please enable internet")
                builder.setNeutralButton("OK"){ _, _ ->            }
                builder.setCancelable(false).create().show()
            }

        }

    }

    override fun onResume() {
        super.onResume()
        initializeRoleDropdown()
        if (!permissionDenied && !registered){
            getCurrentLocation()
//            LocationPermission().getCurrentLocation(this)
        }
    }

    private fun verifyOTP(){

        val jsonParams = JSONObject()
        jsonParams.put("phoneNumber", phoneNumber)
        jsonParams.put("smsOtp", smsOtp)
        jsonParams.put("email", email)
        jsonParams.put("emailOtp", emailOtp)

        val verifyPhoneWithOtpUrl = "${getString(R.string.homeUrl)}api/v1/user/verifyOTP"

        val queue = Volley.newRequestQueue(this@RegisterActivity)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            verifyPhoneWithOtpUrl,
            jsonParams,
            Response.Listener {
                try {

                    val success = it.getBoolean("success")

                    if (success){

                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intent.putExtra("registered", registered)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()

                    }
                    else {
                        binding.llLoading.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, it.getString("message"), Toast.LENGTH_LONG).show()
                    }
                }
                catch (e: Exception){
                    binding.llLoading.visibility = View.GONE
                    Toast.makeText(this@RegisterActivity, "Some error occurred : $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                val response = JSONObject(String(it.networkResponse.data))
                MaterialAlertDialogBuilder(this@RegisterActivity)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()
                binding.llLoading.visibility = View.GONE
                Toast.makeText(this@RegisterActivity, "Some error occurred", Toast.LENGTH_LONG).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)
    }

    private fun onSubmitRegister(){

        val jsonParams = createJsonBody()
        Log.i("JsonBody", jsonParams.toString())
        val registerUrl = getString(R.string.registerUrl)

        val queue = Volley.newRequestQueue(this@RegisterActivity)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            registerUrl,
            jsonParams,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")

                    if (success){

                        registered = true
                        userId = it.getJSONObject("data").getString("userId")

                        binding.btnResendSmsOTP.visibility = View.GONE
                        binding.txtResendSmsOtpTimer.visibility = View.VISIBLE
                        binding.btnResendEmailOTP.visibility = View.GONE
                        binding.txtResendEmailOtpTimer.visibility = View.VISIBLE

                        val timer = object: CountDownTimer(60000, 1000){
                            override fun onTick(p0: Long) {
                                binding.txtResendSmsOtpTimer.text =
                                    "Resend OTP in ${p0/1000} second"
                                binding.txtResendEmailOtpTimer.text =
                                    "Resend OTP in ${p0/1000} second"
                            }

                            override fun onFinish() {
                                binding.btnResendSmsOTP.visibility = View.VISIBLE
                                binding.txtResendSmsOtpTimer.visibility = View.GONE
                                binding.txtResendSmsOtpTimer.text = ""
                                binding.btnResendEmailOTP.visibility = View.VISIBLE
                                binding.txtResendEmailOtpTimer.visibility = View.GONE
                                binding.txtResendEmailOtpTimer.text = ""
                            }

                        }
                        timer.start()

                        binding.svForm.visibility = View.GONE
                        binding.llStep2.visibility = View.VISIBLE
                        binding.llLoading.visibility = View.GONE
                    }
                    else{
                        binding.llLoading.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, it.getString("message"), Toast.LENGTH_LONG).show()
                    }

                }
                catch (e: Exception){
                    binding.llLoading.visibility = View.GONE
                    Toast.makeText(this@RegisterActivity, "Some error occurred : $e", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {

                Log.i("Volley error", it.printStackTrace().toString())

                val response = JSONObject(String(it.networkResponse.data))
                Log.i("Volley error response", response.toString())
                MaterialAlertDialogBuilder(this@RegisterActivity)
                    .setTitle("Message")
                    .setMessage(response.getString("message").toString())
                    .setNeutralButton("OK") { _, _ -> }
                    .show()
                binding.llLoading.visibility = View.GONE
//                Toast.makeText(this@RegisterActivity, "Some error occurred. $response", Toast.LENGTH_LONG).show()
                //Log.i("Volley error", it.printStackTrace().toString())
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun sendOTP(phoneNumber: String?, email: String?, btn: View?){

        if(btn != null){
            btn.visibility = View.GONE
        }


        val url = getString(R.string.homeUrl) + "api/v1/user/forgotPassword"
        val queue = Volley.newRequestQueue(this@RegisterActivity)

        val jsonParams = JSONObject()
        if (phoneNumber != null){
            jsonParams.put("phoneNumber", phoneNumber)
        }
        if (email != null){
            jsonParams.put("email", email)
        }

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {

                    val success = it.getBoolean("success")
                    if (success){

                        val otpSentToPhone = it.getBoolean("otpSentToPhone")
                        val otpSentToEmail = it.getBoolean("otpSentToEmail")

                        if (otpSentToPhone){

                            binding.btnResendSmsOTP.visibility = View.GONE
                            binding.txtResendSmsOtpTimer.visibility = View.VISIBLE

                            val timer = object: CountDownTimer(60000, 1000){
                                override fun onTick(p0: Long) {
                                    binding.txtResendSmsOtpTimer.text =
                                        "Resend OTP in ${p0/1000} seconds"
                                }

                                override fun onFinish() {
                                    binding.btnResendSmsOTP.visibility = View.VISIBLE
                                    binding.txtResendSmsOtpTimer.text = ""
                                }

                            }
                            timer.start()

                        }

                        if (otpSentToEmail){

                            binding.btnResendEmailOTP.visibility = View.GONE
                            binding.txtResendEmailOtpTimer.visibility = View.VISIBLE

                            val timer = object: CountDownTimer(60000, 1000){
                                override fun onTick(p0: Long) {
                                    binding.txtResendEmailOtpTimer.text =
                                        "Resend OTP in ${p0/1000} seconds"
                                }

                                override fun onFinish() {
                                    binding.btnResendEmailOTP.visibility = View.VISIBLE
                                    binding.txtResendEmailOtpTimer.text = ""
                                }

                            }
                            timer.start()
                        }



                    }
                    else{
                        Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()

                    }

                }
                catch (e: Exception){
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {

//                val response = JSONObject(String(it.networkResponse.data))
                Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()

            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun createJsonBody(): JSONObject{

        val jsonBody = JSONObject()
        jsonBody.put("name", binding.textInputEditTextName.text.toString())
        jsonBody.put("phoneNumber", binding.textInputEditTextPhoneNumber.text.toString().toLong())
        jsonBody.put("email", binding.textInputEditTextEmail.text.toString())
        jsonBody.put("role", binding.autoCompleteTextViewRole.text.toString())
        jsonBody.put("firmName", binding.textInputEditTextFirmName.text.toString())
        jsonBody.put("addressLineOne", binding.textInputEditTextAddressLine1.text.toString())
        jsonBody.put("addressLineTwo", binding.textInputEditTextAddressLine2.text.toString())
        jsonBody.put("pincode", binding.textInputEditTextPincode.text.toString().toInt())
        jsonBody.put("state", binding.autoCompleteTextViewStates.text.toString())
        jsonBody.put("city", binding.autoCompleteTextViewCity.text.toString())
        jsonBody.put("latitude", latitude)
        jsonBody.put("longitude", longitude)
        jsonBody.put("password", binding.textInputEditTextNewPassword.text.toString())
        jsonBody.put("inviteCode", binding.textInputEditTextInviteCode.text.toString())
        jsonBody.put("gstin", binding.textInputEditTextGSTINNumber.text.toString())

        return jsonBody
    }

    private fun initializeRoleDropdown(){

        val roles = arrayListOf("Retailer", "Wholesaler", "Dealer", "Distributor", "Super Stockist")

        val arrayAdapter = ArrayAdapter(this@RegisterActivity, R.layout.dropdown_item, roles)
        binding.autoCompleteTextViewRole.setAdapter(arrayAdapter)

    }

    private fun initializeStateDropdown(){

        val states = arrayListOf("Andhra Pradesh", "Arunachal Pradesh", "Bihar", "Gujarat", "Uttar Pradesh")

        val arrayAdapter = ArrayAdapter(this@RegisterActivity, R.layout.dropdown_item, states)
        binding.autoCompleteTextViewStates.setAdapter(arrayAdapter)

    }

    private fun getCityAndStateByPincode(pincode: Int){

        val returnObject = JSONObject()
        returnObject.put("state", "")
        returnObject.put("city", "")

        if (pincode < 100000 || pincode > 999999){
            return
        }

        val url = "https://api.postalpincode.in/pincode/$pincode"

        val queue = Volley.newRequestQueue(this@RegisterActivity)

        Log.i("url", url)

        val jsonRequest = object : JsonArrayRequest(Request.Method.GET, url, null,
        Response.Listener {
            try {

                val responseObject = it.getJSONObject(0)
                Log.i("responseDaArray", responseObject.toString())
                val success = responseObject.getString("Status") == "Success"

                Log.i("Success", success.toString())

                if (success){

                    val pincodeObject = responseObject.getJSONArray("PostOffice").getJSONObject(0)
                    returnObject.put("state", pincodeObject.getString("State"))
                    returnObject.put("city", pincodeObject.getString("District"))

                    binding.autoCompleteTextViewStates.setText(returnObject.getString("state"))
                    binding.autoCompleteTextViewCity.setText(returnObject.getString("city"))

                    binding.progressBarPincode.visibility = ProgressBar.INVISIBLE

                } else {
                    Toast.makeText(this@RegisterActivity, "Request not successful", Toast.LENGTH_LONG).show()
                    binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
                    binding.autoCompleteTextViewStates.setText("")
                    binding.autoCompleteTextViewCity.setText("")
                    binding.autoCompleteTextViewStates.inputType = InputType.TYPE_CLASS_TEXT
                    binding.autoCompleteTextViewCity.inputType = InputType.TYPE_CLASS_TEXT
                }

            } catch (e: Exception){
                Toast.makeText(this@RegisterActivity, "Some Error Occurred", Toast.LENGTH_LONG).show()
                binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
                binding.autoCompleteTextViewStates.setText("")
                binding.autoCompleteTextViewCity.setText("")
                binding.autoCompleteTextViewStates.inputType = InputType.TYPE_CLASS_TEXT
                binding.autoCompleteTextViewCity.inputType = InputType.TYPE_CLASS_TEXT
            }

        }, Response.ErrorListener {

            Toast.makeText(this@RegisterActivity, "Cannot Fetch City and State", Toast.LENGTH_LONG).show()
            binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
            binding.autoCompleteTextViewStates.setText("")
            binding.autoCompleteTextViewCity.setText("")
            binding.autoCompleteTextViewStates.inputType = InputType.TYPE_CLASS_TEXT
            binding.autoCompleteTextViewCity.inputType = InputType.TYPE_CLASS_TEXT

        }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        queue.add(jsonRequest)

    }

    private fun getLocationByLocationManager(){

        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//------------------------------------------------------//
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        val gpsLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByGps = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
//------------------------------------------------------//
        val networkLocationListener: LocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationByNetwork= location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        if (hasGps) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
                return
            }
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                0F,
                gpsLocationListener
            )
        }
//------------------------------------------------------//
        if (hasNetwork) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                0F,
                networkLocationListener
            )
        }


        val lastKnownLocationByGps =
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        lastKnownLocationByGps?.let {
            locationByGps = lastKnownLocationByGps
        }
//------------------------------------------------------//
        val lastKnownLocationByNetwork =
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        lastKnownLocationByNetwork?.let {
            locationByNetwork = lastKnownLocationByNetwork
        }
//------------------------------------------------------//
        if (locationByGps.accuracy > locationByNetwork.accuracy) {
            currentLocation = locationByGps
            latitude = currentLocation!!.latitude
            longitude = currentLocation!!.longitude
            // use latitude and longitude as per your need
            Toast.makeText(this, "latitude : $latitude, longitude: $longitude", Toast.LENGTH_LONG).show()
        } else {
            currentLocation = locationByNetwork
            latitude = currentLocation!!.latitude
            longitude = currentLocation!!.longitude
            // use latitude and longitude as per your need
            Toast.makeText(this, "latitude : $latitude, longitude: $longitude", Toast.LENGTH_LONG).show()
        }
    }

    private fun getCurrentLocation(){

        if (checkPermissions()){

            if (isLocationEnabled()){
                //get location fused location
//                if (ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_FINE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                        this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                    ) != PackageManager.PERMISSION_GRANTED
//                ) {
//                    requestPermission()
//                    return
//                }
//                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
//                    val location: Location? = task.result
//                    if (location == null){
//                        Toast.makeText(this, "Location null", Toast.LENGTH_SHORT).show()
//                    }
//                    else{
//                        latitude = location.latitude
//                        longitude = location.longitude
//                        Toast.makeText(this, "Location received latitude is $latitude and longitude is $longitude", Toast.LENGTH_SHORT).show()
//                    }
//
//                }

                //get location location manager
                getLocationByLocationManager()

            }
            else{
                //open settings
                Toast.makeText(this, "Location not enabled", Toast.LENGTH_SHORT).show()

                MaterialAlertDialogBuilder(this@RegisterActivity)
                    .setTitle("Enable Location")
                    .setMessage("Please enable location access. Click OK to open settings and enable location.")
                    .setCancelable(false)
                    .setPositiveButton("OK"){dialogue, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("Exit"){dialogue, which ->
                        finish()
                    }
                    .show()

            }
        }
        else{
            //request permission
            requestPermission()
        }
    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions(): Boolean{

        if (
            ActivityCompat.checkSelfPermission(
                this@RegisterActivity, android.Manifest.permission.ACCESS_COARSE_LOCATION
             ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@RegisterActivity, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false

    }

    private fun requestPermission(){

        ActivityCompat.requestPermissions(
            this@RegisterActivity,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext, "Location permission granted", Toast.LENGTH_LONG).show()
//                if (isLocationEnabled()){
//                    getCurrentLocation()
//                }
                permissionDenied = false
            }
            else{
                permissionDenied = true
                Toast.makeText(applicationContext, "Location permission denied", Toast.LENGTH_LONG).show()
                MaterialAlertDialogBuilder(this@RegisterActivity)
                    .setTitle("Location Permission Required")
                    .setMessage("Please allow location permission request")
                    .setPositiveButton("OK"){dialogue, which ->
                        requestPermission()
                        permissionDenied = false
                    }
                    .setNegativeButton("Exit"){dialogue, which ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
            }
        }
    }
}
