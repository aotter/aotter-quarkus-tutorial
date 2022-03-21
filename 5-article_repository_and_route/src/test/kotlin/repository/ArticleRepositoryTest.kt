package repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.po.Article
import model.po.User
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.*
import java.time.Instant
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleRepositoryTest {

    @Inject
    lateinit var articleRepo: ArticleRepository

    private lateinit var articleColl: MongoCollection<Document>
    private lateinit var userColl: MongoCollection<Document>

    @ConfigProperty(name = "%test.quarkus.mongodb.database")
    private lateinit var db: String

    @ConfigProperty(name = "%test.quarkus.mongodb.connection-string")
    private lateinit var uri: String

    @ConfigProperty(name = "%test.quarkus.mongodb.col.article")
    private lateinit var articleCol: String

    @ConfigProperty(name = "%test.quarkus.mongodb.col.user")
    private lateinit var userCol: String

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
        const val Role = "USER"
    }

    lateinit var articleId: String
    lateinit var userId: String


    @BeforeEach
    fun init(){
        val pojoCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        val settings: MongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(uri))
            .codecRegistry(pojoCodecRegistry)
            .build()

        articleColl = MongoClients.create(settings).getDatabase(db).getCollection(articleCol)
        userColl = MongoClients.create(settings).getDatabase(db).getCollection(userCol)

        runBlocking {
            val userDoc = Document().append("username",TestRole.USERNAME)
                .append("password",BcryptUtil.bcryptHash(TestRole.PASSWORD))
                .append("roles", listOf(TestRole.Role))
            userId = Uni.createFrom().publisher(userColl.insertOne(userDoc)).awaitSuspending().insertedId!!.asObjectId()!!.value!!.toHexString()

            val articleDoc = Document().append("author", userId)
                .append("authorName", TestRole.USERNAME)
                .append("category", "分類一")
                .append("title", "測試標題一")
                .append("content", "測試內容一")
                .append("enabled", true)
                .append("createdTime", Instant.now())
                .append("lastModifiedTime", Instant.now())
            articleId = Uni.createFrom().publisher(articleColl.insertOne(articleDoc)).awaitSuspending().insertedId!!.asObjectId()!!.value!!.toHexString()
        }
    }

    @Test
    fun `create article`(){
        val testData = Article()
        val TEST_CATEGORY = "分類二"
        val TEST_TITLE = "測試標題二"
        val TEST_CONTENT = "測試內容二"
        testData.append("category", TEST_CATEGORY)
        testData.append("title", TEST_TITLE)
        testData.append("content", TEST_CONTENT)

        runBlocking {
            val beforeCount = Uni.createFrom().publisher(articleColl.countDocuments()).awaitSuspending()
            val insertedArticle = articleRepo.createArticle(testData, userId)
            val afterCount = Uni.createFrom().publisher(articleColl.countDocuments()).awaitSuspending()
            Assertions.assertEquals(afterCount, beforeCount+1)
            val article = insertedArticle?.id?.toHexString()?.let { articleRepo.getArticleById(it) }

            Assertions.assertEquals(TEST_CATEGORY, article?.getString("category"), "分類一")
            Assertions.assertEquals(TEST_TITLE, article?.getString("title"), "測試標題一")
            Assertions.assertEquals(TEST_CONTENT, article?.getString("content"), "測試內容一")
            Assertions.assertEquals(userId, article?.getString("author"), userId)
            Assertions.assertEquals(TestRole.USERNAME, article?.getString("authorName"), TestRole.USERNAME)
        }
    }

    @Test
    fun `update article by id`(){
        val testData = Article()
        val TEST_UPDATE_CATEGORY = "更新分類一"
        val TEST_UPDATE_TITLE = "更新測試標題一"
        val TEST_UPDATE_CONTENT = "更新測試內容一"
        testData.append("category", TEST_UPDATE_CATEGORY)
        testData.append("title", TEST_UPDATE_TITLE)
        testData.append("content", TEST_UPDATE_CONTENT)
        runBlocking {
            articleRepo.updateArticle(articleId, testData)
            val updatedResult = articleRepo.getArticleById(articleId)
            Assertions.assertEquals(TEST_UPDATE_CATEGORY, updatedResult?.getString("category"), "更新分類一")
            Assertions.assertEquals(TEST_UPDATE_TITLE, updatedResult?.getString("title"), "更新測試標題一")
            Assertions.assertEquals(TEST_UPDATE_CONTENT, updatedResult?.getString("content"), "更新測試內容一")
            Assertions.assertNotNull(updatedResult?.lastModifiedTime)
        }
    }

    @Test
    fun `get user article list`(){
        runBlocking {
            val PAGE = 1
            val list = articleRepo.getArticleListByUser(userId, PAGE)
            Assertions.assertEquals(1, list.size)
        }
    }

    @Test
    fun `get article by id`(){
        runBlocking {
            val result = articleRepo.getArticleById(articleId)
            Assertions.assertNotNull(result, "get published entity by id")
        }
    }

    @Test
    fun `delete article by id`(){
        runBlocking {
            articleRepo.deleteArticle(articleId)
            val PAGE = 1
            val deleteResult = articleRepo.getArticleListByUser(userId, PAGE)
            Assertions.assertEquals(0, deleteResult.size)
        }
    }

    @AfterEach
    fun clean(){
        runBlocking {
            Uni.createFrom().publisher(articleColl.drop()).awaitSuspending()
            Uni.createFrom().publisher(userColl.drop()).awaitSuspending()
        }
    }


}