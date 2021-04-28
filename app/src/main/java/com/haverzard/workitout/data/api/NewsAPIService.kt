package com.haverzard.workitout.data.api

import com.haverzard.workitout.data.models.News
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPIService {
    @GET("top-headlines")
    fun getNews(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("apiKey") apiKey: String
    ): Call<News>
}