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

相較於直接使用 MongoDB Client，MongoDB with Panache 提供了 PojoCodeProvider， 你不需要自己撰寫 CodecProvider 處理 MongoDB Document 與物件的轉換。  
同時透過繼承 MongoEntity，你無需自己管理 ID。一些簡單的 CRUD 也可以夠過 PanacheQL 達成，而複雜的操作你還是可以在 Panache 中使用原生 API 並享有 PojoCodeProvider 的支援。  
在我們的情境中很適合使用 PanacheQL 來處理簡單的 CRUD。  

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
Quarkus 提供一種叫過 Dev Services 的功能，當你已經安裝好 Docker 環境，當他發現你將 extension 引入但未對其進行配置，
會自動啟動相關服務並連接至應用程式使用該服務，換句話說它讓你能夠在沒有任何設定下創建有 Dev Services 支援的資料庫。  
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
    @field: BsonProperty("_id")
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
* 使用 data class 作為 po, 因為 PojoCodecProvider 需要無參數建構子，而 data class 要產生無參數建構子需要給予所有屬性預設值
* 因為 PanacheQL 轉換 id 不會自動轉換成 ＿id (不確定是否為 bug)，暫時的解法是標註 BsonProperty 轉換就會正常

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
測試結果發現 sort 接受的欄位名稱為實際名稱

```kotlin
// 使用 id 排序，預設是升冪
postRepository.list(Sort.by("_id"))
// 可以通過第二個參數指定升降冪
postRepository.list(Sort.by("_id", Sort.by("id", Sort.Direction.Descending)))
// 可以排序多個欄位
postRepository.list(Sort.by("_id").and("createdTime"))
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

* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/util/ReflectExtensions 新增 bsonFieldName 方法取得資料庫實際的 field 名稱
* 為了實作分頁，我們需要 count 總共有幾筆資料，還有透過 find 得到 ReactivePanacheQuery 實作分頁
* 在 PostRepository 實作 countByCriteria、findByCriteria、pageDataByCriteria
* 由於我們的搜尋條件不定所以透過 Map 裝載條件參數，在使用 buildQuery 產生 PanacheQL
* 撰寫 findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished 實作文章分頁查詢

ReflectExtensions.kt
```kotlin
package net.aotter.quarkus.tutorial.util

import org.bson.codecs.pojo.annotations.BsonProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaField

fun <T, R> KProperty1<T, R>.bsonFieldName() = this.javaField
    ?.getAnnotation(BsonProperty::class.java)
    ?.let { it.value }
    ?: this.name
