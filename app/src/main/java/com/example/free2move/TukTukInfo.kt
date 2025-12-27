package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class TukTukInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_info)

        val backButton = findViewById<Button>(R.id.backButton)
        val continueButton: Button = findViewById(R.id.continueButton)
        val typeSpinner = findViewById<Spinner>(R.id.typeSpinner)
        val daysInput = findViewById<EditText>(R.id.daysInput)
        val amountField = findViewById<EditText>(R.id.amount)

        // Get the category passed via Intent
        val category = intent.getStringExtra("category")

        // Set the appropriate spinner values based on category
        if ("TukTuk" == category) {
            val typeAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.tuktuk_models,
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
                    this@TukTukInfo,
                    "Please enter the number of days",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val days = enteredDays.toInt()
            val amount = (days * 1500).toString() // Convert amount to String
            amountField.setText(amount)

            val userEmail = FirebaseAuth.getInstance().currentUser?.email
            if (userEmail == null) {
                Toast.makeText(this@TukTukInfo, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailKey = userEmail.replace(".", "_")

            // Firebase setup
            val database: DatabaseReference = FirebaseDatabase.getInstance(
                "https://free2move-82a3b-default-rtdb.firebaseio.com/"
            ).getReference("TukTukBookings")

            val bookingId = database.push().key // Generate a unique ID for the booking
            val booking = BookingData(
                selectedType,
                enteredDays,
                userEmail,
                null,
                amount,  // Save the amount as String
                "TukTuk" // Default value for vehicleMode as TukTuk
            )

            bookingId?.let {
                database.child(it).setValue(booking)
                    .addOnSuccessListener {
                        // Save booking ID to shared preferences
                        val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
                        sharedPrefs.edit()
                            .putString("lastBookingId", bookingId)
                            .putString("bookingType", "tuktuk")
                            .apply()

                        Toast.makeText(this@TukTukInfo, "Select Your Location", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@TukTukInfo, LocationActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@TukTukInfo,
                            "Error saving booking: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }

        backButton.setOnClickListener {
            val intent = Intent(this@TukTukInfo, CategoryActivity::class.java)
            startActivity(intent)
        }
    }

    data class BookingData(
        val vehicleType: String = "",
        val days: String = "",
        val userEmail: String = "",
        val location: Map<String, Any>? = null,
        val amount: String = "",
        val vehicleMode: String = "TukTuk"
    )
}
