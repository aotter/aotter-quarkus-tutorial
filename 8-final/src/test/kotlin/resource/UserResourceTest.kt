package resource

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserResourceTest {

    @Test
    fun `get login page`(){
        runBlocking {
            RestAssured.given()
                .contentType("text/html")
                .`when`()
                .get("/login")
                .then()
                .statusCode(200)
        }
    }

    @Test
    fun `get signup page`(){
        runBlocking {
            RestAssured.given()
                .contentType("text/html")
                .`when`()
                .get("/signup")
                .then()
                .statusCode(200)
        }
    }
}