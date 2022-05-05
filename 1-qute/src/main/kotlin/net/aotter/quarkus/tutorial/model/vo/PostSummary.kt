package net.aotter.quarkus.tutorial.model.vo

data class PostSummary(
    var id: String,
    var title: String,
    var category: String,
    var authorName: String,
    var lastModifiedTime: String,
    var published: Boolean
)