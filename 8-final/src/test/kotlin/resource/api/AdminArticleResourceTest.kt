package resource.api

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.dto.ArticleRequest
import model.po.Article
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.*
import repository.ArticleRepository
import repository.UserRepository
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminArticleResourceTest {

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
    }

    object TestArticle {
        const val CATEGORY = "test-category"
        const val TITLE = "test-title"
        const val CONTENT = "test-content"
    }

    @Inject
    lateinit var articleRepository: ArticleRepository

    @Inject
    lateinit var userRepository: UserRepository

    lateinit var userId: ObjectId
    lateinit var articleId: ObjectId

    @BeforeEach
    fun init(){
        runBlocking {
            val user = userRepository.create(
                TestRole.USERNAME, BcryptUtil.bcryptHash(
                    TestRole.PASSWORD), Role.USER)
            userId = user.id!!

            val articleRequest = ArticleRequest(
                category = "分類一",
                title = "測試標題一",
                content = "測試內容一"
            )
            val article = articleRepository.save(Article(user, articleRequest))
            articleId = article.id!!
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `create article`(){
        val req = ArticleRequest(
            category = TestArticle.CATEGORY,
            title = TestArticle.TITLE,
            content = TestArticle.CONTENT
        )
        runBlocking {
            val beforeCount = articleRepository.count().awaitSuspending()
            RestAssured.given()
                .contentType("application/json")
                .body(req)
                .`when`()
                .post("/api/admin/article")
                .then()
                .statusCode(200)
                .body("lastModifiedTime", CoreMatchers.notNullValue())
                .body("createdTime", CoreMatchers.notNullValue())
                .body("id", CoreMatchers.notNullValue())
                .body("author", CoreMatchers.equalTo(userId.toHexString()))
                .body("authorName", CoreMatchers.equalTo(TestRole.USERNAME))
                .body("category", CoreMatchers.equalTo(TestArticle.CATEGORY))
                .body("title", CoreMatchers.equalTo(TestArticle.TITLE))
                .body("content", CoreMatchers.equalTo(TestArticle.CONTENT))
                .body("published", CoreMatchers.equalTo(false))
                .body("visible", CoreMatchers.equalTo(true))
            val afterCount = articleRepository.count().awaitSuspending()
            Assertions.assertEquals(beforeCount+1, afterCount)
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `get article by articleId`(){
        runBlocking {
            RestAssured.given()
                .`when`()
                .get("/api/admin/article?articleId=${articleId.toHexString()}")
                .then()
                .statusCode(200)
                .body("lastModifiedTime", CoreMatchers.notNullValue())
                .body("createdTime", CoreMatchers.notNullValue())
                .body("id", CoreMatchers.notNullValue())
                .body("author", CoreMatchers.equalTo(userId.toHexString()))
                .body("authorName", CoreMatchers.equalTo(TestRole.USERNAME))
                .body("category", CoreMatchers.equalTo("分類一"))
                .body("title", CoreMatchers.equalTo("測試標題一"))
                .body("content", CoreMatchers.equalTo("測試內容一"))
                .body("published", CoreMatchers.equalTo(false))
                .body("visible", CoreMatchers.equalTo(true))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `update article by articleId`(){
        val req = ArticleRequest(
            category = "update_category",
            title = "update_title",
            content = "update_content"
        )
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .body(req)
                .`when`()
                .put("/api/admin/article?articleId=${articleId.toHexString()}")
                .then()
                .statusCode(200)
                .body("lastModifiedTime", CoreMatchers.notNullValue())
                .body("createdTime", CoreMatchers.notNullValue())
                .body("id", CoreMatchers.notNullValue())
                .body("author", CoreMatchers.equalTo(userId.toHexString()))
                .body("authorName", CoreMatchers.equalTo(TestRole.USERNAME))
                .body("category", CoreMatchers.equalTo("update_category"))
                .body("title", CoreMatchers.equalTo("update_title"))
                .body("content", CoreMatchers.equalTo("update_content"))
                .body("published", CoreMatchers.equalTo(false))
                .body("visible", CoreMatchers.equalTo(true))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `update article publish status by articleId`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .put("/api/admin/update-publish-status?articleId=${articleId.toHexString()}&published=true")
                .then()
                .statusCode(200)
                .body("published", CoreMatchers.equalTo(true))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `delete article by articleId`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .delete("/api/admin/article?articleId=${articleId.toHexString()}")
                .then()
                .statusCode(200)
                .body("visible", CoreMatchers.equalTo(false))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `get article list by userId`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .get("/api/admin/articles")
                .then()
                .statusCode(200)
                .body("totalPages", CoreMatchers.equalTo(1))
        }
    }

    @AfterEach
    fun clean(){
        runBlocking {
            articleRepository.mongoCollection().drop().awaitSuspending()
            userRepository.mongoCollection().drop().awaitSuspending()
        }
    }
}