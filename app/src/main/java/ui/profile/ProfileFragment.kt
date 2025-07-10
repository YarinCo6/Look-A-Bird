package com.example.look_a_bird.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.look_a_bird.R
import com.example.look_a_bird.model.User
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.fragment.findNavController

class ProfileFragment : Fragment() {

    private lateinit var imageProfilePicture: ImageView
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textMemberSince: TextView
    private lateinit var textPostsCount: TextView
    private lateinit var textSpeciesCount: TextView
    private lateinit var textLocationsCount: TextView
    private lateinit var buttonEditProfile: Button
    private lateinit var buttonMyPosts: Button
    private lateinit var buttonLogout: Button
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupClickListeners()
        loadUserProfile()
    }

    private fun setupViews(view: View) {
        imageProfilePicture = view.findViewById(R.id.image_profile_picture)
        textUserName = view.findViewById(R.id.text_user_name)
        textUserEmail = view.findViewById(R.id.text_user_email)
        textMemberSince = view.findViewById(R.id.text_member_since)
        textPostsCount = view.findViewById(R.id.text_posts_count)
        textSpeciesCount = view.findViewById(R.id.text_species_count)
        textLocationsCount = view.findViewById(R.id.text_locations_count)
        buttonEditProfile = view.findViewById(R.id.button_edit_profile)
        buttonMyPosts = view.findViewById(R.id.button_my_posts)
        buttonLogout = view.findViewById(R.id.button_logout)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        buttonEditProfile.setOnClickListener {
            navigateToEditProfile()
        }

        buttonMyPosts.setOnClickListener {
            navigateToMyPosts()
        }

        buttonLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            showLoading(false)
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // For now, we'll just display what Firebase provides
        currentUser = User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "Anonymous",
            email = firebaseUser.email ?: "No email",
            profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
            memberSince = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            postsCount = 0,
            speciesCount = 0,
            locationsCount = 0
        )

        showLoading(false)
        populateUserData()
    }


    private fun populateUserData() {
        currentUser?.let { user ->
            textUserName.text = user.name
            textUserEmail.text = user.email
            textMemberSince.text = "Member since ${formatMemberSince(user.memberSince)}"
            textPostsCount.text = user.postsCount.toString()
            textSpeciesCount.text = user.speciesCount.toString()
            textLocationsCount.text = user.locationsCount.toString()

            // Load profile image if available
            if (user.profileImageUrl.isNotEmpty()) {
                // Here we will load image with Picasso
                // For now, keep placeholder
            }
        }
    }

    private fun formatMemberSince(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
    }

    private fun navigateToEditProfile() {
        // Here we will add navigation to edit profile screen
        Toast.makeText(context, "Navigate to Edit Profile", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMyPosts() {
        // Here we will add navigation to my posts screen
        Toast.makeText(context, "Navigate to My Posts", Toast.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        // Show confirmation dialog
        android.app.AlertDialog.Builder(context)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                confirmLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmLogout() {
        showLoading(true)

        FirebaseAuth.getInstance().signOut()

        showLoading(false)
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()

        // Navigate to login screen
        val action = ProfileFragmentDirections.actionGlobalLogout()
        findNavController().navigate(action)
    }


    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonEditProfile.isEnabled = !show
        buttonMyPosts.isEnabled = !show
        buttonLogout.isEnabled = !show
    }
}