package com.capstone.craftopiaproject

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.category.Home_Adapter
import com.capstone.craftopiaproject.category.Home_List
import com.capstone.craftopiaproject.category.lists
import com.capstone.craftopiaproject.creation.ArtVisualFragment
import com.capstone.craftopiaproject.creation.StoneWorkFragment
import com.capstone.craftopiaproject.creation.TextilesFragment
import com.capstone.craftopiaproject.creation.WoodWorkingFragment
import com.capstone.craftopiaproject.creation.product.CreateFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {
    private lateinit var adapter: Home_Adapter
    private var userType: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var addProductButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val searchBarEditText = view.findViewById<EditText>(R.id.search_bar_edit_text)
        val searchBarButton = view.findViewById<Button>(R.id.search_bar_button)
        addProductButton = view.findViewById(R.id.addProduct)

        addProductButton.setOnClickListener {
            val fragment = CreateFragment.newInstance()
            (activity as? ViewContent)?.loadFragment(fragment)
        }

        searchBarButton.setOnClickListener {
            val searchText = searchBarEditText.text.toString()
            adapter.filter(searchText)
        }

        // Close icon inside the EditText
        searchBarEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val drawableRight = if (s.isNullOrEmpty()) null else resources.getDrawable(R.drawable.baseline_close_24, null)
                searchBarEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchBarEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (searchBarEditText.compoundDrawables[2] != null) {
                    if (event.rawX >= (searchBarEditText.right - searchBarEditText.compoundDrawables[2].bounds.width())) {
                        searchBarEditText.text.clear()
                        adapter.filter("")
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }

        val toolbar = view.findViewById<Toolbar>(R.id.tolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = ""

        val drawerToggle = view.findViewById<ImageButton>(R.id.drawerToggle)
        val drawerLayout = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerToggle?.setOnClickListener {
            drawerLayout?.openDrawer(GravityCompat.START)
        }

        if (userType == "Customer") {
            addProductButton.visibility = View.GONE
        }

        val homeRecyclerView = view.findViewById<RecyclerView>(R.id.homeRecycler)
        homeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        prepareCategoryData()

        adapter = Home_Adapter(requireContext(), lists.getCategory()) { item ->
            navigateToFragment(item.categoryName)
        }
        homeRecyclerView.adapter = adapter

        retrieveUserType()
    }

    private fun prepareCategoryData() {
        if (lists.getCategory().isEmpty()) {
            lists.addCategory(Home_List(R.drawable.embroidery, "Textiles and Fabrics"))
            lists.addCategory(Home_List(R.drawable.sculpt, "Woodworking"))
            lists.addCategory(Home_List(R.drawable.paintings, "Art & Visual Creations"))
            lists.addCategory(Home_List(R.drawable.headstones, "Stonework"))
        }
    }

    private fun navigateToFragment(categoryName: String) {
        val fragment = when (categoryName) {
            "Textiles and Fabrics" -> TextilesFragment.newInstance()//Category Type
            "Woodworking" -> WoodWorkingFragment.newInstance() //Category Type
            "Art & Visual Creations" -> ArtVisualFragment.newInstance() //Category Type
            "Stonework" -> StoneWorkFragment.newInstance() //Category Type
            else -> return
        }
        (activity as? ViewContent)?.loadFragment(fragment)
    }

    private fun retrieveUserType() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("user").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    userType = document.getString("type")
                    // Hide addProduct button if user type is Customer
                    if (userType == "Customer") {
                        addProductButton.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ViewContent.TAG, "Failed to retrieve user type: $exception")
                }
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
