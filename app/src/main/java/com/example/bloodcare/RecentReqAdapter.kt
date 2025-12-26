package com.example.bloodcare.adapter // আপনার প্যাকেজ নাম

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodcare.R
import com.example.bloodcare.model.BloodRequestModel

class RecentReqAdapter(private val postList: List<BloodRequestModel>) :
    RecyclerView.Adapter<RecentReqAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBloodGroup: TextView = itemView.findViewById(R.id.tvBloodGroup)
        val tvPostTitle: TextView = itemView.findViewById(R.id.tvPostTitle)
        val tvHospitalName: TextView = itemView.findViewById(R.id.tvHospitalName)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_request, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = postList[position]

        holder.tvBloodGroup.text = currentItem.bloodGroup
        holder.tvPostTitle.text = currentItem.title
        holder.tvHospitalName.text = currentItem.hospitalName
        holder.tvDate.text = currentItem.date
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}