package com.example.look_a_bird

data class Post(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var userProfileImage: String = "",
    var birdSpecies: String = "",
    var scientificName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var latitude: Float = 0.0f,
    var longitude: Float = 0.0f,
    var location: String = "",
    var timestamp: Long = 0L
)