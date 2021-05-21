# Security
### 說明
* 透過 HttpAuthenticationMechanism，Quarkus 會從 HTTP request 中獲取可驗證的憑證，再交由 [IdentityProvider](https://quarkus.io/guides/security#identity-providers) 將憑證轉換為 SecurityIdentity
* 這邊使用 [Form Based Authentication](https://quarkus.io/guides/security-built-in-authentication#form-auth)
* 提供實作 [Quarkus IdentityProvider](https://quarkus.io/guides/security#identity-providers) 的類別，Quarkus 會透過類別實作的 authenticate( ) 方法驗證使用者身份
* 參考：[QUARKUS - SECURITY ARCHITECTURE AND GUIDES
  ](https://quarkus.io/guides/security)

### 流程
1. 加入 [quarkus-security](https://mvnrepository.com/artifact/io.quarkus/quarkus-security) dependency
1. 建立 extension file，用來定義 vertx 的擴充方法，包裝 coroutine 的使用
    * 套件位置：src.main.kotiln.util

2. 建立 abstract class AbstractMongoIdentityProvider
    * 套件位置：src.main.kotlin.security
    * 抽出實作 IdentityProvider 的類別共同的方法：
        * buildSecurityIdentityProvider( ): SecurityIdentityProvider

3. 建立 MongoIdentityProvider
    * 套件位置：src.main.kotlin.security
    * 繼承 AbstractMongoIdentityProvider
    * 需註解為 @ApplicationScoped
    * 實作 IdentityProvider<TrustedAuthenticationRequest>
    * 處理來自可信任的來源（例如加密的cookie）的身份驗證請求
    * override 方法：
        * getRequestType( )
        * authentication( )
    
3. 建立 MongoTrustedIdentityProvider
    * 套件位置：src.main.kotlin.security
    * 繼承 AbstractMongoIdentityProvider
    * 需註解為 @ApplicationScoped
    * 實作 IdentityProvider<UsernamePasswordAuthenticationRequest>
    * 處理使用用戶名和密碼的簡單身份驗證請求
    * override 方法：
        * getRequestType( )
        * authentication( )