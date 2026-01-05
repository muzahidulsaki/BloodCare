package com.example.bloodcare.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.bloodcare.Login
import com.example.bloodcare.MainActivity
import com.example.bloodcare.Profile
import com.example.bloodcare.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProfileMenuFragment : Fragment() {

    // ভিউ ভেরিয়েবল
    private lateinit var ivProfileImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvBloodGroup: TextView

    // ড্যাশবোর্ড ভেরিয়েবল
    private lateinit var switchDonorStatus: SwitchMaterial
    private lateinit var tvAvailabilityStatus: TextView
    private lateinit var tvBadgeName: TextView
    private lateinit var tvTotalDonations: TextView
    private lateinit var progressBarLevel: ProgressBar
    private lateinit var tvProgressText: TextView
    private lateinit var tvProgressCount: TextView
    private lateinit var tvLastDonationDate: TextView
    private lateinit var tvNextEligibleDate: TextView
    private lateinit var cardMyPosts: CardView
    private lateinit var tvLogout: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_menu, container, false)

        // ১. ভিউ ইনিশিলাইজেশন
        initViews(view)

        // ২. ডেটা লোড
        loadUserData()

        // ৩. ইভেন্ট লিসেনার সেটআপ
        setupListeners(view)

        return view
    }

    private fun initViews(view: View) {
        ivProfileImage = view.findViewById(R.id.ivProfileImage)
        tvName = view.findViewById(R.id.tvName)
        tvBloodGroup = view.findViewById(R.id.textView)

        switchDonorStatus = view.findViewById(R.id.switchDonorStatus)
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus)

        tvBadgeName = view.findViewById(R.id.tvBadgeName)
        tvTotalDonations = view.findViewById(R.id.tvTotalDonations)
        progressBarLevel = view.findViewById(R.id.progressBarLevel)
        tvProgressText = view.findViewById(R.id.tvProgressText)
        tvProgressCount = view.findViewById(R.id.tvProgressCount)

        tvLastDonationDate = view.findViewById(R.id.tvLastDonationDate)
        tvNextEligibleDate = view.findViewById(R.id.tvNextEligibleDate)

        cardMyPosts = view.findViewById(R.id.cardMyPosts)
        tvLogout = view.findViewById(R.id.tvLogout)
    }

    private fun setupListeners(view: View) {
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseMenu)
        btnClose.setOnClickListener { (activity as MainActivity).closeMenu() }

        val editIcon = view.findViewById<ImageView>(R.id.imageView2)
        editIcon.setOnClickListener {
            startActivity(Intent(activity, Profile::class.java))
        }

        // টগল বাটন লিসেনার (ফায়ারবেস আপডেট)
        switchDonorStatus.setOnCheckedChangeListener { _, isChecked ->
            updateAvailabilityStatus(isChecked)
        }

        // My Posts ক্লিক
        cardMyPosts.setOnClickListener {
            Toast.makeText(context, "Showing your post history...", Toast.LENGTH_SHORT).show()
            // ভবিষ্যতে এখানে MyPostsActivity তে যাওয়ার ইন্টেন্ট দেবেন
        }

        // লগআউট
        tvLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && isAdded) {
                    val name = snapshot.child("name").value.toString()
                    val bloodGroup = snapshot.child("bloodGroup").value.toString()
                    val profileImageUrl = snapshot.child("profileImage").value.toString()

                    // ফায়ারবেস থেকে এই ভ্যালুগুলো না থাকলে ডিফল্ট ভ্যালু ধরা হবে
                    val totalDonations = snapshot.child("totalDonations").getValue(Int::class.java) ?: 0
                    val isAvailable = snapshot.child("isAvailable").getValue(Boolean::class.java) ?: true
                    val lastDonation = snapshot.child("lastDonationDate").getValue(String::class.java)

                    // নাম ও ছবি সেট
                    tvName.text = name
                    tvBloodGroup.text = "Blood Group: $bloodGroup"
                    if (profileImageUrl.isNotEmpty() && profileImageUrl != "null") {
                        Glide.with(requireContext()).load(profileImageUrl)
                            .placeholder(R.drawable.person1).error(R.drawable.person1).into(ivProfileImage)
                    }

                    // --- Dashboard Logic Call ---
                    updateBadgeUI(totalDonations)
                    updateAvailabilityUI(isAvailable)
                    updateEligibilityDate(lastDonation)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- 1. Badge Logic ---
    private fun updateBadgeUI(count: Int) {
        tvTotalDonations.text = "Total Donations: $count"

        // লেভেল লজিক
        val (badge, nextBadge, nextTarget) = when {
            count >= 50 -> Triple("Red Guardian", "Max Level", 100)
            count >= 20 -> Triple("Blood Hero", "Red Guardian", 50)
            count >= 10 -> Triple("Hope Giver", "Blood Hero", 20)
            count >= 5 -> Triple("Life Saver", "Hope Giver", 10)
            count >= 1 -> Triple("First Drop", "Life Saver", 5)
            else -> Triple("Newbie", "First Drop", 1)
        }

        tvBadgeName.text = badge

        // প্রোগ্রেস বার সেটআপ
        // লজিক: আগের লেভেল থেকে বর্তমানের দূরত্ব বের করা
        var prevTarget = 0
        if (nextTarget == 5) prevTarget = 1
        else if (nextTarget == 10) prevTarget = 5
        else if (nextTarget == 20) prevTarget = 10
        else if (nextTarget == 50) prevTarget = 20

        if (count >= 50) {
            tvProgressText.text = "Maximum Level Reached!"
            tvProgressCount.text = ""
            progressBarLevel.progress = 100
        } else {
            tvProgressText.text = "Next: $nextBadge"
            tvProgressCount.text = "$count / $nextTarget"

            // ক্যালকুলেশন: (current - prev) / (target - prev) * 100
            val totalSteps = nextTarget - prevTarget
            val stepsTaken = count - prevTarget
            // 0 এর নিচে যেন না নামে (Newbie এর জন্য)
            val progress = if (totalSteps > 0 && stepsTaken > 0) ((stepsTaken.toFloat() / totalSteps) * 100).toInt() else 0

            // Newbie এর জন্য স্পেশাল হ্যান্ডলিং (0 to 1)
            if(count == 0) progressBarLevel.progress = 0
            else progressBarLevel.progress = progress
        }
    }

    // --- 2. Availability Toggle Logic ---
    private fun updateAvailabilityUI(isAvailable: Boolean) {
        // লিসেনার সাময়িকভাবে বন্ধ করা যাতে লুপ না হয়
        switchDonorStatus.setOnCheckedChangeListener(null)
        switchDonorStatus.isChecked = isAvailable

        if (isAvailable) {
            tvAvailabilityStatus.text = "🟢 Available to Donate"
            tvAvailabilityStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            tvAvailabilityStatus.text = "🔴 Not Available"
            tvAvailabilityStatus.setTextColor(Color.parseColor("#F44336")) // Red
        }

        // লিসেনার আবার চালু করা
        switchDonorStatus.setOnCheckedChangeListener { _, isChecked ->
            updateAvailabilityStatus(isChecked)
        }
    }

    private fun updateAvailabilityStatus(isChecked: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users").child(userId)
            .child("isAvailable").setValue(isChecked)
    }

    // --- 3. 90 Days Rule Logic ---
    private fun updateEligibilityDate(lastDateStr: String?) {
        if (lastDateStr == null || lastDateStr == "null" || lastDateStr.isEmpty()) {
            tvLastDonationDate.text = "No record found"
            tvNextEligibleDate.text = "Available Now"
            tvNextEligibleDate.setTextColor(Color.parseColor("#4CAF50"))
            return
        }

        tvLastDonationDate.text = lastDateStr

        // ডেট ক্যালকুলেশন
        val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault()) // আপনার ডেট ফরম্যাট অনুযায়ী
        try {
            val lastDate = sdf.parse(lastDateStr)
            val calendar = Calendar.getInstance()
            calendar.time = lastDate
            calendar.add(Calendar.DAY_OF_YEAR, 90) // ৯০ দিন যোগ করা

            val nextEligibleDate = calendar.time
            val today = Calendar.getInstance().time

            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvNextEligibleDate.text = displayFormat.format(nextEligibleDate)

            if (today.before(nextEligibleDate)) {
                // এখনো ৯০ দিন হয়নি
                tvNextEligibleDate.setTextColor(Color.parseColor("#F44336")) // Red
                // এখানে আপনি চাইলে অটোমেটিক Availability false করে দিতে পারেন
            } else {
                // ৯০ দিন পার হয়েছে
                tvNextEligibleDate.text = "Available Now"
                tvNextEligibleDate.setTextColor(Color.parseColor("#4CAF50")) // Green
            }

        } catch (e: Exception) {
            tvNextEligibleDate.text = "Date Error"
        }
    }
}