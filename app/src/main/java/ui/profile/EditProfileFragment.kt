package com.example.look_a_bird.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.look_a_bird.R
import com.example.look_a_bird.model.User
import com.google.android.material.textfield.TextInputEditText

class EditProfileFragment : Fragment() {

    private lateinit var imageProfile: ImageView
    private lateinit var buttonChangePicture: Button
    private lateinit var editTextName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var buttonCancel: Button
    private lateinit var buttonSave: Button

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
        buttonCancel = view.findViewById(R.id.button_cancel)
        buttonSave = view.findViewById(R.id.button_save)
    }

    private fun loadUserData() {
        // Here we will load current user data
        // For now, using sample data without parameters that don't exist
        editTextName.setText("Current User")
        editTextEmail.setText("user@example.com")

        // Load profile image - here we will add image loading logic
        // For now using placeholder
    }

    private fun setupClickListeners() {
        buttonChangePicture.setOnClickListener {
            changePicture()
        }

        buttonCancel.setOnClickListener {
            // Navigate back without saving
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        buttonSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun changePicture() {
        // Here we will add image picker functionality
        Toast.makeText(context, "Image picker will be implemented", Toast.LENGTH_SHORT).show()
    }

    private fun saveChanges() {
        val newName = editTextName.text.toString().trim()

        if (newName.isEmpty()) {
            editTextName.error = "Name cannot be empty"
            return
        }

        if (!isValidName(newName)) {
            editTextName.error = "Name must be between 2-50 characters"
            return
        }

        // Here we will save to Firebase and local database
        Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()

        // Navigate back
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    private fun isValidName(name: String): Boolean {
        return name.length >= 2 && name.length <= 50
    }
}