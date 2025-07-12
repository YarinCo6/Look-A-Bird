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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.look_a_bird.R
import com.example.look_a_bird.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var imageProfilePicture: ImageView
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    private lateinit var textMemberSince: TextView
    private lateinit var buttonEditProfile: Button
    private lateinit var buttonMyPosts: Button
    private lateinit var buttonLogout: Button
    private lateinit var progressBar: ProgressBar

    private var currentUser: User? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var profileListener: ListenerRegistration? = null

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

    override fun onStart() {
        super.onStart()
        startRealtimeUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopRealtimeUpdates()
    }

    private fun setupViews(view: View) {
        imageProfilePicture = view.findViewById(R.id.image_profile_picture)
        textUserName = view.findViewById(R.id.text_user_name)
        textUserEmail = view.findViewById(R.id.text_user_email)
        textMemberSince = view.findViewById(R.id.text_member_since)
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

        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            showLoading(false)
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        loadUserFromFirestore(firebaseUser.uid)
    }

    private fun loadUserFromFirestore(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        currentUser = document.toObject(User::class.java)
                        currentUser?.id = document.id
                    } catch (e: Exception) {
                        createUserFromFirebaseAuth()
                    }
                } else {
                    createUserFromFirebaseAuth()
                }

                showLoading(false)
                populateUserData()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                createUserFromFirebaseAuth()
                showLoading(false)
                populateUserData()
            }
    }

    private fun createUserFromFirebaseAuth() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            currentUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Anonymous",
                email = firebaseUser.email ?: "No email",
                profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                memberSince = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            )
        }
    }

    private fun startRealtimeUpdates() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) return

        profileListener = db.collection("users").document(firebaseUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val updatedUser = snapshot.toObject(User::class.java)
                        updatedUser?.id = snapshot.id

                        if (updatedUser != null && updatedUser != currentUser) {
                            currentUser = updatedUser
                            populateUserData()
                        }
                    } catch (e: Exception) {
                        // Ignore parsing errors
                    }
                }
            }
    }

    private fun stopRealtimeUpdates() {
        profileListener?.remove()
        profileListener = null
    }

    private fun populateUserData() {
        currentUser?.let { user ->
            textUserName.text = user.name
            textUserEmail.text = user.email
            textMemberSince.text = "Member since ${formatMemberSince(user.memberSince)}"
            loadProfileImage(user.profileImageUrl)
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        if (imageUrl.isNotEmpty() && imageUrl != "null") {
            try {
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .circleCrop()
                    .into(imageProfilePicture)
            } catch (e: Exception) {
                imageProfilePicture.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            imageProfilePicture.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    private fun formatMemberSince(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
    }

    private fun navigateToEditProfile() {
        try {
            val action = ProfileFragmentDirections.actionProfileToEditProfile()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigate to Edit Profile", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMyPosts() {
        try {
            val action = ProfileFragmentDirections.actionProfileToUserPosts()
            findNavController().navigate(action)
        } catch (e: Exception) {
            Toast.makeText(context, "Navigation error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun performLogout() {
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
        stopRealtimeUpdates()

        FirebaseAuth.getInstance().signOut()

        showLoading(false)
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()

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
