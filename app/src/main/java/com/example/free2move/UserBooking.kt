package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserBooking : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var nameTextView: TextView
    private lateinit var daysTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var vehicleTypeTextView: TextView
    private lateinit var vehicleAmountTextView: TextView

    private lateinit var cardaysTextView: TextView
    private lateinit var caraddressTextView: TextView
    private lateinit var carvehicleTypeTextView: TextView
    private lateinit var carvehicleAmountTextView: TextView

    private lateinit var tuktukDaysTextView: TextView
    private lateinit var tuktukAddressTextView: TextView
    private lateinit var tuktukVehicleTypeTextView: TextView
    private lateinit var tuktukVehicleAmountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userbooking)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/").reference

        nameTextView = findViewById(R.id.name_value)
        daysTextView = findViewById(R.id.scooter_days_value)
        addressTextView = findViewById(R.id.scooter_address_value)
        vehicleTypeTextView = findViewById(R.id.scooter_type_value)
        vehicleAmountTextView = findViewById(R.id.scooter_amount_value)

        cardaysTextView = findViewById(R.id.car_days_value)
        caraddressTextView = findViewById(R.id.car_address_value)
        carvehicleTypeTextView = findViewById(R.id.car_type_value)
        carvehicleAmountTextView = findViewById(R.id.car_amount_value)

        tuktukDaysTextView = findViewById(R.id.tuktuk_days_value)
        tuktukAddressTextView = findViewById(R.id.tuktuk_address_value)
        tuktukVehicleTypeTextView = findViewById(R.id.tuktuk_type_value)
        tuktukVehicleAmountTextView = findViewById(R.id.tutuk_amount_value)

        val backButton: Button = findViewById(R.id.back_button)

        // Get current user's email
        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail != null) {
            val userId = auth.currentUser?.uid
            database.child("users").child(userId!!).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.value as? String
                    name?.let { nameTextView.text = it }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UserBooking, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

            // Fetch latest scooter booking
            database.child("ScooterBookings")
                .orderByChild("userEmail").equalTo(currentUserEmail)
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val bookingSnapshot = snapshot.children.first()  // Get the only child
                            val booking = bookingSnapshot.getValue(ScooterBooking::class.java)
                            booking?.let {
                                daysTextView.text = it.days
                                addressTextView.text = it.location?.address ?: "N/A"
                                vehicleAmountTextView.text = it.amount
                                vehicleTypeTextView.text = it.vehicleType
                            }
                        } else {
                            daysTextView.text = "No booking"
                            addressTextView.text = "No booking"
                            vehicleAmountTextView.text = "No booking"
                            vehicleTypeTextView.text = "No booking"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserBooking, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            // Fetch latest car booking
            database.child("VehicleBookings")
                .orderByChild("userEmail").equalTo(currentUserEmail)
                .limitToLast(1)  // Get only the latest booking
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val bookingSnapshot = snapshot.children.first()
                            val booking = bookingSnapshot.getValue(CarBooking::class.java)
                            booking?.let {
                                cardaysTextView.text = it.days
                                caraddressTextView.text = it.location?.address ?: "N/A"
                                carvehicleAmountTextView.text = it.amount
                                carvehicleTypeTextView.text = it.vehicleType
                            }
                        } else {
                            cardaysTextView.text = "No booking"
                            caraddressTextView.text = "No booking"
                            carvehicleAmountTextView.text = "No booking"
                            carvehicleTypeTextView.text = "No booking"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserBooking, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

            // Fetch latest tuk tuk booking
            database.child("TukTukBookings")
                .orderByChild("userEmail").equalTo(currentUserEmail)
                .limitToLast(1)  // Get only the latest booking
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val bookingSnapshot = snapshot.children.first()
                            val booking = bookingSnapshot.getValue(TuktukBooking::class.java)
                            booking?.let {
                                tuktukDaysTextView.text = it.days
                                tuktukAddressTextView.text = it.location?.address ?: "N/A"
                                tuktukVehicleAmountTextView.text = it.amount
                                tuktukVehicleTypeTextView.text = it.vehicleType
                            }
                        } else {
                            tuktukDaysTextView.text = "No booking"
                            tuktukAddressTextView.text = "No booking"
                            tuktukVehicleAmountTextView.text = "No booking"
                            tuktukVehicleTypeTextView.text = "No booking"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@UserBooking, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })

        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    data class ScooterBooking(
        val days: String = "",
        val location: Location? = null,
        val vehicleType: String = "",
        val userEmail: String = "",
        val amount: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    data class CarBooking(
        val days: String = "",
        val location: Location? = null,
        val vehicleType: String = "",
        val userEmail: String = "",
        val amount: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    data class TuktukBooking(
        val days: String = "",
        val location: Location? = null,
        val vehicleType: String = "",
        val userEmail: String = "",
        val amount: String = "",
        val timestamp: Long = System.currentTimeMillis()
    )

    data class Location(
        val address: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null
    )
}