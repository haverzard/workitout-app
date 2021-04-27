package com.haverzard.workitout.data.api

import com.haverzard.workitout.data.models.News
import retrofit2.http.*

interface NewsAPIService {
    @GET("news")
    suspend fun getNews(): News
}