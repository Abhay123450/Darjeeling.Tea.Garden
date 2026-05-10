package com.darjeelingteagarden.features.packagedTea.api

import com.darjeelingteagarden.features.packagedTea.model.PackagedTea
import retrofit2.http.GET
import retrofit2.http.Path

data class PackagedTeaResponse(
    val success: Boolean,
    val data: List<PackagedTea>
)

data class PackagedTeaDetailsResponse(
    val success: Boolean,
    val data: PackagedTea
)

interface PackagedTeaApiService {
    @GET("/api/v1/packaged-tea")
    suspend fun getPackagedTeas(): PackagedTeaResponse
    @GET("/api/v1/packaged-tea/{id}")
    suspend fun getPackagedTeaById(@Path("id") packagedTeaId: String): PackagedTeaDetailsResponse
}