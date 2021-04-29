package com.haverzard.workitout.data.models

data class Article (
    var source: Source,
    var author: String?,
    var title: String,
    var url: String,
    var urlToImage: String?,
    var publishedAt: String,
)