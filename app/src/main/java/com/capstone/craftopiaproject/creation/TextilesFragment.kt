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
import com.capstone.craftopiaproject.creation.product.Feedback
import com.capstone.craftopiaproject.creation.product.ProductDetailFragment
import com.capstone.craftopiaproject.creation.product.Product_Adapter
import com.capstone.craftopiaproject.creation.product.Product_List
import com.google.firebase.firestore.FirebaseFirestore

class TextilesFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: Product_Adapter
    private lateinit var firestore: FirebaseFirestore

    private lateinit var toolsButton: ImageButton
    private lateinit var creationsButton: ImageButton
    private lateinit var rawMaterialsButton: ImageButton

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

        toolsButton = view.findViewById(R.id.textTools)
        creationsButton = view.findViewById(R.id.textCreations)
        rawMaterialsButton = view.findViewById(R.id.textRawMaterials)

        toolsButton.setOnClickListener {
            fetchDataFromFirebase("Tools")
        }

        creationsButton.setOnClickListener {
            fetchDataFromFirebase("Creations")
        }

        rawMaterialsButton.setOnClickListener {
            fetchDataFromFirebase("Raw Materials")
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

    private fun fetchDataFromFirebase(subcategory: String? = null) {
        var query = firestore.collection("products").whereEqualTo("category", "Textiles and Fabrics")

        if (subcategory != null) {
            query = query.whereEqualTo("subcategory", subcategory)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "No products found", Toast.LENGTH_SHORT).show()
                    productsList.clear()
                    productAdapter.updateProducts(productsList)
                } else {
                    productsList.clear()
                    for (document in documents) {
                        val productId = document.id
                        val product = document.toObject(Product_List::class.java)
                        product.productId = productId
                        fetchFeedbackForProduct(productId) { feedback ->
                            product.feedback = feedback
                            productsList.add(product)
                            if (productsList.size == documents.size()) {
                                productAdapter.updateProducts(productsList)
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchFeedbackForProduct(productId: String, callback: (List<Feedback>) -> Unit) {
        val feedbackList: MutableList<Feedback> = mutableListOf()
        val query = firestore.collection("products").document(productId).collection("feedback")

        query.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val feedback = document.toObject(Feedback::class.java)
                    feedbackList.add(feedback)
                }
                callback(feedbackList)
            }
            .addOnFailureListener { exception ->
                Log.e("FetchFeedback", "Failed to fetch feedback for product $productId: ${exception.message}")
            }
    }

    companion object {
        fun newInstance() = TextilesFragment()
    }
}