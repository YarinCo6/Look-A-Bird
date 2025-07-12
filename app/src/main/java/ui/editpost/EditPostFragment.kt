package com.example.look_a_bird.ui.editpost

import Post
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.look_a_bird.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditPostFragment : Fragment() {

    private lateinit var imagePostPreview: ImageView
    private lateinit var buttonChangeImage: Button
    private lateinit var editTextBirdName: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var buttonSaveChanges: Button
    private lateinit var buttonCancel: Button
    private lateinit var buttonDeletePost: Button
    private lateinit var progressBar: ProgressBar

    private var currentPost: Post? = null
    private var postId: String = ""
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        editTextDescription = view.findViewById(R.id.edit_text_description)
        buttonSaveChanges = view.findViewById(R.id.button_save_changes)
        buttonCancel = view.findViewById(R.id.button_cancel)
        buttonDeletePost = view.findViewById(R.id.button_delete_post)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        buttonChangeImage.setOnClickListener {
            pickImage()
        }

        buttonSaveChanges.setOnClickListener {
            saveChanges()
        }

        buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        buttonDeletePost.setOnClickListener {
            confirmDelete()
        }
    }

    private fun loadPostData() {
        if (postId.isEmpty()) {
            Toast.makeText(context, "Error: No post ID provided", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        db.collection("posts").document(postId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    currentPost = doc.toObject(Post::class.java)
                    populateFields()
                }
                showLoading(false)
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Error loading post.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun populateFields() {
        currentPost?.let { post ->
            editTextBirdName.setText(post.birdSpecies)
            editTextDescription.setText(post.description)

            if (post.imageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(imagePostPreview)
            }
        }
    }

    private fun pickImage() {
        val intent = android.content.Intent(android.content.Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == android.app.Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                isImageChanged = true
                Glide.with(this)
                    .load(uri)
                    .centerCrop()
                    .into(imagePostPreview)
            }
        }
    }

    private fun saveChanges() {
        val birdName = editTextBirdName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()

        if (birdName.isEmpty() || description.isEmpty()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // For demo: auto-fill scientific name if desired
        val resolvedScientificName = if (birdName.equals("Emu", true)) {
            "Dromaius novaehollandiae"
        } else {
            ""
        }

        if (isImageChanged && selectedImageUri != null) {
            val imageRef = storage.reference
                .child("post_images/${postId}_${System.currentTimeMillis()}.jpg")

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        updateFirestore(birdName, description, resolvedScientificName, downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
                    Toast.makeText(context, "Image upload failed.", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateFirestore(
                birdName,
                description,
                resolvedScientificName,
                currentPost?.imageUrl ?: ""
            )
        }
    }

    private fun updateFirestore(
        birdName: String,
        description: String,
        scientificName: String,
        imageUrl: String
    ) {
        val updates = mapOf(
            "birdSpecies" to birdName,
            "scientificName" to scientificName,
            "description" to description,
            "imageUrl" to imageUrl
        )

        db.collection("posts").document(postId)
            .update(updates)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Post updated.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Error updating post.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete() {
        android.app.AlertDialog.Builder(context)
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ -> deletePost() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost() {
        showLoading(true)
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Post deleted.", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(context, "Error deleting post.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonSaveChanges.isEnabled = !show
        buttonDeletePost.isEnabled = !show
        buttonChangeImage.isEnabled = !show
        buttonCancel.isEnabled = !show
    }
}
