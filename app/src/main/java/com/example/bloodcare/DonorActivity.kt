package com.example.bloodcare

import android.os.Bundle
import android.util.Log // Import Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bloodcare.adapter.DonorAdapter
import com.example.bloodcare.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DonorActivity : AppCompatActivity() {

    private lateinit var rvDonorList: RecyclerView
    private lateinit var donorList: ArrayList<UserModel>
    private lateinit var adapter: DonorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donor)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        rvDonorList = findViewById(R.id.rvDonorList)
        rvDonorList.layoutManager = LinearLayoutManager(this)
        rvDonorList.setHasFixedSize(true)

        donorList = arrayListOf()
        adapter = DonorAdapter(this, donorList)
        rvDonorList.adapter = adapter

        fetchAvailableDonors()
    }

    private fun fetchAvailableDonors() {
        val ref = FirebaseDatabase.getInstance().getReference("users")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                donorList.clear()

                // Debug Log: Check if data is coming
                Log.d("DonorCheck", "Total Users Found: ${snapshot.childrenCount}")

                for (userSnap in snapshot.children) {
                    try {
                        val user = userSnap.getValue(UserModel::class.java)

                        // Check boolean explicitly using Boolean.TRUE to handle nulls safely
                        val isAvailable = userSnap.child("isAvailable").getValue(Boolean::class.java) == true

                        if (user != null && isAvailable && userSnap.key != currentUserId) {
                            // Manual Mapping just to be safe
                            val userWithId = user.copy(
                                userId = userSnap.key,
                                isAvailable = true // Explicitly setting it
                            )
                            donorList.add(userWithId)
                        }
                    } catch (e: Exception) {
                        Log.e("DonorCheck", "Error parsing user: ${e.message}")
                    }
                }

                adapter.notifyDataSetChanged()

                // Only show toast if the list is actually empty AND the activity is not finishing
                if (donorList.isEmpty() && !isFinishing) {
                    Toast.makeText(this@DonorActivity, "No donors available right now", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DonorActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}