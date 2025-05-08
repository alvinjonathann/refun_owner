package com.refun.owner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.refun.owner.databinding.ActivityBarcodeScannerBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBarcodeScannerBinding
    private val scannedBottles = mutableListOf<String>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessingBarcode = false

    // Only allow EAN-13, UPC-A, and UPC-E barcodes
    private val barcodeOptions = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarcodeScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        cameraExecutor = Executors.newSingleThreadExecutor()

        val bottleIDS = intent.getStringArrayListExtra("bottle_ids")
        if (bottleIDS != null && bottleIDS.isNotEmpty()) {
            for (bottle in bottleIDS) {
                Log.d("TAGG", bottle)
                scannedBottles.add(bottle)
            }
        }

        setupCameraX()
    }

    private fun setupCameraX() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, { imageProxy ->
                        processCameraFrame(imageProxy)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera error: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processCameraFrame(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null && !isProcessingBarcode) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val scanner = BarcodeScanning.getClient(barcodeOptions)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        if (barcode.format == Barcode.FORMAT_EAN_13 ||
                            barcode.format == Barcode.FORMAT_UPC_A ||
                            barcode.format == Barcode.FORMAT_UPC_E) {
                            val code = barcode.rawValue ?: continue
                            //if (!scannedBottles.contains(code)) {
                                // Verifikasi ke Firestore

                                isProcessingBarcode = true
                                verifyBarcodeWithFirestore(code) {
                                    success ->
                                    runOnUiThread {
                                        if (success) {
                                            scannedBottles.add(code)
                                            val intent = Intent(this, CartActivity::class.java)
                                            intent.putStringArrayListExtra("bottle_ids", ArrayList(scannedBottles))
                                            Log.d("BarcodeSendCart", scannedBottles.toString())
                                            startActivity(intent)
                                            finish()
                                        } else {
                                            Toast.makeText(this, "Barcode tidak valid atau sudah digunakan!", Toast.LENGTH_SHORT).show()
                                            isProcessingBarcode = false
                                        }
                                    }
                                }
                                break // Hanya proses satu barcode per frame
                            //}
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    // Fungsi verifikasi barcode ke Firestore
    private fun verifyBarcodeWithFirestore(barcode: String, callback: (Boolean) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("bottle_barcodes").document(barcode)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
} 