```

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
    fun findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(
        authorId: ObjectId?, category: String?, published: Boolean?,
        page: Long, show: Int
    ): Uni<PageData<Post>>{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorId?.let {
                put(Post::authorId.name, it)
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, it)
            }
        }
        return pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by(Post::createdTime.bsonFieldName(), Sort.Direction.Descending).and(Post::id.bsonFieldName()),
            page = page,
            show = show
        )
    }
 
    fun countByCriteria(criteria: Map<String, Any>): Uni<Long> =
        if(criteria.isEmpty()){
            count()
        } else {
            count(buildQuery(criteria), criteria)
        }  

    fun findByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id")): ReactivePanacheQuery<Post> =
        if(criteria.isEmpty()){
            findAll(sort)
        } else {
            find(buildQuery(criteria), criteria)
        }

    fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id"), page: Long, show: Int): Uni<PageData<Post>>{
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
* 在 findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished 實作動態條件查詢，沒有設定的篩選條件就不放進 criteria Map
* 想要顯示最新更新的文章，所以用 lastModifiedTime 降冪排序，若是時間相同再使用 id，都需要使用 bsonFieldName 取得真正的欄位名稱

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

    fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean? , page: Long, show: Int): Uni<PageData<PostSummary>> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()
        
        return postRepository.findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(authorId, category, published, page, show)
            .map { it.map(this::toPostSummary) }
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
* 字串和 ObjectId 轉換，發生錯誤就為 null，不加入篩選條件
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
        return postService.getExistedPostSummary(authorId, category, true, page, show)
            .map{ pageData -> Templates.posts(metaData, pageData) }
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
        val categoryList = arrayListOf("分類三","分類一","分類二")
        val posts = mutableListOf<Post>()
        for(index in 1..7){
            val post = Post(
                authorId = ObjectId("6278b21b245917288cd7220b"),
                authorName = "user",
                title = """Title $index""",
                category = categoryList[index % 3],
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

管於 Mutiny 詳細語法與用法可以參考官方網站 [SmallRye Mutiny](https://smallrye.io/smallrye-mutiny/)

## Coroutines with kotlin

#### No-arg compiler plugin
前面有提到 data class 要產生無參數建構子需要給予所有屬性預設值，而 No-arg compiler plugin 可以幫助我們對標註特定 annotation 的類別額外生成無參數建構子。  
再來我們看向 Post 的 createdTime 和 lastModifiedTime 應該是可以提出來作為抽象類別讓其他需要的 po 繼承，而 PostRepository 的方法也是可以抽象出來讓其他 repository 繼承。

* pom.xml 的 kotlin-maven-plugin 加入 dependency kotlin-maven-noarg、compilerPlugins 加上 plugin no-arg 、option 加上 no-arg:annotation=io.quarkus.mongodb.panache.common.MongoEntity
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/model/po/AuditingEntity 
* 將 createdTime 和 lastModifiedTime 提到 AuditingEntity，新增方法 beforePersistOrUpdate 處理設定 lastModifiedTime
* 修改 Post 繼承 AuditingEntity 拿到提出的欄位，加上 @MongoEntity 讓 plugin 可以產生無參數建構子，可以不需要設定預設值
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/repository/AuditingRepository 
* 將原本的方法提到 AuditingRepository 抽象類別，這次我們改為直接複寫所有使用 Entity 的儲存或更新方法
* 修改 PostRepository 繼承 AuditingRepository
* 修改 AppInitConfig 直接使用 persistOrUpdate 方法

pom.xml
```xml
...
      <plugin>
        <groupId>org.jetbrains.kotlin</groupId>
        <artifactId>kotlin-maven-plugin</artifactId>
        <version>${kotlin.version}</version>
        <executions>
          <execution>
            <id>compile</id>
            <goals>
              <goal>compile</goal>
            </goals>
          </execution>
          <execution>
            <id>test-compile</id>
            <goals>
              <goal>test-compile</goal>
            </goals>
          </execution>
        </executions>
        <dependencies>
          <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-allopen</artifactId>
            <version>${kotlin.version}</version>
          </dependency>
          <dependency>
            <groupId>org.jetbrains.kotlin</groupId>
            <artifactId>kotlin-maven-noarg</artifactId>
            <version>${kotlin.version}</version>
          </dependency>
        </dependencies>
        <configuration>
          <javaParameters>true</javaParameters>
          <jvmTarget>11</jvmTarget>
          <compilerPlugins>
            <plugin>all-open</plugin>
            <plugin>no-arg</plugin>
          </compilerPlugins>
          <pluginOptions>
            <option>all-open:annotation=javax.ws.rs.Path</option>
            <option>all-open:annotation=javax.enterprise.context.ApplicationScoped</option>
            <option>all-open:annotation=io.quarkus.test.junit.QuarkusTest</option>
            <option>no-arg:annotation=io.quarkus.mongodb.panache.common.MongoEntity</option>
          </pluginOptions>
        </configuration>
      </plugin>
...
```
* 加入 dependency kotlin-maven-noarg
* compilerPlugins 加入 plugin no-arg
* pluginOptions 加入 option no-arg:annotation=io.quarkus.mongodb.panache.common.MongoEntity 表示帶有這個 annotation 的類別都會透過 no-arg plugin 產生無參數建構子

AuditingEntity.kt
```kotlin
package net.aotter.quarkus.tutorial.model.po

import java.time.Instant

abstract class AuditingEntity {
 var lastModifiedTime: Instant? = null
 var createdTime: Instant = Instant.now()
 
