package com.example.your_travel_friend.adpters

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.your_travel_friend.R
import com.example.your_travel_friend.model.UserData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class DriversListView(val context: Activity, val destinationName: String,val userName:String,val myLocation: LatLng ,val myDestination: LatLng,val driversList: MutableList<UserData>): ArrayAdapter<UserData>(context,
    R.layout.driver_details) {
    lateinit var  view: View

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        view = convertView ?: LayoutInflater.from(parent.context).inflate(R.layout.driver_details, parent, false)
        val myCurrentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val cUser = driversList[position]
//        val profileImg: ImageView = view.findViewById(R.id.driverImage)
        val driverName: TextView = view.findViewById(R.id.driver_name)
        val arivalTime: TextView = view.findViewById(R.id.arival_time)
        val rating: TextView = view.findViewById(R.id.rating)
        val requestBtn: TextView = view.findViewById(R.id.request_button)
        val vehicleImg: ImageView = view.findViewById(R.id.vehicle_logo)
        if(position == 1){
            arivalTime.text = "9:50 AM"
            rating.text = "3.9(45)"
        }else if(position == 2){
            arivalTime.text = "9:54 AM"
            rating.text = "4.3(64)"
        }
//        Glide.with(this).load(currentUser!!.photoUrl).into(profilePic)
        driverName.text = cUser.getUserData()["userName"]
        requestBtn.setOnClickListener {
            val db = Firebase.database
            val ref = db.getReference("requests").child(cUser.getUserData()["userId"]!!)
            val passegerDestinationData = hashMapOf<String, String>(
                "passengerId" to myCurrentUserId,
                "userName" to userName,
                "originLat" to myLocation.latitude.toString(),
                "originLng" to myLocation.longitude.toString(),
                "destLat" to myDestination.latitude.toString(),
                "destLng" to myDestination.longitude.toString(),
                "destinationName" to destinationName,
                "acceptedRide" to ""
            )
            ref.setValue(passegerDestinationData).addOnSuccessListener {
                Toast.makeText(context, "requested to driver", Toast.LENGTH_SHORT).show()
                checkForRideAcceptedOrNot(cUser.getUserData().get("userId") as String)
            }
        }
        return view
    }

    override fun getCount(): Int {
        return driversList.size
    }

    fun checkForRideAcceptedOrNot(driverUserId: String) {
        val db = Firebase.database
        val passengerData = hashMapOf<String,String>()
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    passengerData[it.key.toString()] = it.value.toString()
                }
                if(passengerData["acceptedRide"] != ""){
                    Log.d("driver_details", "onDataChange: ride accepted = ${passengerData["acceptedRide"] as String}")
                    val rideAccepted = (passengerData["acceptedRide"] as String).toBoolean()
                    if(rideAccepted){
                        Toast.makeText(context, "ride is accepted", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(context, "ride is not accepted", Toast.LENGTH_SHORT).show()

                    }
                }



            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dataChange","error getting data")
            }

        }
        val ref = db.getReference("requests").child(driverUserId)
        ref.addValueEventListener(childEventListener)
    }
}