package com.example.bloodcare.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodcare.R
import com.example.bloodcare.ViewProfileActivity // ✅ ভিউ প্রোফাইল অ্যাক্টিভিটি ইম্পোর্ট
import com.example.bloodcare.model.UserModel

class DonorAdapter(private val context: Context, private val userList: ArrayList<UserModel>) :
    RecyclerView.Adapter<DonorAdapter.DonorViewHolder>() {

    class DonorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivDonorImage)
        val tvName: TextView = itemView.findViewById(R.id.tvDonorName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvDonorPhone)
        val tvBadge: TextView = itemView.findViewById(R.id.tvDonorBadge)
        val tvBloodGroup: TextView = itemView.findViewById(R.id.tvDonorBloodGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donor_card, parent, false)
        return DonorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonorViewHolder, position: Int) {
        val user = userList[position]

        holder.tvName.text = user.name
        holder.tvPhone.text = user.mobile
        holder.tvBloodGroup.text = user.bloodGroup

        // ব্যাজ ক্যালকুলেশন
        val badgeName = when {
            user.totalDonations >= 50 -> "Red Guardian"
            user.totalDonations >= 20 -> "Blood Hero"
            user.totalDonations >= 10 -> "Hope Giver"
            user.totalDonations >= 5 -> "Life Saver"
            user.totalDonations >= 1 -> "First Drop"
            else -> "Newbie"
        }
        holder.tvBadge.text = badgeName

        // ইমেজ লোড
        if (user.profileImage != null && user.profileImage != "null" && user.profileImage.isNotEmpty()) {
            Glide.with(context)
                .load(user.profileImage)
                .placeholder(R.drawable.person1)
                .into(holder.ivImage)
        }

        // ✅ কার্ডে ক্লিক করলে ViewProfileActivity তে নিয়ে যাবে
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ViewProfileActivity::class.java)
            // ইউজারের আইডি পাঠানো হচ্ছে যাতে প্রোফাইল লোড করা যায়
            intent.putExtra("targetUserId", user.userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}