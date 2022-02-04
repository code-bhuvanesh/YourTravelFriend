package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
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


class RideAccepted : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    private lateinit var map: GoogleMap
    private var MAP_VIEW_BUNDLE_KEY = "mapViewBundleKey"
    private val defaultZoom = 16f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var travellingDistance = ""
    private var driverId = ""
    private var arivalTime = ""
    private var rideFare = ""
    private lateinit var rideStartedBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_accepted)
        title = "ride"

        mapView = findViewById(R.id.rideMapView)
        driverId
        var mapViewBundle: Bundle? = null
        rideStartedBtn = findViewById(R.id.rideStartedBtn)
        driverId = intent.extras?.getString("driverId").toString()
        rideFare = intent.extras?.getString("rideFare").toString()
        arivalTime = intent.extras?.getString("arrivalTime").toString()
        Log.d("ridefare", "onCreate: ride fare : $rideFare")

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)
        setDriverDetails()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapView.onResume()
        map = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        driverId = intent.extras!!.getString("driverId")!!
        map.isMyLocationEnabled = true

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((this))

        getCurrentLocation()
        getrequestFromFirebase()
        checkForEndTrip()

        val resultDistance = FloatArray(10)

    }
    var currentLocation: Location? = null
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((this))
        try {
            val location = fusedLocationProviderClient!!.lastLocation
            location.addOnCompleteListener { loc ->
                run {
                    try {
                        currentLocation = loc.result
                        if (currentLocation != null) {
                            moveCamera(
                                LatLng(currentLocation!!.latitude, currentLocation!!.longitude),
                                defaultZoom
                            )
                        } else {
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "location permission is not granted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    fun getrequestFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.database
        val passengerData = hashMapOf<String,String>()
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    passengerData[it.key.toString()] = it.value.toString()
                }
                Log.d("dataChange","data = ${passengerData.toString()}")
//                setDriverLocation(passengerData["originLat"].toString().toDouble(),passengerData["originLng"].toString().toDouble())
                driverLiveLocation()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dataChange","error getting data")
            }

        }
        val ref = db.getReference("drivers").child(driverId)
        ref.addValueEventListener(childEventListener)
    }

    var driverMarker: MarkerOptions? = null
    private fun setDriverLocation(driverLatitude: Double,driverLongitude: Double) {

        driverMarker = MarkerOptions()
            .position(LatLng(driverLatitude,driverLongitude))
            .title("passenger Location")
            .icon(bitmapFromVector(this,R.drawable.car_pin_icon))
        if(driverMarker != null){
            map.clear()
            map.addMarker(driverMarker!!)
        }
    }

    private fun bitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)

        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )

        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    var rideStarted = false
    private fun driverLiveLocation() {
        val db = FirebaseFirestore.getInstance()
        val rb = FirebaseDatabase.getInstance().getReference("OngoingRide")
            .child(driverId)
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    var driverLatitude = snapshot.child("DOriginLat").value.toString().toDouble()
                    var driverLongitude = snapshot.child("DOriginLng").value.toString().toDouble()
                    setDriverLocation(driverLatitude,driverLongitude)
                    if(currentLocation != null){
                        Log.d("checkCurrentLocation", "onDataChange: currentLocation: ${currentLocation!!.latitude}, ${currentLocation!!.longitude}")
                        val myLatLng = LatLng(currentLocation!!.latitude,currentLocation!!.longitude)
                        val dLatLng = LatLng(driverLatitude,driverLongitude)
                        val disDP = SphericalUtil.computeDistanceBetween(myLatLng,dLatLng)
                        if(disDP < 500){
                            if(!rideStarted){
                                rideStartedBtn.visibility = View.VISIBLE
                                rideStartedBtn.setOnClickListener {
                                    FirebaseDatabase.getInstance().getReference("OngoingRide").child(driverId).child("rideStarted").get().addOnSuccessListener {
                                        Log.d("setRideStarted", "setRideStartedTrue: ride stated is ${it.value}")

                                    }
                                    FirebaseDatabase.getInstance().getReference("OngoingRide").child(driverId).child("rideStarted").setValue("true").addOnSuccessListener {
                                        Log.d("setRideStarted", "setRideStartedTrue: ride stated is true")
                                        rideStarted = true
                                        FirebaseDatabase.getInstance().getReference("OngoingRide").child(driverId).child("rideStarted").get().addOnSuccessListener {
                                            Log.d("setRideStarted", "setRideStartedTrue: ride stated is ${it.value}")
                                        }
                                        rideStartedBtn.visibility = View.GONE
                                    }
                                }
                            }

                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        rb.addValueEventListener(childEventListener)
    }

    fun setDriverDetails(){
        val driverCard = findViewById<LinearLayout>(R.id.driver_details)
        val driverName = findViewById<TextView>(R.id.driver_username)
        val driverPhoneNumber = findViewById<TextView>(R.id.driver_phone_number)
        val isVaccinated = findViewById<TextView>(R.id.is_vaccinated_driver)
        val licensePlate = findViewById<TextView>(R.id.driver_licencePlate)
        val vehicleModel = findViewById<TextView>(R.id.vehicle_model)
        val farePrice = findViewById<TextView>(R.id.my_ridefare)
        if(rideFare != null){
            farePrice.text = "₹$rideFare"
        }
        Log.d("driverId","$driverId")
        FirebaseFirestore.getInstance().collection("users").document(driverId).get()
            .addOnSuccessListener(OnSuccessListener { document ->
                run {
                    driverName.text = document["userName"].toString()
                    driverPhoneNumber.text = document["PhoneNumber"].toString()
                    if (document["isVaccinated"].toString() == "true") {
                        isVaccinated.text = "vaccinated"
                    } else {
                        isVaccinated.text = "not vaccinated"
                    }
                    licensePlate.text = document["licencePlateNumber"].toString()
                    vehicleModel.text = document["vehicleModel"].toString()
                }
            })
    }


    fun checkForEndTrip(){
        val rb = FirebaseDatabase.getInstance().getReference("OngoingRide").child(driverId)
        Log.d("rateDriver","sdfhkl")
        rb.child("rideEnded").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        if(snapshot.value.toString().toBoolean()){
                            rateDriver()
                        }else{
                            Log.d("rateDriver","false")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            }
        )
    }

    private fun rateDriver(){
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.rating_layout, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val view =  mapView.rootView
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.setElevation(20.0f)
        var passengerRating = 0.0
        try {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            popupView.findViewById<TextView>(R.id.rateYourText).text = "rate your driver"
            val rating = popupView.findViewById<RatingBar>(R.id.ratingLayout)
            val rideFareText = popupView.findViewById<TextView>(R.id.rideFareTextPop)
            val okBtn = popupView.findViewById<Button>(R.id.review_ok_btn)
            val db = FirebaseFirestore.getInstance()
            val documentReference = db.collection("users").document(driverId)
            rideFareText.text = "Ride Fare : $rideFare"
            var dCurrentRewards = 0.0
            var passengersRated = 0
            okBtn.setOnClickListener {
                documentReference.get().addOnSuccessListener {
                    if(it.exists()){
                        dCurrentRewards = (it["driverRewards"].toString()).toDouble()
                        passengersRated = (it["passengerRated"].toString()).toInt()
                        Log.d("Ratting","current passenger rewards: $dCurrentRewards")
                        FirebaseDatabase.getInstance().getReference("OngoingRide").child(driverId).child("rideEnded").setValue("true")
                        documentReference.update("driverRewards",(dCurrentRewards+rating.rating).toString())
                        documentReference.update("driverRating",((dCurrentRewards+rating.rating)/(passengersRated+1)).toString())
                        documentReference.update("passengerRated",(passengersRated+1).toString())
                        Log.d("Ratting","updated passenger rewards: ${dCurrentRewards+rating.rating}")

                    }
                }

                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                popupWindow.dismiss()
            }

        }catch(e: Exception){
            e.printStackTrace()
        }
    }
}
