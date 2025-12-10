package com.example.aplikasimonitoringkelas.data.remote

import com.example.aplikasimonitoringkelas.utils.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API Configuration
 * Singleton object untuk menyediakan instance ApiService
 */
object ApiConfig {

    /**
     * Create OkHttpClient with logging interceptor
     */
    private fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (AppConfig.isDebugMode()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(AppConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AppConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Create Retrofit instance
     */
    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Get ApiService instance
     *
     * @return ApiService instance untuk melakukan API calls
     */
    fun getApiService(): ApiService {
        return provideRetrofit().create(ApiService::class.java)
    }
}