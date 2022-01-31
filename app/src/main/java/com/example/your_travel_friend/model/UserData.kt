package com.example.your_travel_friend.model

import java.util.HashMap

class UserData(private val userId:String, private val userName: String, private val userPhoneNumber: String, private val licencePlateNumber: String, private val vehicleModel: String, private val isVaccinated: Boolean) {
    private val driverRating = 0.0
    private val passengerRating = 0.0

    fun geUserData(): HashMap<String, String>{

        return hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "PhoneNumber" to userPhoneNumber,
            "licencePlateNumber" to licencePlateNumber,
            "vehicleModel" to vehicleModel,
            "isVaccinated" to isVaccinated.toString(),
            "driverRating" to driverRating.toString(),
            "passengerRating" to passengerRating.toString()
        )
    }

}