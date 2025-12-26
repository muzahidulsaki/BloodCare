package com.example.bloodcare.model

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

data class BloodRequestModel(
    val postId: String? = null,
    val userId: String? = null,
    val title: String? = null,
    val bloodGroup: String? = null,
    val amount: String? = null,
    val date: String? = null,
    val hospitalName: String? = null,
    val reason: String? = null,
    val contactName: String? = null,
    val mobile: String? = null,
    val country: String? = null,
    val city: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)