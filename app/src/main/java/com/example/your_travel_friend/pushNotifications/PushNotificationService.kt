package com.example.your_travel_friend.pushNotifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.your_travel_friend.MainActivity
import com.example.your_travel_friend.R
import com.example.your_travel_friend.RideAccepted
import com.example.your_travel_friend.Traveller
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class PushNotificationService: FirebaseMessagingService() {


    private val ADMIN_CHANNEL_ID = "admin_channel"

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        val myId = FirebaseAuth.getInstance().currentUser!!.uid
        var intent = Intent(this, MainActivity::class.java)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        if(p0.data!!.get("title").equals("book your ride")){
            Log.d("book_ride", "onMessageReceived: book ride is true")
            intent = Intent(this, Traveller::class.java)


            FirebaseDatabase.getInstance().reference.child("bookRequests").child(myId).get().addOnSuccessListener {
                val myLat = it.child("passengerOriginLat").value.toString().toDouble()
                val myLng = it.child("passengerOriginLng").value.toString().toDouble()
                val destLat = it.child("passengerDestLat").value.toString().toDouble()
                val destLng = it.child("passengerDestLng").value.toString().toDouble()
                intent.putExtra("myLatitude",myLat)
                intent.putExtra("myLongitude",myLng)
                intent.putExtra("latitude",destLat)
                intent.putExtra("longitude",destLng)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setupChannels(notificationManager)
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_MUTABLE
                    )
                } else {
                    PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT
                    )
                }

                val largeIcon = BitmapFactory.decodeResource(
                    resources,
                    R.drawable.app_logo
                )

                val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(largeIcon)
                    .setContentTitle(p0?.data?.get("title"))
                    .setContentText(p0?.data?.get("message"))
                    .setAutoCancel(true)
                    .setSound(notificationSoundUri)
                    .setContentIntent(pendingIntent)

//        //Set notification color to match your app color template
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.color = resources.getColor(R.color.background_dark)
//        }
                notificationManager.notify(notificationID, notificationBuilder.build())

            }


        }else{
            Log.d("book_ride", "onMessageReceived: book ride is false")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setupChannels(notificationManager)
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_MUTABLE
                )
            } else {
                PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT
                )
            }

            val largeIcon = BitmapFactory.decodeResource(
                resources,
                R.drawable.app_logo
            )

            val notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(largeIcon)
                .setContentTitle(p0?.data?.get("title"))
                .setContentText(p0?.data?.get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
//                .setContentIntent(pendingIntent)

//        //Set notification color to match your app color template
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            notificationBuilder.color = resources.getColor(R.color.background_dark)
//        }
            notificationManager.notify(notificationID, notificationBuilder.build())
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "New notification"
        val adminChannelDescription = "Device to device notification"

        val adminChannel: NotificationChannel
        adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }

}