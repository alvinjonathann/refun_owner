package com.refun.owner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.refun.owner.databinding.ActivityCartBinding
import com.refun.owner.databinding.ItemBottleBinding
import com.refun.owner.model.ScannedBottle

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: BottleAdapter
    private var scannedBottles: ArrayList<ScannedBottle> = ArrayList()
    private lateinit var barcodeView: BarcodeView
    private val CAMERA_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }

        barcodeView = binding.barcodeView

        val callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result?.text != null) {
                    Toast.makeText(this@CartActivity, "Scanned: ${result.text}", Toast.LENGTH_LONG).show()
                }
            }
        }

        barcodeView.decodeContinuous(callback)

        @Suppress("DEPRECATION")
        scannedBottles = intent.getParcelableArrayListExtra("scannedBottles") ?: ArrayList()

        setupRecyclerView()
        updateTotalPoints()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = BottleAdapter(scannedBottles)
        binding.bottlesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bottlesRecyclerView.adapter = adapter
    }

    private fun updateTotalPoints() {
        val total = scannedBottles.sumOf { it.bottle.points }
        binding.totalPointsTextView.text = "Total Points: $total"
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, QrCodeActivity::class.java)
            intent.putParcelableArrayListExtra("scannedBottles", scannedBottles)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }
}

class BottleAdapter(private val bottles: List<ScannedBottle>) :
    RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {

    class BottleViewHolder(private val binding: ItemBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(scannedBottle: ScannedBottle) {
            binding.brandTextView.text = scannedBottle.bottle.brand
            binding.volumeTextView.text = "${scannedBottle.bottle.volumeMl}ml"
            binding.pointsTextView.text = "${scannedBottle.bottle.points} Points"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottleViewHolder {
        val binding = ItemBottleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BottleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
        holder.bind(bottles[position])
    }

    override fun getItemCount() = bottles.size
} 