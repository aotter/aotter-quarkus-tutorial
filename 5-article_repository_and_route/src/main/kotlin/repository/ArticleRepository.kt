package repository

import com.mongodb.WriteConcern
import com.mongodb.client.model.*
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.po.Article
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.reactivestreams.Publisher
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.List

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {

    private lateinit var database: MongoDatabase

    @ConfigProperty(name = "quarkus.mongodb.col.article")
    private lateinit var articleCol: String

    @ConfigProperty(name = "quarkus.mongodb.col.user")
    private lateinit var userCol: String

    @ConfigProperty(name = "quarkus.mongodb.database")
    private lateinit var dbName: String

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    private lateinit var uri: String

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var articleColl: MongoCollection<Document>
    private lateinit var userColl: MongoCollection<Document>

    private val PAGE_ENTITY_NUM = 6

    @PostConstruct
    fun init(){
        database = MongoClients.create(uri).getDatabase(dbName)
        articleColl = database.getCollection(articleCol).withWriteConcern(WriteConcern.ACKNOWLEDGED)
        userColl = database.getCollection(userCol).withWriteConcern(WriteConcern.ACKNOWLEDGED)
    }

    suspend fun createArticle(data: Article, author: String): Article? {
        val user = userRepository.findByUserId(author)
        data.author = author
        data.authorName = user?.username
        data.enabled = true
        data.createdTime = Instant.now()
        data.lastModifiedTime = Instant.now()

        val result = uniAwait(articleColl.insertOne(data))
        return result?.insertedId?.asObjectId()?.value?.let {
            data.id = it
            data
        }
    }

    suspend fun updateArticle(id: String, data: Article): Article? {
        val updates = data.keys.filterNot { it == "id" }
            .map { Updates.set(it, data[it]) }.toMutableList()
        return coroutineUpdate(id, updates)
    }

    suspend fun getArticleListByUser(author: String, page: Int): List<ArticleView> {
        val result = articleColl.find(
            Filters.and(
                Filters.eq("author", author),
                Filters.eq("enabled", true)))
            .sort(Sorts.descending("lastModifiedTime"))
            .skip((page - 1) * PAGE_ENTITY_NUM)
            .limit(PAGE_ENTITY_NUM)

        val sdFormat = SimpleDateFormat("yyyy/MM/dd a hh:mm ", Locale.TAIWAN)

        return multiAwait(result).map {
            ArticleView(
                id = it["_id"].toString(),
                category = it["category"] as String,
                title = it["title"] as String,
                content = it["content"] as String,
                author = it["author"] as String,
                lastModifiedTime = sdFormat.format(it["lastModifiedTime"] as Date)
            )
        }
    }

    suspend fun getArticleById(id: String): Article? {
        val publisher = articleColl.find(
            Filters.eq("_id", ObjectId(id))
        ).limit(1)
        return uniAwait(publisher)?.let { Article.documentToArticle(it) }
    }

    suspend fun deleteArticle(id: String): Article? {
        val updates = mutableListOf(Updates.set("enabled",false))
        return coroutineUpdate(id, updates)
    }

    private suspend fun <T> uniAwait(publisher: Publisher<T>): T?{
        return Uni.createFrom().publisher(publisher).awaitSuspending()
    }

    private suspend fun <T> multiAwait(publisher: Publisher<T>): List<T>{
        return Multi.createFrom().publisher(publisher).collect().asList().awaitSuspending()
    }

    private suspend fun coroutineUpdate(id: String, updates: MutableList<Bson>): Article?{
        updates.add(Updates.set(Article::lastModifiedTime.name, Instant.now()))
        val publisher = articleColl.findOneAndUpdate(
            Filters.eq(ObjectId(id)),
            updates,
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER).upsert(false)
        )
        return uniAwait(publisher)?.let { Article.documentToArticle(it) }
    }

    data class ArticleView(
        val id: String? = null,
        val userId: String? = null,
        val category: String? = null,
        val title: String? = null,
        val content: String? = null,
        val author: String? = null,
        val lastModifiedTime: String? = null,
    )
}