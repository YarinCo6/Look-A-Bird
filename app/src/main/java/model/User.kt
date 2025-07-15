package com.example.look_a_bird.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var profileImageUrl: String = "",
    var memberSince: Long = 0L,
    var lastUpdated: Long = 0L
) {
    // convert to Firebase format
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "name" to name,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "memberSince" to memberSince,
            "lastUpdated" to FieldValue.serverTimestamp()
        )
    }

    companion object {
        // convert from Firebase to Room format
        fun fromFirestore(id: String, data: Map<String, Any?>): User {
            return User(
                id = id,
                name = data["name"] as? String ?: "",
                email = data["email"] as? String ?: "",
                profileImageUrl = data["profileImageUrl"] as? String ?: "",
                memberSince = data["memberSince"] as? Long ?: 0L,
                lastUpdated = (data["lastUpdated"] as? Timestamp)?.seconds ?: 0L
            )
        }
    }
}
