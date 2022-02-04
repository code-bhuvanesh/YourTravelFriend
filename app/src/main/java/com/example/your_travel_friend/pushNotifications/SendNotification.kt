package com.example.your_travel_friend.pushNotifications

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.your_travel_friend.SelectType
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject

class SendNotification(val context: Activity) {
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAMBj5N8k:APA91bEMAXDkzavVh1r6k4MoNZSGqNneVdryHkP6LHyr6yjdpqO965gmFxdB6FVHhyYCKNYzSihPSl7jeKiqqneJ2qqX-eNYZo4cZGcRLjdaYSBxx8ymSukpwsBg0GeR8jRv3v_-wHma"
    private val contentType = "application/json"


    fun initializeNotification(){
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/Enter_your_topic_name")

        val topic = "/topics/driver_near_you" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", "Your ride is waiting")
            notifcationBody.put("message", "your driver is  near you")   //Enter your notification message
            notification.put("to", topic)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotification(notification)
    }
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }

    private fun sendNotification(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(FCM_API, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("TAG", "onResponse: $response")
            },
            Response.ErrorListener {
                Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show()
                Log.i("TAG", "onErrorResponse: Didn't work")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }
}