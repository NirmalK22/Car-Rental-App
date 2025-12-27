package com.example.free2move

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val cashOption = findViewById<RadioButton>(R.id.cashOption)
        val cardOption = findViewById<RadioButton>(R.id.cardOption)
        val cardOptionsContainer = findViewById<FrameLayout>(R.id.cardOptionsContainer)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        cardOption.setOnClickListener {
            if (cardOption.isChecked) {
                cardOptionsContainer.visibility = FrameLayout.VISIBLE
                loadFragment(CardOptionsFragment())
            }
        }

        cashOption.setOnClickListener {
            cardOptionsContainer.visibility = FrameLayout.GONE
        }

        confirmButton.setOnClickListener {
            handlePaymentConfirmation(cashOption, cardOption)
        }
    }

    private fun handlePaymentConfirmation(cashOption: RadioButton, cardOption: RadioButton) {
        val paymentMethod = when {
            cashOption.isChecked -> "Cash"
            cardOption.isChecked -> {
                val visaOption = findViewById<CheckBox>(R.id.visaOption)
                val masterCardOption = findViewById<CheckBox>(R.id.masterCardOption)

                val selectedCards = mutableListOf<String>()
                if (visaOption.isChecked) selectedCards.add("Visa")
                if (masterCardOption.isChecked) selectedCards.add("MasterCard")

                if (selectedCards.isEmpty()) {
                    showError("Please select a card type")
                    return
                }
                "Card (${selectedCards.joinToString(", ")})"
            }
            else -> {
                showError("Please select a payment method")
                return
            }
        }

        // Retrieve booking data from shared preferences
        val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
        val bookingId = sharedPrefs.getString("lastBookingId", null)
        val bookingType = sharedPrefs.getString("bookingType", null)

        if (bookingId == null || bookingType == null) {
            showError("No booking data found")
            return
        }


        Toast.makeText(this, "Payment Confirmed: $paymentMethod", Toast.LENGTH_LONG).show()


        sharedPrefs.edit().clear().apply()

        val intent = Intent(this, CategoryActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.cardOptionsContainer, fragment)
        transaction.commit()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        val cardOptionsContainer = findViewById<FrameLayout>(R.id.cardOptionsContainer)
        if (cardOptionsContainer.visibility == FrameLayout.VISIBLE) {
            cardOptionsContainer.visibility = FrameLayout.GONE
            findViewById<RadioButton>(R.id.cashOption).isChecked = true
        } else {
            super.onBackPressed()
        }
    }
}
