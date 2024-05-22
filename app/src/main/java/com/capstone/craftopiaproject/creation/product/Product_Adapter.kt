package com.capstone.craftopiaproject.creation.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class Product_Adapter(
    private var products: List<Product_List>,
    private val onItemClick: (Product_List) -> Unit
) : RecyclerView.Adapter<Product_Adapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_Title)
        private val productPrice: TextView = itemView.findViewById(R.id.product_Price)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)

        fun bind(product: Product_List, onItemClick: (Product_List) -> Unit) {
            productName.text = product.name
            productPrice.text = "₱${ product.price.toString() }"
            product.imageLink?.let { loadImage(it, productImage) }
            itemView.setOnClickListener {
                onItemClick(product)
            }
        }

        private fun loadImage(imageUrl: String, imageView: ImageView) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = loadBitmap(imageUrl)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        imageView.setImageResource(R.drawable.avatar)
                    }
                }
            }
        }

        private fun loadBitmap(imageUrl: String): Bitmap? {
            return try {
                val url = URL(imageUrl)
                val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_recycler, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product, onItemClick)
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product_List>) {
        products = newProducts
        notifyDataSetChanged()
    }
}