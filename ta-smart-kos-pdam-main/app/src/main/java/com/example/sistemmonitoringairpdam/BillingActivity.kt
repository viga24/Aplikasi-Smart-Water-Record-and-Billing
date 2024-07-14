package com.example.sistemmonitoringairpdam

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.sistemmonitoringairpdam.databinding.ActivityBillingBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import java.text.DateFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BillingActivity : AppCompatActivity() , TransactionFinishedCallback {

    private var _binding: ActivityBillingBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    lateinit var monthly: CardView
    lateinit var weekly: CardView
    lateinit var daily: CardView
    lateinit var cashless : Button
    lateinit var cash : Button
    lateinit var histori : Button
    lateinit var historiAir : Button

    private var totalKamar: Double = 0.0

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayOfMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date // Setel tanggal pada objek Calendar
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    data class Data(val value: Double, val roomId: Double, val date: Date)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val collectionRef = db.collection("Record_air")

        collectionRef
            .whereGreaterThanOrEqualTo("hari", "2024-06-01 00:00:00")
            .whereLessThanOrEqualTo("hari", "2024-06-30 23:59:59" )
            .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(ContentValues.TAG, "Error getting documents: ", error)
                return@addSnapshotListener
            }
            val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
            val valueReceived = intent.getIntExtra("var", 0)

            val idKamar: Int = sharedPreferences.getInt("idKamar", 0)


            val mutableListBulan = mutableListOf<Pair<String, Float>>()
            val mutableListHari = mutableListOf<Pair<String, Float>>()
            val mutableListMinggu = mutableListOf<Pair<String, Float>>()

            var literkamar1: Double = 0.0
            var literkamar2: Double = 0.0

            daily = findViewById<CardView>(R.id.dailybutton)
            daily.setOnClickListener {
                val valueToSend = 1
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }
            weekly = findViewById<CardView>(R.id.weekly)
            weekly.setOnClickListener {
                val valueToSend = 2
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }
            monthly = findViewById<CardView>(R.id.monthly)
            monthly.setOnClickListener {
                val valueToSend = 3
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }

            val data = ArrayList<String>()
                for (document: QueryDocumentSnapshot in snapshot!!) {
                    val field1: Double? = document.getDouble("air")
                    val field2 = document.getDouble("id_kamar")?.toString() ?: ""
                    val field3 = document.getString("hari")
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
                val hargaPerValue = 0.2

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

                if(idKamar == 1){
                    val kamar1: TextView = findViewById(R.id.totalkamar)
                    totalKamar = literkamar1 * hargaPerValue
                    kamar1.text = "${formatRupiah.format(totalKamar)}"
                } else   {
                    val kamar2: TextView = findViewById(R.id.totalkamar)
                    totalKamar = literkamar2 * hargaPerValue
                    kamar2.text = "${formatRupiah.format(totalKamar)}"
                }

                val formattedData = data.map {
                    val parts = it.split(",")

                    val format = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
                    var date = Date(0)
                    try {
                        date = format.parse(parts[2].trim())!!
                        println("Parsed date: $date") // Output: Parsed date: Sat Jun 22 16:18:00 EDT 2024 (assuming your system's time zone is EDT)
                    } catch (ignored: Exception) {

                    }


                    Data(
                        parts[0].trim().toDouble(), // nilai
                        parts[1].trim().toDouble(), // id kamar
                        date // tanggal
                    )
                }
            collectionRef
                .whereGreaterThanOrEqualTo("hari", "2024-06-01 00:00:00")
                .whereLessThanOrEqualTo("hari", "2024-06-30 23:59:59" )
                .whereEqualTo("id_kamar", idKamar).get()
                .addOnSuccessListener { result ->
                    val data = ArrayList<String>()
                    for (document: QueryDocumentSnapshot in result) {
                        val field1: Double? = document.getDouble("air")
                        val field2 = document.getDouble("id_kamar")?.toString() ?: ""
                        val field3 = document.getString("hari")
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

                    val formattedData = data.map {
                        val parts = it.split(",")
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        val date = formatter.parse(parts[2])


                        Data(
                            parts[0].trim().toDouble(), // nilai
                            parts[1].trim().toDouble(), // id kamar
                            date // tanggal
                        )
                    }


                    val calendar = Calendar.getInstance()
                    val firstDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
                    val lastDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) }.time

// Mendapatkan bulan pertama yang ada dalam data
                    val firstMonth =
                        formattedData.minByOrNull { it.date }?.let { getMonthNumber(it.date) }
// Mendapatkan bulan saat ini
                    val currentMonth =
                        Calendar.getInstance().get(Calendar.MONTH) + 1 // Mendapatkan bulan saat ini

                    val monthlyGroupedData = formattedData.groupBy { getMonthNumber(it.date) }
                    val dailyGroupedDataThisMonth = formattedData
                        .filter { it.date in firstDayOfMonth..lastDayOfMonth }
                        .groupBy { getDateKey(it.date) }
                    val weeklyGroupedDataThisMonth = formattedData
                        .filter { it.date in firstDayOfMonth..lastDayOfMonth }
                        .groupBy { getWeekInMonth(it.date) }


                    val monthlyResult = mutableMapOf<String, Double>()
                    val dailyResultThisMonth = mutableMapOf<String, Double>()
                    val weeklyResultThisMonth = mutableMapOf<Int, Double>()

// Menginisialisasi nilai total untuk setiap bulan yang mungkin ada dalam rentang data
                    for (i in 1..currentMonth) {
                        val month = if (firstMonth != null && i == 1) firstMonth else getMonthNumber(
                            getDateFromMonthAndYear(i, Calendar.getInstance().get(Calendar.YEAR))
                        )
                        monthlyResult[month] = 0.0
                    }

// Menghitung total nilai per bulan
                    monthlyGroupedData.forEach { (month, dataList) ->
                        val totalValue = dataList.sumByDouble { it.value }
                        monthlyResult[month] = totalValue
                    }


// Menghitung total nilai per hari
                    dailyGroupedDataThisMonth.forEach { (date, dataList) ->
                        val totalValue = dataList.sumByDouble { it.value }
                        dailyResultThisMonth[date] = totalValue
                    }

// Menghitung total nilai per minggu
                    weeklyGroupedDataThisMonth.forEach { (week, dataList) ->
                        val totalValue = dataList.sumByDouble { it.value }
                        weeklyResultThisMonth[week.toInt()] = totalValue
                    }


                    val sortedMonthlyResult = monthlyResult.toList().sortedBy { it.first.toInt() }
                    val sortedDailyResultThisMonth = dailyResultThisMonth.toList().sortedBy { it.first }
                    val sortedWeeklyResultThisMonth = weeklyResultThisMonth.toList().sortedBy { it.first }

                    sortedMonthlyResult.forEach { (month, totalValue) ->
                        val formattedTotalValue =
                            String.format("%.2f", totalValue).replace(",", ".").toFloat()
                        mutableListBulan.add(month to formattedTotalValue)
                    }

                    sortedDailyResultThisMonth.forEach { (date, totalValue) ->
                        val formattedDate = SimpleDateFormat("dd", Locale.US).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date))
                        val formattedTotalValue = String.format("%.2f", totalValue).replace(",", ".").toFloat()
                        mutableListHari.add(formattedDate to formattedTotalValue)
                    }


                    sortedWeeklyResultThisMonth.forEach { (week, totalValue) ->
                        val formattedTotalValue = String.format("%.2f", totalValue).replace(",", ".").toFloat()
                        mutableListMinggu.add("Week $week" to formattedTotalValue)
                    }

                    if (mutableListHari.size == 1) {
                        for (i in (mutableListHari.last().first.toString().toInt() - 5) until mutableListHari.last().first.toInt()) {
                            mutableListHari.add(1,i.toString() to 0.0.toFloat())
                        }
                    }
                    if (mutableListMinggu.size == 1) {
                        for (i in ( mutableListMinggu.last().first.toString().last().toString().toInt() - 3) until mutableListMinggu.last().first.toString().last().toString().toInt()) {
                            mutableListMinggu.add(1,"Week $i" to 0.0.toFloat())
                        }
                    }

                    binding.apply {
                        barChart.animation.duration = BillingActivity.animationDuration
                        barChart.labelsFormatter = { value -> String.format("%.1f", value) }

                        when (valueReceived) {
                            1 -> {
                                barChart.animate(mutableListHari)
                                labelbawah.text = "Tanggal"
                            }
                            2 -> {
                                barChart.animate(mutableListMinggu)
                                labelbawah.text = "Minggu"
                            }
                            3 -> {
                                barChart.animate(mutableListBulan)
                                labelbawah.text = "Bulan"
                            }
                            else -> {
                                // Jika nilai yang diterima tidak sesuai dengan 1, 2, atau 3, maka animasikan dengan mutableListBulan
                                barChart.animate(mutableListBulan)
                            }
                        }
                    }


                }.addOnFailureListener { exception ->
                    println("Gagal mengambil data: $exception")
                }

            daily = findViewById<CardView>(R.id.dailybutton)
            daily.setOnClickListener {
                val valueToSend = 1
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }
            weekly = findViewById<CardView>(R.id.weekly)
            weekly.setOnClickListener {
                val valueToSend = 2
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }
            monthly = findViewById<CardView>(R.id.monthly)
            monthly.setOnClickListener {
                val valueToSend = 3
                val intent = Intent(this, BillingActivity::class.java)
                intent.putExtra("var", valueToSend)
                startActivity(intent)
            }

            initMidtransSDK()

            cashless = findViewById( R.id.btn_e_wallet)
            cashless.setOnClickListener {
                if (totalKamar > 0.0) {
                    goToPayment(idKamar)
                } else {
                    // Tampilkan pesan bahwa totalKamar adalah 0.0
                    showToast("Tidak ada tagihan yang harus dibayar.")
                }
            }

            cash = findViewById( R.id.btn_cash)
            cash.setOnClickListener {
                if (totalKamar > 0.0) {
                    showConfirmationDialog()
                } else {
                    // Tampilkan pesan bahwa totalKamar adalah 0.0
                    showToast("Tidak ada tagihan yang harus dibayar.")
                }
            }

            histori = findViewById( R.id.btn_history)
            histori.setOnClickListener{
                val intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
            }

            historiAir = findViewById( R.id.btn_history_air)
            historiAir.setOnClickListener{
                val intent = Intent(this, HistoriAirActivity::class.java)
                startActivity(intent)
            }
        }


    }

    fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi")
        builder.setMessage("Apakah Anda ingin melanjutkan proses?")

        // Jika pengguna memilih "Ya", lanjutkan dengan proses
        builder.setPositiveButton("Lanjutkan") { dialog, which ->
            // Panggil fungsi untuk menambahkan data transaksi
            updateStatus()
        }

        // Jika pengguna memilih "Tidak", batalkan proses
        builder.setNegativeButton("Batal") { dialog, which ->
            // Tidak perlu melakukan apa-apa karena pengguna membatalkan proses
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    fun updateStatus() {
        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()
        val bulanSaatIni = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)

        // Setelah status berhasil diupdate, tambahkan data baru ke tabel Transaksi_air
        val transaksiAirRef = db.collection("Transaksi_air")
        val transaksiData = hashMapOf(
            "transaksi" to totalKamar,
            "bulan" to (bulanSaatIni?.plus(" (cash)") ?: ""),
            "status" to 1,
            "id_kamar" to idKamar
        )
        transaksiAirRef.add(transaksiData)
            .addOnSuccessListener {
                showToast("Data transaksi berhasil ditambahkan")
            }
            .addOnFailureListener { e ->
                showToast("Error adding transaksi data: $e")
            }
    }


    // Fungsi untuk menampilkan pesan toast
    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun initMidtransSDK() {
        val sdkUIFlowBuilder: SdkUIFlowBuilder = SdkUIFlowBuilder.init()
            .setClientKey("SB-Mid-client-wk7DoWv-jUiEr8KE") // client_key
            .setContext(this)
            .setTransactionFinishedCallback(this)
            .setMerchantBaseUrl("https://midtransmonitoringpdam.000webhostapp.com/midtrans.php/") //URL Server
            .enableLog(true)
            .setColorTheme(
                CustomColorTheme(
                    "#002855",
                    "#FF6200EE",
                    "#FF018786"
                )
            )
            .setLanguage("id")
        sdkUIFlowBuilder.buildSDK()
    }

    fun goToPayment(idKamar:Int ) {
        val qty = 1
        val harga = 20000.00
        val idKamar: String = idKamar.toString()

        val calendar = Calendar.getInstance()
        val bulanSaatIni = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())

        val transaksiAirRef = db.collection("Transaksi_air")
        val transaksiData = hashMapOf(
            "transaksi" to totalKamar,
            "bulan" to (bulanSaatIni?.plus(" (ewallet)") ?: ""),
            "status" to 1,
            "id_kamar" to idKamar.toInt()
        )
        transaksiAirRef.add(transaksiData)
            .addOnSuccessListener {
//                showToast("Data transaksi berhasil ditambahkan")
                val transactionRequest =
                    TransactionRequest("Kos Putri Monitoring Air-" + System.currentTimeMillis().toShort(), harga)
                val detail = ItemDetails("Transaksi_101", harga, qty, "kamar "+idKamar)

                val itemDetails = ArrayList<ItemDetails>()
                itemDetails.add(detail)

                uiKitDetails(transactionRequest)
                transactionRequest.itemDetails = itemDetails

                MidtransSDK.getInstance().transactionRequest = transactionRequest
                MidtransSDK.getInstance().startPaymentUiFlow(this)
            }
            .addOnFailureListener { e ->
                showToast("Error adding transaksi data: $e")
            }
    }

    private fun uiKitDetails(transactionRequest: TransactionRequest) {
        val customerDetails = CustomerDetails()
        customerDetails.customerIdentifier = "Asep Sutisna"
        customerDetails.phone = "081775205889"
        customerDetails.firstName = "Asep"
        customerDetails.lastName = "Sutisna"
        customerDetails.email = "yummi2102@gmail.com"

        val shippingAddress = ShippingAddress()
        shippingAddress.address = "cikeas, Nagrak"
        shippingAddress.city = "Bogor"
        shippingAddress.postalCode = "16967"
        customerDetails.shippingAddress = shippingAddress

        val billingAddress = BillingAddress()
        billingAddress.address = "Cikeas, Nagrak"
        billingAddress.city = "Bogor"
        billingAddress.postalCode = "16967"
        customerDetails.billingAddress = billingAddress

        transactionRequest.customerDetails = customerDetails
    }

    fun getWeekInMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }

    fun getMonthName(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val monthNumber = calendar.get(Calendar.MONTH)
        val monthNames = DateFormatSymbols().months
        return monthNames[monthNumber]
    }

    fun getMonthNumber(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return (calendar.get(Calendar.MONTH) + 1).toString()
    }

    fun getDateKey(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    fun getDateFromMonthAndYear(month: Int, year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Bulan dimulai dari 0
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set tanggal ke 1 untuk menghindari kebingungan
        return calendar.time
    }

//    override fun onBackPressed() {
//        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
//    }


    companion object {
        private const val animationDuration = 1000L
    }

    override fun onTransactionFinished(result: TransactionResult) {
        if (result.response != null) {
            when (result.status) {
                TransactionResult.STATUS_SUCCESS -> Toast.makeText(
                    this,
                    "Transaction Finished. ID: " + result.response.transactionId,
                    Toast.LENGTH_LONG
                ).show()
                TransactionResult.STATUS_PENDING -> Toast.makeText(
                    this,
                    "Transaction Pending. ID: " + result.response.transactionId,
                    Toast.LENGTH_LONG
                ).show()
                TransactionResult.STATUS_FAILED -> Toast.makeText(
                    this,
                    "Transaction Failed. ID: " + result.response.transactionId.toString() + ". Message: " + result.response.statusMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (result.isTransactionCanceled) {
            Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_LONG).show()
        } else {
            if (result.status.equals(TransactionResult.STATUS_INVALID, true)) {
                Toast.makeText(this, "Transaction Invalid", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Transaction Finished with failure.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
