package com.example.your_travel_friend.adpters

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.your_travel_friend.R
import com.example.your_travel_friend.RideAccepted
import com.example.your_travel_friend.model.UserData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.maps.android.SphericalUtil
import java.util.*

class DriversListView(val context: Activity, val destinationName: String,val userName:String,val myLocation: LatLng ,val myDestination: LatLng,val dOrigin: LatLng,val driversList: MutableList<UserData>): ArrayAdapter<UserData>(context,
    R.layout.driver_details) {
    lateinit var view: View
    lateinit var requestBtn: TextView
    var driverId = ""
    var arivalMinute = 0
    var totalRideFare = ""

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        view = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.driver_details, parent, false)
        val myCurrentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val cUser = driversList[position]
        val db = Firebase.database
        driverId = cUser.getUserData()["userId"]!!
//        val profileImg: ImageView = view.findViewById(R.id.driverImage)
        val driverName: TextView = view.findViewById(R.id.driver_name)
        val arivalTime: TextView = view.findViewById(R.id.arival_time)
        val rating: TextView = view.findViewById(R.id.rating)
        val farePrice: TextView = view.findViewById(R.id.fare_price)
        requestBtn = view.findViewById(R.id.request_button)
        val vehicleImg: ImageView = view.findViewById(R.id.vehicle_logo)
//        if(position == 1){
//            arivalTime.text = "9:50 AM"
//            rating.text = "3.9(45)"
//        }else if(position == 2){
//            arivalTime.text = "9:54 AM"
//            rating.text = "4.3(64)"
//        }
//        Glide.with(this).load(currentUser!!.photoUrl).into(profilePic)
        val ref = db.getReference("drivers").child(driverId)
        var driverToPassengerDistance = SphericalUtil.computeDistanceBetween(myLocation, dOrigin)
        var myDistance = SphericalUtil.computeDistanceBetween(myLocation, myDestination)
        ref.child("fare").get().addOnSuccessListener { fare ->
            run {
                val rideFare = fare.value.toString().toDouble()
                if (rideFare != 0.0) {

                    totalRideFare = String.format("%.1f", (myDistance / 1000) * rideFare * 1.2)
                    Log.d("farePrice", "getView: ride fare = $totalRideFare, ${myDistance / 1000}")
                    farePrice.text = "₹$totalRideFare"
                } else {
                    totalRideFare = "0.0"
                    farePrice.text = "free"
                }
            }
        }
        val currenthour: Int = Calendar.getInstance().get(Calendar.HOUR)
        val currentminute: Int = Calendar.getInstance().get(Calendar.MINUTE)
        val timeTaken = (((driverToPassengerDistance / 1000.0) / 20.0) * 60).toInt()
        arivalMinute = timeTaken
        if (arivalMinute < 5) {
            arivalMinute = 5
        }
        if(arivalMinute < 10){
            if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) {
                arivalTime.text = "$currenthour:0${currentminute + arivalMinute} AM"
                Log.d("current time","$currenthour:${currentminute + arivalMinute} AM")
            } else {
                arivalTime.text = "$currenthour:0${currentminute + arivalMinute} PM"
                Log.d("current time","$currenthour:${currentminute + arivalMinute} PM")
            }
        }else{
            if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) {
                arivalTime.text = "$currenthour:${currentminute + arivalMinute} AM"
                Log.d("current time","$currenthour:${currentminute + arivalMinute} AM")
            } else {
                arivalTime.text = "$currenthour:$arivalMinute PM"
                Log.d("current time","$currenthour:${currentminute + arivalMinute} PM")
            }
        }
        Log.d("timeTaken", " = $timeTaken, d = $driverToPassengerDistance")
        if (Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM) {
            arivalTime.text = "$currenthour:${currentminute + arivalMinute} AM"
            Log.d("current time","$currenthour:${currentminute + arivalMinute} AM")
        } else {
            arivalTime.text = "$currenthour:$arivalMinute PM"
            Log.d("current time","$currenthour:${currentminute + arivalMinute} PM")
        }
        Log.d("currentTime", "time: $currenthour:$currentminute")
        driverName.text = cUser.getUserData()["userName"]
        requestBtn.setOnClickListener {

            requestBtn.text = "requested"
            val ref = db.getReference("requests").child(driverId)
            val passegerDestinationData = hashMapOf<String, String>(
                "passengerId" to myCurrentUserId,
                "userName" to userName,
                "originLat" to myLocation.latitude.toString(),
                "originLng" to myLocation.longitude.toString(),
                "destLat" to myDestination.latitude.toString(),
                "destLng" to myDestination.longitude.toString(),
                "destinationName" to destinationName,
                "acceptedRide" to "",
                "rideFare" to totalRideFare
            )

            ref.setValue(passegerDestinationData).addOnSuccessListener {
                Toast.makeText(context, "requested to driver", Toast.LENGTH_SHORT).show()
                checkForRideAcceptedOrNot(cUser.getUserData()["userId"] as String)
            }
        }
        return view
    }

    override fun getCount(): Int {
        return driversList.size
    }

    var checkActivty = 1
    fun checkForRideAcceptedOrNot(driverUserId: String) {
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/driver_near_you")
        val db = Firebase.database
        val passengerData = hashMapOf<String, String>()
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    passengerData[it.key.toString()] = it.value.toString()
                }
                if (passengerData["acceptedRide"] != "") {
                    Log.d(
                        "driver_details",
                        "onDataChange: ride accepted = ${passengerData["acceptedRide"] as String}"
                    )
                    val rideAccepted = (passengerData["acceptedRide"] as String).toBoolean()
                    if (rideAccepted) {
                        if (checkActivty == 1) {
                            Toast.makeText(context, "ride is accepted", Toast.LENGTH_SHORT).show()
                            val rideIntent = Intent(context, RideAccepted::class.java)
                            rideIntent.putExtra("driverId", driverUserId)
                            rideIntent.putExtra("arrivalTime", arivalMinute.toString())
                            Log.d("TAG", "onDataChange: ride fare: $totalRideFare")
                            rideIntent.putExtra("rideFare", totalRideFare)
                            Log.d(
                                "checkActivity",
                                "onDataChange: driver id:$driverUserId no: ${checkActivty++}"
                            )
                            context.startActivity(rideIntent)
                            context.finish()
                        }
                    } else {
                        requestBtn.text = "request"
                        deleteDataBase()
                        Toast.makeText(context, "ride is not accepted", Toast.LENGTH_SHORT).show()

                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dataChange", "error getting data")
            }

        }
        val ref = db.getReference("requests").child(driverUserId)
        ref.addValueEventListener(childEventListener)
    }

    fun deleteDataBase() {
        val db = Firebase.database
        val ref = db.getReference("requests").child(driverId)
        ref.removeValue().addOnSuccessListener {
            ref.removeValue().addOnSuccessListener {
                Log.d("database", "removed request from database")
            }
        }

    }
}