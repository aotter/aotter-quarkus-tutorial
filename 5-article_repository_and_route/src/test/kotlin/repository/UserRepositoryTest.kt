package repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoCollection
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.mongodb.FindOptions
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.coroutines.awaitSuspending
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import model.po.User
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.junit.jupiter.api.*
import security.Role
import javax.inject.Inject

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserRepositoryTest {

    @Inject
    lateinit var userRepo: UserRepository

    private val totalCount = 10

    private val adminCount = 3

    private val COLLECTION_NAME = "User"

    @ConfigProperty(name = "%test.quarkus.mongodb.database")
    private lateinit var db: String

    @ConfigProperty(name = "%test.quarkus.mongodb.connection-string")
    private lateinit var uri: String

    private lateinit var collection: MongoCollection<User>

    @BeforeEach
    fun init(){
        val pojoCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))
        val settings: MongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .codecRegistry(pojoCodecRegistry)
                .build()

        collection = MongoClients.create(settings).getDatabase(db).getCollection(COLLECTION_NAME, User::class.java)

        runBlocking {
            for(i in 1 .. totalCount){
                val roles = mutableSetOf<Role>()
                roles.add(Role.USER)
                if(i > 7) roles.add(Role.ADMIN)
                Uni.createFrom().publisher(collection.insertOne(User(
                    username = "user$i",
                    password = BcryptUtil.bcryptHash("pwd$i"),
                    roles = roles
                    ))).awaitSuspending()
            }
        }
    }

    @Test
    fun `find a user by username`(){
        runBlocking {
            val correctUser = userRepo.findByUsername("user1")
            Assertions.assertNotNull(correctUser)
            Assertions.assertEquals("user1", correctUser?.username)
            val wrongUser = userRepo.findByUsername("user11")
            Assertions.assertNull(wrongUser)
        }
    }

    @Test
    fun `find all users as list`(){
        runBlocking {
            val list = userRepo.findAsList()
            Assertions.assertEquals(totalCount, list.size)
        }
    }

    @Test
    fun `find users as list with filter`(){
        runBlocking {
            val list = userRepo.findAsList(Filters.eq("roles", Role.ADMIN.name))
            Assertions.assertEquals(adminCount, list.size)
            list.forEach { Assertions.assertTrue(it.roles?.contains(Role.ADMIN) ?: false) }
        }
    }

    @Test
    fun `find users as list with option`(){
        runBlocking {
            val list = userRepo.findAsList(findOptions = FindOptions().limit(5))
            Assertions.assertEquals(5, list.size)
        }
    }

    @Test
    fun `find users as list with filter and option`(){
        runBlocking {
            val list = userRepo.findAsList(
                    Filters.eq("roles", Role.ADMIN.name),
                    FindOptions().limit(1)
            )
            Assertions.assertEquals(1, list.size)
        }
    }

    @Test
    fun `find all users as flow`(){
        runBlocking {
            val list = userRepo.findAsFlow().toList()
            Assertions.assertEquals(totalCount, list.size)
        }
    }

    @Test
    fun `find users as flow with filter`(){
        runBlocking {
            val list = mutableListOf<User>()
            userRepo.findAsFlow(Filters.eq("roles", Role.ADMIN.name)).collect { list.add(it) }
            Assertions.assertEquals(adminCount, list.size)
        }
    }

    @Test
    fun `find users as flow with option`(){
        runBlocking {
            val list = mutableListOf<User>()
            userRepo.findAsFlow(findOptions = FindOptions().limit(5)).collect { list.add(it) }
            Assertions.assertEquals(5, list.size)
        }
    }

    @Test
    fun `find users as flow with filter and option`(){
        runBlocking {
            val list = mutableListOf<User>()
            userRepo.findAsFlow(
                    Filters.eq("roles", Role.ADMIN.name),
                    FindOptions().limit(1))
                    .collect { list.add(it) }
            Assertions.assertEquals(1, list.size)
            Assertions.assertTrue(list.first().roles?.contains(Role.ADMIN) ?: false)
        }
    }

    @Test
    fun `count filtered users`(){
        runBlocking {
            val count = userRepo.count(Filters.eq("roles", Role.ADMIN.name))
            Assertions.assertEquals(adminCount.toLong(), count)
        }
    }

    @Test
    fun `find specific user`(){
        runBlocking {
            val user = userRepo.findOne(
                    Filters.and(
                            Filters.eq("username", "user8"),
                            Filters.eq("roles", Role.ADMIN.name)
                    )
            )
            Assertions.assertEquals("user8", user?.username)
            Assertions.assertTrue(user?.roles?.contains(Role.ADMIN) ?: false)
        }
    }

    @Test
    fun `find specific user but not exists`(){
        runBlocking {
            val user = userRepo.findOne(
                    Filters.and(
                            Filters.eq("username", "user1"),
                            Filters.eq("roles", Role.ADMIN.name)
                    )
            )
            Assertions.assertNull(user)
        }
    }

    @AfterEach
    fun clean(){
        runBlocking {
            Uni.createFrom().publisher(collection.drop()).awaitSuspending()
        }
    }
}