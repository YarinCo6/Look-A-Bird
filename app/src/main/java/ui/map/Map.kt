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
import kotlinx.coroutines.launch

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var repository: Repository

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
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
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
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 14f)
                    )
                }
            }
    }

    private fun loadAllPosts() {
        lifecycleScope.launch {
            try {
                // Using Repository instead of direct Firebase
                repository.getAllPosts().observe(viewLifecycleOwner) { posts ->
                    for (post in posts) {
                        if (post.latitude != 0.0 && post.longitude != 0.0) {
                            val location = LatLng(post.latitude, post.longitude)
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(location)
                                    .title(post.birdSpecies)
                            )
                            marker?.tag = post
                        }
                    }

                    googleMap.setOnMarkerClickListener { marker ->
                        val post = marker.tag as? Post
                        post?.let {
                            showPostPopup(it)
                        }
                        true
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
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
        imageView.load(post.imageUrl) {
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
