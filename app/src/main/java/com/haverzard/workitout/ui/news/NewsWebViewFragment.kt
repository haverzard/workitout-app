package com.haverzard.workitout.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.haverzard.workitout.R

class NewsWebViewFragment : Fragment() {

    private lateinit var newsViewModel: NewsViewModel
    private val safeArgs: NewsWebViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        newsViewModel =
            ViewModelProviders.of(this).get(NewsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_news_webview, container, false)

        val webView = root.findViewById<WebView>(R.id.news_webview)
        webView.loadUrl(safeArgs.url)
        return root
    }
}