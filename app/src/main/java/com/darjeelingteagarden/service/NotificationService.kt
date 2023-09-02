package com.darjeelingteagarden.service

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.darjeelingteagarden.R
import com.darjeelingteagarden.activity.NewsActivity
import com.darjeelingteagarden.activity.OrdersForMeActivity
import com.darjeelingteagarden.activity.ProfileActivity
import com.darjeelingteagarden.activity.SampleOrderDetailsActivity
import com.darjeelingteagarden.activity.SampleOrdersForMeActivity
import com.darjeelingteagarden.repository.AppDataSingleton
import com.darjeelingteagarden.repository.NotificationDataSingleton
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.Date

class NotificationService : FirebaseMessagingService() {

    private val channelId = "channel_id"
    val channelName = "darjeelingTeaGarden"

    private lateinit var sharedPreferences: SharedPreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putString("fcm-token", token)
        editor.putBoolean("fcm-token-updated", false)
        editor.apply()

        Log.d("fms token -> ", token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        var activity: String? = null
        var resourceId: String? = null
        var imageUrl: String? = null

        if (message.data.isNotEmpty()){
            activity = message.data["activityToOpen"]
            resourceId = message.data["resourceId"]
            imageUrl = message.notification?.imageUrl.toString()
        }

        message.notification?.let {
            showNotification(it.title.toString(), it.body.toString(), imageUrl, activity, resourceId)
        }

        Log.i("fcm message :: ", message.toString())

    }

    private fun showNotification(title: String, message: String, imageUrl: String?, activity: String?, resourceId: String?){

        if (!activity.isNullOrEmpty()){

            var intent: Intent

            if (activity == "order"){
                intent = Intent(this, SampleOrderDetailsActivity::class.java)
                intent.putExtra("orderId", resourceId)
                intent.putExtra("orderType", "order")
            }
            else if (activity == "sampleOrder"){
                intent = Intent(this, SampleOrderDetailsActivity::class.java)
                intent.putExtra("orderId", resourceId)
                intent.putExtra("orderType", "sampleOrder")
            }
            else if (activity == "ordersForMe"){
                intent = Intent(this, OrdersForMeActivity::class.java)
            }
            else if (activity == "sampleOrdersForMe"){
                intent = Intent(this, SampleOrdersForMeActivity::class.java)
            }
            else if (activity == "news"){
                intent = Intent(this, NewsActivity::class.java)
                NotificationDataSingleton.notificationToOpen = true
                NotificationDataSingleton.resourceId = resourceId
            }
            else{
                intent = Intent(this, ProfileActivity::class.java)
            }

            val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.darjeelingteagardenlogo_low)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
//                .setLargeIcon()

            if (imageUrl != null){
                val imageRequest = ImageRequest(
                    imageUrl,
                    {
                        builder.setStyle(NotificationCompat.BigPictureStyle().bigPicture(it))
                    }, 0, 0, ImageView.ScaleType.CENTER_CROP,  null,
                    {

                    }
                )

                Volley.newRequestQueue(applicationContext).add(imageRequest)
            }

            with(NotificationManagerCompat.from(this)){
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                notify(69, builder.build())
            }

        }





    }

    private fun sendTokenToServer(token: String){

        val authToken = getAuthToken()

        if (authToken != null){

            val queue = Volley.newRequestQueue(applicationContext)

            val url = "${getString(R.string.homeUrl)}api/v1/user/deviceToken"

            val jsonBody = JSONObject()
            jsonBody.put("deviceToken", token)

            val jsonObjectRequest = object: JsonObjectRequest(
                Method.POST,
                url,
                jsonBody,
                Response.Listener {
                    try{

                    }
                    catch (e: Exception){

                    }
                },
                Response.ErrorListener {

                }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-Type"] = "application/json"
                    headers["auth-token"] = authToken
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        }

    }

    private fun getAuthToken(): String?{

        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val authToken = sharedPreferences.getString("token", null).toString()
        val lastLoginTime = sharedPreferences.getLong("date", 0)

        val lastLogin = Date().time - lastLoginTime

        if (isLoggedIn && authToken != "null"){

            if (lastLogin > 1000 * 60 * 60 * 24 * 14){ // 14 days
                return authToken
            }
        }

        return null
    }

}