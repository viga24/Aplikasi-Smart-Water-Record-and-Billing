package com.example.sistemmonitoringairpdam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapterRequest(private val requestList: ArrayList<Request>) :
    RecyclerView.Adapter<MyAdapterRequest.MyViewHolder>() {

    private var itemClickListener: RequestItemClickListener? = null

    // Define interface for item click listener
    interface RequestItemClickListener {
        fun onCheckButtonClick(position: Int)
        fun onCrossButtonClick(position: Int)
    }

    // Set item click listener
    fun setItemClickListener(listener: RequestItemClickListener) {
        itemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = requestList[position]

        holder.transaksi.text = currentItem.transaksi
        holder.bulan.text = currentItem.bulan
        holder.idKamar.text = currentItem.id_kamar

        // Set click listener for check_button
        holder.checkButton.setOnClickListener {
            itemClickListener?.onCheckButtonClick(position)
        }
        holder.crossButton.setOnClickListener {
            itemClickListener?.onCrossButtonClick(position)
        }
    }

    override fun getItemCount(): Int {
        return requestList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transaksi: TextView = itemView.findViewById(R.id.transaksi)
        val bulan: TextView = itemView.findViewById(R.id.bulan)
        val idKamar: TextView = itemView.findViewById(R.id.kamar)
        val checkButton: ImageView = itemView.findViewById(R.id.check_button)
        val crossButton: ImageView = itemView.findViewById(R.id.cross_button)
    }
}
