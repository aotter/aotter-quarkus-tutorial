package model.po

import com.fasterxml.jackson.annotation.JsonFormat
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Instant

data class ClamData(
        var id: ObjectId? = null,

        var collectionName: String? = null,

        var authorId: String? = null,

        var createTime: Instant? = null,

        var lastModifiedTime: Instant? = null,

        var publishedTime: Instant? = null,

        var state: State? = null
): Document()

@JsonFormat(shape = JsonFormat.Shape.STRING)
enum class State {
    TEMP,
    PUBLISHED,
    ARCHIVED
}
