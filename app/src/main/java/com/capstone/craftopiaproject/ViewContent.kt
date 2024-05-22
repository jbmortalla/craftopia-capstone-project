package com.capstone.craftopiaproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.capstone.craftopiaproject.creation.product.ProductDetailFragment
import com.capstone.craftopiaproject.menu.WorkshopFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ViewContent : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var userType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_content)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        retrieveUserType()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navbar_bottom)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        loadFragment(UserInformation.newInstance())
        bottomNavigationView.selectedItemId = R.id.profile

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.home -> {
                    selectedFragment = HomeFragment.newInstance()
                }
                R.id.nav -> {
                    selectedFragment = MapsFragment()
                }
                R.id.fav -> {
                    selectedFragment = FavoritesFragment.newInstance()
                }
                R.id.order -> {
                    selectedFragment = OrderFragment.newInstance()
                }
                R.id.profile -> {
                    selectedFragment = UserInformation.newInstance()
                }
            }
            selectedFragment?.let {
                loadFragment(it)
            }
            true
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (userType == "Artist") {
                when (menuItem.itemId) {
                    R.id.workshop -> {
                        loadFragment(WorkshopFragment.newInstance())
                    }
                }
            }else {
                Toast.makeText(this, "You are a Customer, this menu is only meant for Arist.", Toast.LENGTH_SHORT).show()
            }
            true
        }

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.frameLayout)

        // Check if the current fragment is ProductDetailFragment
        if (fragment is ProductDetailFragment) {
            // If ProductDetailFragment is visible, navigate back without adding it to the back stack
            super.onBackPressed()
        } else {
            // Otherwise, proceed with default back navigation behavior
            super.onBackPressed()
        }
    }

    fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun retrieveUserType() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("user").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    userType = document.getString("type")
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Failed to retrieve user type: $exception")
                }
        }
    }

    companion object {
        const val TAG = "ViewContent"
    }
}