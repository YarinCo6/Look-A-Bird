package com.example.look_a_bird.ui.editpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.look_a_bird.R
import com.example.look_a_bird.model.Post
import com.google.android.material.textfield.TextInputEditText

class EditPostFragment : Fragment() {

    private lateinit var imagePostPreview: ImageView
    private lateinit var buttonChangeImage: Button
    private lateinit var editTextBirdName: TextInputEditText
    private lateinit var editTextScientificName: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var editTextLocation: TextInputEditText
    private lateinit var buttonGetLocation: Button
    private lateinit var buttonDeletePost: Button
    private lateinit var buttonSaveChanges: Button
    private lateinit var progressBar: ProgressBar

    private var currentPost: Post? = null
    private var postId: String = ""
    private var selectedImageUri: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get post ID from arguments (will be set via Safe Args later)
        arguments?.let {
            postId = it.getString("postId", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupClickListeners()
        loadPostData()
    }

    private fun setupViews(view: View) {
        imagePostPreview = view.findViewById(R.id.image_post_preview)
        buttonChangeImage = view.findViewById(R.id.button_change_image)
        editTextBirdName = view.findViewById(R.id.edit_text_bird_name)
        editTextScientificName = view.findViewById(R.id.edit_text_scientific_name)
        editTextDescription = view.findViewById(R.id.edit_text_description)
        editTextLocation = view.findViewById(R.id.edit_text_location)
        buttonGetLocation = view.findViewById(R.id.button_get_location)
        buttonDeletePost = view.findViewById(R.id.button_delete_post)
        buttonSaveChanges = view.findViewById(R.id.button_save_changes)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        buttonChangeImage.setOnClickListener {
            changeImage()
        }

        buttonGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        buttonSaveChanges.setOnClickListener {
            saveChanges()
        }

        buttonDeletePost.setOnClickListener {
            deletePost()
        }
    }

    private fun loadPostData() {
        if (postId.isEmpty()) {
            Toast.makeText(context, "Error: No post ID provided", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Here we will load post data from Firebase
        // For now, simulate loading
        simulateLoadPost()
    }

    private fun simulateLoadPost() {
        // Simulate loading post data - will replace with Firebase
        view?.postDelayed({
            showLoading(false)

            // Create sample post for testing - GPS coordinates will use default Float values
            currentPost = Post(
                id = postId,
                userId = "current_user_id",
                userName = "Current User",
                birdSpecies = "American Robin",
                scientificName = "Turdus migratorius",
                description = "Beautiful bird spotted in the park",
                location = "Central Park, New York",
                imageUrl = "",
                latitude = 40.785091f, // Float GPS coordinate
                longitude = -73.968285f, // Float GPS coordinate
                timestamp = System.currentTimeMillis()
            )

            populateFields()
        }, 1000)
    }

    private fun populateFields() {
        currentPost?.let { post ->
            editTextBirdName.setText(post.birdSpecies)
            editTextScientificName.setText(post.scientificName)
            editTextDescription.setText(post.description)
            editTextLocation.setText(post.location)
            selectedImageUri = post.imageUrl

            // Load image if available
            if (post.imageUrl.isNotEmpty()) {
                // Here we will load image with Picasso
                // For now, keep placeholder
            }
        }
    }

    private fun changeImage() {
        // Here we will add image selection functionality
        Toast.makeText(context, "Image change - will implement later", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentLocation() {
        // Here we will add GPS location functionality
        Toast.makeText(context, "GPS location - will implement later", Toast.LENGTH_SHORT).show()
        editTextLocation.setText("Updated Location")
    }

    private fun saveChanges() {
        val birdName = editTextBirdName.text.toString().trim()
        val scientificName = editTextScientificName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val location = editTextLocation.text.toString().trim()

        if (!validateInput(birdName, description, location)) {
            return
        }

        showLoading(true)

        // Update current post
        currentPost?.let { post ->
            val updatedPost = post.copy(
                birdSpecies = birdName,
                scientificName = scientificName,
                description = description,
                location = location,
                imageUrl = selectedImageUri
            )

            // Here we will save changes to Firebase
            simulateSaveChanges(updatedPost)
        }
    }

    private fun deletePost() {
        // Show confirmation dialog
        android.app.AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                performDelete()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performDelete() {
        showLoading(true)

        // Here we will delete from Firebase
        simulateDeletePost()
    }

    private fun validateInput(birdName: String, description: String, location: String): Boolean {
        if (birdName.isEmpty()) {
            editTextBirdName.error = "Bird name is required"
            return false
        }

        // Validate bird name contains only letters and spaces
        if (!birdName.matches(Regex("^[a-zA-Z\\s]+$"))) {
            editTextBirdName.error = "Bird name can only contain letters and spaces"
            return false
        }

        if (description.isEmpty()) {
            editTextDescription.error = "Description is required"
            return false
        }

        if (location.isEmpty()) {
            editTextLocation.error = "Location is required"
            return false
        }

        return true
    }

    private fun simulateSaveChanges(post: Post) {
        // Simulate saving changes - will replace with Firebase
        view?.postDelayed({
            showLoading(false)
            Toast.makeText(context, "Changes saved successfully!", Toast.LENGTH_SHORT).show()

            // Navigate back
            // Here we will add navigation
        }, 2000)
    }

    private fun simulateDeletePost() {
        // Simulate deletion - will replace with Firebase
        view?.postDelayed({
            showLoading(false)
            Toast.makeText(context, "Post deleted successfully!", Toast.LENGTH_SHORT).show()

            // Navigate back
            // Here we will add navigation
        }, 2000)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonSaveChanges.isEnabled = !show
        buttonDeletePost.isEnabled = !show
        buttonChangeImage.isEnabled = !show
        buttonGetLocation.isEnabled = !show
    }
}