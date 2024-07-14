package com.example.sistemmonitoringairpdam

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var historyRecyclerview: RecyclerView
    private lateinit var historyArrayList: ArrayList<History>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)

        // Inisialisasi Firestore
        db = FirebaseFirestore.getInstance()

        historyRecyclerview = findViewById(R.id.recyler_view)
        historyRecyclerview.layoutManager = LinearLayoutManager(this)
        historyRecyclerview.setHasFixedSize(true)

        historyArrayList = arrayListOf()
        getUserData()
    }

    private fun getUserData() {
        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)

        if (idKamar == 1 || idKamar == 2) {
            db.collection("Transaksi_air")
                .whereEqualTo("id_kamar", idKamar)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Error getting documents: ", error)
                        return@addSnapshotListener
                    }

                    historyArrayList.clear()

                    for (document in snapshot!!) {
                        val bulan = document.getString("bulan") ?: ""
                        val kamar = document.getLong("id_kamar")?.toInt() ?: 0
                        val transaksi = document.getDouble("transaksi") ?: 0.0
                        val status = document.getLong("status")?.toInt() ?: 0
                        var statusName = "  "

                        if (status == 1) {
                            statusName = "Pending "
                        } else if (status == 2) {
                            statusName = "Berhasil "
                        } else {
                            statusName = "Gagal "
                        }

                        // Format data untuk ditampilkan di RecyclerView
                        val kamarText = "Kamar $kamar"
                        val bulanText = "Bulan $bulan"
                        val statusText = "Status $statusName"
                        val transaksiText = "Rp ${String.format("%.2f", transaksi)}"

                        // Buat objek History dan tambahkan ke dalam ArrayList
                        val history = History(transaksiText, bulanText, kamarText, statusText)
                        historyArrayList.add(history)
                    }

                    historyRecyclerview.adapter = MyAdapter(historyArrayList)
                }
        } else {
        }
        db.collection("Transaksi_air")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error getting documents: ", error)
                    return@addSnapshotListener
                }

                historyArrayList.clear()

                for (document in snapshot!!) {
                    // Proses data seperti yang dilakukan sebelumnya
                    val bulan = document.getString("bulan") ?: ""
                    val kamar = document.getLong("id_kamar")?.toInt() ?: 0
                    val transaksi = document.getDouble("transaksi") ?: 0.0
                    val status = document.getLong("status")?.toInt() ?: 0
                    var statusName = "  "

                    if (status == 1) {
                        statusName = "Pending "
                    } else if (status == 2) {
                        statusName = "Berhasil "
                    } else {
                        statusName = "Gagal "
                    }

                    val kamarText = "Kamar $kamar"
                    val bulanText = "Bulan $bulan"
                    val statusText = "Status $statusName"
                    val transaksiText = "Rp ${String.format("%.2f", transaksi)}"

                    val history = History(transaksiText, bulanText, kamarText, statusText)
                    historyArrayList.add(history)
                }

                historyRecyclerview.adapter = MyAdapter(historyArrayList)
            }
    }
}
