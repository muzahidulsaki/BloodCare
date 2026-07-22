package com.example.bloodcare

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bloodcare.databinding.ActivityLoginBinding // ViewBinding import
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. ViewBinding setup
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // 2. Login button click logic
        binding.btnLogin.setOnClickListener {
            // etEmail ID is used from binding as per XML
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validation
            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            // Start login process
            Toast.makeText(this@Login, "Logging in...", Toast.LENGTH_SHORT).show()
            loginUser(email, password)
        }

        // 3. Back button
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 4. Click on signup link
        binding.tvSignup.setOnClickListener {
            goToSignup()
        }
    }

    private fun goToSignup() {
        val intent = Intent(this, Signup::class.java)
        startActivity(intent)
    }

    // XML onClick সাপোর্ট করার জন্য (যদি XML এ onClick="goToSignup" থাকে)
    fun goToSignup(view: View) {
        goToSignup()
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // If successful
                    Log.d("LOGIN_DEBUG", "Login Successful")
                    Toast.makeText(this@Login, "Login Successful", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@Login, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    // If failed
                    val exception = task.exception
                    Log.e("LOGIN_DEBUG", "Login Failed", exception)
                    Toast.makeText(this@Login, "Failed: ${exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                // If network error occurs
                Log.e("LOGIN_DEBUG", "Network/System Error", e)
                Toast.makeText(this@Login, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}