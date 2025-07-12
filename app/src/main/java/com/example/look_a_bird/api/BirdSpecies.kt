package com.example.look_a_bird.api

import com.google.gson.annotations.SerializedName

// Main response from iNaturalist API
data class iNaturalistResponse(
    @SerializedName("results")
    val results: List<Taxon>
)

// Individual bird/species data
data class Taxon(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val scientificName: String,

    @SerializedName("preferred_common_name")
    val commonName: String?,

    @SerializedName("default_photo")
    val defaultPhoto: TaxonPhoto?,

    @SerializedName("wikipedia_url")
    val wikipediaUrl: String?,

    @SerializedName("rank")
    val rank: String,

    @SerializedName("ancestor_ids")
    val ancestorIds: List<Int>
) {
    // Helper function to get display name
    fun getDisplayName(): String {
        return commonName ?: scientificName
    }

    // Helper function to get photo URL
    fun getPhotoUrl(): String? {
        return defaultPhoto?.mediumUrl
    }
}

// Photo data from iNaturalist
data class TaxonPhoto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("medium_url")
    val mediumUrl: String?,

    @SerializedName("square_url")
    val squareUrl: String?
)

// Bird species for our app (simplified version)
data class BirdSpecies(
    val id: Int,
    val commonName: String,
    val scientificName: String,
    val photoUrl: String?,
    val wikipediaUrl: String?
) {
    companion object {
        // Convert from iNaturalist Taxon to our BirdSpecies
        fun fromTaxon(taxon: Taxon): BirdSpecies {
            return BirdSpecies(
                id = taxon.id,
                commonName = taxon.getDisplayName(),
                scientificName = taxon.scientificName,
                photoUrl = taxon.getPhotoUrl(),
                wikipediaUrl = taxon.wikipediaUrl
            )
        }
    }
}