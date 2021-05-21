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

2. 在 src/main/resources/application.properties 內設定允許 Quarkus 的 form authentication
    ```
    quarkus.http.auth.form.enabled = true
    ```

3. 整合 vue 與 Quarkus 前後端開發，建立測試登入用的前端頁面
   * 參考：[Build, run and deploy Vuejs app with Quarkus](https://medium.com/@dmi3coder/build-run-and-deploy-vuejs-app-with-quarkus-d6d1ae94ced9)
      * 前端專案位於 src/main/webapp 資料夾，編譯完的檔案會在 src/main/resources/META-INF/resources 下
   * 根據 [Quarkus 官方文件](https://quarkus.io/guides/security-built-in-authentication#form-auth)，準備這三個頁面：
      * login.html, index.html, error.html
      * 因專案重點在後端程式碼，請直接在 src/main/webapp/public 下新增頁面
     