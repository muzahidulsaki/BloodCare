package com.example.bloodcare.model

data class UserModel(
    val userId: String? = null,
    val name: String? = null,
    val mobile: String? = null,
    val bloodGroup: String? = null,
    val profileImage: String? = null,
    val totalDonations: Int = 0,
    val isAvailable: Boolean = false // Default must be false
)