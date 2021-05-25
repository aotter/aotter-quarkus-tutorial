package model.po

import io.quarkus.test.junit.QuarkusTest
import org.bson.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import repository.ClamDataRepository
import java.time.Instant
import javax.inject.Inject

@QuarkusTest
class ClamDataTest {

    private val COLLECTION = "TEST_COLLECTION"

    private val AUTHOR = "TEST_AUTHOR"


    @Test
    fun `convert document to clam data`(){
        val nested = Document("layer_1", Document("layer_2", 2))
        val doc = Document(ClamData::collectionName.name, COLLECTION)
                .append(ClamData::authorId.name, AUTHOR)
                .append(ClamData::createTime.name, Instant.now())
                .append(ClamData::lastModifiedTime.name, Instant.now())
                .append(ClamData::publishedTime.name, null)
                .append(ClamData::state.name, State.TEMP.name)
                .append("test_field", "test")
                .append("test_nested_field", nested)
        val clam = ClamData.documentToClamData(doc)
        Assertions.assertEquals(COLLECTION, clam.collectionName)
        Assertions.assertEquals(AUTHOR, clam.authorId)
        Assertions.assertNotNull(clam.createTime)
        Assertions.assertNotNull(clam.lastModifiedTime)
        Assertions.assertNull(clam.publishedTime)
        Assertions.assertEquals(State.TEMP, clam.state)
        Assertions.assertEquals("test", clam.get("test_field"))
        Assertions.assertEquals(nested, clam.get("test_nested_field"))
    }

}