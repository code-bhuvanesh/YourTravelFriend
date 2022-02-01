package com.example.your_travel_friend

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.your_travel_friend.adpters.DriversListView
import com.example.your_travel_friend.model.UserData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil

class Traveller : AppCompatActivity() {
    private val driversList: MutableList<UserData> = mutableListOf()
    private var adapter: DriversListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traveller)
        title = "traveller"
        val myLat = intent.extras!!.getDouble("myLatitude")
        val myLng = intent.extras!!.getDouble("myLongitude")
        val destinationName = intent.extras!!.getString("destination")
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val myDestinationLatitude = intent.extras!!.getDouble("latitude")
        val myDestinationLongitude = intent.extras!!.getDouble("longitude")
        val driverListView = findViewById<ListView>(R.id.driversListView)
        val user = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val rb = FirebaseDatabase.getInstance().getReference("drivers")
        val prograssBar = findViewById<ProgressBar>(R.id.driverPrograssBar)
        prograssBar.visibility = View.VISIBLE

        rb.get().addOnSuccessListener { data ->
            run {
                if (data.exists()) {
                    data.children.forEach { it ->
                        run {
                            if (it.exists()) {
                                val dDestinationLat =
                                    (it.child("destLat").value as String).toDouble()
                                val dDestinationLng =
                                    (it.child("destLng").value as String).toDouble()
                                val myLatLng = LatLng(myDestinationLatitude, myDestinationLongitude)
                                val dLatLng = LatLng(dDestinationLat, dDestinationLng)
                                val distance =
                                    SphericalUtil.computeDistanceBetween(myLatLng, dLatLng)
                                Log.d("check_direction", "distance is = $distance")
                                if (distance <= 1000.0 && it.key != currentUserId) {
                                    Log.d("check_direction", "key = ${it.key}")
                                    val dc = db.collection("users").document(currentUserId).get().addOnSuccessListener (
                                        OnSuccessListener { tResult ->
                                            run {
                                                val currentUserName = tResult["userName"] as String
                                                val documentReference =
                                                    db.collection("users").document(it.key!!).get()
                                                        .addOnSuccessListener(
                                                            OnSuccessListener { doc ->
                                                                run {
                                                                    Log.d(
                                                                        "check_direction",
                                                                        "doc = ${doc.toString()}"
                                                                    )
                                                                    val userName =
                                                                        doc.get("userName") as String
                                                                    val userId: String =
                                                                        doc.get("userId") as String
                                                                    val userPhoneNumber: String =
                                                                        doc.get("PhoneNumber") as String
                                                                    val licencePlateNumber: String =
                                                                        doc.get("vehicleModel") as String
                                                                    val vehicleModel: String =
                                                                        doc.get("vehicleModel") as String
                                                                    val isVaccinated: Boolean =
                                                                        (doc.get("vehicleModel") as String).toBoolean()
                                                                    val driverData = UserData(
                                                                        userId,
                                                                        userName,
                                                                        userPhoneNumber,
                                                                        licencePlateNumber,
                                                                        vehicleModel,
                                                                        isVaccinated
                                                                    )
                                                                    driversList.add(driverData)
                                                                    Log.d(
                                                                        "driver_data",
                                                                        "driver data added"
                                                                    )
                                                                }
                                                                prograssBar.visibility = View.GONE
                                                                adapter = DriversListView(
                                                                    this,
                                                                    destinationName!!,
                                                                    currentUserName,
                                                                    LatLng(myLat, myLng),
                                                                    LatLng(
                                                                        myDestinationLatitude,
                                                                        myDestinationLongitude
                                                                    ),
                                                                    driversList
                                                                )
                                                                if(driversList.size == 0){
                                                                    findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE
                                                                }
                                                                Log.d(
                                                                    "driver_details",
                                                                    "drivers:  $driversList"
                                                                )
                                                                driverListView.adapter = adapter

                                                            })
                                                        .addOnFailureListener {
                                                            Log.d(
                                                                "driver_data",
                                                                "cannot get driver data"
                                                            )
                                                        }
                                            }
                                        }
                                        )

                                } else {
                                    Log.d(
                                        "check_direction",
                                        "location is long lat: $myDestinationLatitude lng: $myDestinationLongitude"
                                    )
                                }
                            }

                        }


                    }

                } else {
                    Log.d("getting_realtime_data", "cannot get the data")
                }
            }
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.deleteDataBase()
    }

}