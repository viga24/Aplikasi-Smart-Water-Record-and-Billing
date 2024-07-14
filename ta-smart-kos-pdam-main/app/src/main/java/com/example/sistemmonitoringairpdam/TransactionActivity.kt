package com.example.sistemmonitoringairpdam

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityMonitoringBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityTransactionBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import org.w3c.dom.Text
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private var _binding: ActivityTransactionBinding? = null
    private val binding get() = _binding!!

    lateinit var kamar1: Button
    lateinit var kamar2: Button
    lateinit var buttonUserProfile: ImageView
    lateinit var totalsemua: TextView
    lateinit var totalkamar1: TextView
    lateinit var totalkamar2: TextView
    lateinit var transaksi : ImageView
    lateinit var histori : TextView

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        buttonUserProfile = findViewById<ImageView>(R.id.userPorifle)
        buttonUserProfile.setOnClickListener {
            editor.clear()
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        histori = findViewById<TextView>(R.id.history)
        histori.setOnClickListener{
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        transaksi = findViewById<ImageView>(R.id.requestTransaksi)
        transaksi.setOnClickListener{
            val intent = Intent(this, RequestActivity::class.java)
            startActivity(intent)
        }

        kamar1 = findViewById<Button>(R.id.kamar1)
        kamar1.setOnClickListener {
            val intent = Intent(this, HistoriAirActivity::class.java)
            intent.putExtra("idKamar", "1")
            startActivity(intent)
        }

        kamar2 = findViewById<Button>(R.id.kamar2)
        kamar2.setOnClickListener {
            val intent = Intent(this, HistoriAirActivity::class.java)
            intent.putExtra("idKamar", "2")
            startActivity(intent)
        }

        val collectionRef = db.collection("Record_air")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time
        val lastDayOfMonths = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonths)
        val lastDayOfMonth = calendar.time
//        val db = FirebaseFirestore.getInstance()

//        collectionRef.whereNotEqualTo("hari", "NULL")
//            .whereGreaterThanOrEqualTo("hari", firstDayOfMonth)
//            .whereLessThanOrEqualTo("hari", lastDayOfMonth)
//            .whereEqualTo("status", 0)
//            .orderBy("hari")
//        collectionRef.orderBy("status").get()

        var literkamar1: Double = 0.0
        var literkamar2: Double = 0.0

        collectionRef
            .whereGreaterThanOrEqualTo("hari", "2024-06-01 00:00:00")
            .whereLessThanOrEqualTo("hari", "2024-06-30 23:59:59" )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(ContentValues.TAG, "Error getting documents: ", error)
                    return@addSnapshotListener
                }
                val data = ArrayList<String>()
                for (document: QueryDocumentSnapshot in snapshot!!) {
                    val field1: Double? = document.getDouble("air")
                    val field2 = document.getDouble("id_kamar")?.toString() ?: ""
//                    val field3 = document.getDate("hari")
                    val field3 = " "
                    val record = "$field1, $field2 ,$field3"
                    data.add(record)

                    if (field2 == "1.0"){
                        if (field1 != null) {
                            literkamar1 += field1
                        }
                    }

                    if (field2 == "2.0"){
                        if (field1 != null) {
                            literkamar2 += field1
                        }
                    }

                }

                var totalValue = 0.0
                data.forEach { entry ->
                    val parts = entry.split(",")
                    val value = parts[0].trim().toDoubleOrNull() ?: 0.0
                    totalValue += value
                }

                val hargaPerValue = 0.2
                val totalRupiah = totalValue * hargaPerValue

                val totalPerIdKamar = mutableMapOf<String, Double>()
                data.forEach { entry ->
                    val parts = entry.split(",")
                    val value = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val idKamar = parts[1].trim()

                    if (totalPerIdKamar.containsKey(idKamar)) {
                        totalPerIdKamar[idKamar] = totalPerIdKamar.getValue(idKamar) + value
                    } else {
                        totalPerIdKamar[idKamar] = value
                    }
                }

                totalPerIdKamar.forEach { (idKamar, totalValue) ->
                    totalPerIdKamar[idKamar] = totalValue * hargaPerValue
                }

                val localeID = Locale("in", "ID")
                val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)

                val formatLiter: DecimalFormat = (NumberFormat.getCurrencyInstance(localeID) as DecimalFormat)
                val symbols = DecimalFormatSymbols(localeID)
                symbols.currencySymbol = "" // Menghilangkan prefix mata uang
                symbols.monetaryDecimalSeparator = ','
                symbols.groupingSeparator = '.'

                formatLiter.decimalFormatSymbols = symbols

                val kamar1: TextView = findViewById(R.id.totalkamar1)
                val totalKamar1 = literkamar1 * hargaPerValue
                kamar1.text = "${formatRupiah.format(totalKamar1)}"

                val tvliterkamar1: TextView = findViewById(R.id.totalliter1)
                tvliterkamar1.text = "${formatLiter.format(literkamar1)} L"

                val kamar2: TextView = findViewById(R.id.totalkamar2)
                val totalKamar2 = literkamar2 * hargaPerValue
                kamar2.text = "${formatRupiah.format(totalKamar2)}"

                val tvliterkamar2: TextView = findViewById(R.id.totalliter2)
                tvliterkamar2.text = "${formatLiter.format(literkamar2)} L"

                val totalsemua: TextView = findViewById(R.id.totalsemua)
                totalsemua.text = "Total Semua: ${formatRupiah.format(totalRupiah)}"


            }

    }

    override fun onBackPressed() {
        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
        super.onBackPressed()
        finishAffinity()
    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}