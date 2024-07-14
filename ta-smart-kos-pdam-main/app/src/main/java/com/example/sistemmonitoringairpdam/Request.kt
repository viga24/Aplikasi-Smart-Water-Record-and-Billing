package com.example.sistemmonitoringairpdam

data class Request(
    var transaksi : String ?= null,
    var bulan : String ?= null,
    var id_kamar : String ?= null,
    var status: Int? = null,
    var docId: String? = null
)