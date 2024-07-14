package com.example.sistemmonitoringairpdam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val historyList : ArrayList<History>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = historyList[position]

        holder.transaksi.text = currentitem.transaksi.toString()
        holder.bulan.text = currentitem.bulan
        holder.status.text = currentitem.status.toString()
        holder.idKamar.text = currentitem.id_kamar.toString()

    }

    override fun getItemCount(): Int {

        return historyList.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val transaksi : TextView = itemView.findViewById(R.id.transaksi)
        val bulan : TextView = itemView.findViewById(R.id.bulan)
        val status : TextView = itemView.findViewById(R.id.status)
        val idKamar : TextView = itemView.findViewById(R.id.kamar)

    }

}