package com.example.look_a_bird.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.look_a_bird.MainActivity
import com.example.look_a_bird.R
import com.example.look_a_bird.database.Repository
import com.example.look_a_bird.model.Post
import com.example.look_a_bird.ui.adapter.PostAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class UserPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoPosts: TextView
    private lateinit var textPostCount: TextView
    private lateinit var postAdapter: PostAdapter
    private lateinit var repository: Repository

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

        // Initialize repository
        repository = (requireActivity() as MainActivity).getRepository()

        setupViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        observeUserPosts()
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
                // Navigate to edit post
                val post = postAdapter.getPost(position)
                navigateToEditPost(post.id)
            }

            override fun onMapClick(latitude: Double, longitude: Double) {
                // Navigate to map
                try {
                    val action = UserPostsFragmentDirections.actionUserPostsToPostMap(
                        latitude.toFloat(),
                        longitude.toFloat()
                    )
                    findNavController().navigate(action)
                } catch (e: Exception) {
                    Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = postAdapter
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            refreshPosts()
        }
    }

    private fun observeUserPosts() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Please log in to view your posts", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Observe user's posts from repository
        repository.getPostsByUser(currentUser.uid).observe(viewLifecycleOwner) { posts ->
            showLoading(false)
            swipeRefresh.isRefreshing = false
            updateUI(posts)
        }
    }

    private fun refreshPosts() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                repository.syncPosts()
                repository.syncUsers()
            } catch (e: Exception) {
                Toast.makeText(context, "Error refreshing posts", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(posts: List<Post>) {
        if (posts.isEmpty()) {
            textNoPosts.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            textPostCount.visibility = View.GONE
        } else {
            textNoPosts.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            textPostCount.visibility = View.VISIBLE
            textPostCount.text = "No. of posts: ${posts.size}"
            postAdapter.setPosts(posts)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun navigateToEditPost(postId: String) {
        try {
            val action = UserPostsFragmentDirections.actionUserPostsToEditPost(postId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}