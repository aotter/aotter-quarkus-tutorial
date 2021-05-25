package model.po

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoEntity
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import java.time.Instant

abstract class BaseMongoEntity<T>(

        var lastModifiedTime: Instant? = null,

        var createdTime: Instant = Instant.now()

): ReactivePanacheMongoEntity() {

    @Suppress("UNCHECKED_CAST")
    fun save(): Uni<T> {
        lastModifiedTime = lastModifiedTime?.let { Instant.now() } ?: createdTime
        return persistOrUpdate()
                .map { this as T }
    }

    suspend fun coroutineSave(): T{
        return save().awaitSuspending()
    }
}