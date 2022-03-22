# Integrate Vue
### 說明
* 上個步驟使用純 HTML / JS 做前端畫面，這個步驟要用 Vue 改寫前端
* 整合 Vue 與 Quarkus 前後端開發，建立測試登入用的前端頁面
    * 參考：[Build, run and deploy Vuejs app with Quarkus](https://medium.com/@dmi3coder/build-run-and-deploy-vuejs-app-with-quarkus-d6d1ae94ced9)
        * 前端專案位於 src/main/webapp 資料夾，編譯完的檔案會在 src/main/resources/META-INF/resources 下


### 步驟
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
    * 在 package.json 內設定 Vue 編譯完的檔案位置，讓 Quarkus app 可以取用
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
        * Signup，註冊頁面，註冊成功後重導至 Login
        * Login，登入頁面，登入成功重導至 Home，失敗重導至 Error
        * Error，登入失敗的重導頁面
        * Header，依登入狀態顯示不同的 router-link
            * 未登入：顯示 Signup, Login
            * 已登入：顯示 Logout
5. 設定 Quarkus Form Based Authentication error 的重導路徑
    * 參考 [Configuration Property](https://quarkus.io/guides/security-built-in-authentication#form-auth)
    * 設定 src/main/resources/application.properties
        ```
        quarkus.http.auth.form.error-page = /index.html#/error
        ```