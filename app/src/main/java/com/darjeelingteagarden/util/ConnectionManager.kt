package com.darjeelingteagarden.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

class ConnectionManager {
    
    fun isOnline(context: Context): Boolean{

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (connectivityManager != null){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

                if (capabilities !=null){

                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }

                }

            } else {

                val activeNetworkInfo = connectivityManager.activeNetworkInfo

                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }

            }
        }

        return false
    }
}