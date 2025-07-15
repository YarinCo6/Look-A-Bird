package com.example.look_a_bird.database

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.example.look_a_bird.model.Post
import com.example.look_a_bird.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class Repository private constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val context: Context
) {

    private val firestore = FirebaseFirestore.getInstance()
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun getAllPosts(): LiveData<List<Post>> = postDao.getAllPosts()
    fun getPostsByUser(userId: String): LiveData<List<Post>> = postDao.getPostsByUser(userId)
    fun getUserById(userId: String): LiveData<User?> = userDao.getUserByIdLive(userId)

    // sync with Firebase
    suspend fun syncPosts() {
        try {
            val lastUpdate = getLastPostUpdateTime()
            val snapshot = firestore.collection("posts")
                .whereGreaterThan("lastUpdated", com.google.firebase.Timestamp(lastUpdate, 0))
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    Post.fromFirestore(doc.id, data)
                }
            }

            if (posts.isNotEmpty()) {
                postDao.insertAllPosts(posts)
                saveLastPostUpdateTime(System.currentTimeMillis() / 1000)
            }
        } catch (e: Exception) {
            // Handle sync error silently
        }
    }

    suspend fun syncUsers() {
        try {
            val lastUpdate = getLastUserUpdateTime()
            val snapshot = firestore.collection("users")
                .whereGreaterThan("lastUpdated", com.google.firebase.Timestamp(lastUpdate, 0))
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    User.fromFirestore(doc.id, data)
                }
            }

            if (users.isNotEmpty()) {
                userDao.insertAllUsers(users)
                saveLastUserUpdateTime(System.currentTimeMillis() / 1000)
            }
        } catch (e: Exception) {
            // Handle sync error silently
        }
    }

    suspend fun addPost(post: Post) {
        try {
            val docRef = firestore.collection("posts").document()
            post.id = docRef.id
            docRef.set(post.toMap()).await()

            post.lastUpdated = System.currentTimeMillis() / 1000
            postDao.insertPost(post)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updatePost(post: Post) {
        try {
            firestore.collection("posts").document(post.id)
                .set(post.toMap()).await()

            post.lastUpdated = System.currentTimeMillis() / 1000
            postDao.updatePost(post)
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deletePost(postId: String) {
        try {
            firestore.collection("posts").document(postId).delete().await()

            postDao.deletePostById(postId)
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun getLastPostUpdateTime(): Long {
        return prefs.getLong("last_post_update", 0L)
    }

    private fun saveLastPostUpdateTime(time: Long) {
        prefs.edit().putLong("last_post_update", time).apply()
    }

    private fun getLastUserUpdateTime(): Long {
        return prefs.getLong("last_user_update", 0L)
    }

    private fun saveLastUserUpdateTime(time: Long) {
        prefs.edit().putLong("last_user_update", time).apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: Repository? = null

        fun getInstance(postDao: PostDao, userDao: UserDao, context: Context): Repository {
            return INSTANCE ?: synchronized(this) {
                val instance = Repository(postDao, userDao, context)
                INSTANCE = instance
                instance
            }
        }
    }
}