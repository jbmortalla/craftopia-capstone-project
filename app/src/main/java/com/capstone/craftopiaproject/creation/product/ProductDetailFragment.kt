package com.capstone.craftopiaproject.creation.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.capstone.craftopiaproject.R
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

            requireActivity().supportFragmentManager.popBackStack()

            requireActivity().onBackPressed()
        }


        return view
    }


    private fun populateProductDetails(view: View, product: Product_List?) {
        product?.let {
            view.findViewById<TextView>(R.id.detail_name).text = it.name
            view.findViewById<TextView>(R.id.detail_price).text = "₱${it.price}"
            view.findViewById<TextView>(R.id.productDetail).text = it.description

            val detailImageView = view.findViewById<ImageView>(R.id.detailImage)
            LoadImageTask(detailImageView).execute(it.imageLink)
        }
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