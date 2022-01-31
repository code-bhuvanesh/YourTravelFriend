package com.example.your_travel_friend

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.your_travel_friend.model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    lateinit var profilePic: ImageView
    lateinit var nameEditText: EditText
    lateinit var phoneNumberEditText: EditText
    lateinit var licencePlateEditText: EditText
    lateinit var vehicleModelEditText: EditText
    lateinit var vacinatedCard: CardView
    lateinit var saveBtn: Button

    lateinit var firebaseAuth: FirebaseAuth

    var isVaccinated: Boolean = false
    private var currentUser: FirebaseUser? = null
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = firebaseAuth.currentUser
        db = FirebaseFirestore.getInstance()
        profilePic = findViewById(R.id.profileImage)
        nameEditText = findViewById(R.id.nameEditText)
        phoneNumberEditText = findViewById(R.id.phoneNumberEdittext)
        licencePlateEditText = findViewById(R.id.licence_plate_edit_text)
        vehicleModelEditText = findViewById(R.id.vehicle_model_edit_text)
        vacinatedCard = findViewById(R.id.vaccinated_card)
        saveBtn = findViewById(R.id.save_profile_btn)
        val isVaccinatedText = findViewById<TextView>(R.id.isVaccinatedText)

        vacinatedCard.setOnClickListener {
            if (!isVaccinated) {
                vacinatedCard.setCardBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.purple_500
                    )
                )
                isVaccinatedText.setTextColor(ContextCompat.getColor(this, R.color.white))
                isVaccinated = true
                isVaccinatedText.text = "vaccinated"
            } else {
                vacinatedCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white))
                isVaccinatedText.setTextColor(ContextCompat.getColor(this, R.color.black))
                isVaccinatedText.text = "not vaccinated"
                isVaccinated = false
            }
        }

        if (!isVaccinated) {
            isVaccinatedText.text = "not vaccinated"
        } else {
            isVaccinatedText.text = "vaccinated"
        }

        if (currentUser != null) {
            nameEditText.text = SpannableStringBuilder(currentUser!!.displayName)
            Glide.with(this).load(currentUser!!.photoUrl).into(profilePic)
        }

        saveBtn.setOnClickListener {
            run {
                saveDetails(
                    nameEditText.text.toString(),
                    phoneNumberEditText.text.toString(),
                    licencePlateEditText.text.toString(),
                    vehicleModelEditText.text.toString(),
                    isVaccinated
                )
                val intent = Intent(this, SelectType::class.java)
                startActivity(intent)
            }
        }
    }

    private fun saveDetails(
        username: String,
        phoneNumber: String,
        licencePlateNumber: String,
        vehicleNumber: String,
        isVaccinated: Boolean
    ) {
        if (currentUser != null) {


            val userData = UserData(
                currentUser!!.uid,
                username,
                phoneNumber,
                licencePlateNumber,
                vehicleNumber,
                isVaccinated
            )
            val documentReference = db.collection("users").document(currentUser!!.uid)
            documentReference.set(userData.geUserData())
                .addOnSuccessListener { documentReference ->
                    Log.d("addProfile", "user data added for user id ${currentUser!!.uid}")
                }
                .addOnFailureListener { e ->
                    Log.w("addProfile", "Error adding document", e)
                }
        }
    }
}