 fun beforePersistOrUpdate() = (this.lastModifiedTime?.let { Instant.now() } ?: this.createdTime).also { this.lastModifiedTime = it }
}
```
* 創建抽象類別用來記錄稽核用資訊
* 創建 beforePersistOrUpdate 方法在儲存到資料庫前更新 lastModifiedTime

Post.kt
```kotlin
package net.aotter.quarkus.tutorial.model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.types.ObjectId

@MongoEntity
data class Post(
    @field: BsonProperty("_id")
    var id: ObjectId? = null,
    var authorId: ObjectId,
    var authorName: String,
    var category: String,
    var title: String,
    var content: String,
    var published: Boolean,
    var deleted: Boolean,
): AuditingEntity()
```
* 修改 Post 繼承 AuditingEntity
* 加上 @MongoEntity 標註讓 no-arg plugin 產生無參數建構子
* 不需要所有欄位都設定預設值

AuditingRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheQuery
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.AuditingEntity
import java.util.stream.Stream

abstract class AuditingRepository<Entity: AuditingEntity>: ReactivePanacheMongoRepository<Entity>{

 override fun persist(entity: Entity): Uni<Entity> {
  entity.beforePersistOrUpdate()
  return super.persist(entity)
 }

 override fun persist(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
  firstEntity.beforePersistOrUpdate()
  entities.forEach { it.beforePersistOrUpdate() }
  return super.persist(firstEntity, *entities)
 }

 override fun persist(entities: Stream<Entity>): Uni<Void> {
  entities.forEach{ it.beforePersistOrUpdate() }
  return super.persist(entities)
 }

 override fun persist(entities: Iterable<Entity>): Uni<Void> {
  entities.forEach { it.beforePersistOrUpdate() }
  return super.persist(entities)
 }

 override fun update(entity: Entity): Uni<Entity> {
  entity.beforePersistOrUpdate()
  return super.update(entity)
 }

 override fun update(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
  firstEntity.beforePersistOrUpdate()
  entities.forEach { it.beforePersistOrUpdate() }
  return super.update(firstEntity, *entities)
 }

 override fun update(entities: Stream<Entity>): Uni<Void> {
  entities.forEach { it.beforePersistOrUpdate() }
  return super.update(entities)
 }

 override fun update(entities: Iterable<Entity>): Uni<Void> {
  entities.forEach { it.beforePersistOrUpdate() }
  return super.update(entities)
 }


 override fun persistOrUpdate(entity: Entity): Uni<Entity> {
  entity.beforePersistOrUpdate()
  return super.persistOrUpdate(entity)
 }

 override fun persistOrUpdate(firstEntity: Entity, vararg entities: Entity): Uni<Void> {
  firstEntity.beforePersistOrUpdate()
  entities.forEach { it.beforePersistOrUpdate() }
  return super.persistOrUpdate(firstEntity, *entities)
 }

 override fun persistOrUpdate(entities: Stream<Entity>): Uni<Void> {
  entities.forEach { it.beforePersistOrUpdate() }
  return super.persistOrUpdate(entities)
 }

 override fun persistOrUpdate(entities: Iterable<Entity>): Uni<Void> {
  entities.forEach { it.beforePersistOrUpdate() }
  return super.persistOrUpdate(entities)
 }

 fun countByCriteria(criteria: Map<String, Any>): Uni<Long> =
  if(criteria.isEmpty()){
      count()   
  } else {
      count(buildQuery(criteria), criteria)
  }

 fun findByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id")): ReactivePanacheQuery<Entity> =
  if(criteria.isEmpty()){
      findAll(sort)   
  } else {
      find(buildQuery(criteria), criteria)   
  }

 fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id"), page: Long, show: Int): Uni<PageData<Entity>>{
  val total = countByCriteria(criteria)
  val list = findByCriteria(criteria, sort).page(page.toInt() - 1, show).list()

  return Uni.combine().all().unis(total, list).asTuple()
   .map { PageData(it.item2, page, show, it.item1) }
 }

 private fun buildQuery(criteria: Map<String, Any>): String = criteria.keys.joinToString(separator = " and ") { """$it = :$it""" }
}
```
* 創建抽象類別繼承 ReactivePanacheMongoRepository 
* 將共用的方法搬到抽象類別上
* 放棄原本的 persistOrUpdateWithAuditing 方法，這樣無法確保使用的人呼叫正確的方法，lastModifiedTime 就不會正確更新
* 改由複寫 ReactivePanacheMongoRepository 關於 entity 的 persist Or update 方法，在保存前調用 beforePersistOrUpdate
* 如果是直接呼叫 update(update: kotlin.String, params: io.quarkus.panache.common.Parameters) 等 Query 還是會發生 lastModifiedTime 需要要手動更新情形

PostRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import net.aotter.quarkus.tutorial.model.po.Post
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: AuditingRepository<Post>(){
    fun findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(
        authorId: ObjectId?, category: String?, published: Boolean?,
        page: Long, show: Int
    ): Uni<PageData<Post>>{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorId?.let {
                put(Post::authorId.name, it)
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, it)
            }
        }
        return pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by(AuditingEntity::lastModifiedTime.bsonFieldName(), Sort.Direction.Descending).and(Post::id.bsonFieldName()),
            page = page,
            show = show
       )
   }
}
```
* 只留下 findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished

AppInitConfig.kt
```kotlin
...
    private fun initPostData(){
        val categoryList = arrayListOf("分類三","分類一","分類二")
        val posts = mutableListOf<Post>()
        for(index in 1..7){
            val post = Post(
                authorId = ObjectId("6278b21b245917288cd7220b"),
                authorName = "user",
                title = """Title $index""",
                category = categoryList[index % 3],
                content = """Content $index""",
                published = true,
                deleted = false
            )
            posts.add(post)
        }
        postRepository.count()
            .subscribe().with{
                if(it == 0L){
                    postRepository.persistOrUpdate(posts)
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
...
```
改為直接調用 persistOrUpdate 方法 

#### Kotlin Coroutines
前面 Reactive 提到除了 Mutiny 還有提供另一種撰寫的方法，也就是 Kotlin Coroutines。  
Coroutines (共常式，或大陸翻譯為協程) 的特色是每個 Coroutine 可以被 Suspend(暫停) 和 Resume(回復)， 允許被暫停之後再回覆執行，而暫停的狀態被保留等到復原後再以暫停時的狀態繼續執行。  
而 Mutiny 的 mutiny-kotlin 模塊提供了與 Kotlin Coroutines 的結合， 例如再 Coroutine 或 suspend function 當中使用 awaitSuspending 等到 Uni 事件的發射。

* 修改 AuditingRepository 自訂的方法改為 suspend function 然後使用 awaitSuspending
* 修改 PostRepository 改為 suspend function
* 修改 PostService 改為 suspend function
* 修改 PostResource 改為 suspend function

AuditingRepository.kt
```kotlin
...
    suspend fun countByCriteria(criteria: Map<String, Any>): Long =
        if(criteria.isEmpty())
            count().awaitSuspending()
        else
            count(buildQuery(criteria), criteria).awaitSuspending()


    suspend fun pageDataByCriteria(criteria: Map<String, Any>, sort: Sort = Sort.by("_id", Sort.Direction.Ascending), page: Long, show: Int): PageData<Entity>{
        val total = countByCriteria(criteria)
        val list = findByCriteria(criteria, sort)
            .page(page.toInt() - 1, show)
            .list().awaitSuspending()
        return PageData(list, page, show, total)
    }
...
```
* 因為 awaitSuspending 是 suspend function 要調用 suspend function 只能在 Coroutines 或 suspend function 中使用
* 改為 suspend function 然後 awaitSuspending 直接回傳值而不是 Uni<*&gt;
* pageDataByCriteria  方法可以看到我們的寫法跟一般同步寫法一樣，但實際上他是異步執行

PostRepository.kt
```kotlin
package net.aotter.quarkus.tutorial.repository

import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.coroutines.awaitSuspending
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.po.Post
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PostRepository: AuditingRepository<Post>(){
    suspend fun findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(
        authorId: ObjectId?, category: String?, published: Boolean?,
        page: Long, show: Int
    ): PageData<Post> {
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorId?.let {
                put(Post::authorId.name, it)
            }
            category?.let {
                put(Post::category.name, it)
            }
            published?.let {
                put(Post::published.name, it)
            }
        }
        return pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by(AuditingEntity::lastModifiedTime.bsonFieldName(), Sort.Direction.Descending).and(Post::id.bsonFieldName()),
            page = page,
            show = show
        )
    }
}
```

PostService.kt
```kotlin
...
    suspend fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean?, page: Long, show: Int): PageData<PostSummary> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()

        return postRepository.findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(authorId, category, published, page, show)
            .map(this::toPostSummary)
    }
