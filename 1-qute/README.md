# 1-qute

## 前言
經過前章節 0-init ，相信對於 Quarkus 有初步的了解。  
回到我們一開始的專案規劃，我們來完成讓非用戶觀看所有已發布文章的功能。  
針對這個功能我們規劃為以下兩頁

1. 瀏覽發布的文章  
* url path: /
* 可以查看所有發布的文章
* 能夠過關鍵字篩選文章 ex. 作者、分類
* 需要有分頁
* 只顯示必要資訊 ex. 分類、標題、作者、最後更新時間

2. 查看發布文章的詳細內容
* url path: /posts/{postId}
* 顯示完整資訊

為了 SEO 需要加入 meta tag

## First Page
* 在開始寫前我們先調整一下專案結構，將 root package 調整為 net.aotter.quarkus.tutorial 底下創建 resource package 擺放關於 JAX-RS 的 resource
* 前一章節的範例 GreetingResource 還有 test 因為用不到所以可以直接刪除
* 預設的 index.html 也不用了直接刪除即可

創建 PostResource.kt 在 net.aotter.quarkus.tutorial.resource 底下，此時專案結構會長的如下

```
├── src
│  ├── main
│  │  ├── docker
│  │  │   ├── Dockerfile.jvm
│  │  │   ├── Dockerfile.legacy-jar
│  │  │   ├── Dockerfile.native
│  │  │   └── Dockerfile.native-micro
│  │  ├── kotlin
│  │  │   └── net
│  │  │      └── aotter
│  │  │          └── quarkus
│  │  │              └── tutorial
│  │  │                  └── resource
│  │  │                      └── PostResource.kt
│  │  └── resources
│  │      ├── META-INF
│  │      │  └── resources
│  │      └── application.properties
│  └── test
│      └── kotlin
│          └── net
│              └── aotter
```

****

* 我們在 PostResource.kt 新增 listPosts method 回傳 html 格式的文字
* @Path("/") 指定 resource mapping path
* 規劃 PostResource 負責回傳 html 頁面，直接在 class 上標注 @Produces(MediaType.TEXT_HTML) 指定資源可以生成及回應客戶端的 MIME media types 為 text/html
* @GET 指定 HTTP method 為 GET ，瀏覽器在向 server 請求 html 頁面是發送 GET 請求
```kotlin
package net.aotter.quarkus.tutorial.resource

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {
    
    @GET
    fun listPosts(): String =
        """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="X-UA-Compatible" content="IE=edge">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>BLOG</title>
            </head>
            <body>
                <h1>First Page</h1>
            </body>
            </html>
        """.trimIndent()
}
```
****

由於開發模式熱重載，直接打開瀏覽器訪問 http://localhost:8080 ，就可以看到畫面顯示 First Page。  
雖然達成目的，但直接將 html 寫死在程式碼，可讀性非常差也不好維護，上面範例只是靜態的 html ，若是還要動態結合資料會更加複雜。  
要怎麼解決這個問題呢？就要介紹到這章節的重點 Qute


## Qute 介紹

Qute 是專為 Quarkus 設計的 Templating Engine (模板引擎)。  
模板引擎可以動態渲染出 Html ,也會提供變數、條件、迴圈功能來撰寫更簡潔的程式碼，能夠更加輕鬆維護程式碼。

#### 首先我們要加入 quarkus-resteasy-reactive-qute extension
可以透過以下兩種方法加入
1. Maven plugin
```shell
./mvnw quarkus:add-extension -Dextensions="io.quarkus:quarkus-resteasy-reactive-qute"
```
2. 修改 pom.xml 加上 dependency ,由於 quarkus-bom 已經幫我們管理 quarkus extension 的版本所以這邊不需要指定 version
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-resteasy-reactive-qute</artifactId>
</dependency>
```

默認情況下，在 src/main/resources/templates 底下的文件會被註冊為模板。  
我們創建 templates 資料夾，並在其底下創建 posts.html，將原本寫死在 PostResource.kt 的字串移到 posts.html。  
將 h1 tag 的內容改成 {title} ，這是一個表達式會在模板渲染時計算。

src/main/resources/templates/posts.html
```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BLOG</title>
</head>
<body>
    <h1>{title}</h1>
