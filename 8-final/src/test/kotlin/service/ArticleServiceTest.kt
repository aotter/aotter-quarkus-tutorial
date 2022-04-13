package service

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.dto.ArticleRequest
import model.po.Article
import org.bson.types.ObjectId
import org.junit.jupiter.api.*
import repository.ArticleRepository
import repository.UserRepository
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleServiceTest {

    @Inject
    lateinit var articleService: ArticleService

    @Inject
    lateinit var articleRepo: ArticleRepository

    @Inject
    lateinit var userRepo: UserRepository

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
    }

    lateinit var articleId: ObjectId
    lateinit var userId: ObjectId

    @BeforeEach
    fun init(){
        runBlocking {
            val user = userRepo.create(
                TestRole.USERNAME, BcryptUtil.bcryptHash(
                    TestRole.PASSWORD), Role.USER)
            userId = user.id!!

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
    fun `get article list response`(){
        runBlocking {
            val PAGE = 1
            val SHOW = 10
            val response = articleService.findAsListResponse(userId, null, true, PAGE, SHOW)
            Assertions.assertEquals(1, response.totalPages)
            Assertions.assertEquals(1, response.list.size)
        }
    }

    @AfterEach
    fun clean(){
        runBlocking {
            articleRepo.mongoCollection().drop().awaitSuspending()
            userRepo.mongoCollection().drop().awaitSuspending()
        }
    }
}
