package com.example.bloodcare

import BasicInfoFragment
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class Profile : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etMobile: EditText
    private lateinit var ddGroup: TextView
    private lateinit var ddCountry: TextView
    private lateinit var ddCity: TextView
    private lateinit var btnNext: Button
    private lateinit var btnBack: ImageView

    // ✅ Shared ViewModel
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // ✅ ViewModel init
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // View init
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

        // Blood Group Dropdown
        ddGroup.setOnClickListener {
            val popup = PopupMenu(this, ddGroup)
            val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
            bloodGroups.forEach { popup.menu.add(it) }

            popup.setOnMenuItemClickListener {
                ddGroup.text = it.title
                checkInputs()
                true
            }
            popup.show()
        }

        // Country
        ddCountry.setOnClickListener {
            val popup = PopupMenu(this, ddCountry)
            popup.menu.add("Bangladesh")

            popup.setOnMenuItemClickListener {
                ddCountry.text = it.title
                checkInputs()
                true
            }
            popup.show()
        }

        // City
        val districts = resources.getStringArray(R.array.bd_districts)
        ddCity.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Select City")
                .setItems(districts) { _, which ->
                    ddCity.text = districts[which]
                    checkInputs()
                }
                .show()
        }

        // ✅ NEXT BUTTON
        btnNext.setOnClickListener {

            // 🔥 STEP-2: Activity data → ViewModel
            viewModel.name = etName.text.toString().trim()
            viewModel.mobile = etMobile.text.toString().trim()
            viewModel.bloodGroup = ddGroup.text.toString()
            viewModel.country = ddCountry.text.toString()
            viewModel.city = ddCity.text.toString()

            // Debug (optional)
            Toast.makeText(this, "Data saved in ViewModel", Toast.LENGTH_SHORT).show()

            // Fragment open
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, BasicInfoFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun checkInputs() {
        val name = etName.text.toString().trim()
        val mobile = etMobile.text.toString().trim()
        val group = ddGroup.text.toString().trim()
        val country = ddCountry.text.toString().trim()
        val city = ddCity.text.toString().trim()

        btnNext.isEnabled =
            name.isNotEmpty() &&
                    mobile.matches(Regex("01[3-9]\\d{8}")) &&
                    group.isNotEmpty() &&
                    country.isNotEmpty() &&
                    city.isNotEmpty()
    }
}
