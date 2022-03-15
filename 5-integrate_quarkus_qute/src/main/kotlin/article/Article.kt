package article


import io.quarkus.mongodb.FindOptions
import io.quarkus.mongodb.panache.MongoEntity
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.reactive.ReactiveMongoCollection
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.ExperimentalCoroutinesApi
import model.po.User
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.time.Instant
import org.jboss.logging.Logger
import repository.BaseMongoRepository
import javax.inject.Inject
import javax.inject.Singleton

@MongoEntity(collection = "Article")
class Article(
    var _id: ObjectId? = null,
    var author: String? = null,
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var enabled: Boolean? = true,
    var createdTime: Instant? = Instant.now(),
    var lastModifiedTime: Instant? = null
)

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {}

@ExperimentalCoroutinesApi
abstract class ArticleMongoRepository<Entity : Any>: ReactivePanacheMongoRepository<Entity> {

    @Inject
    lateinit var logger: Logger

    private val col: ReactiveMongoCollection<Entity> = this.mongoCollection()

    suspend fun findAsList(filter: Bson? = null): List<Entity> =
        (filter?.let { col.find(it) } ?: col.find()).collect().asList().awaitSuspending()

    suspend fun findOne(filter: Bson): Entity? =
        col.find(filter, FindOptions().limit(1)).collect().asList().awaitSuspending().firstOrNull()

}