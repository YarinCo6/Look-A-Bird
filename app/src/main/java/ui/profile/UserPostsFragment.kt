package com.example.look_a_bird.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.look_a_bird.R
import com.example.look_a_bird.model.Post
import com.example.look_a_bird.ui.adapter.PostAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAddPost: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoPosts: TextView
    private lateinit var postAdapter: PostAdapter

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()

        // Load user's posts on start
        loadUserPosts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_user_posts)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        fabAddPost = view.findViewById(R.id.fab_add_post)
        progressBar = view.findViewById(R.id.progress_bar)
        textNoPosts = view.findViewById(R.id.text_no_posts)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val post = postAdapter.getPost(position)
                // Navigate to edit post (since these are user's own posts)
                navigateToEditPost(post)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = postAdapter
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            loadUserPosts()
        }
    }

    private fun setupFab() {
        fabAddPost.setOnClickListener {
            // Navigate to Add Post screen
            try {
                findNavController().navigate(R.id.addPostFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserPosts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to view your posts", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        db.collection("posts")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = mutableListOf<Post>()

                for (document in documents) {
                    try {
                        val post = document.toObject(Post::class.java)
                        post.id = document.id
                        posts.add(post)
                    } catch (e: Exception) {
                        continue
                    }
                }

                updateUI(posts)
                showLoading(false)
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error loading your posts: ${exception.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
                swipeRefresh.isRefreshing = false
                updateUI(emptyList())
            }
    }

    private fun updateUI(posts: List<Post>) {
        if (posts.isEmpty()) {
            textNoPosts.visibility = View.VISIBLE
            textNoPosts.text = "You haven't posted any bird sightings yet.\nTap + to share your first sighting!"
            recyclerView.visibility = View.GONE
        } else {
            textNoPosts.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            postAdapter.setPosts(posts) // FIXED: Use setPosts() instead of updatePosts()
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToEditPost(post: Post) {
        try {
            // Navigate to Edit Post with Safe Args
            val action = UserPostsFragmentDirections.actionUserPostsToEditPost(post.id)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Edit functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}