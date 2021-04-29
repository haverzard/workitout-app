package com.haverzard.workitout.ui.news

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haverzard.workitout.R
import com.haverzard.workitout.data.models.Article
import java.text.DateFormat
import java.util.*

class NewsAdapter(val articles: List<Article>): RecyclerView.Adapter<NewsViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        return NewsViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        return holder.bind(articles[position])
    }
}

class NewsViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
    private val photo = itemView.findViewById<ImageView>(R.id.image_url)
    private val title = itemView.findViewById<TextView>(R.id.article_title)
    private val author = itemView.findViewById<TextView>(R.id.article_author)
    private val publishedDate = itemView.findViewById<TextView>(R.id.article_published_date)

    fun bind(article: Article) {
        if (article.urlToImage != null) {
            Glide.with(itemView.context).load(article.urlToImage).into(photo)
        }
        title.text = article.title
        val authorName = """Author: ${if (article.author != null) article.author else "-"}"""
        author.text = authorName
        if (article.publishedAt != null) {
            val formatter = SimpleDateFormat("yyyy-mm-d", Locale.ENGLISH)
            val date = formatter.parse(article.publishedAt)
            val df: DateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)
            publishedDate.text = df.format(date.time)
        }
    }

    companion object {
        fun create(parent: ViewGroup): NewsViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.news_item, parent, false)
            return NewsViewHolder(view)
        }
    }
}