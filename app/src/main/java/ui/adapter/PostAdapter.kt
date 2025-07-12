package com.example.look_a_bird.ui.adapter

import Post
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.look_a_bird.R
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import com.bumptech.glide.Glide

class PostAdapter : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onMapClick(latitude: Double, longitude: Double)
    }

    private var posts: List<Pair<String, Post>> = listOf()
    private var listener: OnItemClickListener? = null

    fun setPosts(posts: List<Pair<String, Post>>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    fun getPost(position: Int): Pair<String, Post> = posts[position]

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

        fun bind(item: Pair<String, Post>) {
            val post = item.second

            textUserName.text = post.userName
            textTimestamp.text = formatTimestamp(post.timestamp)
            textDescription.text = post.description

            textBirdSpecies.text = if (post.scientificName.isNotEmpty()) {
                "${post.birdSpecies} (${post.scientificName})"
            } else {
                post.birdSpecies
            }

            if (post.imageUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(imagePost)
            } else {
                imagePost.setImageDrawable(null)
            }

            if (post.userProfileImage.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(post.userProfileImage)
                    .circleCrop()
                    .into(imageUserProfile)
            } else {
                imageUserProfile.setImageDrawable(null)
            }

            buttonShowOnMap.setOnClickListener {
                listener?.onMapClick(post.latitude, post.longitude)
            }
        }

        private fun formatTimestamp(timestamp: Timestamp?): String {
            return if (timestamp != null) {
                val date = timestamp.toDate()
                val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                format.format(date)
            } else {
                "No Date"
            }
        }
    }
}
