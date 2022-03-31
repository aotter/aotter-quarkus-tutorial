package repository

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.dto.ArticleRequest
import model.po.Article
import org.bson.Document
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.*
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleRepositoryTest {

    @Inject
    lateinit var articleRepo: ArticleRepository

    @Inject
    lateinit var userRepo: UserRepository

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
    }

    lateinit var articleId: ObjectId

    @BeforeEach
    fun init(){
        articleColl = MongoClients.create(uri).getDatabase(db).getCollection(articleCol)
        userColl = MongoClients.create(uri).getDatabase(db).getCollection(userCol)

        runBlocking {
            val user = userRepo.create(TestRole.USERNAME, BcryptUtil.bcryptHash(TestRole.PASSWORD), Role.USER)

            val articleRequest = ArticleRequest(
                category = "分類一",
                title = "測試標題一",
                content = "測試內容一"
            )
            val article = articleRepo.save(Article(user, articleRequest))
            articleId = article.id!!
            articleRepo.updatePublishStatus(articleId,true)
        }
    }

    @Test
    fun `create article`(){
        val TEST_CATEGORY = "分類二"
        val TEST_TITLE = "測試標題二"
        val TEST_CONTENT = "測試內容二"
        val testData = ArticleRequest(
            category = TEST_CATEGORY,
            title = TEST_TITLE,
            content = TEST_CONTENT)

        runBlocking {
            val user = userRepo.findByUsername(TestRole.USERNAME)!!

            val beforeCount = articleRepo.count(Filters.exists(Article::authorName.name))
            val insertedArticle = articleRepo.save(Article(user, testData))
            val afterCount = articleRepo.count(Filters.exists(Article::authorName.name))
            Assertions.assertEquals(afterCount, beforeCount+1)
            Assertions.assertEquals(TEST_CATEGORY, insertedArticle.category, "分類一")
            Assertions.assertEquals(TEST_TITLE, insertedArticle.title, "測試標題一")
            Assertions.assertEquals(TEST_CONTENT, insertedArticle.content, "測試內容一")
        }
    }

    @Test
    fun `update article by id`(){
        val TEST_UPDATE_CATEGORY = "更新分類一"
        val TEST_UPDATE_TITLE = "更新測試標題一"
        val TEST_UPDATE_CONTENT = "更新測試內容一"
        val testData = ArticleRequest(
            category = TEST_UPDATE_CATEGORY,
            title = TEST_UPDATE_TITLE,
            content = TEST_UPDATE_CONTENT)

        runBlocking {
            val updatedResult = articleRepo.update(articleId ,testData)!!
            Assertions.assertEquals(TEST_UPDATE_CATEGORY, updatedResult.category, "更新分類一")
            Assertions.assertEquals(TEST_UPDATE_TITLE, updatedResult.title, "更新測試標題一")
            Assertions.assertEquals(TEST_UPDATE_CONTENT, updatedResult.content, "更新測試內容一")
            Assertions.assertNotNull(updatedResult.lastModifiedTime)
        }
    }

    @Test
    fun `update article publish status`(){
        runBlocking {
            articleRepo.updatePublishStatus(articleId, false)
            val PAGE = 1
            val SHOW = 10
            val list = articleRepo.list(null, null, true, PAGE, SHOW)
            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `delete article by id`(){
        runBlocking {
            val deleteResult = articleRepo.delete(articleId)
            Assertions.assertEquals(false, deleteResult?.visible, "false")
        }
    }

    @Test
    fun `get user article list`(){
        runBlocking {
            val PAGE = 1
            val SHOW = 10
            val list = articleRepo.list(null, null, true, PAGE, SHOW)
            Assertions.assertEquals(1, list.size)
        }
    }

    @Test
    fun `get article by id`(){
        runBlocking {
            val result = articleRepo.findOne(Filters.eq("_id", articleId))
            Assertions.assertNotNull(result, "get published entity by id")
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