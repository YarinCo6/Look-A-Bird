package com.example.look_a_bird.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface iNaturalistService {

    /**
     * Search for bird species by name
     * @param query - bird name to search for
     * @param taxonId - 3 = Birds class in iNaturalist taxonomy
     * @param perPage - number of results to return (default 20)
     * @param order - sort order (name, observations_count)
     */
    @GET("taxa")
    suspend fun searchBirds(
        @Query("q") query: String,
        @Query("taxon_id") taxonId: Int = 3, // 3 = Birds (Aves) in iNaturalist
        @Query("per_page") perPage: Int = 20,
        @Query("order") order: String = "observations_count",
        @Query("order_by") orderBy: String = "desc"
    ): Response<iNaturalistResponse>

    /**
     * Search for bird species by location
     * @param latitude - GPS latitude
     * @param longitude - GPS longitude
     * @param radius - search radius in KM (default 50)
     * @param taxonId - 3 = Birds
     */
    @GET("taxa")
    suspend fun searchBirdsByLocation(
        @Query("lat") latitude: Float,
        @Query("lng") longitude: Float,
        @Query("radius") radius: Int = 50,
        @Query("taxon_id") taxonId: Int = 3, // 3 = Birds
        @Query("per_page") perPage: Int = 20,
        @Query("order") order: String = "observations_count",
        @Query("order_by") orderBy: String = "desc"
    ): Response<iNaturalistResponse>

    /**
     * Get popular birds globally
     * Used as default suggestions
     */
    @GET("taxa")
    suspend fun getPopularBirds(
        @Query("taxon_id") taxonId: Int = 3, // 3 = Birds
        @Query("per_page") perPage: Int = 10,
        @Query("order") order: String = "observations_count",
        @Query("order_by") orderBy: String = "desc",
        @Query("rank") rank: String = "species"
    ): Response<iNaturalistResponse>

    companion object {
        const val BASE_URL = "https://api.inaturalist.org/v1/"

        // Common bird families for filtering
        val BIRD_FAMILIES = listOf(
            "Passeriformes", // Songbirds
            "Falconiformes", // Raptors
            "Columbiformes", // Doves and pigeons
            "Piciformes",    // Woodpeckers
            "Strigiformes"   // Owls
        )
    }
}