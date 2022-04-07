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
     
* 1-user
  1. 建立 enum class Role
     * 套件位置：src.main.kotlin.security
     * USER, ADMIN

  2. 建立 data class User
     * 套件位置：src.main.kotlin.model.po
     * 繼承 BaseMongoRepository<User>
     * 標註 @MongoEntity
     * 屬性 :

       | username     | String?   |    皆為小寫  |
       | -------- | -------- | -------- |
       | password     | String?   |  為使用者輸入的密碼加密後，標記為 @JsonIgnore  |
       | roles     | Role?     |   Role 為自訂的 enum class，預設 Role 為 User   |
     
     * User 繼承 BaseMongoEntity，撰寫 User 的 CRUD 方法
       * verifyPassword(passwordToVerify: CharArray): Boolean
           * 回傳驗證的結果

  3. 建立 abstract class BaseMongoEntity
     * 套件位置：src.main.kotlin.model.po
     * 是一個通用的 PanacheMongoEntity base
     * 繼承 ReactivePanacheMongoEntity
     * 使用 PanacheQL
     * 屬性：

       | lastModifiedTime     | Instant?     |    |
       | -------- | -------- | -------- |
       | createdTime     | Instant?     |  預設值為 Instant.now()  |
     
  4. 建立 UserRepository
      * 套件位置：src.main.kotlin.repository
      * 繼承 BaseMongoRepository<User>
      * 需註解為 @Singleton
          * 參考：[QUARKUS - CONTEXTS AND DEPENDENCY INJECTION](https://quarkus.io/guides/cdi-reference)
          * 實作方法：
              * init
                  * create user index
                  * 在 initializer block 內
              * create(username: String, password: String, roles: MutableSet<Role>): User
                  * 靜態方法，需將使用者輸入的密碼加密後再存入 DB，回傳新增的 User
                  * 使用 [quarkus-elytron-security-common](https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common/1.13.4.Final) 加密
              * updateRole(roles: MutableSet<Role>): User
                  * 回傳更新後的 User
              * updatePassword(password: String): User
                  * 回傳更新後的 User
              * findByUsername(username: String): User
                  * 回傳 User
                
  5. 撰寫 BaseMongoRepository 方法
      * 建立索引
          * createIndex(vararg indexModels: IndexModel)
      * 更新 lastModifiedTime or createdTime
          * save(entity: Entity)
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
          * findOneAndUpdate(filter: Bson, update: Bson, option: FindOneAndUpdateOptions): Entity?
          
* 2-security
  1. 加入 
     1. [quarkus-security](https://mvnrepository.com/artifact/io.quarkus/quarkus-security) dependency
     2. [quarkus-elytron-security-common](https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common) dependency
  
  2. 建立 extension file，用來定義 vertx 的擴充方法，包裝 coroutine 的使用
     * 套件位置：src.main.kotiln.util

  3. 建立 abstract class AbstractMongoIdentityProvider
     * 套件位置：src.main.kotlin.security
     * 抽出實作 IdentityProvider 的類別共同的方法：
        * buildSecurityIdentityProvider( ): SecurityIdentityProvider

  4. 建立 MongoIdentityProvider
     * 套件位置：src.main.kotlin.security
     * 繼承 AbstractMongoIdentityProvider
     * 需註解為 @ApplicationScoped
     * 實作 IdentityProvider<TrustedAuthenticationRequest>
     * 處理來自可信任的來源（例如加密的cookie）的身份驗證請求
     * override 方法：
        * getRequestType( )
        * authentication( )

  5. 建立 MongoTrustedIdentityProvider
     * 套件位置：src.main.kotlin.security
     * 繼承 AbstractMongoIdentityProvider
     * 需註解為 @ApplicationScoped
     * 實作 IdentityProvider<UsernamePasswordAuthenticationRequest>
     * 處理使用用戶名和密碼的簡單身份驗證請求
     * override 方法：
        * getRequestType( )
        * authentication( )


* 3-quarkus_qute
    1. 加入 [quarkus-security](https://mvnrepository.com/artifact/io.quarkus/quarkus-resteasy-reactive-qute) dependency

    2. 建立 data class UserResponse
       * 套件位置：src.main.kotlin.model.vo
       * 屬性 :

       | username     | String?   |    使用者名稱  |        
       | -------- | -------- | -------- |
       | role     | Role    |  使用者角色  |
       
       * constructor(user: User)

    3. 建立 UserResource
        * 套件位置：src.main.kotlin.resource
        * 需註解為 @Consumes(MediaType.TEXT_HTML)
        * 需註解為 @Produces(MediaType.TEXT_HTML)
        * @Path("/rest")
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

    4. 建立 UserResource
        * 套件位置：src.main.kotlin.resource.api
        * 需註解為 @Consumes(MediaType.APPLICATION_JSON)
        * 需註解為 @Produces(MediaType.APPLICATION_JSON)
        * @Path("api/rest/user")
        * data class SignUpRequest
          | username     | String   |    @JsonProperty("username")  |
          | -------- | -------- | -------- |
          | password     | String    |  @JsonProperty("password")  |
        * api path：
            * signUp(@RequestBody body: SignUpRequest): User
              * 使用者註冊
              * @POST
              * @Path("signup")
            * user(@Context securityContext: SecurityContext): UserResponse
                * 使用者資訊
                * @GET
                * @Path("me")           
                * @RolesAllowed 註解做權限保護, 限定 ADMIN, USER 存取 
            * admin(@Context securityContext: SecurityContext): String
                * 管理者名稱
                * @GET
                * @Path("admin")
                * @RolesAllowed 註解做權限保護, 限定 ADMIN 存取
            * logout(): Response
                * 登出
                * @GET
                * @Path("logout")
        * 透過 SecurityContext 可存取權限相關資訊

    5. 建立前端測試頁面
        * 檔案位置：src.main.resources.templates.UserResource
        * 需求頁面：
            * signUp.html：帳號註冊頁
            * login.html：登入頁

        
* 4-article
    1. 建立 data class Article
        * 套件位置：src.main.kotlin.model.po
        * 繼承 BaseMongoRepository<Article>
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
       * 套件位置：src.main.kotlin.model.dto
       * 屬性 :

         | category     | String   |    文章分類  |   @field:[NotNull() NotBlank()] |
         | -------- | -------- | -------- | -------- |
         | title     | String    |  文章標題  |   @field:[NotNull() NotBlank()] |
         | content     | String    |   文章內容   |   @field:[NotNull() ] |

    3. 建立 data class PageRequest
       * 套件位置：src.main.kotlin.model.dto
       * 屬性 :

           | page     | Int |    頁碼  | @field:[ QueryParam("page") DefaultValue("1") Min(1) Max(100)]  |
           |-----| -------- |-----------------------------------------------------------------| -------- |
           | show     | Int |  單頁顯示數量  | @field:[ QueryParam("show") DefaultValue("10") Min(1) Max(100)] |

    4. 建立 data class ArticleResponse
       * 套件位置：src.main.kotlin.model.vo
       * 屬性 :

           | category     | String?   | 文章分類           |        
           | -------- |----------------| -------- |
           | title     | String?    | 文章標題           |
           | content     | String??    | 文章內容           |
           | author     | String?    | 使用者（作者） UserId |
           | authorName     | String?    | 使用者名稱          |
           | lastModifiedTime     | String?    | 最後更新時間         |

    5. 建立 data class BaseListResponse
       * 套件位置：src.main.kotlin.model.vo
       * 屬性 :

           | list     | List<T>   |        |        
           | -------- |--------| -------- |
           | page     | Int    | 頁碼     |
           | show     | Int    | 單頁顯示數量 |
           | total     | Long    | 總量     |
           | totalPages     | Int    | 總頁碼    |
       
         * constructor(list: List<T>, page: Int, show: Int, total: Long)

    6. 建立 ArticleRepository
        * 套件位置：src.main.kotlin.repository
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

    7. 建立 ArticleService
        * 套件位置：src.main.kotlin.service
        * 需註解為 @Singleton
            * 實作方法：
                * findAsListResponse(
                  authorId: ObjectId?, category: String?, published: Boolean?, page: Int, show: Int): BaseListResponse<ArticleListResponse>
                    * 回傳組裝好的 BaseListResponse<ArticleListResponse>
                * convertToArticleListResponse(list: List<Article>): List<ArticleListResponse>
                    * 回傳組裝好的 ArticleListResponse

    8. 建立 PublicArticleResource
        * 套件位置：src.main.kotlin.resource
        * 需註解為 @Consumes(MediaType.TEXT_HTML)
        * 需註解為 @Produces(MediaType.TEXT_HTML)
        * 註解為 @Path("/")
        * 端點：
            * article(@QueryParam("articleId") articleId: String): TemplateInstance
                * @GET
                * @Path("article-content")
            * getAllArticleList(@QueryParam("author") author: String?, @QueryParam("category") category: String?, @QueryParam("page") @DefaultValue("1") page: Int, @QueryParam("show") @DefaultValue("6") show: Int): TemplateInstance
                * @GET: TemplateInstance
                * @Path("article-list")
        * 實作方法：
            * 建立 object Templates
            * 需註解為 @CheckedTemplate(requireTypeSafeExpressions = false)
                * external fun article(article: ArticleResponse): TemplateInstance
                    * 需註解 @JvmStatic
                * external fun articleList(): TemplateInstance
                    * 需註解 @JvmStatic

    9. 建立 AdminArticleResource
        * 套件位置：src.main.kotlin.resource.api
        * 需註解為 @Consumes(MediaType.APPLICATION_JSON)
        * 需註解為 @Produces(MediaType.APPLICATION_JSON)
        * @RolesAllowed 註解做權限保護, 限定 ADMIN, USER 存取
        * @Path("api/admin")
        * api path：
            * create(@Context securityContext: SecurityContext, @Valid req: ArticleRequest): Article
                * 新增文章
                * @POST
                * @Path("article")
            * getArticleById(@Context securityContext: SecurityContext, @QueryParam("articleId") articleId: String): Article
                * 取得文章資訊
                * @GET
                * @Path("article")
            * update(@Context securityContext: SecurityContext, @QueryParam("articleId") articleId: String, @Valid req: ArticleRequest): Article
                * 更新文章
                * @PUT
                * @Path("article")
            * publish(@Context securityContext: SecurityContext, @QueryParam("articleId") articleId: String, @QueryParam("published") published: Boolean): Article
                * 更新發布狀態
                * @PUT
                * @Path("update-publish-status")
            * delete(@Context securityContext: SecurityContext, @QueryParam("articleId") articleId: String): Article
                * 刪除文章
                * @DELETE
                * @Path("article")
            * getArticleListByUser(@Context securityContext: SecurityContext, @QueryParam("page") @DefaultValue("1") page: Int, @QueryParam("show") @DefaultValue("10") show: Int): BaseListResponse<ArticleListResponse>
                * 取得登入使用者的文章列
                * @GET
                * @Path("articles")       
        * 透過 SecurityContext 可存取權限相關資訊

    10. 建立前端測試頁面
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
  4. 撰寫前端頁面
      * 使用 [Vue Router](https://router.vuejs.org/) 達到 [SPA (Single Page Application)](https://zh.wikipedia.org/wiki/%E5%8D%95%E9%A1%B5%E5%BA%94%E7%94%A8) 的效果，這也是公司慣用的前端模式
      * router 使用預設的 hash-mode，如果使用 history-mode 的話 server 端必須額外處理請求 404 的重新導向，[參考這篇](https://router.vuejs.org/guide/essentials/history-mode.html#example-server-configurations)
      * 共有四個頁面 (component) 和一個 Header component
          * Home，預設與登入成功後的重導頁面，顯示當前登入狀態
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