...
```

PostResource.kt
```kotlin
...
    @GET
    suspend fun listPosts(
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
        val pageData = postService.getExistedPostSummary(authorId, category, true, page, show)
        return Templates.posts(metaData, pageData)
    }
...
```

可以看到使用 Kotlin Coroutines 可以將異步操作用一般同步寫法，這樣寫起來更加簡單可讀性高，所以我們傾向使用 Kotlin Coroutines

#### 文章詳細內容

再來我們完成查看發布文章的詳細內容。

* PostRepository 創建 findOneByDeletedIsFalseAndIdAndPublished 方法
* PostService 創建 getExistedPostDetail 方法
* 修改 PostResource 調用 getExistedPostDetail 取代寫死的假資料

```kotlin
...
    suspend fun findOneByDeletedIsFalseAndIdAndPublished(
        id: ObjectId, published: Boolean?
    ): Post?{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            put(Post::id.name, id)
            published?.let {
                put(Post::published.name, it)
            }
        }
        return  findByCriteria(criteria).firstResult().awaitSuspending()
    }
...
```
* 有可能查不到

PostService.kt
```kotlin
package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostDetail
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import org.bson.types.ObjectId
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.ws.rs.NotFoundException

@ApplicationScoped
class PostService {
    @Inject
    lateinit var postRepository: PostRepository

    suspend fun getExistedPostSummary(authorIdValue: String?, category: String?, published: Boolean?, page: Long, show: Int): PageData<PostSummary> {
        val authorId = kotlin.runCatching {
            ObjectId(authorIdValue)
        }.getOrNull()

        return postRepository.findPageDataByDeletedIsFalseAndAuthorIdAndCategoryAndPublished(authorId, category, published, page, show)
            .map(this::toPostSummary)
    }

    suspend fun getExistedPostDetail(idValue: String, published: Boolean?): PostDetail{
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw NotFoundException("post detail not found")

        return postRepository.findOneByDeletedIsFalseAndIdAndPublished(id, published)
            ?.let(this::toPostDetail)
            ?: throw NotFoundException("post detail not found")
    }

    private fun toPostSummary(post: Post): PostSummary = PostSummary(
        id = post?.id.toString(),
        title = post.title ,
        category = post.category ,
        authorName = post.authorName,
        lastModifiedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime),
        published = post.published
    )

    private fun toPostDetail(post: Post): PostDetail = PostDetail(
        category = post.category,
        title =  post.title,
        content = post.content,
        authorId = post.authorId.toString(),
        authorName = post.authorName,
        lastModifiedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Taipei"))
            .withLocale(Locale.TAIWAN)
            .format(post.lastModifiedTime)
    )
}
```
* 傳進來的 id 有可能不是 ObjectId 的格式，使用 runCatching 做錯誤處理
* 查不到時丟出 NotFoundException


PostResource.kt
```kotlin
...
    @Path("/posts/{postId}")
    @GET
    suspend fun showPostDetail(
        @PathParam("postId") postId: String
    ): TemplateInstance {
        val postDetail = postService.getExistedPostDetail(postId, true)
        val metaData = buildHTMLMetaData(
            title = """"BLOG-${postDetail.title}""",
            type = "article",
            description = postDetail.content ?: ""
        )
        return Templates.postDetail(metaData, postDetail)
    }
...
```

這樣我們就完成了 Quarkus 與 MongoDB 的操作，並瞭解到 Quarkus Reactive 的寫法