package repository

import com.mongodb.WriteConcern
import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.set
import com.mongodb.reactivestreams.client.*
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import io.vertx.core.Vertx
import model.po.ClamData
import model.po.State
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import org.reactivestreams.Publisher
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ClamDataRepository @Inject constructor(val logger: Logger, val vertx: Vertx) {

    @ConfigProperty(name = "quarkus.mongodb.database")
    private lateinit var dbName: String

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    private lateinit var uri: String

    private lateinit var mongoClient: MongoClient

    private val PAGE_ENTITY_NUM = 10

    @PostConstruct
    fun init(){
        mongoClient = MongoClients.create(uri)
    }

    private fun getCol(collectionName: String): MongoCollection<Document> =
            mongoClient.getDatabase(dbName).getCollection(collectionName).withWriteConcern(WriteConcern.ACKNOWLEDGED)

    suspend fun create(collectionName: String, data: ClamData, author: String): String? {
        data.apply {
            this.append(ClamData::collectionName.name, collectionName)
            this.append(ClamData::authorId.name, author)
            this.append(ClamData::state.name, State.TEMP.name)
            this.append(ClamData::createTime.name, Instant.now())
        }
        val result = uniAwait(getCol(collectionName).insertOne(data))
        return result?.insertedId?.asObjectId()?.value?.toHexString()
    }

    suspend fun getPublishedEntities(collectionName: String): List<Document> {
        return multiAwait(getCol(collectionName).find(eq(ClamData::state.name, State.PUBLISHED.name)))
    }

    suspend fun getPublishedEntityById(collectionName: String, id: String): Document? {
        val publisher = getCol(collectionName).find(
                and(
                        eq(ClamData::state.name, State.PUBLISHED.name),
                        eq("_id", ObjectId(id))
                )
        ).limit(1)
        return uniAwait(publisher)
    }

    suspend fun updateEntity(collectionName: String, id: String, data: Document): Document? {
        val updates = data.keys.filterNot { it == "id" }
                .map { set(it, data[it]) }.toMutableList()
        return coroutineUpdate(collectionName, id, updates)
    }

    suspend fun updateEntityState(collectionName: String, id: String, state: State): Document? {
        val updates = mutableListOf<Bson>()
        updates.add(set(ClamData::state.name, state.name))
        if(state == State.PUBLISHED)
            updates.add(set(
                    ClamData::publishedTime.name,
                    Document("\$ifNull", listOf("\$${ClamData::publishedTime.name}", Date()))
            ))
        updates.add(set(ClamData::state.name, state.name))
        return coroutineUpdate(collectionName, id, updates)
    }

    private suspend fun coroutineUpdate(collectionName: String, id: String, updates: MutableList<Bson>): Document?{
        updates.add(set(ClamData::lastModifiedTime.name, Instant.now()))
        val publisher = getCol(collectionName).findOneAndUpdate(
                eq(ObjectId(id)),
                updates,
                FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        )
        return uniAwait(publisher)
    }

    suspend fun getEntityById(collectionName: String, id: String): Document? {
        val publisher = getCol(collectionName).find(
                eq("_id", ObjectId(id))
        ).limit(1)
        return uniAwait(publisher)
    }

    private suspend fun <T> uniAwait(publisher: Publisher<T>): T{
        return Uni.createFrom().publisher(publisher).awaitSuspending()
    }

    private suspend fun <T> multiAwait(publisher: Publisher<T>): List<T>{
        return Multi.createFrom().publisher(publisher).collect().asList().awaitSuspending()
    }

}

