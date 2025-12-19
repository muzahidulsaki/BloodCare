package com.example.bloodcare.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.bloodcare.MainActivity
import com.example.bloodcare.Profile
import com.example.bloodcare.R

class ProfileMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_menu, container, false)

        // Close button
        val btnClose = view.findViewById<ImageView>(R.id.btnCloseMenu)
        btnClose.setOnClickListener {
            (activity as MainActivity).closeMenu()
        }

        val editIcon = view.findViewById<ImageView>(R.id.imageView2)

        editIcon.setOnClickListener {
            // নোট: Fragment এ 'this' এর বদলে 'activity' বা 'requireContext()' ব্যবহার করতে হয়
            // 'TargetActivity' এর জায়গায় আপনি যে পেজে যেতে চান তার নাম লিখুন (যেমন: EditProfileActivity::class.java)

            val intent = Intent(activity, Profile::class.java)
            startActivity(intent)
        }

        return view
    }
}