</body>
</html>
```
* 透過 @Inject 注入模板，這邊是透過 quarkus-arc 提供的 Dependency Injection (倚賴注入) 達成，我們下面解釋
* 如果沒有使用 @Location 指定位置就會使用變數名去定位，這個例子我們會注入這個路徑的模板 templates/posts.html
* 我們將原本 listPosts method 回傳值改為 TemplateInstance
* Template.data() 會回傳 template instance, 我們可以在它渲染前做些操作設定，例如我們將設定一組 key map,設定的資料可以在渲染時被模板取用
* 我們不需要手動觸發渲染，他會自動在 ContainerResponseFilter 中完成

src/main/kotlin/net/aotter/quarkus/tutorial/resource/PostResource
```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {

    @Inject
    lateinit var posts: Template

    @GET
    fun listPosts(): TemplateInstance = posts.data("title", "First Qute")
}
```

我們再次透過瀏覽器訪問 http://localhost:8080  
會發現標題已經改為我們設定的 First Qute 了

## Type-safe templates
除了上面提到的方式，還有另外一種方式來使用模板，它依賴於以下約定

* 對於 resource 所參考的模板放置的位置為 src/main/resources/templates/{resourceName}/{templateName}。
以我們的例子來說， PostResource.kt 中使用的 posts.html 應該為 /src/main/resources/templates/PostResource/posts.html
* 每個 resource 需要宣告 static class Templates {} 加上 @CheckedTemplate (在 kotlin 使用 object 達到 static class)
* 對每個 template file 宣告 public static native TemplateInstance method() (在 kotlin 使用 @JvmStatic external fun method(): TemplateInstance )
* 使用宣告的 static method 來建構 template instance

我們使用 Type-safe templates 方式來改寫上面的例子  
將 posts.html 移到 src/main/resources/templates/PostResource/posts.html  
按照上面約定改寫 PostResource

src/main/kotlin/net/aotter/quarkus/tutorial/resource/PostResource
```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(): TemplateInstance
    }

    @GET
    fun listPosts(): TemplateInstance = Templates.posts().data("title", "First Qute")
}
```
我們重新整理瀏覽器就會發現出了問題，他告訴我們使用 type-safe template 時，我們在 posts.html 中使用的表達式 {title}  需要宣告為 external fun posts(): TemplateInstance 的參數。 
* 你可以通過 @CheckedTemplate(requireTypeSafeExpressions = false) 告訴它不要檢查  
* type-safe template 好處是經由他的檢查就不會出現忘記設定模板需要的資料的情況，所以這邊我們修改 posts() 將 title 做為參數，順便把 title 改成這頁的標題

```kotlin
@CheckedTemplate
object Templates{
    @JvmStatic
    external fun posts(title: String): TemplateInstance
}

