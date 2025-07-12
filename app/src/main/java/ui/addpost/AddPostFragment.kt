package com.example.look_a_bird.ui.addpost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.look_a_bird.R
import com.example.look_a_bird.model.Post
import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.widget.AutoCompleteTextView
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.text.TextWatcher
import android.text.Editable
import com.example.look_a_bird.api.ApiRepository
import com.example.look_a_bird.api.ApiResult
import com.example.look_a_bird.api.BirdSpecies

class AddPostFragment : Fragment() {

    private lateinit var imagePostPreview: ImageView
    private lateinit var buttonSelectImage: Button
    private lateinit var autoCompleteBirdName: AutoCompleteTextView
    private lateinit var editTextScientificName: TextInputEditText
    private lateinit var editTextDescription: TextInputEditText
    private lateinit var editTextLocation: TextInputEditText
    private lateinit var buttonGetLocation: Button
    private lateinit var buttonSavePost: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var birdSearchProgress: ProgressBar

    private val args: AddPostFragmentArgs by navArgs()
    private val apiRepository = ApiRepository.getInstance()

    private var selectedImageUri: String = ""
    private var selectedBird: BirdSpecies? = null
    private var searchJob: Job? = null

    // New: Map of names to BirdSpecies
    private val birdSuggestionsMap = linkedMapOf<String, BirdSpecies>()

    private lateinit var birdAdapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupBirdSearch()
        setupClickListeners()
        loadInitialBirdSuggestions()
    }

    private fun setupViews(view: View) {
        imagePostPreview = view.findViewById(R.id.image_post_preview)
        buttonSelectImage = view.findViewById(R.id.button_select_image)
        autoCompleteBirdName = view.findViewById(R.id.auto_complete_bird_name)
        editTextScientificName = view.findViewById(R.id.edit_text_scientific_name)
        editTextDescription = view.findViewById(R.id.edit_text_description)
        editTextLocation = view.findViewById(R.id.edit_text_location)
        buttonGetLocation = view.findViewById(R.id.button_get_location)
        buttonSavePost = view.findViewById(R.id.button_save_post)
        progressBar = view.findViewById(R.id.progress_bar)
        birdSearchProgress = view.findViewById(R.id.bird_search_progress)
    }

    private fun setupBirdSearch() {
        birdAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        autoCompleteBirdName.setAdapter(birdAdapter)
        autoCompleteBirdName.threshold = 2

        autoCompleteBirdName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.length >= 2) {
                    searchBirds(query)
                } else if (query.isEmpty()) {
                    selectedBird = null
                    editTextScientificName.setText("")
                }
            }
        })

        autoCompleteBirdName.setOnItemClickListener { _, _, position, _ ->
            val selectedName = birdAdapter.getItem(position)
            val selected = birdSuggestionsMap[selectedName]
            if (selected != null) {
                selectedBird = selected
                autoCompleteBirdName.setText(selected.commonName, false)
                editTextScientificName.setText(selected.scientificName)

                autoCompleteBirdName.clearFocus()
                autoCompleteBirdName.dismissDropDown()
                Toast.makeText(context, "Selected: ${selected.commonName}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBirdSuggestions(birds: List<BirdSpecies>) {
        birdSuggestionsMap.clear()
        for (bird in birds) {
            birdSuggestionsMap[bird.commonName] = bird
        }

        birdAdapter.clear()
        birdAdapter.addAll(birdSuggestionsMap.keys)
        birdAdapter.notifyDataSetChanged()

        if (autoCompleteBirdName.hasFocus()) {
            autoCompleteBirdName.showDropDown()
        }
    }

    private fun loadInitialBirdSuggestions() {
        birdSearchProgress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val result = if (args.latitude != 0.0f && args.longitude != 0.0f) {
                    apiRepository.searchBirdsByLocation(args.latitude, args.longitude)
                } else {
                    apiRepository.getPopularBirds()
                }

                when (result) {
                    is ApiResult.Success -> {
                        updateBirdSuggestions(result.data)
                        if (args.latitude != 0.0f && args.longitude != 0.0f) {
                            Toast.makeText(context, "Showing birds common in your area!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(context, "Error loading bird suggestions", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            birdSearchProgress.visibility = View.GONE
        }
    }

    private fun searchBirds(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(300)

            birdSearchProgress.visibility = View.VISIBLE

            try {
                when (val result = apiRepository.searchBirds(query)) {
                    is ApiResult.Success -> {
                        updateBirdSuggestions(result.data)
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(context, "Search error: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            birdSearchProgress.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        buttonSelectImage.setOnClickListener {
            selectImage()
        }

        buttonGetLocation.setOnClickListener {
            getCurrentLocation()
        }

        buttonSavePost.setOnClickListener {
            savePost()
        }
    }

    private fun selectImage() {
        Toast.makeText(context, "Image selection - will implement later", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude.toFloat()
                    val lon = location.longitude.toFloat()
                    editTextLocation.setText("Lat: %.5f, Lon: %.5f".format(lat, lon))
                    loadLocationBasedSuggestions(lat, lon)
                } else {
                    Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLocationBasedSuggestions(lat: Float, lon: Float) {
        lifecycleScope.launch {
            birdSearchProgress.visibility = View.VISIBLE

            try {
                when (val result = apiRepository.searchBirdsByLocation(lat, lon)) {
                    is ApiResult.Success -> {
                        updateBirdSuggestions(result.data)
                        Toast.makeText(context, "Updated suggestions for your location!", Toast.LENGTH_SHORT).show()
                    }
                    is ApiResult.Error -> {
                        Toast.makeText(context, "Error loading location suggestions", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Location search error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            birdSearchProgress.visibility = View.GONE
        }
    }

    private fun savePost() {
        val birdName = autoCompleteBirdName.text.toString().trim()
        val scientificName = editTextScientificName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val location = editTextLocation.text.toString().trim()

        if (!validateInput(birdName, description, location)) return

        showLoading(true)

        val newPost = Post(
            id = "",
            userId = "current_user_id",
            userName = "Current User",
            userProfileImage = "",
            birdSpecies = birdName,
            scientificName = scientificName,
            description = description,
            imageUrl = selectedImageUri,
            latitude = args.latitude,
            longitude = args.longitude,
            location = location,
            timestamp = System.currentTimeMillis()
        )

        simulateSavePost(newPost)
    }

    private fun validateInput(birdName: String, description: String, location: String): Boolean {
        if (birdName.isEmpty()) {
            autoCompleteBirdName.error = "Bird name is required"
            return false
        }

        if (!birdName.matches(Regex("^[a-zA-Z\\s]+$"))) {
            autoCompleteBirdName.error = "Bird name can only contain letters and spaces"
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

    private fun simulateSavePost(post: Post) {
        view?.postDelayed({
            showLoading(false)
            Toast.makeText(context, "Post saved successfully!", Toast.LENGTH_SHORT).show()
            clearForm()
        }, 2000)
    }

    private fun clearForm() {
        autoCompleteBirdName.text?.clear()
        editTextScientificName.text?.clear()
        editTextDescription.text?.clear()
        editTextLocation.text?.clear()
        selectedImageUri = ""
        selectedBird = null
        imagePostPreview.setImageResource(android.R.drawable.ic_menu_camera)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonSavePost.isEnabled = !show
        buttonSelectImage.isEnabled = !show
        buttonGetLocation.isEnabled = !show
        autoCompleteBirdName.isEnabled = !show
    }
}
