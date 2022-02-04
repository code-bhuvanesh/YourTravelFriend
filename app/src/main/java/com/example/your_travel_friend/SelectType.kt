package com.example.your_travel_friend

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import com.example.your_travel_friend.menuActivities.*
import com.example.your_travel_friend.pushNotifications.SendNotification
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        menuItemsSelected()
//        actionBar?.setDisplayHomeAsUpEnabled(true)

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
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun menuItemsSelected(){
        findViewById<NavigationView>(R.id.nav_view).setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_profile -> {
                    openProfile()
                }
                R.id.nav_store-> {
                    openStore()
                }
                R.id.nav_Book ->{
                    BookYourRide()
                }
                R.id.nav_reviews-> {
                    openReviews()
                }
                R.id.nav_settings-> {
                    openSettings()
                }
                R.id.nav_about-> {
                    openAboutPage()
                }
                R.id.nav_logout-> {
                    logout()
                }
            }
            true
        }

    }

    private fun BookYourRide(){
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("code", 3)
        startActivity(intent)
    }
    private fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun openAboutPage() {
        val aboutIntent = Intent(this,AboutActivity::class.java)
        startActivity(aboutIntent)
    }

    private fun openSettings() {
        val aboutIntent = Intent(this,SettingsActivity::class.java)
        startActivity(aboutIntent)
    }

    private fun openReviews() {
        val aboutIntent = Intent(this,MyReviewsActivity::class.java)
        startActivity(aboutIntent)
    }

    private fun openStore() {
        val aboutIntent = Intent(this,StoreActivity::class.java)
        startActivity(aboutIntent)
    }

    private fun openProfile() {
        val aboutIntent = Intent(this,MyProfileActivity::class.java)
        startActivity(aboutIntent)
    }


}