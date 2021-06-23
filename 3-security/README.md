# Security
### 說明
* 透過 HttpAuthenticationMechanism，Quarkus 會從 HTTP request 中獲取可驗證的憑證，再交由 [IdentityProvider](https://quarkus.io/guides/security#identity-providers) 將憑證轉換為 SecurityIdentity
* 這邊使用 [Form Based Authentication](https://quarkus.io/guides/security-built-in-authentication#form-auth)
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
