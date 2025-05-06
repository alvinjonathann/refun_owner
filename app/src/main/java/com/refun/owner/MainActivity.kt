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
// batas resolve conflict
            startActivity(Intent(this, CartActivity::class.java))
//            startBarcodeScanner()
        }
    }

    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
        integrator.setPrompt("Scan a bottle barcode")
        integrator.setCameraId(0)
        integrator.setBeepEnabled(true)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                handleScannedBarcode(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleScannedBarcode(barcode: String) {
        val bottle = BottleDatabase.findByBarcode(barcode)
        if (bottle != null) {
            scannedBottles.add(ScannedBottle(bottle, Instant.now().toString()))
            showContinueScanningDialog()
        } else {
            Toast.makeText(this, "Unknown bottle barcode", Toast.LENGTH_LONG).show()
            startBarcodeScanner()
// batas resolve conflict
            // Buka BarcodeScannerActivity (ML Kit + CameraX)
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivity(intent)
// batas resolve conflict
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