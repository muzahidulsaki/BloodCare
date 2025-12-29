package com.example.bloodcare

import android.app.DatePickerDialog
import android.app.TimePickerDialog // ✅ ইম্পোর্ট করা হয়েছে
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloodcare.databinding.ActivityCreatePostBinding
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale // ✅ Locale ইম্পোর্ট

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ১. স্পিনার সেটআপ
        setupSpinners()

        // ২. ব্যাক বাটন
        binding.backButton.setOnClickListener {
            finish()
        }

        // ৩. ডেট পিকার
        binding.dateField.setOnClickListener {
            showDatePicker()
        }

        // ৪. টাইম পিকার (✅ নতুন যোগ করা হয়েছে)
        binding.timeField.setOnClickListener {
            showTimePicker()
        }

        // ৫. সাবমিট বাটন
        binding.submitButton.setOnClickListener {
            validateAndSavePost()
        }
    }

    private fun setupSpinners() {
        val bloodGroups = arrayOf("Select Group", "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
        val groupAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bloodGroups)
        binding.groupSpinner.adapter = groupAdapter

        val countries = arrayOf("Select Country", "Bangladesh")
        val countryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, countries)
        binding.countrySpinner.adapter = countryAdapter

        val districts = resources.getStringArray(R.array.bd_districts)
        val cityList = mutableListOf("Select City")
        cityList.addAll(districts)
        val cityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cityList)
        binding.citySpinner.adapter = cityAdapter
    }

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

    // ✅ টাইম পিকার ফাংশন (AM/PM ফরম্যাট সহ)
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // সময় ফরম্যাট করা (যেমন: 02:30 PM)
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour12 = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                val timeString = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)

                binding.timeField.setText(timeString)
            },
            currentHour,
            currentMinute,
            false // false মানে 24 ঘন্টা ফরম্যাট হবে না, আমরা 12 ঘন্টা ফরম্যাট কাস্টম বানাচ্ছি
        )
        timePickerDialog.show()
    }

    private fun validateAndSavePost() {
        // ডাটা নেওয়া
        val title = binding.postTitle.text.toString().trim()
        val amount = binding.amountField.text.toString().trim()
        val date = binding.dateField.text.toString().trim()
        val time = binding.timeField.text.toString().trim() // ✅ টাইম নেওয়া হলো
        val hospital = binding.hospitalName.text.toString().trim()
        val reason = binding.whyField.text.toString().trim()
        val contact = binding.contactName.text.toString().trim()
        val mobile = binding.mobileNumber.text.toString().trim()

        val selectedGroup = binding.groupSpinner.selectedItem?.toString() ?: "Select Group"
        val selectedCountry = binding.countrySpinner.selectedItem?.toString() ?: "Select Country"
        val selectedCity = binding.citySpinner.selectedItem?.toString() ?: "Select City"

        // ভ্যালিডেশন
        if (title.isEmpty() || amount.isEmpty() || date.isEmpty() || time.isEmpty() || hospital.isEmpty() ||
            reason.isEmpty() || contact.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill all fields including Time", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedGroup == "Select Group" || selectedCountry == "Select Country" || selectedCity == "Select City") {
            Toast.makeText(this, "Please select Group, Country and City correctly", Toast.LENGTH_SHORT).show()
            return
        }

        // সেভ ফাংশন কল
        saveToFirebase(title, amount, date, time, hospital, reason, contact, mobile, selectedGroup, selectedCountry, selectedCity)
    }

    private fun saveToFirebase(
        title: String, amount: String, date: String, time: String, hospital: String,
        reason: String, contact: String, mobile: String,
        group: String, country: String, city: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.submitButton.isEnabled = false
        binding.submitButton.text = "Posting..."

        val databaseRef = FirebaseDatabase.getInstance().getReference("usersPost")
        val postId = databaseRef.push().key

        if (postId != null) {
            val post = BloodRequestModel(
                postId = postId,
                userId = userId,
                title = title,
                bloodGroup = group,
                amount = amount,
                date = date,
                time = time, // ✅ টাইম ডাটাবেসে পাঠানো হচ্ছে
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
                    finish()
                }
                .addOnFailureListener { e ->
                    binding.submitButton.isEnabled = true
                    binding.submitButton.text = "Post"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}