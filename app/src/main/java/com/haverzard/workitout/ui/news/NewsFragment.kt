package com.haverzard.workitout.ui.news

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haverzard.workitout.R

class NewsFragment : Fragment() {

    private lateinit var newsViewModel: NewsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_news, container, false)

        val recyclerView = root.findViewById<RecyclerView>(R.id.newsRecyclerView)
        recyclerView.setHasFixedSize(true)
        var totalCol = 1
        if (activity?.resources?.configuration?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            totalCol = 2
        }
        recyclerView.layoutManager = GridLayoutManager(context, totalCol)
        newsViewModel.articles.observe(this) {
            if (it.isNotEmpty()) {
                recyclerView.adapter = NewsAdapter(it)
                root.findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }
        newsViewModel.error.observe(this) {
            if (it) {
                Toast.makeText(context, "Error on fetching data", Toast.LENGTH_SHORT).show()
                root.findViewById<ProgressBar>(R.id.progress_bar).visibility = View.GONE
            }
        }
        return root
    }
}