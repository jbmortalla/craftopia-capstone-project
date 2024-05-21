package com.capstone.craftopiaproject.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R

class Home_Adapter(
    private val context: Context,
    private var homeList: List<Home_List>,
    private val itemClickListener: (Home_List) -> Unit
) : RecyclerView.Adapter<Home_Adapter.HomeViewHolder>() {

    private var filteredList: List<Home_List> = homeList

    class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_recycler, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val homeItem = filteredList[position]
        holder.categoryImage.setImageResource(homeItem.categoryImg)
        holder.categoryName.text = homeItem.categoryName
        holder.itemView.setOnClickListener { itemClickListener(homeItem) }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(text: String) {
        filteredList = if (text.isEmpty()) {
            homeList
        } else {
            homeList.filter { it.categoryName.contains(text, true) }
        }
        notifyDataSetChanged()
    }
}