package net.aotter.quarkus.tutorial.model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import net.aotter.quarkus.tutorial.security.Role
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity
data class User(
    @field: BsonProperty("_id")
    var id: ObjectId? = null,
    var username: String,
    var credentials: String,
    var role: Role,
    var deleted: Boolean
): AuditingEntity()
