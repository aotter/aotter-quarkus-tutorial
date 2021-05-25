package repository

import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.runBlocking
import model.po.ClamData
import model.po.State
import org.bson.Document
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.*
import java.time.Instant
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClamDataRepositoryTest {

    @Inject
    lateinit var clamDataRepo: ClamDataRepository

    private val TEST_COLLECTION = "test_collection"

    private val TEST_AUTHOR = "test_author"

    private lateinit var col: MongoCollection<Document>

    private lateinit var publishedEntity: ClamData

    private lateinit var unpublishedEntity: ClamData

    private lateinit var publishedId: String

    private lateinit var unpublishedId: String

    @ConfigProperty(name = "%test.quarkus.mongodb.database")
    private lateinit var db: String

    @ConfigProperty(name = "%test.quarkus.mongodb.connection-string")
    private lateinit var uri: String

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

    @Test
    fun `create entity`(){
        val testData = ClamData()
        val TEST_CATEGORY = "test_category"
        val TEST_TITLE = "test_title"
        val TEST_CONTENT = "test_content"
        testData.append("category", TEST_CATEGORY)
        testData.append("title", TEST_TITLE)
        testData.append("content", TEST_CONTENT)
        runBlocking {
            val beforeCount = Uni.createFrom().publisher(col.countDocuments()).awaitSuspending()
            val insertedClam = clamDataRepo.create(TEST_COLLECTION, testData, TEST_AUTHOR)
            val afterCount = Uni.createFrom().publisher(col.countDocuments()).awaitSuspending()
            Assertions.assertEquals(afterCount, beforeCount+1)

            val clamData = insertedClam?.id?.toHexString()?.let { clamDataRepo.getEntityById(TEST_COLLECTION, it) }
            Assertions.assertEquals(TEST_CATEGORY, clamData?.getString("category"), "entity category")
            Assertions.assertEquals(TEST_TITLE, clamData?.getString("title"), "entity title")
            Assertions.assertEquals(TEST_CONTENT, clamData?.getString("content"), "entity content")
            `check entity initial attributes`(clamData, TEST_AUTHOR, TEST_COLLECTION)
        }
    }

    fun `check entity initial attributes`(clamData: ClamData?, author: String, collection: String){
        Assertions.assertEquals(author, clamData?.authorId, "created entity authorId")
        Assertions.assertEquals(State.TEMP, clamData?.state, "created entity state")
        Assertions.assertEquals(collection, clamData?.collectionName, "created entity collectionName")
        Assertions.assertNotNull(clamData?.createTime, "created entity createTime")
    }

    @Test
    fun `get all published entities in collection`(){
        runBlocking {
            clamDataRepo.getPublishedEntities(TEST_COLLECTION).forEach {
                Assertions.assertEquals(State.PUBLISHED.name, it.getString(ClamData::state.name))
                Assertions.assertNotNull(it.publishedTime)
            }
        }
    }

    @Test
    fun `get published entity by id`(){
        runBlocking {
            val publishedEntity = clamDataRepo.getPublishedEntityById(TEST_COLLECTION, publishedId)
            Assertions.assertNotNull(publishedEntity, "get published entity by id")
            Assertions.assertEquals(State.PUBLISHED, publishedEntity?.state, "published entity state")

            val unpublishedEntity = clamDataRepo.getPublishedEntityById(TEST_COLLECTION, unpublishedId)
            Assertions.assertNull(unpublishedEntity, "get unpublished entity by id")
        }
    }

    @Test
    fun `update entity by id`(){
        runBlocking {
            val TEST_UPDATE_FIELD = "updateField"
            val TEST_UPDATE_VALUE = "test_update"
            publishedEntity.append(TEST_UPDATE_FIELD, TEST_UPDATE_VALUE)
            clamDataRepo.updateEntity(TEST_COLLECTION,  publishedId, publishedEntity)
            val updatedEntity = clamDataRepo.getEntityById(TEST_COLLECTION, publishedId)
            Assertions.assertEquals(TEST_UPDATE_VALUE, updatedEntity?.getString(TEST_UPDATE_FIELD))
            Assertions.assertNotNull(updatedEntity?.lastModifiedTime)
        }
    }

    @Test
    fun `publish entity by id`(){
        runBlocking {
            val entity = clamDataRepo.getEntityById(TEST_COLLECTION, unpublishedId)
            clamDataRepo.updateEntityState(TEST_COLLECTION, unpublishedId, State.PUBLISHED)
            val publishedEntity = clamDataRepo.getEntityById(TEST_COLLECTION, unpublishedId)
            Assertions.assertEquals(State.PUBLISHED.name, publishedEntity?.getString(ClamData::state.name))
            val publishedTime = publishedEntity?.get(ClamData::publishedTime.name) as Instant?
            Assertions.assertNotNull(publishedTime)
            Assertions.assertNotEquals(entity?.lastModifiedTime, publishedEntity?.lastModifiedTime)
            `check republish entity won't change initial publishedTime`(publishedTime)
        }
    }

    fun `check republish entity won't change initial publishedTime`(publishedTime: Instant?){
        runBlocking {
            val republishedEntity = clamDataRepo.updateEntityState(TEST_COLLECTION, unpublishedId, State.PUBLISHED)
            Assertions.assertEquals(publishedTime, republishedEntity?.publishedTime)
        }
    }

    @Test
    fun `archive entity by id`(){
        runBlocking {
            val entity = clamDataRepo.getEntityById(TEST_COLLECTION, publishedId)
            clamDataRepo.updateEntityState(TEST_COLLECTION, publishedId, State.ARCHIVED)
            val archivedEntity = clamDataRepo.getEntityById(TEST_COLLECTION, publishedId)
            Assertions.assertEquals(State.ARCHIVED, archivedEntity?.state)
            Assertions.assertNotEquals(entity?.lastModifiedTime, archivedEntity?.lastModifiedTime)
        }
    }


    @AfterEach
    fun clean(){
        runBlocking {
            Uni.createFrom().publisher(col.drop()).awaitSuspending()
        }
    }


}