package com.example.free2move

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.free2move.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewDataBinding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()


        database = FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/").reference

        binding.apply {
            registerButton.setOnClickListener {
                val email = registerEmail.text.toString()
                val name=registerName.text.toString()
                val phone = registerPhoneNumber.text.toString()
                val password = registerPassword.text.toString()
                val confirmPassword = registerConfirmPassword.text.toString()


                if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (password == confirmPassword) {
                        registerUser(name,email, password, phone)
                    } else {
                        Toast.makeText(this@RegisterActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            registerLoginLink.setOnClickListener {
                finish()
            }
        }
    }

    private fun registerUser(name:String,email: String, password: String, phone: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid
                    val user = User(name,email,phone,password)

                    // Save the user data in Firebase Realtime Database
                    userId?.let {
                        database.child("users").child(it).setValue(user)
                            .addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                                    finish() // Redirect back to Login Activity
                                } else {
                                    Toast.makeText(this, "Failed to save data: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
data class User(
    val name:String,
    val email: String,
    val phone: String,
    val password: String

)
