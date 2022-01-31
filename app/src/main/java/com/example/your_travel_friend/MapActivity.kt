package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.*


class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnCameraIdleListener {

    private var origin_address: String = ""
    private lateinit var mapView: MapView
    private lateinit var addressTextView: EditText
    private lateinit var setDestinationBtn: Button

    private lateinit var map: GoogleMap
    private var MAP_VIEW_BUNDLE_KEY = "mapViewBundleKey"
    private val defaultZoom = 15f
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

    private var code: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        mapView = findViewById(R.id.map_view)
        addressTextView = findViewById(R.id.addressTextView)
        setDestinationBtn = findViewById(R.id.confirm_destinationBtn)

        code = intent.extras?.getInt("code")
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
        getLocationPermission()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ){
            getCurrentLocation()
        }
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLocationCallback, mainLooper)

        setDestinationBtn.setOnClickListener{
            run {

                if(code == 1){
                    addDestinationToFirebase()
                    val travellingIntent = Intent(this, Travelling::class.java)
                    travellingIntent.putExtra("destination",addressTextView.text)
                    if(destinationLatitude != null && destinationLongitude != null){
                        travellingIntent.putExtra("latitude", destinationLatitude!!)
                        travellingIntent.putExtra("longitude", destinationLongitude!!)
                        startActivity(travellingIntent)
                    }
                }else if(code == 2){
                    val travellerIntent = Intent(this, Traveller::class.java)
                    travellerIntent.putExtra("destination",addressTextView.text)
                    if(destinationLatitude != null && destinationLongitude != null){
                        travellerIntent.putExtra("latitude", destinationLatitude!!)
                        travellerIntent.putExtra("longitude", destinationLongitude!!)
                        startActivity(travellerIntent)
                    }
                }
            }
        }

    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
            } else {
                Log.i("Permission: ", "Denied")
            }
        }
    val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {

            for (location in locationResult.locations) {
                if (location != null) {
                    Log.d("lastLocation","location recived")
                }
            }
        }
    }
    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        } else {


            map.isMyLocationEnabled = false
//            getCurrentLocation()
            map.setOnCameraIdleListener(this)

        }
    }

    var origin_lat = 0.0
    var origin_lng = 0.0

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            val location = fusedLocationProviderClient!!.lastLocation
            Log.d("test_location", "current location result = $location")
            location.addOnSuccessListener {
                loc ->
                run {
                    try {
                        val currentLocation = loc
                        Log.d("test_location", "current location result = $currentLocation")
                        if (currentLocation != null) {
                            Log.d("test_location", "current location result")
                            origin_lat = currentLocation.latitude
                            origin_lng = currentLocation.longitude
                            moveCamera(
                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                defaultZoom
                            )
                            val geocoder = Geocoder(this, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(origin_lat,origin_lng,1)
                            origin_address = addresses[0].getAddressLine(0) + ", " + addresses[0].getAddressLine(1)
                            fusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
                        } else {
                            getLocationPermission()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "location permission is not granted",
                            Toast.LENGTH_SHORT
                        ).show()
                        getLocationPermission()
                    }
                }
            }
            location.addOnFailureListener {
                Log.d("test_location", "cannot get current location")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onLocationChanged(location: Location) {
        Log.d("set_address", "location changed")
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (e: Exception) {
        }
        setAddress(addresses!![0])
    }

    private fun setAddress(address: Address) {
        Log.d("set_address", "setAddress: address : ${address.toString()}")
        if (address != null) {
            if (address.getAddressLine(0) != null) {
                addressTextView.text = SpannableStringBuilder(address.getAddressLine(0))
            }
            if (address.getAddressLine(1) != null) {
                addressTextView.text = SpannableStringBuilder("${addressTextView.text}, ${address.getAddressLine(1)}")
            }
        }
    }

    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null
    override fun onCameraIdle() {
        Log.d("map_camera", "onCameraMove: camera idle")
        var addresses:  List<Address>? = null
        val geocoder = Geocoder(this,Locale.getDefault())
        destinationLatitude = map.cameraPosition.target.latitude
        destinationLongitude = map.cameraPosition.target.longitude

        try {
            addresses = geocoder.getFromLocation(destinationLatitude!!,destinationLongitude!!,1)
            setAddress(addresses[0])
            Log.d("map_camera", "onCameraMove: sent address")
        }catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun addDestinationToFirebase(){
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.database
        val ref = db.getReference("drivers").child(currentUserId)

        val destinationData = hashMapOf<String,String>(
            "originLat" to origin_lat.toString(),
            "originLng" to origin_lng.toString(),
            "destLat" to destinationLatitude.toString(),
            "destLng" to destinationLongitude.toString(),
            "origin" to origin_address,
            "destination" to addressTextView.text.toString()
        )

        ref.setValue(destinationData).addOnSuccessListener {
            Toast.makeText(this, "added destination data to server", Toast.LENGTH_SHORT).show()
        }
    }


}