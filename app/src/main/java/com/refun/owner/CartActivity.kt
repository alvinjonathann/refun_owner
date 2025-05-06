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

import com.google.firebase.firestore.FirebaseFirestore

import com.refun.owner.databinding.ActivityCartBinding
import com.refun.owner.databinding.ItemBottleBinding
import com.refun.owner.model.Bottle
import com.refun.owner.model.ScannedBottle
import java.time.Instant

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding

    // private lateinit var adapter: BottleAdapter
    private var scannedBottles: ArrayList<ScannedBottle> = ArrayList()
    private lateinit var barcodeView: BarcodeView
    private val CAMERA_PERMISSION_REQUEST = 100
    private var barcodeViewIsRunning = false
    private lateinit var scannerLine: View
    private lateinit var scannerLineAnimator: ObjectAnimator

    private lateinit var adapter: ProductCountAdapter
    private val productCountMap = mutableMapOf<String, Int>()
    private val firestore = FirebaseFirestore.getInstance()


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

        val bottleIds = intent.getStringArrayListExtra("bottle_ids")
        if (bottleIds != null && bottleIds.isNotEmpty()) {
            fetchProductNamesAndCount(bottleIds)
        } else {
            setupRecyclerView()
            updateTotalPoints()
            setupButtons()
        }
    }


    private fun fetchProductNamesAndCount(barcodes: List<String>) {
        productCountMap.clear()
        var fetched = 0
        for (barcode in barcodes) {
            firestore.collection("bottle_barcodes").document(barcode)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name") ?: "Unknown"
                    productCountMap[name] = (productCountMap[name] ?: 0) + 1
                }
                .addOnCompleteListener {
                    fetched++
                    if (fetched == barcodes.size) {
                        setupRecyclerView()
                        updateTotalPoints()
                        setupButtons()
                    }
                }
        }
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
//
//        adapter = BottleAdapter(scannedBottles, object : BottleAdapterListener {
//            override fun onBottleRemoved(removedBottle: ScannedBottle) {
//                updateTotalPoints()
//            }
//        })
//
        adapter = ProductCountAdapter(productCountMap)

        binding.bottlesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bottlesRecyclerView.adapter = adapter
    }

    private fun updateTotalPoints() {
        val total = productCountMap.values.sum()
        binding.totalPointsTextView.text = "Total Bottles: $total"
    }

    private fun setupButtons() {
        binding.backButton.setOnClickListener {
//
//            finish()
            if (barcodeViewIsRunning) {
                turnOffBarcodeView()
            }
            else {
                turnOnBarcodeView()
            }
//
            val ids = ArrayList<String>()
            productCountMap.forEach { (name, count) ->
                repeat(count) { ids.add(name) }
            }
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            intent.putStringArrayListExtra("bottle_ids", ids)
            startActivity(intent)
            finish()
//
        }

        binding.generateQrButton.setOnClickListener {
            // Ambil daftar barcode asli dari intent (agar benar-benar sesuai scan)
            val bottleIds = intent.getStringArrayListExtra("bottle_ids") ?: arrayListOf()
            val qrIntent = Intent(this, QRGeneratorActivity::class.java)
            qrIntent.putStringArrayListExtra("bottle_ids", bottleIds)
            startActivity(qrIntent)
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

//
//class BottleAdapter(private val bottles: MutableList<ScannedBottle>, private val listener: BottleAdapterListener) :
//    RecyclerView.Adapter<BottleAdapter.BottleViewHolder>() {
//
class ProductCountAdapter(private val productCountMap: Map<String, Int>) :
    RecyclerView.Adapter<ProductCountAdapter.ProductCountViewHolder>() {
//

    class ProductCountViewHolder(private val binding: ItemBottleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String, count: Int) {
            binding.brandTextView.text = name
            binding.volumeTextView.text = ""
            binding.pointsTextView.text = "Jumlah: $count"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCountViewHolder {
        val binding = ItemBottleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductCountViewHolder(binding)
    }

//
//    override fun onBindViewHolder(holder: BottleViewHolder, position: Int) {
//        holder.bind(bottles[position])
//
//        holder.itemView.findViewById<Button>(R.id.removeBottleBtn).setOnClickListener {
//            val removedBottle = bottles[position]
//            bottles.removeAt(position)
//            notifyItemRemoved(position)
//            listener.onBottleRemoved(removedBottle)
//        }
//
    override fun onBindViewHolder(holder: ProductCountViewHolder, position: Int) {
        val entry = productCountMap.entries.elementAt(position)
        holder.bind(entry.key, entry.value)
//
    }

    override fun getItemCount() = productCountMap.size
} 