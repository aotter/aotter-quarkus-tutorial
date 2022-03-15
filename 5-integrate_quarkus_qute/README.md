# Integrate Quarkus Qute

### 說明
* 上個步驟使用純 HTML / JS 做前端畫面，這個步驟要用 Quarkus Qute 改寫前端
* 整合 QUTE 與 Quarkus 前後端開發，建立文章列表及內容頁
   * 參考：[QUTE TEMPLATING ENGINE](https://quarkus.io/guides/qute)
   * 參考：[QUTE REFERENCE GUIDE](https://quarkus.io/guides/qute-reference)
      * 前端專案位於 src/main/resources/templates 資料夾下


### 步驟
1. 前端環境設置
   * 加入 [quarkus-resteasy-reactive-qute](https://mvnrepository.com/artifact/io.quarkus/quarkus-resteasy-reactive-qute) dependency
   2. 在 src/main/resources 下建立 templates

3. 撰寫前端頁面 article-list.html 以及 articles/article.html