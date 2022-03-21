# Article Repository and Route
### 說明
* 加入文章相關功能

### 流程
1. 建立 Article file
    * 套件位置：src.main.kotlin.model.po
    * data class Article
        * Article 繼承 Document，屬性 :
            * author: String
            * authorName: String
            * category: String
            * title: String
            * content: String
            * enabled: Boolean
            * createdTime: Instant
            * lastModifiedTime:  Instant
            

2. 建立 ArticleRepository
    * 套件位置：src.main.kotlin.repository
    * 資料庫的部分使用 [MongoDB Reactive Streams Java Driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/)
      ，相較於 Panache 有較大的彈性
    * 預計新增功能
      * createArticle - 新增文章
      * updateArticle - 更新文章
      * getArticleListByUser - 該使用者發佈的文章列
      * getArticleById - 取得該文章內容
      * deleteArticle - 刪除文章
    

3. 建立 ArticleRoute
    * 套件位置：src.main.kotlin.route
    * 繼承 BaseRoute
    * api 規格：
   
    | method | path | description |  roles allowed |
    | -------- | -------- | -------- | -------- |
    | POST    | /article     | 新增文章    | ADMIN, USER |
    | GET     | /article-list     | 文章列表   | ADMIN, USER |
    | GET     | /article?articleId={articleId}    | 文章內容  | ADMIN, USER |
    | PUT     | /article?articleId={articleId}     | 編輯更新文章    | ADMIN, USER |
    | DELETE  | /article?articleId={articleId}       | 刪除文章   | ADMIN, USER |


4.建立 ArticleRepository 的 unit test, ArticleRepositoryTest
    * 套件位置：src.test.kotlin.repository
    * 繼承 BaseRoute
    * 在方法上加上註解，以便在測試開始前設置資料，或測試結束後清理資料

| 註解   | @BeforeAll | @BeforeEach  |  @AfterAll   |  @AfterEach  |
| -------- | -------- | -------- | -------- | -------- |
| 執行時機  |   所有 test 開始前  |  每個 test 開始前    |  所有 test 結束後  |   每個 test 結束後     |

* 參考：[QUARKUS - TESTING YOUR APPLICATION](https://quarkus.io/guides/getting-started-testing)