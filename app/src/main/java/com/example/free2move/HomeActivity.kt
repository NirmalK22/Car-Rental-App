package com.example.free2move


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        val userDetailsButton = findViewById<Button>(R.id.btn_user_details)
        val bookingButton = findViewById<Button>(R.id.btn_booking)
        val bookingDetailsButton = findViewById<Button>(R.id.btn_history)


        userDetailsButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, SettingsActivity::class.java)
            startActivity(intent)
        }



        bookingButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, CategoryActivity::class.java)
            startActivity(intent)
        }

        bookingDetailsButton.setOnClickListener {
            val intent = Intent(this@HomeActivity, UserBooking::class.java)
            startActivity(intent)
        }
    }
}
