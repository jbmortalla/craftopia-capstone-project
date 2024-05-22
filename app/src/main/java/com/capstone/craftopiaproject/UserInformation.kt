package com.capstone.craftopiaproject

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserInformation : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var imageProfile: ImageView
    private lateinit var logoutButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_information, container, false)

        nameTextView = view.findViewById(R.id.userName)
        emailTextView = view.findViewById(R.id.userEmail)
        typeTextView = view.findViewById(R.id.userType)
        imageProfile = view.findViewById(R.id.profile)
        logoutButton = view.findViewById(R.id.logoutButton)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = firestore.collection("user").document(userId)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val name = documentSnapshot.getString("name")
                    val email = documentSnapshot.getString("email")
                    val type = documentSnapshot.getString("type")
                    val imageUrl = documentSnapshot.getString("imageUrl") // Get imageUrl from Firestore

                    nameTextView.text = name
                    emailTextView.text = email
                    typeTextView.text = type

                    if (imageUrl != null) {
                        Glide.with(this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(imageProfile)
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve user data!", Toast.LENGTH_SHORT).show()
            }
        }

        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        firebaseAuth.signOut()
        startActivity(Intent(requireContext(), Login::class.java))
        requireActivity().finish()
        Toast.makeText(requireContext(), "Logout successfully!", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = UserInformation()
    }
}
