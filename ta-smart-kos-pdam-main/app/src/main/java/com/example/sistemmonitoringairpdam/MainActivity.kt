package com.example.sistemmonitoringairpdam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import com.example.sistemmonitoringairpdam.databinding.ActivityMainBinding

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.mikephil.charting.charts.BarChart
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    lateinit var barChart: BarChart

    lateinit var buttonMonitor: Button
    lateinit var buttonBilling: Button
    lateinit var buttonUserProfile: ImageView

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayOfMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date // Setel tanggal pada objek Calendar
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        createNotificationChannels()
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)
        val doubleValueKamar: Double = idKamar.toDouble()

        // Melakukan sesuatu dengan data yang Anda dapatkan
//        if (idKamar != null) {
        // Lakukan sesuatu dengan data yang diterima dari Intent
        // Contoh: Menampilkan data di TextView
//            Toast.makeText(
//                applicationContext,
//                idKamar.toString(),
//                Toast.LENGTH_SHORT
//            )
//                .show()
//        }

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonUserProfile = findViewById<ImageView>(R.id.userPorifle)
        buttonUserProfile.setOnClickListener {
            editor.clear()
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        buttonMonitor = findViewById<Button>(R.id.monitor)
        buttonMonitor.setOnClickListener {
            val intent = Intent(this, HistoriAirActivity::class.java)
            intent.putExtra("idKamar", idKamar.toString())
            startActivity(intent)
        }

        buttonBilling = findViewById<Button>(R.id.billing)
        buttonBilling.setOnClickListener {
            val intent = Intent(this, BillingActivity::class.java)
            startActivity(intent)
        }


        val calendar = Calendar.getInstance()
        val bulanSaatIni =
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
//        val textView: TextView = findViewById(R.id.bulan)
//        textView.text = bulanSaatIni
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time
        val lastDayOfMonths = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonths)
        val lastDayOfMonth = calendar.time
        val db = FirebaseFirestore.getInstance()

        val collectionRef = db.collection("Record_air")
        val mutableList = mutableListOf<Pair<String, Float>>()

        collectionRef.orderBy("hari", Query.Direction.DESCENDING).limit(1)
            .whereEqualTo("id_kamar", idKamar)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    var shouldShowNotification = false
                    var airValueNotification: Double? = null
                    var kamarIDNotification: Int? = null

                    for (doc in snapshot.documents) {
                        val airValue = doc.getDouble("air")
                        val kamarID = doc.getDouble("id_kamar")?.toInt()
                        if (airValue != null && airValue >= 50 && kamarID != null ) {
                            shouldShowNotification = true
                            airValueNotification = airValue
                            kamarIDNotification = kamarID
                            break
                        }
                    }

                    // Periksa apakah notifikasi sudah ditampilkan sebelumnya
                    val notificationShownBefore = sharedPreferences.getBoolean("notificationShown", false)

                    if (shouldShowNotification && !notificationShownBefore) {
                        val notificationManager = NotificationManagerCompat.from(this)
                        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

                        val title: String = "NOTIFIKASI AIR !!!"
                        val message = "Air sudah penuh pada kamar $kamarIDNotification dan air $airValueNotification"
                        val builder = NotificationCompat.Builder(this, CHANNEL_1_ID)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                            .setContentIntent(pendingIntent)

                        val notification = builder.build()
                        notificationManager.notify(1, notification)

                        // Menandai bahwa notifikasi telah ditampilkan
                        val editor = sharedPreferences.edit()
                        editor.putBoolean("notificationShown", true)
                        editor.apply()
                    }
                } else {
                    Log.d(TAG, "Current data: null")
                }
        }
        // Mengambil data dari Firestore
//        collectionRef
//            .whereGreaterThanOrEqualTo("hari", firstDayOfMonth)
//            .whereLessThanOrEqualTo("hari", lastDayOfMonth)
//            .get()
//            .addOnSuccessListener { result ->
//                val data = ArrayList<String>()
//                for (document: QueryDocumentSnapshot in result) {
//                    val field1: Double? = document.getDouble("air")
//                    val doubleValue: Double = field1?.toDouble() ?: 0.0
//                    val field1Float: Float = doubleValue.toFloat()
//                    val field2 = document.getDouble("id_kamar")
//                    val field3 = document.getDate("hari")
//                    val label = getDayOfMonth(field3 as Date).toString()
//                    val record = "$field1, $field2 ,$field3"
//                    data.add(record)
//                    if (field2 != null) {
//                        if (field2.equals(doubleValueKamar)) {
//                            mutableList.add(label to field1Float)
//                        }
//                    }
//                }
//                binding.apply {
//                    barChart.animation.duration = animationDuration
//                    barChart.animate(mutableList)
//                }
//                println(mutableList)
//            }
//            .addOnFailureListener { exception ->
//                println("Gagal mengambil data: $exception")
//            }
    }

    companion object {
        private const val animationDuration = 1000L
        const val CHANNEL_1_ID = " channel1"
    }

    override fun onBackPressed() {
        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
        super.onBackPressed()
        finishAffinity()
    }

    private fun createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                "Channel One",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel1.description = "This Channel 1"

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel1)
        }
    }
}