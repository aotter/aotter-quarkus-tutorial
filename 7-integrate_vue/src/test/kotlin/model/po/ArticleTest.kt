package model.po

import io.quarkus.test.junit.QuarkusTest
import org.bson.Document
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

@QuarkusTest
class ArticleTest {
    @Test
    fun `convert document to article`(){
//        val doc = Document(Article::author.name, "")
//            .append(Article::authorName.name, "914")
//            .append(Article::category.name, "分類一")
//            .append(Article::title.name, "測試標題一")
//            .append(Article::content.name, "測試內容一")
//            .append(Article::enabled.name, true)
//            .append(Article::createdTime.name, Date())
//            .append(Article::lastModifiedTime.name, Date())
//
//        val article = Article.documentToArticle(doc)
//        Assertions.assertNotNull(article.author)
//        Assertions.assertNotNull(article.category)
//        Assertions.assertNotNull(article.title)
//        Assertions.assertNotNull(article.content)
//        Assertions.assertNotNull(article.enabled)
//        Assertions.assertNotNull(article.createdTime)
//        Assertions.assertNotNull(article.lastModifiedTime)
    }
}