package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.your_travel_friend.directionHelpers.TaskLoadedCallback
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase


class Travelling : AppCompatActivity(), OnMapReadyCallback, TaskLoadedCallback {
    private lateinit var mapView: MapView

    private lateinit var map: GoogleMap
    private var MAP_VIEW_BUNDLE_KEY = "mapViewBundleKey"
    private val defaultZoom = 16f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var travellingDistance = ""

    private var origin_lat = 0.0
    private var orign_lng = 0.0
    private var currentPolyline: Polyline? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travelling)
        title = "travelling"

        mapView = findViewById(R.id.travellingMapView)
        val passengerCard = findViewById<LinearLayout>(R.id.driver_details)
        passengerCard.visibility = View.GONE

        var mapViewBundle: Bundle? = null

        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
        }
        mapView.onCreate(mapViewBundle)
        mapView.getMapAsync(this)

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
        map.isMyLocationEnabled = true

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((this))

        getCurrentLocation()
        val destinationLatitude = intent.extras!!.getDouble("latitude")
        val destinationLongitude = intent.extras!!.getDouble("longitude")

        val dest_latLng = LatLng(destinationLatitude, destinationLongitude)

        val destinationMarker = MarkerOptions()
            .position(dest_latLng)
        destinationMarker.title("my destination")
        map.addMarker(destinationMarker)
        getrequestFromFirebase()
        getMyLocation()
        val url = getUrl(LatLng(origin_lat, orign_lng), dest_latLng, "driving")
        val resultDistance = FloatArray(10)

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

    fun getMyLocation(){
        val rb = FirebaseDatabase.getInstance().getReference("drivers")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
//                    moveCamera(LatLng(location.latitude,location.longitude), defaultZoom)
                    Log.d("locationChanged","my location is changed")
                    origin_lat = location.latitude
                    orign_lng = location.longitude
                    rb.child("originLat").setValue(origin_lat.toString())
                    rb.child("originLng").setValue(orign_lng.toString())
                }
                super.onLocationResult(locationResult)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10_000
            fastestInterval = 5_000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }


    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((this))
        try {
            val location = fusedLocationProviderClient!!.lastLocation
            location.addOnCompleteListener { loc ->
                run {
                    try {
                        val currentLocation = loc.result
                        if (currentLocation != null) {
                            moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude),
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


    private fun getUrl(origin: LatLng, dest: LatLng, directionMode: String): String? {
        // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Mode
        val mode = "mode=$directionMode"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyDkMQZjs8Hxxjt0uL8X0xoCHVi5UYscvVU"
    }
    val passengerData = hashMapOf<String,String>()
    fun getrequestFromFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.database
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    passengerData[it.key.toString()] = it.value.toString()
                }
                Log.d("dataChange","data = ${passengerData.toString()}")
                if(passengerData["userName"] != null && passengerData["destinationName"] != null && passengerData["acceptedRide"] == ""){
                    Log.d("dataChange","card pop up")
                    openPopUp(passengerData["userName"]!!,passengerData["destinationName"]!!,db.getReference("requests").child(currentUserId),passengerData)

                }
                if(passengerData["originLat"] != null && passengerData["originLng"] != null && passengerData["destLat"] != null && passengerData["destLng"] != null && passengerData["acceptedRide"] == "true"){
                    setPassengerLocation(passengerData)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("dataChange","error getting data")
            }

        }
        val ref = db.getReference("requests").child(currentUserId)
        ref.addValueEventListener(childEventListener)
    }

    var rideAccepted = false
    override fun onTaskDone(vararg values: Any?) {
        currentPolyline?.remove()
        Log.d("addPolyline", "onTaskDone: adding polyline")
        currentPolyline = map.addPolyline(values[0] as PolylineOptions)
    }

    fun openPopUp(
        username: String,
        destination: String,
        db: DatabaseReference,
        passengerData: HashMap<String, String>
    ){
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.activity_passenger_pop_card, null)
        popupView.findViewById<TextView>(R.id.passengerName).text = username
        popupView.findViewById<TextView>(R.id.passengerDestination).text = destination
        popupView.findViewById<TextView>(R.id.rideFare).text = "ride fare : ₹${passengerData["rideFare"].toString()}"
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val view =  mapView.rootView
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.setElevation(20.0f)

        try {
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            popupView.setOnTouchListener { _, _ ->
                rideAccepted = false
                passengerData["acceptedRide"] = "false"
                Log.d("ride","not accepted")
                db.setValue(passengerData)
                popupWindow.dismiss()
                true
            }
            popupView.findViewById<Button>(R.id.passengerAccept).setOnClickListener {
                rideAccepted = true
                passengerData["acceptedRide"] = "true"
                Log.d("ride","accepted")
                db.setValue(passengerData)
                setPassengerLocation(passengerData)
                setPassengerDetails(passengerData["rideFare"].toString())
                popupWindow.dismiss()
            }
            popupView.findViewById<Button>(R.id.passengerDecline).setOnClickListener {
                rideAccepted = false
                passengerData["acceptedRide"] = "false"
                Log.d("ride","not accepted")
                db.setValue(passengerData)
                popupWindow.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setPassengerLocation(passengerData: HashMap<String, String>) {
        val passengerLatitude = passengerData["originLat"]
        val passengerLongitude = passengerData["originLng"]
        var pDestinationLatitude = passengerData["destLat"]!!
        var pDestinationLongitude = passengerData["destLng"]!!

        val passengerMarker = MarkerOptions()
            .position(LatLng(passengerLatitude!!.toDouble(),passengerLongitude!!.toDouble()))
            .title("passenger Location")
            .icon(bitmapFromVector(this,R.drawable.passenger_location))
        val passengerDestinationMarker = MarkerOptions()
            .position(LatLng(pDestinationLatitude.toDouble(),pDestinationLongitude.toDouble()))
            .title("passenger Location")
            .icon(bitmapFromVector(this,R.drawable.passenger_dest))
        if(map != null){
            map.addMarker(passengerMarker)
            map.addMarker(passengerDestinationMarker)
        }
    }

    fun setPassengerDetails(fareprice: String) {

        val passengerCard = findViewById<LinearLayout>(R.id.driver_details)
        val passengerName = findViewById<TextView>(R.id.username)
        val passengerPhoneNumber = findViewById<TextView>(R.id.driver_phone_number)
        val isVaccinated = findViewById<TextView>(R.id.is_vaccinated_driver)
        val passengerAddress = findViewById<TextView>(R.id.user_address)
        val passengerFare = findViewById<TextView>(R.id.passenger_fare)
        passengerFare.text = "₹$fareprice"
        Log.d("passengerDetails","${passengerData.toString()}")
        passengerName.text = passengerData["userName"]
        passengerAddress.text ="passenger destination: "+ passengerData["destinationName"]
        val Fb = FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener(object: OnSuccessListener<QuerySnapshot>{
            override fun onSuccess(documentSnapshots: QuerySnapshot?) {
                if (documentSnapshots != null) {
                    if(!documentSnapshots.isEmpty){
                        for (document in documentSnapshots){
                            passengerPhoneNumber.text = document["PhoneNumber"].toString()
                            if(document["isVaccinated"].toString() == "true"){
                                isVaccinated.text = "vaccinated"
                            }else{
                                isVaccinated.text = "not vaccinated"
                            }
                        }
                    }

                }
            }
        })
//
        Log.d("passengerDetails","${passengerAddress.text}")
        passengerCard.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.database
        val ref = db.getReference("drivers").child(currentUserId)

            ref.removeValue().addOnSuccessListener {
                Toast.makeText(this, "removed travelling destination", Toast.LENGTH_SHORT).show()
            }
        }
}