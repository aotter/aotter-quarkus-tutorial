# User
### 說明
* 建立與資料庫溝通的 User data class
* 使用 [Quarkus MongoDB with Panache](https://quarkus.io/guides/mongodb-panache) 的 reactive 版本
* Panache 提供兩種 pattern
   * [active record pattern](https://quarkus.io/guides/mongodb-panache#solution-1-using-the-active-record-pattern)
     ，User 將使用此 pattern
   * [repository pattern](https://quarkus.io/guides/mongodb-panache#solution-2-using-the-repository-pattern)
* 以 abstract class BaseMongoEntity 包裝 User 的 CRUD；
   * BaseMongoEntity 將繼承 ReactivePanacheMongoEntity，User 再繼承 ReactivePanacheMongoEntity

### 流程
1. 設定 DB 連線字串
   * 在 src/main/resources/application.properties 內設定 mongoDB 連線字串
    ```
        quarkus.mongodb.connection-string = mongodb://localhost:27017
        quarkus.mongodb.database = test-quarkus
    ```
   * 參考：[QUARKUS - CONFIGURING YOUR APPLICATION](https://quarkus.io/guides/config)

1. 建立 enum class Role
   * 套件位置：src.main.kotlin.security
   * USER, ADMIN

1. 建立 data class User
   * 套件位置：src.main.kotlin.model.po
   * 屬性 :

   | username     | String?     |    皆為小寫  |
   | -------- | -------- | -------- |
   | password     | String?     |  為使用者輸入的密碼加密後，標記為 @JsonIgnore  |
   | roles     | MutableSet\<Role>?     |   Role 為自訂的 enum class，預設 Role 為 User   |

1. 建立 abstract class BaseMongoEntity
   * 套件位置：src.main.kotlin.model.po
   * 是一個通用的 PanacheMongoEntity base
   * 繼承 ReactivePanacheMongoEntity
   * 使用 PanacheQL  
   * 屬性：

   | lastModifiedTime     | Instant?     |    |
   | -------- | -------- | -------- |
   | createdTime     | Instant?     |  預設值為 Instant.now()  |
1. User 繼承 BaseMongoEntity，撰寫 User 的 CRUD 方法
   * create(username: String, password: String, roles: MutableSet<Role>): User
      * 靜態方法，需將使用者輸入的密碼加密後再存入 DB，回傳新增的 User
      * 使用 [quarkus-elytron-security-common](https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common/1.13.4.Final) 加密
   * updateRole(roles: MutableSet<Role>): User
      * 回傳更新後的 User
   * updatePassword(password: String): User
      * 回傳更新後的 User
   * verifyPassword(passwordToVerify: CharArray): Boolean
      * 回傳驗證的結果

1. 建立 User 的 unit test, UserTest
   * 套件位置：src.test.kotlin.model.po
   * 在 src/main/resources/application.properties 內設定 unit test 用的 mongoDB 連線字串
    ```
        %test.quarkus.mongodb.connection-string = mongodb://localhost:27017
        %test.quarkus.mongodb.database = unit-test-quarkus
    ```
   * unit test 的 class 必須註解為 @QuarkusTest
   * 測試的方法必須註解為 @Test
   * 在方法上加上註解，以便在測試開始前設置資料，或測試結束後清理資料
     
     | 註解   | @BeforeAll | @BeforeEach  |  @AfterAll   |  @AfterEach  |
     | -------- | -------- | -------- | -------- | -------- |
     | 執行時機  |   所有 test 開始前  |  每個 test 開始前    |  所有 test 結束後  |   每個 test 結束後     |
        ps. 使用註解的方法必須為靜態方法，或在 class 層級加上 @TestInstance(TestInstance.Lifecycle.PER_CLASS) 註解，
   否則執行時會拋錯

   * 參考：[QUARKUS - TESTING YOUR APPLICATION](https://quarkus.io/guides/getting-started-testing)
