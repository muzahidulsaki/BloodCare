package com.example.bloodcare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloodcare.databinding.ActivityCreatePostBinding
import com.example.bloodcare.model.BloodRequestModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.Locale

class CreatePostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreatePostBinding

    // Variable to track Edit Mode
    private var isEditMode = false
    private var existingPostId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Spinner Setup
        setupSpinners()

        // 2. Check Intent (if Edit Mode)
        checkForEditIntent()

        // 3. Back Button
        binding.backButton.setOnClickListener {
            finish()
        }

        // 4. Date Picker
        binding.dateField.setOnClickListener {
            showDatePicker()
        }

        // 5. Time Picker
        binding.timeField.setOnClickListener {
            showTimePicker()
        }

        // 6. Submit Button
        binding.submitButton.setOnClickListener {
            validateAndSavePost()
        }
    }

    private fun checkForEditIntent() {
        if (intent.hasExtra("postId")) {
            isEditMode = true
            existingPostId = intent.getStringExtra("postId")

            binding.submitButton.text = "Update Post"
            binding.titleText.text = "Edit Request"

            binding.postTitle.setText(intent.getStringExtra("title"))
            binding.amountField.setText(intent.getStringExtra("amount"))
            binding.hospitalName.setText(intent.getStringExtra("hospitalName"))
            binding.whyField.setText(intent.getStringExtra("reason"))
            binding.contactName.setText(intent.getStringExtra("contactName"))
            binding.mobileNumber.setText(intent.getStringExtra("mobile"))
            binding.dateField.setText(intent.getStringExtra("date"))
            binding.timeField.setText(intent.getStringExtra("time"))

            val bloodGroup = intent.getStringExtra("bloodGroup")
            val city = intent.getStringExtra("city")
            val country = intent.getStringExtra("country")

            setSpinnerSelection(binding.groupSpinner, bloodGroup)
            setSpinnerSelection(binding.citySpinner, city)
            setSpinnerSelection(binding.countrySpinner, country)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(value)
        if (position >= 0) {
            spinner.setSelection(position)
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

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val hour12 = if (hourOfDay > 12) hourOfDay - 12 else if (hourOfDay == 0) 12 else hourOfDay
                val timeString = String.format(Locale.getDefault(), "%02d:%02d %s", hour12, minute, amPm)

                binding.timeField.setText(timeString)
            },
            currentHour,
            currentMinute,
            false
        )
        timePickerDialog.show()
    }

    private fun validateAndSavePost() {
        val title = binding.postTitle.text.toString().trim()
        val amount = binding.amountField.text.toString().trim()
        val date = binding.dateField.text.toString().trim()
        val time = binding.timeField.text.toString().trim()
        val hospital = binding.hospitalName.text.toString().trim()
        val reason = binding.whyField.text.toString().trim()
        val contact = binding.contactName.text.toString().trim()
        val mobile = binding.mobileNumber.text.toString().trim()

        val selectedGroup = binding.groupSpinner.selectedItem?.toString() ?: "Select Group"
        val selectedCountry = binding.countrySpinner.selectedItem?.toString() ?: "Select Country"
        val selectedCity = binding.citySpinner.selectedItem?.toString() ?: "Select City"

        if (title.isEmpty() || amount.isEmpty() || date.isEmpty() || time.isEmpty() || hospital.isEmpty() ||
            reason.isEmpty() || contact.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedGroup == "Select Group" || selectedCountry == "Select Country" || selectedCity == "Select City") {
            Toast.makeText(this, "Please select Group, Country and City correctly", Toast.LENGTH_SHORT).show()
            return
        }

        if (isEditMode && existingPostId != null) {
            updatePostInFirebase(existingPostId!!, title, amount, date, time, hospital, reason, contact, mobile, selectedGroup, selectedCountry, selectedCity)
        } else {
            saveToFirebase(title, amount, date, time, hospital, reason, contact, mobile, selectedGroup, selectedCountry, selectedCity)
        }
    }

    // Save new post (Updated: SuccessBottomSheet added)
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
                postId = postId, userId = userId, title = title, bloodGroup = group,
                amount = amount, date = date, time = time, hospitalName = hospital,
                reason = reason, contactName = contact, mobile = mobile,
                country = country, city = city
            )

            databaseRef.child(postId).setValue(post)
                .addOnSuccessListener {
                    // Showing Success Bottom Sheet
                    val successSheet = SuccessBottomSheet("Create Post Successfully") {
                        finish() // Close activity
                    }
                    successSheet.show(supportFragmentManager, "SuccessSheet")
                }
                .addOnFailureListener { e ->
                    binding.submitButton.isEnabled = true
                    binding.submitButton.text = "Post"
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Update post (Updated: SuccessBottomSheet added)
    private fun updatePostInFirebase(
        postId: String, title: String, amount: String, date: String, time: String, hospital: String,
        reason: String, contact: String, mobile: String,
        group: String, country: String, city: String
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        binding.submitButton.isEnabled = false
        binding.submitButton.text = "Updating..."

        val updatedData = mapOf(
            "title" to title, "bloodGroup" to group, "amount" to amount,
            "date" to date, "time" to time, "hospitalName" to hospital,
            "reason" to reason, "contactName" to contact, "mobile" to mobile,
            "country" to country, "city" to city
        )

        FirebaseDatabase.getInstance().getReference("usersPost").child(postId)
            .updateChildren(updatedData)
            .addOnSuccessListener {
                // Showing Success Bottom Sheet
                val successSheet = SuccessBottomSheet("Post Updated Successfully") {
                    finish() // Close activity
                }
                successSheet.show(supportFragmentManager, "SuccessSheet")
            }
            .addOnFailureListener { e ->
                binding.submitButton.isEnabled = true
                binding.submitButton.text = "Update Post"
                Toast.makeText(this, "Update Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}