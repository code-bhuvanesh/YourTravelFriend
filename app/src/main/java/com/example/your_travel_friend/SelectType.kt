package com.example.your_travel_friend

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class SelectType : AppCompatActivity() {

    private val TRAVELLER_CODE: Int = 546
    private val TRAVELLING_CODE: Int = 543

    lateinit var traveller: CardView
    lateinit var travelling: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_type)

        travelling = findViewById(R.id.havingVehicle)
        traveller = findViewById(R.id.notHavingVehicle)

        travelling.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("code", 1)
            startActivity(intent)
        }

        traveller.setOnClickListener {

            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("code", 2)
            startActivity(intent)

        }


    }

//    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == TRAVELLER_CODE) {
//            // There are no request codes
//            val data: Intent? = result.data
//            val destination: String? = data?.extras?.get("destination").toString()
//            if(destination != null){
//                Log.d("confirm_destination", "onCreate: destination : $destination")
//                val travellerIntent = Intent(this,Traveller::class.java)
//                startActivity(travellerIntent)
//            }else{
//                Log.d("confirm_destination", "onCreate: destination : destination is null")
//
//            }
//        }
//        if (result.resultCode == TRAVELLING_CODE) {
//            // There are no request codes
//            val data: Intent? = result.data
//
//            if (data != null) {
//                val destination: String = data.extras?.get("destination").toString()
//                val destinationLatitude: Double = data.extras?.get("latitude") as Double
//                val destinationLongitude: Double = data.extras?.get("longitude") as Double
//                Log.d("confirm_destination", "onCreate: destination : $destination")
//                val travellerIntent = Intent(this,Travelling::class.java)
//                travellerIntent.putExtra("latitude", destinationLatitude)
//                travellerIntent.putExtra("longitude", destinationLongitude)
//                startActivity(travellerIntent)
//            }
//        }
//    }


}