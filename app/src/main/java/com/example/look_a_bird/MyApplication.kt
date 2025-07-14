package com.example.look_a_bird

import android.app.Application
import android.content.Context
import com.example.look_a_bird.database.AppDatabase
import java.util.concurrent.Executors

class MyApplication : Application() {

    object Globals {
        var appContext: Context? = null
    }

    // Thread pool for background operations
    val executorService = Executors.newFixedThreadPool(4)

    // Room database instance
    val database by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        Globals.appContext = applicationContext
    }
}