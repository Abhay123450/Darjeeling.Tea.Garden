package com.darjeelingteagarden.features.looseTea.api

import com.darjeelingteagarden.features.looseTea.model.LooseTea
import retrofit2.http.GET
import retrofit2.http.Path

data class LooseTeaResponse(
    val success: Boolean,
    val data: List<LooseTea>
)

data class LooseTeaDetailsResponse(
    val success: Boolean,
    val data: LooseTea
)

interface LooseTeaApiService {
    @GET("/api/v1/loose-tea")
    suspend fun getLooseTeas(): LooseTeaResponse
    @GET("/api/v1/loose-tea/{id}")
    suspend fun getLooseTeaById(@Path("id") looseTeaId: String): LooseTeaDetailsResponse
}