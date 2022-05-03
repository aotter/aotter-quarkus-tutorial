package net.aotter.quarkus.tutorial.model.vo

data class PostSummary(
    var id: String? = null,
    var title: String? = null,
    var category: String? = null,
    var authorName: String? = null,
    var lastModifiedTime: String? = null,
    var published: Boolean? = null
)