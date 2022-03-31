package repository

import com.mongodb.client.model.*
import io.quarkus.mongodb.FindOptions
import io.quarkus.panache.common.Page
import io.smallrye.mutiny.coroutines.awaitSuspending
import model.dto.ArticleReq
import model.dto.PageRequest
import model.po.Article
import org.bson.conversions.Bson
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

    //TODO add publishedTime and different published Status
    /**
     * update article published status by id
     * @param id [ObjectId] of the article
     * @param published [Boolean] of the article
     */
    suspend fun updatePublishStatus(id: ObjectId, published: Boolean): Article? {
        return col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::published.name, published),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()
    }

    /**
     * update article by id
     * @param id [ObjectId] of the article
     * @param data [ArticleReq] of the article
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
     * delete article by id
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
     * get per page articleList by query
     * @param authorId
     * @param category
     * @param published
     * @param page
     * @param show
     */
    suspend fun list(authorId: ObjectId?, category: String?, published: Boolean?, page: Int = 1, show: Int) =
        find(
            buildQuery(authorId, category, published),
            PageRequest().apply {
                this.page = page
                this.show = show
            })

    /**
     * count total articles num by query
     * @param authorId
     * @param category
     * @param published
     */
    suspend fun count(authorId: ObjectId?, category: String?, published: Boolean?) =
        count(Filters.and(buildQuery(authorId, category, published)))

    private fun buildQuery(authorId: ObjectId?, category: String?, published: Boolean?): List<Bson>{
        return listOfNotNull(
            authorId?.let { Filters.eq(Article::author.name, it) },
            category?.let { Filters.eq(Article::category.name, it) },
            published?.let { Filters.eq(Article::published.name, it) },
            Filters.eq(Article::visible.name, true)
        )
    }

    /**
     * @param filters
     * @param pageReq
     */
    suspend fun find(filters: List<Bson>, pageReq: PageRequest): List<Article> {

        // reuse Page for logic check
        val checkedPage = Page.of(pageReq.page - 1 , pageReq.show)

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
}