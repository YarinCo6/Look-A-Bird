package com.example.look_a_bird.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.look_a_bird.R
import com.example.look_a_bird.ui.adapter.PostAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAddPost: FloatingActionButton
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_posts)
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        fabAddPost = view.findViewById(R.id.fab_add_post)
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter()

        postAdapter.setOnItemClickListener(object : PostAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                val post = postAdapter.getPost(position)
                // כאן נוסיף פעולה כשלוחצים על פוסט
            }

            override fun onMapClick(latitude: Double, longitude: Double) {
                // For example, just show a toast:
                Toast.makeText(context, "Lat: $latitude, Lon: $longitude", Toast.LENGTH_SHORT).show()
            }

        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = postAdapter
    }


    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            // כאן נוסיף רענון נתונים
            swipeRefresh.isRefreshing = false
        }
    }

    private fun setupFab() {
        fabAddPost.setOnClickListener {
            // כאן נוסיף מעבר למסך הוספת פוסט
        }
    }
}