package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class RideAccepted : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView

    private lateinit var map: GoogleMap
    private var MAP_VIEW_BUNDLE_KEY = "mapViewBundleKey"
    private val defaultZoom = 16f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var travellingDistance = ""

    private var origin_lat = 0.0
    private var orign_lng = 0.0
    private var driverId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_accepted)
        title = "ride"

        mapView = findViewById(R.id.rideMapView)
        driverId
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
        driverId = intent.extras!!.getString("driverId")!!
        map.isMyLocationEnabled = true

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient((this))

        getCurrentLocation()
        getrequestFromFirebase()

        val resultDistance = FloatArray(10)

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

    private fun driverLiveLocation() {
        val db = FirebaseFirestore.getInstance()
        val rb = FirebaseDatabase.getInstance().getReference("drivers")
            .child(driverId)
        val childEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var driverLatitude = snapshot.child("originLat").value.toString().toDouble()
                var driverLongitude = snapshot.child("originLng").value.toString().toDouble()
                setDriverLocation(driverLatitude,driverLongitude)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        rb.addValueEventListener(childEventListener)
    }



}