@GET
fun listPosts(): TemplateInstance = Templates.posts("BLOG")
```
再次整理瀏覽器就會發現又正常了

## 容器與倚賴注入

#### 什麼是 Bean
bean 是一種 container-managed (受容器管理) 的物件，它提供了基本的功能像是 injection of dependencies (倚賴注入), lifecycle callbacks (生命週期的回調) and interceptors (攔截器)

#### 什麼是 Container 
* container 是應用程式執行的環境，他負責管理 bean 實例的創建與銷毀，並將 bean 指定的上下文關聯注入到其他 bean。
* 使用者不直接控制實例的生命週期，透過 annotation 或設定去影響他
* 帶來的好處是開發人員無需關注 bean 在哪裡創建要如何取得，可以專注在業務邏輯上

#### 如何使用 Bean 與注入 Bean
* 有不同種類的 bean 可以透過 scope annotation 標註告訴 container 這個 bean 的實例與哪個上下文關聯
* 透過 @Inject 指定注入點，告訴容器這個 bean 倚賴哪些其他 bean
* 除了 field 還可以使用建構子注入(常規的 CDI 實作不支持，Quarkus 動了點手腳)、
[initializer methods](https://jakarta.ee/specifications/cdi/2.0/cdi-spec-2.0.html#initializer_methods)
(取代 setter inject)
* 注入是通過 bean type ，如果有相同的 bean type 存在可以透過內建的 Qualifier annotations @Named 指定注入 bean 的名稱
* 也可以透過 @Default 設定預設注入的 bean
* 建議默認使用 @ApplicationScoped，除非有充分的理由使用 @Singleton

更詳細的介紹可以參考官方指引 [INTRODUCTION TO CONTEXTS AND DEPENDENCY INJECTION](https://quarkus.io/guides/cdi)

## Include Section
#### Static Resource
我們先為網站加入 favicon ，瀏覽器預設會去抓 /favicon.ico 路徑底下的圖示，  
上一章節有提到 quarkus 會映射 src/main/resources/META-INF.resources 底下的檔案
所以你可以直接將 favicon.ico 檔案放到 src/main/resources/META-INF.resources 即可。  
但這邊我們使用另一種方式，在 src/main/resources/META-INF.resources 底下創建 assets 資料夾管理靜態檔案 ex 圖片、js、css  
我們借用一下電獺官網的 [icon](https://aotter.net/assets/images/favicon.png) (請注意智慧財產權！！！)，  
下載在 asset 底下創建 images 資料夾專門擺放網站用到的圖片，將 icon 下載後放到資料夾中，路徑長這樣 src/main/resource/META-INF.resource/assets/images/favicon.png  
瀏覽器打開 http://localhost:8080/assets/images/favicon.png ，就會看到我們剛剛放進去的 icon
再來只要透過 <link rel="icon" href="/assets/images/favicon.png"> 指定我們 icon 路徑就好了

#### 我們使用 bootstrap 來刻畫面

posts.html
```html
<!doctype html>
<html lang="en">
<head>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
  <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
        integrity="sha384-zCbKRCUGaJDkqS1kPbPd7TveP5iyJE0EjAuZQTgFLD2ylzuqKfdKlfG/eSrtxUkn" crossorigin="anonymous">
  <title>{title}</title>
  <meta name="title" content="{title}">
  <meta name="description" content="BLOG 有很多精彩的文章">
  <!-- Open Graph / Facebook -->
  <meta property="og:type" content="website">
  <meta property="og:url" content="#">
  <meta property="og:title" content="{title}">
  <meta property="og:description" content="BLOG 有很多精彩文章">
  <meta property="og:image" content="#">
  
  <link rel="icon" href="/assets/images/favicon.png">  
</head>
<body>
<header>
  <!-- Fixed navbar -->
  <nav class="navbar navbar-expand-md navbar-dark bg-dark">
    <a class="navbar-brand" href="/">首頁</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarCollapse">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarCollapse">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item">
        </li>
      </ul>
    </div>
  </nav>
</header>

<main role="main" class="container">
  <h1 class="my-5 text-center">文章列表</h1>
  <div class="row">
    <div class="col-sm-12 col-lg-6 my-1">
      <div class="card">
        <div class="card-body">
          <p>
            <a class="my-5" href="#">類別一</a>
          </p>
          <h5 class="card-title">Test Title 1</h5>
          <p class="card-text">最後修改時間：2022-04-26 12:00:00</p>
          <p class="text-right mb-0">
            <a href="#" >閱讀更多</a>
          </p>
        </div>
      </div>
    </div>
    <div class="col-sm-12 col-lg-6 my-1">
      <div class="card">
        <div class="card-body">
          <p>
            <a class="my-5" href="#">類別二</a>
          </p>
          <h5 class="card-title">Test Title 2</h5>
          <p class="card-text">最後修改時間：2022-04-26 12:00:00</p>
          <p class="text-right mb-0">
            <a href="#" >閱讀更多</a>
          </p>
        </div>
      </div>
    </div>
  </div>
  <nav class="my-5">
    <ul class="pagination justify-content-center">
      <li class="page-item disabled"><a class="page-link" href="#">&laquo; 前一頁</a></li>
      <li class="page-item active"><a class="page-link" href="#">1</a></li>
      <li class="page-item disabled"><a class="page-link" href="#">後一頁 &raquo;</a></li>
    </ul>
  </nav>
