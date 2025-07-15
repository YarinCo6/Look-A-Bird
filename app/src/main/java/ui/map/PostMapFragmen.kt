package com.example.look_a_bird.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.look_a_bird.MainActivity
import com.example.look_a_bird.R
import com.example.look_a_bird.database.Repository
import com.example.look_a_bird.model.Post
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PostMapFragment : Fragment(), OnMapReadyCallback {

    private var latitude: Float = 0f
    private var longitude: Float = 0f
    private lateinit var fabBack: FloatingActionButton
    private lateinit var repository: Repository

    // Using Safe Args if available, fallback to arguments
    private val args: PostMapFragmentArgs? by lazy {
        try {
            PostMapFragmentArgs.fromBundle(requireArguments())
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (requireActivity() as MainActivity).getRepository()
        
        // Get coordinates from Safe Args or regular arguments
        args?.let {
            latitude = it.latitude
            longitude = it.longitude
        } ?: run {
            arguments?.let {
                latitude = it.getFloat("latitude", 0f)
                longitude = it.getFloat("longitude", 0f)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupBackButton(view)
    }

    private fun setupBackButton(view: View) {
        fabBack = view.findViewById(R.id.fab_back)
        fabBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        lifecycleScope.launch {
            try {
                // Using Repository instead of direct Firebase
                repository.getAllPosts().observe(viewLifecycleOwner) { posts ->
                    val postList = posts.filter { it.latitude != 0.0 && it.longitude != 0.0 }

                    if (postList.isEmpty()) {
                        // If no posts with location, just show the passed coordinates
                        if (latitude != 0f && longitude != 0f) {
                            val location = LatLng(latitude.toDouble(), longitude.toDouble())
                            googleMap.addMarker(
                                MarkerOptions().position(location).title("Bird Sighting Location")
                            )
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                        }
                        return@observe
                    }

                    // Add markers for all posts
                    for (post in postList) {
                        val location = LatLng(post.latitude, post.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(location).title(post.birdSpecies)
                        )
                        marker?.tag = post
                    }

                    // Set marker click listener for popup
                    googleMap.setOnMarkerClickListener { marker ->
                        val post = marker.tag as? Post
                        post?.let {
                            showPostPopup(it)
                        }
                        true
                    }

                    // Move camera to specific location if coordinates were passed
                    if (latitude != 0f && longitude != 0f) {
                        val targetLocation = LatLng(latitude.toDouble(), longitude.toDouble())
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 15f))
                    } else {
                        // Otherwise focus on first post
                        val firstLocation = LatLng(postList[0].latitude, postList[0].longitude)
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: just show the coordinates if repository fails
                if (latitude != 0f && longitude != 0f) {
                    val location = LatLng(latitude.toDouble(), longitude.toDouble())
                    googleMap.addMarker(
                        MarkerOptions().position(location).title("Bird Sighting Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
                }
            }
        }
    }

    private fun showPostPopup(post: Post) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_post_popup, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.image_post)
        val birdText = dialogView.findViewById<TextView>(R.id.text_bird_species)
        val scientificText = dialogView.findViewById<TextView>(R.id.text_scientific_name)
        val descriptionText = dialogView.findViewById<TextView>(R.id.text_description)

        birdText.text = post.birdSpecies
        scientificText.text = post.scientificName
        descriptionText.text = post.description

        // Using Coil instead of Glide
        imageView.load(post.imageUrl.takeIf { it.isNotEmpty() }) {
            placeholder(android.R.drawable.ic_menu_report_image)
            error(android.R.drawable.stat_notify_error)
            crossfade(true)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(post.userName)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
}
