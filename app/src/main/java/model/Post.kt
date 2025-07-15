package com.example.look_a_bird.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var userProfileImage: String = "",
    var birdSpecies: String = "",
    var scientificName: String = "",
    var description: String = "",
    var imageUrl: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var timestamp: Long = 0L,
    var lastUpdated: Long = 0L
) {
    // convert to Firebase format
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "userId" to userId,
            "userName" to userName,
            "userProfileImage" to userProfileImage,
            "birdSpecies" to birdSpecies,
            "scientificName" to scientificName,
            "description" to description,
            "imageUrl" to imageUrl,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to FieldValue.serverTimestamp(),
            "lastUpdated" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        // convert from Firebase to Room format
        fun fromFirestore(id: String, data: Map<String, Any?>): Post {
            return Post(
                id = id,
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "",
                userProfileImage = data["userProfileImage"] as? String ?: "",
                birdSpecies = data["birdSpecies"] as? String ?: "",
                scientificName = data["scientificName"] as? String ?: "",
                description = data["description"] as? String ?: "",
                imageUrl = data["imageUrl"] as? String ?: "",
                latitude = data["latitude"] as? Double ?: 0.0,
                longitude = data["longitude"] as? Double ?: 0.0,
                timestamp = (data["timestamp"] as? Timestamp)?.seconds ?: 0L,
                lastUpdated = (data["lastUpdated"] as? Timestamp)?.seconds ?: 0L
            )
        }
    }
}
