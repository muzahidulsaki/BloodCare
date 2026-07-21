package com.example.bloodcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase // ✅ সঠিক ইম্পোর্ট

class Signup : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)
        val btnGoogle = findViewById<MaterialButton>(R.id.btnGoogle)
        val btnFacebook = findViewById<MaterialButton>(R.id.btnFacebook)

        // Back button
        backBtn.setOnClickListener {
            finish()
        }

        // Login redirect
        txtLogin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Signup button
        btnSignup.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All field are required", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                edtPassword.error = "Password must be 6 letter"
            } else {
                createAccount(name, email, password)
            }
        }
    }

    private fun createAccount(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: ""

                    val userMap = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "uid" to userId
                    )

                    //  Firebase Database
                    FirebaseDatabase.getInstance().getReference("users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Database Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}