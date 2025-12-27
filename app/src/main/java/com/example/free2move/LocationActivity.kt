package com.example.free2move

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.location.Geocoder
import android.location.Address
import java.util.*

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var databaseReference: DatabaseReference
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var currentLocation: LatLng? = null
    private var address: String? = null
    private var vehicleMode: String? = null
    private lateinit var auth: FirebaseAuth
    private var bookingVerified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()

    
        val shareLocationButton: Button = findViewById(R.id.shareLocationButton)
        val backButton: Button = findViewById(R.id.backButton)

        val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
        val bookingId = sharedPrefs.getString("lastBookingId", null)
        val bookingType = sharedPrefs.getString("bookingType", null)

        // Initialize database reference based on booking type
        databaseReference = when (bookingType) {
            "car" -> FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/")
                .getReference("VehicleBookings")
            "tuktuk" -> FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/")
                .getReference("TukTukBookings")
            else -> FirebaseDatabase.getInstance("https://free2move-82a3b-default-rtdb.firebaseio.com/")
                .getReference("ScooterBookings")
        }

        // Initialize map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Fetch vehicle mode and verify booking
        fetchVehicleAndVerifyBooking(bookingId, bookingType)

        backButton.setOnClickListener {
            finish()
        }



        shareLocationButton.setOnClickListener {
            if (bookingVerified) {
                shareCurrentLocationAndGoToPayment()
            } else {
                Toast.makeText(this, "Please wait for booking verification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchVehicleAndVerifyBooking(bookingId: String?, bookingType: String?) {
        if (bookingId == null || bookingType == null) {
            Toast.makeText(this, "Booking details not found", Toast.LENGTH_SHORT).show()
            return
        }

        databaseReference.child(bookingId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    vehicleMode = snapshot.child("vehicleMode").getValue(String::class.java)

                    val isValidBooking = when (bookingType.toLowerCase()) {
                        "car" -> vehicleMode?.toLowerCase() == "car"
                        "tuktuk" -> vehicleMode?.toLowerCase() == "tuktuk"
                        "scooter" -> vehicleMode?.toLowerCase() == "scooter"
                        else -> false
                    }

                    bookingVerified = isValidBooking

                    if (!isValidBooking) {
                        Toast.makeText(baseContext, "Invalid booking type!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, "Booking not found!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(baseContext, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun shareCurrentLocationAndGoToPayment() {
        currentLocation?.let { location ->
            val sharedPrefs = getSharedPreferences("BookingPrefs", MODE_PRIVATE)
            val bookingId = sharedPrefs.getString("lastBookingId", null)

            if (bookingId != null) {
                val currentUser = auth.currentUser
                val userEmail = currentUser?.email ?: "Unknown User"

                val locationData = mapOf(
                    "latitude" to location.latitude,
                    "longitude" to location.longitude,
                    "address" to address,
                    "userEmail" to userEmail
                )

                databaseReference.child(bookingId).child("location").setValue(locationData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Location shared successfully!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PaymentActivity::class.java))
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to share location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } ?: Toast.makeText(this, "Please select a location first", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            currentLocation = latLng
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            getAddressFromLatLng(latLng)
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            enableMyLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                }
            }
        }
    }

    private fun getAddressFromLatLng(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                address = addr.getAddressLine(0)
                Toast.makeText(this, "Address: $address", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to get address", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation()
                }
            }
        }
    }
}
