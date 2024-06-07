package com.example.your_travel_friend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

//        mAuth.signOut()
        val user = mAuth.currentUser

        if (user != null) {
            val selectActivityIntent = Intent(this, SelectType::class.java)
            val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
            val db = Firebase.database
            val ref = db.getReference("drivers").child(currentUserId)

            ref.removeValue().addOnSuccessListener {
//                Toast.makeText(this, "removed travelling destination", Toast.LENGTH_SHORT).show()
            }
            startActivity(selectActivityIntent)
            finish()
        } else {
            val signInIntent = Intent(this, SignInActivity::class.java)
            startActivity(signInIntent)
            finish()
        }
    }
}