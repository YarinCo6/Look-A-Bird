package com.example.look_a_bird.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.look_a_bird.R
import com.example.look_a_bird.model.Post
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var posts: List<Post> = listOf()
    private var listener: OnItemClickListener? = null

    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun getPost(position: Int): Post = posts[position]

    class PostViewHolder(
        itemView: View,
        private val listener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageUserProfile: ImageView = itemView.findViewById(R.id.image_user_profile)
        private val textUserName: TextView = itemView.findViewById(R.id.text_user_name)
        private val textLocation: TextView = itemView.findViewById(R.id.text_location)
        private val textTimestamp: TextView = itemView.findViewById(R.id.text_timestamp)
        private val textBirdSpecies: TextView = itemView.findViewById(R.id.text_bird_species)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val imagePost: ImageView = itemView.findViewById(R.id.image_post)

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }

        fun bind(post: Post) {
            textUserName.text = post.userName
            textLocation.text = post.location
            textDescription.text = post.description
            textTimestamp.text = formatTimestamp(post.timestamp)

            // Display scientific name if available
            if (post.scientificName.isNotEmpty()) {
                textBirdSpecies.text = "${post.birdSpecies} (${post.scientificName})"
            } else {
                textBirdSpecies.text = post.birdSpecies
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return if (timestamp == 0L) {
                "Now"
            } else {
                val now = System.currentTimeMillis() / 1000
                val diff = now - (timestamp / 1000)

                when {
                    diff < 60 -> "Now"
                    diff < 3600 -> "${diff / 60} minutes ago"
                    diff < 86400 -> "${diff / 3600} hours ago"
                    diff < 604800 -> "${diff / 86400} days ago"
                    else -> {
                        val date = Date(timestamp)
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
                    }
                }
            }
        }
    }
}