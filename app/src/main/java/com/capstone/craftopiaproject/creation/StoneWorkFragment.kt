package com.capstone.craftopiaproject.creation

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import com.capstone.craftopiaproject.creation.product.Lists
import com.capstone.craftopiaproject.creation.product.Product_Adapter
import com.capstone.craftopiaproject.creation.product.Product_List
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class StoneWorkFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: Product_Adapter
    private lateinit var firestore: FirebaseFirestore

    private var productsList: MutableList<Product_List> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stone_work, container, false)
        recyclerView = view.findViewById(R.id.stoneWorkRecycler)

        recyclerView.layoutManager = GridLayoutManager(context, 2)
        productAdapter = Product_Adapter(productsList)
        recyclerView.adapter = productAdapter

        firestore = FirebaseFirestore.getInstance()

        fetchDataFromFirebase()

        return view
    }

    private fun fetchDataFromFirebase() {
        firestore.collection("products")
            .whereEqualTo("category", "Stonework")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val product = document.toObject(Product_List::class.java)
                    productsList.add(product)
                }
                productAdapter.updateProducts(productsList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()

            }
    }

    companion object {
        fun newInstance() = StoneWorkFragment()
    }
}