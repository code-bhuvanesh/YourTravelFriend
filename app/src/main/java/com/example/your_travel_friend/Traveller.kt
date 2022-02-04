package com.example.your_travel_friend

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.your_travel_friend.adpters.DriversListView
import com.example.your_travel_friend.model.UserData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import java.lang.NullPointerException

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
        val myOriginLatitude = intent.extras!!.getDouble("myLatitude")
        val myOriginLongitude = intent.extras!!.getDouble("myLongitude")
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
                            try {
                                if (it.exists()) {
                                    val dDestinationLat =
                                        (it.child("destLat").value as String).toDouble()
                                    val dDestinationLng =
                                        (it.child("destLng").value as String).toDouble()
                                    val dOriginLat =
                                        (it.child("originLat").value as String).toDouble()
                                    val dOriginLng =
                                        (it.child("originLng").value as String).toDouble()
                                    val myOriginLatLng = LatLng(myOriginLatitude, myOriginLongitude)
                                    val dOriginLatLng = LatLng(dOriginLat, dOriginLng)
                                    val myDestLatLng = LatLng(myDestinationLatitude, myDestinationLongitude)
                                    val dDestLatLng = LatLng(dDestinationLat, dDestinationLng)
                                    val distance = distanceCheck(myOriginLatLng,myDestLatLng,dOriginLatLng,dDestLatLng)
                                    Log.d("check_direction", "distance is = $distance")
                                    Log.d("check_direction", "key = ${it.key}")
                                    if (distance && it.key != currentUserId) {
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
//                                                                        driverRating = (doc.get("vehicleModel") as String).toBoolean()
                                                                        driversList.add(driverData)
                                                                        Log.d(
                                                                            "driver_data",
                                                                            "driver data added"
                                                                        )
                                                                        prograssBar.visibility = View.GONE

                                                                        adapter = DriversListView(
                                                                            this,
                                                                            destinationName!!,
                                                                            currentUserName,
                                                                            LatLng(myLat, myLng),
                                                                            LatLng(
                                                                                myDestinationLatitude,
                                                                                myDestinationLongitude
                                                                            ),LatLng(dOriginLat,dOriginLng),
                                                                            driversList
                                                                        )
                                                                        if(driversList.size == 0){
                                                                            findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE
                                                                        }else{
                                                                            findViewById<TextView>(R.id.noDrivers).visibility = View.GONE

                                                                        }
                                                                        Log.d(
                                                                            "driver_details",
                                                                            "drivers:  $driversList"
                                                                        )
                                                                        driverListView.adapter = adapter
                                                                    }


                                                                })
                                                            .addOnFailureListener {
                                                                Log.d(
                                                                    "driver_data",
                                                                    "cannot get driver data"
                                                                )
                                                                prograssBar.visibility = View.GONE
                                                                findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE

                                                            }
                                                }
                                            }
                                        )

                                    } else {
                                        if(driversList.size == 0){
                                            prograssBar.visibility = View.GONE
                                            findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE
                                        }


                                        Log.d(
                                            "check_direction",
                                            "location is long lat: $myDestinationLatitude lng: $myDestinationLongitude"
                                        )
                                    }
                            }
                            }catch (e: NullPointerException){
                                if(driversList.size == 0){
                                    prograssBar.visibility = View.GONE
                                    findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE
                                }
                                e.printStackTrace()
                            }

                        }


                    }

                } else {
                    prograssBar.visibility = View.GONE
                    findViewById<TextView>(R.id.noDrivers).visibility = View.VISIBLE
                    Log.d("getting_realtime_data", "cannot get the data")
                }
            }
        }



    }

    private fun distanceCheck(
        myOriginLatLng: LatLng,
        myDestLatLng: LatLng,
        dOriginLatLng: LatLng,
        dDestLatLng: LatLng
    ): Boolean {
        var myDistance = SphericalUtil.computeDistanceBetween(dOriginLatLng,dDestLatLng)
        var myCenterlatlng = computeCentroid(listOf(dOriginLatLng,dDestLatLng))
        var a = SphericalUtil.computeDistanceBetween(myCenterlatlng,myOriginLatLng)
        var b = SphericalUtil.computeDistanceBetween(myCenterlatlng,myDestLatLng)
        var c = SphericalUtil.computeDistanceBetween(myOriginLatLng,dOriginLatLng)
        var d = SphericalUtil.computeDistanceBetween(myDestLatLng,dDestLatLng)
        var result = ((a.toInt() <= myDistance/1.7)
                && (b.toInt() <= myDistance/1.7))
                && ((c.toInt() <= myDistance/2)
                && (d.toInt() <= myDistance/2))
        Log.d("distance","(${a <= myDistance/1.7}: $a <= ${myDistance/1.5}")
        Log.d("distance","(${b <= myDistance/1.7} : $b <= ${myDistance/1.5}")
        Log.d("distance","(${c <= myDistance/2} : $c <= ${myDistance/2}")
        Log.d("distance","(${d <= myDistance/2} : $d <= ${myDistance/2}")
        Log.d("distance","myDestination = ${myOriginLatLng.latitude}, driverDestination = ${dOriginLatLng.latitude}")
        Log.d("distance","myDestination = ${myOriginLatLng.longitude}, driverDestination = ${dOriginLatLng.longitude}")
        Log.d("distance","myDestination = ${myDestLatLng.latitude}, driverDestination = ${dDestLatLng.latitude}")
        Log.d("distance","myDestination = ${myDestLatLng.longitude}, driverDestination = ${dDestLatLng.longitude}")
        return result
    }

    private fun computeCentroid(points: List<LatLng>): LatLng? {
        var latitude = 0.0
        var longitude = 0.0
        val n = points.size
        for (point in points) {
            latitude += point.latitude
            longitude += point.longitude
        }
        return LatLng(latitude / n, longitude / n)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.deleteDataBase()
    }

}