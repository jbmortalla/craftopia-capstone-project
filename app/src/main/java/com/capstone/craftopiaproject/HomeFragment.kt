package com.capstone.craftopiaproject

import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.category.Home_Adapter
import com.capstone.craftopiaproject.category.Home_List
import com.capstone.craftopiaproject.category.lists

class HomeFragment : Fragment() {
    private lateinit var adapter: Home_Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBarEditText = view.findViewById<EditText>(R.id.search_bar_edit_text)
        val searchBarButton = view.findViewById<Button>(R.id.search_bar_button)

        searchBarButton.setOnClickListener {
            val searchText = searchBarEditText.text.toString()
            adapter.filter(searchText)
        }

        searchBarEditText.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchBarEditText.right - searchBarEditText.compoundDrawables[drawableRight].bounds.width())) {
                    searchBarEditText.text.clear()
                    adapter.filter("")
                    return@setOnTouchListener true
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

        val homeRecyclerView = view.findViewById<RecyclerView>(R.id.homeRecycler)
        homeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        prepareCategoryData()

        adapter = Home_Adapter(requireContext(), lists.getCategory())
        homeRecyclerView.adapter = adapter
    }

    private fun prepareCategoryData() {
        if (lists.getCategory().isEmpty()) {
            lists.addCategory(Home_List(R.drawable.embroidery, "Category 1"))
            lists.addCategory(Home_List(R.drawable.sculpt, "Category 2"))
            lists.addCategory(Home_List(R.drawable.paintings, "Category 3"))
            lists.addCategory(Home_List(R.drawable.headstones, "Category 4"))
        }
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
