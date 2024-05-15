package com.capstone.craftopiaproject

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.security.MessageDigest

class Register : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userId: FirebaseAuth
    private lateinit var storageReference: StorageReference

    private lateinit var loginButton: TextView
    private lateinit var uploadButton: Button
    private lateinit var uploadedImageLink: TextView
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference

        val signUp = findViewById<Button>(R.id.btn_sign)
        val signName = findViewById<TextInputEditText>(R.id.sign_name)
        val signEmail = findViewById<TextInputEditText>(R.id.sign_email)
        val signPass = findViewById<TextInputEditText>(R.id.sign_pass)
        val confirmPass = findViewById<TextInputEditText>(R.id.sign_confirmpass)
        val signType = findViewById<TextInputEditText>(R.id.sign_type)
        uploadButton = findViewById(R.id.uploadButton)
        uploadedImageLink = findViewById(R.id.imageLink)

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        signUp.setOnClickListener {
            val name = signName.text.toString()
            val email = signEmail.text.toString()
            val password = signPass.text.toString()
            val confirmpass = confirmPass.text.toString()
            val type = signType.text.toString()

            if (name.isEmpty()) {
                signName.error = "Name is required"
                signName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                signEmail.error = "Email is required"
                signEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                signPass.error = "Password is required"
                signPass.requestFocus()
                return@setOnClickListener
            }

            if (confirmpass.isEmpty()) {
                confirmPass.error = "Confirmation password is required"
                confirmPass.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmpass) {
                confirmPass.error = "Passwords do not match"
                confirmPass.requestFocus()
                return@setOnClickListener
            }

            if (type.isEmpty()) {
                signType.error = "User type is required"
                signType.requestFocus()
                return@setOnClickListener
            }

            userId.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = userId.currentUser
                        val userId = user!!.uid

                        // Hash the password
                        val hashedPassword = hashPassword(password)

                        val userMap = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "password" to hashedPassword,
                            "type" to type
                        )

                        if (selectedImageUri != null) {
                            uploadImageToFirebase(userId, selectedImageUri!!) { imageUrl ->
                                userMap["imageUrl"] = imageUrl
                                saveUserToFirestore(userId, userMap)
                            }
                        } else {
                            saveUserToFirestore(userId, userMap)
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

        loginButton = findViewById(R.id.clickHere)
        val text = "Already have an account? Click Here"
        val spannableString = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Register, Login::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }
        val startIndex = text.indexOf("Click Here")
        val endIndex = startIndex + "Click Here".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        loginButton.text = spannableString
        loginButton.movementMethod = LinkMovementMethod.getInstance()
        loginButton.highlightColor = Color.TRANSPARENT
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            uploadedImageLink.text = selectedImageUri.toString()
        }
    }

    private fun uploadImageToFirebase(userId: String, imageUri: Uri, callback: (String) -> Unit) {
        val fileRef = storageReference.child("users/$userId/profile.jpg")
        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    callback(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Image Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirestore(userId: String, userMap: HashMap<String, String>) {
        db.collection("user").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully Created an Account!", Toast.LENGTH_SHORT).show()
                clearInputs()
                startActivity(Intent(this, ViewContent::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        findViewById<TextInputEditText>(R.id.sign_name).text?.clear()
        findViewById<TextInputEditText>(R.id.sign_email).text?.clear()
        findViewById<TextInputEditText>(R.id.sign_pass).text?.clear()
        findViewById<TextInputEditText>(R.id.sign_confirmpass).text?.clear()
        findViewById<TextInputEditText>(R.id.sign_type).text?.clear()
        uploadedImageLink.text = ""
    }

    // Function to hash password
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}