# Integrate Quarkus Qute

### 說明
* 上個步驟使用純 HTML / JS 做前端畫面，這個步驟要用 Quarkus Qute 改寫前端
* 整合 QUTE 與 Quarkus 前後端開發，建立文章列表及內容頁
   * 參考：[QUTE TEMPLATING ENGINE](https://quarkus.io/guides/qute)
   * 參考：[QUTE REFERENCE GUIDE](https://quarkus.io/guides/qute-reference)
      * 前端專案位於 src/main/resources/templates 資料夾下


### 流程
1. 環境設置
   * 加入 [quarkus-resteasy-reactive-qute](https://mvnrepository.com/artifact/io.quarkus/quarkus-resteasy-reactive-qute) dependency
   * 確認 quarkus 的版本應為 2.1.0 以上
   * 將資料匯入MongoDB
     * [User.json](https://www.dropbox.com/s/wgvd1c03p8hy2ef/User.json?dl=0)
     * [Article.json](https://www.dropbox.com/s/znkgbtgtf5feyeh/Article.json?dl=0)
   
2. 建立 article 跟 articleList html
    * 套件位置：src.main.resources.templates.ArticleResource

3. 實作 findArticleListViaPage function
   * 套件位置：src.main.kotlin.repository.ArticleRepository

4. 建立 class ArticleResource 
    * 套件位置：src.main.kotlin.resource
    * 實作方法：
      * 將 Templates 以 static fun 加入（ 對應 html 檔案位置等於 class 名稱/ template function 名稱 eg. ArticleResource/article.html）
      * 新增 [GET][ /article ] 跟 [GET][ /article-list ]

