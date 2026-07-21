package com.example.bloodcare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PostDetailsActivity : AppCompatActivity() {

    // ভেরিয়েবল
    private lateinit var layoutOwnerActions: LinearLayout
    private lateinit var btnConfirm: AppCompatButton
    private lateinit var btnEdit: AppCompatButton
    private lateinit var btnDelete: AppCompatButton

    // উপরের প্রোফাইল কার্ডের ভিউ
    private lateinit var ivPosterImage: ImageView
    private lateinit var tvPosterName: TextView
    private lateinit var tvPosterBadge: TextView
    private lateinit var cardPosterProfile: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        initViews()

        val post = intent.getSerializableExtra("postData") as? BloodRequestModel

        if (post != null) {
            // ১. বেসিক ডাটা সেট করা
            setDataToViews(post)

            // ২. পোস্টকারীর ইনফো লোড করা
            loadPosterInfo(post.userId)

            // ৩. লজিক চেক (মালিকানা চেক করা জরুরি)
            checkPostOwnership(post)

            // ৪. লিসেনার সেটআপ
            setupClickListeners(post)
        } else {
            Toast.makeText(this, "Error loading post details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        layoutOwnerActions = findViewById(R.id.layoutOwnerActions)
        btnConfirm = findViewById(R.id.btnConfirmDonation)
        btnEdit = findViewById(R.id.btnEditPost)
        btnDelete = findViewById(R.id.btnDeletePost)

        ivPosterImage = findViewById(R.id.ivPosterImage)
        tvPosterName = findViewById(R.id.tvPosterName)
        tvPosterBadge = findViewById(R.id.tvPosterBadge)
        cardPosterProfile = findViewById(R.id.cardPosterProfile)
    }

    private fun setDataToViews(post: BloodRequestModel) {
        findViewById<TextView>(R.id.tvDetailBloodGroup).text = post.bloodGroup
        findViewById<TextView>(R.id.tvDetailTitle).text = post.title
        findViewById<TextView>(R.id.tvDetailReason).text = post.reason
        findViewById<TextView>(R.id.tvDetailHospital).text = "${post.hospitalName}, ${post.city}"
        findViewById<TextView>(R.id.tvDetailDate).text = post.date
        findViewById<TextView>(R.id.tvDetailTime).text = post.time
        findViewById<TextView>(R.id.tvDetailContactName).text = post.contactName
        findViewById<TextView>(R.id.tvDetailPhone).text = post.mobile
    }

    private fun setupClickListeners(post: BloodRequestModel) {
        val ivCopyPhone: ImageView = findViewById(R.id.ivCopyPhone)
        val tvShare: TextView = findViewById(R.id.tvShareNow)
        val btnBack: ImageView = findViewById(R.id.btnBack)

        ivCopyPhone.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Phone Number", post.mobile)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Number copied", Toast.LENGTH_SHORT).show()
        }

        tvShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val shareBody = "Blood Need: ${post.bloodGroup} \nHospital: ${post.hospitalName}\nDate: ${post.date}\nTime: ${post.time}\nContact: ${post.mobile}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        }

        btnBack.setOnClickListener { finish() }

        btnConfirm.setOnClickListener { showConfirmationDialog(post) }

        // ✅ Delete Button Listener
        btnDelete.setOnClickListener {
            if (post.postId != null) {
                showDeleteConfirmation(post)
            } else {
                Toast.makeText(this, "Error: Post ID is missing!", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ Edit Button Listener (ফিক্সড - সব ডাটা পাঠানো হচ্ছে)
        btnEdit.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)

            // সব ডাটা পাঠানো হচ্ছে
            intent.putExtra("postId", post.postId)
            intent.putExtra("title", post.title)       // ✅ আগে মিসিং ছিল
            intent.putExtra("amount", post.amount)     // ✅ আগে মিসিং ছিল
            intent.putExtra("bloodGroup", post.bloodGroup)
            intent.putExtra("hospitalName", post.hospitalName)
            intent.putExtra("city", post.city)
            intent.putExtra("country", post.country)   // ✅ আগে মিসিং ছিল
            intent.putExtra("reason", post.reason)
            intent.putExtra("date", post.date)
            intent.putExtra("time", post.time)
            intent.putExtra("contactName", post.contactName)
            intent.putExtra("mobile", post.mobile)

            startActivity(intent)
            // finish() // আপনি চাইলে এডিট পেজে যাওয়ার সময় এই পেজ বন্ধ করতে পারেন, তবে না করাই ভালো
        }

        cardPosterProfile.setOnClickListener {
            val intent = Intent(this, ViewProfileActivity::class.java)
            intent.putExtra("targetUserId", post.userId)
            startActivity(intent)
        }
    }

    // ✅ পোস্টের মালিকানা চেক (Debug Log সহ)
    private fun checkPostOwnership(post: BloodRequestModel) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        Log.d("PostDebug", "Current User: $currentUserId, Post User: ${post.userId}")

        if (currentUserId != null && currentUserId == post.userId) {
            // নিজের পোস্ট হলে Edit/Delete দেখাবে
            btnConfirm.visibility = View.GONE
            layoutOwnerActions.visibility = View.VISIBLE
        } else {
            // অন্যের পোস্ট হলে Confirm বাটন দেখাবে
            btnConfirm.visibility = View.VISIBLE
            layoutOwnerActions.visibility = View.GONE

            // ডোনেশন ভ্যালিডেশন
            validateUserForDonation(post)
        }
    }

    private fun showDeleteConfirmation(post: BloodRequestModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Post")
        builder.setMessage("Are you sure you want to delete this post?")
        builder.setPositiveButton("Delete") { dialog, _ ->
            deletePostFromFirebase(post)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun deletePostFromFirebase(post: BloodRequestModel) {
        val ref = FirebaseDatabase.getInstance().getReference("usersPost").child(post.postId!!)

        ref.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Post Deleted Successfully", Toast.LENGTH_SHORT).show()
                // ডিলিট হওয়ার পর মেইন পেজে ফিরে যাওয়া
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- বাকি ফাংশনগুলো অপরিবর্তিত আছে (loadPosterInfo, validateUserForDonation, etc.) ---

    private fun loadPosterInfo(userId: String?) {
        if (userId == null) return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    val profileImage = snapshot.child("profileImage").value.toString()
                    val totalDonations = snapshot.child("totalDonations").getValue(Int::class.java) ?: 0

                    val badgeName = when {
                        totalDonations >= 50 -> "Red Guardian"
                        totalDonations >= 20 -> "Blood Hero"
                        totalDonations >= 10 -> "Hope Giver"
                        totalDonations >= 5 -> "Life Saver"
                        totalDonations >= 1 -> "First Drop"
                        else -> "Newbie"
                    }

                    tvPosterName.text = name
                    tvPosterBadge.text = badgeName

                    if (profileImage.isNotEmpty() && profileImage != "null") {
                        Glide.with(this@PostDetailsActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.person1)
                            .error(R.drawable.person1)
                            .into(ivPosterImage)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun validateUserForDonation(post: BloodRequestModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userBloodGroup = snapshot.child("bloodGroup").getValue(String::class.java)
                    val lastDateStr = snapshot.child("lastDonationDate").getValue(String::class.java)
                    if (userBloodGroup != post.bloodGroup) {
                        disableButton("Blood Not Match")
                        return
                    }
                    checkIfAlreadyDonated(userId, post, lastDateStr)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkIfAlreadyDonated(userId: String, post: BloodRequestModel, lastDateStr: String?) {
        val historyRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("donationHistory")
        val query = historyRef.orderByChild("postId").equalTo(post.postId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    disableButton("Already Confirmed")
                } else {
                    check90DayRule(lastDateStr)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun check90DayRule(lastDateStr: String?) {
        if (lastDateStr != null && lastDateStr != "null" && lastDateStr.isNotEmpty()) {
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            try {
                val lastDate = sdf.parse(lastDateStr)
                val calendar = Calendar.getInstance()
                calendar.time = lastDate
                calendar.add(Calendar.DAY_OF_YEAR, 90)
                val nextEligibleDate = calendar.time
                val today = Calendar.getInstance().time

                if (today.before(nextEligibleDate)) {
                    val displayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val dateText = displayFormat.format(nextEligibleDate)
                    disableButton("Available after $dateText")
                } else {
                    enableButton()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            enableButton()
        }
    }

    private fun disableButton(text: String) {
        btnConfirm.text = text
        btnConfirm.isEnabled = false
        btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
    }

    private fun enableButton() {
        btnConfirm.text = "Confirm Donation"
        btnConfirm.isEnabled = true
        btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF4C4C"))
    }

    private fun showConfirmationDialog(post: BloodRequestModel) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Donation")
        builder.setMessage("Are you sure you want to confirm donation?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            updateDonationCount(post)
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateDonationCount(post: BloodRequestModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.child("totalDonations").setValue(ServerValue.increment(1))
            userRef.child("lastDonationDate").setValue(post.date)

            val historyRef = userRef.child("donationHistory").push()
            val donationData = mapOf(
                "postId" to post.postId,
                "bloodGroup" to post.bloodGroup,
                "hospital" to post.hospitalName,
                "date" to post.date,
                "donatedAt" to ServerValue.TIMESTAMP
            )

            historyRef.setValue(donationData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Donation Confirmed!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update record", Toast.LENGTH_SHORT).show()
                }
        }
    }
}