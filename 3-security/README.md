# 3-security

## 前言
接下來我們要來完成系統的會員註冊與登入功能，我們的使用者角色又分為管理員與使用者。 Quarkus Security 可以很好的完成我們所需的功能。

## Security
對於一個應用程式來說， Security (安全性) 分為以下兩部分
1. Authentication(認證): 認證使用者的身份
2. Authorization(授權): 使用者是否有權限使用這項功能

在我們的情境下，使用者使用帳號密碼登入，系統透過帳號密碼與資料庫中使用者資料比對，認證使用者的身份。  
在使用管理文章功能時，判斷使用者角色是否有權限操作。

## Quarkus Security
提供開發者整套安全架構，包含多種認證與授權的機制，方便 Quarkus 應用程式建構安全性。

HttpAuthenticationMechanism 是 Quarkus HTTP 安全性的主要進入點， 用來從 HTTP 請求中提取身份驗證憑證，委派給 IdentityProvider 完成從憑證到 SecurityIdentity 轉換。  
舉例來說憑證從 HTTP Authorization header、 HTTP request body、 Cookie 取得，使用 IdentityProvider 驗證憑證並轉換為 SecurityIdentity 包含使用者名稱、角色、原本的憑證還有其他屬性。  
對於每個經過身份認證的資料，都可以注入 SecurityIdentity 取得經過認證後的身份資料，在不同環境中你也可以透過其他物件取得相同（或部分）身份資料，JAX-RS 的 SecurityContext 、 JWT 的 JsonWebToken。

#### Authentication mechanisms
Quarkus 支援從不同來源載入認證資訊，如 Basic HTTP、Form HTTP、OpenID Connect、SmallRye JWT、OAuth2 ...... 等等。  
在我們情境中我們就使用 Form HTTP 進行應用程式的身份認證，需要以下幾個步驟：
* 在 application.properties 設定 quarkus.http.auth.form.enabled=true ，啟用 Form HTTP Authentication
* 需要一個基於帳號密碼的 IdentityProvider extension 像是 Elytron JDBC ，但因為我們使用的資料庫為 MongoDB ，目前還沒有支援的 extension，我們需要自己客製 IdentityProvider
* 與傳統 Servlet 使用 HTTP session 不同，Quarkus 使用加密 Cookie，集群中共享相同的加密密鑰讓所有成員可以讀取。
* 在 application.properties 設定 quarkus.http.auth.session.encryption-key 長度必須大於 16 ，先使用 SHA-256 雜湊後用來當作 AES-256 加密 Cookie 值的密鑰

#### Authorization
在 Quarkus Security 設定授權支援配置文件(application.properties)和 annotation，但要知道的是會先執行配置文件檢查才會繼續檢查 annotation。  
在我們的情境中推薦直接使用 annotation 配置，可以在 REST 端點和 CDI beans 使用基於 Role-Based Access Control (RBAC) 的 annotation ex @RolesAllowed, @DenyAll, @PermitAll。  
另外還提供了 ＠Authenticated 表達 @RolesAllowed("**")，有更好的語意表達。  

