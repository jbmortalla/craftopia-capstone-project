package com.capstone.craftopiaproject.creation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import com.capstone.craftopiaproject.creation.product.ProductDetailFragment
import com.capstone.craftopiaproject.creation.product.Product_Adapter
import com.capstone.craftopiaproject.creation.product.Product_List
import com.google.firebase.firestore.FirebaseFirestore

class TextilesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: Product_Adapter
    private lateinit var firestore: FirebaseFirestore

    private var productsList: MutableList<Product_List> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_textiles, container, false)
        recyclerView = view.findViewById(R.id.textilesRecycler)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productAdapter = Product_Adapter(productsList) { product ->
            product.productId?.let { navigateToProductDetail(it) }
        }
        recyclerView.adapter = productAdapter

        firestore = FirebaseFirestore.getInstance()

        val backButton: ImageButton = view.findViewById(R.id.textile_backbutton)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        fetchDataFromFirebase()

        return view
    }

    private fun navigateToProductDetail(productId: String) {
        val productDetailFragment = ProductDetailFragment.newInstance(productId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, productDetailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun fetchDataFromFirebase() {
        firestore.collection("products")
            .whereEqualTo("category", "Textiles and Fabrics")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val productId = document.id // Get the document ID as productId
                    val product = document.toObject(Product_List::class.java)
                    product.productId = productId // Assign the productId to the product
                    productsList.add(product)
                }
                productAdapter.updateProducts(productsList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        fun newInstance() = TextilesFragment()
    }
}