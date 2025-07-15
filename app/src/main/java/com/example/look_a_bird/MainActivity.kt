package com.example.look_a_bird

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.look_a_bird.database.Repository
import com.example.look_a_bird.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Repository
        val application = application as MyApplication
        repository = Repository.getInstance(
            application.database.postDao(),
            application.database.userDao(),
            this
        )

        // Setup Navigation
        setupNavigation()

        // Navigate if needed
        navigateToStartDestination()

        // Start background sync
        startBackgroundSync()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }

    private fun navigateToStartDestination() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val currentUser = auth.currentUser
        val currentDest = navController.currentDestination?.id

        if (currentUser != null && currentDest == R.id.loginFragment) {
            navController.navigate(R.id.homeFragment)
        } else if (currentUser == null && currentDest == R.id.homeFragment) {
            navController.navigate(R.id.loginFragment)
        }
    }

    private fun startBackgroundSync() {
        if (auth.currentUser != null) {
            lifecycleScope.launch {
                try {
                    repository.syncPosts()
                    repository.syncUsers()
                } catch (_: Exception) {
                    // Silent catch
                }
            }
        }
    }

    fun getRepository(): Repository {
        return repository
    }
}
