# 4-vue

## 前言
再來我們來完成最後的文章管理，我們使用 Vue 來完成我們前端的畫面的呈現。

## 事前準備
* Node.js v10 以上

## Vue
Vue 是一款 JavaScript 的前端框架，提供了聲明式、組建化的模型幫助你開發。  
Vue 與其他前端框架相比，他是一款漸進式的框架，從靜態網頁無需編譯到 SPA 專案開發編譯，學習曲線比較平滑。

## 與 Quarkus 結合
現在 Vue 版本已經出到 Vue 3 ，且在 Vue 3 建議使用 TypeScript( JavaScript 的超集為強型別語言 )，
但在這次的範例中我們還是使用較為熟悉的 Vue 2 和 JavaScript。

我們直接使用 Vue CLI，他是 Vue 官方的建構工具，能夠讓你不要糾結配置問題，專注在應用的開發上。
我們要使用 npm 全局安裝 Vue CLI ，而 npm 是 Node.js 預設的套件管理系統（像是 Maven 之於 Java）,
所以當你安裝好 Node.js 也就安裝好 npm ，不需要另外安裝。

```shell
npm install -g @vue/cli
```

我們切換到 src/main 這個路徑
使用 vue cli 創建項目，webapp 是專案名稱
```shell
cd src/main
vue create webapp
```
他會採用互動式的介面讓你選擇，我們這邊選擇
```
❯ Default ([Vue 2] babel, eslint) 
```
再來我們透過 Vue CLI 安裝 Vue Router，Vue 提供的前端路由套件。  
我們切換到 src/main/webapp 這個路徑
```shell
vue add router
```
他會詢問你路由的模式，有 history 和 hashtag ，我們這邊使用 hashtag 所以選 n
```
? Use history mode for router? (Requires proper server setup for index fallback in production) (Y/n) n
```
使用 npm 指令執行開發模式
```shell
npm run serve
```
可以查看範例的頁面

#### 專案結構
```
├── README.md
├── babel.config.js
├── jsconfig.json
├── node_modules
├── package-lock.json
├── package.json
├── public
│   ├── favicon.ico
│   └── index.html
├── src
│   ├── App.vue
│   ├── assets
│   │   └── logo.png
│   ├── components
│   │   └── HelloWorld.vue
│   ├── main.js
│   ├── router
│   │   └── index.js
│   └── views
│       ├── AboutView.vue
│       └── HomeView.vue
└── vue.config.js
```

這是一個 npm 專案，專案結構如上
* package.json 定義相依的模組，還有描述專案資訊等等(類似 Maven pom.xml)
* node_modules 擺放專案引入的模組(類似 Maven 的 .m2)
* src 就是擺放你的原始碼

#### 結合 Quarkus
我們執行以下指令，使用 webpack 將我們的前端專案打包成靜態的 html 檔案
```shell
npm run build
```
可以看到產生 dist 資料夾，底下有 html、css、js 各種檔案，類似於我們 Maven 打包後的 target。  
為了結合 Quarkus 我們要將 webpack 打包後的檔案放到 /src/main/resources 底下對應的位置。  

* 修改 src/main/webapp/vue.config.js
* vue.config.js 是擺放關於 vue 的設定
* 設定 outputDir 為 '../resources/META-INF/resources/assets/webapp' ，將編譯後的靜態資源放到 quarkus 對應的位置
* 設定 indexPath 為 '../../../../templates/ConsoleResource/index.html' ，這是相對於 outputDir 的路徑，打包後的 html 我們把它放到 template 底下
* 設定 publicPath 為 '/assets/webapp'，表示子路徑為 /assets/webapp 也就是我們靜態資源顯示的路徑

vue.config.js
```
const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
    transpileDependencies: true,
    outputDir: '../resources/META-INF/resources/assets/webapp',
    indexPath: '../../../../templates/ConsoleResource/index.html',
    publicPath: '/assets/webapp'
})
```

再來我們創建 ConsoleResource 回應我們編譯後的 html 
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/resource/ConsoleResource

