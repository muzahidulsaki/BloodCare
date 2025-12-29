package com.example.bloodcare

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.bloodcare.model.BloodRequestModel

class PostDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        val tvBloodGroup: TextView = findViewById(R.id.tvDetailBloodGroup)
        val tvTitle: TextView = findViewById(R.id.tvDetailTitle)
        val tvReason: TextView = findViewById(R.id.tvDetailReason)
        val tvHospital: TextView = findViewById(R.id.tvDetailHospital)
        val tvDate: TextView = findViewById(R.id.tvDetailDate)
        val tvTime: TextView = findViewById(R.id.tvDetailTime)
        val tvContactName: TextView = findViewById(R.id.tvDetailContactName)
        val tvPhone: TextView = findViewById(R.id.tvDetailPhone)
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnConfirm: AppCompatButton = findViewById(R.id.btnConfirmDonation)
        val tvShare: TextView = findViewById(R.id.tvShareNow)
        val ivCopyPhone: ImageView = findViewById(R.id.ivCopyPhone) // কপি আইকন

        val post = intent.getSerializableExtra("postData") as? BloodRequestModel

        if (post != null) {
            tvBloodGroup.text = post.bloodGroup
            tvTitle.text = post.title
            tvReason.text = post.reason
            tvHospital.text = "${post.hospitalName}, ${post.city}"
            tvDate.text = post.date
            tvTime.text = post.time
            tvContactName.text = post.contactName
            tvPhone.text = post.mobile

            // ✅ ফোন নম্বর কপি করার কোড
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

            // কনফার্ম বাটন
            btnConfirm.setOnClickListener {
                Toast.makeText(this, "Confirmed Donation!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}