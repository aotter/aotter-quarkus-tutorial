# Clam Endpoint
### 說明
* 參照自現有的服務 Aotter Clam，是一組萬用的 CRUD api，透過請求參數決定要操作的 collection，也不限制 request body 的資料長怎樣
* api 規格：

  | method | path | description |  roles allowed |
      | -------- | -------- | -------- | -------- |
  | POST     | /api/{collectionName}     | 建立新的 entity, 初始 State 為 TEMP    | ADMIN |
  | PUT     | /api/{collectionName}/{id}     | 更新 entity 與 lastModifiedTime   | ADMIN |
  | PUT     | /api/{collectionName}/{id}/state/{state}     | 更新 entity State 與 lastModifiedTime, 如果更新 State 為 PUBLISHED 而且 publishedTime 為 null 則 set publishedTime  | ADMIN |
  | DELETE     | /api/{collectionName}/{id}     | 更新 entity State 為 Archived 與 lastModifiedTime    | ADMIN |
  | GET     | /api/{collectionName}/{id}     | 取得特定 id 的 entity 如果 State 為 PUBLISHED   | ADMIN, USER |
  | GET     | /api/{collectionName}     | 取得 collection 中所有 State 為 PUBLISHED 的 entity   | ADMIN, USER

### 流程
1. 建立 ClamEndpoint
    * 套件位置：src.main.kotlin.endpoint
    * 繼承 BaseRoute
2. 建立 abstract class ClamSetup
    * 套件位置：src.test.kotlin.setup
    * 因 Clam 的 unit test 前置流程較為繁瑣
        * 將 @BeforeEach 與 @AfterEach 的方法抽到 abstract class
        * ClamRepositoryTest 和 ClamRouteTest 將繼承 ClamSetup
2. 建立 ClamRoute 的 unit test, ClamRouteTest
    * 套件位置：src.test.kotlin.route
    * 繼承 ClamSetup
    * 使用 [rest-assured](https://github.com/rest-assured/rest-assured/wiki/GettingStarted) 做 API 的測試
    * security 測試額外的設定
        * 參考：[QUARKUS - SECURITY TESTING](https://quarkus.io/guides/security-testing)    
