# Clam Repository
### 說明
* 參照自現有的服務 Aotter Clam，是一組萬用的 CRUD api，透過請求參數決定要操作的 collection，也不限制 request body 的資料長怎樣
* 因此 Panche MongoDB 的 ORM 就不太適合用在這裡，這邊用的是 [MongoDB Reactive Streams Java Driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/)

### 流程
1. 建立 ClamData file
    * 套件位置：src.main.kotlin.model.po
    * data class ClamData
        * ClamData 繼承 Document，只定義幾個必要的屬性 :
            * collectionName
            * authorId
            * createdTime
            * lastModifiedTime
            * publishedTime
            * state
    * enum class State
        * TEMP
        * PUBLISHED
        * ARCHIVED

2. 建立 ClamDataRepository
    * 套件位置：src.main.kotlin.repository
    * 資料庫的部分使用 [MongoDB Reactive Streams Java Driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/)
      ，相較於 Panache 有較大的彈性
    * 客製化的 DB 操作，透過 getCollection( ) 傳入的參數 collection 決定要操作的 collection
        ```        
        fun getCol(collectionName:String) = mongoClient.getDatabase(db).getCollection(collection)
        ```


3. 建立 ClamDataRepository 的 unit test, ClamDataRepositoryTest
    * 套件位置：src.test.kotlin.repository
    * 繼承 BaseRoute
    * 在方法上加上註解，以便在測試開始前設置資料，或測試結束後清理資料

      | 註解   | @BeforeAll | @BeforeEach  |  @AfterAll   |  @AfterEach  |
               | -------- | -------- | -------- | -------- | -------- |
      | 執行時機  |   所有 test 開始前  |  每個 test 開始前    |  所有 test 結束後  |   每個 test 結束後     |

    * 參考：[QUARKUS - TESTING YOUR APPLICATION](https://quarkus.io/guides/getting-started-testing)