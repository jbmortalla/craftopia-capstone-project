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
    private val homeList: List<Home_List>
) : RecyclerView.Adapter<Home_Adapter.HomeViewHolder>() {

    class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryImage: ImageView = itemView.findViewById(R.id.category_image)
        val categoryName: TextView = itemView.findViewById(R.id.category_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_recycler, parent, false)
        return HomeViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val homeItem = homeList[position]
        holder.categoryImage.setImageResource(homeItem.categoryImg)
        holder.categoryName.text = homeItem.categoryName
    }

    override fun getItemCount(): Int {
        return homeList.size
    }
}