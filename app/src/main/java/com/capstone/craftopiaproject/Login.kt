package com.capstone.craftopiaproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.btn_enter)
        "val signUpButton = findViewById<TextView>(R.id.clickHere)"
        val emailInput = findViewById<TextInputEditText>(R.id.text_user)
        val passwordInput = findViewById<TextInputEditText>(R.id.text_pass)
        val showPasswordCheckBox = findViewById<CheckBox>(R.id.checkBox)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, Content::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this, "Authentication failed: " + task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }
}