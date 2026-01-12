package com.darjeelingteagarden.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import com.darjeelingteagarden.activity.RegisterActivity
import com.darjeelingteagarden.model.LocationInfo
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LocationPermission {

    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager
    lateinit var locationByNetwork: Location
    lateinit var locationByGps: Location

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    fun checkPermissions(context: Context): Boolean{

        if (
            ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ){
            return true
        }

        return false

    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    fun requestPermission(context: Context){

        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    fun isLocationEnabled(context: Context): Boolean{
        val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))
    }

    fun getLocationByLocationManager(context: Context): LocationInfo?{

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
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(context)
                return null
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
            Toast.makeText(context, "latitude : $latitude, longitude: $longitude", Toast.LENGTH_LONG).show()
            return LocationInfo(currentLocation, latitude, longitude)
        } else {
            currentLocation = locationByNetwork
            latitude = currentLocation!!.latitude
            longitude = currentLocation!!.longitude
            // use latitude and longitude as per your need
            Toast.makeText(context, "latitude : $latitude, longitude: $longitude", Toast.LENGTH_LONG).show()
            return LocationInfo(currentLocation, latitude, longitude)
        }
    }

    fun getCurrentLocation(context: Context): LocationInfo?{

        if (checkPermissions(context)){

            if (isLocationEnabled(context)){

                //get location location manager
                return getLocationByLocationManager(context)

            }
            else{
                //open settings
                Toast.makeText(context, "Location not enabled", Toast.LENGTH_SHORT).show()

                MaterialAlertDialogBuilder(context)
                    .setTitle("Enable Location")
                    .setMessage("Please enable location access. Click OK to open settings and enable location.")
                    .setCancelable(false)
                    .setPositiveButton("OK"){dialogue, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                    .setNegativeButton("Exit"){dialogue, which ->
                        (context as Activity).finish()
                    }
                    .show()

                return null
            }
        }
        else{
            //request permission
            requestPermission(context)
            return null
        }
    }

}