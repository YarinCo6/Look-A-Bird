package com.example.look_a_bird.ui.map

import Post
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.look_a_bird.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class PostMapFragment : Fragment(), OnMapReadyCallback {

    private var latitude: Float = 0f
    private var longitude: Float = 0f
    private lateinit var fabBack: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            latitude = it.getFloat("latitude")
            longitude = it.getFloat("longitude")
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
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val postList = mutableListOf<Post>()

                for (doc in documents) {
                    try {
                        val post = doc.toObject(Post::class.java)
                        postList.add(post)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                if (postList.isEmpty()) return@addOnSuccessListener

                for (post in postList) {
                    val location = LatLng(post.latitude, post.longitude)
                    val marker = googleMap.addMarker(
                        MarkerOptions().position(location).title(post.birdSpecies)
                    )
                    marker?.tag = post
                }

                googleMap.setOnMarkerClickListener { marker ->
                    val post = marker.tag as? Post
                    post?.let {
                        showPostPopup(it)
                    }
                    true
                }

                // Move camera to specific post location if passed
                if (latitude != 0f && longitude != 0f) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude.toDouble(), longitude.toDouble()), 15f))
                } else {
                    val firstLocation = LatLng(postList[0].latitude, postList[0].longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
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
            .load(post.imageUrl.takeIf { !it.isNullOrBlank() })
            .placeholder(android.R.drawable.ic_menu_report_image)
            .error(android.R.drawable.stat_notify_error)
            .into(imageView)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(post.userName)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

}
