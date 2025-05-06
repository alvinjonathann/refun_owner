package com.refun.owner

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import com.refun.owner.data.BottleDatabase
import com.refun.owner.databinding.ActivityCartBinding
import com.refun.owner.databinding.ItemBottleBinding
import com.refun.owner.model.Bottle
import com.refun.owner.model.ScannedBottle
import java.time.Instant

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: BottleAdapter
    private var scannedBottles: ArrayList<ScannedBottle> = ArrayList()
    private lateinit var barcodeView: BarcodeView
    private val CAMERA_PERMISSION_REQUEST = 100
    private var barcodeViewIsRunning = false
    private lateinit var scannerLine: View
    private lateinit var scannerLineAnimator: ObjectAnimator

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
        scannerLine = binding.scannerLine

        val callback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (result?.text != null) {
                    val bottle = BottleDatabase.findByBarcode(result.text)
                    if (bottle != null) {
                        scannedBottles.add(ScannedBottle(bottle, Instant.now().toString()))
                        adapter.notifyItemInserted(scannedBottles.size - 1)
                        updateTotalPoints()
                    }
                    else {
                        Toast.makeText(this@CartActivity, "Barcode: ${result.text} not available at database", Toast.LENGTH_LONG).show()
                    }
                    turnOffBarcodeView()
                }
            }
        }

        barcodeView.decodeContinuous(callback)
        startScanningLineAnimation()
        turnOnBarcodeView()

        @Suppress("DEPRECATION")
        scannedBottles = intent.getParcelableArrayListExtra("scannedBottles") ?: ArrayList()

        setupRecyclerView()
        updateTotalPoints()
        setupButtons()
    }

    private fun startScanningLineAnimation() {
        barcodeView.post {
            val viewHeight = barcodeView.height
            val lineHeight = scannerLine.height
            val distance = ((viewHeight - lineHeight) / 2).toFloat()

            scannerLineAnimator = ObjectAnimator.ofFloat(scannerLine, "translationY", -distance, distance)
            scannerLineAnimator.duration = 2000
            scannerLineAnimator.repeatCount = ObjectAnimator.INFINITE
            scannerLineAnimator.repeatMode = ObjectAnimator.REVERSE
            scannerLineAnimator.start()
        }
    }

    private fun setupRecyclerView() {
        adapter = BottleAdapter(scannedBottles, object : BottleAdapterListener {
            override fun onBottleRemoved(removedBottle: ScannedBottle) {
                updateTotalPoints()
            }
        })
        binding.bottlesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bottlesRecyclerView.adapter = adapter
    }

    private fun updateTotalPoints() {
        val total = scannedBottles.sumOf { it.bottle.points }
        binding.totalPointsTextView.text = "Total Points: $total"
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
//            finish()
            if (barcodeViewIsRunning) {
                turnOffBarcodeView()
            }
            else {
                turnOnBarcodeView()
            }
        }

        binding.generateQrButton.setOnClickListener {
            val intent = Intent(this, QrCodeActivity::class.java)
            intent.putParcelableArrayListExtra("scannedBottles", scannedBottles)
            startActivity(intent)
        }
    }

    private fun turnOffBarcodeView() {
        barcodeView.pause()
        barcodeViewIsRunning = false
        binding.backButton.text = "Start Scan"
        if (::scannerLineAnimator.isInitialized &&
            scannerLineAnimator.isRunning) {
            scannerLineAnimator.pause()
        }
    }

    private fun turnOnBarcodeView() {
        barcodeView.resume()
        barcodeViewIsRunning = true
        binding.backButton.text = "Stop Scan"
        if (::scannerLineAnimator.isInitialized && scannerLineAnimator.isPaused) {
            scannerLineAnimator.resume()
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

interface BottleAdapterListener {
    fun onBottleRemoved(removedBottle: ScannedBottle)
}

class BottleAdapter(private val bottles: MutableList<ScannedBottle>, private val listener: BottleAdapterListener) :
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

        holder.itemView.findViewById<Button>(R.id.removeBottleBtn).setOnClickListener {
            val removedBottle = bottles[position]
            bottles.removeAt(position)
            notifyItemRemoved(position)
            listener.onBottleRemoved(removedBottle)
        }
    }

    override fun getItemCount() = bottles.size
} 