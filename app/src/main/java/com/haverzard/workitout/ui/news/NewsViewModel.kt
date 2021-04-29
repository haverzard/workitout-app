package com.haverzard.workitout.ui.news

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R
import com.haverzard.workitout.data.api.NewsAPIClient
import com.haverzard.workitout.data.api.NewsAPIService
import com.haverzard.workitout.data.models.Article
import com.haverzard.workitout.data.models.News
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsViewModel : ViewModel() {

    private val country: String = "id"
    private val category: String = "sports"
    private val api_key: String = "76b33f118a4a497dafe751d9db583719"
    val articles = MutableLiveData<List<Article>>()
    val error = MutableLiveData<Boolean>(false)

    init {
        fetchData(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                if (response.isSuccessful) {
                    println("TEST")
                    articles.value = response.body()!!.articles
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                error.value = true
            }
        })
    }

    fun fetchData(callback: Callback<News>) {
        val request = NewsAPIClient.buildClient(NewsAPIService::class.java)
        val call = request.getNews(country, category, api_key)
        call.enqueue(callback)
    }
}