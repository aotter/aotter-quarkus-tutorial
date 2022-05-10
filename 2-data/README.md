# 2-data

## 前言
經過上一章節利用 Qute 完成畫面，一切看似很美好，但其實我們的資料是寫死在程式碼中，在這章節我們會使用知名的 NoSQL MongoDB 存放我們的資料。

## MongoDB
MongoDB 是一種 文件資料庫，與關聯性資料庫最大的差別是不需要事先定義好 Schema，因此適合資料格式常變動的應用。  
MongoDB 層級如下

* Database: 一個 MongoDB 伺服器中會有一至多個 Database，可以將不同的應用放在不同的 Database ，分別儲存資料
* Collection: 就是資料表，概念好比關聯式資料庫的 Table，一個 Database 會有許多 Collection
* Document: 就是一筆資料，概念好比關聯式資料庫的 Row，一個 Collection 會有許多 Document，才會將 MongoDB 稱為文件資料庫

#### BSON
每筆 Document 儲存是使用 BSON 格式(JSON 的二進位表示形式)

## 規劃資料
我們預計將文章資料儲存在 Database: blog，Collection: Post  

| name             | type     |
|------------------|----------|
| id               | ObjectId |
| authorId         | ObjectId |
 | authorName       | String   |
| category         | String   |
| title            | String   |
| content          | String   |
| published        | Boolean  |
| deleted          | Boolean  |
| lastModifiedTime | Instant  |
| createdTime      | Instant  |

## MongoDB with Panache for Kotlin 介紹

使用 MongoDB 原始 API 十分繁瑣，你必須將你的實體和查詢轉換為 MongoDB Document。  
MongoDB with Panache 建立在 MongoDB Client 之上，提供了活動紀錄模式 (active record pattern) 和儲存庫 (repository) 兩種方式，處理大部分的樣板程式碼讓開發者專注於業務邏輯。  
使用 PojoCodecProvider 自動轉換實體與 MongoDB Document ，有以下 annotation 客製化映射
* @BsonId: 指定你的 id field
* @BsonProperty: 序列化時指定你的 field 名稱
* @BsonIgnore: 序列化時忽略此 filed

#### 首先加入 MongoDB with Panache for Kotlin extension  
可透過以下兩種方式加入

Maven plugin
```shell
./mvnw quarkus:add-extension -Dextensions="io.quarkus:quarkus-mongodb-panache-kotlin"
```
add pom.xml dependency
```xml
<dependency>
 <groupId>io.quarkus</groupId>
 <artifactId>quarkus-mongodb-panache-kotlin</artifactId>
</dependency>
```

#### 設定 MongoDB database
在 application.properties 加上
```properties
quarkus.mongodb.connection-string = mongodb://localhost:27017
quarkus.mongodb.database = blog
```
* quarkus.mongodb.connection-string 是設定 MongoDB 連線資訊，這表示你在本機有 MongoDB 且連接埠為 27017
* quarkus.mongodb.database 設定如果你沒有使用 @MongoEntity 指定你的實體要存到哪個 Database，他將會存到 blog 這個 Database

@MongoEntity 也可以設定 collection 名稱，預設使用 class 名稱  

#### Dev Services
Quarkus 提供一種叫過 Dev Services 的功能，它讓你能夠在沒有任何設定下創建各種資料庫。  
使用方法就是不要設定 quarkus.mongodb.connection-string，這樣 Quarkus 就會在測試或開發模式時自動啟動一個 MongoDB 容器並設定連接。

我們這邊使用 Dev Services 所以將 application.properties 的 quarkus.mongodb.connection-string 設定拿掉
```properties
quarkus.mongodb.database = blog
```
啟動開發模式使用 docker container ls，確實有啟用 MongoDB 容器
```shell
docker container ls
CONTAINER ID   IMAGE                       COMMAND                  CREATED          STATUS          PORTS                      NAMES
da9563625cf9   mongo:4.4.13                "docker-entrypoint.s…"   33 minutes ago   Up 33 minutes   0.0.0.0:64210->27017/tcp   zealous_johnson
```

## Repository

* 創建 src/main/net/aotter/quarkus/tutorial/po/Post.kt ， persistent object 用來對應 MongoDB 的 collection
* 創建 src/main/net/aotter/quarkus/tutorial/repository/PostRepository.kt ,繼承 ReactivePanacheMongoRepository

