package com.capstone.craftopiaproject

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class ViewContent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_content)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navbar_bottom)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

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

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}