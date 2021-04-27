package com.haverzard.workitout.data.api

class NewsAPIHelper(private val newsAPIService: NewsAPIService) {

    suspend fun getNews() = newsAPIService.getNews()
}