Post.kt
```kotlin
package net.aotter.quarkus.tutorial.model.po

import org.bson.types.ObjectId
import java.time.Instant

data class Post (
    var id: ObjectId? = null,
    var authorId: ObjectId? = null,
    var authorName: String? = null,
    var category: String? = null,
    var title: String? = null,
    var content: String? = null,
    var published: Boolean? = null,
    var deleted: Boolean? = null,
    var lastModifiedTime: Instant? = null,
    var createdTime: Instant = Instant.now()
)
```
使用 data class 作為 po, 因為 PojoCodecProvider 需要無參數建構子，而 data class 要產生無參數建構子需要給予所有屬性預設值

PostRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.Post

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: ReactivePanacheMongoRepository<Post>{
}
```



* 因為我們要使用 Reactive 寫法，所以繼承的是帶有 Reactive 前綴的 PanacheMongoRepository
* Reactive API 使用 Mutiny 所以回傳會被 Uni 包裹 ex. Uni<Long&gt;, 下面會介紹 Reactive

#### CRUD操作

因為繼承 PanacheMongoRepository 所以基本的 CRUD 都有提供了，可以直接使用
```kotlin
val post = Post(
    authorId = ObjectId("6278b21b245917288cd7220b"),
    authorName = "user",
    title = """Title $index""", 
    category = "分類一",
    content = """Content $index""",
    published = true,
    deleted = false
)
// create
postRepository.persist(post)
// read
postRepository.findById(ObjectId(/* idString */))
// update
postRepository.update(post)
// delete
postRepository.delete(post)

// list all
postRepository.listAll()
// count
postRepository.count()
```

#### Page
不同於 list 或 stream 方法直接回傳所有資料， Panache 提供了 find 回傳值是 ReactivePanacheQuery 可以用來實作分頁
```kotlin

val query:ReactivePanacheQuery<Post> = postRepository.findAll()
// 一頁六筆資料，取出第一頁的資料 
val list: Uni<List<Post>> = query.page(0, 6).list()
```

#### Sorting
PanacheMongoRepository 所有的方法都接受參數 Sort 用來排序

```kotlin
// 使用 id 排序，預設是升冪
postRepository.list(Sort.by("id"))
// 可以通過第二個參數指定升降冪
postRepository.list(Sort.by("id", Sort.by("id", Sort.Direction.Descending)))
// 可以排序多個欄位
postRepository.list(Sort.by("id").and("createdTime"))
```
#### Query

Panache 除了可以使用 MongoDB 原生的查詢還提供了一種稱為 PanacheQL 的方法，他最後會轉換為原生的查詢。
如果你的查詢語句沒有以 { 開始他會被判定為 PanacheQL。

* <singlePropertyName&gt; 會轉成 {'singleColumnName': '?1'}
* <query$gt; 提供運算子轉換為 MongoDB 的運算子 ex. authorName = ?1 and createdTime > ?1 轉換為 { 'authorName': ?1, 'cratedTime': {'$gt': ?1} }
* Date, LocalDate, LocalDateTime, Instant 都會被轉換為 BSON Date 的 ISODate
* 參數的傳遞可以使用參數順序、Map、Parameters

```kotlin
// 查詢作者名稱為 user 的文章
postRepository.list("authorName", "user")
// 查詢作者名稱為 user 且最後修改時間大於 2022-05-01 12點的文章
// 用參數順序傳入
postRepository.list("authorName = ?1 and lastModifiedTime > ?2", "user", Instant.parse("2022-05-01T12:00:30Z"))

// Map
val params: MutableMap<Stirng, Any> = HashMap()
params.put("authorName", "user")
params.put("lastModifiedTime", Instant.parse("2022-05-01T12:00:30Z"))
postRepoistory.list("authorName = :authorName and lastModifiedTime > :lastModifiedTime", params)

// Parameters
postRepository.list("authorName = :authorName and lastModifiedTime > :lastModifiedTime",
 Parameters.with("authorName", "user").and("lastModifiedTime", Instant.parse("2022-05-01T12:00:30Z")))