ConsoleResource.kt
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
class ConsoleResource {

    @CheckedTemplate
    object Templates{
        @JvmStatic
        external fun index(): TemplateInstance
    }

    @Path("/console/")
    @GET
    fun index(): TemplateInstance = Templates.index()
}
```
* 這裡的模板剛好就對應到我們剛剛在 vue.config.js 設定的 indexPath
* 設定 @Path 路徑

我們在 src/main/webapp 執行打包
```shell
npm run build
```
再切換到專案執行 quarkus 開發模式
```shell
./mvnw quarkus:dev
```
訪問 http://localhost:8080/console/ ，就會看到 vue 的畫面

但這樣每次修改 vue 的文件都需要手動執行 npm run build 太麻煩了，接下來我們來讓他自動化。

* 修改 package.json ，在 script 新增 watch
* 修改 pom.xml ，新增 exec-maven-plugin 執行 npm watch

package.json
```json
{
  "name": "webapp",
  "version": "0.1.0",
  "private": true,
  "scripts": {
    "serve": "vue-cli-service serve",
    "build": "vue-cli-service build",
    "lint": "vue-cli-service lint",
    "watch": "vue-cli-service build --mode=development --watch"
  },
  "dependencies": {
    "core-js": "^3.8.3",
    "vue": "^2.6.14",
    "vue-router": "^3.5.1"
  },
...
```
* 這邊新增 watch 一樣是使用 Vue CLI 打包，並設定為開發模式，並使用 watch 自動監控檔案的變化

pom.xml
```xml
...
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>exec-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>npm-watch</id>
      <goals>
        <goal>exec</goal>
      </goals>
      <configuration>
        <executable>npm</executable>
        <async>true</async>
        <arguments>
          <argument>run</argument>
          <argument>watch</argument>
        </arguments>
        <workingDirectory>${basedir}/src/main/webapp</workingDirectory>
      </configuration>
    </execution>
  </executions>
</plugin>
...
```
* 這邊新增 exec-maven-plugin，設定執行 npm 的 run watch，也就是我們剛剛在 package.js 寫的
* workingDirectory 就是我們 vue 所在的地方

這樣我們只要執行 maven ，夠過 exec-maven-plugin 會幫我們執行 npm 指令
```shell
./mvnw exec:exec@npm-watch quarkus:dev
```

除了開發模式需要先執行 npm ，當我們在執行 maven package 應該也要先執行 npm ，
我們還需要使用 frontend-maven-plugin 來幫我們完成這項工作。

* 修改 pom.xml 新增 frontend-maven-plugin 

pom.xml
```xml
...
      <plugin>
        <groupId>com.github.eirslett</groupId>
        <artifactId>frontend-maven-plugin</artifactId>
        <version>1.12.1</version>
        <configuration>
          <workingDirectory>${project.basedir}/src/main/webapp</workingDirectory>
          <installDirectory>target</installDirectory>
        </configuration>
        <executions>
          <execution>
            <id>install node and npm</id>
            <goals>
              <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
              <nodeVersion>v16.14.2</nodeVersion>
              <npmVersion>8.5.0</npmVersion>
            </configuration>
          </execution>
          <execution>
            <id>npm install</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>install</arguments>
            </configuration>
          </execution>
          <execution>
            <id>npm run build</id>
            <goals>
              <goal>npm</goal>
            </goals>
            <configuration>
              <arguments>run build</arguments>
            </configuration>
          </execution>
        </executions>
      </plugin>
...
```
* workingDirectory 一樣指定到 /src/main/webapp
* nodeVersion 和 npmVersion 指定你要的版本

現在執行打包就會透過 plugin 自動執行 npm run build
```shell
./mvnw package
```

最後我們將 .gitignore 新增我們 webpack 打包產生的檔案

.gitignore
```
# webpack build
src/main/resources/templates/ConsoleResource/index.html
src/main/resources/META-INF/resources/assets/webapp/
```
* 將 webpack 打包產生的檔案忽略掉，不要盡到版本控制裡

#### 導覽列
再來我們來完成基本的畫面，使用 Vue 完成我們原本 Qute 的 layout.html，  
但首先我們要引入以下需要的 lib
* Axios
* BootstrapVue
* Bootstrap v4.6.1

Axios 用來使用 AJAX 與後端溝通，BootstrapVue 是封裝許多 Bootstrap 的 Vue Component 函式庫，  
因為它基於 Bootstrap v4 開發，所以我們引入 Bootstrap 時要指定版本 v4.6.1

切換到 /src/main/webapp 使用 npm install 安裝上面的 lib
```shell
npm install axios bootstrap@4.6.1 bootstrap-vue
 ```

首先看向 /src/main/webapp/public/index.html   
* 我們修改 icon 從 vue 預設的 icon 換成我們的 icon
* 刪除 /src/main/webapp/public/favicon.ico 

index.html
```html
<!DOCTYPE html>
<html lang="">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1.0">
    <link rel="icon" href="<%= BASE_URL %>../images/favicon.png">
    <title><%= htmlWebpackPlugin.options.title %></title>
  </head>
  <body>
    <noscript>
      <strong>We're sorry but <%= htmlWebpackPlugin.options.title %> doesn't work properly without JavaScript enabled. Please enable it to continue.</strong>
    </noscript>
    <div id="app"></div>
    <!-- built files will be auto injected -->
  </body>
</html>
```
* 記得 BASE_URL 是我們之前設定的 publicPath 也就是 /assets/webapp ，我們照片放在 /assets/image ，所以使用相對路徑取到真正的 icon
* 這邊看到 id 為 app 的 div ，這就是我們 vue 的掛載點，之後會 mount 到這邊

接下來是 /src/main/webapp/src/main.js

* 引入 bootstrap.css 和 bootstrap-vue.css

main.js
```
import Vue from 'vue'
import App from './App.vue'
import router from './router'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
```
* 因為整個專案都要使用 bootstrap 的 css ，所以我們在這裡直接全局引用
* 而關於 bootstrap vue 只是用少數的 component ，所以不直接全局引用再使用實在引入，這樣才不會打包出太大的檔案
* 剛剛看到的 id 為 app 的 div ，看到在這邊 vue mount 它

最後是 /src/main/webapp/src/App.vue ，這是 vue 的模板

* 創建 src/main/webapp/src/components/NavbarHeader ，這是網頁上方的導覽列
* 刪除 App.vue 預設的 style
* 在 App.vue 使用 NavbarHeader component 取代原本的 nav

```html
<template>
    <header>
        <b-navbar toggleable="md" type="dark" variant="dark">
        <b-navbar-brand href="/">首頁</b-navbar-brand>

        <b-navbar-toggle target="nav-collapse"></b-navbar-toggle>

        <b-collapse id="nav-collapse" is-nav>
            <b-navbar-nav>
            <b-nav-item to="/">文章管理</b-nav-item>
            </b-navbar-nav>

            <!-- Right aligned nav items -->
            <b-navbar-nav class="ml-auto">
            <b-nav-item href="/logout">登出</b-nav-item>
            </b-navbar-nav>
        </b-collapse>
        </b-navbar>
    </header>
</template>
<script>

import Vue from 'vue'
import { NavbarPlugin } from 'bootstrap-vue'
Vue.use(NavbarPlugin)

export default {
    name: 'NavbarHeader'
}
</script>
```
* 首先這邊我們引入 NavbarPlugin
* 在 BootstrapVue 中有些組件是由多個組件組合而成的，這時就會提供 plugin 讓你不用一一引入
* 引入 vue ，讓他使用 NavbarPlugin
* template 部分就是使用對應的組件，由於我們設定管理後台是登入後才能使用，所以這邊也不用判斷狀態，直接顯示登入後的導覽列
* 注意關於 b-nav-item 的 to，與一般連結的 href 不同，to 是基於 vue router 的路徑

App.vue
```html
<template>
  <div id="app">
    <navbar-header />
    <router-view/>
  </div>
</template>
<script>
import NavbarHeader from '@/components/NavbarHeader.vue'
export default {
  name: 'App',
  components: {
    NavbarHeader
  }
}
</script>
```
* 看到我們 import 使用 @ ，這是定義在 /src/main/webapp/jsconfig.json 裡，對應到路徑 /src/main/webapp/src 

再來我們還要修改 quarkus 部分來與 vue 對接

* 修改 ConsoleResource 的 index 方法，加上 @Authenticated，表示需要登入後才能訪問頁面
* 修改 layout.html 在導覽列加上文章管理的連結

ConsoleResource.kt
```kotlin
...
    @Authenticated
    @Path("/console/")
    @GET
    fun index(): TemplateInstance = Templates.index()
...
```

layout.html
```html
...
        <div class="collapse navbar-collapse" id="navbarCollapse">
            <ul class="navbar-nav mr-auto">
                {#if securityContext.isAuthenticated()}
                <li class="nav-item">
                    <a class="nav-link" href="/console/">文章管理</a>
                </li>
                {/if}
            </ul>
            <ul class="navbar-nav ml-auto">
                {#if !securityContext.isAuthenticated()}
                <li class="nav-item">
                    <a class="nav-link" href="/login">登入</a>
                </li>
                {/if}
                {#if securityContext.isAuthenticated()}
                <li class="nav-item">
                    <a class="nav-link" href="/logout">登出</a>
                </li>
                {/if}
            </ul>
        </div>
...
```

現在我們在登入後就會顯示文章管理的連結，並且點擊後連結到 vue 的頁面

## 文章管理
我們接下來要完成文章管理的功能，規劃是登入後可以到文章管理頁面，使用者會顯示自己的文章，管理原則是顯示所有的文章，  
每篇文章都可以修改、發布下架、刪除，還能夠創建新的文章，所以規劃會有以下三個頁面。

* /console/#/ 顯示文章頁面
* /console/#/posts/:id 修改文章
* /console/#/posts 創建新的文章

#### 顯示文章頁面
首先我們先來完成顯示文章頁面，我們看向 /src/main/webapp/src/router/index.js，  
會發現目前有兩個預設的路由 / 和 /about，這兩個路由的設定有些許不一樣，  
我們看向說明，/about 的用法會使用懶加載，使用懶加載能夠縮減第一次載入網站的等待時間，所以我們之後都採用這種設定。

現在每一頁顯示的標題都是預設的專案名稱，我們需要在不同頁面設定不同的標題，
此時就可以使用 vue router 提供的導航守衛，搭配 router 的 meta 設定我們的標題。

* 修改 src/main/webapp/src/router/index.js ，設定標題還有使用導航守衛再換頁前更改網頁標題
* 在 index.js 拿掉 移除所有有關 AboutView.vue，並刪除它
* 修改 src/main/webapp/src/view/HomeView.vue

index.js
```
import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta:{
        title: '文章管理'
    }
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
      document.title = `BLOG-${to.meta.title}`
  }
  next();
})

export default router
```
* 在 meta 設定標題名稱 title
* 使用 beforeEach 在前端路由切換前改變 html 的 title

HomeView.html
```html
<template>
  <main role="main" class="container">
    <h1 class="my-5 text-center">{{ $route.meta.title }}</h1>

  </main>
</template>

<script>

export default {
  name: 'HomeView',
  components: {
  }
}
</script>
```
* 拿掉原本預設的 HelloWorld.vue 並且刪除
* 使用 $router 取得路由的 meta

#### 文章 API
再來我們來完成文章的 API，規劃如下
* /api/posts GET ，查詢自己的文章分頁，有 authorId 、category 篩選
* /api/posts/:id DELETE ，刪除自己的文章
* /api/posts/:id/published PUT ，發布或下架自己的文章

#### 查詢自己文章
首先我們先完成查詢自己文章的 API
* 創建 /src/main/kotlin/net/aotter/quarkus/tutorial/resource/api/PostManageApiResource.kt
* 新增方法 listSelfPost 接受參數使用者名稱、分類、頁數、顯示幾筆


```kotlin
package net.aotter.quarkus.tutorial.resource.api

import io.quarkus.security.Authenticated
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

@Authenticated
@Path("/api/post-manage")
class PostManageApiResource {

    @GET
    suspend fun listSelfPost(
        @Context securityContext: SecurityContext,
        @QueryParam("authorName") authorName: String?,
        @QueryParam("category") category: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("10") show: Int
    ): ApiResponse<PageData<PostSummary>> {

        return ApiResponse("成功")
    }
}
```
* 加上 @Authenticated 表示這個 API 需要登入
* 回傳 PageData<PostSummary> 分頁後的文章摘要

由於我們文章管理當不同角色會有不同的權限，我們使用 Interface 再依不同角色實作。
* 修改 PostRepository 新增 findPageDataByDeletedIsFalseAndAuthorNameAndCategory 方法
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/service/PostManageService.kt
* 新增方法 getSelfPostSummary 處理取得自己的文章摘要
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/service/AdminPostManageService.kt 繼承 PostManageService
* 實作 getSelfPostSummary 方法
* 創建 src/main/kotlin/net/aotter/quarkus/tutorial/service/UserPostManageService.kt 繼承 PostManageService
* 實作 getSelfPostSummary 方法
* 修改 PostManageApiResource 注入 AdminPostManageService 和 UserPostManageService，新增方法 getPostManageServiceByRole 透過角色決定使用哪個實作
* 修改 PostManageApiResource 的 listSelfPost 方法調用 PostServiceManage

PostRepository.kt
```kotlin
...
    suspend fun findPageDataByDeletedIsFalseAndAuthorNameAndCategory(
        authorName: String?, category: String?,
        page: Long, show: Int): PageData<Post>{
        val criteria = HashMap<String, Any>().apply {
            put(Post::deleted.name, false)
            authorName?.let {
                put(Post::authorName.name, it)
            }
            category?.let {
                put(Post::category.name, it)
            }
        }
        return pageDataByCriteria(
            criteria = criteria,
            sort = Sort.by(AuditingEntity::createdTime.bsonFieldName(), Sort.Direction.Descending).and(Post::id.bsonFieldName()),
            page = page,
            show = show
        )
    }
...
```

PostManageService.kt
```kotlin
package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.PostSummary

interface PostManageService {
    suspend fun getSelfPostSummary(
        username: String,
        category: String?, authorName: String?,
        page: Long, show: Int): PageData<PostSummary>
}
```

AdminPostManageService.kt
```kotlin
package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Named

@Named("AdminPostManageService")
@ApplicationScoped
class AdminPostManageService: PostManageService {
    @Inject
    lateinit var postRepository: PostRepository

    override suspend fun getSelfPostSummary(
        username: String,
        category: String?,
        authorName: String?,
        page: Long,
        show: Int
    ): PageData<PostSummary> {

        return postRepository.findPageDataByDeletedIsFalseAndAuthorNameAndCategory(
            authorName = authorName,
            category = category,
            page = page,
            show = show
        ).map { this.toPostSummary(it) }
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
}
```
* 由於有兩個實作需要使用 @Named("AdminPostManageService") 區分，之後注入也要使用 @Named("AdminPostManageService") 指定
* 由於管理員可以查看所有人的文章，需要篩選時可以透過 authorName 參數

```kotlin
package net.aotter.quarkus.tutorial.service

import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.dto.map
import net.aotter.quarkus.tutorial.model.po.Post
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.repository.PostRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.inject.Named

@Named("UserPostManageService")
@ApplicationScoped
class UserPostMangeService: PostManageService {
    @Inject
    lateinit var postRepository: PostRepository

    override suspend fun getSelfPostSummary(
        username: String,
        category: String?,
        authorName: String?,
        page: Long,
        show: Int
    ): PageData<PostSummary> {
        return postRepository.findPageDataByDeletedIsFalseAndAuthorNameAndCategory(
            authorName = username,
            category = category,
            page = page,
            show = show
        ).map { this.toPostSummary(it) }
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
}
```
* 這裡一樣也要使用 @Named("UserPostManageService") 註記
* 由於使用者只能查詢自己的文章，這裡查詢的 authorName 直接丟進 username


```kotlin
package net.aotter.quarkus.tutorial.resource.api

import io.quarkus.security.Authenticated
import net.aotter.quarkus.tutorial.model.dto.PageData
import net.aotter.quarkus.tutorial.model.vo.ApiResponse
import net.aotter.quarkus.tutorial.model.vo.PostSummary
import net.aotter.quarkus.tutorial.security.Role
import net.aotter.quarkus.tutorial.service.PostManageService
import javax.inject.Inject
import javax.inject.Named
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext

@Authenticated
@Path("/api/post-manage")
class PostManageApiResource {

    @Inject
    @field:Named("UserPostManageService")
    lateinit var userPostManageService: PostManageService

    @Inject
    @field:Named("AdminPostManageService")
    lateinit var adminPostManageService: PostManageService

    @GET
    suspend fun listSelfPost(
        @Context securityContext: SecurityContext,
        @QueryParam("authorName") authorName: String?,
        @QueryParam("category") category: String?,
        @QueryParam("page") @DefaultValue("1") page: Long,
        @QueryParam("show") @DefaultValue("10") show: Int
    ): ApiResponse<PageData<PostSummary>> {
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        val result = postManageService.getSelfPostSummary(username, category, authorName, page, show)
        return ApiResponse("成功", result)
    }

    private fun getPostManageServiceByRole(securityContext: SecurityContext) =
        if (securityContext.isUserInRole(Role.ADMIN_VALUE)) {
            adminPostManageService
        } else {
            userPostManageService
        }
}
```
* 這裡注入兩個不同的實作，透過 @Named 區分哪個
* 創建 getPostManageServiceByRole 方法透過角色取得實作
* 在 listSelfPost 調用 PostManageService


#### 接下來仿造剛剛的流程依序完成刪除自己的文章、發布或下架自己的文章
* 修改 PostManageService 新增 publishSelfPost、deleteSelfPost 方法
* 修改 AdminPostManageService 和 UserPostManageService 實作上面兩個方法
* 修改 PostManageApiResource 新增 publishSelfPost 和 deleteSelfPost 兩個 API 調用 PostManageService

PostManageService.kt
```kotlin
...
    suspend fun publishSelfPost(username: String, idValue: String, status: Boolean)

    suspend fun deleteSelfPost(username: String, idValue: String)
...
```

AdminPostManageService.kt
```kotlin
...
    override suspend fun publishSelfPost(username: String, idValue: String, status: Boolean) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted){
            throw BusinessException("無此文章")
        }
        post.published = status
        postRepository.update(post).awaitSuspending()
    }

    override suspend fun deleteSelfPost(username: String, idValue: String) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted){
            throw BusinessException("無此文章")
        }
        post.deleted = true
        postRepository.update(post).awaitSuspending()
    }
