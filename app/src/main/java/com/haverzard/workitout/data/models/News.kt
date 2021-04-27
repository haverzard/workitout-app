package com.haverzard.workitout.data.models

import com.google.gson.annotations.SerializedName

data class News (
    var status: String,
    var totalResults: Int,
    var articles: ArrayList<Article>
)