package com.capstone.craftopiaproject.creation.product

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.craftopiaproject.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class FeedbackAdapter(private val feedbackList: List<Feedback>) : RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder>() {

    inner class FeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val feedbackText: TextView = itemView.findViewById(R.id.feedback_text)
        val feedbackRating: RatingBar = itemView.findViewById(R.id.feedback_rating)
        val feedbackTimestamp: TextView = itemView.findViewById(R.id.feedback_timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedbackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feedback, parent, false)
        return FeedbackViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedbackViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.userName.text = feedback.name
        holder.feedbackText.text = feedback.feedback
        holder.feedbackRating.rating = feedback.rating
        holder.feedbackTimestamp.text = android.text.format.DateFormat.format("dd MMM yyyy", feedback.timestamp)

        if (feedback.imageUrl!!.isNotEmpty()) {
            LoadImageTask(holder.userImage).execute(feedback.imageUrl)
        } else {
            holder.userImage.setImageResource(R.drawable.avatar)
        }
    }

    override fun getItemCount() = feedbackList.size

    private class LoadImageTask(private val imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            val imageUrl = params[0]
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
            result?.let {
                imageView.setImageBitmap(it)
            }
        }
    }
}