...
```

UserPostManageService.kt
```kotlin
...
    override suspend fun publishSelfPost(username: String, idValue: String, status: Boolean) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted || post.authorName != username){
            throw BusinessException("無此文章")
        }
        post.published = status
        postRepository.update(post).awaitSuspending()
    }

    override suspend fun deleteSelfPost(username: String, idValue: String) {
        val id = kotlin.runCatching {
            ObjectId(idValue)
        }.getOrNull() ?: throw BusinessException("無此文章")
        val post = postRepository.findById(id).awaitSuspending() ?: throw BusinessException("無此文章")
        if(post.deleted || post.authorName != username){
            throw BusinessException("無此文章")
        }
        post.deleted = true
        postRepository.update(post).awaitSuspending()
    }
...
```
* 需要多判斷是不是操作自己的文章

PostManageApiResource.kt
```kotlin
...
    @PUT
    @Path("/{id}/published")
    suspend fun publishSelfPost(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String,
        @Valid request: PublishPostRequest
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.publishSelfPost(username, id, request.status)
        return ApiResponse("成功")
    }

    @DELETE
    @Path("/{id}")
    suspend fun deleteSelfPost(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String
    ): ApiResponse<Unit>{
        val postManageService = getPostManageServiceByRole(securityContext)
        val username = securityContext.userPrincipal.name
        postManageService.deleteSelfPost(username, id)
        return ApiResponse("成功")
    }
