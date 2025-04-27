package com.refun.owner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.refun.owner.databinding.ActivityQrCodeBinding
import com.refun.owner.model.ScannedBottle
import com.refun.owner.utils.QrCodeUtils

class QrCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val scannedBottles = intent.getParcelableArrayListExtra<ScannedBottle>("scannedBottles")
        
        if (scannedBottles != null) {
            val qrCodeBitmap = QrCodeUtils.generateQrCodeBitmap(
                scannedBottles,
                800,
                800
            )
            binding.qrCodeImageView.setImageBitmap(qrCodeBitmap)
        }

        binding.backToMainButton.setOnClickListener {
            // Clear all activities and go back to MainActivity
            finishAffinity()
        }
    }
} 