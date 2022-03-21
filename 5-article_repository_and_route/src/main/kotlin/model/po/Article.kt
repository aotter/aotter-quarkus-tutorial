package model.po

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.mongodb.panache.MongoEntity
import org.bson.Document
import org.bson.types.ObjectId
import security.Role
import java.time.Instant
import java.util.*

class Article: Document(){
    var id: ObjectId? = null

    var author: String? = null
        set(value) {
            field = value
            put(Article::author.name, value)
        }

    var authorName: String? = null
        set(value) {
            field = value
            put(Article::authorName.name, value)
        }

    var category: String? = null
        set(value) {
            field = value
            put(Article::category.name, value)
        }
    var title: String? = null
        set(value) {
            field = value
            put(Article::title.name, value)
        }
    var content: String? = null
        set(value) {
            field = value
            put(Article::content.name, value)
        }
    var enabled: Boolean? = true
        set(value) {
            field = value
            put(Article::enabled.name, value)
        }

    var createdTime: Instant? = null
        set(value) {
            field = value
            put(Article::createdTime.name, value)
        }

    var lastModifiedTime: Instant? = null
        set(value) {
            field = value
            put(Article::lastModifiedTime.name, value)
        }

    companion object {
        fun documentToArticle(document: Document): Article{
            val article = Article()
            article.author = document.getString(Article::author.name)
            article.authorName = document.getString(Article::authorName.name)
            article.category = document.getString(Article::category.name)
            article.title = document.getString(Article::title.name)
            article.content = document.getString(Article::content.name)
            article.enabled = document.getBoolean(Article::enabled.name)
            article.createdTime = dateToInstant(document.getDate(Article::createdTime.name))
            article.lastModifiedTime = dateToInstant(document.getDate(Article::lastModifiedTime.name))
            return article
        }

        private fun dateToInstant(date: Date?): Instant?{
            return date?.toInstant()
        }

    }

}


//    : BaseMongoEntity<Article>(){
//
//    companion object {
//        suspend fun create(userId: String, author: String, category: String, title: String, content: String):Article{
//            return Article(
//                userId = userId,
//                author = author,
//                category = category,
//                title = title,
//                content = content,
//                enabled = true
//            ).coroutineSave()
//        }
//    }
//
//    suspend fun updateArticle(category: String, title: String, content: String): Article =
//        this.apply { this.category = category }.apply {this.title = title}.apply {this.content = content}.coroutineSave()
//
//    suspend fun deleteArticle(): Article =
//        this.apply { this.enabled = false }.coroutineSave()
//}