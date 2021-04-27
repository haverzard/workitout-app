package com.haverzard.workitout.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsAPIClient {
    private val BASE_URL: String = "https://newsapi.org/v2/"
    private val country: String = "id";
    private val category: String = "sports";
    private val API_KEY: String = "76b33f118a4a497dafe751d9db583719"

    private val gson : Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val httpClient : OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    private val retrofit : Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService : NewsAPIService by lazy{
        retrofit.create(NewsAPIService::class.java)
    }
}