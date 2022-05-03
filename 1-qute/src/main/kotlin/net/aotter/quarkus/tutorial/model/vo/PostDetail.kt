package net.aotter.quarkus.tutorial.model.vo

data class PostDetail(
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var authorId: String? = null,
    var authorName: String? = null,
    var lastModifiedTime: String? = null
)