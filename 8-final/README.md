# 專案說明

###說明
從零開始建立 quarkus 專案， 目標是完成一個可以讓用戶註冊登入後，透過後台發布文章，非用戶可在前台觀看所有已發布文章。管理者則可以在後台系統查詢所有文章或用戶。


###步驟
* 0-Init
  1. 建立專案
     * 使用 [Quarkus 專案初始化工具](https://code.quarkus.io/)
       設定：
        * quarkus version : 2.1.0.Final 以上
        * kotlin version : 1.5.21 以上
        * Group : aotter.net
        * Artifact : aotter-quarkus-tutorial
        * Build Tool : Maven
        * Example Code : No, thanks
        * extensions :
           * Alternative languages : Kotlin
           * Data : MongoDB with Panache for Kotlin
           * Web : RESTEasy reactive Jackson, SmallRye OpenAPI
  2. 加入當前必要 dependency
     * [vertx-lang-kotlin-coroutines](https://mvnrepository.com/artifact/io.vertx/vertx-lang-kotlin-coroutines)
     * [mutiny-kotlin](https://mvnrepository.com/artifact/io.smallrye.reactive/mutiny-kotlin)
     * [quarkus-elytron-security-common](https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common/1.13.4.Final)
       * 請移除 version ，否則編譯的時候會報錯
     * [quarkus-security](https://mvnrepository.com/artifact/io.quarkus/quarkus-security)  
       * 請移除 version ，否則編譯的時候會報錯
       
* 1-user
  1. 說明
     1. 使用 [Quarkus MongoDB with Panache](https://quarkus.io/guides/mongodb-panache) 的 reactive 版本
     2. Panache 提供兩種 pattern
        1. [active record pattern](https://quarkus.io/guides/mongodb-panache#solution-1-using-the-active-record-pattern)
        2. [repository pattern](https://quarkus.io/guides/mongodb-panache#solution-2-using-the-repository-pattern)
  
  2. 設定 DB 連線字串
    * 在 src/main/resources/application.properties 內設定 mongoDB 連線字串
      ```
          quarkus.mongodb.connection-string = mongodb://localhost:27017
          quarkus.mongodb.database = test-quarkus
      ```
    * 參考：[QUARKUS - CONFIGURING YOUR APPLICATION](https://quarkus.io/guides/config)
  2. 建立 enum class Role
     * 套件位置：src.main.kotlin.aotter.net.security
     * USER, ADMIN
     
  3. 建立 abstract class BaseMongoEntity<T>
      * 套件位置：src.main.kotlin.aotter.net.model.po
      * 是一個通用的 PanacheMongoEntity base
      * 繼承 ReactivePanacheMongoEntity
      * 使用 PanacheQL
      * 屬性：

        | lastModifiedTime     | Instant?     |    |
        | -------- | -------- | -------- |
        | createdTime     | Instant?     |  預設值為 Instant.now()  |

  4. 撰寫 abstract class BaseMongoRepository<Entity: Any> 方法
       * 套件位置：src.main.kotlin.aotter.net.repository
       * 繼承 ReactivePanacheMongoRepository<Entity>
       * 標註 @ExperimentalCoroutinesApi
       * import org.jboss.logging.Logger
       * init mongoCollection
         ```
         val col: ReactiveMongoCollection<Entity> = this.mongoCollection()
         ```
       * 建立索引
          * createIndexes(vararg indexModels: IndexModel)
          * 加上 log 追蹤建立索引是否成功
       * 更新 lastModifiedTime or createdTime
           * save(entity: Entity): Entity
       * PanacheQL 擴充方法
           * ReactivePanacheQuery<Entity>.toList()
               * PanacheQL list() with coroutine
           * ReactivePanacheQuery<Entity>.asFlow()
               * PanacheQL stream() to coroutine flow
       * 查詢方法
           * findAsList(filter: Bson?): List<Entity>
           * findAsList(filter: Bson?, findOptions: FindOptions): List<Entity>
           * findAsFlow(filter: Bson?): Flow<Entity>
           * findAsFlow(filter: Bson?, findOptions: FindOptions): Flow<Entity>
           * count(filter: Bson): Long
           * findOne(filter: Bson): Entity?
           * findOneAndUpdate(filter: Bson, update: Bson, option: FindOneAndUpdateOptions): Entity?
     
  5. 建立 data class User
     * 套件位置：src.main.kotlin.aotter.net.model.po
     * 繼承 BaseMongoEntity<User>
     * 標註 @MongoEntity
     * 屬性 :

       | username    | String?   |    皆為小寫  |
       | -------- | -------- | -------- |
       | password    | String?   |  為使用者輸入的密碼加密後，標記為 @JsonIgnore  |
       | role     | Role?     |   Role 為自訂的 enum class，預設 Role 為 User   |
     
     * User 繼承 BaseMongoEntity，撰寫 User 的 CRUD 方法
       * verifyPassword(passwordToVerify: CharArray): Boolean
           * 回傳驗證的結果
     
  6. 建立 UserRepository
      * 套件位置：src.main.kotlin.aotter.net.repository
      * 繼承 BaseMongoRepository<User>
      * 需註解為 @Singleton
          * 參考：[QUARKUS - CONTEXTS AND DEPENDENCY INJECTION](https://quarkus.io/guides/cdi-reference)
          * 實作方法：
              * init
                  * create user index( 使用 username 作為 index)
                  * 在 initializer block 內
              * create(username: String, password: String, role:Role): User
                  * 使用 BaseMongoRepository 的 save()，需將使用者輸入的密碼加密後再存入 DB，回傳新增的 User
                  * 使用 [quarkus-elytron-security-common](https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common/1.13.4.Final) 加密
              * updateRole(user: User, role: Role): User
                  * 使用 BaseMongoRepository 的 save(entity), apply 更新的角色
                  * 回傳更新後的 User
              * updatePassword(user: User,password: String): User
                  * 使用 BaseMongoRepository 的 save(entity), apply 更新的角色
                  * 回傳更新後的 User
              * findByUsername(username: String): User
                  * 使用 BaseMongoRepository 的 findOne(filter)
                  * 回傳 User
          
* 2-security
  1. 說明
     1. 透過 HttpAuthenticationMechanism，Quarkus 會從 HTTP request 中獲取可驗證的憑證，再交由 [IdentityProvider](https://quarkus.io/guides/security#identity-providers) 將憑證轉換為 SecurityIdentity
     2. 這邊使用 [Form Based Authentication](https://quarkus.io/guides/security-built-in-authentication#form-auth)
       * 補充：quarkus-credential cookie 更新行為主要由這兩個 config 控制：
         * **quarkus.http.auth.form.new-cookie-interval**：
           當距離 cookie 產生的時間大於此設定值後，產生新的 cookie 替換舊的（同時會更新 expire time）
         * **quarkus.http.auth.form.timeout**：
           當距離 cookie 產生的時間大於此設定值後，不再更新 cookie，而是在下次請求時強制重新登入
         * 預設行為是重導回 login-page，並加上一個 key 為 quarkus-redirect-location、value 為請求路徑的 cookie，  
           在重新登入後直接重導回 cookie 紀錄的網址
       * 舉例：
         ```
           quarkus.http.auth.form.new-cookie-interval=PT10M
           quarkus.http.auth.form.timeout=PT30M
    
           // 只要距上次請求時間大於 10 分鐘小於 30 分鐘，就會更新 cookie 與其 expire time
           // 但當距上次請求時間大於 30 分鐘，則會強制重新登入 
         ```
     3. 提供實作 [Quarkus IdentityProvider](https://quarkus.io/guides/security#identity-providers) 的類別，Quarkus 會透過類別實作的 authenticate( ) 方法驗證使用者身份
     4. 參考：[QUARKUS - SECURITY ARCHITECTURE AND GUIDES](https://quarkus.io/guides/security)
     
  2. 建立 extension file，用來定義 vertx 的擴充方法，包裝 coroutine 的使用
     * 套件位置：src.main.kotiln.aotter.net.util
     * 實作：
       * Vertx.launch
       * Vertx.async
       * Vertx.uni

  3. 建立 abstract class AbstractMongoIdentityProvider
     * 套件位置：src.main.kotlin.aotter.net.security
     * inject Vertx
     * inject UserRepository
     * 抽出實作 IdentityProvider 的類別共同的方法：
        * buildSecurityIdentityProvider(user: User): SecurityIdentity
          * 使用 username 做為 Principal
          * addRole，加入使用者角色

  4. 建立 MongoIdentityProvider
     * 套件位置：src.main.kotlin.aotter.net.security
     * 繼承 AbstractMongoIdentityProvider
     * 需註解為 @ApplicationScoped
     * 實作 IdentityProvider<UsernamePasswordAuthenticationRequest>
     * 處理來自可信任的來源（例如加密的cookie）的身份驗證請求
     * override 方法：
        * getRequestType( )
        * authentication( )

  5. 建立 MongoTrustedIdentityProvider
     * 套件位置：src.main.kotlin.aotter.net.security
     * 繼承 AbstractMongoIdentityProvider
     * 需註解為 @ApplicationScoped
     * 實作 IdentityProvider<TrustedAuthenticationRequest>
     * 處理使用用戶名和密碼的簡單身份驗證請求
     * override 方法：
        * getRequestType( )
        * authentication( )


* 3-quarkus_qute
    1. 加入 [quarkus-resteasy-reactive-qute](https://mvnrepository.com/artifact/io.quarkus/quarkus-resteasy-reactive-qute) dependency
       * 參考 [QUTE TEMPLATING ENGINE](https://quarkus.io/guides/qute)
       * 參考 [QUTE REFERENCE GUIDE](https://quarkus.io/guides/qute-reference)
       
    2. 在 src/main/resources/application.properties 內設定

         ```
            quarkus.http.auth.session.encryption-key= moXBxFqO1fUCtcYNsQAEWEjm0AXM84kgpi7HKDePq+k=
            quarkus.http.auth.form.enabled = true
            quarkus.http.auth.form.login-page = /login
         ```  
    
    3. 建立 data class UserResponse
       * 套件位置：src.main.kotlin.aotter.net.model.vo
       * 屬性 :

       | username     | String?   |    使用者名稱  |        
       | -------- | -------- | -------- |
       | role     | Role    |  使用者角色  |
       
       * constructor(user: User)

    4. 建立 UserResource
        * 套件位置：src.main.kotlin.aotter.net.resource
        * 需註解為 @Consumes(MediaType.TEXT_HTML)
        * 需註解為 @Produces(MediaType.TEXT_HTML)
        * @Path("/")
        * 端點：
            * login(): TemplateInstance
              * 使用者登入頁
              * @GET
              * @Path("login")
            * signUp(): TemplateInstance
              * 使用者註冊頁
              * @GET
              * @Path("signup")
        * 實作方法：
          * 建立 object Templates 
          * 需註解為 @CheckedTemplate(requireTypeSafeExpressions = false)
            * external fun login(): TemplateInstance
              * 需註解 @JvmStatic
            * external fun signup(): TemplateInstance
                * 需註解 @JvmStatic
              
    5. Role 新增 companion object
       * 因為  @RolesAllowed argument must be a compile-time constant
       * 新增 ADMIN 及 USER 的 CONSTANT

    6. 建立 UserResource 的 api
        * 套件位置：src.main.kotlin.aotter.net.resource.api
        * 需註解為 @Consumes(MediaType.APPLICATION_JSON)
        * 需註解為 @Produces(MediaType.APPLICATION_JSON)
        * @Path("/api/user")
        * data class SignUpRequest
       
          | username     | String   |    @JsonProperty("username")  |
          | -------- | -------- | -------- |
          | password     | String    |  @JsonProperty("password")  |
        * api path：
            * signUp(): User
              * 使用者註冊
              * RequestBody 送 SignUpRequest 進來
              * @POST
              * @Path("signup")
            * user(): UserResponse
                * 使用者資訊
                * Context 中的 SecurityContext 包含使用者資訊
                * @GET
                * @Path("me")           
                * @RolesAllowed 註解做權限保護, 限定 ADMIN, USER 存取 
            * admin(@Context securityContext: SecurityContext): String
                * 管理者名稱
                * Context 中的 SecurityContext 包含使用者資訊
                * @GET
                * @Path("admin")
                * @RolesAllowed 註解做權限保護, 限定 ADMIN 存取
            * logout(): Response
                * 登出
                * 清空 quarkus-credential 裡的值
                * @GET
                * @Path("logout")

    7. 建立前端測試頁面
        * 檔案位置：src.main.resources.templates.UserResource
        * 需求頁面：
            * signUp.html：帳號註冊頁
            * login.html：登入頁

        
* 4-article
    1. 建立 data class Article
        * 套件位置：src.main.kotlin.aotter.net.model.po
        * 繼承 BaseMongoEntity<Article>
        * 標註 @MongoEntity
        * 屬性 :

          | author     | ObjectId   |    使用者（作者） UserId  |
          | -------- | -------- | -------- |
          | authorName     | String    |  使用者名稱  |
          | category     | String    |   文章分類   |
          | title     | String    |   文章標題   |
          | content     | String    |   文章內容   |
          | published     | Boolean    |   是否已發佈   |
          | visible     | Boolean    |   是否已刪除   |
       
        * constructor(author: User, req: ArticleRequest)
       
    2. 建立 data class ArticleRequest
       * 加入 [quarkus-hibernate-validator](https://mvnrepository.com/artifact/io.quarkus/quarkus-hibernate-validator) dependency
       * 套件位置：src.main.kotlin.aotter.net.model.dto
       * 屬性 :

         | category     | String   |    文章分類  |   @field:[NotNull() NotBlank()] |
         | -------- | -------- | -------- | -------- |
         | title     | String    |  文章標題  |   @field:[NotNull() NotBlank()] |
         | content     | String    |   文章內容   |   @field:[NotNull() ] |

    3. 建立 data class PageRequest
       * 套件位置：src.main.kotlin.aotter.net.model.dto
       * 屬性 :

           | page     | Int |    頁碼  | @field:[ QueryParam("page") DefaultValue("1") Min(1) Max(100)]  |
           |-----| -------- |-----------------------------------------------------------------| -------- |
           | show     | Int |  單頁顯示數量  | @field:[ QueryParam("show") DefaultValue("10") Min(1) Max(100)] |

    4. 建立 data class ArticleResponse
       * 套件位置：src.main.kotlin.aotter.net.model.vo
       * 屬性 :

           | category     | String?   | 文章分類           |        
           | -------- |----------------| -------- |
           | title     | String?    | 文章標題           |
           | content     | String?    | 文章內容           |
           | author     | String?    | 使用者（作者） UserId |
           | authorName     | String?    | 使用者名稱          |
           | lastModifiedTime     | String?    | 最後更新時間         |

    5. 建立 data class BaseListResponse
       * 套件位置：src.main.kotlin.aotter.net.model.vo
       * 屬性 :

           | list     | List<T>   |        |        
           | -------- |--------| -------- |
           | page     | Int    | 頁碼     |
           | show     | Int    | 單頁顯示數量 |
           | total     | Long    | 總量     |
           | totalPages     | Int    | 總頁碼    |
       
         * constructor(list: List<T>, page: Int, show: Int, total: Long)
         
    6. 建立 data class ArticleListResponse
       * 套件位置：src.main.kotlin.aotter.net.model.vo
       * 屬性 :
       
         | id     | String?   | 文章id           |        
         | -------- |----------------| -------- |
         | title     | String?    | 文章標題           |
         | category     | String?    | 文章分類           |
         | authorName     | String?    | 使用者名稱          |
         | lastModifiedTime     | String?    | 最後更新時間         |
         | published     | Boolean?    | 是否已發佈         |

    7. 建立 ArticleRepository
          * 套件位置：src.main.kotlin.aotter.net.repository
          * 繼承 BaseMongoRepository<Article>
          * 需註解為 @Singleton
              * 實作方法：
                  * init
                      * create article index
                      * 在 initializer block 內
                  * updatePublishStatus(id: ObjectId, published: Boolean): Article?
                      * 更新文章發布狀態,回傳更新後的 Article
                  * update(id: ObjectId, data: ArticleRequest): Article?
                      * 回傳更新後的 Article
                  * delete(id: ObjectId): Article?
                      * 刪除該Article
                  * list(authorId: ObjectId?, category: String?, published: Boolean?, page: Int = 1, show: Int): List<Article>
                      * 回傳該頁文章列
                  * count(authorId: ObjectId?, category: String?, published: Boolean?): Long
                      * 文章總數
                  * buildQuery(authorId: ObjectId?, category: String?, published: Boolean?): List<Bson>
                    * 回傳搜尋條件
                  * find(filters: List<Bson>, pageReq: PageRequest): List<Article>
                    * 回傳搜尋條件的文章列

    8. 建立 ArticleService
        * 套件位置：src.main.kotlin.aotter.net.service
        * 需註解為 @Singleton
            * 實作方法：
                * findAsListResponse(
                  authorId: ObjectId?, category: String?, published: Boolean?, page: Int, show: Int): BaseListResponse<ArticleListResponse>
                    * 回傳組裝好的 BaseListResponse<ArticleListResponse>
                * convertToArticleListResponse(list: List<Article>): List<ArticleListResponse>
                    * 回傳組裝好的 ArticleListResponse

    9. 建立 PublicArticleResource
        * 套件位置：src.main.kotlin.aotter.net.resource
        * 需註解為 @Consumes(MediaType.TEXT_HTML)
        * 需註解為 @Produces(MediaType.TEXT_HTML)
        * 註解為 @Path("/")
        * 端點：
            * article(): TemplateInstance
                * 得到文章內容
                * QueryParam 帶入articleId
                * @GET
                * @Path("article-content")
            * getAllArticleList(@QueryParam("author") author: String?, @QueryParam("category") category: String?, @QueryParam("page") @DefaultValue("1") page: Int, @QueryParam("show") @DefaultValue("6") show: Int): TemplateInstance
                * 得到已發布文章列表
                * QueryParam 帶入搜尋資訊 使用者id; 分類別; 頁碼; 單頁顯示比數
                * @GET: TemplateInstance
                * @Path("article-list")
        * 實作方法：
            * 建立 object Templates
            * 需註解為 @CheckedTemplate(requireTypeSafeExpressions = false)
                * external fun article(article: ArticleResponse): TemplateInstance
                    * 需註解 @JvmStatic
                * external fun articleList(): TemplateInstance
                    * 需註解 @JvmStatic

    10. 建立 AdminArticleResource
         * 套件位置：src.main.kotlin.aotter.net.resource.api
         * 需註解為 @Consumes(MediaType.APPLICATION_JSON)
         * 需註解為 @Produces(MediaType.APPLICATION_JSON)
         * @RolesAllowed 註解做權限保護, 限定 ADMIN, USER 存取
         * @Path("api/admin")
         * api path：
             * create(): Article
                 * 新增文章
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * @Valid 帶入文章資訊
                 * @POST
                 * @Path("article")
             * getArticleById(): Article
                 * 取得文章資訊
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * QueryParam 帶入 articleId
                 * @GET
                 * @Path("article")
             * update(): Article
                 * 更新文章
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * QueryParam 帶入 articleId
                 * @Valid 帶入文章資訊
                 * @PUT
                 * @Path("article")
             * publish(): Article
                 * 更新發布狀態
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * QueryParam 帶入 articleId
                 * QueryParam 帶入 published 狀態
                 * @PUT
                 * @Path("update-publish-status")
             * delete(): Article
                 * 刪除文章
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * QueryParam 帶入 articleId
                 * @DELETE
                 * @Path("article")
             * getArticleListByUser(): BaseListResponse<ArticleListResponse>
                 * 取得登入使用者的文章列
                 * Context 中的 SecurityContext 確認使用者是否已登入
                 * QueryParam 帶入 頁碼; 單頁顯示比數
                 * @GET
                 * @Path("articles")       

    11. 建立前端測試頁面
         * 檔案位置：src.main.resources.templates.PublicArticleResource
         * 需求頁面：
             * article.html：文章內容頁
             * articleList.html：文章列頁

* 5-integrate_vue
  1. 前端環境設置
      * [安裝 node / npm](https://nodejs.org/en/download/)
      * 安裝 Vue cli
          ```
          npm install -g @vue/cli
          ```
  2. 初始化 Vue app
      * 位置：src.main.webapp
      * 使用 Vue cli
          ```
          vue create .
          ```
  3. 整合 Quarkus 後端與 Vue 前端的開發
      * 在 vue.config.js 內設定 Vue 編譯完的檔案位置，讓 Quarkus app 可以取用
        ```
           module.exports = {
           outputDir: '../resources/META-INF/resources/assets/webapp',
           indexPath: '../../../../templates/VueResource/index.html', //relative to outputDir
           publicPath: '/assets/webapp',
           }
        ```
      * 設定 Quarkus app 的 pom.xml，添加 maven plugin 監聽 Vue app 變化
      * 執行這段指令就可以同步前後端的變化來開發
          ```
          ./mvnw exec:exec@npm-watch quarkus:dev
          ```
      * 設定 Quarkus app 的 pom.xml，添加 [frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin)
      * 執行這段指令就可以初始專案時一併下載打包好前後端
          ```
          ./mvnw install
          ```
  4. 撰寫前端頁面
      * 使用 [Vue Router](https://router.vuejs.org/) 達到 [SPA (Single Page Application)](https://zh.wikipedia.org/wiki/%E5%8D%95%E9%A1%B5%E5%BA%94%E7%94%A8) 的效果，這也是公司慣用的前端模式
      * router 使用預設的 hash-mode，如果使用 history-mode 的話 server 端必須額外處理請求 404 的重新導向，[參考這篇](https://router.vuejs.org/guide/essentials/history-mode.html#example-server-configurations)
      * 共有三個頁面 (component) 和一個 Header component
          * ArticleEdit，文章編輯頁
          * BackStage，後台頁
          * Error，登入失敗的重導頁面
          * Header，依登入狀態顯示不同的 router-link
              * 未登入：顯示 Signup, Login
              * 已登入：顯示 Logout
  5. 設定 Quarkus Form Based Authentication error 的重導路徑
      * 參考 [Configuration Property](https://quarkus.io/guides/security-built-in-authentication#form-auth)
      * 設定 src/main/resources/application.properties
          ```
          quarkus.http.auth.form.error-page = /index#/error
          ```