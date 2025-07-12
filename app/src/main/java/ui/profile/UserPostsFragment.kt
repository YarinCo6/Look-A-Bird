package com.example.look_a_bird.ui.profile

import Post
import android.os.Bundle
import android.util.Log
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
import com.example.look_a_bird.ui.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoPosts: TextView
    private lateinit var textPostCount: TextView
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
        loadUserPosts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_user_posts)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        textNoPosts = view.findViewById(R.id.text_no_posts)
        textPostCount = view.findViewById(R.id.text_post_count)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val postPair = postAdapter.getPost(position)
                val post = postPair.second
                Toast.makeText(context, "Clicked: ${post.birdSpecies}", Toast.LENGTH_SHORT).show()
            }

            override fun onMapClick(latitude: Double, longitude: Double) {
                Toast.makeText(context, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_SHORT).show()
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
                val posts = mutableListOf<Pair<String, Post>>()

                for (document in documents) {
                    try {
                        val post = document.toObject(Post::class.java)
                        posts.add(Pair(document.id, post))
                    } catch (e: Exception) {
                        Log.e("Firestore", "Error parsing post: ${e.message}")
                        continue
                    }
                }

                updateUI(posts)
                showLoading(false)
                swipeRefresh.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error loading posts: ${exception.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
                swipeRefresh.isRefreshing = false
                updateUI(emptyList())
            }
    }

    private fun updateUI(posts: List<Pair<String, Post>>) {
        if (posts.isEmpty()) {
            textNoPosts.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            textPostCount.visibility = View.GONE
        } else {
            textNoPosts.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            textPostCount.visibility = View.VISIBLE
            textPostCount.text = "No. of posts: ${posts.size}" // <-- NEW
            postAdapter.setPosts(posts)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToEditPost(postPair: Pair<String, Post>) {
        val postId = postPair.first
        try {
            val action = UserPostsFragmentDirections.actionUserPostsToEditPost(postId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
