package model.po


import org.bson.types.ObjectId
import java.time.Instant

data class Article(
    var id: ObjectId? = null,
    var author: String? = null,
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var enabled: Boolean? = true,
    var createdTime: Instant? = Instant.now(),
    var lastModifiedTime: Instant? = null
)