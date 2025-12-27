package com.example.free2move

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class SettingsActivity : AppCompatActivity() {

    private lateinit var profilePicture: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private val CAMERA_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        try {
            // Initialize Firebase
            firebaseAuth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance().getReference("users")
            storage = FirebaseStorage.getInstance()
            storageReference = storage.reference

            // Initialize views
            initializeViews()
            // Load user data
            loadUserData()
            // Set up button click listeners
            setupButtonListeners()

        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeViews() {
        profilePicture = findViewById(R.id.profilePicture)
        nameEditText = findViewById(R.id.editTextName)
        emailEditText = findViewById(R.id.editTextEmail)
        phoneEditText = findViewById(R.id.editTextPhone)
        passwordEditText = findViewById(R.id.editTextPassword)
    }

    private fun setupButtonListeners() {
        val updateProfileButton: Button = findViewById(R.id.updateProfileButton)
        val updateButton: Button = findViewById(R.id.updateUsername)
        val backButton: Button = findViewById(R.id.backButton)

        updateProfileButton.setOnClickListener {
            openCamera()
        }

        updateButton.setOnClickListener {
            updateUserProfile()
            updatePassword()
        }

        backButton.setOnClickListener {
            val intent = Intent(this@SettingsActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserData() {
        try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return
            }

            // Safely set email
            emailEditText.setText(currentUser.email ?: "")

            // Get additional user data
            database.child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if (snapshot.exists()) {
                                val name = snapshot.child("name").getValue(String::class.java)
                                val phone = snapshot.child("phone").getValue(String::class.java)
                                val email = snapshot.child("email").getValue(String::class.java)
                                val password = snapshot.child("password").getValue(String::class.java)

                                nameEditText.setText(name ?: "")
                                phoneEditText.setText(phone ?: "")
                                passwordEditText.setText(password ?: "")
                                if (emailEditText.text.isEmpty() && email != null) {
                                    emailEditText.setText(email)
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@SettingsActivity,
                                "Error loading data: ${e.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SettingsActivity,
                            "Failed to load user data: ${error.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                })

            // Load profile picture if available
            loadProfilePicture()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfilePicture() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            database.child(currentUser.uid).child("profile_picture").get()
                .addOnSuccessListener { snapshot ->
                    val base64Image = snapshot.value as? String
                    if (base64Image != null) {
                        // Decode the Base64 image
                        val decodedImage = decodeBase64ToBitmap(base64Image)
                        // Set the decoded image in the ImageView
                        profilePicture.setImageBitmap(decodedImage)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

    private fun updatePassword() {
        try {
            val newPassword = passwordEditText.text.toString()

            if (newPassword.isNotEmpty()) {
                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters long",
                        Toast.LENGTH_SHORT).show()
                    return
                }

                val user = firebaseAuth.currentUser
                user?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password updated successfully",
                                Toast.LENGTH_SHORT).show()
                            passwordEditText.setText("")
                        } else {
                            Toast.makeText(this,
                                "Failed to update password: ${task.exception?.message}",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating password: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile() {
        try {
            val currentUser = firebaseAuth.currentUser ?: return

            // Create a Map<String, Any> instead of HashMap<String, String>
            val updatedUser: MutableMap<String, Any> = mutableMapOf(
                "name" to nameEditText.text.toString(),
                "email" to emailEditText.text.toString(),
                "phone" to phoneEditText.text.toString()
            )

            // Update in Realtime Database
            database.child(currentUser.uid).updateChildren(updatedUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully",
                        Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Update failed: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error updating profile: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCamera() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening camera: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
                val photo: Bitmap? = data?.extras?.get("data") as? Bitmap
                photo?.let {
                    // Convert bitmap to Base64 string
                    val base64Image = convertBitmapToBase64(it)
                    saveImageToFirebase(base64Image)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error handling camera result: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveImageToFirebase(base64Image: String) {
        try {
            val currentUser = firebaseAuth.currentUser ?: return

            // Update profile picture in Firebase
            val updatedUser: MutableMap<String, Any> = mutableMapOf(
                "profile_picture" to base64Image
            )

            database.child(currentUser.uid).updateChildren(updatedUser)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated successfully",
                        Toast.LENGTH_SHORT).show()
                    loadProfilePicture()  // Reload image after update
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update profile picture: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, "Error saving profile picture: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }
}