</main>

<!-- jQuery and Bootstrap Bundle (includes Popper) -->
<script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/js/bootstrap.bundle.min.js" integrity="sha384-fQybjgWLrvvRgtW6bFlB7jaZrFsaBXjsOMm/tB9LTS58ONXgqbR9W8oWht/amnpF" crossorigin="anonymous"></script>

</body>
</html>
```

畫面結果

![posts 畫面](../image/1-qute/posts.png)

通常網頁的外框都是相同的，不同的只有內容。  
每次都複製貼上不僅麻煩，當你要改動其中一個部分時，所有檔案都要修改，容易發生忘記改的情況。
可以透過 qute 提供的 {#include} {#insert} 去模組化你的 template。
* 將 posts.html 中共用的部分提到新的檔案 src/main/resources/templates/layout.html 中
* 在 layout.html 將每頁不同的部分用 #{insert} 宣告 ex. main tag
* 在 posts.html include layout ，在指定覆蓋的區塊
* meta tag 的部分，雖然每個網頁都有所以放在 layout ，但每次需要設定不同的值。
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/vo/HTMLMetaData 用來紀錄 meta tag 的值
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/StringExtensions.kt 工具類，撰寫 abbreviate method，利用 kotlin extension method 擴充 String 方便我們省略字串
* 修改 PostResource method 將 HTMLMetaData 傳入模板，在 layout 使用

layout.html
```html
<!doctype html>
<html lang="en">
<head>
    <!-- Required meta tags -->
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <!-- Bootstrap CSS -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/css/bootstrap.min.css"
          integrity="sha384-zCbKRCUGaJDkqS1kPbPd7TveP5iyJE0EjAuZQTgFLD2ylzuqKfdKlfG/eSrtxUkn" crossorigin="anonymous">

    <title>{metaData.title}</title>
    <meta name="title" content="{metaData.title}">
    <meta name="description" content="{metaData.description}">
    <!-- Open Graph / Facebook -->
    <meta property="og:type" content="{metaData.type}">
    <meta property="og:url" content="{metaData.url}">
    <meta property="og:title" content="{metaData.title}}">
    <meta property="og:description" content="{metaData.description}">
    <meta property="og:image" content="{metaData.image}">

    <link rel="icon" href="/assets/images/favicon.png">
</head>
<body>
<header>
    <!-- Fixed navbar -->
    <nav class="navbar navbar-expand-md navbar-dark bg-dark">
        <a class="navbar-brand" href="/">首頁</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarCollapse">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item">
                </li>
            </ul>
        </div>
    </nav>
</header>

