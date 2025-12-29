package com.example.bloodcare

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bloodcare.adapter.RecentReqAdapter
import com.example.bloodcare.databinding.ActivityBloodRequestBinding
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BloodRequestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBloodRequestBinding
    private lateinit var requestList: ArrayList<BloodRequestModel>
    private lateinit var adapter: RecentReqAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBloodRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 2. RecyclerView Setup
        binding.rvAllRequests.layoutManager = LinearLayoutManager(this)
        binding.rvAllRequests.setHasFixedSize(true)

        requestList = arrayListOf()
        adapter = RecentReqAdapter(requestList)
        binding.rvAllRequests.adapter = adapter

        // 3. Load Data
        loadAllRequests()
    }

    private fun loadAllRequests() {
        // এখানে আমরা .limitToLast() দিচ্ছি না, কারণ সব পোস্ট দরকার
        val ref = FirebaseDatabase.getInstance().getReference("usersPost")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestList.clear()

                // ডেট চেকিং লজিক (আজকের আগের ডেট বাদ দেওয়া)
                val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                val todayCalendar = Calendar.getInstance()
                // সময় রিসেট
                todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(Calendar.MINUTE, 0)
                todayCalendar.set(Calendar.SECOND, 0)
                todayCalendar.set(Calendar.MILLISECOND, 0)
                val todayDate = todayCalendar.time

                if (snapshot.exists()) {
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(BloodRequestModel::class.java)

                        if (post != null && post.date != null) {
                            try {
                                val postDate = sdf.parse(post.date)

                                // যদি ডেট আজকের সমান বা পরে হয়, তবেই লিস্টে যোগ হবে
                                if (postDate != null && (postDate.after(todayDate) || postDate.equals(todayDate))) {
                                    requestList.add(post)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    // নতুন পোস্ট সবার উপরে দেখানোর জন্য রিভার্স করা হলো
                    requestList.reverse()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BloodRequestActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}