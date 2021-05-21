# Init
### 說明
初始化專案

### 流程
1. 建立專案
* 使用 [Quarkus 專案初始化工具](https://code.quarkus.io/)
  設定：
    * Group : aotter.net
    * Artifact : aotter-quarkus-tutorial
    * Build Tool : Maven
    * Example Code : No, thanks
    * extensions :
        * Alternative languages : Kotlin
        * Data : MongoDB with Panache for Kotlin
        * Web : RESTEasy reactive Jackson, SmallRye OpenAPI
2. 加入當前必要 dependency
    * [vertx-lang-kotlin-coroutines](https://mvnrepository.com/artifact/io.vertx/vertx-lang-kotlin-coroutines)
    * [mutiny-kotlin](https://mvnrepository.com/artifact/io.smallrye.reactive/mutiny-kotlin)

