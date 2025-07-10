package com.example.look_a_bird.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiRepository {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(iNaturalistService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(iNaturalistService::class.java)

    /**
     * Search for birds by name
     * Returns list of matching bird species
     */
    suspend fun searchBirds(query: String): ApiResult<List<BirdSpecies>> {
        return try {
            val response = apiService.searchBirds(query)
            if (response.isSuccessful && response.body() != null) {
                val birds = response.body()!!.results.map { taxon ->
                    BirdSpecies.fromTaxon(taxon)
                }
                ApiResult.Success(birds)
            } else {
                ApiResult.Error("Failed to search birds: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Search for birds by GPS location
     * Useful for location-based suggestions
     */
    suspend fun searchBirdsByLocation(
        latitude: Float,
        longitude: Float
    ): ApiResult<List<BirdSpecies>> {
        return try {
            val response = apiService.searchBirdsByLocation(latitude, longitude)
            if (response.isSuccessful && response.body() != null) {
                val birds = response.body()!!.results.map { taxon ->
                    BirdSpecies.fromTaxon(taxon)
                }
                ApiResult.Success(birds)
            } else {
                ApiResult.Error("Failed to search birds by location: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get popular birds for default suggestions
     * Used when user hasn't typed anything yet
     */
    suspend fun getPopularBirds(): ApiResult<List<BirdSpecies>> {
        return try {
            val response = apiService.getPopularBirds()
            if (response.isSuccessful && response.body() != null) {
                val birds = response.body()!!.results.map { taxon ->
                    BirdSpecies.fromTaxon(taxon)
                }
                ApiResult.Success(birds)
            } else {
                ApiResult.Error("Failed to get popular birds: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ApiRepository? = null

        fun getInstance(): ApiRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ApiRepository().also { INSTANCE = it }
            }
        }
    }
}

/**
 * Generic result wrapper for API responses
 */
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String) : ApiResult<T>()
    data class Loading<T>(val message: String = "Loading...") : ApiResult<T>()
}