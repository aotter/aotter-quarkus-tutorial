package model.vo

data class ArticleResponse (
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var author: String? = null,
    var authorName: String? = null,
    var lastModifiedTime: String? = null
)