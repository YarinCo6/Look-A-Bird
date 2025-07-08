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

class LoginFragment : Fragment() {

    private lateinit var editTextEmail: TextInputEditText
    private lateinit var editTextPassword: TextInputEditText
    private lateinit var buttonLogin: MaterialButton
    private lateinit var textRegisterLink: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupClickListeners()
    }

    private fun setupViews(view: View) {
        editTextEmail = view.findViewById(R.id.edit_text_email)
        editTextPassword = view.findViewById(R.id.edit_text_password)
        buttonLogin = view.findViewById(R.id.button_login)
        textRegisterLink = view.findViewById(R.id.text_register_link)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        buttonLogin.setOnClickListener {
            performLogin()
        }

        textRegisterLink.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        if (!validateInput(email, password)) {
            return
        }

        showLoading(true)

        // Here we will add Firebase Authentication
        // For now, simulate login
        simulateLogin(email, password)
    }

    private fun validateInput(email: String, password: String): Boolean {
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

        return true
    }

    private fun simulateLogin(email: String, password: String) {
        // Simulate login - will replace with Firebase
        view?.postDelayed({
            showLoading(false)

            // This is just for testing - will replace with real authentication
            if (email == "test@test.com" && password == "123456") {
                navigateToHome()
            } else {
                Toast.makeText(context, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }, 2000)
    }

    private fun navigateToRegister() {
        // Here we will add navigation to register screen
        Toast.makeText(context, "Navigate to Register", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        // Here we will add navigation to home screen
        Toast.makeText(context, "Login successful! Navigate to Home", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonLogin.isEnabled = !show
    }
}