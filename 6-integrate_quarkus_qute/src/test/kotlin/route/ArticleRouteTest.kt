package route

import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.po.Article
import org.bson.Document
import org.bson.types.ObjectId
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import security.Role
import java.time.Instant

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleRouteTest: BaseRoute() {

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


    object TestFile {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
        const val USER_ID = "6232fe9f9030836ef30f9238"
        const val PAGE = 1
    }

    lateinit var articleId: String
    lateinit var userId: String


    @BeforeEach
    fun init(){
        articleColl = MongoClients.create(uri).getDatabase(db).getCollection(articleCol)
        userColl = MongoClients.create(uri).getDatabase(db).getCollection(userCol)

        runBlocking {
            val userDoc = Document()
                .append("_id",ObjectId("6232fe9f9030836ef30f9238"))
                .append("username",TestFile.USERNAME)
                .append("password", BcryptUtil.bcryptHash(TestFile.PASSWORD))
                .append("roles", listOf(Role.USER_CONSTANT))
            userId = Uni.createFrom().publisher(userColl.insertOne(userDoc)).awaitSuspending().insertedId!!.asObjectId()!!.value!!.toHexString()

            val articleDoc = Document()
                .append("author", TestFile.USER_ID)
                .append("authorName", TestFile.USERNAME)
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
    @TestSecurity(user = TestFile.USER_ID, roles = [Role.USER_CONSTANT])
    fun `user create article`(){
        RestAssured.given()
            .contentType("application/json; charset=UTF-8")
            .body("""{
                    "title":"test-title",
                    "content":"test-content",
                    "category":"test-category"
                }""".trimIndent())
            .`when`()
            .post("/article")
            .then()
            .statusCode(200)
            .body("title", Matchers.equalTo("test-title"))
            .body("content", Matchers.equalTo("test-content"))
            .body("category", Matchers.equalTo("test-category"))
            .body(Article::author.name, Matchers.equalTo(userId))
            .body(Article::authorName.name, Matchers.equalTo(TestFile.USERNAME))
            .body(Article::createdTime.name, Matchers.notNullValue())
            .body(Article::lastModifiedTime.name, Matchers.notNullValue())
    }

    @Test
    @TestSecurity(user = TestFile.USER_ID, roles = [Role.USER_CONSTANT])
    fun `get article By articleId`(){
        RestAssured.given()
            .contentType("application/json; charset=UTF-8")
            .get("/article?articleId=$articleId")
            .then()
            .statusCode(200)
            .body(Article::author.name, Matchers.equalTo(TestFile.USER_ID))
            .body(Article::authorName.name, Matchers.equalTo(TestFile.USERNAME))
            .body(Article::category.name, Matchers.equalTo("分類一"))
            .body(Article::title.name, Matchers.equalTo("測試標題一"))
            .body(Article::content.name, Matchers.equalTo("測試內容一"))
            .body(Article::lastModifiedTime.name, Matchers.notNullValue())
    }

    @Test
    @TestSecurity(user = TestFile.USER_ID, roles = [Role.USER_CONSTANT])
    fun `update article By articleId`(){
        val TEST_UPDATE_CATEGORY = "更新分類一"
        val TEST_UPDATE_TITLE = "更新測試標題一"
        val TEST_UPDATE_CONTENT = "更新測試內容一"

        RestAssured.given()
            .contentType("application/json; charset=UTF-8")
            .body("""{
                    "title":"$TEST_UPDATE_TITLE",
                    "content":"$TEST_UPDATE_CONTENT",
                    "category":"$TEST_UPDATE_CATEGORY"
                }""".trimIndent())
            .put("/article?articleId=$articleId")
            .then()
            .statusCode(200)
            .body(Article::author.name, Matchers.equalTo(TestFile.USER_ID))
            .body(Article::authorName.name, Matchers.equalTo(TestFile.USERNAME))
            .body(Article::category.name, Matchers.equalTo(TEST_UPDATE_CATEGORY))
            .body(Article::title.name, Matchers.equalTo(TEST_UPDATE_TITLE))
            .body(Article::content.name, Matchers.equalTo(TEST_UPDATE_CONTENT))
            .body(Article::lastModifiedTime.name, Matchers.notNullValue())
    }

    @Test
    @TestSecurity(user = TestFile.USER_ID, roles = [Role.USER_CONSTANT])
    fun `delete article By articleId`(){
        RestAssured.given()
            .contentType("application/json; charset=UTF-8")
            .delete("/article?articleId=$articleId")
            .then()
            .statusCode(200)
            .body(Article::enabled.name, Matchers.equalTo(false))
    }

    @Test
    @TestSecurity(user = TestFile.USER_ID, roles = [Role.USER_CONSTANT])
    fun `get article list by userId`(){
        val response = RestAssured.given()
            .contentType("application/json; charset=UTF-8")
            .get("/articles?page=${TestFile.PAGE}")
            .then()
            .statusCode(200)
            .extract()
            .response()
        response.`as`<List<Map<String, Any>>>(List::class.java).forEach {
            Assertions.assertNotNull(it[Article::id.name])
            Assertions.assertNotNull(it[Article::author.name])
            Assertions.assertNotNull(it[Article::authorName.name])
            Assertions.assertNotNull(it[Article::category.name])
            Assertions.assertNotNull(it[Article::title.name])
            Assertions.assertNotNull(it[Article::content.name])
            Assertions.assertNotNull(it[Article::lastModifiedTime.name])
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