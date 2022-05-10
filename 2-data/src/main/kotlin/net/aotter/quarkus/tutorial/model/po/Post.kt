package net.aotter.quarkus.tutorial.model.po

import org.bson.types.ObjectId
import java.time.Instant

data class Post (
    var id: ObjectId? = null,
    var authorId: ObjectId? = null,
    var authorName: String? = null,
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var published: Boolean? = null,
    var deleted: Boolean? = null,
    var lastModifiedTime: Instant? = null,
    var createdTime: Instant = Instant.now()
)