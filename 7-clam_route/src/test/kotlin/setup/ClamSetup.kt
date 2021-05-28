package setup

import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.po.ClamData
import model.po.State
import org.bson.Document
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class ClamSetup {

    @ConfigProperty(name = "%test.quarkus.mongodb.database")
    private lateinit var db: String

    @ConfigProperty(name = "%test.quarkus.mongodb.connection-string")
    private lateinit var uri: String

    lateinit var col: MongoCollection<Document>

    lateinit var publishedEntity: ClamData

    lateinit var unpublishedEntity: ClamData

    lateinit var publishedId: String

    lateinit var unpublishedId: String

    val TEST_COLLECTION = "test_collection"

    val TEST_AUTHOR = "test_author"

    @BeforeEach
    fun init(){
        col = MongoClients.create(uri).getDatabase(db).getCollection(TEST_COLLECTION)

        publishedEntity = ClamData()
        with(publishedEntity) {
            collectionName = TEST_COLLECTION
            state = State.PUBLISHED
            publishedTime = Instant.now()
        }

        unpublishedEntity = ClamData()
        with(unpublishedEntity) {
            collectionName = TEST_COLLECTION
            state = State.TEMP
        }

        runBlocking {
            publishedId = Uni.createFrom().publisher(col.insertOne(publishedEntity)).awaitSuspending().insertedId!!.asObjectId()!!.value!!.toHexString()
            unpublishedId = Uni.createFrom().publisher(col.insertOne(unpublishedEntity)).awaitSuspending().insertedId!!.asObjectId()!!.value!!.toHexString()
        }
    }

    @AfterEach
    fun clean(){
        runBlocking {
            Uni.createFrom().publisher(col.drop()).awaitSuspending()
        }
    }


}