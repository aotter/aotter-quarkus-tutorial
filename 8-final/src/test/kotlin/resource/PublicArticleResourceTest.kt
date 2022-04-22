package resource

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.dto.ArticleRequest
import model.po.Article
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import repository.ArticleRepository
import repository.UserRepository
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublicArticleResourceTest {

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
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
            articleRepository.updatePublishStatus(articleId,true)
        }
    }

    @Test
    fun `get article page by articleId`(){
        runBlocking {
            RestAssured.given()
                .contentType("text/html")
                .`when`()
                .get("/article-content?articleId=$articleId")
                .then()
                .statusCode(200)
        }
    }


    @Test
    fun `get article list page`(){
        runBlocking {
            RestAssured.given()
                .contentType("text/html")
                .`when`()
                .get("/article-list?articleId=$articleId")
                .then()
                .statusCode(200)
        }
    }

    @AfterAll
    fun clean(){
        GlobalScope.launch {
            articleRepository.mongoCollection().drop().awaitSuspending()
            userRepository.mongoCollection().drop().awaitSuspending()
        }
    }
}