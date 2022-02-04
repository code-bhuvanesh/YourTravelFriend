package com.example.your_travel_friend.menuActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.example.your_travel_friend.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlin.math.log

class MyProfileActivity : AppCompatActivity() {
    lateinit var profileImg: ImageView
    lateinit var mainUserName: TextView
    lateinit var userEmail: TextView
    lateinit var username: TextView
    lateinit var phoneNumber: TextView
    lateinit var licensePlate: TextView
    lateinit var vehicleModel: TextView
    lateinit var ridesGiven: TextView
    lateinit var ridesTaken: TextView
    lateinit var vaccinated: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        profileImg = findViewById(R.id.profile_img)
        mainUserName = findViewById(R.id.profile_name)
        userEmail = findViewById(R.id.profile_email)
        username = findViewById(R.id.profile_username)
        phoneNumber = findViewById(R.id.profile_phoneNumber)
        licensePlate = findViewById(R.id.profile_licensePlate)
        vehicleModel = findViewById(R.id.profile_vehicleModel)
        ridesGiven = findViewById(R.id.profile_ridesGiven)
        ridesTaken = findViewById(R.id.profile_ridesTaken)
        vaccinated = findViewById(R.id.profile_vaccinated)
        val currentUser = FirebaseAuth.getInstance().currentUser
        val currentUserId = currentUser!!.uid
        FirebaseFirestore.getInstance().collection("users").document(currentUserId).get().addOnSuccessListener {
            if(it.exists()){
                Log.d("fetching user","getting user")
                userEmail.text = currentUser.email
                mainUserName.text = it["userName"].toString()
                username.text = it["userName"].toString()
                phoneNumber.text = it["PhoneNumber"].toString()
                licensePlate.text = it["licencePlateNumber"].toString()
                vehicleModel.text = it["vehicleModel"].toString()
                ridesGiven.text = it["passengersRated"].toString()
                ridesTaken.text = it["driversRated"].toString()
                if(it["userName"].toString().equals("true")){
                    vaccinated.text = "vaccinated"
                }else{
                    vaccinated.text = "not vaccinated"
                }
            }
        }

    }
}