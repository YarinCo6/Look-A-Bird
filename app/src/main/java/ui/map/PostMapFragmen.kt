package com.example.look_a_bird.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.look_a_bird.MainActivity
import com.example.look_a_bird.R
import com.example.look_a_bird.database.Repository
import com.example.look_a_bird.model.Post
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var repository: Repository
    private lateinit var fabAddPost: FloatingActionButton

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = (requireActivity() as MainActivity).getRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize map
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize FAB for adding posts
        fabAddPost = view.findViewById(R.id.fab_add_post)
        fabAddPost.setOnClickListener { openAddPost() }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()
        loadAllPosts()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            centerMapOnLastLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun centerMapOnLastLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(requireActivity())

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 14f)
                    )
                }
            }
    }

    private fun loadAllPosts() {
        lifecycleScope.launch {
            try {
                // Using Repository instead of direct Firebase access
                repository.getAllPosts().observe(viewLifecycleOwner) { posts ->
                    // Clear existing markers
                    googleMap.clear()
                    
                    // Add markers for each post with location
                    for (post in posts) {
                        if (post.latitude != 0.0 && post.longitude != 0.0) {
                            addPostMarker(post)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addPostMarker(post: Post) {
        val location = LatLng(post.latitude, post.longitude)
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(location)
                .title(post.birdSpecies)
                .snippet("by ${post.userName}")
        )
        marker?.tag = post

        // Set marker click listener
        googleMap.setOnMarkerClickListener { clickedMarker ->
            val postData = clickedMarker.tag as? Post
            postData?.let {
                showPostPopup(it)
            }
            true
        }
    }

    private fun showPostPopup(post: Post) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_post_popup, null)

        val imageView = dialogView.findViewById<ImageView>(R.id.image_post)
        val birdText = dialogView.findViewById<TextView>(R.id.text_bird_species)
        val scientificText = dialogView.findViewById<TextView>(R.id.text_scientific_name)
        val descriptionText = dialogView.findViewById<TextView>(R.id.text_description)
        val userNameText = dialogView.findViewById<TextView>(R.id.text_user_name)
        val dateText = dialogView.findViewById<TextView>(R.id.text_date)

        // Populate data
        birdText.text = post.birdSpecies
        scientificText.text = post.scientificName.ifEmpty { "Scientific name not available" }
        descriptionText.text = post.description
        userNameText.text = "Posted by: ${post.userName}"
        
        // Format date
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
        dateText.text = dateFormat.format(java.util.Date(post.timestamp))

        // Load image using Coil instead of Glide
        imageView.load(post.imageUrl) {
            placeholder(android.R.drawable.ic_menu_gallery)
            error(android.R.drawable.ic_menu_report_image)
            crossfade(true)
        }

        // Create dialog
        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Bird Sighting")
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .setNeutralButton("View Profile") { _, _ ->
                // Navigate to user profile if needed
                navigateToUserProfile(post.userId)
            }
            .create()

        // Show edit button only if it's current user's post
        val currentUserId = repository.getCurrentUserId()
        if (post.userId == currentUserId && currentUserId.isNotEmpty()) {
            dialog.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, "Edit") { _, _ ->
                navigateToEditPost(post.id)
            }
        }

        dialog.show()
    }

    private fun navigateToUserProfile(userId: String) {
        try {
            val action = MapFragmentDirections.actionMapFragmentToUserProfileFragment(userId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToEditPost(postId: String) {
        try {
            val action = MapFragmentDirections.actionMapFragmentToEditPostFragment(postId)
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAddPost() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    try {
                        val action = MapFragmentDirections.actionMapFragmentToAddPostFragment(
                            latitude = location.latitude.toFloat(),
                            longitude = location.longitude.toFloat()
                        )
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh posts when returning to map
        if (::googleMap.isInitialized) {
            loadAllPosts()
        }
    }
}
