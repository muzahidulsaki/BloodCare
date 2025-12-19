package com.example.bloodcare

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bloodcare.databinding.ActivityMainBinding
import com.example.bloodcare.fragments.ProfileMenuFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @SuppressLint("SetTextI18n")
    private var isMenuOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // বাইন্ডিং অবজেক্ট তৈরি এবং লেআউট সেট করা
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.profileImage.setOnClickListener {
            openMenu()
        }
        binding.userName.setOnClickListener {
            openMenu()
        }

        // 🩸 Blood Donor কার্ডের জন্য OnClickListener
        binding.cardBloodDonor.setOnClickListener {
            val intent = Intent(this, DonorActivity::class.java)
            startActivity(intent)
        }

        // 💉 Blood Recipient কার্ডের জন্য OnClickListener
        binding.cardRecipient.setOnClickListener {
            val intent = Intent(this, RecipientActivity::class.java)
            startActivity(intent)
        }

        // 📝 Create Post কার্ডের জন্য OnClickListener
        binding.cardCreatePost.setOnClickListener {
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        // ❤️ Blood Given কার্ডের জন্য OnClickListener
        binding.cardGiven.setOnClickListener {
            val intent = Intent(this, BloodGivenActivity::class.java)
            startActivity(intent)
        }

        // ইউজারনেম এবং স্ট্যাটাস সেট করা
        binding.userName.text = "Muzahidul Islam Saki"
        binding.donateStatus.text = "Donate Blood: On"

        // সার্চ বাটনের জন্য OnClickListener
        binding.searchBlood.setOnClickListener {
            Toast.makeText(this, "Search feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMenu() {
        val fragment = ProfileMenuFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.menuContainer, fragment)
            .commit()

        findViewById<View>(R.id.mainScroll).visibility = View.GONE
        findViewById<View>(R.id.menuContainer).visibility = View.VISIBLE
    }

    fun closeMenu() {
        findViewById<View>(R.id.menuContainer).visibility = View.GONE
        findViewById<View>(R.id.mainScroll).visibility = View.VISIBLE
    }


    // Fragment Load Helper
    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.menuContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}