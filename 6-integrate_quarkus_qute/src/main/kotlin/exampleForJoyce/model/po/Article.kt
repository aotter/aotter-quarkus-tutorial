package exampleForJoyce.model.po

import exampleForJoyce.model.dto.ArticleReq
import io.quarkus.mongodb.panache.common.MongoEntity
import model.po.BaseMongoEntity
import model.po.User
import org.bson.types.ObjectId

@MongoEntity
data class Article(

    var authorId: ObjectId,

    var category: String?,

    var title: String,

    var content: String,

    var published: Boolean = false

) : BaseMongoEntity<Article>() {

    constructor(author: User, req: ArticleReq) : this(
        authorId = author.id!!, // validate not null in resource (controller)
        category = req.category,
        title = req.title!!, // validate not null in resource (controller)
        content = req.content
    )

}