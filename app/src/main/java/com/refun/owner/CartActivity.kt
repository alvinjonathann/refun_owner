package com.refun.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.refun.owner.databinding.ActivityCartBinding
import com.refun.owner.databinding.ItemBottleBinding
import com.refun.owner.model.ScannedBottle

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: ProductCountAdapter
    private val productCountMap = mutableMapOf<String, Int>()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    private fun setupRecyclerView() {
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
            val ids = ArrayList<String>()
            productCountMap.forEach { (name, count) ->
                repeat(count) { ids.add(name) }
            }
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            intent.putStringArrayListExtra("bottle_ids", ids)
            startActivity(intent)
            finish()
        }

        binding.generateQrButton.setOnClickListener {
            // Ambil daftar barcode asli dari intent (agar benar-benar sesuai scan)
            val bottleIds = intent.getStringArrayListExtra("bottle_ids") ?: arrayListOf()
            val qrIntent = Intent(this, QRGeneratorActivity::class.java)
            qrIntent.putStringArrayListExtra("bottle_ids", bottleIds)
            startActivity(qrIntent)
        }
    }
}

class ProductCountAdapter(private val productCountMap: Map<String, Int>) :
    RecyclerView.Adapter<ProductCountAdapter.ProductCountViewHolder>() {

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

    override fun onBindViewHolder(holder: ProductCountViewHolder, position: Int) {
        val entry = productCountMap.entries.elementAt(position)
        holder.bind(entry.key, entry.value)
    }

    override fun getItemCount() = productCountMap.size
} 