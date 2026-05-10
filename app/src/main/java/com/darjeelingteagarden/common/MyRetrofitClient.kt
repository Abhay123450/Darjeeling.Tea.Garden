package com.darjeelingteagarden.common

import com.darjeelingteagarden.features.looseTea.api.LooseTeaApiService
import com.darjeelingteagarden.features.order.api.OrderApiService
import com.darjeelingteagarden.features.packagedTea.api.PackagedTeaApiService
import com.darjeelingteagarden.repository.AppDataSingleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.kotlinx.serialization.asConverterFactory

const val BASE_URL = "https://rapti-tea.el.r.appspot.com"

object MyRetrofitClient {

    class AuthInterceptor(private val authToken: String) : Interceptor{
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val newRequest = originalRequest.newBuilder()
            newRequest.addHeader("auth-token", authToken)
            return chain.proceed(newRequest.build())
        }
    }

    private val retrofit1 by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(AppDataSingleton.getAuthToken))
            .build()
        Retrofit
            .Builder().baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val retrofit2 by lazy {

        val json = Json {
            classDiscriminator = "itemType"
            ignoreUnknownKeys = true
        }

        val contentType = "application/json".toMediaType()

        Retrofit
            .Builder().baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    val looseTeaApi: LooseTeaApiService by lazy {
        retrofit1.create(LooseTeaApiService::class.java)
    }

    val packagedTeaApi: PackagedTeaApiService by lazy {
        retrofit1.create(PackagedTeaApiService::class.java)
    }

    val orderApi: OrderApiService by lazy {
        retrofit1.create(OrderApiService::class.java)
    }

}