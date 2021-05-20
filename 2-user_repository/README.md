# User Repository
### 說明
* 建立與資料庫溝通的 UserRepository
* 使用 [Quarkus MongoDB with Panache](https://quarkus.io/guides/mongodb-panache) 的 reactive 版本
* Panache 提供兩種 pattern
    * [active record pattern](https://quarkus.io/guides/mongodb-panache#solution-1-using-the-active-record-pattern)
    * [repository pattern](https://quarkus.io/guides/mongodb-panache#solution-2-using-the-repository-pattern)
      ，UserRepository 將使用此 pattern
* 以 abstract class BaseMongoRepository 包裝 UserRepository 的 CRUD；
    * BaseMongoRepository 將實作 ReactivePanacheMongoRepository，UserRepository 再繼承 BaseMongoRepository

### 流程
1. 建立 abstract class BaseMongoRepository
    * 套件位置：src.main.kotlin.repository
    * 是一個通用的 PanacheMongoRepository base
    * 繼承 ReactivePanacheMongoRepository

2. 撰寫 BaseMongoRepository 方法
    * 建立索引
        * createIndex(vararg indexModels: IndexModel)
    * PanchQL 擴充方法
        * ReactivePanacheQuery<Entity>.toList()
            * PancheQL list() with coroutine
        * ReactivePanacheQuery<Entity>.asFlow()
            * PancheQL stream() to coroutine flow
    * 查詢方法
        * findAsList(filter: Bson?): List<Entity>
        * findAsList(filter: Bson?, findOptions: FindOptions): List<Entity>
        * findAsFlow(filter: Bson?): Flow<Entity>
        * findAsFlow(filter: Bson?, findOptions: FindOptions): Flow<Entity>
        * count(filter: Bson): Long
        * findOne(filter: Bson): Entity?
1. 建立 UserRepository
    * 套件位置：src.main.kotlin.repository
    * 繼承 BaseMongoRepository<User>
    * 需註解為 @Singleton
        * 參考：[QUARKUS - CONTEXTS AND DEPENDENCY INJECTION](https://quarkus.io/guides/cdi-reference)
    * 實作方法：
        * init
            * create user index
            * 在 initializer block 內
        * findByUserName(username: String?): User?

3. 建立 UserRepository 的 unit test, UserRepositoryTest
    * 套件位置：src.test.kotlin.repository
    * 在 src/main/resources/application.properties 內設定 unit test 用的 mongoDB 連線字串
    ```
        %test.quarkus.mongodb.connection-string = mongodb://localhost:27017
        %test.quarkus.mongodb.database = unit-test-quarkus
    ```
    * unit test 的 class 必須註解為 @QuarkusTest
    * unit test 的方法必須註解為 @Test
    * 在方法上加上註解，以便在測試開始前設置資料，或測試結束後清理資料

      | 註解   | @BeforeAll | @BeforeEach  |  @AfterAll   |  @AfterEach  |
               | -------- | -------- | -------- | -------- | -------- |
      | 執行時機  |   所有 test 開始前  |  每個 test 開始前    |  所有 test 結束後  |   每個 test 結束後     |

    * 參考：[QUARKUS - TESTING YOUR APPLICATION](https://quarkus.io/guides/getting-started-testing)