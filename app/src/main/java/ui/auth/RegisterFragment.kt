package com.example.look_a_bird.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.look_a_bird.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest


class RegisterFragment : Fragment() {

    private lateinit var editTextFullName: TextInputEditText
    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var editTextConfirmPassword: TextInputEditText
    private lateinit var buttonRegister: MaterialButton
    private lateinit var textLoginLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        editTextFullName = view.findViewById(R.id.edit_text_full_name)
        editTextEmail = view.findViewById(R.id.edit_text_email)
        editTextPassword = view.findViewById(R.id.edit_text_password)
        editTextConfirmPassword = view.findViewById(R.id.edit_text_confirm_password)
        buttonRegister = view.findViewById(R.id.button_register)
        textLoginLink = view.findViewById(R.id.text_login_link)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        buttonRegister.setOnClickListener {
            performRegister()
        }

        textLoginLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun performRegister() {
        val fullName = editTextFullName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()
        val confirmPassword = editTextConfirmPassword.text.toString()

        if (!validateInput(fullName, email, password, confirmPassword)) return

        showLoading(true)

        FirebaseAuth.getInstance()
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                    val user = FirebaseAuth.getInstance().currentUser
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                        showLoading(false)
                        if (updateTask.isSuccessful) {
                            Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                            navigateToHome()
                        } else {
                            Toast.makeText(
                                context,
                                "Error updating profile: ${updateTask.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(
                        context,
                        "Error: ${task.exception?.message ?: "Registration failed"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

    }


    private fun validateInput(fullName: String, email: String, password: String, confirmPassword: String): Boolean {
        if (fullName.isEmpty()) {
            editTextFullName.error = "Full name is required"
            return false
        }

        if (fullName.length < 2) {
            editTextFullName.error = "Full name must be at least 2 characters"
            return false
        }

        if (email.isEmpty()) {
            editTextEmail.error = "Email is required"
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Please enter a valid email"
            return false
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            editTextPassword.error = "Password must be at least 6 characters"
            return false
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.error = "Please confirm your password"
            return false
        }

        if (password != confirmPassword) {
            editTextConfirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun simulateRegister(fullName: String, email: String, password: String) {
        // Simulate registration - will replace with Firebase
        view?.postDelayed({
            showLoading(false)

            // This is just for testing - will replace with real registration
            Toast.makeText(context, "Registration successful! Welcome $fullName", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }, 2000)
    }

    private fun navigateToLogin() {
        val action = RegisterFragmentDirections.actionRegisterToLogin()
        findNavController().navigate(action)
    }



    private fun navigateToHome() {
        val action = RegisterFragmentDirections.actionRegisterToHome()
        findNavController().navigate(action)
    }


    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonRegister.isEnabled = !show
    }
}