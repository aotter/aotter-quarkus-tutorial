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
                user?.takeIf { BcryptUtil.matches(String(password), user?.credentials) }
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