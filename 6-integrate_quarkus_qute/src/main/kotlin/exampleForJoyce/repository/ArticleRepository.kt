package exampleForJoyce.repository

import com.mongodb.client.model.*
import exampleForJoyce.model.po.Article
import io.quarkus.mongodb.FindOptions
import io.quarkus.panache.common.Page
import io.smallrye.mutiny.coroutines.awaitSuspending
import org.bson.types.ObjectId
import repository.BaseMongoRepository
import java.time.Instant
import javax.inject.Singleton

@Singleton
class ArticleRepository : BaseMongoRepository<Article>() {

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
                    Indexes.ascending(Article::authorId.name),
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
    suspend fun publish(id: ObjectId): Article? =
        col.findOneAndUpdate(
            Filters.eq(id),
            Updates.combine(
                Updates.set(Article::published.name, true),
                Updates.set(Article::lastModifiedTime.name, Instant.now())
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        ).awaitSuspending()


    /**
     * TODO
     *
     * @param authorId
     * @param category
     * @param page
     * @param limit
     */
    suspend fun findPublished(authorId: ObjectId?, category: String?, page: Int = 1, limit: Int = 10) =
        find(authorId, category, true, page, limit)


    /**
     * TODO
     *
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
            authorId?.let { Filters.eq(Article::authorId.name, it) },
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


}