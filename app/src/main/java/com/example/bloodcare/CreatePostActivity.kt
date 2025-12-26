package com.example.bloodcare

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloodcare.databinding.ActivityCreatePostBinding
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ১. স্পিনার বা ড্রপডাউন সেটআপ (ডাটা লোড করা)
        setupSpinners()

        // ২. ব্যাক বাটন
        binding.backButton.setOnClickListener {
            finish()
        }

        // ৩. ডেট পিকার
        binding.dateField.setOnClickListener {
            showDatePicker()
        }

        // ৪. সাবমিট বাটন (Firebase এ সেভ করার জন্য)
        binding.submitButton.setOnClickListener {
            validateAndSavePost()
        }
    }

    // স্পিনারগুলোতে ডাটা সেট করার ফাংশন
    private fun setupSpinners() {
        // ব্লাড গ্রুপ
        val bloodGroups = arrayOf("Select Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bloodGroups)
        binding.groupSpinner.adapter = groupAdapter

        // দেশ (উদাহরণ)
        val countries = arrayOf("Select Country", "Bangladesh")
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        binding.countrySpinner.adapter = countryAdapter

        // ৩. শহর (শহরগুলো এখন strings.xml থেকে আসবে) ✅ পরিবর্তন এখানে

        // প্রথমে XML থেকে জেলাগুলো নিয়ে আসা হলো
        val districts = resources.getStringArray(R.array.bd_districts)

        // একটি নতুন লিস্ট তৈরি করা হলো যেখানে প্রথমে "Select City" থাকবে
        val cityList = mutableListOf("Select City")

        // এরপর সেই লিস্টে জেলাগুলো যোগ করা হলো
        cityList.addAll(districts)

        // এখন এই নতুন cityList টি এডাপ্টারে দেওয়া হলো
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityList)
        binding.citySpinner.adapter = cityAdapter
    }

    // ডেট পিকার দেখানোর ফাংশন
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dp = DatePickerDialog(
            this,
            { _, year, month, day ->
                binding.dateField.setText("$day/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dp.show()
    }

    // ইনপুট ভ্যালিডেশন এবং সেভ প্রসেস শুরু
    private fun validateAndSavePost() {
        // বাইন্ডিং ব্যবহার করে ডাটা নেওয়া
        val title = binding.postTitle.text.toString().trim()
        val amount = binding.amountField.text.toString().trim()
        val date = binding.dateField.text.toString().trim()
        val hospital = binding.hospitalName.text.toString().trim()
        val reason = binding.whyField.text.toString().trim()
        val contact = binding.contactName.text.toString().trim()
        val mobile = binding.mobileNumber.text.toString().trim()

        // স্পিনার ডাটা
        val selectedGroup = binding.groupSpinner.selectedItem?.toString() ?: "Select Group"
        val selectedCountry = binding.countrySpinner.selectedItem?.toString() ?: "Select Country"
        val selectedCity = binding.citySpinner.selectedItem?.toString() ?: "Select City"

        // চেক করা কোনো ঘর খালি আছে কিনা
        if (title.isEmpty() || amount.isEmpty() || date.isEmpty() || hospital.isEmpty() ||
            reason.isEmpty() || contact.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // চেক করা স্পিনার সিলেক্ট করা হয়েছে কিনা
        if (selectedGroup == "Select Group" || selectedCountry == "Select Country" || selectedCity == "Select City") {
            Toast.makeText(this, "Please select Group, Country and City correctly", Toast.LENGTH_SHORT).show()
            return
        }

        // সব ঠিক থাকলে Firebase এ সেভ ফাংশন কল করা
        saveToFirebase(title, amount, date, hospital, reason, contact, mobile, selectedGroup, selectedCountry, selectedCity)
    }

    // ফায়ারবেসে ডাটা পাঠানোর ফাংশন
    private fun saveToFirebase(
        title: String, amount: String, date: String, hospital: String,
        reason: String, contact: String, mobile: String,
        group: String, country: String, city: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        // লোডিং বোঝানোর জন্য বাটন ডিজেবল করা যেতে পারে (অপশনাল)
        binding.submitButton.isEnabled = false
        binding.submitButton.text = "Posting..."

        val databaseRef = FirebaseDatabase.getInstance().getReference("usersPost")
        val postId = databaseRef.push().key // ইউনিক আইডি তৈরি

        if (postId != null) {
            val post = BloodRequestModel(
                postId = postId,
                userId = userId,
                title = title,
                bloodGroup = group,
                amount = amount,
                date = date,
                hospitalName = hospital,
                reason = reason,
                contactName = contact,
                mobile = mobile,
                country = country,
                city = city
            )

            databaseRef.child(postId).setValue(post)
                .addOnSuccessListener {
                    Toast.makeText(this, "Request Posted Successfully!", Toast.LENGTH_LONG).show()
                    finish() // সফল হলে পেজ বন্ধ করে দেওয়া
                }
                .addOnFailureListener { e ->
                    binding.submitButton.isEnabled = true
                    binding.submitButton.text = "Get Started" // টেক্সট আগের মতো করে দেওয়া
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}