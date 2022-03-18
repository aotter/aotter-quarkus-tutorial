package model.po

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoEntity
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoEntityBase
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import java.time.Instant

abstract class BaseMongoEntity<T>(

        var lastModifiedTime: Instant? = null,

        var createdTime: Instant = Instant.now()

): ReactivePanacheMongoEntity() {

    @Suppress("UNCHECKED_CAST")
    fun <T : ReactivePanacheMongoEntityBase> save(): Uni<T> {
        lastModifiedTime = lastModifiedTime?.let { Instant.now() } ?: createdTime
        return persistOrUpdate<T>()
            .map { this as T }
    }


    suspend fun <T : ReactivePanacheMongoEntityBase> coroutineSave(): T {
        return save<T>().awaitSuspending()
    }
}