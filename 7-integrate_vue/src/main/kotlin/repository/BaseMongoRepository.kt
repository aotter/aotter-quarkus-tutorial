package repository

import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.IndexModel
import io.quarkus.mongodb.FindOptions
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.mongodb.reactive.ReactiveMongoCollection
import io.smallrye.mutiny.coroutines.asFlow
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import model.po.BaseMongoEntity
import org.bson.conversions.Bson
import org.jboss.logging.Logger
import java.time.Instant
import javax.inject.Inject

@ExperimentalCoroutinesApi
abstract class BaseMongoRepository<Entity : Any> : ReactivePanacheMongoRepository<Entity> {

    @Inject
    lateinit var logger: Logger

    val col: ReactiveMongoCollection<Entity> = this.mongoCollection()


    /**
     * create indexes
     */
    fun createIndexes(vararg indexModels: IndexModel) {
        col.createIndexes(indexModels.asList())
                .onItemOrFailure()
                .transform { result, t ->
                    if (t != null) {
                        logger.error("collection ${col.documentClass.simpleName} index creation failed: ${t.message}")
                    } else {
                        logger.info("collection ${col.documentClass.simpleName} index created: $result")
                    }
                }.subscribe().with {  }
    }

    /**
     * save the entity
     */
    suspend fun save(entity: Entity): Entity {
        if (entity is BaseMongoEntity<*>) {
            entity.lastModifiedTime = entity.lastModifiedTime?.let { Instant.now() } ?: entity.createdTime
        }
        return persistOrUpdate(entity).awaitSuspending()
    }

    /**
     * convert a [ReactivePanacheQuery] to list in coroutine
     */
    suspend fun ReactivePanacheQuery<Entity>.toList() = this.list().awaitSuspending()

    /**
     * convert a [ReactivePanacheQuery] to coroutine flow
     */
    suspend fun ReactivePanacheQuery<Entity>.asFlow() = this.stream().asFlow()

    suspend fun findAsList(filter: Bson? = null): List<Entity> =
            (filter?.let { col.find(it) } ?: col.find()).collect().asList().awaitSuspending()

    suspend fun findAsList(filter: Bson? = null, findOptions: FindOptions): List<Entity> =
            (filter?.let { col.find(it, findOptions) } ?: col.find(findOptions)).collect().asList().awaitSuspending()

    suspend fun findAsFlow(filter: Bson? = null): Flow<Entity> =
            (filter?.let { col.find(it) } ?: col.find()).asFlow()

    suspend fun findAsFlow(filter: Bson? = null, findOptions: FindOptions): Flow<Entity> =
            (filter?.let { col.find(it, findOptions) } ?: col.find(findOptions)).asFlow()

    suspend fun count(filter: Bson): Long = col.countDocuments(filter).awaitSuspending()

    suspend fun findOne(filter: Bson): Entity? =
            col.find(filter, FindOptions().limit(1)).collect().asList().awaitSuspending().firstOrNull()

    suspend fun findOneAndUpdate(filter: Bson, update: Bson, option: FindOneAndUpdateOptions): Entity? =
        col.findOneAndUpdate(filter, update, option).awaitSuspending()

}