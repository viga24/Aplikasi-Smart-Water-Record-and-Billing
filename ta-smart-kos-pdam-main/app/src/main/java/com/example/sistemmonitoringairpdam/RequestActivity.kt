package com.example.sistemmonitoringairpdam

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class RequestActivity : AppCompatActivity(), MyAdapterRequest.RequestItemClickListener {

    private lateinit var requestRecyclerview: RecyclerView
    private lateinit var requestArrayList: ArrayList<Request>
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MyAdapterRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        requestRecyclerview = findViewById(R.id.request_view)
        requestRecyclerview.layoutManager = LinearLayoutManager(this)
        requestRecyclerview.setHasFixedSize(true)

        requestArrayList = arrayListOf()
        adapter = MyAdapterRequest(requestArrayList)
        adapter.setItemClickListener(this)
        requestRecyclerview.adapter = adapter

        getUserData()
    }

    private fun getUserData() {
        db.collection("Transaksi_air")
            .whereEqualTo("status", 1)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Process data as before
                    val bulan = document.getString("bulan") ?: ""
                    val kamar = document.getLong("id_kamar")?.toInt() ?: 0
                    val transaksi = document.getDouble("transaksi") ?: 0.0

                    val kamarText = "Kamar $kamar"
                    val bulanText = "Bulan $bulan"
                    val transaksiText = "Rp ${String.format("%.2f", transaksi)}"

                    val request = Request(transaksiText, bulanText, kamarText)
                    request.docId = document.id

                    requestArrayList.add(request)
                }
                adapter.notifyDataSetChanged() // Notify adapter after data is loaded
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


    override fun onCheckButtonClick(position: Int) {
        val request = requestArrayList[position]
        // Assuming you have the document ID
        val docId = request.docId

        val idKamar = request.id_kamar

        val angka = idKamar?.substringAfter(" ")?.toIntOrNull()
        updateStatus(angka)
        db.collection("Transaksi_air").document(docId.toString())
            .update("status", 2)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                // Update local data if needed
                request.status = 2
                // Update RecyclerView view
                requestRecyclerview.adapter?.notifyItemChanged(position)
                // Start HistoryActivity
                val intent = Intent(this, TransactionActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }

    }

    override fun onCrossButtonClick(position: Int) {
        val request = requestArrayList[position]
        val docId = request.docId

        db.collection("Transaksi_air").document(docId.toString())
            .update("status", 3) // Update status to 3
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
                // Update local data if needed
                request.status = 3
                // Update RecyclerView view
                requestRecyclerview.adapter?.notifyItemChanged(position)
                // Start HistoryActivity
                val intent = Intent(this, TransactionActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
    }

    fun updateStatus(idKamar: Int?) {
        db.collection("Record_air")
            .whereEqualTo("status", 0)
            .whereEqualTo("id_kamar", idKamar )
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val docId = document.id
                    db.collection("Record_air").document(docId)
                        .update("status", 1)
                        .addOnSuccessListener {
                            Log.d(TAG, "update status")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error updating document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


}
