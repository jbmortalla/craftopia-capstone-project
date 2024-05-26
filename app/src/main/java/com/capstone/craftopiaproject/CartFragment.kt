package com.capstone.craftopiaproject

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.ViewContent.Companion.TAG
import com.capstone.craftopiaproject.transaction.CartAdapter
import com.capstone.craftopiaproject.transaction.CartList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class CartFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var subTotalTextView: TextView
    private lateinit var taxTextView: TextView
    private lateinit var totalTextView: TextView
    private val orderList = mutableListOf<CartList>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        recyclerView = view.findViewById(R.id.orderRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        subTotalTextView = view.findViewById(R.id.subTotal)
        taxTextView = view.findViewById(R.id.tax)
        totalTextView = view.findViewById(R.id.total)

        val deleteCartProductButton: ImageButton = view.findViewById(R.id.deleteCartProduct)
        deleteCartProductButton.setOnClickListener {
            deleteSelectedItems()
        }

        fetchCartItems()

        return view
    }

    private fun fetchCartItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            FirebaseFirestore.getInstance()
                .collection("user")
                .document(uid)
                .collection("cart")
                .get()
                .addOnSuccessListener { documents ->
                    val cartItems = documents.toObjects(CartList::class.java)
                    updateCartItems(cartItems)
                    calculateAndUpdateTotal(cartItems)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching cart items", e)
                }
        }
    }

    private fun updateCartItems(cartItems: List<CartList>) {
        orderList.clear()
        orderList.addAll(cartItems)
        cartAdapter = CartAdapter(orderList,
            onDeleteClicked = {
                calculateAndUpdateTotal(orderList)
            },
            onPlusButtonClicked = { position ->
                onPlusButtonClicked(position)
                updateQuantityText(position)
            },
            onMinusButtonClicked = { position ->
                onMinusButtonClicked(position)
                updateQuantityText(position)
            }
        )
        recyclerView.adapter = cartAdapter
    }
    private fun calculateAndUpdateTotal(cartItems: List<CartList>) {
        var subtotal = 0.0
        for (item in cartItems) {
            if (item.isSelected) {
                subtotal += item.price * item.quantity
            }
        }
        val tax = 0.15 * subtotal
        val total = subtotal + tax

        subTotalTextView.text = "₱${ String.format(Locale.getDefault(), "%.2f", subtotal) }"
        taxTextView.text = "₱${ String.format(Locale.getDefault(), "%.2f", tax) }"
        totalTextView.text = "₱${ String.format(Locale.getDefault(), "%.2f", total) }"
    }

    private fun deleteSelectedItems() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val selectedItems = cartAdapter.getSelectedItems()

        if (userId != null && selectedItems.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()

            val cartCollectionRef = db.collection("user").document(userId).collection("cart")
            selectedItems.forEach { cartItem ->
                val docRef = cartCollectionRef.whereEqualTo("productId", cartItem.productId)
                docRef.get()
                    .addOnSuccessListener { documents ->
                        for (document in documents) {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    Log.d(TAG, "Document ${document.id} successfully deleted")
                                }
                                .addOnFailureListener { e ->
                                    Log.e(TAG, "Error deleting document ${document.id}", e)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error getting documents", e)
                    }
            }

            Toast.makeText(context, "Selected items deleted", Toast.LENGTH_SHORT).show()
            cartAdapter.removeItems(selectedItems)
            calculateAndUpdateTotal(cartAdapter.currentList)
        } else {
            Toast.makeText(context, "No items selected or user not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPlusButtonClicked(position: Int) {
        val currentItem = orderList[position]
        currentItem.quantity++
        recyclerView.adapter?.notifyItemChanged(position)
        updateQuantityText(position)
    }

    private fun onMinusButtonClicked(position: Int) {
        val currentItem = orderList[position]
        if (currentItem.quantity > 0) {
            currentItem.quantity--
            recyclerView.adapter?.notifyItemChanged(position)
            updateQuantityText(position)
        }
    }

    private fun updateQuantityText(position: Int) {
        val currentItem = orderList[position]
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as? CartAdapter.OrderViewHolder
        viewHolder?.quantityText?.text = currentItem.quantity.toString()
    }

    companion object {
        fun newInstance() = CartFragment()
    }
}