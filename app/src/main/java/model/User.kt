package com.example.look_a_bird.model

data class User(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var profileImageUrl: String = "",
    var memberSince: Long = 0L,
    var postsCount: Int = 0,
    var speciesCount: Int = 0,
    var locationsCount: Int = 0
)