...
```

#### 再來我們要來完成前端畫面的呈現，但在開始之前我們先撰寫一些工具完成對 API 的調用
* 創建 src/main/webapp/src/util/request.js 用來封裝 axios 完成一些全局的錯誤處理
* 創建 src/main/webapp/src/api/post-manage.js 完成對 API 的調用

request.js
```
import axios from 'axios'

const service = axios.create({
    baseURL: '/',
    timeout: 5000
})

service.interceptors.response.use(
    response => {
        return response.data
    },
    error => {
        if(error.response.data.title === 'Constraint Violation'){
            Promise.reject(error)
        }
        alert(error.response.data.message)
        if(error.response.status === 401){
            window.location.href = '/login'
        }
    }
)

export default service
```
* 這裡封裝 axios 當錯誤時統一處理
* error.response.data.title === 'Constraint Violation' 表示 hibernate-validator 錯誤個別處理
* 401 錯誤表示未登入導引使用者到登入頁
* 其他錯誤就跳 alert 通知使用者

post-manage.js
```
import request from '@/util/request'

export function fetchPostSummary(authorName, category, page, show){
    var url = `/api/post-manage?page=${page}&show=${show}`
    if(authorName !== null){
        url += `&authorName=${authorName}`
    }
    if(category !== null){
        url += `&category=${category}`
    }
    return request({
        url: url,
        method: 'get'
    })
}

