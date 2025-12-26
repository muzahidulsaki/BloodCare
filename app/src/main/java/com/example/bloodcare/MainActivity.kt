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
            val intent = Intent(this, RecipientActivity::class.java)
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

        // limitToLast(3) মানে শেষ ৩টি ডাটা আনবে
        ref.limitToLast(3).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // ডুপ্লিকেট এড়াতে লিস্ট ক্লিয়ার করা
                if (snapshot.exists()) {
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(BloodRequestModel::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    // Firebase ডাটা পুরনো -> নতুন অর্ডারে দেয়, তাই রিভার্স করতে হবে
                    postList.reverse()

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