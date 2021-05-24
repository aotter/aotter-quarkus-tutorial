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
