package com.refun.owner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.refun.owner.data.BottleDatabase
import com.refun.owner.databinding.ActivityMainBinding
import com.refun.owner.model.ScannedBottle
import java.time.Instant

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val scannedBottles = mutableListOf<ScannedBottle>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.scanButton.setOnClickListener {
            // Buka BarcodeScannerActivity (ML Kit + CameraX)
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showContinueScanningDialog() {
        AlertDialog.Builder(this)
            .setMessage("Do you want to scan another bottle?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, BarcodeScannerActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("No") { _, _ -> goToCart() }
            .show()
    }

    private fun goToCart() {
        val intent = Intent(this, CartActivity::class.java)
        intent.putParcelableArrayListExtra("scannedBottles", ArrayList(scannedBottles))
        startActivity(intent)
    }
} 