export function publishPost(id, status){
    return request({
        url: `/api/post-manage/${id}/published`,
        method: 'put',
        data: {
            'status': status
        }
    })
}

export function deletePost(id){
    return request({
        url: `/api/post-manage/${id}`,
        method: 'delete'
    })
}
```

#### 再來我們來完成我們的畫面
* 使用 BootstrapVue 的 BTable, BButton, BPagination 完成畫面
* 規劃 data 需要的資料

HomeView.vue
```
<template>
  <main role="main" class="container">
    <h1 class="my-5 text-center">{{ $route.meta.title }}</h1>
        <b-button 
      class="mb-2" 
      variant="warning"
    >新增文章</b-button>

    <b-table 
      :items="items" 
      :fields="fields" 
      responsive="lg"
    >
          
    </b-table>
    <div>
      <b-pagination
        align="center"
        prev-text="« 前一頁"
        next-text="後一頁 »"
        v-model="page"
        :total-rows="total"
        :per-page="show"
        first-number
        last-number
      ></b-pagination>
    </div>
  </main>
</template>

<script>
import { BTable, BButton, BPagination } from "bootstrap-vue";

export default {
  name: 'HomeView',
  components: {
    BTable,
    BButton,
    BPagination
  },
  data(){
    return {
      fields:[
        {
          key: "title",
          label: "標題",
        },
        {
          key: "category",
          label: "分類",
        },
        {
          key: "authorName",
          label: "作者名稱",
        },
        {
          key: "lastModifiedTime",
          label: "更新時間",
        },
        {
          key: "action",
          label: "操作",
        }
      ],
      items:[],
      page: 1,
      show: 6,
      total: 0,
      authorName: null,
      category: null
    }
  }
}
</script>
```
* fields 就是表格的表頭
* items 就是分頁後的資料
* page，show，total 是分頁需要的資料
* authorName 和 category 是篩選條件

完成實際調用 API
* 使用 watch 監控路由，完成篩選條件和分頁與網址參數的綁定
* 新增 loadData 方法調用 API 取得資料
* 新增 changePage 方法切換路由完成換頁
* 新增 publishPost 方法調用 API 發布或下架文章
* 新增 deletePost 方法調用 API 刪除文章
* 使用 Scoped field slots 客製化 Table
```
<template>
  <main role="main" class="container">
    <h1 class="my-5 text-center">{{ $route.meta.title }}</h1>
    <b-button 
      class="mb-2" 
      variant="warning"
      :to="`/posts`"
    >新增文章</b-button>

    <b-table 
      :items="items" 
      :fields="fields" 
      responsive="lg"
    >
      <template #cell(category)="data">
        <b-link :to="`/?category=${data.item.category}&show=${show}`">{{data.item.category}}</b-link>
      </template>

      <template #cell(action)="data">
        <b-button 
          variant="primary" 
          :to="`/posts/${data.item.id}`"
        >編輯</b-button>

        <b-button 
          variant="info"
          @click="publishPost(data.item.id, !data.item.published)"
        >{{data.item.published ? "下架" : "發布"}}
        </b-button>
        
        <b-button 
          @click="deletePost(data.item.id)"
          variant="danger"
        >刪除</b-button>
      </template>
    </b-table>
    <div>
      <b-pagination
        align="center"
        prev-text="« 前一頁"
        next-text="後一頁 »"
        v-model="page"
        :total-rows="total"
        :per-page="show"
        first-number
        last-number
        @change="changePage"
      ></b-pagination>
    </div>
  </main>
