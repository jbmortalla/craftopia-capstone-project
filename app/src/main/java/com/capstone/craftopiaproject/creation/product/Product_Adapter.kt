package com.capstone.craftopiaproject.creation.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class Product_Adapter(private var products: List<Product_List>) :
    RecyclerView.Adapter<Product_Adapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_Title)
        private val productPrice: TextView = itemView.findViewById(R.id.product_Price)
        private val productImage: ImageView = itemView.findViewById(R.id.product_image)

        fun bind(product: Product_List) {
            productName.text = product.name
            productPrice.text = product.price.toString()

            LoadImageTask(product.imageLink, productImage).execute()
        }

        private class LoadImageTask(private val imageUrl: String, private val imageView: ImageView) : AsyncTask<Void, Void, Bitmap?>() {
            override fun doInBackground(vararg params: Void?): Bitmap? {
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

            override fun onPostExecute(result: Bitmap?) {
                if (result != null) {
                    imageView.setImageBitmap(result)
                } else {
                    imageView.setImageResource(R.drawable.avatar)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_recycler, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product_List>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
