package com.example.bloodcare

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bloodcare.adapter.RecentReqAdapter
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ViewProfileActivity : AppCompatActivity() {

    // Views
    private lateinit var ivUserProfile: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserBlood: TextView
    private lateinit var tvCity: TextView
    private lateinit var tvCountry: TextView
    private lateinit var tvMobile: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnCall: AppCompatButton
    private lateinit var btnChat: AppCompatButton

    // Tabs
    private lateinit var tabAbout: LinearLayout
    private lateinit var tabPosts: LinearLayout
    private lateinit var txtAbout: TextView
    private lateinit var txtPosts: TextView
    private lateinit var lineAbout: View
    private lateinit var linePosts: View
    private lateinit var layoutAboutContent: LinearLayout
    private lateinit var layoutPostsContent: LinearLayout

    // RecyclerView
    private lateinit var rvUserPosts: RecyclerView
    private lateinit var postList: ArrayList<BloodRequestModel>
    private lateinit var adapter: RecentReqAdapter
    private lateinit var tvNoPosts: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvDOB: TextView

    private var targetUserId: String? = null
    private var userMobileNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        // 1. Get Intent Data
        targetUserId = intent.getStringExtra("targetUserId")

        if (targetUserId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Init Views
        initViews()

        // 3. Load Data
        loadUserProfile(targetUserId!!)
        loadUserPosts(targetUserId!!)

        // 4. Set Listeners
        setupTabs()

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        btnCall.setOnClickListener {
            if (userMobileNumber.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:$userMobileNumber")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Mobile number not available", Toast.LENGTH_SHORT).show()
            }
        }

        btnChat.setOnClickListener {
            Toast.makeText(this, "Chat feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        ivUserProfile = findViewById(R.id.ivUserProfile)
        tvUserName = findViewById(R.id.tvUserName)
        tvUserBlood = findViewById(R.id.tvUserBlood)
        tvCity = findViewById(R.id.tvCity)
        tvCountry = findViewById(R.id.tvCountry)
        tvMobile = findViewById(R.id.tvMobile)
        tvEmail = findViewById(R.id.tvEmail)
        btnCall = findViewById(R.id.btnCall)
        btnChat = findViewById(R.id.btnChat)
        tvGender = findViewById(R.id.tvGender)
        tvAge = findViewById(R.id.tvAge)
        tvDOB = findViewById(R.id.tvDOB)

        tabAbout = findViewById(R.id.tabAbout)
        tabPosts = findViewById(R.id.tabPosts)
        txtAbout = findViewById(R.id.txtAbout)
        txtPosts = findViewById(R.id.txtPosts)
        lineAbout = findViewById(R.id.lineAbout)
        linePosts = findViewById(R.id.linePosts)
        layoutAboutContent = findViewById(R.id.layoutAboutContent)
        layoutPostsContent = findViewById(R.id.layoutPostsContent)

        rvUserPosts = findViewById(R.id.rvUserPosts)
        tvNoPosts = findViewById(R.id.tvNoPosts)

        // Setup Recycler
        rvUserPosts.layoutManager = LinearLayoutManager(this)
        postList = arrayListOf()
        adapter = RecentReqAdapter(postList)
        rvUserPosts.adapter = adapter
    }

    private fun loadUserProfile(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("users").child(userId)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val blood = snapshot.child("bloodGroup").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val mobile = snapshot.child("mobile").value.toString()

                    // City Email
                    val city = snapshot.child("city").value?.toString() ?: "N/A"
                    val email = snapshot.child("email").value?.toString() ?: "N/A"

                    val gender = snapshot.child("gender").value?.toString() ?: "N/A"
                    val age = snapshot.child("age").value?.toString() ?: "N/A"
                    val dob = snapshot.child("dob").value?.toString() ?: "N/A"

                    tvUserName.text = name
                    tvUserBlood.text = "$blood Blood"

                    tvCity.text = city
                    tvMobile.text = mobile
                    tvEmail.text = email

                    tvGender.text = gender
                    tvAge.text = "$age Years"
                    tvDOB.text = dob

                    userMobileNumber = mobile

                    if (profileImage.isNotEmpty() && profileImage != "null") {
                        Glide.with(this@ViewProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.person1)
                            .error(R.drawable.person1)
                            .into(ivUserProfile)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadUserPosts(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("usersPost")
        // Filter posts by userId
        ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                if (snapshot.exists()) {
                    for (postSnap in snapshot.children) {
                        val post = postSnap.getValue(BloodRequestModel::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    postList.reverse() // Newest first
                    adapter.notifyDataSetChanged()
                }

                if (postList.isEmpty()) {
                    tvNoPosts.visibility = View.VISIBLE
                    rvUserPosts.visibility = View.GONE
                } else {
                    tvNoPosts.visibility = View.GONE
                    rvUserPosts.visibility = View.VISIBLE
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun setupTabs() {
        tabAbout.setOnClickListener {
            // Show About
            layoutAboutContent.visibility = View.VISIBLE
            layoutPostsContent.visibility = View.GONE

            // Update Styles
            txtAbout.setTextColor(Color.BLACK)
            lineAbout.setBackgroundColor(Color.parseColor("#FF4C4C"))

            txtPosts.setTextColor(Color.parseColor("#999999"))
            linePosts.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }

        tabPosts.setOnClickListener {
            // Show Posts
            layoutAboutContent.visibility = View.GONE
            layoutPostsContent.visibility = View.VISIBLE

            // Update Styles
            txtPosts.setTextColor(Color.BLACK)
            linePosts.setBackgroundColor(Color.parseColor("#FF4C4C"))

            txtAbout.setTextColor(Color.parseColor("#999999"))
            lineAbout.setBackgroundColor(Color.parseColor("#E0E0E0"))
        }
    }
}