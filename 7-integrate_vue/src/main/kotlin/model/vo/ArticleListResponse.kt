package model.vo

data class ArticleListResponse (
    val id: String? = null,
    val authorName:String? = null,
    val category: String? = null,
    val title: String? = null,
    val lastModifiedTime: String? = null,
    val published: Boolean?= null
)