package router

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.*
import repository.UserRepository
import route.BaseRoute
import javax.inject.Inject


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRouteTest: BaseRoute() {

    @Inject
    lateinit var userRepository: UserRepository

    private val TEST_USER = "test-user"

    private val TEST_PASSWORD = "test-password"

    @BeforeAll
    fun init(){

    }

    @Test
//    @TestSecurity(authorizationEnabled = false)
    fun `user sign up`(){
        runBlocking {
            val beforeCount = userRepository.count().awaitSuspending()
            given()
                    .contentType("application/json")
                    .body("""{
                       "username":"$TEST_USER",
                       "password":"$TEST_PASSWORD"
                }""".trimIndent())
                    .`when`()
                    .post("/rest/signUp")
                    .then()
                    .statusCode(200)
                    .body("username", equalTo(TEST_USER))
                    .body("lastModifiedTime", notNullValue())
                    .body("createdTime", notNullValue())
                    .body("id", notNullValue())
                    .body("roles", equalTo(listOf(security.Role.USER.name)))
                    .body("password", nullValue())
            val afterCount = userRepository.count().awaitSuspending()
            Assertions.assertEquals(beforeCount+1, afterCount)
            val user = userRepository.findByUsername(TEST_USER)
            Assertions.assertNotNull(user)
            user?.let { Assertions.assertTrue(user.verifyPassword(TEST_PASSWORD.toCharArray())) }

        }
    }

    @Test
    @TestSecurity(user = "test-user", roles = ["USER", "ADMIN"])
    fun `get user principal name with right security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/me")
                    .then()
                    .statusCode(200)
                    .body(equalTo("user $TEST_USER"))
        }
    }

    @Test
    @TestSecurity(user = "test-user", roles = [])
    fun `get user principal name with wrong security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/me")
                    .then()
                    .statusCode(403)
        }
    }

    @Test
    @TestSecurity(user = "test-user", roles = ["USER", "ADMIN"])
    fun `get admin principal name with right security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/admin")
                    .then()
                    .statusCode(200)
                    .body(equalTo("admin $TEST_USER"))
        }
    }

    @Test
    @TestSecurity(user = "test-user", roles = ["USER"])
    fun `get admin principal name with wrong security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/admin")
                    .then()
                    .statusCode(403)
        }
    }

    @Test
    @TestSecurity(user = "test-user", roles = ["USER"])
    fun `user logout`(){
        GlobalScope.launch {
            given()
                    .cookie("quarkus-credential", "test-credential")
                    .`when`()
                    .get("/rest/me")
                    .then()
                    .statusCode(200)
                    .cookie("quarkus-credential", nullValue())
        }
    }

    @AfterAll
    fun clean(){
        GlobalScope.launch {
            userRepository.mongoCollection().drop().awaitSuspending()
        }
    }

}