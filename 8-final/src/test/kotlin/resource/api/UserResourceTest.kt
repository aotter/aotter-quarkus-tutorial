package resource.api

import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.*
import repository.UserRepository
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserResourceTest {

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
    }

    @Inject
    lateinit var userRepository: UserRepository

    @Test
    @Order(1)
    fun `user sign up`(){
        runBlocking {
            val beforeCount = userRepository.count().awaitSuspending()
            RestAssured.given()
                .contentType("application/json")
                .body("""{
                       "username":"${TestRole.USERNAME}",
                       "password":"${TestRole.PASSWORD}"
                }""".trimIndent())
                .`when`()
                .post("/api/user/signUp")
                .then()
                .statusCode(200)
                .body("username", CoreMatchers.equalTo(TestRole.USERNAME))
                .body("lastModifiedTime", CoreMatchers.notNullValue())
                .body("createdTime", CoreMatchers.notNullValue())
                .body("id", CoreMatchers.notNullValue())
                .body("role", CoreMatchers.equalTo(Role.USER.name))
                .body("password", CoreMatchers.nullValue())
            val afterCount = userRepository.count().awaitSuspending()
            Assertions.assertEquals(beforeCount+1, afterCount)
            val user = userRepository.findByUsername(TestRole.USERNAME)
            Assertions.assertNotNull(user)
            user?.let { Assertions.assertTrue(user.verifyPassword(TestRole.PASSWORD.toCharArray())) }
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    @Order(2)
    fun `get user principal name with right security identity`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .get("/api/user/me")
                .then()
                .statusCode(200)
                .body("username", CoreMatchers.equalTo(TestRole.USERNAME))
                .body("role", CoreMatchers.equalTo(Role.USER_CONSTANT))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [])
    @Order(3)
    fun `get user principal name with wrong security identity`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .get("/api/user/me")
                .then()
                .statusCode(403)
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT, Role.USER_CONSTANT])
    @Order(4)
    fun `get admin principal name with right security identity`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .get("/api/user/admin")
                .then()
                .statusCode(200)
                .body(CoreMatchers.equalTo("admin ${TestRole.USERNAME}"))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
    @Order(5)
    fun `get admin principal name with wrong security identity`(){
        runBlocking {
            RestAssured.given()
                .contentType("application/json")
                .`when`()
                .get("/api/user/admin")
                .then()
                .statusCode(403)
        }
    }

    @Test
    @Order(6)
    fun `user logout`(){
        runBlocking {
            RestAssured.given()
                .cookie("quarkus-credential", "test-credential")
                .`when`()
                .get("/api/user/logout")
                .then()
                .statusCode(200)
                .cookie("quarkus-credential", "")
        }
    }

    @AfterAll
    fun clean(){
        runBlocking {
            userRepository.mongoCollection().drop().awaitSuspending()
        }
    }
}