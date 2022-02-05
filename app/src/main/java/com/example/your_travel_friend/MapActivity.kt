package com.example.your_travel_friend

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.your_travel_friend.menuActivities.BookRideActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.slider.Slider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class MapActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnCameraIdleListener,GoogleMap.OnCameraMoveListener {

    private var destinationName: String = ""
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
        setTitle("choose your location")
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
        val fusedLocationProviderClient by lazy {
            LocationServices.getFusedLocationProviderClient(this)
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult ?: return
                for (location in locationResult.locations){
                    //moveCamera(LatLng(location.latitude,location.longitude), defaultZoom)
                    Log.d("locationChanged","my location is changed")
                    origin_lat = location.latitude
                    origin_lng = location.longitude
                }
                super.onLocationResult(locationResult)
            }
        }

        val locationRequest = LocationRequest.create().apply {
            interval = 10_000
            fastestInterval = 5_000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
            initSearchLocation()
        }
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

//        LocationServices.getFusedLocationProviderClient(this)
//            .requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        setDestinationBtn.setOnClickListener {
            run {

                if (code == 1) {
                    addDestinationToFirebase()
                    val travellingIntent = Intent(this, Travelling::class.java)
                    travellingIntent.putExtra("destination", addressTextView.text)
                    if (destinationLatitude != null && destinationLongitude != null) {
                        travellingIntent.putExtra("latitude", destinationLatitude!!)
                        travellingIntent.putExtra("longitude", destinationLongitude!!)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                        openFarePopupView(travellingIntent)
                    }
                } else if (code == 2) {
                    val travellerIntent = Intent(this, Traveller::class.java)
                    travellerIntent.putExtra("destination", addressTextView.text)
                    if (destinationLatitude != null && destinationLongitude != null) {
                        travellerIntent.putExtra("latitude", destinationLatitude!!)
                        travellerIntent.putExtra("longitude", destinationLongitude!!)
                        travellerIntent.putExtra("myLatitude", origin_lat)
                        travellerIntent.putExtra("myLongitude", origin_lng)
                        travellerIntent.putExtra("destination", destinationName)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                        startActivity(travellerIntent)
                    }
                }else if (code == 3) {
                    val travellerIntent = Intent(this, BookRideActivity::class.java)
                    travellerIntent.putExtra("destination", addressTextView.text)
                    if (destinationLatitude != null && destinationLongitude != null) {
                        travellerIntent.putExtra("latitude", destinationLatitude!!)
                        travellerIntent.putExtra("longitude", destinationLongitude!!)
                        travellerIntent.putExtra("myLatitude", origin_lat)
                        travellerIntent.putExtra("myLongitude", origin_lng)
                        travellerIntent.putExtra("destination", destinationName)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                        startActivity(travellerIntent)
                        finish()
                    }
                }
            }
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun initSearchLocation() {

        addressTextView.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || event.action == KeyEvent.ACTION_DOWN || event.action == KeyEvent.KEYCODE_ENTER){
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                cameraMoved = true
                geoLocate()
                true
            } else {
                false
            }
        }
    }

    private fun geoLocate() {
        Log.d("geoLocating" ,"geolocate started")
        val searchString = addressTextView.text.toString()
        val geocoder = Geocoder(this)
        var list = ArrayList<Address>()
        try {
            list = geocoder.getFromLocationName(searchString,1) as ArrayList<Address>
        }catch (e: IOException){
            Log.d("geoLocating","location for address cannot be find")
            Toast.makeText(this,"can't find the searched location",Toast.LENGTH_SHORT).show()
        }
        if(list.size >0){
            val geoLocateAddress = list.get(0)
            moveCamera(LatLng(geoLocateAddress.latitude,geoLocateAddress.longitude),defaultZoom)
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
                    Log.d("lastLocation", "location recived")
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
            map.setOnCameraMoveListener(this)

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
            location.addOnSuccessListener { loc ->
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
                            val addresses = geocoder.getFromLocation(origin_lat, origin_lng, 1)
                            origin_address =
                                addresses[0].getAddressLine(0) + ", " + addresses[0].getAddressLine(
                                    1
                                )
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

    override fun onLocationChanged(p0: Location) {
        Log.d("locationChanged","location is changed lat: ${p0.latitude}, longitude: ${p0.longitude}")
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        super.onLocationChanged(locations)
        Log.d("locationChanged","location is changed lat: ${locations[0].latitude}, longitude: ${locations[0].longitude}")

    }


    private fun setAddress(address: Address) {
        Log.d("set_address", "setAddress: address : $address")
        Log.d("set_address", "setAddress: address : $address")
        if (address != null) {
            if (address.getAddressLine(0) != null) {
                addressTextView.text = SpannableStringBuilder(address.getAddressLine(0))
                destinationName = address.getAddressLine(0)
            }
            if (address.getAddressLine(1) != null) {
                addressTextView.text =
                    SpannableStringBuilder("${addressTextView.text}, ${address.getAddressLine(1)}")
            }
        }
    }

    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null
    private var cameraMoved = false
    override fun onCameraIdle() {
        if (cameraMoved) {
            Log.d("map_camera", "onCameraMove: camera idle")
            var addresses: List<Address>? = null
            val geocoder = Geocoder(this, Locale.getDefault())
            destinationLatitude = map.cameraPosition.target.latitude
            destinationLongitude = map.cameraPosition.target.longitude

            try {
                addresses = geocoder.getFromLocation(destinationLatitude!!, destinationLongitude!!, 1)
                setAddress(addresses[0])
                Log.d("map_camera", "onCameraMove: sent address")
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun addDestinationToFirebase() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = Firebase.database
        val ref = db.getReference("drivers").child(currentUserId)

        if(destinationLatitude != null && destinationLongitude != null){
            val destinationData = hashMapOf<String, String>(
                "originLat" to origin_lat.toString(),
                "originLng" to origin_lng.toString(),
                "destLat" to destinationLatitude.toString(),
                "destLng" to destinationLongitude.toString(),
                "origin" to origin_address,
                "destination" to addressTextView.text.toString(),
                "fare" to "0"
            )
            ref.setValue(destinationData).addOnSuccessListener {
                Toast.makeText(this, "added destination data to server", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCameraMove() {
        cameraMoved = true
        Log.d("camera_listener", "onCameraMove: camera moved")
    }
    val fareAmount = 0.0
    @SuppressLint("ClickableViewAccessibility")
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
        var fareAmount = 17.0
        val amount = popupView.findViewById<TextView>(R.id.money_text)
        val sideText = popupView.findViewById<TextView>(R.id.per_text)
        val slider = popupView.findViewById<Slider>(R.id.fare_slider)
        val okBtn = popupView.findViewById<Button>(R.id.ok_btn)
        slider.addOnChangeListener { slider, value, fromUser ->
            run {
                if(value != 0.0f){
                    fareAmount = ((value * 10.0).roundToInt() / 10.0)
                    amount.text = fareAmount.toString()
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
            ref.setValue(fareAmount.toString()).addOnSuccessListener {
                Log.d("fareChange","travel fare added to ")
            }
            startActivity(intent)
            popupWindow.dismiss()
        }

    }


}