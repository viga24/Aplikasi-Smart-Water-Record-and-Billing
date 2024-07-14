package com.example.sistemmonitoringairpdam

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.BillingActivity.Data
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoriAirActivity : AppCompatActivity() {
    private val calendar = Calendar.getInstance()
    private lateinit var tvTglAwal: TextView
    private lateinit var tvTglAkhir: TextView
    private lateinit var tvTotalAir: TextView
    private lateinit var tvTotalHargaAir: TextView
    private lateinit var btnHitung: Button
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_histori_air)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvTglAwal = findViewById(R.id.tvTglAwal)
        tvTglAkhir = findViewById(R.id.tvTglAkhir)
        tvTotalAir = findViewById(R.id.tvTotalAir)
        tvTotalHargaAir = findViewById(R.id.tvTotalHargaAir)
        btnHitung = findViewById(R.id.btn_hitung)

        tvTglAwal.setOnClickListener {
            showDatePickerAwal()
        }

        tvTglAkhir.setOnClickListener {
            showDatePickerAkhir()
        }
        btnHitung.setOnClickListener {
            hitungAir()
        }


    }

    private fun hitungAir(){
        val collectionRef = db.collection("Record_air")
        val idKamar = intent.getStringExtra("idKamar")
        println(idKamar)

        val localeID = Locale("in", "ID")
        val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)

        collectionRef
            .whereEqualTo("id_kamar", idKamar?.toInt())
            .whereGreaterThanOrEqualTo("hari", tvTglAwal.text.toString() + " 00:00:00")
            .whereLessThanOrEqualTo("hari", tvTglAkhir.text.toString() + " 23:59:59" ).get()
            .addOnSuccessListener { result ->
                var total = 0.0
                for (document: QueryDocumentSnapshot in result) {
                    val air: Double? = document.getDouble("air")
                    if(air != null)
                        total += air
                }
//                tvTotalAir.text = "$total L"
                val hargaPerValue = 0.2
                tvTotalHargaAir.text = "${formatRupiah.format(total * hargaPerValue)}"

                val localeID = Locale("in", "ID")
                val formatLiter: DecimalFormat = (NumberFormat.getCurrencyInstance(localeID) as DecimalFormat)
                val symbols = DecimalFormatSymbols(localeID)
                symbols.currencySymbol = "" // Menghilangkan prefix mata uang
                symbols.monetaryDecimalSeparator = ','
                symbols.groupingSeparator = '.'

                formatLiter.decimalFormatSymbols = symbols

                tvTotalAir.text = "${formatLiter.format(total)} L"

            } .addOnFailureListener { exception ->
                println("Gagal mengambil data: $exception")
            }

    }

    private fun showDatePickerAwal() {
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                tvTglAwal.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }
    private fun showDatePickerAkhir() {
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            this, {DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                tvTglAkhir.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }
}