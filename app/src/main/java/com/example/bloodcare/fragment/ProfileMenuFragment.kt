package com.example.bloodcare.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bloodcare.MainActivity
import com.example.bloodcare.Profile
import com.example.bloodcare.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileMenuFragment : Fragment() {

    // ভিউ ভেরিয়েবল ডিক্লেয়ারেশন
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvBloodGroup: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_menu, container, false)

        // ১. ভিউ গুলো খুঁজে বের করা (XML এর ID অনুযায়ী)
        // নোট: আপনার আগের XML কোড অনুযায়ী ID গুলো এখানে দেওয়া হলো
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        tvName = view.findViewById(R.id.tvName)
        tvBloodGroup = view.findViewById(R.id.textView) // আপনার XML এ ব্লাড গ্রুপের ID ছিল 'textView'

        // ২. Firebase থেকে ডেটা লোড করার ফাংশন কল
        loadUserData()

        // Close button logic
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseMenu)
        btnClose.setOnClickListener {
            (activity as MainActivity).closeMenu()
        }

        // Edit Profile button logic
        val editIcon = view.findViewById<ImageView>(R.id.imageView2)
        editIcon.setOnClickListener {
            val intent = Intent(activity, Profile::class.java)
            startActivity(intent)
        }

        return view
    }

    // ৩. ইউজারের তথ্য লোড করার ফাংশন
    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && isAdded) { // isAdded চেক করা জরুরি যাতে ক্র্যাশ না করে

                        // ডেটা আনা
                        val name = snapshot.child("name").value.toString()
                        val bloodGroup = snapshot.child("bloodGroup").value.toString()
                        val profileImageUrl = snapshot.child("profileImage").value.toString()

                        // নাম এবং ব্লাড গ্রুপ সেট করা
                        tvName.text = name
                        tvBloodGroup.text = "Blood Group: $bloodGroup"

                        // Glide দিয়ে ছবি লোড করা
                        // Cloudinary বা Firebase যে কোনো URL এই কোড হ্যান্ডেল করতে পারবে
                        if (profileImageUrl.isNotEmpty() && profileImageUrl != "null") {
                            Glide.with(requireContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.person1) // লোড হওয়ার আগে ডিফল্ট ছবি
                                .error(R.drawable.person1)       // এরর হলে ডিফল্ট ছবি
                                .into(ivProfileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) {
                        Toast.makeText(context, "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}