package com.example.bloodcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // New import
import com.bumptech.glide.Glide
import com.example.bloodcare.adapter.RecentReqAdapter // New import (Adapter)
import com.example.bloodcare.databinding.ActivityMainBinding
import com.example.bloodcare.fragments.ProfileMenuFragment
import com.example.bloodcare.model.BloodRequestModel // New import (Model)
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


    // List and Adapter variables for recent posts
    private lateinit var postList: ArrayList<BloodRequestModel>
    private lateinit var recentReqAdapter: RecentReqAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Create binding object and set layout
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. User info will load when the app starts
        loadUserInfo()

        // 2. Recent Requests section setup (new code)
        setupRecentRequests()

        binding.profileImage.setOnClickListener {
            openMenu()
        }
        binding.userName.setOnClickListener {
            openMenu()
        }

        // OnClickListener for Blood Donor card
        binding.cardBloodDonor.setOnClickListener {
            val intent = Intent(this, DonorActivity::class.java)
            startActivity(intent)
        }

        // OnClickListener for Blood Recipient card
        binding.cardRecipient.setOnClickListener {
            val intent = Intent(this, BloodRequestActivity::class.java)
            startActivity(intent)
        }

        // OnClickListener for Create Post card
        binding.cardCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        // OnClickListener for Blood Given card
        binding.cardGiven.setOnClickListener {
            val intent = Intent(this, BloodGivenActivity::class.java)
            startActivity(intent)
        }

        // Default status
        binding.donateStatus.text = "Donate Blood: On"

        // For search button
        binding.searchBlood.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        // 3. View All button functionality
        binding.tvViewAll.setOnClickListener {
            val intent = Intent(this, BloodRequestActivity::class.java)
            startActivity(intent)
        }
        // Activity As Expand / Collapse
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
                // HIDE (delay added)
                val slideUp = android.view.animation.AnimationUtils.loadAnimation(
                    this,
                    R.anim.slide_up
                )

                binding.activityGrid.startAnimation(slideUp)

                // GONE after animation ends
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

    // Function to load recent requests (new)
    private fun setupRecentRequests() {
        // RecyclerView configuration
        binding.rvRecentRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRecentRequests.setHasFixedSize(true)

        postList = arrayListOf()
        // Adapter set with an empty list initially
        recentReqAdapter = RecentReqAdapter(postList)
        binding.rvRecentRequests.adapter = recentReqAdapter

        // Fetch data from Firebase
        loadRecentPostsFromFirebase()
    }

    private fun loadRecentPostsFromFirebase() {
        val ref = FirebaseDatabase.getInstance().getReference("usersPost")

        // Fetching more data (e.g., 100) instead of limitToLast(3)
        // to avoid an empty list after filtering.
        ref.limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // Clear list to avoid duplicates

                // 1. Date format and today's date setup
                val sdf = java.text.SimpleDateFormat("d/M/yyyy", java.util.Locale.getDefault()) // according to your date format (e.g., 25/12/2025)

                val todayCalendar = java.util.Calendar.getInstance()
                // Reset time to compare only dates
                todayCalendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                todayCalendar.set(java.util.Calendar.MINUTE, 0)
                todayCalendar.set(java.util.Calendar.SECOND, 0)
                todayCalendar.set(java.util.Calendar.MILLISECOND, 0)

                val todayDate = todayCalendar.time // Today's date

                if (snapshot.exists()) {
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(BloodRequestModel::class.java)

                        if (post != null && post.date != null) {
                            try {
                                // 2. Convert post date string to Date object
                                val postDate = sdf.parse(post.date)

                                // 3. Comparison: if the post date is "today" or "future"
                                if (postDate != null && (postDate.after(todayDate) || postDate.equals(todayDate))) {
                                    postList.add(post)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace() // Error handling if date format is incorrect
                            }
                        }
                    }

                    // 4. Firebase data is old -> new, so reversing it (to keep latest on top)
                    postList.reverse()

                    // 5. Keep the following code if you want to show only 3 cards,
                    // otherwise remove the following 3 lines to show all filtered posts.
                    if (postList.size > 3) {
                        val limitedList = ArrayList(postList.subList(0, 3))
                        postList.clear()
                        postList.addAll(limitedList)
                    }

                    // Notify adapter that data has changed
                    recentReqAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error loading posts", Toast.LENGTH_SHORT).show()
            }
        })
    }
    // Function to fetch user name and photo from Firebase
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