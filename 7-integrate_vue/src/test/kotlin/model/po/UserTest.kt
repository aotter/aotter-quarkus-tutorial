//package model.po
//
//import com.mongodb.ConnectionString
//import com.mongodb.MongoClientSettings
//import com.mongodb.client.model.Filters
//import com.mongodb.client.model.Filters.eq
//import com.mongodb.reactivestreams.client.MongoClients
//import com.mongodb.reactivestreams.client.MongoCollection
//import io.quarkus.test.junit.QuarkusTest
//import io.smallrye.mutiny.Uni
//import io.smallrye.mutiny.coroutines.awaitSuspending
//import kotlinx.coroutines.runBlocking
//import org.bson.codecs.configuration.CodecRegistries
//import org.bson.codecs.configuration.CodecRegistry
//import org.bson.codecs.pojo.PojoCodecProvider
//import org.eclipse.microprofile.config.inject.ConfigProperty
//import org.junit.jupiter.api.*
//import org.reactivestreams.Publisher
//import repository.UserRepository
//import security.Role
//import javax.inject.Inject
//
//@QuarkusTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//class UserTest {
//
//    private val TEST_USER_NAME_A = "test_user_a"
//
//    private val TEST_PASSWORD_A = "test_password_a"
//
//    private val TEST_USER_NAME_B = "test_user_b"
//
//    private val TEST_PASSWORD_B = "test_password_b"
//
//    private val TEST_ROLES = mutableSetOf(Role.USER)
//
//    private val COLLECTION_NAME = "User"
//
//    @ConfigProperty(name = "%test.quarkus.mongodb.database")
//    private lateinit var db: String
//
//    @ConfigProperty(name = "%test.quarkus.mongodb.connection-string")
//    private lateinit var uri: String
//
//    private lateinit var collection: MongoCollection<User>
//
//    @Inject
//    lateinit var userRepo: UserRepository
//
//    @BeforeAll
//    fun init(){
//        runBlocking {
//
//            val pojoCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
//                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))
//            val settings: MongoClientSettings = MongoClientSettings.builder()
//                    .applyConnectionString(ConnectionString(uri))
//                    .codecRegistry(pojoCodecRegistry)
//                    .build()
//
//            collection = MongoClients.create(settings).getDatabase(db).getCollection(COLLECTION_NAME, User::class.java)
//
//            userRepo.create(TEST_USER_NAME_A, TEST_PASSWORD_A, TEST_ROLES)
//            userRepo.create(TEST_USER_NAME_B, TEST_PASSWORD_B, TEST_ROLES)
//        }
//    }
//
//    @Test
//    fun `create user`(){
//        runBlocking {
//            val beforeCount = userRepo.count(Filters.exists(User::username.name))
//            val TEST_USER_NAME_C = "test_user_c"
//            val TEST_PASSWORD_C = "test_password_c"
//            val insertUser = userRepo.create(TEST_USER_NAME_B, TEST_PASSWORD_B, TEST_ROLES)
//            val afterCount = userRepo.count(Filters.exists(User::username.name))
//            Assertions.assertEquals(afterCount, beforeCount + 1 )
//            Assertions.assertEquals(TEST_USER_NAME_C, insertUser.username)
//            Assertions.assertEquals(TEST_ROLES, insertUser.roles)
//            `verify user password`(TEST_PASSWORD_C, insertUser)
//        }
//    }
//
//    fun `verify user password`(expectedPassword: String, user: User?){
//        val result = user?.verifyPassword(expectedPassword.toCharArray())
//        Assertions.assertTrue(result ?: false)
//    }
////
////    @Test
////    fun `update user role`(){
////        runBlocking {
////            val newRole = mutableSetOf(Role.USER, Role.ADMIN)
////            val user = uniAwait(collection.find(eq("username", TEST_USER_NAME_A)).first())
////            val updatedUser = user?.updateRole(newRole)
////            Assertions.assertEquals(newRole, updatedUser?.roles)
////        }
////    }
////
////    @Test
////    fun `update user password`(){
////        runBlocking {
////            val newPassword = "new_password"
////            val user = uniAwait(collection.find(eq("username", TEST_USER_NAME_A)).first())
////            val updateUser = user?.updatePassword(newPassword)
////            val result = updateUser?.verifyPassword(newPassword.toCharArray())
////            Assertions.assertTrue(result ?: false)
////        }
////    }
////
//    @Test
//    fun `verify user password with wrong password`(){
//        runBlocking {
//            val wrongPassword = "wrong_password"
//            val user = uniAwait(collection.find(eq("username", TEST_USER_NAME_B)).first())
//            val result = user?.verifyPassword(wrongPassword.toCharArray())
//            Assertions.assertFalse(result ?: false)
//        }
//    }
//
//    @Test
//    fun `verify user password with right password`(){
//        runBlocking {
//            val user = uniAwait(collection.find(eq("username", TEST_USER_NAME_B)).first())
//            val result = user?.verifyPassword(TEST_PASSWORD_B.toCharArray())
//            Assertions.assertTrue(result ?: false)
//        }
//    }
//
//    private suspend fun <T> uniAwait(publisher: Publisher<T>): T{
//        return Uni.createFrom().publisher(publisher).awaitSuspending()
//    }
//
//    @AfterAll
//    fun clean(){
//        runBlocking {
//            collection.drop()
//        }
//    }
//
//}