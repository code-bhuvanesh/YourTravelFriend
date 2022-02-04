package com.example.your_travel_friend

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth


class SelectType : AppCompatActivity() {

    private val TRAVELLER_CODE: Int = 546
    private val TRAVELLING_CODE: Int = 543

    lateinit var traveller: CardView
    lateinit var travelling: CardView

    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_type)

        travelling = findViewById(R.id.havingVehicle)
        traveller = findViewById(R.id.notHavingVehicle)

        drawerLayout = findViewById(R.id.my_drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);

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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_profile -> {
                openProfile()
                true
            }
            R.id.nav_store-> {
                openStore()
                true
            }
            R.id.nav_reviews-> {
                openReviews()
                true
            }
            R.id.nav_settings-> {
                openSettings()
                true
            }
            R.id.nav_about-> {
                openAboutPage()
                true
            }
            R.id.nav_logout-> {
                logout()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun openAboutPage() {
        val aboutIntent = Intent()
    }

    private fun openSettings() {
        TODO("Not yet implemented")
    }

    private fun openReviews() {
        TODO("Not yet implemented")
    }

    private fun openStore() {
        TODO("Not yet implemented")
    }

    private fun openProfile() {
        TODO("Not yet implemented")
    }


}