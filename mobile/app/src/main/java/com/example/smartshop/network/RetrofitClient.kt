package com.example.smartshop.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Use 10.0.2.2 for the Android emulator to reach the host machine.
    // For a physical device, replace with your PC's local IP, e.g. http://192.168.1.100:8000/
    const val BASE_URL = "http://10.0.2.2:8000/"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun resolveUrl(rawUrl: String?): String? {
        return rawUrl?.let {
            if (it.startsWith("http://") || it.startsWith("https://")) it
            else BASE_URL.trimEnd('/') + "/" + it.trimStart('/')
        }
    }
}
