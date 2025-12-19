package com.example.bloodcare

import BasicInfoFragment
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Profile : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etMobile: EditText
    private lateinit var ddGroup: TextView
    private lateinit var ddCountry: TextView
    private lateinit var ddCity: TextView
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ভিউ ইনিশিয়ালাইজেশন
        etName = findViewById(R.id.etName)
        etMobile = findViewById(R.id.etMobile)
        ddGroup = findViewById(R.id.ddGroup)
        ddCountry = findViewById(R.id.ddCountry)
        ddCity = findViewById(R.id.ddCity)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        val inputWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkInputs()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etName.addTextChangedListener(inputWatcher)
        etMobile.addTextChangedListener(inputWatcher)

        // TextView এ টেক্সট চেঞ্জ লিসেনার সরাসরি কাজ করে না যদি ইউজার টাইপ না করে
        // তাই এগুলো আপাতত checkInputs এর বাইরে রাখাই ভালো অথবা ডাটা সিলেক্ট করার পর checkInputs() কল করুন

        btnNext.setOnClickListener {

            // ১. BasicInfoFragment এর একটি অবজেক্ট তৈরি করা
            val basicInfoFragment =BasicInfoFragment()

            // ২. Fragment ম্যানেজার দিয়ে বর্তমান স্ক্রিন রিপ্লেস করা
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, basicInfoFragment)
                .addToBackStack(null) // যাতে ব্যাক বাটন চাপলে আবার এই পেজে ফিরে আসা যায়
                .commit()
        }
    }

    private fun checkInputs() {
        val name = etName.text.toString().trim()
        val mobile = etMobile.text.toString().trim()

        // শুধু নাম এবং মোবাইল চেক করছি আপাতত
        btnNext.isEnabled = name.isNotEmpty() && mobile.length >= 10
    }
}