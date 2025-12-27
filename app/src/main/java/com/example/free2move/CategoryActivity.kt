package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val scooterImage = findViewById<ImageView>(R.id.scooterImage)
        val carImage = findViewById<ImageView>(R.id.carImage)
        val tuktukImage = findViewById<ImageView>(R.id.tuktukImage)
        val leadingButton = findViewById<ImageButton>(R.id.leadingButton)

        leadingButton.setOnClickListener {
            val intent = Intent(this@CategoryActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        scooterImage.setOnClickListener {
            val intent = Intent(this@CategoryActivity, ScooterInfo::class.java)
            intent.putExtra("category", "Scooter")
            startActivity(intent)
        }

        carImage.setOnClickListener {
            val intent = Intent(this@CategoryActivity, CarInfo::class.java)
            intent.putExtra("category", "Car")
            startActivity(intent)
        }

        tuktukImage.setOnClickListener {
            val intent = Intent(this@CategoryActivity, TukTukInfo::class.java)
            intent.putExtra("category", "TukTuk")
            startActivity(intent)
        }
    }
}