{#insert main}{/}

<!-- jQuery and Bootstrap Bundle (includes Popper) -->
<script src="https://cdn.jsdelivr.net/npm/jquery@3.5.1/dist/jquery.slim.min.js" integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.1/dist/js/bootstrap.bundle.min.js" integrity="sha384-fQybjgWLrvvRgtW6bFlB7jaZrFsaBXjsOMm/tB9LTS58ONXgqbR9W8oWht/amnpF" crossorigin="anonymous"></script>

</body>
</html>
```
posts.html

```html
{#include layout}
{#main}
<main role="main" class="container">
  <h1 class="my-5 text-center">文章列表</h1>
  <div class="row">
    <div class="col-sm-12 col-lg-6 my-1">
      <div class="card">
        <div class="card-body">
          <p>
            <a class="my-5" href="#">類別一</a>
          </p>
          <h5 class="card-title">Test Title 1</h5>
          <p class="card-text">最後修改時間：2022-04-26 12:00:00</p>
          <p class="text-right mb-0">
            <a href="#" >閱讀更多</a>
          </p>
        </div>
      </div>
    </div>
    <div class="col-sm-12 col-lg-6 my-1">
      <div class="card">
        <div class="card-body">
          <p>
            <a class="my-5" href="#">類別二</a>
          </p>
          <h5 class="card-title">Test Title 2</h5>
          <p class="card-text">最後修改時間：2022-04-26 12:00:00</p>
          <p class="text-right mb-0">
            <a href="#" >閱讀更多</a>
          </p>
        </div>
      </div>
    </div>
  </div>
  <nav class="my-5">
    <ul class="pagination justify-content-center">
      <li class="page-item disabled"><a class="page-link" href="#">&laquo; 前一頁</a></li>
      <li class="page-item active"><a class="page-link" href="#">1</a></li>
      <li class="page-item disabled"><a class="page-link" href="#">後一頁 &raquo;</a></li>
    </ul>
  </nav>
</main>
{/main}
{/include}
```
HTMLMetaData.kt

```kotlin
package net.aotter.quarkus.tutorial.model.vo

data class HTMLMetaData(
    var title: String,
    var type: String,
    var description: String,
    var url: String,
    var image: String
)
```

StringExtensions.kt

```kotlin
package net.aotter.quarkus.tutorial.util

fun String.abbreviate(maxWidth: Int, abbrevMarker: String = "...") = takeIf { it.length > maxWidth }
    ?.let { "${it.take(maxWidth)}$abbrevMarker" }
    ?: this
```

PostResource.kt

```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {
    @Context
    lateinit var uriInfo: UriInfo

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData): TemplateInstance
    }

    @GET
    fun listPosts(): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        return Templates.posts(metaData)
    }
    
    private fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData{
        val url = uriInfo.baseUriBuilder
            .path(uriInfo.requestUri.path.toString())
            .build().toString()
        
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}
```
* og:url 要求沒有參數的網址，所以手動去除參數
* 考量到 description 可能太長，我們撰寫 StringExtensions 幫助我們省略超過 20 個字以後的內容
* buildHTMLMetaData 方法與注入 UriInfo 可以考慮提到 abstract class 讓所有需要的 resource 繼承，因為這個範例只有一個 resource 使用到就不做了

## Loop Section

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/dto/Page 為泛型用來承裝分頁資訊
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/vo/PostSummary 用來展示文章列表的資訊
* 修改 PostResource Template posts method 多傳入 Page<PostSummary> 讓模板渲染
* 修改 PostResource listPost method 接收 QueryParam ，包括分頁與篩選的參數
* 修改 post.html 使用 for loop 渲染資料

Page.kt
```kotlin
package net.aotter.quarkus.tutorial.model.dto

import kotlin.math.ceil

data class Page<T>(
    var list: List<T>,
    var page: Long,
    var show: Int,
    var total: Long,
    var totalPages: Long
){
    constructor(list: List<T>, page: Long, show: Int, total: Long): this(
        list, page, show, total, ceil(total.toDouble() / show).toLong()
    )
}
```
list(資料)、page(第幾頁)、show(一頁有幾筆資料)、total(總共有幾筆資料)、totalPages(總共有幾頁)

PostSummary.kt
```kotlin
package net.aotter.quarkus.tutorial.model.vo

data class PostSummary(
    var id: String,
    var title: String,
    var category: String,
    var authorName: String,
    var lastModifiedTime: String,
    var published: Boolean
)
```

PostResource.kt
```kotlin
...
@CheckedTemplate
object Templates{
    @JvmStatic
    external fun posts(metaData: HTMLMetaData, pageData: Page<PostSummary>): TemplateInstance
    @JvmStatic
    external fun postDetail(metaData: HTMLMetaData): TemplateInstance
}

@GET
fun listPosts(
    @QueryParam("category") category: String?,
    @QueryParam("authorId") authorId: String?,
    @QueryParam("page") @DefaultValue("1") page: Long,
    @QueryParam("show") @DefaultValue("6") show: Int
): TemplateInstance {
    val metaData = buildHTMLMetaData(
        title = "BLOG",
        type = "website",
        description = "BLOG 有許多好文章"
    )
    val postSummary = PostSummary(
        id = "123",
        title = "Test 1",
        category = "類別一",
        authorName = "user",
        lastModifiedTime = "2022-04-06 12:01:00",
        published = true
    )
    val pageData = Page(arrayListOf(postSummary, postSummary, postSummary, postSummary, postSummary, postSummary), page, show, 100)
    return Templates.posts(metaData, pageData)
}

...
```
* 省略其他部分只顯示重點
* 創建假資料用 Page 包裝，透過 posts 方法傳入到模板渲染

posts.html
```html
...
<div class="row">
    {#for item in pageData.list}
      <div class="col-sm-12 col-lg-6 my-1">
        <div class="card">
          <div class="card-body">
            <p>
              <a class="my-5" href="/?category={item.category}">{item.category}</a>
            </p>
            <h5 class="card-title">{item.title}</h5>
            <p class="card-text">最後修改時間：{item.lastModifiedTime}</p>
            <p class="text-right mb-0">
              <a href="/posts/{item.id}" >閱讀更多</a>
            </p>
          </div>
        </div>
      </div>
    {/for}
</div>
...
```
* 省略其他部分，只顯示重點
* loop section 可以遍歷 Iterable、Iterator、array、Map、Stream、Integer、int
* 有兩種方式使用，一種是只用 each 和 it，另一種是使用 for 別名 in iteration
* 我們這邊使用 for ，別名為 item ，遍歷 Page<PostSummary> 的 list
* 可以在 loop section 參考到個別物件
* 另外可透過 ＿訪問 iteration metadata, ex item_index 可以拿到以零為基礎遍歷的指標

## Template Extension Methods

再來我們要完成下面的頁碼，這部分比較複雜，要考慮到換頁時應該要攜帶原本的參數，還要判斷前一頁後一頁。  
首先我們要知道在模板中只有表達式，所以連簡單的計算也無法 ex {1+1} 模板會解析錯誤，那如果我們想要做簡單的計算最後在呈現出來該怎麼辦呢  
就可以透過 Template Extension Methods 達成
* 當在方法上使用 @TemplateExtension 標註，會自動產生 ValueResolver，若是標註在 class 上則所有符合的方法都會自動產生
* template extension method 不能為 private 、 必須為 static 、 回傳值不得為 void
* 沒有定義 namespace 會使用沒有標註 @TemplateAttribute 第一個參數作為匹配

Qute 有內建一些 Template Extensions ex Number 有 mod {#if counter.mod(5) == 0}  
我們先來建立一些 Template Extension Methods，方便等等使用
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/template/BasicExtensions.kt
* 在 BasicExtension 創建 static method inc 用來對 Number 的簡單加法
* 在 BasicExtension 創建 static method paginationList 用來產生等等要顯示的頁碼
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/template/RouteExtensions.kt
* 在 RouteExtensions 創建 static method toRouteBuilder 從 HttpServerRequest 取出 absoluteURI 轉成 UriBuilder 用來建構網址
* 在 RouteExtensions 創建 static method setRouteQueryParam 設定 UriBuilder 的 QueryParam

BasicExtensions.kt
```kotlin
package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import kotlin.math.max
import kotlin.math.min

@TemplateExtension
class BasicExtensions {
    companion object{
        @JvmStatic
        fun inc(number: Number, amount: Number): Long = number.toLong() + amount.toLong()

        @JvmStatic
        fun paginationList(currentPage: Number, totalPages: Number, elements: Int): List<Long>{
            val half: Int = Math.floorDiv(elements - 1 , 2)
            val leftMinus: Int = if(elements % 2 == 0) 1 else 0
            var left = (currentPage.toInt() - half - leftMinus).toLong()
            var right = (currentPage.toInt() + half).toLong()
            if(left < 1){
                right = min(totalPages.toLong(), right + (1L - left))
                left = 1
            }else if(right > totalPages.toLong()){
                left = max(1, left - (right - totalPages.toLong()))
                right = totalPages.toLong()
            }
            return listOf(left..right).flatten()
        }
    }
}
```
* 一樣使用 companion object @JvmStatic 來撰寫 static method 
* 由於沒有指定 namespace 所以 inc 方法和 paginationList 方法都會使用第一個參數來匹配
* inc 匹配表達式 Number.class 然後屬性為 inc ex {count.inc(1)}
* paginationList 是用來計算需要顯示的頁碼，currentPage(第幾頁)、totalPages(總共幾頁)、elements(最多顯示幾個頁碼)

RouteExtensions.kt
```kotlin
package net.aotter.quarkus.tutorial.util.template

import io.quarkus.qute.TemplateExtension
import io.vertx.core.http.HttpServerRequest
import javax.ws.rs.core.UriBuilder

@TemplateExtension
class RouteExtensions {
    companion object{
        @JvmStatic
        fun toRouteBuilder(request: HttpServerRequest): UriBuilder = UriBuilder.fromUri(request.absoluteURI())
        @JvmStatic
        fun setRouteQueryParam(builder: UriBuilder, name: String, value: Any): UriBuilder  {
            return  builder.replaceQueryParam(name, value)
        }
    }
}
```
* 利用 qute inject 可以取得 HttpServerRequest 之後在模板可以使用
* 為了換頁還要保留其他 QueryParam ，我們使用 UriBuilder 方便取代 page 參數

#### 最後修改我們的 post.html

```html
...
<nav class="my-5">
    {#if pageData.totalPages > 1}
    <ul class="pagination justify-content-center">

        {#if pageData.page > 1}
        <li class="page-item"><a class="page-link" href="{inject:vertxRequest.toRouteBuilder().setRouteQueryParam('page', pageData.page.inc(-1)).toTemplate()}">&laquo; 前一頁</a></li>
        {#else}
        <li class="page-item disabled"><a class="page-link" href="#">&laquo; 前一頁</a></li>
        {/if}

        {#for i in pageData.page.paginationList(pageData.totalPages,5)}

        {#if (i_index == 0) && (i > 1)}
        <li class="page-item"><a class="page-link" href="{inject:vertxRequest.toRouteBuilder().setRouteQueryParam('page', 1).toTemplate()}">1</a></li>
        {/if}

        {#if (i_index == 0) && (i > 2)}
        <li class="page-item disabled"><span class="page-link">…</span></li>
        {/if}

        <li class="page-item {#if pageData.page == i}active{/if}"><a class="page-link" href="{inject:vertxRequest.toRouteBuilder().setRouteQueryParam('page', i).toTemplate()}">{i}</a></li>

        {#if (!i_hasNext) && (i < (pageData.totalPages.inc(-1)))}
        <li class="page-item disabled"><span class="page-link">…</span></li>
        {/if}

        {#if (!i_hasNext) && (i < pageData.totalPages)}
        <li class="page-item"><a class="page-link" href="{inject:vertxRequest.toRouteBuilder().setRouteQueryParam('page', pageData.totalPages).toTemplate()}">{pageData.totalPages}</a></li>
        {/if}

        {/for}

        {#if pageData.page < pageData.totalPages}
        <li class="page-item"><a class="page-link" href="{inject:vertxRequest.toRouteBuilder().setRouteQueryParam('page', pageData.page.inc(1)).toTemplate()}">後一頁 &raquo;</a></li>
        {#else}
        <li class="page-item disabled"><a class="page-link" href="#">後一頁 &raquo;</a></li>
        {/if}

    </ul>

    {#else}
    <ul class="pagination justify-content-center">
        <li class="page-item disabled"><a class="page-link" href="#">&laquo; 前一頁</a></li>
        <li class="page-item active"><a class="page-link" href="#">1</a></li>
        <li class="page-item disabled"><a class="page-link" href="#">後一頁 &raquo;</a></li>
    </ul>
    {/if}
</nav>
...
```
* 一開始判斷只有一頁的話直接顯示
* {inject:vertxRequest} 可以取得 HttpServerRequest 再搭配我們的 toRouteBuilder() 設定網址，最後呼叫 toTemplate() 得到網址
* 使用 paginationList 搭配 for loop 渲染頁碼
* 使用 i_index 取得 for 的索引用來判斷要不要顯示首頁和略過符號
* !i_hasNext 也是一樣的道理，用來判斷要不要顯示末頁和略過符號



完成了瀏覽發布文章的頁面，接下來我們來完成發布文章詳細內容的頁面，這方便比較簡單就只是呈現資料而已
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/vo/PostDetail.kt 用來呈現發布文章詳細內容頁面
* 在 PostResource 的 Templates 新增 postDetail 方法傳入 metaData 和 postDetail
* 在 PostResource 新增方法 showPostDetail ，標註 @Path("{/id}") @GET
* 創建 src/main/resources/templates/PostResource/postDetail.html
* 一樣 include layout ，簡單拉一下畫面，將 postDetail 資訊呈現出來

PostDetail.kt
```kotlin
package net.aotter.quarkus.tutorial.model.vo

data class PostDetail(
    var category: String,
    var title: String,
    var content: String? = null,
    var authorId: String,
    var authorName: String,
    var lastModifiedTime: String
)
```

PostResource.kt
```kotlin
package net.aotter.quarkus.tutorial.resource

import io.quarkus.qute.CheckedTemplate
import io.quarkus.qute.TemplateInstance
import io.vertx.core.http.HttpServerRequest
import net.aotter.quarkus.tutorial.model.dto.Page
import net.aotter.quarkus.tutorial.model.vo.HTMLMetaData
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.util.abbreviate
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.UriInfo

@Path("/")
@Produces(MediaType.TEXT_HTML)
class PostResource {
    @Context
    lateinit var uriInfo: UriInfo

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun posts(metaData: HTMLMetaData, pageData: Page<PostSummary>): TemplateInstance
        @JvmStatic
        external fun postDetail(metaData: HTMLMetaData, postDetail: PostDetail): TemplateInstance
    }

    @GET
    fun listPosts(
        request: HttpServerRequest,

        @QueryParam("category") category: String?,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("6") show: Int
    ): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        val postSummary = PostSummary(
            id = "123",
            title = "Test 1",
            category = "類別一",
            authorName = "user",
            lastModifiedTime = "2022-04-06 12:01:00",
            published = true
        )
        val pageData = Page(arrayListOf(postSummary, postSummary, postSummary, postSummary, postSummary, postSummary), page, show, 100)
        return Templates.posts(metaData, pageData)
    }

    @Path("/posts/{postId}")
    @GET
    fun showPostDetail(): TemplateInstance {
        val metaData = buildHTMLMetaData(
            title = "BLOG-Test title 1",
            type = "article",
            description = "Test content 1"
        )
        val postDetail = PostDetail(
            category = "類別一",
            title = "Test 1",
            content = "test content",
            authorId = "user",
            authorName = "user",
            lastModifiedTime = "2022-04-06 12:01:00"
        )
        return Templates.postDetail(metaData, postDetail)
    }

    private fun buildHTMLMetaData(title: String, type: String, description: String, image: String = ""): HTMLMetaData{
        val url = uriInfo.baseUriBuilder.replaceQuery("").toTemplate()
        return HTMLMetaData(title, type, description.abbreviate(20), url, image)
    }
}
```

postDetail.html
```html
{#include layout}
{#main}
<main role="main" class="container">
    <h1 class="my-5 text-center">{postDetail.title}</h1>
    <h5>{postDetail.lastModifiedTime} by <a href="/?authorId={postDetail.authorId}">{postDetail.authorName}</a></h5>
    <p>{postDetail.content}</p>
</main>
{/}
{/}
```

完成後使用瀏覽器訪問 http://localhost:8080/posts/123 ，postId先隨便打只是要看畫面
![postDetail 畫面](../image/1-qute/postDetail.png)
可以點選超連結會發現兩邊的頁面已經連結好了，我們就完成了畫面的製作


qute 還有許多功能更詳細的介紹可以參考官方指引 [QUTE REFERENCE GUIDE](https://quarkus.io/guides/qute-reference)
