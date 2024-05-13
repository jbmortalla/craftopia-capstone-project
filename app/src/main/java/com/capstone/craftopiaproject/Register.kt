package com.capstone.craftopiaproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Register : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userId: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance()

        val signUp = findViewById<Button>(R.id.btn_sign)
        val signName = findViewById<TextInputEditText>(R.id.sign_name)
        val signEmail = findViewById<TextInputEditText>(R.id.sign_email)
        val signPass = findViewById<TextInputEditText>(R.id.sign_pass)
        val confirmPass = findViewById<TextInputEditText>(R.id.sign_confirmpass)
        val signType = findViewById<TextInputEditText>(R.id.sign_type)

        signUp.setOnClickListener {
            val name = signName.text.toString()
            val email = signEmail.text.toString()
            val password = signPass.text.toString()
            val confirmpass = confirmPass.text.toString()
            val type = signType.text.toString()

            if (password != confirmpass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userId.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = userId.currentUser
                        val userId = user!!.uid

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "password" to password,
                            "type" to type
                        )

                        db.collection("user").document(userId).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Successfully Created an Account!", Toast.LENGTH_SHORT).show()
                                signName.text?.clear()
                                signEmail.text?.clear()
                                signPass.text?.clear()
                                confirmPass.text?.clear()
                                signType.text?.clear()

                                startActivity(Intent(this, Content::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Authentication failed: " + task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
