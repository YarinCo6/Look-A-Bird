package com.example.look_a_bird.model

data class Post(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var userProfileImage: String = "",
    var birdSpecies: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var location: String = "",
    var timestamp: Long = 0L
)