package com.refun.owner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.refun.owner.databinding.ActivityCartBinding
import com.refun.owner.databinding.ItemBottleBinding
import com.refun.owner.model.ScannedBottle

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: BottleAdapter
    private var scannedBottles: ArrayList<ScannedBottle> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

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