[SECURITY ARCHITECTURE AND GUIDES](https://quarkus.io/guides/security)  
[BUILT-IN AUTHENTICATION SUPPORT](https://quarkus.io/guides/security-built-in-authentication)  
[AUTHORIZATION OF WEB ENDPOINTS](https://quarkus.io/guides/security-authorization)

## 登入
由於沒有關於 MongoDB IdentityProvider 的 extension ，我們直接加入 quarkus-security 倚賴，  
由於應用系統中資料庫不應該儲存明碼的密碼，我們還要加入 quarkus-elytron-security-common 用來雜湊密碼和比對。

add pom.xml dependency
```xml
    <!-- https://mvnrepository.com/artifact/io.quarkus/quarkus-security -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-security</artifactId>
    </dependency>
    <!-- https://mvnrepository.com/artifact/io.quarkus/quarkus-elytron-security-common -->
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-elytron-security-common</artifactId>
    </dependency>
```

#### 會員資料

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/security/Role ，enum 用來對應使用者的角色
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/po/User ，po 用來對應 MongoDB 的 collection
* 創建 src/main/koltin/net/aotter/quarkus/tutorial/repository/UserRepository

Role.kt
```kotlin
package net.aotter.quarkus.tutorial.security

enum class Role {
    USER,
    ADMIN;
}
```
* 有兩種角色 USER 和 ADMIN


User.kt
```kotlin
package net.aotter.quarkus.tutorial.model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import net.aotter.quarkus.tutorial.security.Role
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity
data class User(
    @field: BsonProperty("_id")
    var id: ObjectId? = null,
    var username: String,
    var credentials: String,
    var role: Role,
    var deleted: Boolean
): AuditingEntity()

```
* 一樣繼承 AuditingEntity
* 包含 SecurityIdentity 需要的資料 ，使用者名稱、角色 ......等等
* 讓 No-arg compiler plugin 作用加上 @MongoEntity
* 讓 PanacheQL 正常的 @field: BsonProperty("_id")

UserRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import net.aotter.quarkus.tutorial.model.po.User
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class UserRepository: AuditingRepository<User>() {
}
```
* 一樣繼承 AuditingRepository

我們看到 User.kt ，我們的 username 應該也要是唯一的，所以我們要加上 unique index
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/ReflectExtensions 新增 bsonFieldName 方法取得資料庫實際的 field 名稱
* 在 UserRepository 使用 PostConstruct 創建 unique index

ReflectExtensions.kt
```kotlin
package net.aotter.quarkus.tutorial.util

import org.bson.codecs.pojo.annotations.BsonProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun <T, R> KProperty1<T, R>.bsonFieldName() = this.javaField
    ?.getAnnotation(BsonProperty::class.java)
    ?.let { it.value }
    ?: this.name
```

* 取得 field 的 BsonProperty annotation ，拿到資料庫中真正的 field 名稱

UserRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.util.bsonFieldName
import org.jboss.logging.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class UserRepository: AuditingRepository<User>() {
    @Inject
    lateinit var logger: Logger

    @PostConstruct
    fun init(){
        mongoCollection().createIndex(
            Indexes.ascending(User::username.bsonFieldName()),
            IndexOptions().unique(true)
        )
            .onItemOrFailure()
            .transform { result, t ->
                if (t != null) {
                    logger.error("collection ${mongoCollection().documentClass.simpleName} index creation failed: ${t.message}")
                } else {
                    logger.info("collection ${mongoCollection().documentClass.simpleName} index creation: $result")
                }
            }.subscribe().with { /** ignore **/ }
    }
}
```

* 在初始化的時候對 username 建立 unique index
* Panache 不支援索引的創建，所以用底層 MongoDB Client 的方法，注意要寫實際的 field 名稱，不會自動轉換
* 這邊使用 Mutiny，因為 PostConstruct 無法使用 suspend 方法

再來我們新增兩位使用者到資料庫，當作範例資料

* 在 AppInitConfig 注入 UserRepository
* 在 AppInitConfig 新增 initUserData 方法用來新增資料到資料庫
* 在 onStart 方法中調用 initUserData

AppInitConfig.kt
```kotlin
package net.aotter.quarkus.tutorial.config

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.runtime.Startup
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.PostRepository
import net.aotter.quarkus.tutorial.repository.UserRepository
import net.aotter.quarkus.tutorial.security.Role
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@Startup
@ApplicationScoped
class AppInitConfig{
    @Inject
    lateinit var postRepository: PostRepository
    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var logger: Logger

    @PostConstruct
     fun onStart() {
        initPostData()
        initUserData()
    }

    private fun initPostData(){
        val posts = mutableListOf<Post>()
        for(index in 1..7){
            val post = Post(
                authorId = ObjectId("6278b21b245917288cd7220b"),
                authorName = "user",
                title = """Title $index""",
                category = "分類一",
                content = """Content $index""",
                published = true,
                deleted = false
            )
            posts.add(post)
        }
        postRepository.count()
            .subscribe().with{
                if(it == 0L){
                    postRepository.persistOrUpdate(posts)
                        .onItemOrFailure()
                        .transform{ _, t ->
                            if(t != null){
                                logger.error("post insert failed")
                            }else{
                                logger.info("post insert successfully")
                            }
                        }.subscribe().with{ /** ignore **/ }
                }
            }
    }

    private fun initUserData(){
        val admin = User(
            username = "admin",
            credentials = BcryptUtil.bcryptHash("admin@123"),
            role = Role.ADMIN,
            deleted = false
        )
        val user = User(
            username = "user",
            credentials = BcryptUtil.bcryptHash("user@123"),
            role = Role.USER,
            deleted = false
        )
        userRepository.count()
            .subscribe().with {
                if(it == 0L){
                    userRepository.persistOrUpdate(admin, user)
                        .onItemOrFailure()
                        .transform{_, t ->
                            if(t!= null){
                                logger.error("user insert failed")
                            }else{
                                logger.info("user insert successfully")
                            }
                        }.subscribe().with { /** ignore **/ }
                }
            }
    }
}


```
* 資料庫存的 credentials 是密碼經由 Bcrypt 雜湊的值

完成會員資料的設定，我們現在要來完成應用程式安全性設定

* 在 application.properties 加入 quarkus.http.auth.form.enabled=true ，啟用 Form HTTP Authentication
* 在 application.properties 設定 quarkus.http.auth.session.encryption-key ，用來加密 cookie
* 在 UserRepository 新增 findOneByDeletedIsFalseAndUsername 方法
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/security/AbstractUserIdentityProvider 
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/security/MongoUsernamePasswordIdentityProvider
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/security/MongoTrustedIdentityProvider

application.properties
```properties
quarkus.mongodb.database = blog

quarkus.http.auth.form.enabled=true
quarkus.http.auth.session.encryption-key= moXBxFqO1fUCtcYNsQAEWEjm0AXM84kgpi7HKDePq+k=
```

UserRepository.kt
```kotlin
...
    fun findOneByDeletedIsFalseAndUsername(username: String): Uni<User?>{
        val criteria = HashMap<String, Any>().apply {
            put(User::deleted.name, false)
            put(User::username.name, username)
        }
        return findByCriteria(criteria).firstResult()
    }
...
```

AbstractUserIdentityProvider.kt
```kotlin
package net.aotter.quarkus.tutorial.security

import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.runtime.QuarkusSecurityIdentity
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.UserRepository
import javax.inject.Inject

abstract class AbstractUserIdentityProvider {
    @Inject
    lateinit var userRepository: UserRepository


    fun loadUserByUsername(username: String): Uni<User?> {
        return userRepository.findOneByDeletedIsFalseAndUsername(username)
    }

    fun buildSecurityIdentity(user: User): SecurityIdentity {
        val builder = QuarkusSecurityIdentity.builder()
            .setPrincipal{ user.username }
        builder.addRole(user.role.name)
        return builder.build() as SecurityIdentity
    }
}
```
* 這裡我們將 User 轉換為 SecurityIdentity 方法提出來讓實際的 IdentityProvider 繼承使用
* SecurityIdentity 包含的使用者資訊有用來認證是誰的使用者名稱，判斷是否有權限的角色

MongoUsernamePasswordIdentityProvider.kt
```kotlin
package net.aotter.quarkus.tutorial.security

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoUsernamePasswordIdentityProvider: AbstractUserIdentityProvider(), IdentityProvider<UsernamePasswordAuthenticationRequest> {
    override fun getRequestType(): Class<UsernamePasswordAuthenticationRequest> {
        return UsernamePasswordAuthenticationRequest::class.java
    }

    override fun authenticate(
        request: UsernamePasswordAuthenticationRequest?,
        context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        val username = request?.username
        val password = request?.password?.password
        if(username == null || password == null){
            throw AuthenticationFailedException()
        }
        return loadUserByUsername(username).map { user ->
                user?.takeIf { BcryptUtil.matches(String(password), user.credentials) }
                    ?.let { buildSecurityIdentity(it) }
                    ?: throw AuthenticationFailedException()
        }
    }
}
```
* FormAuthenticationMechanism 會處理表單登入請求，並將請求中的帳號密碼封裝成 UsernamePasswordAuthenticationRequest，委派給 IdentityProvider 認證
* 因此我們實作 IdentityProvider 處理 UsernamePasswordAuthenticationRequest
* 從 MongoDB 查詢符合的使用者並且驗證密碼，記住由於我們存的是雜湊後的密碼，需要透過 BcryptUtil 來驗證是否相符，直接使用等於判斷會有問題
* 最後將使用者資訊封裝為 SecurityIdentity

MongoTrustedIdentityProvider.kt
```kotlin
package net.aotter.quarkus.tutorial.security

import io.quarkus.security.AuthenticationFailedException
import io.quarkus.security.identity.AuthenticationRequestContext
import io.quarkus.security.identity.IdentityProvider
import io.quarkus.security.identity.SecurityIdentity
import io.quarkus.security.identity.request.TrustedAuthenticationRequest
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MongoTrustedIdentityProvider: AbstractUserIdentityProvider(), IdentityProvider<TrustedAuthenticationRequest> {
    override fun getRequestType(): Class<TrustedAuthenticationRequest> {
        return TrustedAuthenticationRequest::class.java
    }

    override fun authenticate(
        request: TrustedAuthenticationRequest?,
        context: AuthenticationRequestContext?
    ): Uni<SecurityIdentity> {
        val username = request?.principal ?: throw AuthenticationFailedException()
        return loadUserByUsername(username).map { user ->
            user?.let { buildSecurityIdentity(it) }
                ?: throw AuthenticationFailedException()
        }
    }
}
```
* TrustedAuthenticationRequest 是指從可信任的地方取得的認證資訊，依我們的例子就是加密過後的 cookie
* 由於沒有使用 HTTP session ，所以當我們使用表單成功後，會將使用者資訊加密後存到 cookie，這樣之後的請求也會自動夾帶 cookie
* 因此我們需要實作 IdentityProvider 處理 TrustedAuthenticationRequest
* 這邊由於是信任地方取得的認證資訊，我們只要查詢使用者確保使用者還存在
* 最後一樣將使用者資訊封裝為 SecurityIdentity

再來我們要來完成登入頁面，但在這之前我們要先來調整我們的畫面，在上面的導覽列新增登入與登出的連結，並且要判斷登入狀態顯示或隱藏。  
為了判斷登入狀態，我們需要將身份認證資訊傳入模板，因此我們在 resource 注入 SecurityContext 並傳入模板。  
並且將共用的部分提到 AbstractTemplateResource 讓實際 Resource 繼承。  

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/template/SecurityExtensions ，在模板幫助判斷是否有登入
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/resource/AbstractTemplateResource ，將原本注入 UriInfo 和 buildHTMLMetaData 提到這裡，並多注入 SecurityContext
* 修改 PostResource 繼承 AbstractTemplateResource ，並修改 Templates 的方法 多傳入 SecurityContext
* 修改 layout.html 的 navbar 放上登入登出的連結並判斷登入狀態

SecurityExtensions.kt
```kotlin
package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import javax.ws.rs.core.SecurityContext

@TemplateExtension
class SecurityExtensions {
    companion object{
        @JvmStatic
        fun isAuthenticated(securityContext: SecurityContext): Boolean{
            return securityContext.userPrincipal != null
        }
    }
}
```

AbstractTemplateResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource

import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.util.abbreviate
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo

abstract class AbstractTemplateResource {
    @Context
    lateinit var uriInfo: UriInfo
    @Context
    lateinit var securityContext: SecurityContext

    fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData {
        val url = uriInfo.baseUriBuilder.replaceQuery("").toTemplate()
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}
```

PostResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.service.PostService
import net.aotter.quarkus.tutorial.util.abbreviate
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.RestPath
import javax.inject.Inject
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.UriInfo

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource: AbstractTemplateResource() {
    @Inject
    lateinit var postService: PostService

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData, securityContext: SecurityContext, pageData: PageData<PostSummary>): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData, securityContext: SecurityContext, postDetail: PostDetail): TemplateInstance
    }

    @GET
    suspend fun listPosts(
        @QueryParam("category") category: String?,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("6") show: Int
    ): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        val pageData = postService.getExistedPostSummary(authorId, category, true, page, show)
        return Templates.posts(metaData, securityContext, pageData)
    }

    @Path("/posts/{postId}")
    @GET
    suspend fun showPostDetail(
        @PathParam("postId") postId: String
    ): TemplateInstance {
        val postDetail = postService.getExistedPostDetail(postId, true)
        val metaData = buildHTMLMetaData(
            title = """"BLOG-${postDetail.title}""",
            type = "article",
            description = postDetail.content ?: ""
        )
        return Templates.postDetail(metaData, securityContext, postDetail)
    }
}
```

layout.html
```html
...
        <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav ml-auto">
                {#if !securityContext.isAuthenticated()}
                <li class="nav-item">
                    <a class="nav-link" href="/login">登入</a>
                </li>
                {/if}
                {#if securityContext.isAuthenticated()}
                <li class="nav-item">
                    <a class="nav-link" href="/logout">登出</a>
                </li>
                {/if}
            </ul>
        </div>
...
```

這時候在重新整理就會發現導覽列右上角出現登入按鈕，我們點擊後發現找不到頁面，這是因為我們還沒完成我們的登入頁面。
我們接下來繼續完成登入頁與登出功能

* 在 application.properties 設定 登入頁面、登入成功頁面、登入失敗頁面 路徑
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/resource/UserResource 
* 在 src/main/kotlin/net/aotter/quarkus/tutorial/util/template/RouteExtensions 新增 getRouteQueryParam 方法用來取得請求參數
* 創建 src/main/resources/templates/UserResource/login.html

application.properties
```properties
...
quarkus.http.auth.form.landing-page = /
quarkus.http.auth.form.login-page = /login
quarkus.http.auth.form.error-page = /login?error=true
...
```
* landing-page 是登入成功要倒轉到的路徑
* login-page 是我們要完成的登入頁面路徑
* error-page 是登入失敗要倒轉到的頁面，我們這邊帶一個 error 參數，讓模板判斷顯示錯誤訊息

UserResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import org.eclipse.microprofile.config.inject.ConfigProperty
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.*

@Path("/")
@Produces(MediaType.TEXT_HTML)
class UserResource: AbstractTemplateResource() {
    @ConfigProperty(name = "quarkus.http.auth.form.cookie-name")
    lateinit var cookieName: String

    @ConfigProperty(name = "quarkus.http.auth.form.location-cookie")
    lateinit var redirectCookieName: String

    @ConfigProperty(name = "quarkus.http.auth.form.login-page")
    lateinit var loginPage: String


    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun login(metaData: HTMLMetaData, securityContext: SecurityContext): TemplateInstance
    }

    @GET
    @Path("/login")
    fun login(): TemplateInstance{
        val metaData = buildHTMLMetaData(
            title = "BLOG-登入",
            type = "website",
            description = "登入BLOG系統"
        )
        return Templates.login(metaData, securityContext)
    }

    @GET
    @Path("/logout")
    fun logout(): Response = Response
        .temporaryRedirect(UriBuilder.fromPath(loginPage).build())
        .cookie(NewCookie(cookieName, null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
        .cookie(NewCookie(redirectCookieName, null, "/", null, NewCookie.DEFAULT_VERSION, null, 0, false))
        .build()
}
```
* 一樣繼承 AbstractTemplateResource
* 一樣創建 Templates 處理 qute 模板
* login 方法顯示登入頁面
* logout 就是將 cookie 清掉後倒轉到登入頁面
* 透過 ConfigProperty 取得 properties 設定的值

RouteExtensions.kt
```kotlin
...
      @JvmStatic
        fun getRouteQueryParam(request: HttpServerRequest, parameterName: String): String? = request.getParam(parameterName)
...
```
* 新增 getRouteQueryParam 在模板取得請求的參數

login.html
```html
{#include layout}
{#main}
<main role="main" class="container">
  <h1 class="my-5 text-center">登入</h1>
  <form action="j_security_check" method="POST">
    <div class="form-group row">
      <label for="username" class="col-sm-2 col-form-label">帳號</label>
      <div class="col-sm-10">
        <input type="text" class="form-control" id="username" name="j_username">
      </div>
    </div>
    <div class="form-group row">
      <label for="password" class="col-sm-2 col-form-label">密碼</label>
      <div class="col-sm-10">
        <input type="password" class="form-control" id="password" name="j_password">
      </div>
    </div>
    {#if inject:vertxRequest.getRouteQueryParam("error") == "true"}
    <p class="text-center text-danger">帳號或密碼錯誤</p>
    {/if}
    <button class="btn btn-primary btn-block">登入</button>
    <a class="btn btn-light btn-block" href="/signup">註冊會員</a>
  </form>
</main>
{/}
{/}
```
* 一樣使用 qute 的 include 引入 layout
* 這邊 form action 是預設值，可以透過設定 quarkus.http.auth.form.post-location 替換
* 同樣道理帳號密碼的參數名稱也是預設的，可以透過設定 quarkus.http.auth.form.username-parameter、quarkus.http.auth.form.password-parameter 替換
* 透過判斷請求參數是否帶有 error 用來顯示登入錯誤訊息

我們這樣就完成了系統的登入和登出，當登入失敗時也會看到錯誤訊息

## 會員註冊
再來我們要完成會員註冊頁面，在登入頁面中會員註冊的連結目前還沒完成。
我們會在註冊頁面透過 AJAX 發送請求註冊帳號，伺服器端要驗證請求的參數並完成會員資料的儲存。
為了完成參數的驗證我們額外引入 hibernate validator 幫助我們完成。

#### 加入 quarkus hibernate validator extension
可以透過以下兩種方式加入

Maven plugin
```shell
./mvnw quarkus:add-extension -Dextensions="io.quarkus:quarkus-hibernate-validator"
```

add pom.xml dependency
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-validator</artifactId>
</dependency>
```
 
#### 會員註冊頁面
再來我們先完成會員註冊的頁面

* 在 UserResource 的 Templates 新增 signup 方法對應 qute 模板
* 在 UserResource 新增方法 signup 完成會員註冊頁面
* 修改 layout insert script section
* 新增 src/main/resources/templates/UserResource/signup.html

UserResource.kt
```kotlin
...
    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun login(metaData: HTMLMetaData, securityContext: SecurityContext): TemplateInstance
        @JvmStatic
        external fun signup(metaData: HTMLMetaData, securityContext: SecurityContext): TemplateInstance
    }

    @GET
    @Path("/signup")
    fun signup(): TemplateInstance{
        val metaData = buildHTMLMetaData(
            title = "BLOG-註冊會員",
            type = "website",
            description = "註冊BLOG系統會員"
        )
        return Templates.signup(metaData, securityContext)
    }
...
```

layout.html
```html
<!-- jQuery and Bootstrap Bundle (includes Popper) -->
<script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/js/bootstrap.bundle.min.js" integrity="sha384-fQybjgWLrvvRgtW6bFlB7jaZrFsaBXjsOMm/tB9LTS58ONXgqbR9W8oWht/amnpF" crossorigin="anonymous"></script>
{#insert script}{/}
</body>
</html>
```
* 在 script 最後新增 insert script 區塊，讓我們可以撰寫 javascript

signup.html
```html
{#include layout}
{#main}
<main role="main" class="container">
    <h1 class="my-5 text-center">註冊會員</h1>
    <form id="signup-form">
        <div class="form-group row">
            <label for="username" class="col-sm-2 col-form-label">帳號</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="username">
            </div>
        </div>
        <div class="form-group row">
            <label for="password" class="col-sm-2 col-form-label">密碼</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="password">
            </div>
        </div>
        <div class="form-group row">
            <label for="checked-password" class="col-sm-2 col-form-label">確認密碼</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="checked-password">
            </div>
        </div>
        <button type="submit" class="btn btn-primary btn-block">註冊</button>
        <a class="btn btn-light btn-block" href="/login">登入</a>
    </form>
</main>
{/}
{#script}
<script>


</script>
{/}
{/}
```
* 我們之後的 javascript 就可以寫到這個區塊

#### 會員註冊 API

再來我們要完成會員註冊的 API ,接收帳號、密碼、確認密碼，並驗證參數是否符合限制，最後儲存會員資料。

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/dto/SignupRequest 
* 在 SignupRequest field 使用 annotation 設定驗證條件
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/vo/ApiResponse ，用來統一 API 回傳資料格式
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/resource/api/UserApiResource 
* 在 signup.html 引入 axios 發送 AJAX POST 請求到 API

SignupRequest.kt
```kotlin
package net.aotter.quarkus.tutorial.model.dto

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.Size

data class SignupRequest(
    @field:[
        NotEmpty(message = "使用者名稱不得為空")
    ]
    var username: String,
    @field:[
        NotEmpty(message = "密碼不得為空")
        Size(min = 8, max = 16, message = "密碼長度需為 8 到 16")
    ]
    var password: String,
    @field:[
        NotEmpty(message = "確認密碼不得為空")
        Size(min = 8, max = 16, message = "密碼長度需為 8 到 16")
    ]
    var checkedPassword: String
)
```
* 使用 annotation 設定驗證條件

ApiResponse.kt
```kotlin
package net.aotter.quarkus.tutorial.model.vo

import com.fasterxml.jackson.annotation.JsonInclude

data class ApiResponse<T>(
    var message: String,
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    var data: T? = null
)
```
* 使用 jackson annotation 設定 data 為空值的話不序列化

UserApiResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource.api

import net.aotter.quarkus.tutorial.model.dto.SignupRequest
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/api")
class UserApiResource {

    @Path("/user")
    @POST
    suspend fun signup(@Valid request: SignupRequest): ApiResponse<Void>{
        return ApiResponse(message = "會員註冊成功")
    }
}
```
* 透過 @Valid 自動驗證 SignupRequest

signup.html
```html
...
{#script}
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<script>
    $(function(){
        $('#signup-form').on('submit', function(e){
            e.preventDefault()
            signup()
        })

        function signup(){
            var data = {
                username: $('#username').val().trim(),
                password: $('#password').val().trim(),
                checkedPassword: $('#checked-password').val().trim()
            }
            axios.post('/api/user', data)
            .then(function(response){
                var res = response.data
                console.log(res)
            })
            .catch(function(error){
                var res = error.response.data
                console.log(res)
            })
        }
    })
</script>
{/}
...
```
* 當表單送出時取消預設行為，使用 axios 發送 AJAX 請求
* 使用 console.log 印出回傳資料

我們什麼資料都不輸入直接送出表單看到回傳以下錯誤訊息
```json
{
  "title": "Constraint Violation",
  "status": 400,
  "violations": [
    {
      "field": "signup.request.password",
      "message": "密碼長度需為 8 到 16"
    },
    {
      "field": "signup.request.checkedPassword",
      "message": "密碼長度需為 8 到 16"
    },
    {
      "field": "signup.request.checkedPassword",
      "message": "確認密碼不得為空"
    },
    {
      "field": "signup.request.username",
      "message": "使用者名稱不得為空"
    },
    {
      "field": "signup.request.password",
      "message": "密碼不得為空"
    }
  ]
}
```
我們接下來處理 hibernate validator 錯誤訊息顯示到表單上。

* 修改 signup.html ，在表單的輸入下新增 div 並設定 invalid-feedback class 用來顯示錯誤訊息
* 修改 signup 方法，將對應的錯誤訊息顯示到對應的區塊，成功時顯示成功訊息並倒轉到登入頁

signup.html
```html
{#include layout}
{#main}
<main role="main" class="container">
    <h1 class="my-5 text-center">註冊會員</h1>
    <form id="signup-form" class="needs-validation" novalidate>
        <div class="form-group row">
            <label for="username" class="col-sm-2 col-form-label">帳號</label>
            <div class="col-sm-10">
                <input type="text" class="form-control" id="username">
                <div id="username-feedback" class="invalid-feedback">
                </div>
            </div>

        </div>
        <div class="form-group row">
            <label for="password" class="col-sm-2 col-form-label">密碼</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="password">
                <div id="password-feedback" class="invalid-feedback">
                </div>
            </div>

        </div>
        <div class="form-group row">
            <label for="checked-password" class="col-sm-2 col-form-label">確認密碼</label>
            <div class="col-sm-10">
                <input type="password" class="form-control" id="checked-password">
                <div id="checked-password-feedback" class="invalid-feedback">
                </div>
            </div>
        </div>
        <div id="error-message" class="alert alert-danger d-none" role="alert">
        </div>
        <button type="submit" class="btn btn-primary btn-block">註冊</button>
        <a class="btn btn-light btn-block" href="/login">登入</a>
    </form>
</main>
{/}

{#script}
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
<script>
    $(function(){
        $('#signup-form').on('submit', function(e){
            e.preventDefault()
            signup()
        })

        function signup(){
            $('#username').removeClass('is-invalid')
            $('#password').removeClass('is-invalid')
            $('#checked-password').removeClass('is-invalid')

            $('#username-feedback').empty()
            $('#password-feedback').empty()
            $('#checked-password-feedback').empty()

            var data = {
                username: $('#username').val().trim(),
                password: $('#password').val().trim(),
                checkedPassword: $('#checked-password').val().trim()
            }
            axios.post('/api/user', data)
            .then(function(response){
                var res = response.data
                alert(res.message)
                window.location.href = '/login'
            })
            .catch(function(error){
                var res = error.response.data
                console.log(res)
                if(res.title === 'Constraint Violation'){
                    res.violations.forEach(function(violation){
                        handleErrorMessage(violation)
                    })
                }else{
                    alert(res.message)
                }
            })

        }

        function handleErrorMessage(violation){
            var mapping = {
                "signup.request.username": "username",
                "signup.request.password": "password",
                "signup.request.checkedPassword": "checked-password"
            }
            var field = mapping[violation.field]
            $('#' + field).addClass('is-invalid')
            $('#' + field + '-feedback').append('<p class="mb-0">' + violation.message + '</p>')
        }
    })
</script>
{/}

{/}
```
* 使用 handleErrorMessage 方法將錯誤訊息顯示到對應的區塊
* 錯誤透過 title 區分是否是 hibernate validator 錯誤，是的話就顯示錯誤訊息到表單上
* 其他錯誤使用 alert 通知使用者
* 會員註冊成功顯示成功訊息並倒轉到登入頁

我們再次發送空白表單，此時會發現錯誤訊息都顯示在對應的位置，
再來輸入想要註冊的會員資訊再次發送表單，這時跳出會員註冊成功訊息並跳轉回登入頁。
我們完成了前端的部分但此時後端並沒有真正完成，我們繼續完成後端程式。

#### ServerExceptionMapper
首先我們先設計基礎的 Exception ，並使用 ServerExceptionMapper 完成全局的錯誤處理，
1. BusinessException 當違反業務邏輯時要拒絕請求，可以在 service 層直接丟出
2. DataException 有時候只想處理資料庫的某些異常像是違反唯一索引，其他資料庫的異常就包裹後直接丟出

在創建 GlobalExceptionMapper 使用 ServerExceptionMapper 全局處理異常

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/exception/BusinessException
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/exception/DataException
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/resource/api/GlobalExceptionMapper
* 在 GlobalExceptionMapper 使用 ServerExceptionMapper 處理異常
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/service/UserService
* 在 UserService 新增 createUser 方法處理會員新增的業務邏輯
* 修改 UserResource 調用 createUser

BusinessException.kt
```kotlin
package net.aotter.quarkus.tutorial.model.exception

open class BusinessException: RuntimeException{
    constructor(message: String): super(message)
}
```
DataException.kt
```kotlin
package net.aotter.quarkus.tutorial.model.exception

open class DataException: RuntimeException {
    constructor(message: String): super(message)
}
```
* 這兩個自訂異常都設為 open 允許繼承，若是之後還要細分可以創立新的異常並繼承

GlobalExceptionMapper.kt
```kotlin
package net.aotter.quarkus.tutorial.resource.api

import net.aotter.quarkus.tutorial.model.exception.BusinessException
import net.aotter.quarkus.tutorial.model.exception.DataException
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import org.jboss.logging.Logger
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import javax.inject.Inject
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class GlobalExceptionMapper {
    @Inject
    lateinit var logger: Logger

    @ServerExceptionMapper
    fun businessException(e: BusinessException): Response = Response.status(Response.Status.BAD_REQUEST)
        .type(MediaType.APPLICATION_JSON)
        .entity(ApiResponse<Unit>(message = e.message ?: ""))
        .build()

    @ServerExceptionMapper
    fun dataException(e: DataException): Response {
        logger.error(e.message)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiResponse<Unit>(message = "資料存取錯誤，請稍後再試或洽管理員"))
            .build()
    }

    @ServerExceptionMapper
    fun exception(e: Exception): Response{
        logger.error(e.message)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(ApiResponse<Unit>(message = "系統異常，請稍後再試或洽管理員"))
            .build()
    }
}
```
* 業務邏輯錯誤返回 400 錯誤，統一使用 ApiResponse 包裹異常訊息返回
* 資料錯誤返回 500 錯誤，並打印實際錯誤訊息讓工程師查看，但返回給前端的訊息是給使用者看的
* 最後指定 Exception 將所有意想不到的錯誤都統一處理，一樣打印實際錯誤訊息並返回使用者提示

UserService.kt
```kotlin
package net.aotter.quarkus.tutorial.service

import com.mongodb.MongoWriteException
import io.quarkus.elytron.security.common.BcryptUtil
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.exception.BusinessException
import net.aotter.quarkus.tutorial.model.exception.DataException
import net.aotter.quarkus.tutorial.model.po.User
import net.aotter.quarkus.tutorial.repository.UserRepository
import net.aotter.quarkus.tutorial.security.Role
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class UserService {

    @Inject
    lateinit var userRepository: UserRepository

    suspend fun createUser(username: String, password: String, checkedPassword: String){
        if(password != checkedPassword){
            throw BusinessException("密碼與確認密碼不相符")
        }
        val user = User(
            username = username,
            credentials = BcryptUtil.bcryptHash(password),
            role = Role.USER,
            deleted = false,
        )
        try{
            userRepository.persist(user).awaitSuspending()
        }catch (e: MongoWriteException){
            if(e.error.code == 11000){
                throw BusinessException("使用者名稱已存在")
            }else{
                throw DataException(e.message ?: "")
            }
        }
    }
}
```
* 由於有全局的錯誤處理，在 createUser 方法中，若密碼與確認密碼不相符直接丟出業務邏輯錯誤拒絕請求
* 我們在儲存資料到 MongoDB 時，由於有在 username 設定唯一索引，所以當使用者名稱重複時會拋出異常
* 我們只想要處理使用者名稱重複時的異常，所以處理 MongoWriteException 且 error code 是 1100 時丟出業務異常
* 並將其他意想不到的資料庫異常包裹成 DataException 丟出

UserApiResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource.api

import net.aotter.quarkus.tutorial.model.dto.SignupRequest
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.service.UserService
import javax.inject.Inject
import javax.validation.Valid
import javax.ws.rs.POST
import javax.ws.rs.Path

@Path("/api")
class UserApiResource {
    @Inject
    lateinit var userService: UserService

    @Path("/user")
    @POST
    suspend fun signup(@Valid request: SignupRequest): ApiResponse<Void>{
        userService.createUser(request.username, request.password, request.checkedPassword)
        return ApiResponse(message = "會員註冊成功")
    }
}
```
* 注入 UserService 使用 crateUser 簡單完成會員註冊的功能

我們這樣就完成了應用程式的安全性，並完成了我們的會員註冊、會員登入功能，也了解到了 Quarkus Security 的基本運作