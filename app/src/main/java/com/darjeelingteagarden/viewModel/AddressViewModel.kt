package com.darjeelingteagarden.viewModel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.darjeelingteagarden.R
import com.darjeelingteagarden.model.Address
import com.darjeelingteagarden.model.AddressState
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.VolleySingleton
import org.json.JSONObject

class AddressViewModel: ViewModel() {
    private val _addressState = MutableLiveData<AddressState>(AddressState.Home)
    val addressState: LiveData<AddressState> = _addressState

    private val _addresses = mutableListOf<Address>()

    init {
        _addresses.add(Address(
            "1",
            "Home",
            "9876543210",
            "9876543210",
            "123, Main Street",
            "Apt 101",
            "Near School",
            "123456",
            "Maharashtra",
            "Mumbai",
            "India",
            isDefault = true,
            isSelected = false
            )
        )
        _addresses.add(Address(
            "2",
            "Home",
            "9876543210",
            "9876543210",
            "123, Main Street",
            "Apt 101",
            "Near School",
            "123456",
            "Maharashtra",
            "Mumbai",
            "India",
            isDefault = true,
            isSelected = true
        )
        )
    }

    fun fetchMyAddresses(context: Context){
        _addressState.value = AddressState.Loading

        val url = "${context.getString(R.string.homeUrl)}api/v1/user/address"

        Log.i("address volley token", AppDataSingleton.getAuthToken)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            url,
            null,
            Response.Listener{
                try {

                    Log.i("fetch address response", it.toString())

                    val success = it.getBoolean("success")

                    if (success){

                        _addresses.clear()

                        val data = it.getJSONArray("addresses")

                        if (data.length() > 0){
                            for (i in 0 until data.length()) {
                                val addressObj = data.getJSONObject(i)
                                val address = Address(
                                    addressObj.optString("addressId"),
                                    addressObj.optString("name"),
                                    addressObj.optString("phoneNumber"),
                                    addressObj.optString("alternatePhoneNumber"),
                                    addressObj.optString("addressLine1"),
                                    addressObj.optString("addressLine2"),
                                    addressObj.optString("landmark"),
                                    addressObj.optString("postalCode"),
                                    addressObj.optString("state"),
                                    addressObj.optString("city"),
                                    addressObj.optString("country"),
                                    addressObj.getBoolean("isDefault"),
                                    false
                                )
                                _addresses.add(address)
                            }
                            _addressState.value = AddressState.Home
                        }
                        else{
                            _addressState.value = AddressState.NoAddress
                        }

                        Log.i("addresses", _addresses.toString())
                        _addressState.value = AddressState.Home

                    }
                    else{
                        _addressState.value = AddressState.NoAddress
                        val message = it.getString("message")
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }

                }
                catch (e: Exception){
                    _addressState.value = AddressState.NoAddress
                    Log.e("fetch address", e.toString())
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            },
            Response.ErrorListener{
                _addressState.value = AddressState.NoAddress
                val response = VolleySingleton.extractVolleyErrorResponseBody(it)
                Log.e("volley address", response.toString())
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        VolleySingleton.getInstance(context).requestQueue.add(jsonObjectRequest)

    }

    fun addAddress(context: Context, address: Address, callback: () -> Unit = {}){

        _addressState.value = AddressState.Loading

        val url = "${context.getString(R.string.homeUrl)}api/v1/user/address"

        val jsonObj = JSONObject()
        jsonObj.put("name", address.name)
        jsonObj.put("phoneNumber", address.phoneNumber)
        jsonObj.put("alternatePhoneNumber", address.alternatePhoneNumber)
        jsonObj.put("addressLine1", address.addressLine1)
        jsonObj.put("addressLine2", address.addressLine2)
        jsonObj.put("landmark", address.landmark)
        jsonObj.put("postalCode", address.postalCode)
        jsonObj.put("state", address.state)
        jsonObj.put("city", address.city)
        jsonObj.put("country", address.country)
        jsonObj.put("isDefault", address.isDefault)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonObj,
            Response.Listener{
                try {
                    val success = it.getBoolean("success")

                    if (success) {

                        _addresses.add(address)
                        _addressState.value = AddressState.AddressSaved
                    }
                    else{
                        _addressState.value = AddressState.AddressNotSaved
                    }
                }
                catch (e: Exception){
                    _addressState.value = AddressState.AddressNotSaved
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    Log.e("add address error", e.toString())
                }
            },
            Response.ErrorListener{
                _addressState.value = AddressState.AddressNotSaved
                val response = VolleySingleton.extractVolleyErrorResponseBody(it)
                Log.e("volley address", response.toString())
                Toast.makeText(context, response.toString(), Toast.LENGTH_SHORT).show()
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                headers["auth-token"] = AppDataSingleton.getAuthToken
                return headers
            }
        }

        VolleySingleton.getInstance(context).requestQueue.add(jsonObjectRequest)
    }

    fun fetchCityAndStateFromPincode(context: Context, pincode: String, callback: (String, String) -> Unit){

        if (pincode.toInt() !in 100000..999999){
            Toast.makeText(context, "Invalid pincode", Toast.LENGTH_SHORT).show()
        }

        val url = "https://api.postalpincode.in/pincode/$pincode"

        Log.i("url", url)

        val jsonRequest = object : JsonArrayRequest(
            Method.GET, url, null,
            Response.Listener {
                try {

                    val responseObject = it.getJSONObject(0)
                    Log.i("responseDaArray", responseObject.toString())
                    val success = responseObject.getString("Status") == "Success"

                    Log.i("Success", success.toString())

                    if (success){

                        val pincodeObject = responseObject.getJSONArray("PostOffice").getJSONObject(0)
                        val state = pincodeObject.getString("State")
                        val city = pincodeObject.getString("District")
                        callback(city, state)

                    }
                    else{
                        callback("", "")
                    }

                } catch (e: Exception){
                    Log.e("fetchCityAndStateFromPincode", e.toString())
                }

            }, Response.ErrorListener {

                val response = VolleySingleton.extractVolleyErrorResponseBody(it)
                Log.e("fetchCityAndStateFromPincode", response.toString())


            }){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        VolleySingleton.getInstance(context).requestQueue.add(jsonRequest)

    }

    fun getAddresses(): List<Address>{
        return _addresses
    }

}
