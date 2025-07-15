package com.example.look_a_bird.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.look_a_bird.R
import com.example.look_a_bird.model.Post
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onMapClick(latitude: Double, longitude: Double)
    }

    private var posts: List<Post> = emptyList()
    private var listener: OnItemClickListener? = null

    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    fun getPost(position: Int): Post = posts[position]

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

    class PostViewHolder(
        itemView: View,
        private val listener: OnItemClickListener?
    ) : RecyclerView.ViewHolder(itemView) {

        private val imageUserProfile: ImageView = itemView.findViewById(R.id.image_user_profile)
        private val textUserName: TextView = itemView.findViewById(R.id.text_user_name)
        private val textTimestamp: TextView = itemView.findViewById(R.id.text_timestamp)
        private val textBirdSpecies: TextView = itemView.findViewById(R.id.text_bird_species)
        private val textDescription: TextView = itemView.findViewById(R.id.text_description)
        private val imagePost: ImageView = itemView.findViewById(R.id.image_post)
        private val buttonShowOnMap: Button = itemView.findViewById(R.id.button_show_on_map)

        init {
            itemView.setOnClickListener {
                listener?.onItemClick(adapterPosition)
            }
        }

        fun bind(post: Post) {
            textUserName.text = post.userName
            textTimestamp.text = formatTimestamp(post.timestamp)
            textDescription.text = post.description

            textBirdSpecies.text = if (post.scientificName.isNotEmpty()) {
                "${post.birdSpecies} (${post.scientificName})"
            } else {
                post.birdSpecies
            }

            loadImage(post.imageUrl, imagePost, centerCrop = true)
            loadImage(post.userProfileImage, imageUserProfile, centerCrop = false)

            buttonShowOnMap.setOnClickListener {
                listener?.onMapClick(post.latitude, post.longitude)
            }
        }

        private fun loadImage(imageUrl: String, imageView: ImageView, centerCrop: Boolean) {
            if (imageUrl.isNotEmpty()) {
                imageView.load(imageUrl) {
                    crossfade(true)
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                    if (centerCrop) {
                        scale(coil.size.Scale.FILL)
                    } else {
                        transformations(CircleCropTransformation())
                    }
                }
            } else {
                imageView.setImageDrawable(null)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            return if (timestamp > 0) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = Date(timestamp * 1000)
                sdf.format(date)
            } else {
                "No Date"
            }
        }
    }
}
