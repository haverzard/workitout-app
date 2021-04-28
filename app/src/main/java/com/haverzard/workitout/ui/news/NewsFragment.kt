package com.haverzard.workitout.ui.news

import android.content.Context

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R
import com.haverzard.workitout.data.api.NewsAPIClient
import com.haverzard.workitout.data.api.NewsAPIService
import com.haverzard.workitout.data.models.News
import com.haverzard.workitout.ui.news.NewsViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewsFragment : Fragment() {

    private lateinit var newsViewModel: NewsViewModel
    private val country: String = "id";
    private val category: String = "sports";
    private val api_key: String = "76b33f118a4a497dafe751d9db583719";

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_news, container, false)
        val textView: TextView = root.findViewById(R.id.text_dashboard)
        newsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val request = NewsAPIClient.buildClient(NewsAPIService::class.java)
        val call = request.getNews(country, category, api_key)
        val recyclerView = view.findViewById<RecyclerView>(R.id.newsRecyclerView)

        call.enqueue(object : Callback<News> {
            override fun onResponse(call: Call<News>, response: Response<News>) {
                if (response.isSuccessful) {
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = NewsAdapter(response.body()!!.articles)
                }
            }

            override fun onFailure(call: Call<News>, t: Throwable) {
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}