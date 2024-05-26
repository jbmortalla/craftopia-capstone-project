package com.capstone.craftopiaproject.transaction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import com.capstone.craftopiaproject.ViewContent.Companion.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class CartAdapter(
    private val orderList: MutableList<CartList>,
    private val onDeleteClicked: () -> Unit,
    private val onPlusButtonClicked: (position: Int) -> Unit,
    private val onMinusButtonClicked: (position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.OrderViewHolder>() {

    var currentList: List<CartList> = orderList.toList()

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val cartSubCategory: TextView = itemView.findViewById(R.id.cartSubCategory)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val cartCheckbox: CheckBox = itemView.findViewById(R.id.cartCheckbox)
        val plusButton: TextView = itemView.findViewById(R.id.plusButton)
        val minusButton: TextView = itemView.findViewById(R.id.minusButton)
        val quantityText: TextView = itemView.findViewById(R.id.quantity)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentItem = currentList[position]
        holder.productName.text = currentItem.name
        holder.productPrice.text = "₱${ String.format("%.2f", currentItem.price) }"
        holder.quantity.text = currentItem.quantity.toString()
        holder.cartCheckbox.isChecked = currentItem.isSelected
        holder.cartSubCategory.text = currentItem.subcategory

        holder.cartCheckbox.setOnCheckedChangeListener { _, isChecked ->
            currentItem.isSelected = isChecked
            onDeleteClicked()
        }

        holder.plusButton.setOnClickListener {
            onPlusButtonClicked(position)
            currentItem.quantity+1
            holder.quantity.text = currentItem.quantity.toString()
        }

        holder.minusButton.setOnClickListener {
            onMinusButtonClicked(position)
            if (currentItem.quantity > 0) {
                currentItem.quantity-1
                holder.quantity.text = currentItem.quantity.toString()
            }
        }

        LoadImageTask(holder.productImage).execute(currentItem.imageUrl)
    }

    fun getSelectedItems(): List<CartList> {
        return currentList.filter { it.isSelected }
    }

    fun removeItems(itemsToRemove: List<CartList>) {
        orderList.removeAll(itemsToRemove)
        currentList = orderList.toList()
        notifyDataSetChanged()
    }

    override fun getItemCount() = currentList.size

    private class LoadImageTask(private val imageView: ImageView) :
        AsyncTask<String, Void, Bitmap?>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            val imageUrl = params[0]
            Log.d("LoadImageTask", "Image URL: $imageUrl")
            return try {
                if (imageUrl.isNullOrEmpty()) {
                    Log.e("LoadImageTask", "Image URL is null or empty")
                    null
                } else {
                    val url = URL(imageUrl)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    val inputStream: InputStream = connection.inputStream
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: IOException) {
                Log.e("LoadImageTask", "Error loading image: ${e.message}")
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            if (result != null) {
                imageView.setImageBitmap(result)
            } else {
                Log.e("LoadImageTask", "Bitmap is null for imageView: $imageView")
            }
        }
    }
}