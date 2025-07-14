package com.example.look_a_bird.ui.map

import Post
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.look_a_bird.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            enableMyLocation()
        } else {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
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
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14f))
                }
            }
    }

    private fun loadAllPosts() {
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    try {
                        val post = doc.toObject(Post::class.java)
                        val location = LatLng(post.latitude, post.longitude)
                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(location)
                                .title(post.birdSpecies)
                        )
                        marker?.tag = post
                    } catch (e: Exception) {
                        e.printStackTrace()
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
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading posts: ${e.message}", Toast.LENGTH_SHORT).show()
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

        Glide.with(this)
            .load(post.imageUrl)
            .placeholder(android.R.drawable.ic_menu_report_image) // default placeholder
            .error(android.R.drawable.stat_notify_error)          // default error icon
            .into(imageView)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(post.userName)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
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
                    val action = MapFragmentDirections.actionMapFragmentToAddPostFragment(
                        latitude = location.latitude.toFloat(),
                        longitude = location.longitude.toFloat()
                    )
                    findNavController().navigate(action)
                } else {
                    Toast.makeText(context, "Could not get current location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }
}
