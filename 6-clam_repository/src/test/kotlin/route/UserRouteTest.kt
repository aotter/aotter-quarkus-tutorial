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
import security.Role
import javax.inject.Inject


@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRouteTest: BaseRoute() {

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Test
    fun `user sign up`(){
        runBlocking {
            val beforeCount = userRepository.count().awaitSuspending()
            given()
                    .contentType("application/json")
                    .body("""{
                       "username":"${TestRole.USERNAME}",
                       "password":"${TestRole.PASSWORD}"
                }""".trimIndent())
                    .`when`()
                    .post("/rest/signUp")
                    .then()
                    .statusCode(200)
                    .body("username", equalTo(TestRole.USERNAME))
                    .body("lastModifiedTime", notNullValue())
                    .body("createdTime", notNullValue())
                    .body("id", notNullValue())
                    .body("roles", equalTo(listOf(Role.USER.name)))
                    .body("password", nullValue())
            val afterCount = userRepository.count().awaitSuspending()
            Assertions.assertEquals(beforeCount+1, afterCount)
            val user = userRepository.findByUsername(TestRole.USERNAME)
            Assertions.assertNotNull(user)
            user?.let { Assertions.assertTrue(user.verifyPassword(TestRole.PASSWORD.toCharArray())) }

        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `get user principal name with right security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/me")
                    .then()
                    .statusCode(200)
                    .body(equalTo("user ${TestRole.USERNAME}"))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [])
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
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    fun `get admin principal name with right security identity`(){
        runBlocking {
            given()
                    .contentType("application/json")
                    .`when`()
                    .get("/rest/admin")
                    .then()
                    .statusCode(200)
                    .body(equalTo("admin ${TestRole.USERNAME}"))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
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
    fun `user logout`(){
        runBlocking {
            given()
                    .cookie("quarkus-credential", "test-credential")
                    .`when`()
                    .get("/rest/logout")
                    .then()
                    .statusCode(200)
                    .cookie("quarkus-credential", "")
        }
    }

    @AfterAll
    fun clean(){
        GlobalScope.launch {
            userRepository.mongoCollection().drop().awaitSuspending()
        }
    }

}