```

更多詳細的語法可以參考官方指引 [SIMPLIFIED MONGODB WITH PANACHE](https://quarkus.io/guides/mongodb-panache)

## Reactive

#### 什麼是 Reactive
Reactive 是建構響應式分佈系統的準則與指引, [The Reactive Manifesto](https://www.reactivemanifesto.org/) 中提到 Reactive System 需要有以下四種特色

1. Responsive(響應式): 需要即時回應
2. Elastic(彈性): 需要適應波動 (流量會有高峰與低峰，根據負載縮放單個組件)
3. Resilient(韌性): 需要優雅的處理發生的錯誤
4. Asynchronous message passing(異步的訊息傳遞): 響應式系統的組件之間透過傳遞訊息互動(異步的消息傳遞有助於不同組建的解耦)

Quarkus 就是因此量身訂製的，它提供的功能將幫助你設計、實現和操作響應式的系統

#### Quarkus 如何啟用 Reactive
Quarkus 有響應式引擎，由 Eclipse Vert.x 和 Netty 處理非阻塞的 I/O 操作，Quarkus Extension 和應用程式代碼可以使用此引擎來完成 I/O 操作，資料庫操作、消息的接收與發送。  

#### I/O 模型
傳統應用程式使用阻塞 I/O 模型，每個 HTTP 請求都會使用一條執行序處理，當該執行序與其他遠程服務交互時會阻塞等待 I/O 結果。  
此模型易於開發但有些缺點，處理並發請求需要多個執行緒因此使用工作執行緒池，執行緒池限制了應用程式的最大並發請求，執行序對於記憶體與 CPU 都是有開銷的，大量請求將快速消耗系統資源。  
而使用非阻塞 I/O 模型可以避免這個問題，只使用少數執行緒處理許多並發 I/O ，因為只有少數執行緒所以調用遠程服務時，不能在阻塞執行緒而是安排 I/O 傳遞一個 continuation(計算續體)，也就是 I/O 完成後要執行的程式碼。

#### Reactive Programming Models
Quarkus 的架構基於非阻塞 I/O 和消息傳遞，允許支援多種響應式程式設計模型，這些模型在表達計算續體的方式都不同，主要有一下兩種方式。  
1. Mutiny
2. Coroutines with kotlin

Mutiny 是一個事件驅動的響應式程式設計函式庫，使用它可以撰寫事件驅動的程式碼，你的程式碼是接受事件和處理的管道，管道中每個階段都可以看作是一個執行續體，因為他會在上游發出事件時調用。  
Coroutines 是一種順序式撰寫異步程式碼的方法，他在 I/O 期間暫停程式碼的執行，並將剩餘程式碼註冊為執行續體。

可以參考官方指引 [GETTING STARTED WITH REACTIVE](https://quarkus.io/guides/getting-started-reactive)

## 查詢文章

我們接下來在 PostRepository 實作查詢文章分頁的功能

* 為了實作分頁，我們需要 count 總共有幾筆資料，還有透過 find 得到 ReactivePanacheQuery 實作分頁
* 在 PostRepository 實作 countByCriteria、findByCriteria、pageDataByCriteria
* 由於我們的搜尋條件不定所以透過 Map 裝載條件參數，在使用 buildQuery 產生 PanacheQL

PostRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.Post
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: ReactivePanacheMongoRepository<Post>{
    fun countByCriteria(criteria: Map<String, Any>): Uni<Long> =
        if(criteria.isEmpty())
            count()
        else
            count(buildQuery(criteria), criteria)

    fun findByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("id")): ReactivePanacheQuery<Post> =
        if(criteria.isEmpty())
            findAll(sort)
        else
            find(buildQuery(criteria), criteria)

    fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("id"), page: Long, show: Int): Uni<PageData<Post>>{
        val total = countByCriteria(criteria)
        val list = findByCriteria(criteria, sort).page(page.toInt() - 1, show).list()
        return Uni.combine().all().unis(total, list).asTuple()
            .map { PageData(it.item2, page, show, it.item1) }
    }

    fun buildQuery(criteria: Map<String, Any>): String = criteria.keys.joinToString(separator = " and ") { """$it = :$it""" }
}
```
* 回傳值都是 Mutiny 包裹的 Uni， Mutiny 只有兩種型別 Uni(只會發送單個項目或錯誤), Multi(多個項目)
* pageDataByCriteria 最後透過 Mutiny combine 可以將多個流發出的項目結合成一個聚合發送，下游收到再處理
* .map{} 是 Mutiny 提供的簡寫就等於是 uni.onItem().transform{} ，接受到發出的項目後轉換往下游發出
* 我們查詢傳遞是透過 Map，使用 buildQuery 來串接查詢條件產生 PanacheQL

#### 建立 PostService 處理業務邏輯

* PageData 創建方法 map 方便轉換 list
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/service/PostService.kt
* PostService 創建方法 getExistedPostPageData，接收 PostResource 傳入的參數處理產生 Map 
* 使用 PostRepository 查詢將回傳回來的 PageData<Post&gt; 轉換為 PageData<PostSummary&gt;
* 修改 PostResource 使用 PostService 查詢取代寫死的假資料

PageData.kt
```kotlin
package net.aotter.quarkus.tutorial.model.dto

import kotlin.math.ceil

data class PageData<T>(
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
inline fun <T, R> PageData<T>.map(transform: (T) -> R): PageData<R> = PageData(list.map(transform), page, show, total)
```

PostService.kt

