package com.capstone.craftopiaproject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.rpc.Help

class Login : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var signUpButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        val loginButton = findViewById<Button>(R.id.btn_enter)

        val emailInput = findViewById<TextInputEditText>(R.id.text_user)
        val passwordInput = findViewById<TextInputEditText>(R.id.text_pass)
        val showPasswordCheckBox = findViewById<CheckBox>(R.id.checkBox)

        showPasswordCheckBox.setOnClickListener{
            if(showPasswordCheckBox.isChecked){
                passwordInput.inputType = 1
            }
            else
                passwordInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

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

        // Link setup
        signUpButton = findViewById(R.id.clickHere)
        val text = "New to Craftopia? Click Here"
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Login, Register::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left)
            }
        }
        val startIndex = text.indexOf("Click Here")
        val endIndex = startIndex + "Click Here".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        signUpButton.text = spannableString
        signUpButton.movementMethod = LinkMovementMethod.getInstance()
        signUpButton.highlightColor = Color.TRANSPARENT
    }
}