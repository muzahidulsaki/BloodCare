package com.example.bloodcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // ✅ নতুন ইম্পোর্ট
import com.bumptech.glide.Glide
import com.example.bloodcare.adapter.RecentReqAdapter // ✅ নতুন ইম্পোর্ট (Adapter)
import com.example.bloodcare.databinding.ActivityMainBinding
import com.example.bloodcare.fragments.ProfileMenuFragment
import com.example.bloodcare.model.BloodRequestModel // ✅ নতুন ইম্পোর্ট (Model)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    private var isMenuOpen = false
    private var isActivityExpanded = false


    // ✅ রিসেন্ট পোস্টের জন্য লিস্ট এবং এডাপ্টার ভেরিয়েবল
    private lateinit var postList: ArrayList<BloodRequestModel>
    private lateinit var recentReqAdapter: RecentReqAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // বাইন্ডিং অবজেক্ট তৈরি এবং লেআউট সেট করা
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ ১. অ্যাপ চালু হলেই ইউজারের তথ্য লোড হবে
        loadUserInfo()

        // ✅ ২. Recent Requests সেকশন সেটআপ (নতুন কোড)
        setupRecentRequests()

        binding.profileImage.setOnClickListener {
            openMenu()
        }
        binding.userName.setOnClickListener {
            openMenu()
        }

        // 🩸 Blood Donor কার্ডের জন্য OnClickListener
        binding.cardBloodDonor.setOnClickListener {
            val intent = Intent(this, DonorActivity::class.java)
            startActivity(intent)
        }

        // 💉 Blood Recipient কার্ডের জন্য OnClickListener
        binding.cardRecipient.setOnClickListener {
            val intent = Intent(this, BloodRequestActivity::class.java)
            startActivity(intent)
        }

        // 📝 Create Post কার্ডের জন্য OnClickListener
        binding.cardCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        // ❤️ Blood Given কার্ডের জন্য OnClickListener
        binding.cardGiven.setOnClickListener {
            val intent = Intent(this, BloodGivenActivity::class.java)
            startActivity(intent)
        }

        // ডিফল্ট স্ট্যাটাস
        binding.donateStatus.text = "Donate Blood: On"

        // সার্চ বাটনের জন্য
        binding.searchBlood.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // ✅ ৩. View All বাটনের কাজ
        binding.tvViewAll.setOnClickListener {
            Toast.makeText(this, "All posts page coming soon!", Toast.LENGTH_SHORT).show()
            // ভবিষ্যতে এখানে AllPostsActivity তে যাওয়ার ইন্টেন্ট দেবেন
        }
        // 🔽 Activity As Expand / Collapse
        // 🔽 Activity As Expand / Collapse
        binding.activityHeader.setOnClickListener {

            if (!isActivityExpanded) {
                // SHOW
                binding.activityGrid.visibility = View.VISIBLE
                binding.activityGrid.startAnimation(
                    android.view.animation.AnimationUtils.loadAnimation(
                        this,
                        R.anim.slide_down
                    )
                )

                binding.ivDropdown.animate()
                    .rotation(180f)
                    .setDuration(300)
                    .start()

                isActivityExpanded = true

            } else {
                // HIDE (🔥 delay added)
                val slideUp = android.view.animation.AnimationUtils.loadAnimation(
                    this,
                    R.anim.slide_up
                )

                binding.activityGrid.startAnimation(slideUp)

                // ⏱ animation শেষ হলে GONE হবে
                binding.activityGrid.postDelayed({
                    binding.activityGrid.visibility = View.GONE
                }, 300)

                binding.ivDropdown.animate()
                    .rotation(0f)
                    .setDuration(300)
                    .start()

                isActivityExpanded = false
            }
        }


    }

    // ✅ রিসেন্ট রিকোয়েস্ট লোড করার ফাংশন (নতুন)
    private fun setupRecentRequests() {
        // RecyclerView কনফিগারেশন
        binding.rvRecentRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRecentRequests.setHasFixedSize(true)

        postList = arrayListOf()
        // শুরুতে খালি লিস্ট দিয়ে এডাপ্টার সেট করা হলো
        recentReqAdapter = RecentReqAdapter(postList)
        binding.rvRecentRequests.adapter = recentReqAdapter

        // Firebase থেকে ডাটা আনা
        loadRecentPostsFromFirebase()
    }

    private fun loadRecentPostsFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("usersPost")

        // আমরা limitToLast(3) এর বদলে একটু বেশি ডাটা (যেমন ১০০) আনছি,
        // কারণ ফিল্টার করার পর যেন লিস্ট খালি না হয়ে যায়।
        ref.limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // ডুপ্লিকেট এড়াতে লিস্ট ক্লিয়ার করা

                // ১. ডেট ফরম্যাট এবং আজকের তারিখ সেটআপ
                val sdf = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()) // আপনার ডেট ফরম্যাট অনুযায়ী (যেমন 25/12/2025)

                val todayCalendar = java.util.Calendar.getInstance()
                // সময় (Time) রিসেট করা হচ্ছে যাতে শুধু তারিখ তুলনা করা যায়
                todayCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(java.util.Calendar.MINUTE, 0)
                todayCalendar.set(java.util.Calendar.SECOND, 0)
                todayCalendar.set(java.util.Calendar.MILLISECOND, 0)

                val todayDate = todayCalendar.time // আজকের তারিখ

                if (snapshot.exists()) {
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(BloodRequestModel::class.java)

                        if (post != null && post.date != null) {
                            try {
                                // ২. পোস্টের তারিখ স্ট্রিং থেকে Date অবজেক্টে কনভার্ট করা
                                val postDate = sdf.parse(post.date)

                                // ৩. তুলনা: যদি পোস্টের তারিখ "আজ" বা "ভবিষ্যৎ" হয়
                                if (postDate != null && (postDate.after(todayDate) || postDate.equals(todayDate))) {
                                    postList.add(post)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace() // ডেট ফরম্যাট ভুল থাকলে এরর হ্যান্ডলিং
                            }
                        }
                    }

                    // ৪. Firebase ডাটা পুরনো -> নতুন অর্ডারে দেয়, তাই রিভার্স করা হচ্ছে (যাতে লেটেস্ট উপরে থাকে)
                    postList.reverse()

                    // ৫. আপনি যদি শুধু ৩টা কার্ড দেখাতে চান, তাহলে নিচের কোডটুকু রাখুন,
                    // আর যদি ফিল্টার করা সব দেখাতে চান তবে নিচের ৩ লাইন বাদ দিন।
                    if (postList.size > 3) {
                        val limitedList = ArrayList(postList.subList(0, 3))
                        postList.clear()
                        postList.addAll(limitedList)
                    }

                    // এডাপ্টারকে জানানো যে ডাটা চেঞ্জ হয়েছে
                    recentReqAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading posts", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // ✅ Firebase থেকে ইউজারের নাম এবং ছবি আনার ফাংশন
    private fun loadUserInfo() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            databaseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value.toString()
                        val profileImageUrl = snapshot.child("profileImage").value.toString()

                        binding.userName.text = name

                        if (profileImageUrl.isNotEmpty() && profileImageUrl != "null" && !isDestroyed) {
                            Glide.with(this@MainActivity)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.person1)
                                .error(R.drawable.person1)
                                .into(binding.profileImage)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    fun openMenu() {
        val fragment = ProfileMenuFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.menuContainer, fragment)
            .commit()

        findViewById<View>(R.id.mainScroll).visibility = View.GONE
        findViewById<View>(R.id.menuContainer).visibility = View.VISIBLE
    }

    fun closeMenu() {
        findViewById<View>(R.id.menuContainer).visibility = View.GONE
        findViewById<View>(R.id.mainScroll).visibility = View.VISIBLE
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.menuContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}