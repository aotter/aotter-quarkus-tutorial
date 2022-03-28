package model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import model.dto.ArticleReq
import org.bson.Document
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

@MongoEntity
data class Article(
    /**
     *  使用者（作者） UserId
     */
    var author: ObjectId,

    /**
     *  使用者名稱
     */
    var authorName: String,

    /**
     *  文章分類
     */
    var category: String,

    /**
     *  文章標題
     */
    var title: String,

    /**
     *  文章內容
     */
    var content: String,

    /**
     *  是否已發佈
     */
    var published: Boolean = false,


    /**
     *  是否已刪除
     */
    var visible: Boolean = true

): BaseMongoEntity<Article>(){

    constructor(author: User, req: ArticleReq) : this(
        author = author.id!!,
        authorName = author.username!!,
        category = req.category!!,
        title = req.title!!,
        content = req.content?:""
    )
}