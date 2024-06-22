package com.darjeelingteagarden.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.databinding.ActivityRegisterBinding
import com.darjeelingteagarden.databinding.ActivityUserDetailsBinding
import com.darjeelingteagarden.model.User
import com.darjeelingteagarden.model.UserDetails
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.util.ConnectionManager
import com.darjeelingteagarden.util.InputValidator
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

class UserDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailsBinding

    private lateinit var queue: RequestQueue

    private lateinit var userDetails: UserDetails
    private lateinit var updatedUserDetails: UserDetails

    private var errorList = arrayListOf<String>()

    private var smsOtpRequired = false
    private var emailOtpRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        queue = Volley.newRequestQueue(this)

        fetchUserDetails()

        binding.toolbarUserDetailsActivity.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.textInputEditTextPhoneNumber.doOnTextChanged { text, start, before, count ->

            if (text!!.isNotEmpty() && InputValidator().validatePhoneNumber(text.trim().toString())){
                binding.textInputLayoutPhoneNumber.error = null
                errorList.remove("phoneNumber")
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
            }
            else{
                if (!errorList.contains("email")){
                    errorList.add("email")
                }
                binding.textInputLayoutEmail.error = "Enter a valid email"
            }
        }

        binding.textInputEditTextGSTINNumber.doOnTextChanged { text, start, before, count ->
            if (text.toString().isBlank() || InputValidator().validateGSTINNumber(text!!.toString())){
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

                if (ConnectionManager().isOnline(this)){

                    binding.progressBarPincode.visibility = ProgressBar.VISIBLE
                    getCityAndStateByPincode(text.toString().toInt())

                }
                else{
                    AppDataSingleton.noInternet(this)
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

        binding.btnUpdate.setOnClickListener {

            updatedUserDetails = getFormData()

            var phoneNumber: Long? = 0

            if (
                InputValidator().validatePhoneNumber(userDetails.phoneNumber.toString()) &&
                InputValidator().validatePhoneNumber(updatedUserDetails.phoneNumber.toString()) &&
                userDetails.phoneNumber != updatedUserDetails.phoneNumber
            ){
                phoneNumber = updatedUserDetails.phoneNumber
                smsOtpRequired = true
            }
            else{
                phoneNumber = null
            }

            var email: String? = ""

            if (
                InputValidator().validateEmailAddress(userDetails.email) &&
                InputValidator().validateEmailAddress(updatedUserDetails.email) &&
                userDetails.email != updatedUserDetails.email
            ){
                email = updatedUserDetails.email
                emailOtpRequired = true
            } else {
                email = null
            }

            if (email == null && phoneNumber == null){
                if (errorList.size == 0){
                    updateUserDetails(updatedUserDetails, null, null)
                }
                else{
                    Toast.makeText(this, "Invalid Input", Toast.LENGTH_LONG).show()
                }
            }
            else{
                binding.rlProgressBar.visibility = View.VISIBLE

                sendOTP(phoneNumber, userDetails.phoneNumber, email, userDetails.email, null)

                binding.llUpdateDetails.visibility = View.GONE
                binding.llStep2.visibility = View.VISIBLE
                var text = ""
                if (smsOtpRequired){
                    binding.llPhoneOtp.visibility = View.VISIBLE
                    text += "Enter otp sent to $phoneNumber. "
                }
                else{
                    binding.llPhoneOtp.visibility = View.GONE
                }
                if (emailOtpRequired){
                    binding.llEmailOtp.visibility = View.VISIBLE
                    text += "\nEnter otp sent to $email"
                }
                else{
                    binding.llEmailOtp.visibility = View.GONE
                }
                binding.txtStep2Hint.text = text

            }

        }

        binding.btnResendSmsOTP.setOnClickListener {
            val phoneNumber = if (
                InputValidator().validatePhoneNumber(userDetails.phoneNumber.toString()) &&
                InputValidator().validatePhoneNumber(updatedUserDetails.phoneNumber.toString()) &&
                userDetails.phoneNumber != updatedUserDetails.phoneNumber
            ){
                updatedUserDetails.phoneNumber
            }
            else{
                null
            }

            sendOTP(phoneNumber, userDetails.phoneNumber,null, null,  it)
        }

        binding.btnResendEmailOTP.setOnClickListener {

            val email = if (userDetails.email != updatedUserDetails.email){
                updatedUserDetails.email
            } else {
                null
            }

            sendOTP(null, null, email, userDetails.email, it)
        }

        binding.btnSubmitOTP.setOnClickListener {

            var smsOtp: String? = null
            var emailOtp: String? = null

            if (smsOtpRequired){
                if (binding.textInputEditTextSmsOTP.text.isNullOrBlank() ||
                        binding.textInputEditTextSmsOTP.text!!.trim().length != 6){
                    Toast.makeText(this, "Enter valid otp", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                else{
                    smsOtp = binding.textInputEditTextSmsOTP.text.toString()
                }
            }

            if (emailOtpRequired){
                if (binding.textInputEditTextEmailOTP.text.isNullOrBlank() ||
                    binding.textInputEditTextEmailOTP.text!!.trim().length != 6){
                    Toast.makeText(this, "Enter valid otp", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                else{
                    emailOtp = binding.textInputEditTextEmailOTP.text.toString()
                }
            }
            binding.rlProgressBar.visibility = View.VISIBLE
            updateUserDetails(updatedUserDetails, smsOtp, emailOtp)

        }

    }

    private fun fillUserDetails(userDetails: UserDetails){
        binding.textInputEditTextName.setText(userDetails.name)
        binding.textInputEditTextPhoneNumber.setText(userDetails.phoneNumber.toString())
        binding.textInputEditTextEmail.setText(userDetails.email)
        binding.textInputEditTextFirmName.setText(userDetails.firmName)
        binding.textInputEditTextGSTINNumber.setText(userDetails.gstin)
        binding.textInputEditTextInviteCode.setText(userDetails.inviteCode)
        binding.textInputEditTextAddressLine1.setText(userDetails.addressLineOne)
        binding.textInputEditTextAddressLine2.setText(userDetails.addressLineTwo)
        binding.textInputEditTextPincode.setText(userDetails.pincode.toString())
        binding.autoCompleteTextViewCity.setText(userDetails.city)
        binding.autoCompleteTextViewStates.setText(userDetails.state)
    }

    private fun getFormData(): UserDetails{
        return UserDetails(
            userDetails._id,
            userDetails.userId,
            binding.textInputEditTextName.text.toString(),
            userDetails.role,
            binding.textInputEditTextPhoneNumber.text.toString().toLong(),
            binding.textInputEditTextEmail.text.toString(),
            binding.textInputEditTextAddressLine1.text.toString(),
            binding.textInputEditTextAddressLine2.text.toString(),
            binding.textInputEditTextPincode.text.toString().toInt(),
            binding.autoCompleteTextViewCity.text.toString(),
            binding.autoCompleteTextViewStates.text.toString(),
            binding.textInputEditTextFirmName.text.toString(),
            binding.textInputEditTextGSTINNumber.text.toString(),
            binding.textInputEditTextInviteCode.text.toString(),
        )
    }

    private fun validateUserInput(updatedUserDetails: UserDetails): Boolean{

        var allGood = true

        if (InputValidator().validatePhoneNumber(updatedUserDetails.phoneNumber.toString())){
            binding.textInputLayoutPhoneNumber.error = null
        }
        else{
            allGood = false
            binding.textInputLayoutPhoneNumber.error = "Phone number should be of 10 digits"
        }

        if (InputValidator().validateEmailAddress(updatedUserDetails.email)){

        }


        return allGood

    }

    private fun fetchUserDetails(){

        val url = getString(R.string.homeUrl) + "api/v1/user"

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener {
                try {

                    binding.rlProgressBar.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success){

                        binding.llUpdateDetails.visibility = View.VISIBLE

                        val data = it.getJSONObject("data")

                        userDetails = UserDetails(
                            data.getString("_id"),
                            data.getString("userId"),
                            data.getString("name"),
                            data.getString("role"),
                            data.getLong("phoneNumber"),
                            data.getString("email"),
                            data.getString("addressLineOne"),
                            data.optString("addressLineTwo"),
                            data.getInt("pincode"),
                            data.getString("city"),
                            data.getString("state"),
                            data.optString("firmName"),
                            data.optString("gstin"),
                            data.optString("inviteCode"),
                        )

                        fillUserDetails(userDetails)

                    }
                    else{
                        Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception){
                    binding.rlProgressBar.visibility = View.GONE
                    Toast.makeText(this, "An error occurred $e", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                binding.rlProgressBar.visibility = View.GONE
                Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        queue.add(jsonObjectRequest)

    }

    private fun updateUserDetails(userUpdatedDetails: UserDetails, smsOtp: String?, emailOtp: String?){

        val url = getString(R.string.homeUrl) + "api/v1/user/update"
        val updatedUserData = getFormData()

        val jsonBody = JSONObject(Gson().toJson(userUpdatedDetails))
        if (smsOtp != null){
            jsonBody.put("smsOtp", smsOtp)
        }
        if (emailOtp != null){
            jsonBody.put("emailOtp", emailOtp)
        }

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonBody,
            Response.Listener {
                try {

                    binding.rlProgressBar.visibility = View.GONE

                    val success = it.getBoolean("success")

                    if (success) {

                        Toast.makeText(this, "Updated successfully", Toast.LENGTH_LONG).show()


                        val data = it.getJSONObject("data")

                        userDetails = UserDetails(
                            data.getString("_id"),
                            data.getString("userId"),
                            data.getString("name"),
                            data.getString("role"),
                            data.getLong("phoneNumber"),
                            data.getString("email"),
                            data.getString("addressLineOne"),
                            data.optString("addressLineTwo"),
                            data.getInt("pincode"),
                            data.getString("city"),
                            data.getString("state"),
                            data.optString("firmName"),
                            data.optString("gstin"),
                            data.optString("inviteCode"),
                        )

                        AppDataSingleton.setUserInfo(User(
                            userDetails.userId,
                            userDetails.name,
                            userDetails.role,
                            userDetails.phoneNumber,
                            userDetails.email
                        ))

                        fillUserDetails(userDetails)

                        binding.llUpdateDetails.visibility = View.VISIBLE
                        binding.llStep2.visibility = View.GONE

//                        val requirePhoneOtp = it.getBoolean("requirePhoneOtp")
//                        val requireEmailOtp = it.getBoolean("requireEmailOtp")
//
//                        if (requirePhoneOtp){
//                            binding.llPhoneOtp.visibility = View.VISIBLE
//
//                            binding.btnResendSmsOTP.visibility = View.GONE
//                            binding.txtResendSmsOtpTimer.visibility = View.VISIBLE
//
//                            val timer = object: CountDownTimer(60000, 1000){
//                                override fun onTick(p0: Long) {
//                                    binding.txtResendSmsOtpTimer.text =
//                                        "Resend OTP in ${p0/1000} second"
//                                }
//
//                                override fun onFinish() {
//                                    binding.btnResendSmsOTP.visibility = View.VISIBLE
//                                    binding.txtResendSmsOtpTimer.visibility = View.GONE
//                                    binding.txtResendSmsOtpTimer.text = ""
//                                }
//
//                            }
//                            timer.start()
//                        }
//
//                        if (requireEmailOtp){
//                            binding.llEmailOtp.visibility = View.VISIBLE
//
//                            binding.btnResendEmailOTP.visibility = View.GONE
//                            binding.txtResendEmailOtpTimer.visibility = View.VISIBLE
//
//                            val timer = object: CountDownTimer(60000, 1000){
//                                override fun onTick(p0: Long) {
//                                    binding.txtResendEmailOtpTimer.text =
//                                        "Resend OTP in ${p0/1000} second"
//                                }
//
//                                override fun onFinish() {
//                                    binding.btnResendEmailOTP.visibility = View.VISIBLE
//                                    binding.txtResendEmailOtpTimer.visibility = View.GONE
//                                    binding.txtResendEmailOtpTimer.text = ""
//                                }
//
//                            }
//                            timer.start()
//                        }
//
//
//
//
//                        val timer = object: CountDownTimer(60000, 1000){
//                            override fun onTick(p0: Long) {
//                                binding.txtResendSmsOtpTimer.text =
//                                    "Resend OTP in ${p0/1000} second"
//                                binding.txtResendEmailOtpTimer.text =
//                                    "Resend OTP in ${p0/1000} second"
//                            }
//
//                            override fun onFinish() {
//                                binding.btnResendSmsOTP.visibility = View.VISIBLE
//                                binding.txtResendSmsOtpTimer.visibility = View.GONE
//                                binding.txtResendSmsOtpTimer.text = ""
//                                binding.btnResendEmailOTP.visibility = View.VISIBLE
//                                binding.txtResendEmailOtpTimer.visibility = View.GONE
//                                binding.txtResendEmailOtpTimer.text = ""
//                            }
//
//                        }
//                        timer.start()

                    }

                }catch (e: Exception){
                    binding.rlProgressBar.visibility = View.GONE
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
                }
            },
            Response.ErrorListener {
                binding.rlProgressBar.visibility = View.GONE
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

    private fun sendOTP(newPhoneNumber: Long?, currentPhoneNumber: Long?, newEmail: String?, currentEmail: String?, btn: View?){

        if(btn != null){
            btn.visibility = View.GONE
        }


        val url = getString(R.string.homeUrl) + "api/v1/user/sendOtpNew"
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        if (newPhoneNumber != null && currentPhoneNumber != null){
            jsonParams.put("newPhoneNumber", newPhoneNumber)
            jsonParams.put("currentPhoneNumber", currentPhoneNumber)
        }
        if (newEmail != null && currentEmail != null){
            jsonParams.put("newEmail", newEmail)
            jsonParams.put("currentEmail", currentEmail)
        }

        val jsonObjectRequest = object: JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {

                try {
                    binding.rlProgressBar.visibility = View.GONE

                    val success = it.getBoolean("success")
                    if (success){

//                        val otpSentToPhone = it.getBoolean("otpSentToPhone")
//                        val otpSentToEmail = it.getBoolean("otpSentToEmail")

                        if (smsOtpRequired){

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

                        if (emailOtpRequired){

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
                        if (btn != null) {
                            btn.visibility = View.VISIBLE
                        }
                        Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
                    }

                }
                catch (e: Exception){
                    if (btn != null) {
                        btn.visibility = View.VISIBLE
                    }
                    binding.rlProgressBar.visibility = View.GONE
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {
                if (btn != null) {
                    btn.visibility = View.VISIBLE
                }
                binding.rlProgressBar.visibility = View.GONE
//                val response = JSONObject(String(it.networkResponse.data))
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

    private fun getCityAndStateByPincode(pincode: Int){

        val returnObject = JSONObject()
        returnObject.put("state", "")
        returnObject.put("city", "")

        if (pincode < 100000 || pincode > 999999){
            return
        }

        val url = "https://api.postalpincode.in/pincode/$pincode"

        val queue = Volley.newRequestQueue(this)

        Log.i("url", url)

        val jsonRequest = object : JsonArrayRequest(
            Request.Method.GET, url, null,
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
                        binding.autoCompleteTextViewStates.inputType = InputType.TYPE_NULL
                        binding.autoCompleteTextViewCity.setText(returnObject.getString("city"))
                        binding.autoCompleteTextViewCity.inputType = InputType.TYPE_NULL

                        binding.progressBarPincode.visibility = ProgressBar.INVISIBLE

                    } else {
                        Toast.makeText(this, "Request not successful", Toast.LENGTH_LONG).show()
                        binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
                        binding.autoCompleteTextViewStates.inputType = InputType.TYPE_CLASS_TEXT
                        binding.autoCompleteTextViewCity.inputType = InputType.TYPE_CLASS_TEXT
                    }

                } catch (e: Exception){
                    Toast.makeText(this, "Some Error Occurred", Toast.LENGTH_LONG).show()
                    binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
                    binding.autoCompleteTextViewStates.inputType = InputType.TYPE_CLASS_TEXT
                    binding.autoCompleteTextViewCity.inputType = InputType.TYPE_CLASS_TEXT
                }

            }, Response.ErrorListener {

                Toast.makeText(this, "Cannot Fetch City and State", Toast.LENGTH_LONG).show()
                binding.progressBarPincode.visibility = ProgressBar.INVISIBLE
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
}