package com.example.look_a_bird

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)


        setupNavigation()
        setupBottomNavigation()
    }

    private fun setupNavigation() {
        // Get the NavHostFragment from the layout
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        // Get the NavController from the NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setupBottomNavigation() {
        // Find the bottom navigation view
        bottomNavigation = findViewById(R.id.bottom_navigation)

        // Connect bottom navigation with NavController
        NavigationUI.setupWithNavController(bottomNavigation, navController)

        // Hide bottom navigation on login/register screens
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> {
                    bottomNavigation.visibility = android.view.View.GONE
                }
                else -> {
                    bottomNavigation.visibility = android.view.View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle the Up button in the action bar
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}