</template>

<script>
import { BTable, BButton, BPagination, BLink } from "bootstrap-vue";
import { fetchPostSummary, publishPost, deletePost } from "@/api/post-manage";

export default {
  name: 'HomeView',
  components: {
    BTable,
    BButton,
    BPagination,
    BLink
  },
  data(){
    return {
      fields:[
        {
          key: "title",
          label: "標題",
        },
        {
          key: "category",
          label: "分類",
        },
        {
          key: "authorName",
          label: "作者名稱",
        },
        {
          key: "lastModifiedTime",
          label: "更新時間",
        },
        {
          key: "action",
          label: "操作",
        }
      ],
      items:[],
      page: 1,
      show: 6,
      total: 0,
      authorName: null,
      category: null
    }
  },
  watch:{
    '$route': {
      handler: function(item){
        const query = item.query
   
        if(query.authorName !== undefined){
          this.authorName = query.authorName
        }else{
          this.authorName = null
        }

        if(query.category !== undefined){
          this.category = query.category
        }else{
          this.category = null
        }

        if(query.page !== undefined){
          this.page = query.page
        }else{
          this.page = 1
        }

        if(query.show !== undefined){
          this.show = query.show
        }else{
          this.show = 6
        }

        this.loadData()
      },
      deep: true,
      immediate: true
    }
  },
  methods:{
    loadData: function(){
      fetchPostSummary(this.authorName, this.category, this.page, this.show).then(res => {
        this.items = res.data.list;
        this.total = res.data.total;
      })
    },
    changePage: function(page){
      const query = Object.assign({}, this.$route.query)
      query.page = page
      this.$router.push({query: query})
    },
    publishPost: function(id, status){
      publishPost(id, status).then(res => {
        alert(res.message)
        this.loadData()
      })
    },
    deletePost: function(id){
      deletePost(id).then(res => {
        alert(res.message)
        this.loadData()
      })
    }
  }
}
</script>
```
* BLink 和 BButton 的 to 會走 Vue Router 跟 html tag a 的 href 不一樣
* 新增文章和編輯文章會轉到到編輯頁面之後實作
* 發布或下架和刪除按鈕綁定 click 事件，使用對應的 method 調用 API 最後再使用 loadData 重新取得更新後的資料
* watch 監控路由，當路由改變時會改變 data 那對應的資料再次觸發 loadData 重新取得資料

