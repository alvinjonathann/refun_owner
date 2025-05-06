package com.refun.owner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.refun.owner.databinding.ActivityQrGeneratorBinding
import com.google.gson.Gson

class QRGeneratorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrGeneratorBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var bottleIds: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        bottleIds = intent.getStringArrayListExtra("bottle_ids") ?: listOf()

        if (bottleIds.isEmpty()) {
            Toast.makeText(this, "No bottles to generate QR for", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        generateTransaction()
    }

    private fun generateTransaction() {
        val transaction = hashMapOf(
            "user_id" to userId,
            "bottle_ids" to bottleIds,
            "created_at" to Timestamp.now()
        )

        firestore.collection("transactions")
            .add(transaction)
            .addOnSuccessListener { doc ->
                showQRCode(doc.id)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error creating transaction: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun updateBottles(transactionId: String) {
        for (id in bottleIds) {
            val ref = firestore.collection("bottle_barcodes").document(id)
            ref.update(
                "is_used", true,
                "scanned_at", Timestamp.now(),
                "user_id", userId
            )
        }
    }

    private fun showQRCode(transactionId: String) {
        val bitmap = generateQRCode(transactionId)
        binding.qrImageView.setImageBitmap(bitmap)
    }

    private fun generateQRCode(text: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }
} 