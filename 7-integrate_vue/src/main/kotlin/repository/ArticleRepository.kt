package repository

import com.mongodb.client.model.*
import io.quarkus.mongodb.FindOptions
import io.quarkus.panache.common.Page
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.dto.ArticleReq
import model.po.Article
import org.bson.types.ObjectId
import java.time.Instant
import javax.inject.Singleton

@Singleton
class ArticleRepository: BaseMongoRepository<Article>() {

    init {
        createIndexes(
            IndexModel(
                Indexes.compoundIndex(
                    Indexes.ascending(Article::category.name),
                    Indexes.ascending(Article::published.name),
                    Indexes.descending(Article::createdTime.name)
                ),
            ),
            IndexModel(
                Indexes.compoundIndex(
                    Indexes.ascending(Article::author.name),
                    Indexes.ascending(Article::published.name),
                    Indexes.descending(Article::createdTime.name)
                )
            )
        )
    }

    /**
     * publish article by id
     * @param id [ObjectId] of the article
     */
    suspend fun publish(id: ObjectId): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::published.name, true),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * update article by id
     * @param id [ObjectId] of the article
     * @Req data[ArticleReq] of the article
     */
    suspend fun update(id: ObjectId, data: ArticleReq): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::category.name, data.category),
                Updates.set(Article::title.name, data.title),
                Updates.set(Article::content.name, data.content),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * update article by id
     * @param id [ObjectId] of the article
     */
    suspend fun delete(id: ObjectId): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::visible.name, false),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * @param authorId
     * @param category
     * @param page
     * @param limit
     */
    suspend fun findPublished(authorId: ObjectId?, category: String?, page: Int = 1, limit: Int = 10) =
        find(authorId, category, true, page, limit)


    /**
     * @param authorId
     * @param category
     * @param published
     * @param page
     * @param show
     */
    suspend fun find(authorId: ObjectId?, category: String?, published: Boolean?, page: Int = 1, show: Int = 10): List<Article> {

        // reuse Page for logic check
        val checkedPage = Page.of(page - 1 , show)

        val filters = listOfNotNull(
            published?.let { Filters.eq(Article::published.name, it) },
            authorId?.let { Filters.eq(Article::author.name, it) },
            category?.let { Filters.eq(Article::category.name, it) }
        )
        val filter = if (filters.size > 1) {
            Filters.and(filters)
        } else {
            filters.firstOrNull()
        }


        val options = FindOptions()
            .sort(Sorts.descending(Article::createdTime.name))
            .limit(checkedPage.size)
            .skip((checkedPage.index) * checkedPage.size)

        val find = filter?.let { col.find(filter, options) } ?: col.find(options)

        return find.collect().asList().awaitSuspending()
    }

    data class ArticleView(
        val id: String? = null,
        val author:String? = null,
        val authorName:String? = null,
        val category: String? = null,
        val title: String? = null,
        val content: String? = null,
        val lastModifiedTime: String? = null,
        val pageLength: Int? = 1
    )
}