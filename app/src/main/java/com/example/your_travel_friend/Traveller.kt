package com.example.your_travel_friend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class Traveller : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traveller)
        title = "traveller"
    }
}