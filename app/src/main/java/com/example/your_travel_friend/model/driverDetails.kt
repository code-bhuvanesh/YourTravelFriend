package com.example.your_travel_friend.model

import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class driverDetails(private val driverId: String) {
    private val coursesArrayList: ArrayList<UserData> = ArrayList()
    private var driverData: UserData? = null
    private var driversList: MutableList<UserData> = mutableListOf()
    fun getDetails(): MutableList<UserData>{
        val user = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val documentReference = db.collection("users").document().collection(driverId).get()
            .addOnSuccessListener(
            OnSuccessListener { tResult ->
                run {
                    for (doc in tResult) {
                        driverData = doc.toObject(UserData::class.java)
                        if(driverData != null){
                            driversList.add(driverData!!)
                        }
                    }
                    Log.d("driver_data", driverData?.geUserData()?.get("userName").toString())
                }
            })
            .addOnFailureListener {
                Log.d("driver_data", "cannot get driver data")
            }
        return driversList
    }
}