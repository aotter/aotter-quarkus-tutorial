package router

import setup.ClamSetup
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.security.TestSecurity
import io.restassured.RestAssured.given
import model.po.ClamData
import model.po.State
import org.bson.Document
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import security.Role

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClamRouteTest: ClamSetup() {

    object TestRole {
        const val USERNAME = "test-user"
        const val PASSWORD = "test-password"
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.ADMIN_CONSTANT])
    fun `admin create entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .body("""{
                    "title":"test-title",
                    "subtitle":"test-subtitle",
                    "content":[
                        {"title":"title1", "article":"article1"},
                        {"title":"title2", "article":"article2"},
                        {"title":"title3", "article":"article3"}
                        ]
                }""".trimIndent())
                .`when`()
                .post("/api/$TEST_COLLECTION")
                .then()
                .statusCode(200)
                .body("title", equalTo("test-title"))
                .body("subtitle", equalTo("test-subtitle"))
                .body("content", equalTo(listOf(
                        Document("title", "title1").append("article", "article1"),
                        Document("title", "title2").append("article", "article2"),
                        Document("title", "title3").append("article", "article3"),
                )))
                .body(ClamData::collectionName.name, equalTo(TEST_COLLECTION))
                .body(ClamData::authorId.name, equalTo(TestRole.USERNAME))
                .body(ClamData::createTime.name, notNullValue())
                .body(ClamData::state.name, equalTo(State.TEMP.name))
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
    fun `user create entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .body("""{
                    "test": "just failed"
                 }""".trimIndent())
                .post("/api/$TEST_COLLECTION")
                .then()
                .statusCode(403)
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT, Role.ADMIN_CONSTANT])
    fun `get published entity by id`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .get("/api/$TEST_COLLECTION/$publishedId")
                .then()
                .statusCode(200)
                .body(ClamData::state.name, equalTo(State.PUBLISHED.name))
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT, Role.ADMIN_CONSTANT])
    fun `get published entities in collection`(){
        val response = given()
                .contentType("application/json; charset=UTF-8")
                .get("/api/$TEST_COLLECTION")
                .then()
                .statusCode(200)
                .extract()
                .response()
        response.`as`<List<Map<String, Any>>>(List::class.java).forEach {
            Assertions.assertEquals(State.PUBLISHED.name, it.get(ClamData::state.name))
            Assertions.assertNotNull(it.get(ClamData::publishedTime.name))
        }
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT, Role.ADMIN_CONSTANT])
    fun `admin update entity by id`(){
        val TEST_UPDATE_FIELD = "updateField"
        val TEST_UPDATE_VALUE = "test_update"
        given()
                .contentType("application/json; charset=UTF-8")
                .body("""{
                    "$TEST_UPDATE_FIELD":"$TEST_UPDATE_VALUE"   
                 }""".trimIndent())
                .put("/api/$TEST_COLLECTION/$publishedId")
                .then()
                .statusCode(200)
                .body(TEST_UPDATE_FIELD, equalTo(TEST_UPDATE_VALUE))
                .body(ClamData::lastModifiedTime.name, notNullValue())
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
    fun `user update entity by id`(){
        val TEST_UPDATE_FIELD = "updateField"
        val TEST_UPDATE_VALUE = "test_update"
        given()
                .contentType("application/json; charset=UTF-8")
                .body("""{
                    "$TEST_UPDATE_FIELD":"$TEST_UPDATE_VALUE"   
                 }""".trimIndent())
                .put("/api/$TEST_COLLECTION/$publishedId")
                .then()
                .statusCode(403)
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT, Role.ADMIN_CONSTANT])
    fun `admin publish entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .put("/api/$TEST_COLLECTION/$unpublishedId/state/${State.PUBLISHED.name}")
                .then()
                .statusCode(200)
                .body(ClamData::state.name, equalTo(State.PUBLISHED.name))
                .body(ClamData::publishedTime.name, notNullValue())
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
    fun `user publish entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .put("/api/$TEST_COLLECTION/$unpublishedId/state/${State.PUBLISHED.name}")
                .then()
                .statusCode(403)
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT, Role.ADMIN_CONSTANT])
    fun `admin archive entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .delete("/api/$TEST_COLLECTION/$unpublishedId")
                .then()
                .statusCode(200)
                .body(ClamData::state.name, equalTo(State.ARCHIVED.name))
    }

    @Test
    @TestSecurity(user = TestRole.USERNAME, roles = [Role.USER_CONSTANT])
    fun `user archive entity`(){
        given()
                .contentType("application/json; charset=UTF-8")
                .delete("/api/$TEST_COLLECTION/$unpublishedId")
                .then()
                .statusCode(403)
    }
}