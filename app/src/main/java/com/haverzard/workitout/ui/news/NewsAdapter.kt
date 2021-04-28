package com.haverzard.workitout.ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haverzard.workitout.R
import com.haverzard.workitout.data.models.Article

class NewsAdapter(val articles: List<Article>): RecyclerView.Adapter<NewsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        return holder.bind(articles[position])
    }
}

class NewsViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
    private val photo:ImageView = itemView.findViewById(R.id.image_url)
    private val title:TextView = itemView.findViewById(R.id.article_title)
    private val description:TextView = itemView.findViewById(R.id.article_description)
    private val author:TextView = itemView.findViewById(R.id.article_author)

    fun bind(article: Article) {
        Glide.with(itemView.context).load(article.urlToImage).into(photo)
        title.text = article.title
        description.text = article.description
        author.text = "Author: "+article.author
    }

}