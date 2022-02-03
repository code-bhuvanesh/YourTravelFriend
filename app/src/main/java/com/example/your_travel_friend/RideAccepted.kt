package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
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
import com.google.android.material.slider.Slider
import com.google.api.LogDescriptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import kotlin.math.roundToInt


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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_accepted)
        title = "ride"

        mapView = findViewById(R.id.rideMapView)
        driverId
        var mapViewBundle: Bundle? = null
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
                try {
                    var driverLatitude = snapshot.child("originLat").value.toString().toDouble()
                    var driverLongitude = snapshot.child("originLng").value.toString().toDouble()
                    setDriverLocation(driverLatitude,driverLongitude)
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

    fun openFarePopupView(intent: Intent){
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.set_price_layout,null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = false // lets taps outside the popup also dismiss it
        val view =  mapView.rootView
        val popupWindow = PopupWindow(popupView, width, height, focusable)
        popupWindow.setElevation(20.0f)
        val rupeeSymbol = popupView.findViewById<TextView>(R.id.rupeeSymbol)
        val amount = popupView.findViewById<TextView>(R.id.money_text)
        val sideText = popupView.findViewById<TextView>(R.id.per_text)
        val slider = popupView.findViewById<Slider>(R.id.fare_slider)
        val okBtn = popupView.findViewById<Button>(R.id.ok_btn)
        slider.addOnChangeListener { slider, value, fromUser ->
            run {
                if(value != 0.0f){
                    amount.text = ((value * 10.0).roundToInt() / 10.0).toString()
                    rupeeSymbol.text = "₹"
                    sideText.text = "/KM"
                }else{
                    amount.text = "free"
                    rupeeSymbol.text = ""
                    sideText.text = ""
                }
            }
        }

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        popupView.setOnTouchListener { _, _ ->
            popupWindow.dismiss()
            true
        }
        okBtn.setOnClickListener {
            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            val db = Firebase.database
            val ref = db.getReference("drivers").child(currentUserId).child("fare")
            ref.setValue(amount.text).addOnSuccessListener {
                Log.d("fareChange","travel fare added to ")
            }
            startActivity(intent)
            popupWindow.dismiss()
        }

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
}