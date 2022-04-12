package model.po

import io.quarkus.mongodb.panache.common.MongoEntity
import model.dto.ArticleRequest
import org.bson.types.ObjectId

@MongoEntity
data class Article(
    /**
     *  使用者（作者） UserId
     */
    var author: ObjectId? = null,

    /**
     *  使用者名稱
     */
    var authorName: String? = null,

    /**
     *  文章分類
     */
    var category: String? = null,

    /**
     *  文章標題
     */
    var title: String? = null,

    /**
     *  文章內容
     */
    var content: String? = null,

    /**
     *  是否已發佈
     */
    var published: Boolean = false,


    /**
     *  是否已刪除
     */
    var visible: Boolean = true

): BaseMongoEntity<Article>(){

    constructor(user: User, req: ArticleRequest) : this(
        author = user.id!!,
        authorName = user.username!!,
        category = req.category!!,
        title = req.title!!,
        content = req.content?:""
    )
}