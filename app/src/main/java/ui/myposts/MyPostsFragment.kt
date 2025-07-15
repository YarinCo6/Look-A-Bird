package com.example.look_a_bird.ui.myposts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
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
import kotlinx.coroutines.launch

class MyPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoPosts: TextView
    private lateinit var postAdapter: PostAdapter
    private lateinit var repository: Repository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize repository
        repository = (requireActivity() as MainActivity).getRepository()

        setupViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        observePosts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_my_posts)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        textNoPosts = view.findViewById(R.id.text_no_posts)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
            }

            override fun onMapClick(latitude: Double, longitude: Double) {
                val action = MyPostsFragmentDirections.actionMyPostsToPostMap(
                    latitude.toFloat(),
                    longitude.toFloat()
                )
                findNavController().navigate(action)
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

    private fun observePosts() {
        showLoading(true)

        // Observe all posts from repository
        repository.getAllPosts().observe(viewLifecycleOwner) { posts ->
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
                // handle error silently
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI(posts: List<Post>) {
        if (posts.isEmpty()) {
            textNoPosts.visibility = View.VISIBLE
            textNoPosts.text = "No bird sightings shared yet.\nBe the first to share!"
            recyclerView.visibility = View.GONE
        } else {
            textNoPosts.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            postAdapter.setPosts(posts)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}