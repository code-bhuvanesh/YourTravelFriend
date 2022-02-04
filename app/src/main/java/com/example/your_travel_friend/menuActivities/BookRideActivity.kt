package com.example.your_travel_friend.menuActivities

import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.example.your_travel_friend.R
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import org.w3c.dom.Text

class BookRideActivity : AppCompatActivity() {
    
    var destinationLongitude: String? = null
    var destinationLatitude: String? = null
    var originLatitude: String? = null
    var originLongitude: String? = null
    var destination: String? = null
    var startHrs = "0"
    var startmins = "0"
    var endHrs = "0"
    var endmins = "0"
    var startSelected = false
    var endSelected = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_ride)
        destinationLatitude = intent.extras!!.getDouble("latitude").toString()
        destinationLongitude = intent.extras!!.getDouble("longitude").toString()
        originLatitude = intent.extras!!.getDouble("myLatitude").toString()
        originLongitude = intent.extras!!.getDouble("myLongitude").toString()
        destination = intent.extras!!.getString("destination")
        var yourLocationText = findViewById<TextView>(R.id.pick_location)
        val destinatinText = findViewById<TextView>(R.id.drop_location)
        val startTime = findViewById<CardView>(R.id.set_startTime)
        val endTime = findViewById<CardView>(R.id.set_endTime)
        val startTimeText = findViewById<TextView>(R.id.startTimeText)
        val endTimeText = findViewById<TextView>(R.id.endTimeText)
        val bookRide = findViewById<Button>(R.id.book_ride)
        destinatinText.text = destination

        startTime.setOnClickListener{
            clickTimePicker(startTimeText,1)
        }
        endTime.setOnClickListener{
            clickTimePicker(endTimeText,2)
        }
        bookRide.setOnClickListener {
            if(startSelected && endSelected){
                var db = FirebaseDatabase.getInstance().reference.child("bookRequests")
                var myUserId = FirebaseAuth.getInstance().currentUser!!.uid
                val bookRequestMap = hashMapOf<String,String>(
                    "passengerId" to myUserId,
                    "passengerOriginLat" to originLatitude.toString(),
                    "passengerOriginLng" to originLongitude.toString(),
                    "passengerDestLat" to destinationLatitude.toString(),
                    "passengerDestLng" to destinationLongitude.toString(),
                    "destinationAddress" to destination!!,
                    "startHrs" to startHrs,
                    "startmins" to startmins,
                    "endHrs" to endHrs,
                    "endmins" to endmins,
                )
                db.child(myUserId).setValue(bookRequestMap)
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/driver_near_you")
                Toast.makeText(this,"your ride is booked",Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun clickTimePicker(textView: TextView,check: Int) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR)
        val minute = c.get(Calendar.MINUTE)


        val tpd = TimePickerDialog(this,TimePickerDialog.OnTimeSetListener(function = { view, h, m ->
            if(check == 1){
                if(h>12){
                    startHrs = h.toString()
                    startmins = m.toString()
                    textView.text = "${h-12} : $m PM"
                }else{
                    startHrs = h.toString()
                    startmins = m.toString()
                    textView.text = "$h : $m AM"
                    if(h == 0){
                        textView.text = "12 : $m AM"

                    }
                }
                startSelected = true
            }
            if(check == 2){
                if(h>12){
                    endHrs = h.toString()
                    endmins = m.toString()
                    textView.text = "${h-12} : $m PM"
                }else{
                    endHrs = h.toString()
                    endmins = m.toString()
                    if(h == 0){
                        endHrs = 12.toString()
                    }
                    textView.text = "$h : $m AM"
                }
                endSelected = true
            }

//            Toast.makeText(this, "$h : $m : ", Toast.LENGTH_LONG).show()

        }),hour,minute,false)

        tpd.show()
    }
}