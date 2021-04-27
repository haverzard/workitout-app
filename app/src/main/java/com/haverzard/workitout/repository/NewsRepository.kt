package com.haverzard.workitout.repository

import com.haverzard.workitout.data.api.NewsAPIHelper

class NewsRepository(private val newsAPIHelper: NewsAPIHelper) {

    suspend fun getNews() = newsAPIHelper.getNews()
}