package net.aotter.quarkus.tutorial.model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity
class Post(
    @field: BsonProperty("_id")
    var id: ObjectId? = null,
    var authorId: ObjectId,
    var authorName: String,
    var category: String,
    var title: String,
    var content: String,
    var published: Boolean,
    var deleted: Boolean,
): AuditingEntity()