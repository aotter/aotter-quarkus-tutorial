# User Endpoint

### 流程
1. 建立 abstract class BaseRoute
    * 套件位置：src.main.kotlin.route
    * 抽出 endpoint 共同的方法

2. 建立 UserEndpoint
    * 套件位置：src.main.kotlin.endpoint
    * 繼承 BaseRoute
    * api 端點以 @RolesAllowed 註解做權限保護
    * 端點：
        * signUp
        * getUser (限定 ADMIN, USER 存取)
        * getAdmin (限定 ADMIN 存取)
        * logout
    * 透過 SecurityContext 可存取權限相關資訊

2. 在 src/main/resources/application.properties 內設定允許 Quarkus form authentication 與 encryption-key
    ```
    quarkus.http.auth.form.enabled = true
    quarkus.http.auth.session.encryption-key= moXBxFqO1fUCtcYNsQAEWEjm0AXM84kgpi7HKDePq+k=
    ```

3. 建立前端測試頁面
    * 檔案位置：src.main.resources.META-INF.resources
    * 需求頁面：
        * index.html：帳號註冊、登出
        * login.html：登入
        * error.html：登入錯誤導向頁面
     