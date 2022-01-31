package com.example.your_travel_friend

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

//        mAuth.signOut()
        val user = mAuth.currentUser

        if(user != null){
            val mapActivityIntent = Intent(this,SelectType::class.java)
            startActivity(mapActivityIntent)
            finish()
        }else{
            val signInIntent = Intent(this,SignInActivity::class.java)
            startActivity(signInIntent)
            finish()
        }
    }
}