package com.example.bloodcare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        // View Initialization
        val tvBloodGroup: TextView = findViewById(R.id.tvDetailBloodGroup)
        val tvTitle: TextView = findViewById(R.id.tvDetailTitle)
        val tvReason: TextView = findViewById(R.id.tvDetailReason)
        val tvHospital: TextView = findViewById(R.id.tvDetailHospital)
        val tvDate: TextView = findViewById(R.id.tvDetailDate)
        val tvTime: TextView = findViewById(R.id.tvDetailTime)
        val tvContactName: TextView = findViewById(R.id.tvDetailContactName)
        val tvPhone: TextView = findViewById(R.id.tvDetailPhone)
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val tvShare: TextView = findViewById(R.id.tvShareNow)
        val ivCopyPhone: ImageView = findViewById(R.id.ivCopyPhone)

        // বাটন ও লেআউট ফাইন্ড করা
        btnConfirm = findViewById(R.id.btnConfirmDonation)
        layoutOwnerActions = findViewById(R.id.layoutOwnerActions)
        btnEdit = findViewById(R.id.btnEditPost)
        btnDelete = findViewById(R.id.btnDeletePost)

        // Intent থেকে ডাটা রিসিভ
        val post = intent.getSerializableExtra("postData") as? BloodRequestModel

        if (post != null) {
            // ডাটা সেট করা
            tvBloodGroup.text = post.bloodGroup
            tvTitle.text = post.title
            tvReason.text = post.reason
            tvHospital.text = "${post.hospitalName}, ${post.city}"
            tvDate.text = post.date
            tvTime.text = post.time
            tvContactName.text = post.contactName
            tvPhone.text = post.mobile

            // ১. মালিকানা চেক (সবার আগে)
            checkPostOwnership(post)

            // ফোন নম্বর কপি
            ivCopyPhone.setOnClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Phone Number", post.mobile)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Number copied to clipboard", Toast.LENGTH_SHORT).show()
            }

            // শেয়ার বাটন
            tvShare.setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                val shareBody = "Blood Need: ${post.bloodGroup} \nHospital: ${post.hospitalName}\nDate: ${post.date}\nTime: ${post.time}\nContact: ${post.mobile}"
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }

            // কনফার্ম বাটন (অন্যদের জন্য)
            btnConfirm.setOnClickListener {
                showConfirmationDialog(post)
            }

            // ডিলিট বাটন (মালিকের জন্য)
            btnDelete.setOnClickListener {
                showDeleteConfirmation(post)
            }

            // এডিট বাটন (মালিকের জন্য)
            btnEdit.setOnClickListener {
                Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    // ✅ ধাপ ১: মালিকানা চেক
    private fun checkPostOwnership(post: BloodRequestModel) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId != null && currentUserId == post.userId) {
            // নিজের পোস্ট হলে Edit/Delete দেখাবে
            btnConfirm.visibility = View.GONE
            layoutOwnerActions.visibility = View.VISIBLE
        } else {
            // অন্যের পোস্ট হলে Confirm বাটন দেখাবে এবং ভ্যালিডেশন শুরু করবে
            btnConfirm.visibility = View.VISIBLE
            layoutOwnerActions.visibility = View.GONE

            // 🛑 ভ্যালিডেশন চেইন শুরু (ব্লাড গ্রুপ -> হিস্ট্রি -> এলিজিবিলিটি)
            validateUserForDonation(post)
        }
    }

    // ✅ ধাপ ২, ৩ ও ৪: ইউজারের ব্লাড গ্রুপ, হিস্ট্রি এবং এলিজিবিলিটি চেক
    private fun validateUserForDonation(post: BloodRequestModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

        // ১. ইউজারের প্রোফাইল থেকে ব্লাড গ্রুপ এবং লাস্ট ডোনেশন ডেট আনা
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userBloodGroup = snapshot.child("bloodGroup").getValue(String::class.java)
                    val lastDateStr = snapshot.child("lastDonationDate").getValue(String::class.java)

                    // 🔴 চেক ২: ব্লাড গ্রুপ ম্যাচিং
                    if (userBloodGroup != post.bloodGroup) {
                        disableButton("Blood Not Match")
                        return // এখানেই থামবে
                    }

                    // 🔴 চেক ৩: আগেই কনফার্ম করেছে কিনা (History Check)
                    checkIfAlreadyDonated(userId, post, lastDateStr)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ ধাপ ৩ (চলমান): হিস্ট্রি চেক করা
    private fun checkIfAlreadyDonated(userId: String, post: BloodRequestModel, lastDateStr: String?) {
        val historyRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId).child("donationHistory")

        val query = historyRef.orderByChild("postId").equalTo(post.postId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // যদি আগেই কনফার্ম করে থাকে
                    disableButton("Already Confirmed")
                } else {
                    // 🔴 চেক ৪: ৯০ দিনের রুল চেক (এলিজিবিলিটি)
                    check90DayRule(lastDateStr)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // ✅ ধাপ ৪: ৯০ দিনের রুল চেক
    private fun check90DayRule(lastDateStr: String?) {
        if (lastDateStr != null && lastDateStr != "null" && lastDateStr.isNotEmpty()) {
            val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
            try {
                val lastDate = sdf.parse(lastDateStr)
                val calendar = Calendar.getInstance()
                calendar.time = lastDate
                calendar.add(Calendar.DAY_OF_YEAR, 90) // ৯০ দিন যোগ করা

                val nextEligibleDate = calendar.time
                val today = Calendar.getInstance().time

                if (today.before(nextEligibleDate)) {
                    // এখনো ৯০ দিন পার হয়নি
                    val displayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                    val dateText = displayFormat.format(nextEligibleDate)
                    disableButton("Available after $dateText")
                } else {
                    // ৯০ দিন পার হয়েছে -> সব ঠিক আছে
                    // বাটন এনাবল থাকবে (ডিফল্ট)
                    enableButton()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // যদি আগে কখনো রক্ত না দিয়ে থাকে, তাহলে সে এলিজিবল
            enableButton()
        }
    }

    // বাটন ডিজেবল করার হেল্পার
    private fun disableButton(text: String) {
        btnConfirm.text = text
        btnConfirm.isEnabled = false
        btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.GRAY) // ধূসর রং
    }

    // বাটন এনাবল করার হেল্পার (ডিফল্ট স্টাইল ফেরত আনা)
    private fun enableButton() {
        btnConfirm.text = "Confirm Donation"
        btnConfirm.isEnabled = true
        btnConfirm.backgroundTintList = ColorStateList.valueOf(Color.RED) // বা আপনার থিম কালার
    }

    // ডিলিট কনফার্মেশন ডায়ালগ
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

    // ফায়ারবেস থেকে ডিলিট
    private fun deletePostFromFirebase(post: BloodRequestModel) {
        if (post.postId != null) {
            val ref = FirebaseDatabase.getInstance().getReference("usersPost").child(post.postId)
            ref.removeValue()
                .addOnSuccessListener {
                    Toast.makeText(this, "Post Deleted Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to delete post", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ডোনেশন কনফার্মেশন ডায়ালগ
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

    // ডোনেশন আপডেট
    private fun updateDonationCount(post: BloodRequestModel) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)

            userRef.child("totalDonations").setValue(ServerValue.increment(1))
            // ✅ Last Donation Date আপডেট (প্রোফাইল ড্যাশবোর্ডের জন্য)
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