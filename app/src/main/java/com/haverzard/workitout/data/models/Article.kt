package com.haverzard.workitout.data.models

import com.google.gson.annotations.SerializedName

data class Article (
    var source: Source,
    var totalResults: String,
    var status: String,
    var author: String,
    var title: String,
    var description: String,
    var url: String,
    var urlToImage: String,
    var publishedAt: String,
    var content: String
)