```kotlin
package net.aotter.quarkus.tutorial.service

import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import kotlin.collections.HashMap

@ApplicationScoped
class PostService {
    @Inject
    lateinit var postRepository: PostRepository

    fun getExistedPostPageData(authorIdValue: String?, category: String?, published: Boolean? , page: Long, show: Int): Uni<PageData<PostSummary>> {
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorIdValue?.let {
                put(Post::authorId.name, ObjectId(it))
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, published)
            }
        }
        return postRepository.pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by("lastModifiedTime", Sort.Direction.Descending),
            page = page,
            show = show
        ).map { it.map(this::toPostSummary) }
    }
    
    private fun toPostSummary(post: Post): PostSummary = PostSummary(
        post.id.toString(),
        post.title ?: "",
        post.category ?: "",
        post.authorName ?: "",
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime),
        post.published ?: true
    )
}
```
* 由於我們要達到動態條件查詢，所以對於沒有設定的篩選條件就不放進 criteria Map
* 查詢回來的 Uni<Post&gt; 使用 Mutiny map 轉換為 Uni<PostSummary&gt;

PostResource.kt
```kotlin
...
    @GET
    fun listPosts(
        @QueryParam("category") category: String?,
        @QueryParam("authorId") authorId: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("6") show: Int
    ): Uni<TemplateInstance> {
        val metaData = buildHTMLMetaData(
            title = "BLOG",
            type = "website",
            description = "BLOG 有許多好文章"
        )
        return postService.getExistedPostPageData(authorId, category, true, page, show)
            .map{ item -> Templates.posts(metaData, item) }
    }
...
```
* 原本回傳值為 TemplateInstance 改為 Reactive 的 Uni<TemplateInstance&gt;
* 寫死的假資料改為調用 postService 的 getExistedPostPageData 方法
* 透過 Uni map 進行模板的呼叫

我們這樣就完成了與真實資料庫的互動，接下來我們查看 http://localhost:8080  
發現畫面一片空，因為現在我們的資料庫一筆資料都沒有，我們接下來完成將初始化資料新增至資料庫  

* 在 PostRepository 創建 persistOrUpdateWithAuditing 方法，在新增或更新資料時設定 lastModifiedTime
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/config/AppInitConfig
* 使用 @Startup 表示當應用程式啟動後初始化 CDI bean
* 在 AppInitConfig 創建 onStart 方法，使用 @PostConstruct 標註來做些初始化操作
* 創建 initPostData 方法新增初始化資料，並在 onStart 方法調用

PostRepository.kt
```kotlin
...
    fun persistOrUpdateWithAuditing(posts: Iterable<Post>): Uni<Void>{
        posts.forEach{ it.lastModifiedTime = it.lastModifiedTime?.let { Instant.now() } ?: it.createdTime }
        return persistOrUpdate(posts)
    }

    fun persistOrUpdateWithAuditing(post: Post): Uni<Post> {
        post.lastModifiedTime = post.lastModifiedTime?.let { Instant.now() } ?: post.createdTime
        return persistOrUpdate(post)
    }
...
```

AppInitConfig.kt
```kotlin
package net.aotter.quarkus.tutorial.config

import io.quarkus.runtime.Startup
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import javax.annotation.PostConstruct
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@Startup
@ApplicationScoped
class AppInitConfig{
    @Inject
    lateinit var postRepository: PostRepository
    @Inject
    lateinit var logger: Logger

    @PostConstruct
    fun onStart() {
        initPostData()
    }

    private fun initPostData(){
        val posts = mutableListOf<Post>()
        for(index in 1..7){
            val post = Post(
                authorId = ObjectId("6278b21b245917288cd7220b"),
                authorName = "user",
                title = """Title $index""",
                category = "分類一",
                content = """Content $index""",
                published = true,
                deleted = false
            )
            posts.add(post)
        }
        postRepository.count()
            .subscribe().with{
                if(it == 0L){
                    postRepository.persistOrUpdateWithAuditing(posts)
                        .onItemOrFailure()
                        .transform{ _, t ->
                            if(t != null){
                                logger.error("insert failed")
                            }else{
                                logger.info("""insert successful""")
                            }
                        }.subscribe().with{ /*ignore*/ }
                }
            }
    }
}
```
* 因為在開發模式時，修改程式碼應用程式會重啟，所以我們先查詢有無資料，沒有的話才新增
* 注意 Mutiny 是惰性的，在沒有訂閱時是不會執行的，所以最後要使用 subscribe().with{}

這樣就完成了初始資料的新增，再次使用瀏覽器訪問 http://localhost:8080 ,就會看到精美的七筆資料顯示出來