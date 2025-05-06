package com.refun.owner

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import com.refun.owner.model.HistoryItem

class QRHistory : AppCompatActivity() {
    private lateinit var historyRecyclerView: RecyclerView
    val qrHistoryModel = ArrayList<HistoryItem>()
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_qrhistory)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        historyRecyclerView = findViewById(R.id.historyRecyclerView)

        historyRecyclerView.setHasFixedSize(true)
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        retrieveQRHistory()
    }

    private fun retrieveQRHistory() {
        db.collection("qr-history")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document != null) {
                        lateinit var history: HistoryItem
                        history.id = document.id
                        history.status = document.getBoolean("status") ?: false
                        history.timestamp = document.getString("timestamp").toString()
                        qrHistoryModel.add(history)
                    }
                }
            }
    }
}