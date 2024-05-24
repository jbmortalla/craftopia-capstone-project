package com.capstone.craftopiaproject.creation.product

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ProductDetailFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)

        val productId = arguments?.getString("productId")

        if (productId != null) {
            db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val productId = document.id // Get the document ID as productId
                        val product = document.toObject(Product_List::class.java)
                        if (product != null) {
                            product.productId = productId
                            Log.d("ProductDetailFragment", "Product: $product")
                            populateProductDetails(view, product)
                        } else {
                            Toast.makeText(context, "Product details are not available", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Product not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ProductDetailFragment", "Error fetching product details: ${exception.message}")
                    Toast.makeText(context, "Error fetching product details", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.e("ProductDetailFragment", "Product ID is null")
            Toast.makeText(context, "Product ID is null", Toast.LENGTH_SHORT).show()
        }

        val backButton: ImageButton = view.findViewById(R.id.delailbackbutton)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val feedbackButton: ImageButton = view.findViewById(R.id.feedbackButton)
        feedbackButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid
                val userRef = db.collection("user").document(userId)
                userRef.get().addOnSuccessListener { documentSnapshot ->
                    val userName = documentSnapshot.getString("name")
                    val imageUrl = documentSnapshot.getString("imageUrl")

                    val dialogView = layoutInflater.inflate(R.layout.dialog_feedback, null)
                    val feedbackEditText = dialogView.findViewById<EditText>(R.id.feedbackEditText)
                    val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

                    AlertDialog.Builder(context)
                        .setTitle("Write a Feedback")
                        .setView(dialogView)
                        .setPositiveButton("Submit") { _, _ ->
                            val feedbackMessage = feedbackEditText.text.toString()
                            val rating = ratingBar.rating
                            val timestamp = System.currentTimeMillis()

                            val feedback = Feedback(
                                userId = userId,
                                name = userName,
                                feedback = feedbackMessage,
                                rating = rating,
                                timestamp = timestamp,
                                imageUrl = imageUrl
                            )

                            if (productId != null) {
                                db.collection("products")
                                    .document(productId)
                                    .collection("feedback")
                                    .add(feedback)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Feedback submitted", Toast.LENGTH_SHORT).show()
                                        setupRecyclerView(view)
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Failed to submit feedback: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } else {
                Toast.makeText(context, "Please log in to submit feedback", Toast.LENGTH_SHORT).show()
            }
        }

        setupRecyclerView(view)

        return view
    }

    private fun populateProductDetails(view: View, product: Product_List?) {
        product?.let {
            view.findViewById<TextView>(R.id.detail_name).text = it.name
            view.findViewById<TextView>(R.id.detail_price).text = "₱${it.price}"
            view.findViewById<TextView>(R.id.productDetail).text = it.description

            val detailImageView = view.findViewById<ImageView>(R.id.detailImage)
            LoadImageTask(detailImageView).execute(it.imageLink)

            val ratingBar: RatingBar = view.findViewById(R.id.ratingBarDetail)
            ratingBar.numStars = 5
            ratingBar.stepSize = 0.1f
            ratingBar.rating = 0.toFloat()
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.detailRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val feedbackList = mutableListOf<Feedback>()
        val productId = arguments?.getString("productId") ?: return

        db.collection("products")
            .document(productId)
            .collection("feedback")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val feedback = document.toObject(Feedback::class.java)
                    feedbackList.add(feedback)
                }
                recyclerView.adapter = FeedbackAdapter(feedbackList)

                updateOverallRating(feedbackList, view)
            }
            .addOnFailureListener { exception ->
                Log.e("ProductDetailFragment", "Error fetching feedback: ${exception.message}")
            }
    }

    private fun updateOverallRating(feedbackList: List<Feedback>, view: View) {
        if (feedbackList.isEmpty()) return

        val totalRating = feedbackList.sumByDouble { it.rating.toDouble() }
        val averageRating = totalRating / feedbackList.size

        val ratingBar: RatingBar = view.findViewById(R.id.ratingBarDetail)
        ratingBar.rating = averageRating.toFloat()

        val ratingText: TextView = view.findViewById(R.id.ratingDetailText)
        ratingText.text = "${feedbackList.size} rating${if (feedbackList.size > 1) "s" else ""}"
    }

    private class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            val imageUrl = params[0]
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            result?.let {
                imageView.setImageBitmap(it)
            }
        }
    }

    companion object {
        fun newInstance(productId: String) = ProductDetailFragment().apply {
            arguments = Bundle().apply {
                putString("productId", productId)
            }
        }
    }
}