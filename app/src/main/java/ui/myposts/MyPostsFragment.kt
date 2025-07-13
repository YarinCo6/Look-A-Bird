package com.example.look_a_bird.ui.myposts

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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.look_a_bird.R
import com.example.look_a_bird.ui.adapter.PostAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.navigation.fragment.findNavController

class MyPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoPosts: TextView
    private lateinit var postAdapter: PostAdapter

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        listenForPosts()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_my_posts)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        textNoPosts = view.findViewById(R.id.text_no_posts)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

        // REMOVED: onItemClick for general feed - users can't click on posts
        // KEPT: onMapClick only - users can still view posts on map
        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // REMOVED: No action on post click in general feed
                // This prevents users from clicking on posts in the general feed
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

    private fun listenForPosts() {
        showLoading(true)

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    swipeRefresh.isRefreshing = false
                    updateUI(emptyList())
                    return@addSnapshotListener
                }

                val posts = mutableListOf<Pair<String, Post>>()

                for (doc in snapshots!!) {
                    try {
                        val post = doc.toObject(Post::class.java)
                        posts.add(Pair(doc.id, post))
                    } catch (ex: Exception) {
                        Log.e("Firestore", "Error parsing post: ${ex.message}")
                        continue
                    }
                }

                updateUI(posts)
                showLoading(false)
                swipeRefresh.isRefreshing = false
            }
    }

    private fun updateUI(posts: List<Pair<String, Post>>) {
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