package com.example.bloodcare.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bloodcare.ProfileViewModel
import com.example.bloodcare.R
import com.example.bloodcare.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
// Cloudinary Imports
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.HashMap

class UploadImageFragment : Fragment() {

    private lateinit var ivProfilePreview: ImageView
    private lateinit var ivUploadIcon: ImageView
    private lateinit var tvUploadLabel: TextView
    private lateinit var btnSubmit: AppCompatButton

    private lateinit var viewModel: ProfileViewModel
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                ivProfilePreview.setImageURI(uri)
                ivProfilePreview.visibility = View.VISIBLE
                ivUploadIcon.visibility = View.GONE
                tvUploadLabel.visibility = View.GONE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_upload_image, container, false)

        // Cloudinary setup (function called below)
        initCloudinary()

        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        val uploadArea: View = view.findViewById(R.id.uploadArea)
        ivProfilePreview = view.findViewById(R.id.ivProfilePreview)
        ivUploadIcon = view.findViewById(R.id.ivUploadIcon)
        tvUploadLabel = view.findViewById(R.id.tvUploadLabel)
        btnSubmit = view.findViewById(R.id.btnSubmit)

        uploadArea.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSubmit.setOnClickListener {
            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "প্রোফাইল পিকচার সিলেক্ট করুন", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageToCloudinary()
            }
        }

        return view
    }

    // 1. Cloudinary initialization function
    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            config["cloud_name"] = BuildConfig.CLOUDINARY_CLOUD_NAME
            config["api_key"] = BuildConfig.CLOUDINARY_API_KEY
            config["api_secret"] = BuildConfig.CLOUDINARY_API_SECRET

            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
            // Ignore error if already initialized
        }
    }

    // 2. Image upload function
    private fun uploadImageToCloudinary() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "আপলোড হচ্ছে, দয়া করে অপেক্ষা করুন...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(selectedImageUri)
            .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
            .option("public_id", userId) // ছবির নাম হবে ইউজারের ID
            // New optimization code starts
            .option("resource_type", "image") // Ensure it's an image
            .option("quality", "auto")        // Quality will adjust automatically (reduces size)
            .option("width", 800)             // Image width will not exceed 800 pixels
            .option("crop", "limit")          // Reduce size without cropping
            // New optimization code ends
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // Code to execute when upload starts
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Code for progress bar if needed
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // URL will be available if successful
                    val imageUrl = resultData["secure_url"].toString()

                    // Save to database after getting the URL
                    saveDataToRealtimeDatabase(userId, imageUrl)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Upload Failed: ${error.description}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            })
            .dispatch()
    }

    // 3. Save data to database function
    private fun saveDataToRealtimeDatabase(userId: String, imageUrl: String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "No Email Found"
        val userData = mapOf(
            "name" to viewModel.name,
            "mobile" to viewModel.mobile,
            "email" to userEmail, // Sending that email to the database here
            "bloodGroup" to viewModel.bloodGroup,
            "country" to viewModel.country,
            "city" to viewModel.city,
            "dob" to viewModel.dob,
            "age" to viewModel.age,
            "gender" to viewModel.gender,
            "wantToDonate" to viewModel.donate,
            "profileImage" to imageUrl
        )

        FirebaseDatabase.getInstance()
            .reference
            .child("users")
            .child(userId)
            .setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile completed successfully", Toast.LENGTH_LONG).show()
                requireActivity().finish()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}