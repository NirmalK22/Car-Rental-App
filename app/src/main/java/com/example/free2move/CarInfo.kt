package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CarInfo : AppCompatActivity() {

    // Default variable
    private val vehicleMode: String = "car"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_info)

        val backButton = findViewById<Button>(R.id.backButton)
        val continueButton: Button = findViewById(R.id.continueButton)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val daysInput = findViewById<EditText>(R.id.daysInput)
        val amountField = findViewById<EditText>(R.id.amount)

        val category = intent.getStringExtra("category")

        // Set the appropriate spinner values based on category
        if ("Car" == category) {
            val typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.car_models,
                android.R.layout.simple_spinner_item
            )
            typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            typeSpinner.adapter = typeAdapter
        }

        continueButton.setOnClickListener {
            val selectedType = typeSpinner.selectedItem.toString()
            val enteredDays = daysInput.text.toString()

            if (enteredDays.isEmpty()) {
                Toast.makeText(
                    this@CarInfo,
                    "Please enter the number of days",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Calculate the amount
            val days = enteredDays.toInt()
            val amount = days * 2000
            amountField.setText(amount.toString())

            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            if (userEmail == null) {
                Toast.makeText(this@CarInfo, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailKey = userEmail.replace(".", "_") // Replace '.' to make it Firebase-safe

            // Firebase setup
            val database: DatabaseReference = FirebaseDatabase.getInstance(
                "https://free2move-82a3b-default-rtdb.firebaseio.com/"
            ).getReference("VehicleBookings")

            val bookingId = database.push().key // Generate a unique ID for the booking
            val booking = BookingData(
                selectedType,
                enteredDays,
                userEmail,
                amount.toString(),  // Save the amount as a String
                vehicleMode
            )

            bookingId?.let {
                database.child(it).setValue(booking)
                    .addOnSuccessListener {
                        // Save booking ID to shared preferences
                        val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
                        sharedPrefs.edit()
                            .putString("lastBookingId", bookingId)
                            .putString("bookingType", vehicleMode)  // Save default vehicle mode
                            .apply()

                        Toast.makeText(this@CarInfo, "Select Your Location", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@CarInfo, LocationActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@CarInfo,
                            "Error saving booking: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this@CarInfo, CategoryActivity::class.java)
            startActivity(intent)
        }
    }

    data class BookingData(
        val vehicleType: String = "",
        val days: String = "",
        val userEmail: String = "",
        val amount: String = "",  // Changed to String
        val vehicleMode: String = "",
        val location: Map<String, Any>? = null
    )
}
