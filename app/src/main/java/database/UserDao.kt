package com.example.look_a_bird.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.look_a_bird.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserByIdLive(userId: String): LiveData<User?>

    @Query("SELECT * FROM users WHERE lastUpdated > :lastUpdate")
    suspend fun getUsersNewerThan(lastUpdate: Long): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<User>)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT MAX(lastUpdated) FROM users")
    suspend fun getLastUpdateTime(): Long?
}