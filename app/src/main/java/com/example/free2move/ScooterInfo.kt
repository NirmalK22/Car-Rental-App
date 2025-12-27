package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ScooterInfo : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_info)

        firebaseAuth = FirebaseAuth.getInstance()

        val backButton = findViewById<Button>(R.id.backButton)
        val continueButton = findViewById<Button>(R.id.continueButton)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val daysInput = findViewById<EditText>(R.id.daysInput)
        val amountField = findViewById<EditText>(R.id.amount)

        val category = intent.getStringExtra("category")

        // Define vehicleMode variable
        var vehicleMode = "scooter" // Default to "scooter"

        if ("Scooter" == category) {
            val typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.bike_models, android.R.layout.simple_spinner_item
            )
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = typeAdapter

            vehicleMode = "scooter" // Set the vehicle mode to scooter for this activity
        }

        continueButton.setOnClickListener {
            val selectedType = typeSpinner.selectedItem.toString()
            val enteredDays = daysInput.text.toString()

            if (enteredDays.isEmpty()) {
                Toast.makeText(
                    this@ScooterInfo,
                    "Please enter the number of days",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val days = enteredDays.toInt()
            val amount = days * 1000
            amountField.setText(amount.toString())

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val userEmail = currentUser.email
                if (userEmail != null) {
                    val database = FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/")
                        .getReference("ScooterBookings")

                    val bookingId = database.push().key
                    val booking = BookingData(selectedType, enteredDays, amount.toString(), userEmail, null, vehicleMode)

                    bookingId?.let {
                        database.child(it).setValue(booking)
                            .addOnSuccessListener {
                                val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
                                sharedPrefs.edit()
                                    .putString("lastBookingId", bookingId)
                                    .putString("bookingType", "scooter")
                                    .apply()

                                Toast.makeText(this@ScooterInfo, "Select Your Location", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@ScooterInfo, LocationActivity::class.java)
                                startActivity(intent)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@ScooterInfo,
                                    "Error saving booking: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve user email", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this@ScooterInfo, CategoryActivity::class.java)
            startActivity(intent)
        }
    }

    internal data class BookingData(
        val vehicleType: String = "",
        val days: String = "",
        val amount: String = "",
        val userEmail: String = "",
        val location: Map<String, Double>? = null,
        val vehicleMode: String = ""
    )
}
