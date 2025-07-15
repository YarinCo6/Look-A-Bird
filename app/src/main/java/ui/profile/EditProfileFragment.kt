package com.example.look_a_bird.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.example.look_a_bird.R
import com.example.look_a_bird.model.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var imageProfile: ImageView
    private lateinit var buttonChangePicture: Button
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextCurrentPassword: TextInputEditText
    private lateinit var editTextNewPassword: TextInputEditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button
    private lateinit var progressBar: ProgressBar

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var currentUser: User? = null
    private var selectedImageUri: Uri? = null
    private var isImageChanged = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                isImageChanged = true
                imageProfile.load(uri) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        loadUserData()
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        imageProfile = view.findViewById(R.id.image_profile_edit)
        buttonChangePicture = view.findViewById(R.id.button_change_picture)
        editTextName = view.findViewById(R.id.edit_text_name)
        editTextEmail = view.findViewById(R.id.edit_text_email)
        editTextCurrentPassword = view.findViewById(R.id.edit_text_current_password)
        editTextNewPassword = view.findViewById(R.id.edit_text_new_password)
        buttonCancel = view.findViewById(R.id.button_cancel)
        buttonSave = view.findViewById(R.id.button_save)
        progressBar = view.findViewById(R.id.progress_bar_edit)
    }

    private fun loadUserData() {
        showLoading(true)

        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            showLoading(false)
            Toast.makeText(context, "No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { document ->
                showLoading(false)

                if (document.exists()) {
                    try {
                        currentUser = document.toObject(User::class.java)
                        currentUser?.id = document.id
                        populateFields()
                    } catch (e: Exception) {
                        loadFromFirebaseAuth()
                    }
                } else {
                    loadFromFirebaseAuth()
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(context, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                loadFromFirebaseAuth()
            }
    }

    private fun loadFromFirebaseAuth() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            currentUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: "",
                profileImageUrl = firebaseUser.photoUrl?.toString() ?: "",
                memberSince = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis(),
            )
            populateFields()
        }
    }

    private fun populateFields() {
        currentUser?.let { user ->
            editTextName.setText(user.name)
            editTextEmail.setText(user.email)

            if (user.profileImageUrl.isNotEmpty()) {
                imageProfile.load(user.profileImageUrl) {
                    placeholder(android.R.drawable.ic_menu_gallery)
                    error(android.R.drawable.ic_menu_gallery)
                    transformations(CircleCropTransformation())
                }
            }
        }
    }

    private fun setupClickListeners() {
        buttonChangePicture.setOnClickListener {
            changePicture()
        }

        buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun changePicture() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun saveChanges() {
        val newName = editTextName.text.toString().trim()
        val newEmail = editTextEmail.text.toString().trim()
        val currentPassword = editTextCurrentPassword.text.toString().trim()
        val newPassword = editTextNewPassword.text.toString().trim()

        if (!validateInputs(newName, newEmail, currentPassword, newPassword)) {
            return
        }

        showLoading(true)

        if (newPassword.isNotEmpty()) {
            verifyAndUpdatePassword(newName, newEmail, currentPassword, newPassword)
        } else {
            updateProfile(newName, newEmail)
        }
    }

    private fun validateInputs(name: String, email: String, currentPassword: String, newPassword: String): Boolean {
        if (name.isEmpty()) {
            editTextName.error = "Name cannot be empty"
            editTextName.requestFocus()
            return false
        }

        if (name.length < 2 || name.length > 50) {
            editTextName.error = "Name must be between 2-50 characters"
            editTextName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            editTextEmail.error = "Email cannot be empty"
            editTextEmail.requestFocus()
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Please enter a valid email"
            editTextEmail.requestFocus()
            return false
        }

        if (newPassword.isNotEmpty()) {
            if (currentPassword.isEmpty()) {
                editTextCurrentPassword.error = "Current password required to change password"
                editTextCurrentPassword.requestFocus()
                return false
            }

            if (newPassword.length < 6) {
                editTextNewPassword.error = "New password must be at least 6 characters"
                editTextNewPassword.requestFocus()
                return false
            }
        }

        return true
    }

    private fun verifyAndUpdatePassword(name: String, email: String, currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            showLoading(false)
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        updateProfile(name, email)
                    }
                    .addOnFailureListener { exception ->
                        showLoading(false)
                        Toast.makeText(context, "Error updating password: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                showLoading(false)
                editTextCurrentPassword.error = "Current password is incorrect"
                editTextCurrentPassword.requestFocus()
            }
    }

    private fun updateProfile(name: String, email: String) {
        val user = auth.currentUser
        if (user == null) {
            showLoading(false)
            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (email != user.email) {
            user.updateEmail(email)
                .addOnSuccessListener {
                    updateFirestoreAndStorage(name, email)
                }
                .addOnFailureListener { exception ->
                    showLoading(false)
                    Toast.makeText(context, "Error updating email: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            updateFirestoreAndStorage(name, email)
        }
    }

    private fun updateFirestoreAndStorage(name: String, email: String) {
        if (isImageChanged && selectedImageUri != null) {
            uploadImageAndUpdateProfile(name, email)
        } else {
            updateFirestoreProfile(name, email, currentUser?.profileImageUrl ?: "")
        }
    }

    private fun uploadImageAndUpdateProfile(name: String, email: String) {
        val user = auth.currentUser
        if (user == null || selectedImageUri == null) {
            updateFirestoreProfile(name, email, currentUser?.profileImageUrl ?: "")
            return
        }

        val imageRef = storage.reference
            .child("profile_images")
            .child("${user.uid}_${UUID.randomUUID()}.jpg")

        imageRef.putFile(selectedImageUri!!)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateFirestoreProfile(name, email, downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Image upload failed, profile updated without image", Toast.LENGTH_SHORT).show()
                updateFirestoreProfile(name, email, currentUser?.profileImageUrl ?: "")
            }
    }

    private fun updateFirestoreProfile(name: String, email: String, imageUrl: String) {
        val user = auth.currentUser
        if (user == null) {
            showLoading(false)
            return
        }

        val updatedUser = currentUser?.copy(
            name = name,
            email = email,
            profileImageUrl = imageUrl
        ) ?: User(
            id = user.uid,
            name = name,
            email = email,
            profileImageUrl = imageUrl,
            memberSince = user.metadata?.creationTimestamp ?: System.currentTimeMillis(),
        )

        db.collection("users").document(user.uid)
            .set(updatedUser)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Toast.makeText(context, "Error updating profile: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonSave.isEnabled = !show
        buttonCancel.isEnabled = !show
        buttonChangePicture.isEnabled = !show
    }
}
