data class Post(
    var userId: String = "",
    var userName: String = "",
    var userProfileImage: String = "",
    var birdSpecies: String = "",
    var scientificName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timestamp: com.google.firebase.Timestamp? = null
)
