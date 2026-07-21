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

        // Cloudinary সেটআপ (ফাংশনটি নিচে কল করা হয়েছে)
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

    // ১. Cloudinary ইনিশিলাইজ করার ফাংশন
    private fun initCloudinary() {
        try {
            val config = HashMap<String, String>()
            // FIXME: ড্যাশবোর্ড থেকে আপনার তথ্যগুলো নিচে বসান
            config["cloud_name"] = "dr00dyggw"
            config["api_key"] = "142699121148985"
            config["api_secret"] = "YFwJGHjJnQQzctLJX6LO0H48L0Q"

            MediaManager.init(requireContext(), config)
        } catch (e: Exception) {
            // যদি আগে থেকেই ইনিশিলাইজ করা থাকে, তাহলে এরর ইগনোর করুন
        }
    }

    // ২. ছবি আপলোড করার ফাংশন
    private fun uploadImageToCloudinary() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(requireContext(), "আপলোড হচ্ছে, দয়া করে অপেক্ষা করুন...", Toast.LENGTH_SHORT).show()

        MediaManager.get().upload(selectedImageUri)
            // FIXME: নিচে আপনার Unsigned Upload Preset এর নাম দিন
            .unsigned("my_app_preset")
            .option("public_id", userId) // ছবির নাম হবে ইউজারের ID
            // ✅ নতুন অপ্টিমাইজেশন কোড শুরু
            .option("resource_type", "image") // নিশ্চিত করা এটি ছবি
            .option("quality", "auto")        // কোয়ালিটি অটোমেটিক অ্যাডজাস্ট হবে (Size কমবে)
            .option("width", 800)             // ছবির চওড়া ৮০০ পিক্সেলের বেশি হবে না
            .option("crop", "limit")          // ছবি ক্রপ না করে শুধু সাইজ ছোট করবে
            // ✅ নতুন অপ্টিমাইজেশন কোড শেষ
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // আপলোড শুরু হলে কিছু করতে চাইলে এখানে লিখুন
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // প্রোগ্রেস বার দেখাতে চাইলে এখানে কোড করতে পারেন
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    // সফল হলে URL পাওয়া যাবে
                    val imageUrl = resultData["secure_url"].toString()

                    // URL পাওয়ার পর ডাটাবেসে সেভ করা হবে
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

    // ৩. ডাটাবেসে তথ্য সেভ করার ফাংশন
    private fun saveDataToRealtimeDatabase(userId: String, imageUrl: String) {
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "No Email Found"
        val userData = mapOf(
            "name" to viewModel.name,
            "mobile" to viewModel.mobile,
            "email" to userEmail, // ✅ এখানে সেই ইমেইলটি ডাটাবেসে পাঠানো হলো
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