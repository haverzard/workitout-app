package com.haverzard.workitout.data.models

data class News (
    var status: String,
    var totalResults: Int,
    var articles: ArrayList<Article>
)