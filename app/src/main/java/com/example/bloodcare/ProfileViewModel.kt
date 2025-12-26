package com.example.bloodcare

import android.net.Uri
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {

    // Activity data
    var name: String? = null
    var mobile: String? = null
    var bloodGroup: String? = null
    var country: String? = null
    var city: String? = null

    // Fragment basic info
    var dob: String? = null
    var age: String? = null
    var gender: String? = null
    var donate: String? = null
    var about: String? = null

    // Image
    var imageUri: Uri? = null
}
