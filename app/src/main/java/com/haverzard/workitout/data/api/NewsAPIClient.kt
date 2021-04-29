package com.haverzard.workitout.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsAPIClient {
    private const val BASE_URL: String = "https://newsapi.org/v2/"

    private val httpClient : OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val retrofit : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    fun<T> buildClient(service: Class<T>): T{
        return retrofit.create(service)
    }
}