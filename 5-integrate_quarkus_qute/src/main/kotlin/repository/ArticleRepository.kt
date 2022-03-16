package repository

import com.mongodb.WriteConcern
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import com.mongodb.reactivestreams.client.MongoDatabase
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.po.Article
import org.bson.Document
import org.bson.conversions.Bson
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.reactivestreams.Publisher
import java.text.SimpleDateFormat
import java.util.*
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {

    private lateinit var database: MongoDatabase

    @ConfigProperty(name = "quarkus.mongodb.database")
    private lateinit var dbName: String

    @ConfigProperty(name = "quarkus.mongodb.connection-string")
    private lateinit var uri: String

    private val PAGE_ENTITY_NUM = 6

    @PostConstruct
    fun init(){
        database = MongoClients.create(uri).getDatabase(dbName)
    }

    private fun getCol(): MongoCollection<Document> =
        database.getCollection("Article").withWriteConcern(WriteConcern.ACKNOWLEDGED)

    suspend fun findArticleListViaPage(filters: MutableList<Bson>, page: Int): List<ArticleView>{
        val result = getCol().find(Filters.and(filters))
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

    private suspend fun <T> multiAwait(publisher: Publisher<T>): List<T>{
        return Multi.createFrom().publisher(publisher).collect().asList().awaitSuspending()
    }

    data class ArticleView(
        val id: String? = null,
        val category: String? = null,
        val title: String? = null,
        val content: String? = null,
        val author: String? = null,
        val lastModifiedTime: String? = null,
    )
}