# **概覽**

### 說明

從零開始建立 quarkus 專案， 目標是完成一個可以讓用戶註冊登入後，透過後台發布文章，非用戶可在前台觀看所有已發布文章。管理者則可以在後台系統查詢所有文章或用戶。

### 內容包含

1. 後端 -
   * 語言: kotlin
   * 資料庫: MongoDB
   * 資料庫 ORM框架: [Panache](https://quarkus.io/guides/mongodb-panache#query-projection) 跟 [MongoDB Reactive Streams Java Driver](https://mongodb.github.io/mongo-java-driver-reactivestreams/)
   * 功能列: 
       * 基本功能的 [reactive CRUD](https://quarkus.io/guides/getting-started-reactive)
       * 實作 [IdentityProvider](https://quarkus.io/guides/security#identity-providers) ，驗證使用者資訊
   * 建立單元測試
2. 前端 - 
   * 框架: [Quarkus Qute](https://quarkus.io/guides/qute) 跟 [Vue](https://vuejs.org/)
   * 頁面 - 
     * Home，預設與登入成功後的重導頁面，顯示當前登入狀態
     * Signup，註冊頁面，註冊成功後重導至 Login
     * Login，登入頁面，登入成功重導至 Home，失敗重導至 Error
     * BackStage，一般使用者顯示為文章發佈頁，管理者顯示使用者及文章管理頁
     * Error，登入失敗的重導頁面
3. 部署相關 - 
   * [Quarkus jib](https://quarkus.io/guides/container-image#jib)
   * [GitHub Actions](https://github.com/features/actions)
