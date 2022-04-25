# 0-init

## Quarkus 介紹

A Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM, crafted from the best of breed Java libraries and standards.  
[Quarkus是Kubernetes原生的Java框架，由一些Java函式庫以及標準組成，專為GraalVM和HotSpot量身定做。](https://www.ithome.com.tw/news/137907)

## 事前準備

* JDK11  
* Apache Maven 3.8.1以上版本
* Docker

## 創建專案

1. [Quarkus 專案初始化工具](https://code.quarkus.io/)  

<table>
    <tr>
        <td>Group</td>
        <td>net.aotter</td>
    </tr>
    <tr>
        <td>Artifact</td>
        <td>aotter-quarkus-tutorial</td>
    </tr>
    <tr>
        <td>Build Tool</td>
        <td>Maven</td>
    </tr>
    <tr>
        <td>Version</td>
        <td>1.0.0-SNAPSHOT</td>
    </tr>
    <tr>
        <td>Java Version</td>
        <td>11</td>
    </tr>
    <tr>
        <td>Starter Code</td>
        <td>Yes</td>
    </tr>
    <tr>
        <td>Selected Extensions</td>
        <td>Kotlin, RESTEasy Reactive Jackson</td>
    </tr>
</table>

2. Maven plugin

```shell
mvn io.quarkus.platform:quarkus-maven-plugin:2.8.1.Final:create \
    -DprojectGroupId=net.aotter \
    -DprojectArtifactId=aotter-quarkus-tutorial \
    -Dextensions="kotlin,resteasy-reactive-jackson"
```
可透過以上兩種方式創建專案。  
這裡我們採用第一種，若你對 Maven 十分熟悉也可以使用第二種透過 quarkus maven plugin 創建專案。
* 選擇 Maven 為專案管理工具， 輸入必備的 Group (專案隸屬的組織或公司)、 ArtifactId (專案名稱)、 Version (專案版本號)  
* 選擇 Java 版本為 11 、 Starter Code 為 Yes (產生範例程式碼)
* 由於我們要使用 Kotlin 作爲專案開發語言，在 extensions list 加入 kotlin 使專案產生相容 Kotlin 的配置
* 為了開發 RESTful Web Services ，也要將 RESTEasy Reactive Jackson 加入 extension list，這包括 RESTEasy Reactive (JAX-RS 的實現)和 Jackson (JSON 函式庫)

## 專案結構

```
./aotter-quarkus-tutorial
├── README.md
├── mvnw
├── mvnw.cmd
├── pom.xml
└── src
    ├── main
    │   ├── docker
    │   │   ├── Dockerfile.jvm
    │   │   ├── Dockerfile.legacy-jar
    │   │   ├── Dockerfile.native
    │   │   └── Dockerfile.native-micro
    │   ├── kotlin
    │   │   └── net
    │   │       └── aotter
    │   │           └── GreetingResource.kt
    │   └── resources
    │       ├── META-INF
    │       │   └── resources
    │       │       └── index.html
    │       └── application.properties
    └── test
        └── kotlin
            └── net
                └── aotter
                    ├── GreetingResourceIT.kt
                    └── GreetingResourceTest.kt
```

#### 基本的 Maven 專案結構
* 範例的 Dockerfile 在 src/main/docker 路徑底下
* 範例的 GreetingResource.kt
* 靜態檔案映射路徑為 src/main/resources/META-INF/resources
* 預設的 landing page index.html 在應用程式執行後可透過 http://localhost:8080 訪問
* src/main/resources/application.properties 為專案設定檔

#### pom.xml
與沒有選擇 kotlin extension 相比產生的 pom.xml 有些修改
* 加入 quarkus-kotlin 到 dependencies 支援 Kotlin 的 live reload
* 加入 kotlin-stdlib-jdk8 到 dependencies
* Maven 的 build properties 設定 sourceDirectory 和 testSourceDirectory 指向 src/main/kotlin 和 src/test/kotlin
* 設定 kotlin-maven-plugin ，最重要是使用 all-open Kotlin compiler plugin ，為何需要使用是因為預設 class 從 kotlin 編譯會標記成 final ，
而有些框架需要使用到 Dynamic Proxy (動態代理)， final 不利於動態代理

詳細資訊可以參考官方指引 [USING KOTLIN](https://quarkus.io/guides/kotlin)

## Hello World

我們看向 GreetingResource.kt

```kotlin
package net.aotter

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/hello")
class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "Hello from RESTEasy Reactive"
}
```

很簡單的 RestFul API 當調用 /hello 路徑回應 "Hello RESTEasy Reactive" 文字，使用 JAX-RS annotations
* @Path 指出 resource 的 HTTP 路徑
* @GET HTTP 的 method

現在可以執行我們的應用程式

```shell
./mvnw quarkus:dev
```

執行成功的畫面

```
$ ./mvnw quarkus:dev
[INFO] Invoking org.apache.maven.plugins:maven-compiler-plugin:3.8.1:testCompile) @ aotter-quarkus-tutorial
[INFO] Changes detected - recompiling the module!
Listening for transport dt_socket at address: 5005
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2022-04-25 15:31:06,225 INFO  [io.quarkus] (Quarkus Main Thread) aotter-quarkus-tutorial 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.8.1.Final) started in 0.820s. Listening on: http://localhost:8080

2022-04-25 15:31:06,233 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2022-04-25 15:31:06,234 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cdi, kotlin, resteasy-reactive, resteasy-reactive-jackson, smallrye-context-propagation, vertx]
2022-04-25 15:31:42,361 INFO  [io.qua.dep.dev.RuntimeUpdatesProcessor] (vert.x-worker-thread-0) Files changed but restart not needed - notified extensions in: 0.005s 

--
Tests paused
Press [r] to resume testing, [o] Toggle test output, [:] for the terminal, [h] for more options>
```

調用 http://localhost:8080/hello ，觀察到回傳文字

```
curl http://localhost:8080/hello

Hello RESTEasy Reactive
```

不需要使用 curl 可以直接透過瀏覽器發出  GET 請求

## 開發模式

quarkus:dev 會執行 Quarkus 的開發模式使用熱重載，這表示當你修改文件時他會自動偵測並重新編譯，你只需重新調用 REST API 就可以看到更改過後的結果

我們修改 GreetingResource.kt 的 hello method 回傳值為 “Hello World”

```kotlin
@GET
@Produces(MediaType.TEXT_PLAIN)
fun hello() = "Hello World"
```

再次調用 http://localhost:8080/hello 就可以發現回傳文字改變

```
curl http://localhost:8080/hello

Hello World
```

## 測試應用程式

再來是 GreetingResourceTest.kt

```kotlin
package net.aotter

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class GreetingResourceTest {

    @Test
    fun testHelloEndpoint() {
        given()
          .`when`().get("/hello")
          .then()
             .statusCode(200)
             .body(`is`("Hello from RESTEasy Reactive"))
    }

}
```

使用 QuarkusTest runner， testHelloEndpoint method 會檢查   HTTP 回傳碼和內容，測試使用 RestAssured

```
Press [r] to re-run, [o] Toggle test output, [:] for the terminal, [h] for more options>
```
按下 r 鍵就會看到 Quarkus 自動執行測試，然後發現測試沒有通過

```
2022-04-25 17:42:26,501 ERROR [io.qua.test] (Test runner thread) >>>>>>>>>>>>>>>>>>>> 1 TEST FAILED <<<<<<<<<<<<<<<<<<<<


--
1 test failed (0 passing, 0 skipped), 1 test was run in 185ms. Tests completed at 17:42:26 due to changes to GreetingResource.class.
Press [r] to re-run, [o] Toggle test output, [:] for the terminal, [h] for more options>

```

那是因為我們剛剛更改了 GreetingResource.hello() 回傳值，我們只要修改測試的 body 值就好

```kotlin
@Test
fun testHelloEndpoint() {
    given()
        .`when`().get("/hello")
        .then()
        .statusCode(200)
        .body(`is`("Hello World"))
}
```

再次執行測試你會發現測試通過

```
All 1 test is passing (0 skipped), 1 test was run in 169ms. Tests completed at 17:48:19.
Press [r] to re-run, [o] Toggle test output, [:] for the terminal, [h] for more options>
```

除了 Quarkus 的開發模式，你也可以透過 Maven 執行測試

```shell
./mvnw test
```