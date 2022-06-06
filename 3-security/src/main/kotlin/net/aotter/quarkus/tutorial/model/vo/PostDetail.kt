package net.aotter.quarkus.tutorial.model.vo

data class PostDetail(
    var category: String,
    var title: String,
    var content: String? = null,
    var authorId: String,
    var authorName: String,
    var lastModifiedTime: String
)